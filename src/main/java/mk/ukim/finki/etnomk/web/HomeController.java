package mk.ukim.finki.etnomk.web;

import mk.ukim.finki.etnomk.service.RecordService;
import mk.ukim.finki.etnomk.service.RegionService;
import mk.ukim.finki.etnomk.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final RecordService recordService;
    private final RegionService regionService;
    private final CategoryService categoryService;

    public HomeController(RecordService recordService, RegionService regionService, CategoryService categoryService) {
        this.recordService = recordService;
        this.regionService = regionService;
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("records", recordService.findAll());
        model.addAttribute("regions", regionService.findAll());
        model.addAttribute("categories", categoryService.findAll());
        return "home";
    }

    @GetMapping("/dashboard")
    public String dashboardRedirect() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

}
