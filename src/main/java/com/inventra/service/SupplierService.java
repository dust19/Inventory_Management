package com.inventra.service;

import com.inventra.dto.request.SupplierRequestDTO;
import com.inventra.dto.response.SupplierResponseDTO;
import com.inventra.entity.Category;
import com.inventra.entity.Supplier;
import com.inventra.entity.User;
import com.inventra.exception.ResourceNotFoundException;
import com.inventra.repository.CategoryRepository;
import com.inventra.repository.SupplierRepository;
import com.inventra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    // CREATE
    @Transactional
    public SupplierResponseDTO createSupplier(SupplierRequestDTO dto) {

        if (dto == null) throw new IllegalArgumentException("Supplier data is required");
        if (dto.getCategoryId() == null) throw new IllegalArgumentException("CategoryId is required");

        if (dto.getCompanyName() == null || dto.getCompanyName().trim().isEmpty()) {
            throw new IllegalArgumentException("Company name is required");
        }

        User user = null;

        if (dto.getUserId() != null && dto.getUserId() > 0) {
            user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "User not found with id " + dto.getUserId()));
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + dto.getCategoryId()));

        if (user != null) {
            supplierRepository.findByUserId(dto.getUserId())
                    .ifPresent(s -> {
                        throw new IllegalArgumentException(
                                "Supplier already exists for this user");
                    });
        }

        Supplier supplier = new Supplier();
        supplier.setUser(user);
        supplier.setCategory(category);
        supplier.setCompanyName(dto.getCompanyName().trim());
        supplier.setAddress(dto.getAddress());
        supplier.setActive(true);

        return toResponse(supplierRepository.save(supplier));
    }

    // GET ALL
    public List<SupplierResponseDTO> getAllSuppliers() {
        return supplierRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // GET BY ID
    public SupplierResponseDTO getSupplierById(Long id) {
        Supplier s = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id " + id));
        return toResponse(s);
    }

    // GET BY USER ID
    public SupplierResponseDTO getSupplierByUserId(Long userId) {
        Supplier s = supplierRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found for user id " + userId));
        return toResponse(s);
    }

    // UPDATE
    @Transactional
    public SupplierResponseDTO updateSupplier(Long id, SupplierRequestDTO dto) {
        Supplier existing = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id " + id));

        if (dto == null) throw new IllegalArgumentException("Update data is required");

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + dto.getCategoryId()));
            existing.setCategory(category);
        }

        if (dto.getCompanyName() != null && !dto.getCompanyName().trim().isEmpty()) {
            existing.setCompanyName(dto.getCompanyName().trim());
        }

        if (dto.getAddress() != null) {
            existing.setAddress(dto.getAddress());
        }

        return toResponse(supplierRepository.save(existing));
    }

    // ACTIVATE / DEACTIVATE
    @Transactional
    public SupplierResponseDTO setActive(Long id, boolean active) {

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Supplier not found with id " + id));

        supplier.setActive(active);

        if (supplier.getUser() != null) {
            supplier.getUser().setActive(active);
            userRepository.save(supplier.getUser());
        }

        supplierRepository.save(supplier);

        return toResponse(supplier);
    }

    // DELETE
    public void deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id " + id));
        supplierRepository.delete(supplier);
    }

    // ===================== helpers =====================

    private SupplierResponseDTO toResponse(Supplier s) {
        SupplierResponseDTO dto = new SupplierResponseDTO();
        dto.setId(s.getId());
        dto.setCompanyName(s.getCompanyName());
        dto.setAddress(s.getAddress());
        dto.setActive(s.isActive());      // assumes boolean active in entity
        dto.setCreatedAt(s.getCreatedAt()); // if you have createdAt in entity

        dto.setUserId(s.getUser() != null ? s.getUser().getId() : null);
        dto.setUserName(s.getUser() != null ? s.getUser().getName() : null);

        dto.setCategoryId(s.getCategory() != null ? s.getCategory().getId() : null);
        dto.setCategoryName(s.getCategory() != null ? s.getCategory().getName() : null);

        return dto;
    }
}

