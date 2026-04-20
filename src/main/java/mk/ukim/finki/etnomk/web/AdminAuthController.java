package mk.ukim.finki.etnomk.web;

import mk.ukim.finki.etnomk.model.Role;
import mk.ukim.finki.etnomk.model.User;
import mk.ukim.finki.etnomk.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    private final UserService userService;

    public AdminAuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerAdmin(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String email = body.get("email");
        String password = body.get("password");
        String confirmPassword = body.get("confirmPassword");

        if (!password.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Passwords do not match"));
        }
        try {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(password);
            user.setRole(Role.ROLE_ADMIN);

            userService.register(user);
            return ResponseEntity.ok(Map.of("message", "Admin registration successful!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Admin registration failed: " + e.getMessage()));
        }
    }
}