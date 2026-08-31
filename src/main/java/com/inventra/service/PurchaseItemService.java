package com.inventra.service;

import com.inventra.dto.response.PurchaseItemResponseDTO;
import com.inventra.entity.PurchaseItem;
import com.inventra.repository.PurchaseItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseItemService {

    private final PurchaseItemRepository purchaseItemRepo;

    // ================= GET BY PURCHASE =================
    @Transactional(readOnly = true)
    public List<PurchaseItemResponseDTO> getByPurchaseId(Long purchaseId) {

        if (purchaseId == null) {
            throw new IllegalArgumentException("PurchaseId is required");
        }

        return purchaseItemRepo.findByPurchaseId(purchaseId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ================= GET BY PRODUCT =================
    @Transactional(readOnly = true)
    public List<PurchaseItemResponseDTO> getByProductId(Long productId) {

        if (productId == null) {
            throw new IllegalArgumentException("ProductId is required");
        }

        return purchaseItemRepo.findByProductId(productId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ================= MAPPER =================
    private PurchaseItemResponseDTO toDTO(PurchaseItem item) {

        PurchaseItemResponseDTO dto = new PurchaseItemResponseDTO();

        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setSubTotal(item.getSubTotal());
        System.out.println(item.getProduct().getName());

        return dto;
    }
}