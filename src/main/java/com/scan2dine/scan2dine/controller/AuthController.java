package com.scan2dine.scan2dine.controller;

import com.scan2dine.scan2dine.entity.AppUser;
import com.scan2dine.scan2dine.repo.AppUserRepo;
import com.scan2dine.scan2dine.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "${app.frontend.url}")
public class AuthController {

    private final AppUserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(AppUserRepo userRepo, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String email = body.getOrDefault("email", "");
        String password = body.get("password");
        String role = body.getOrDefault("role", "CUSTOMER");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password required"));
        }

        if (userRepo.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
        }

        AppUser user = AppUser.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .build();

        userRepo.save(user);

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole(), user.getRestaurantId());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "username", user.getUsername(),
                "role", user.getRole(),
                "userId", user.getId()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        Optional<AppUser> optUser = userRepo.findByUsername(username);
        if (optUser.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        AppUser user = optUser.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole(), user.getRestaurantId());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "username", user.getUsername(),
                "role", user.getRole(),
                "userId", user.getId(),
                "restaurantId", user.getRestaurantId() != null ? user.getRestaurantId() : 0
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }

        String username = jwtUtil.extractUsername(token);
        Optional<AppUser> optUser = userRepo.findByUsername(username);
        if (optUser.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "User not found"));
        }

        AppUser user = optUser.get();
        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "role", user.getRole(),
                "userId", user.getId(),
                "restaurantId", user.getRestaurantId() != null ? user.getRestaurantId() : 0
        ));
    }
}
