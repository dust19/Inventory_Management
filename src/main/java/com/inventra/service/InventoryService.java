package com.inventra.service;

import com.inventra.dto.request.InventoryRequestDTO;
import com.inventra.dto.response.InventoryResponseDTO;
import com.inventra.entity.Inventory;
import com.inventra.entity.Product;
import com.inventra.exception.ResourceNotFoundException;
import com.inventra.repository.InventoryRepository;
import com.inventra.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepo;

    public InventoryResponseDTO getByProduct(Long productId) {
        Inventory inv = inventoryRepo.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        return toDTO(inv);
    }

    public List<InventoryResponseDTO> getAll() {
        return inventoryRepo.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public InventoryResponseDTO updateReorderLevel(Long productId, Integer level) {
        Inventory inv = inventoryRepo.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        inv.setReorderLevel(level);
        return toDTO(inventoryRepo.save(inv));
    }

    public List<InventoryResponseDTO> getLowStock() {
        return inventoryRepo.findAll().stream()
                .filter(inv -> inv.getAvailableStock() <= inv.getReorderLevel())
                .map(this::toDTO).toList();
    }

    private InventoryResponseDTO toDTO(Inventory inv) {
        InventoryResponseDTO dto = new InventoryResponseDTO();
        dto.setId(inv.getId());
        dto.setSku(inv.getProduct().getSku());
        dto.setSupplierId(inv.getProduct().getSupplier().getId());
        if(inv.getProduct().getSupplier().getUser() != null){
        dto.setSupplierName(inv.getProduct().getSupplier().getUser().getName());
        }
        dto.setProductId(inv.getProduct().getId());
        dto.setProductName(inv.getProduct().getName());
        dto.setAvailableStock(inv.getAvailableStock());
        dto.setReorderLevel(inv.getReorderLevel());
        dto.setLastUpdated(inv.getLastUpdated());
        return dto;
    }
}