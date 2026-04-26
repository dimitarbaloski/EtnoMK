package mk.ukim.finki.etnomk.service.impl;

import mk.ukim.finki.etnomk.model.Image;
import mk.ukim.finki.etnomk.model.Record;
import mk.ukim.finki.etnomk.repository.ImageRepository;
import mk.ukim.finki.etnomk.service.ImageService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

@Service
public class ImageServiceImpl implements ImageService {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;
    private static final String UPLOAD_DIR = "/app/uploads/";
    private static final String SIMILARITY_SERVICE_URL = "http://similarity:5000/embed";

    private final ImageRepository imageRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
            float[] embedding = getEmbedding(file);
            image.setEmbedding(embedding);
        } catch (Exception e) {
            System.err.println("[EtnoMK] Could not get embedding (similarity service may be down): " + e.getMessage());
        }

        return imageRepository.save(image);
    }

    @Override
    public float[] getEmbedding(MultipartFile file) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        String boundary = "----Boundary" + System.currentTimeMillis();
        byte[] fileBytes = file.getBytes();
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";

        String bodyStart = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"image\"; filename=\"" + fileName + "\"\r\n" +
                "Content-Type: image/jpeg\r\n\r\n";
        String bodyEnd = "\r\n--" + boundary + "--\r\n";

        byte[] start = bodyStart.getBytes();
        byte[] end = bodyEnd.getBytes();
        byte[] body = new byte[start.length + fileBytes.length + end.length];
        System.arraycopy(start, 0, body, 0, start.length);
        System.arraycopy(fileBytes, 0, body, start.length, fileBytes.length);
        System.arraycopy(end, 0, body, start.length + fileBytes.length, end.length);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SIMILARITY_SERVICE_URL))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Similarity service returned HTTP " + response.statusCode() + ": " + response.body());
        }

        JsonNode json = objectMapper.readTree(response.body());
        JsonNode embeddingNode = json.get("embedding");

        float[] embedding = new float[embeddingNode.size()];
        for (int i = 0; i < embeddingNode.size(); i++) {
            embedding[i] = (float) embeddingNode.get(i).asDouble();
        }
        return embedding;
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
        if (images.isEmpty() || images.get(0).getEmbedding() == null) {
            return List.of();
        }

        String vectorStr = toVectorString(images.get(0).getEmbedding());

        return imageRepository.findSimilar(recordId, vectorStr, limit)
                .stream()
                .map(Image::getRecord)
                .filter(r -> r != null)
                .distinct()
                .toList();
    }

    @Override
    public List<Record> findSimilarByUpload(MultipartFile file, int limit)
            throws IOException, InterruptedException {
        float[] embedding = getEmbedding(file);
        String vectorStr = toVectorString(embedding);

        // Use recordId = -1 so nothing is excluded (no real record has that ID)
        return imageRepository.findSimilar(-1L, vectorStr, limit)
                .stream()
                .map(Image::getRecord)
                .filter(r -> r != null)
                .distinct()
                .toList();
    }
}