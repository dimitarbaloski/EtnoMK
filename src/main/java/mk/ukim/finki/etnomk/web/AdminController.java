package mk.ukim.finki.etnomk.web;

import mk.ukim.finki.etnomk.model.Category;
import mk.ukim.finki.etnomk.model.Material;
import mk.ukim.finki.etnomk.model.Record;
import mk.ukim.finki.etnomk.model.Region;
import mk.ukim.finki.etnomk.model.Technique;
import mk.ukim.finki.etnomk.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
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

    // Dashboard
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalRecords", recordService.findAll().size());
        model.addAttribute("totalRegions", regionService.findAll().size());
        model.addAttribute("totalCategories", categoryService.findAll().size());
        return "admin/dashboard";
    }

    // Record Management
    @GetMapping("/records")
    public String manageRecords(Model model) {
        model.addAttribute("records", recordService.findAll());
        return "admin/manage-records";
    }

    @GetMapping("/records/edit/{id}")
    public String editRecordForm(@PathVariable Long id, Model model) {
        recordService.findById(id).ifPresent(record -> {
            model.addAttribute("record", record);
            model.addAttribute("regions", regionService.findAll());
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("materials", materialService.findAll());
            model.addAttribute("techniques", techniqueService.findAll());
        });
        return "admin/edit-record";
    }

    @PostMapping("/records/edit/{id}")
    public String editRecord(@PathVariable Long id, @ModelAttribute Record record,
                            @RequestParam(name = "regionId") Long regionId,
                            @RequestParam(name = "categoryId") Long categoryId,
                            @RequestParam(name = "materialId", required = false) Long materialId,
                            @RequestParam(name = "techniqueId", required = false) Long techniqueId) {

        record.setRegion(regionService.findById(regionId).orElse(null));
        record.setCategory(categoryService.findById(categoryId).orElse(null));

        if (materialId != null) {
            record.setMaterial(materialService.findById(materialId).orElse(null));
        }
        if (techniqueId != null) {
            record.setTechnique(techniqueService.findById(techniqueId).orElse(null));
        }

        recordService.updateRecord(id, record);
        return "redirect:/admin/records";
    }

    @PostMapping("/records/delete/{id}")
    public String deleteRecord(@PathVariable Long id) {
        recordService.deleteRecord(id);
        return "redirect:/admin/records";
    }

    // Region Management
    @GetMapping("/regions")
    public String manageRegions(Model model) {
        model.addAttribute("regions", regionService.findAll());
        return "admin/manage-regions";
    }

    @GetMapping("/regions/create")
    public String createRegionForm() {
        return "admin/create-region";
    }

    @PostMapping("/regions/create")
    public String createRegion(@ModelAttribute Region region) {
        regionService.createRegion(region);
        return "redirect:/admin/regions";
    }

    @PostMapping("/regions/delete/{id}")
    public String deleteRegion(@PathVariable Long id) {
        regionService.deleteRegion(id);
        return "redirect:/admin/regions";
    }

    // Category Management
    @GetMapping("/categories")
    public String manageCategories(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "admin/manage-categories";
    }

    @GetMapping("/categories/create")
    public String createCategoryForm() {
        return "admin/create-category";
    }

    @PostMapping("/categories/create")
    public String createCategory(@ModelAttribute Category category) {
        categoryService.createCategory(category);
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return "redirect:/admin/categories";
    }

    // Material Management
    @GetMapping("/materials")
    public String manageMaterials(Model model) {
        model.addAttribute("materials", materialService.findAll());
        return "admin/manage-materials";
    }

    @GetMapping("/materials/create")
    public String createMaterialForm() {
        return "admin/create-material";
    }

    @PostMapping("/materials/create")
    public String createMaterial(@ModelAttribute Material material) {
        materialService.createMaterial(material);
        return "redirect:/admin/materials";
    }

    @PostMapping("/materials/delete/{id}")
    public String deleteMaterial(@PathVariable Long id) {
        materialService.deleteMaterial(id);
        return "redirect:/admin/materials";
    }

    // Technique Management
    @GetMapping("/techniques")
    public String manageTechniques(Model model) {
        model.addAttribute("techniques", techniqueService.findAll());
        return "admin/manage-techniques";
    }

    @GetMapping("/techniques/create")
    public String createTechniqueForm() {
        return "admin/create-technique";
    }

    @PostMapping("/techniques/create")
    public String createTechnique(@ModelAttribute Technique technique) {
        techniqueService.createTechnique(technique);
        return "redirect:/admin/techniques";
    }

    @PostMapping("/techniques/delete/{id}")
    public String deleteTechnique(@PathVariable Long id) {
        techniqueService.deleteTechnique(id);
        return "redirect:/admin/techniques";
    }
}