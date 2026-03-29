package mk.ukim.finki.etnomk.web;

import mk.ukim.finki.etnomk.model.Record;
import mk.ukim.finki.etnomk.service.RecordService;
import mk.ukim.finki.etnomk.service.RegionService;
import mk.ukim.finki.etnomk.service.CategoryService;
import mk.ukim.finki.etnomk.service.MaterialService;
import mk.ukim.finki.etnomk.service.TechniqueService;
import mk.ukim.finki.etnomk.service.ImageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/records")
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

    @GetMapping("/view")
    public String viewRecords(Model model) {
        model.addAttribute("records", recordService.findAll());
        model.addAttribute("regions", regionService.findAll());
        model.addAttribute("categories", categoryService.findAll());
        return "records/view-records";
    }

    @GetMapping("/{id}")
    public String viewRecordDetail(@PathVariable Long id, Model model) {
        recordService.findById(id).ifPresent(record -> model.addAttribute("record", record));
        return "records/record-detail";
    }

    @GetMapping("/create")
    public String createRecordForm(Model model) {
        model.addAttribute("regions", regionService.findAll());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("materials", materialService.findAll());
        model.addAttribute("techniques", techniqueService.findAll());
        return "records/create-record";
    }

    @PostMapping("/create")
    public String createRecord(@ModelAttribute Record record,
                              @RequestParam(name = "regionId") Long regionId,
                              @RequestParam(name = "categoryId") Long categoryId,
                              @RequestParam(name = "materialId", required = false) Long materialId,
                              @RequestParam(name = "techniqueId", required = false) Long techniqueId,
                              @RequestParam(name = "image", required = false) MultipartFile imageFile) {

        record.setDateCreated(LocalDate.now());
        record.setRegion(regionService.findById(regionId).orElse(null));
        record.setCategory(categoryService.findById(categoryId).orElse(null));

        if (materialId != null) {
            record.setMaterial(materialService.findById(materialId).orElse(null));
        }
        if (techniqueId != null) {
            record.setTechnique(techniqueService.findById(techniqueId).orElse(null));
        }

        Record savedRecord = recordService.createRecord(record);

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                imageService.uploadImage(imageFile, savedRecord);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return "redirect:/records/view";
    }

    @GetMapping("/search")
    public String searchRecords(@RequestParam(name = "keyword") String keyword, Model model) {
        List<Record> searchResults = recordService.searchRecords(keyword);
        model.addAttribute("records", searchResults);
        model.addAttribute("keyword", keyword);
        return "records/search-results";
    }

    @GetMapping("/filter")
    public String filterRecords(@RequestParam(name = "regionId", required = false) Long regionId,
                               @RequestParam(name = "categoryId", required = false) Long categoryId,
                               Model model) {
        List<Record> filteredRecords = recordService.findAll();

        if (regionId != null) {
            filteredRecords = filteredRecords.stream()
                    .filter(r -> r.getRegion().getRegionId().equals(regionId))
                    .toList();
        }

        if (categoryId != null) {
            filteredRecords = filteredRecords.stream()
                    .filter(r -> r.getCategory().getCategoryId().equals(categoryId))
                    .toList();
        }

        model.addAttribute("records", filteredRecords);
        model.addAttribute("regions", regionService.findAll());
        model.addAttribute("categories", categoryService.findAll());
        return "records/view-records";
    }

}
