package mk.ukim.finki.etnomk.web;

import mk.ukim.finki.etnomk.model.Record;
import mk.ukim.finki.etnomk.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/records")
public class RecordController {

    private final RecordService recordService;
    private final RegionService regionService;
    private final CategoryService categoryService;
    private final MaterialService materialService;
    private final TechniqueService techniqueService;
    private final ImageService imageService;

    public RecordController(RecordService recordService, RegionService regionService,
                            CategoryService categoryService, MaterialService materialService,
                            TechniqueService techniqueService, ImageService imageService) {
        this.recordService = recordService;
        this.regionService = regionService;
        this.categoryService = categoryService;
        this.materialService = materialService;
        this.techniqueService = techniqueService;
        this.imageService = imageService;
    }

    @GetMapping
    public ResponseEntity<List<Record>> getAll() {
        return ResponseEntity.ok(recordService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return recordService.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Record>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(recordService.searchRecords(keyword));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Record>> filter(
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) Long categoryId) {

        List<Record> results = recordService.findAll();

        if (regionId != null) {
            results = results.stream()
                    .filter(r -> r.getRegion().getRegionId().equals(regionId))
                    .toList();
        }
        if (categoryId != null) {
            results = results.stream()
                    .filter(r -> r.getCategory().getCategoryId().equals(categoryId))
                    .toList();
        }
        return ResponseEntity.ok(results);
    }

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> create(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam Long regionId,
            @RequestParam Long categoryId,
            @RequestParam(required = false) Long materialId,
            @RequestParam(required = false) Long techniqueId,
            @RequestParam(required = false) MultipartFile image) {

        try {
            Record record = new Record();
            record.setTitle(title);
            record.setDescription(description);
            record.setDateCreated(LocalDate.now());
            record.setRegion(regionService.findById(regionId).orElse(null));
            record.setCategory(categoryService.findById(categoryId).orElse(null));

            if (materialId != null) {
                record.setMaterial(materialService.findById(materialId).orElse(null));
            }
            if (techniqueId != null) {
                record.setTechnique(techniqueService.findById(techniqueId).orElse(null));
            }

            Record saved = recordService.createRecord(record);

            if (image != null && !image.isEmpty()) {
                imageService.uploadImage(image, saved);
            }

            return ResponseEntity.ok(recordService.findById(saved.getRecordId()).orElse(saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/records/{id}/similar
     * Returns up to 5 records visually similar to the record's primary image.
     */
    @GetMapping("/{id}/similar")
    public ResponseEntity<?> getSimilar(
            @PathVariable Long id,
            @RequestParam(defaultValue = "5") int limit) {
        return recordService.findById(id)
                .<ResponseEntity<?>>map(record ->
                        ResponseEntity.ok(imageService.findSimilarRecords(id, limit))
                )
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/records/similar-by-image
     * Accepts a multipart image upload and returns records visually similar to it.
     * No authentication required — anyone can do a pattern search.
     */
    @PostMapping("/similar-by-image")
    public ResponseEntity<?> getSimilarByImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam(defaultValue = "5") int limit) {

        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No image provided"));
        }
        try {
            List<Record> similar = imageService.findSimilarByUpload(image, limit);
            return ResponseEntity.ok(similar);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Similarity search failed: " + e.getMessage()));
        }
    }
}