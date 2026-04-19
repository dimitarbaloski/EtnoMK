package mk.ukim.finki.etnomk.web;

import mk.ukim.finki.etnomk.model.User;
import mk.ukim.finki.etnomk.model.Role;
import mk.ukim.finki.etnomk.security.JwtConstants;
import mk.ukim.finki.etnomk.security.JwtHelper;
import mk.ukim.finki.etnomk.security.JwtLoginRequest;
import mk.ukim.finki.etnomk.security.JwtLoginResponse;
import mk.ukim.finki.etnomk.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtHelper jwtHelper;

    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtHelper jwtHelper) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtHelper = jwtHelper;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout,
                       Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        return "auth/login";
    }

    // No manual POST /login; Spring Security formLogin handles it on /perform_login

    @PostMapping("/api/auth/login")
    public ResponseEntity<JwtLoginResponse> jwtLogin(@RequestBody JwtLoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String token = jwtHelper.generateToken((UserDetails) authentication.getPrincipal());
        return ResponseEntity.ok(new JwtLoginResponse(token, "Bearer", JwtConstants.EXPIRATION_TIME));
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                              @RequestParam String password,
                              @RequestParam String email,
                              @RequestParam String confirmPassword,
                              RedirectAttributes redirectAttributes) {
        try {
            if (!password.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Passwords do not match");
                return "redirect:/register";
            }

            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setEmail(email);
            user.setRole(Role.ROLE_USER);

            userService.register(user);
            redirectAttributes.addFlashAttribute("message", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Registration failed: " + e.getMessage());
            return "redirect:/register";
        }
    }
}
