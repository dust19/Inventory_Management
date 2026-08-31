package com.inventra.dto.request;

import lombok.Data;

@Data
public class InventoryRequestDTO {
    private Long productId;
    private Integer availableStock;
    private Integer reorderLevel;
}