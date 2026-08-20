package mk.ukim.finki.etnomk.web;

import mk.ukim.finki.etnomk.model.Record;
import mk.ukim.finki.etnomk.service.*;
import mk.ukim.finki.etnomk.service.impl.ImageServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.StringUtils;
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
    private final ImageServiceImpl imageService;

    public RecordController(RecordService recordService, RegionService regionService,
                            CategoryService categoryService, MaterialService materialService,
                            TechniqueService techniqueService, ImageServiceImpl imageService) {
        this.recordService = recordService;
        this.regionService = regionService;
        this.categoryService = categoryService;
        this.materialService = materialService;
        this.techniqueService = techniqueService;
        this.imageService = imageService;
    }

    @GetMapping
    public ResponseEntity<Page<Record>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {
        return ResponseEntity.ok(recordService.findAll(pageable(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return recordService.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Record>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {
        return ResponseEntity.ok(recordService.searchRecords(keyword, pageable(page, size)));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<Record>> filter(
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {

        List<Record> results = recordService.findAll();
        if (regionId != null) {
            results = results.stream()
                    .filter(r -> r.getRegion() != null && r.getRegion().getRegionId().equals(regionId))
                    .toList();
        }
        if (categoryId != null) {
            results = results.stream()
                    .filter(r -> r.getCategory() != null && r.getCategory().getCategoryId().equals(categoryId))
                    .toList();
        }
        return ResponseEntity.ok(toPage(results, pageable(page, size)));
    }

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<?> create(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam Long regionId,
            @RequestParam Long categoryId,
            @RequestParam(required = false) String materialId,
            @RequestParam(required = false) String techniqueId,
            @RequestParam(required = false) MultipartFile image) {

        try {
            Record record = new Record();
            record.setTitle(title);
            record.setDescription(description);
            record.setDateCreated(LocalDate.now());
            record.setRegion(regionService.findById(regionId)
                    .orElseThrow(() -> new IllegalArgumentException("Region not found: " + regionId)));
            record.setCategory(categoryService.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId)));

            Long parsedMaterialId = parseOptionalId(materialId, "materialId");
            Long parsedTechniqueId = parseOptionalId(techniqueId, "techniqueId");

            if (parsedMaterialId != null) {
                record.setMaterial(materialService.findById(parsedMaterialId)
                        .orElseThrow(() -> new IllegalArgumentException("Material not found: " + parsedMaterialId)));
            }
            if (parsedTechniqueId != null) {
                record.setTechnique(techniqueService.findById(parsedTechniqueId)
                        .orElseThrow(() -> new IllegalArgumentException("Technique not found: " + parsedTechniqueId)));
            }

            Record saved = recordService.createRecord(record);
            if (image != null && !image.isEmpty()) imageService.uploadImage(image, saved);

            return ResponseEntity.ok(recordService.findById(saved.getRecordId()).orElse(saved));
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Long parseOptionalId(String value, String fieldName) {
        if (!StringUtils.hasText(value)) return null;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + ": " + value);
        }
    }

    private Pageable pageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "recordId"));
    }

    private Page<Record> toPage(List<Record> records, Pageable pageable) {
        int start = Math.min((int) pageable.getOffset(), records.size());
        int end = Math.min(start + pageable.getPageSize(), records.size());
        return new PageImpl<>(records.subList(start, end), pageable, records.size());
    }

    /**
     * GET /api/records/{id}/similar
     * Region boost is applied automatically using the record's own region.
     */
    @GetMapping("/{id}/similar")
    public ResponseEntity<?> getSimilar(
            @PathVariable Long id,
            @RequestParam(defaultValue = "20") int limit) {
        return recordService.findById(id)
                .<ResponseEntity<?>>map(record ->
                        ResponseEntity.ok(imageService.findSimilarRecords(id, limit))
                )
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/records/similar-by-image
     * Accepts a multipart image and an optional regionId hint.
     * If regionId is provided, results from that region are ranked higher.
     * If omitted, results are ranked by pattern similarity only.
     */
    @PostMapping("/similar-by-image")
    public ResponseEntity<?> getSimilarByImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) Long regionId) {

        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No image provided"));
        }
        try {
            List<Record> similar = imageService.findSimilarByUpload(image, limit, regionId);
            return ResponseEntity.ok(similar);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Similarity search failed: " + e.getMessage()));
        }
    }
}
