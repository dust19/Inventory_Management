package com.inventra.service;

import com.inventra.dto.request.CategoryRequestDTO;
import com.inventra.dto.response.CategoryResponseDTO;
import com.inventra.entity.Category;
import com.inventra.exception.ResourceNotFoundException;
import com.inventra.repository.CategoryRepository;
import com.inventra.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;

    // CREATE
    public CategoryResponseDTO createCategory(CategoryRequestDTO dto) {

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required");
        }

        categoryRepository.findByName(dto.getName().trim())
                .ifPresent(c -> {
                    throw new IllegalArgumentException("Category already exists");
                });

        Category category = new Category();
        category.setName(dto.getName().trim());

        return toResponse(categoryRepository.save(category));
    }

    // GET ALL
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // GET BY ID
    public CategoryResponseDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found with id " + id));
        return toResponse(category);
    }

    //GET COUNTS PER CATEGORY
    public List<CategoryResponseDTO> getCategoryWiseProductCount() {

        return inventoryRepository.getProductCountByCategory()
                .stream()
                .map(row -> {
                    CategoryResponseDTO dto = new CategoryResponseDTO();

                    dto.setId((Long) row[0]);
                    dto.setName((String) row[1]);
                    dto.setTotalProducts((Long) row[2]);

                    return dto;
                })
                .toList();
    }

    // UPDATE
    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO dto) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found with id " + id));

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required");
        }

        String newName = dto.getName().trim();

        categoryRepository.findByName(newName).ifPresent(c -> {
            if (!c.getId().equals(existing.getId())) {
                throw new IllegalArgumentException("Category name already exists");
            }
        });

        existing.setName(newName);
        return toResponse(categoryRepository.save(existing));
    }

    // DELETE
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found with id " + id));
        categoryRepository.delete(category);
    }

    // ===== mapper =====
    private CategoryResponseDTO toResponse(Category category) {
        CategoryResponseDTO dto = new CategoryResponseDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }
}
