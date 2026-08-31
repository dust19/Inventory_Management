package com.inventra.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventoryResponseDTO {
    private Long id;

    private Long productId;
    private String productName;
    private String sku;
    private Integer availableStock;
    private Integer reorderLevel;
    private Long supplierId;
    private String supplierName;
    private LocalDateTime lastUpdated;
}