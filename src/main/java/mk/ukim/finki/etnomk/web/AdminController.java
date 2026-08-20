package mk.ukim.finki.etnomk.web;

import mk.ukim.finki.etnomk.model.Record;
import mk.ukim.finki.etnomk.model.Region;
import mk.ukim.finki.etnomk.model.Category;
import mk.ukim.finki.etnomk.model.Material;
import mk.ukim.finki.etnomk.model.Technique;
import mk.ukim.finki.etnomk.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final RecordService recordService;
    private final RegionService regionService;
    private final CategoryService categoryService;
    private final MaterialService materialService;
    private final TechniqueService techniqueService;
    private final ImageService imageService;

    public AdminController(RecordService recordService, RegionService regionService,
                           CategoryService categoryService, MaterialService materialService,
                           TechniqueService techniqueService, ImageService imageService) {
        this.recordService = recordService;
        this.regionService = regionService;
        this.categoryService = categoryService;
        this.materialService = materialService;
        this.techniqueService = techniqueService;
        this.imageService = imageService;
    }

    // ── Dashboard ──────────────────────────────────────

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Long>> dashboard() {
        return ResponseEntity.ok(Map.of(
                "totalRecords", (long) recordService.findAll().size(),
                "totalRegions", (long) regionService.findAll().size(),
                "totalCategories", (long) categoryService.findAll().size()
        ));
    }

    // ── Records ────────────────────────────────────────

    @GetMapping("/records")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<Record>> getRecords() {
        return ResponseEntity.ok(recordService.findAll());
    }

    @PutMapping("/records/edit/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> editRecord(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            return recordService.findById(id).<ResponseEntity<?>>map(existing -> {
            if (body.containsKey("title")) existing.setTitle((String) body.get("title"));
            if (body.containsKey("description")) existing.setDescription((String) body.get("description"));

            if (body.containsKey("regionId")) {
                Long regionId = requiredId(body.get("regionId"), "regionId");
                existing.setRegion(regionService.findById(regionId)
                        .orElseThrow(() -> new IllegalArgumentException("Region not found: " + regionId)));
            }
            if (body.containsKey("categoryId")) {
                Long categoryId = requiredId(body.get("categoryId"), "categoryId");
                existing.setCategory(categoryService.findById(categoryId)
                        .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId)));
            }
            if (body.containsKey("materialId")) {
                Long materialId = optionalId(body.get("materialId"));
                existing.setMaterial(materialId == null ? null : materialService.findById(materialId)
                        .orElseThrow(() -> new IllegalArgumentException("Material not found: " + materialId)));
            }
            if (body.containsKey("techniqueId")) {
                Long techniqueId = optionalId(body.get("techniqueId"));
                existing.setTechnique(techniqueId == null ? null : techniqueService.findById(techniqueId)
                        .orElseThrow(() -> new IllegalArgumentException("Technique not found: " + techniqueId)));
            }

            recordService.updateRecord(id, existing);
            return ResponseEntity.ok(existing);
            }).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Long requiredId(Object value, String fieldName) {
        Long id = optionalId(value);
        if (id == null) throw new IllegalArgumentException(fieldName + " is required");
        return id;
    }

    private Long optionalId(Object value) {
        if (value == null || value.toString().isBlank()) return null;
        return Long.valueOf(value.toString());
    }

    @DeleteMapping("/records/delete/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteRecord(@PathVariable Long id) {
        recordService.deleteRecord(id);
        return ResponseEntity.ok(Map.of("message", "Record deleted"));
    }

    // ── Regions ────────────────────────────────────────

    @GetMapping("/regions")
    public ResponseEntity<List<Region>> getRegions() {
        return ResponseEntity.ok(regionService.findAll());
    }

    @PostMapping("/regions/create")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createRegion(@RequestBody Map<String, String> body) {
        Region region = new Region();
        region.setName(body.get("name"));
        regionService.createRegion(region);
        return ResponseEntity.ok(region);
    }

    @DeleteMapping("/regions/delete/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteRegion(@PathVariable Long id) {
        regionService.deleteRegion(id);
        return ResponseEntity.ok(Map.of("message", "Region deleted"));
    }

    // ── Categories ─────────────────────────────────────

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(categoryService.findAll());
    }

    @PostMapping("/categories/create")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createCategory(@RequestBody Map<String, String> body) {
        Category category = new Category();
        category.setName(body.get("name"));
        categoryService.createCategory(category);
        return ResponseEntity.ok(category);
    }

    @DeleteMapping("/categories/delete/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(Map.of("message", "Category deleted"));
    }

    // ── Materials ──────────────────────────────────────

    @GetMapping("/materials")
    public ResponseEntity<List<Material>> getMaterials() {
        return ResponseEntity.ok(materialService.findAll());
    }

    @PostMapping("/materials/create")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createMaterial(@RequestBody Map<String, String> body) {
        Material material = new Material();
        material.setName(body.get("name"));
        materialService.createMaterial(material);
        return ResponseEntity.ok(material);
    }

    @DeleteMapping("/materials/delete/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteMaterial(@PathVariable Long id) {
        materialService.deleteMaterial(id);
        return ResponseEntity.ok(Map.of("message", "Material deleted"));
    }

    // ── Techniques ─────────────────────────────────────

    @GetMapping("/techniques")
    public ResponseEntity<List<Technique>> getTechniques() {
        return ResponseEntity.ok(techniqueService.findAll());
    }

    @PostMapping("/techniques/create")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createTechnique(@RequestBody Map<String, String> body) {
        Technique technique = new Technique();
        technique.setName(body.get("name"));
        techniqueService.createTechnique(technique);
        return ResponseEntity.ok(technique);
    }

    @DeleteMapping("/techniques/delete/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteTechnique(@PathVariable Long id) {
        techniqueService.deleteTechnique(id);
        return ResponseEntity.ok(Map.of("message", "Technique deleted"));
    }

    @PostMapping("/images/backfill-embeddings")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> backfillEmbeddings() {
        int updated = imageService.backfillMissingEmbeddings();
        return ResponseEntity.ok(Map.of(
                "message", "Embedding backfill completed",
                "updated", updated
        ));
    }

    @PostMapping("/images/reindex-embeddings")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> reindexEmbeddings() {
        int updated = imageService.reindexAllEmbeddings();
        return ResponseEntity.ok(Map.of(
                "message", "Embedding reindex completed",
                "updated", updated
        ));
    }
}
