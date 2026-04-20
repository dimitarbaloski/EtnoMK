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
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    private final RecordService recordService;
    private final RegionService regionService;
    private final CategoryService categoryService;
    private final MaterialService materialService;
    private final TechniqueService techniqueService;

    public AdminController(RecordService recordService, RegionService regionService,
                           CategoryService categoryService, MaterialService materialService,
                           TechniqueService techniqueService) {
        this.recordService = recordService;
        this.regionService = regionService;
        this.categoryService = categoryService;
        this.materialService = materialService;
        this.techniqueService = techniqueService;
    }

    // ── Dashboard ──────────────────────────────────────

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Long>> dashboard() {
        return ResponseEntity.ok(Map.of(
                "totalRecords", (long) recordService.findAll().size(),
                "totalRegions", (long) regionService.findAll().size(),
                "totalCategories", (long) categoryService.findAll().size()
        ));
    }

    // ── Records ────────────────────────────────────────

    @GetMapping("/records")
    public ResponseEntity<List<Record>> getRecords() {
        return ResponseEntity.ok(recordService.findAll());
    }

    @PutMapping("/records/edit/{id}")
    public ResponseEntity<?> editRecord(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return recordService.findById(id).map(existing -> {
            if (body.containsKey("title")) existing.setTitle((String) body.get("title"));
            if (body.containsKey("description")) existing.setDescription((String) body.get("description"));

            if (body.containsKey("regionId")) {
                Long regionId = Long.valueOf(body.get("regionId").toString());
                existing.setRegion(regionService.findById(regionId).orElse(null));
            }
            if (body.containsKey("categoryId")) {
                Long categoryId = Long.valueOf(body.get("categoryId").toString());
                existing.setCategory(categoryService.findById(categoryId).orElse(null));
            }
            if (body.containsKey("materialId")) {
                Long materialId = Long.valueOf(body.get("materialId").toString());
                existing.setMaterial(materialService.findById(materialId).orElse(null));
            }
            if (body.containsKey("techniqueId")) {
                Long techniqueId = Long.valueOf(body.get("techniqueId").toString());
                existing.setTechnique(techniqueService.findById(techniqueId).orElse(null));
            }

            recordService.updateRecord(id, existing);
            return ResponseEntity.ok(existing);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/records/delete/{id}")
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
    public ResponseEntity<?> createRegion(@RequestBody Map<String, String> body) {
        Region region = new Region();
        region.setName(body.get("name"));
        regionService.createRegion(region);
        return ResponseEntity.ok(region);
    }

    @DeleteMapping("/regions/delete/{id}")
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
    public ResponseEntity<?> createCategory(@RequestBody Map<String, String> body) {
        Category category = new Category();
        category.setName(body.get("name"));
        categoryService.createCategory(category);
        return ResponseEntity.ok(category);
    }

    @DeleteMapping("/categories/delete/{id}")
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
    public ResponseEntity<?> createMaterial(@RequestBody Map<String, String> body) {
        Material material = new Material();
        material.setName(body.get("name"));
        materialService.createMaterial(material);
        return ResponseEntity.ok(material);
    }

    @DeleteMapping("/materials/delete/{id}")
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
    public ResponseEntity<?> createTechnique(@RequestBody Map<String, String> body) {
        Technique technique = new Technique();
        technique.setName(body.get("name"));
        techniqueService.createTechnique(technique);
        return ResponseEntity.ok(technique);
    }

    @DeleteMapping("/techniques/delete/{id}")
    public ResponseEntity<?> deleteTechnique(@PathVariable Long id) {
        techniqueService.deleteTechnique(id);
        return ResponseEntity.ok(Map.of("message", "Technique deleted"));
    }
}