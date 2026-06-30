package mk.ukim.finki.etnomk.web;

import mk.ukim.finki.etnomk.model.Role;
import mk.ukim.finki.etnomk.model.User;
import mk.ukim.finki.etnomk.security.JwtConstants;
import mk.ukim.finki.etnomk.security.JwtHelper;
import mk.ukim.finki.etnomk.security.JwtLoginRequest;
import mk.ukim.finki.etnomk.security.JwtLoginResponse;
import mk.ukim.finki.etnomk.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtHelper jwtHelper;

    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtHelper jwtHelper) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtHelper = jwtHelper;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody JwtLoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            String token = jwtHelper.generateToken((UserDetails) authentication.getPrincipal());
            return ResponseEntity.ok(new JwtLoginResponse(token, "Bearer", JwtConstants.EXPIRATION_TIME));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String email = body.get("email");
        String confirmPassword = body.get("confirmPassword");

        if (!password.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Passwords do not match"));
        }

        try {
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setEmail(email);
            user.setRole(Role.ROLE_USER);

            userService.register(user);
            return ResponseEntity.ok(Map.of("message", "Registration successful! Please login."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }
}