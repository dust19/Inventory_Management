package com.inventra.service;

import com.inventra.dto.response.SaleItemResponseDTO;
import com.inventra.entity.SaleItem;
import com.inventra.repository.SaleItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleItemService {

    private final SaleItemRepository saleItemRepo;

    public List<SaleItemResponseDTO> getBySaleId(Long saleId) {

        if (saleId == null) {
            throw new IllegalArgumentException("SaleId is required");
        }

        return saleItemRepo.findBySaleId(saleId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private SaleItemResponseDTO toDTO(SaleItem item) {

        SaleItemResponseDTO dto = new SaleItemResponseDTO();

        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setSubtotal(item.getSubtotal());

        return dto;
    }
}