package mk.ukim.finki.etnomk.service.impl;

import mk.ukim.finki.etnomk.model.Image;
import mk.ukim.finki.etnomk.model.ImagePatternPatch;
import mk.ukim.finki.etnomk.model.Record;
import mk.ukim.finki.etnomk.repository.ImagePatternPatchRepository;
import mk.ukim.finki.etnomk.repository.ImageRepository;
import mk.ukim.finki.etnomk.service.ImageService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.core.task.TaskExecutor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageServiceImpl implements ImageService {
    private static final Logger log = LoggerFactory.getLogger(ImageServiceImpl.class);

    private record PatternPatchEmbedding(int x, int y, int width, int height, float[] embedding) {}

    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;
    private static final int MIN_PATTERN_SEARCH_WIDTH = 128;
    private static final int MIN_PATTERN_SEARCH_HEIGHT = 128;
    private static final String SIMILARITY_SERVICE_URL = "http://similarity:5000/embed";
    private static final String PATCH_SIMILARITY_SERVICE_URL = "http://similarity:5000/embed-patches";

    /**
     * Maximum cosine distance to be considered "similar".
     * DINOv2-base produces rich 1536-dim embeddings — 0.30 gives a good
     * balance between recall (not missing genuine matches) and precision
     * (not returning unrelated records). Tune up/down if needed.
     */
    private static final double MAX_SIMILARITY_DISTANCE = 0.10;
    private static final double MAX_PATTERN_PATCH_DISTANCE = 0.11;
    private static final double SAME_REGION_DISTANCE_BOOST = 0.002;

    private final ImageRepository imageRepository;
    private final ImagePatternPatchRepository imagePatternPatchRepository;
    private final Path uploadDir;
    private final TaskExecutor taskExecutor;
    private final TransactionTemplate transactionTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public ImageServiceImpl(ImageRepository imageRepository,
                            ImagePatternPatchRepository imagePatternPatchRepository,
                            @Qualifier("embeddingTaskExecutor") TaskExecutor taskExecutor,
                            TransactionTemplate transactionTemplate,
                            @Value("${etnomk.upload-dir:uploads}") String uploadDir) {
        this.imageRepository = imageRepository;
        this.imagePatternPatchRepository = imagePatternPatchRepository;
        this.taskExecutor = taskExecutor;
        this.transactionTemplate = transactionTemplate;
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    // ── Public API ────────────────────────────────────────────────────────

    @Override
    public Image saveImage(Image image) {
        return imageRepository.save(image);
    }

    @Override
    public List<Image> getImagesByRecord(Long recordId) {
        return imageRepository.findByRecord_RecordId(recordId);
    }

    @Override
    @Transactional
    public Image uploadImage(MultipartFile file, Record record) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IllegalArgumentException("The uploaded file is not a valid image.");
        }
        BufferedImage resizedImage = resizeImage(originalImage);

        String filename = UUID.randomUUID() + ".jpg";
        Files.createDirectories(uploadDir);

        Path outputPath = uploadDir.resolve(filename);
        if (!ImageIO.write(resizedImage, "jpg", outputPath.toFile())) {
            throw new IOException("Could not save uploaded image.");
        }

        Image image = new Image();
        image.setImagePath("/uploads/" + filename);
        image.setRecord(record);

        byte[] imageBytes = Files.readAllBytes(outputPath);
        Image savedImage = imageRepository.save(image);
        scheduleEmbeddingGeneration(savedImage.getImageId(), imageBytes, filename, "image/jpeg");

        return savedImage;
    }

    @Override
    public float[] getEmbedding(MultipartFile file) throws IOException, InterruptedException {
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";
        String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";
        return requestEmbedding(file.getBytes(), fileName, contentType);
    }

    /**
     * Find records similar to a stored record, with region-aware re-ranking.
     *
     * Steps:
     *   1. Get the embedding for the query record.
     *   2. Fetch candidates within MAX_SIMILARITY_DISTANCE from pgvector.
     *   3. Rank by visual distance, with a small same-region boost for close ties.
     */
    @Override
    @Transactional
    public List<Record> findSimilarRecords(Long recordId, int limit) {
        List<Image> images = imageRepository.findByRecord_RecordId(recordId);
        if (images.isEmpty()) return List.of();

        float[] embedding = null;
        Long queryRegionId = null;

        for (Image image : images) {
            embedding = ensureEmbedding(image);
            if (embedding != null) {
                // Grab the query region so same-region candidates can be preferred.
                if (image.getRecord() != null && image.getRecord().getRegion() != null) {
                    queryRegionId = image.getRecord().getRegion().getRegionId();
                }
                break;
            }
        }

        if (embedding == null) {
            log.info("No usable embedding found for record {}", recordId);
            return List.of();
        }

        String vectorStr = toVectorString(embedding);

        int candidateLimit = Math.max(limit * 4, 40);
        List<Image> candidates = findCandidates(recordId, vectorStr, candidateLimit);

        if (candidates.isEmpty()) {
            int backfilled = backfillMissingEmbeddings();
            if (backfilled == 0) return List.of();
            candidates = findCandidates(recordId, vectorStr, candidateLimit);
        }

        return reRankByRegion(candidates, queryRegionId, vectorStr, limit);
    }

    /**
     * Find records similar to an uploaded image, with optional region hint.
     *
     * @param file       the uploaded query image
     * @param limit      max results to return
     * @param regionId   optional region hint; pass null to rank by pattern only
     */
    @Override
    public List<Record> findSimilarByUpload(MultipartFile file, int limit)
            throws IOException, InterruptedException {
        return findSimilarByUpload(file, limit, null);
    }

    @Transactional(readOnly = true)
    public List<Record> findSimilarByUpload(MultipartFile file, int limit, Long regionId)
            throws IOException, InterruptedException {
        validatePatternSearchImageSize(file);
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";
        String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";
        List<PatternPatchEmbedding> queryPatches = requestPatchEmbeddings(file.getBytes(), fileName, contentType);

        if (queryPatches.isEmpty()) {
            return List.of();
        }

        int candidateLimit = Math.max(limit * 10, 80);
        return findSimilarRecordsByPatternPatches(queryPatches, regionId, candidateLimit, limit);
    }

    private List<Image> findCandidates(Long recordId,
                                       String vectorStr,
                                       int candidateLimit) {
        return imageRepository.findSimilar(
                recordId,
                vectorStr,
                MAX_SIMILARITY_DISTANCE,
                candidateLimit
        );
    }

    private List<Record> findSimilarRecordsByPatternPatches(List<PatternPatchEmbedding> queryPatches,
                                                            Long regionId,
                                                            int candidateLimit,
                                                            int limit) {
        class PatchScore {
            private final Record record;
            private double bestDistance;
            private int matchCount;

            private PatchScore(Record record, double bestDistance) {
                this.record = record;
                this.bestDistance = bestDistance;
                this.matchCount = 1;
            }

            private void addMatch(double distance) {
                matchCount++;
                if (distance < bestDistance) bestDistance = distance;
            }
        }

        Map<Long, PatchScore> bestByRecord = new LinkedHashMap<>();

        for (PatternPatchEmbedding queryPatch : queryPatches) {
            String vectorStr = toVectorString(queryPatch.embedding());

            List<ImagePatternPatch> matches = imagePatternPatchRepository.findSimilarPatches(
                    -1L,
                    vectorStr,
                    MAX_PATTERN_PATCH_DISTANCE,
                    candidateLimit
            );

            for (ImagePatternPatch patch : matches) {
                if (patch.getImage() == null || patch.getImage().getRecord() == null) continue;

                Record record = patch.getImage().getRecord();
                Long recordId = record.getRecordId();
                if (recordId == null) continue;

                double distance = cosineDist(patch.getEmbedding(), queryPatch.embedding());
                PatchScore currentScore = bestByRecord.get(recordId);

                if (currentScore == null) {
                    bestByRecord.put(recordId, new PatchScore(record, distance));
                } else {
                    currentScore.addMatch(distance);
                }
            }
        }

        int minimumMatches = queryPatches.size() > 1 ? 2 : 1;

        return bestByRecord.values().stream()
                .filter(score -> score.matchCount >= minimumMatches)
                .sorted(Comparator.comparingDouble(score -> boostedDistance(
                        score.bestDistance,
                        isSameRegion(score.record, regionId)
                )))
                .map(score -> score.record)
                .limit(limit)
                .toList();
    }

    // ── Region re-ranking ─────────────────────────────────────────────────

    /**
     * Re-rank a visual candidate list with region preference.
     *
     * The database query fetches visual candidates globally. This method keeps
     * the final result distinct by record and ordered by visual distance, with
     * a small same-region boost for close matches.
     */
    private List<Record> reRankByRegion(List<Image> candidates,
                                        Long queryRegionId,
                                        String vectorStr,
                                        int limit) {
        record ScoredRecord(Record record, double distance, boolean sameRegion) {}

        List<ScoredRecord> scored = new ArrayList<>();
        float[] queryVector = toFloatArray(vectorStr);

        for (Image img : candidates) {
            Record rec = img.getRecord();
            if (rec == null) continue;

            // Cosine distance already came back ordered from pgvector,
            // but we need the actual value for region-aware ordering.
            // We approximate it using the dot product of stored embeddings
            // since all vectors are L2-normalised: distance ≈ 1 - dot(a,b).
            double distance = cosineDist(img.getEmbedding(), queryVector);

            boolean sameRegion = queryRegionId != null
                    && rec.getRegion() != null
                    && queryRegionId.equals(rec.getRegion().getRegionId());

            scored.add(new ScoredRecord(rec, distance, sameRegion));
        }

        Comparator<ScoredRecord> comparator = Comparator.comparingDouble(
                score -> boostedDistance(score.distance(), score.sameRegion())
        );

        Map<Long, Record> uniqueByRecord = new LinkedHashMap<>();
        scored.stream()
                .sorted(comparator)
                .forEach(s -> {
                    Long id = s.record().getRecordId();
                    if (id != null) uniqueByRecord.putIfAbsent(id, s.record());
                });

        return uniqueByRecord.values().stream()
                .limit(limit)
                .toList();
    }

    private boolean isSameRegion(Record record, Long queryRegionId) {
        return queryRegionId != null
                && record != null
                && record.getRegion() != null
                && queryRegionId.equals(record.getRegion().getRegionId());
    }

    private double boostedDistance(double distance, boolean sameRegion) {
        if (!sameRegion) return distance;
        return Math.max(0.0, distance - SAME_REGION_DISTANCE_BOOST);
    }

    // ── Admin / maintenance ───────────────────────────────────────────────

    @Override
    @Transactional
    public int backfillMissingEmbeddings() {
        int updated = 0;
        for (Image image : imageRepository.findAll()) {
            boolean needsWholeImageEmbedding = image.getEmbedding() == null;
            boolean needsPatchEmbeddings = image.getImageId() != null
                    && !imagePatternPatchRepository.existsByImage_ImageId(image.getImageId());

            if ((needsWholeImageEmbedding || needsPatchEmbeddings) && refreshStoredImageEmbedding(image)) {
                updated++;
            }
        }
        return updated;
    }

    @Override
    @Transactional
    public int reindexAllEmbeddings() {
        int updated = 0;
        for (Image image : imageRepository.findAll()) {
            if (refreshStoredImageEmbedding(image)) updated++;
        }
        return updated;
    }

    // ── Internals ─────────────────────────────────────────────────────────

    private BufferedImage resizeImage(BufferedImage originalImage) {
        java.awt.Image tmp = originalImage.getScaledInstance(WIDTH, HEIGHT, java.awt.Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }

    private void validatePatternSearchImageSize(MultipartFile file) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) throw new IllegalArgumentException("The uploaded file is not a valid image.");
        if (image.getWidth() < MIN_PATTERN_SEARCH_WIDTH || image.getHeight() < MIN_PATTERN_SEARCH_HEIGHT) {
            throw new IllegalArgumentException(
                    "Pattern search images must be at least "
                            + MIN_PATTERN_SEARCH_WIDTH + "x" + MIN_PATTERN_SEARCH_HEIGHT
                            + " pixels. Uploaded image was "
                            + image.getWidth() + "x" + image.getHeight() + ".");
        }
    }

    private String toVectorString(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            sb.append(embedding[i]);
            if (i < embedding.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private float[] toFloatArray(String vectorStr) {
        String inner = vectorStr.substring(1, vectorStr.length() - 1);
        String[] parts = inner.split(",");
        float[] arr = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            arr[i] = Float.parseFloat(parts[i].trim());
        }
        return arr;
    }

    /** Cosine distance between two L2-normalised vectors: 1 - dot(a, b). */
    private double cosineDist(float[] a, float[] b) {
        if (a == null || b == null || a.length != b.length) return 1.0;
        double dot = 0;
        for (int i = 0; i < a.length; i++) dot += a[i] * b[i];
        return 1.0 - dot;
    }

    private float[] ensureEmbedding(Image image) {
        if (image.getEmbedding() != null) return image.getEmbedding();
        if (refreshStoredImageEmbedding(image)) return image.getEmbedding();
        return null;
    }

    private boolean refreshStoredImageEmbedding(Image image) {
        if (image.getImagePath() == null || image.getImagePath().isBlank()) return false;
        try {
            Path path = resolveStoredImagePath(image.getImagePath());
            if (!Files.exists(path)) {
                log.warn("Cannot generate embedding — image file missing: {}", path);
                return false;
            }
            String contentType = Files.probeContentType(path);
            if (contentType == null) contentType = "image/jpeg";
            byte[] imageBytes = Files.readAllBytes(path);

            float[] embedding = requestEmbedding(
                    imageBytes,
                    path.getFileName().toString(),
                    contentType
            );
            image.setEmbedding(embedding);
            Image savedImage = imageRepository.save(image);
            replacePatternPatches(
                    savedImage,
                    requestPatchEmbeddings(imageBytes, path.getFileName().toString(), contentType)
            );
            return true;
        } catch (Exception e) {
            log.warn("Failed to generate embedding for image {}: {}", image.getImageId(), e.getMessage());
            return false;
        }
    }

    private Path resolveStoredImagePath(String imagePath) {
        String normalized = imagePath.startsWith("/uploads/")
                ? imagePath.substring("/uploads/".length())
                : imagePath;
        return uploadDir.resolve(normalized).normalize();
    }

    private void scheduleEmbeddingGeneration(Long imageId, byte[] imageBytes, String filename, String contentType) {
        Runnable task = () -> taskExecutor.execute(
                () -> generateEmbeddingsForImage(imageId, imageBytes, filename, contentType)
        );

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run();
        }
    }

    private void generateEmbeddingsForImage(Long imageId, byte[] imageBytes, String filename, String contentType) {
        try {
            float[] embedding = requestEmbedding(imageBytes, filename, contentType);
            transactionTemplate.executeWithoutResult(status ->
                    imageRepository.findById(imageId).ifPresent(image -> {
                        image.setEmbedding(embedding);
                        imageRepository.save(image);
                    })
            );
        } catch (Exception e) {
            log.warn("Could not generate embedding for uploaded image {}: {}", filename, e.getMessage());
        }

        try {
            List<PatternPatchEmbedding> patches = requestPatchEmbeddings(imageBytes, filename, contentType);
            transactionTemplate.executeWithoutResult(status ->
                    imageRepository.findById(imageId).ifPresent(image -> replacePatternPatches(image, patches))
            );
        } catch (Exception e) {
            log.warn("Could not generate pattern patches for uploaded image {}: {}", filename, e.getMessage());
        }
    }

    private void replacePatternPatches(Image image, List<PatternPatchEmbedding> patchEmbeddings) {
        if (image.getImageId() == null || patchEmbeddings.isEmpty()) return;

        imagePatternPatchRepository.deleteByImage_ImageId(image.getImageId());

        List<ImagePatternPatch> patches = patchEmbeddings.stream()
                .map(patchEmbedding -> {
                    ImagePatternPatch patch = new ImagePatternPatch();
                    patch.setImage(image);
                    patch.setX(patchEmbedding.x());
                    patch.setY(patchEmbedding.y());
                    patch.setWidth(patchEmbedding.width());
                    patch.setHeight(patchEmbedding.height());
                    patch.setEmbedding(patchEmbedding.embedding());
                    return patch;
                })
                .toList();

        imagePatternPatchRepository.saveAll(patches);
    }

    private float[] requestEmbedding(byte[] fileBytes, String fileName, String contentType)
            throws IOException, InterruptedException {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        ByteArrayResource resource = new ByteArrayResource(fileBytes) {
            @Override public String getFilename() { return fileName; }
        };
        HttpHeaders partHeaders = new HttpHeaders();
        partHeaders.setContentType(MediaType.parseMediaType(contentType));
        parts.add("image", new HttpEntity<>(resource, partHeaders));

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        ResponseEntity<String> response = restTemplate.exchange(
                SIMILARITY_SERVICE_URL,
                HttpMethod.POST,
                new HttpEntity<>(parts, requestHeaders),
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

    private List<PatternPatchEmbedding> requestPatchEmbeddings(byte[] fileBytes, String fileName, String contentType)
            throws IOException, InterruptedException {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        ByteArrayResource resource = new ByteArrayResource(fileBytes) {
            @Override public String getFilename() { return fileName; }
        };
        HttpHeaders partHeaders = new HttpHeaders();
        partHeaders.setContentType(MediaType.parseMediaType(contentType));
        parts.add("image", new HttpEntity<>(resource, partHeaders));

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        ResponseEntity<String> response = restTemplate.exchange(
                PATCH_SIMILARITY_SERVICE_URL,
                HttpMethod.POST,
                new HttpEntity<>(parts, requestHeaders),
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IOException("Similarity service returned HTTP "
                    + response.getStatusCode().value() + ": " + response.getBody());
        }

        JsonNode json = objectMapper.readTree(response.getBody());
        JsonNode patchesNode = json.get("patches");
        if (patchesNode == null || !patchesNode.isArray()) {
            throw new IOException("Similarity service response did not contain a valid patches array");
        }

        List<PatternPatchEmbedding> patches = new ArrayList<>();
        for (JsonNode patchNode : patchesNode) {
            JsonNode embeddingNode = patchNode.get("embedding");
            if (embeddingNode == null || !embeddingNode.isArray()) continue;

            float[] embedding = new float[embeddingNode.size()];
            for (int i = 0; i < embeddingNode.size(); i++) {
                embedding[i] = (float) embeddingNode.get(i).asDouble();
            }

            patches.add(new PatternPatchEmbedding(
                    patchNode.path("x").asInt(),
                    patchNode.path("y").asInt(),
                    patchNode.path("width").asInt(),
                    patchNode.path("height").asInt(),
                    embedding
            ));
        }

        return patches;
    }
}
