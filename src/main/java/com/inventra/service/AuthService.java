package com.inventra.service;

import com.inventra.dto.request.LoginRequest;
import com.inventra.dto.request.RegisterRequest;
import com.inventra.dto.response.JwtResponse;
import com.inventra.entity.enums.Role;
import com.inventra.entity.User;
import com.inventra.repository.UserRepository;
import com.inventra.security.JwtUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /* ---------------- REGISTER ---------------- */
    public String register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()
                && userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ROLE_USER);
        user.setActive(true);

        userRepository.save(user);
        return "User registered successfully";
    }

    /* ---------------- REGISTER with role ---------------- */
    public String registerWithRole(RegisterRequest request, Role role) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new RuntimeException("Email already exists");

        if (request.getPhone() != null && !request.getPhone().isBlank()
                && userRepository.existsByPhone(request.getPhone()))
            throw new RuntimeException("Phone already exists");

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setActive(true);

        userRepository.save(user);
        return "User registered successfully with role " + role.name();
    }

    /* ---------------- LOGIN (Email OR Phone) ---------------- */
    public JwtResponse login(LoginRequest request) {

        String loginValue = request.getUsername();

        User user = userRepository.findByEmailOrPhone(loginValue, loginValue)
                .orElseThrow(() -> new RuntimeException("Invalid email/phone or password"));

        if (!user.isActive()) {
            throw new RuntimeException("User account is inactive");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email/phone or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new JwtResponse(token);
    }

    /* ---------------- CURRENT USER ---------------- */
    public Map<String, Object> me(Authentication authentication) {

        String email = authentication.getName(); // comes from JWT (sub)

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return Map.of(
                "id",user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "phone", user.getPhone(),
                "address", user.getAddress(),
                "role", user.getRole().name()
        );
    }
}