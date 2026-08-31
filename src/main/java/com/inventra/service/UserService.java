package com.inventra.service;

import com.inventra.dto.response.UserResponseDto;
import com.inventra.entity.Supplier;
import com.inventra.entity.User;
import com.inventra.entity.enums.Role;
import com.inventra.exception.ResourceNotFoundException;
import com.inventra.repository.ManagerRepository;
import com.inventra.repository.StaffRepository;
import com.inventra.repository.SupplierRepository;
import com.inventra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ManagerRepository managerRepo;
    private final StaffRepository staffRepo;
    private final SupplierRepository supplierRepo;

    // GET ALL
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }
    // GET BY ID
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));

        return mapToDto(user);
    }

    // UPDATE (basic profile)
    public UserResponseDto updateUser(Long id, User updated) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));

        if (updated.getName() != null) existing.setName(updated.getName());
        if (updated.getPhone() != null) existing.setPhone(updated.getPhone());
        if (updated.getAddress() != null) existing.setAddress(updated.getAddress());
        if (updated.getEmail() != null && !updated.getEmail().trim().isEmpty()) {
            String newEmail = updated.getEmail().trim().toLowerCase();

            userRepository.findByEmail(newEmail).ifPresent(u -> {
                if (!u.getId().equals(existing.getId())) {
                    throw new RuntimeException("Email already in use");
                }
            });

            existing.setEmail(newEmail);
        }

        return mapToDto(userRepository.save(existing));
    }

    @Transactional
    public UserResponseDto setActive(Long id, boolean active) {
        User user = getUserEntityById(id);
        user.setActive(active);
        switch (user.getRole()) {
            case ROLE_MANAGER -> {
                managerRepo.findByUserId(id)
                        .ifPresent(m -> {
                            m.setActive(active);
                            managerRepo.save(m);
                        });
            }
            case ROLE_STAFF -> {
                staffRepo.findByUserId(id)
                        .ifPresent(s -> {
                            s.setActive(active);
                            staffRepo.save(s);
                        });
            }
            case ROLE_SUPPLIER -> {
                supplierRepo.findByUserId(id)
                        .ifPresent(sp -> {
                            sp.setActive(active);
                            supplierRepo.save(sp);
                        });
            }
        }

        return mapToDto(userRepository.save(user));
    }

    public void changePassword(Long id, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new RuntimeException("New password is required");
        }

        User user = getUserEntityById(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public UserResponseDto assignRole(Long id, Role role) {
        User user = getUserEntityById(id);

        user.setRole(role);

        return mapToDto(userRepository.save(user));
    }

    // DELETE
    public void deleteUser(Long id) {
        User user = getUserEntityById(id);
        userRepository.delete(user);
    }

    private User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }

    private UserResponseDto mapToDto(User user) {
        UserResponseDto dto = new UserResponseDto();

        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        dto.setActive(user.isActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setRole(user.getRole());

        return dto;
    }

}