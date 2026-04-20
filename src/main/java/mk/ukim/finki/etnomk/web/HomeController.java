package mk.ukim.finki.etnomk.web;

import mk.ukim.finki.etnomk.service.CategoryService;
import mk.ukim.finki.etnomk.service.RecordService;
import mk.ukim.finki.etnomk.service.RegionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class HomeController {

    private final RecordService recordService;
    private final RegionService regionService;
    private final CategoryService categoryService;

    public HomeController(RecordService recordService, RegionService regionService, CategoryService categoryService) {
        this.recordService = recordService;
        this.regionService = regionService;
        this.categoryService = categoryService;
    }

    @GetMapping("/home")
    public ResponseEntity<?> home() {
        return ResponseEntity.ok(Map.of(
                "records", recordService.findAll(),
                "regions", regionService.findAll(),
                "categories", categoryService.findAll()
        ));
    }
}