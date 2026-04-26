package mk.ukim.finki.etnomk.service.impl;

import mk.ukim.finki.etnomk.model.Image;
import mk.ukim.finki.etnomk.model.Record;
import mk.ukim.finki.etnomk.repository.ImageRepository;
import mk.ukim.finki.etnomk.service.ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
public class ImageServiceImpl implements ImageService {
    private static final Logger log = LoggerFactory.getLogger(ImageServiceImpl.class);

    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;
    private static final int MIN_PATTERN_SEARCH_WIDTH = 128;
    private static final int MIN_PATTERN_SEARCH_HEIGHT = 128;
    private static final String UPLOAD_DIR = "/app/uploads/";
    private static final String SIMILARITY_SERVICE_URL = "http://similarity:5000/embed";
    private static final double MAX_SIMILARITY_DISTANCE = 0.22;

    private final ImageRepository imageRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public ImageServiceImpl(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Override
    public Image saveImage(Image image) {
        return imageRepository.save(image);
    }

    @Override
    public List<Image> getImagesByRecord(Long recordId) {
        return imageRepository.findByRecord_RecordId(recordId);
    }

    private BufferedImage resizeImage(BufferedImage originalImage) {
        java.awt.Image tmp = originalImage.getScaledInstance(WIDTH, HEIGHT, java.awt.Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }

    @Override
    public Image uploadImage(MultipartFile file, Record record) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        BufferedImage resizedImage = resizeImage(originalImage);

        String filename = UUID.randomUUID() + ".jpg";
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File outputFile = new File(UPLOAD_DIR + filename);
        ImageIO.write(resizedImage, "jpg", outputFile);

        Image image = new Image();
        image.setImagePath("/uploads/" + filename);
        image.setRecord(record);

        try {
            float[] embedding = requestEmbedding(
                    Files.readAllBytes(outputFile.toPath()),
                    filename,
                    "image/jpeg"
            );
            image.setEmbedding(embedding);
        } catch (Exception e) {
            log.warn("Could not generate embedding for uploaded image {}: {}", filename, e.getMessage());
        }

        return imageRepository.save(image);
    }

    @Override
    public float[] getEmbedding(MultipartFile file) throws IOException, InterruptedException {
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";
        String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";
        return requestEmbedding(file.getBytes(), fileName, contentType);
    }

    /** Build the "[v1,v2,...]" string pgvector expects from a float array. */
    private String toVectorString(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            sb.append(embedding[i]);
            if (i < embedding.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public List<Record> findSimilarRecords(Long recordId, int limit) {
        List<Image> images = imageRepository.findByRecord_RecordId(recordId);
        if (images.isEmpty()) {
            return List.of();
        }

        float[] embedding = null;
        for (Image image : images) {
            embedding = ensureEmbedding(image);
            if (embedding != null) {
                break;
            }
        }

        if (embedding == null) {
            log.info("No usable embedding found for record {}", recordId);
            return List.of();
        }

        String vectorStr = toVectorString(embedding);

        List<Record> matches = imageRepository.findSimilar(recordId, vectorStr, MAX_SIMILARITY_DISTANCE, limit)
                .stream()
                .map(Image::getRecord)
                .filter(r -> r != null)
                .distinct()
                .toList();

        if (!matches.isEmpty()) {
            return matches;
        }

        int backfilled = backfillMissingEmbeddings();
        if (backfilled == 0) {
            return List.of();
        }

        log.info("Backfilled {} missing embeddings while searching for records similar to record {}", backfilled, recordId);

        return imageRepository.findSimilar(recordId, vectorStr, MAX_SIMILARITY_DISTANCE, limit)
                .stream()
                .map(Image::getRecord)
                .filter(r -> r != null)
                .distinct()
                .toList();
    }

    @Override
    public List<Record> findSimilarByUpload(MultipartFile file, int limit)
            throws IOException, InterruptedException {
        validatePatternSearchImageSize(file);
        float[] embedding = getEmbedding(file);
        String vectorStr = toVectorString(embedding);

        // Use recordId = -1 so nothing is excluded (no real record has that ID)
        return imageRepository.findSimilar(-1L, vectorStr, MAX_SIMILARITY_DISTANCE, limit)
                .stream()
                .map(Image::getRecord)
                .filter(r -> r != null)
                .distinct()
                .toList();
    }

    private void validatePatternSearchImageSize(MultipartFile file) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            throw new IllegalArgumentException("The uploaded file is not a valid image.");
        }

        if (image.getWidth() < MIN_PATTERN_SEARCH_WIDTH || image.getHeight() < MIN_PATTERN_SEARCH_HEIGHT) {
            throw new IllegalArgumentException(
                    "Pattern search images must be at least "
                            + MIN_PATTERN_SEARCH_WIDTH + "x" + MIN_PATTERN_SEARCH_HEIGHT
                            + " pixels. Uploaded image was "
                            + image.getWidth() + "x" + image.getHeight() + "."
            );
        }
    }

    @Override
    public int backfillMissingEmbeddings() {
        int updated = 0;

        for (Image image : imageRepository.findByEmbeddingIsNull()) {
            if (refreshStoredImageEmbedding(image)) {
                updated++;
            }
        }

        return updated;
    }

    @Override
    public int reindexAllEmbeddings() {
        int updated = 0;

        for (Image image : imageRepository.findAll()) {
            if (refreshStoredImageEmbedding(image)) {
                updated++;
            }
        }

        return updated;
    }

    private Path resolveStoredImagePath(String imagePath) {
        String normalized = imagePath.startsWith("/uploads/")
                ? imagePath.substring("/uploads/".length())
                : imagePath;
        return Path.of(UPLOAD_DIR, normalized);
    }

    private float[] ensureEmbedding(Image image) {
        if (image.getEmbedding() != null) {
            return image.getEmbedding();
        }

        if (refreshStoredImageEmbedding(image)) {
            return image.getEmbedding();
        }

        return null;
    }

    private boolean refreshStoredImageEmbedding(Image image) {
        if (image.getImagePath() == null || image.getImagePath().isBlank()) {
            return false;
        }

        try {
            Path path = resolveStoredImagePath(image.getImagePath());
            if (!Files.exists(path)) {
                log.warn("Cannot generate embedding because image file is missing: {}", path);
                return false;
            }

            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "image/jpeg";
            }

            float[] embedding = requestEmbedding(
                    Files.readAllBytes(path),
                    path.getFileName().toString(),
                    contentType
            );
            image.setEmbedding(embedding);
            imageRepository.save(image);
            return true;
        } catch (Exception e) {
            log.warn("Failed to generate missing embedding for image {}: {}", image.getImageId(), e.getMessage());
            return false;
        }
    }

    private float[] requestEmbedding(byte[] fileBytes, String fileName, String contentType)
            throws IOException, InterruptedException {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        ByteArrayResource resource = new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };
        HttpHeaders partHeaders = new HttpHeaders();
        partHeaders.setContentType(MediaType.parseMediaType(contentType));
        parts.add("image", new HttpEntity<>(resource, partHeaders));

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(parts, requestHeaders);

        ResponseEntity<String> response = restTemplate.exchange(
                SIMILARITY_SERVICE_URL,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IOException("Similarity service returned HTTP "
                    + response.getStatusCode().value() + ": " + response.getBody());
        }

        JsonNode json = objectMapper.readTree(response.getBody());
        JsonNode embeddingNode = json.get("embedding");
        if (embeddingNode == null || !embeddingNode.isArray()) {
            throw new IOException("Similarity service response did not contain a valid embedding array");
        }

        float[] embedding = new float[embeddingNode.size()];
        for (int i = 0; i < embeddingNode.size(); i++) {
            embedding[i] = (float) embeddingNode.get(i).asDouble();
        }
        return embedding;
    }
}
