package mk.ukim.finki.etnomk.web;

import mk.ukim.finki.etnomk.model.Role;
import mk.ukim.finki.etnomk.model.User;
import mk.ukim.finki.etnomk.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminAuthController {

    private final UserService userService;

    public AdminAuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/admin/login")
    public String adminLogin(@RequestParam(value = "error", required = false) String error,
                             @RequestParam(value = "logout", required = false) String logout,
                             Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        return "auth/admin-login";
    }

    @GetMapping("/admin/register")
    public String adminRegister() {
        return "auth/admin-register";
    }

    @PostMapping("/admin/register")
    public String processAdminRegister(@RequestParam String username,
                                       @RequestParam String email,
                                       @RequestParam String password,
                                       @RequestParam String confirmPassword,
                                       RedirectAttributes redirectAttributes) {
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match");
            return "redirect:/admin/register";
        }
        try {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(password);
            user.setRole(Role.ROLE_ADMIN);

            userService.register(user);
            redirectAttributes.addFlashAttribute("message", "Admin registration successful! Please login.");
            return "redirect:/admin/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Admin registration failed: " + e.getMessage());
            return "redirect:/admin/register";
        }
    }

}