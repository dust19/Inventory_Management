package com.inventra.controller;

import com.inventra.dto.request.LoginRequest;
import com.inventra.dto.request.RegisterRequest;
import com.inventra.dto.response.JwtResponse;
import com.inventra.entity.enums.Role;
import com.inventra.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@Tag(
        name = "Authentication",
        description = "User registration, login and current-user APIs"
)
@RestController
@RequestMapping("/auth")
@CrossOrigin("http://localhost:4200")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /* ---------------- REGISTER ---------------- */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
            @RequestBody RegisterRequest request) {

        return ResponseEntity.ok(
                Map.of("message", authService.register(request))
        );
    }

    /* ----- Create Supplier, Staff, Mangager by admin ----------- */
    @PostMapping("/register/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> registerWithRole(
            @RequestBody RegisterRequest request,
            @PathVariable Role role) {

        return ResponseEntity.ok(
                Map.of("message", authService.registerWithRole(request, role))
        );
    }

    /* ---------------- LOGIN ---------------- */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /* ---------------- ME ---------------- */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(authService.me(authentication));
    }
}