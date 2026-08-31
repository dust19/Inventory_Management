package com.inventra.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductResponseDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal sellingPrice;
    private Integer availableStock;   // instead of quantity
    private Integer threshold;
    private String categoryName;
    private String supplierName;
    private String sku;
    private boolean isActive;
    private LocalDateTime createdAt;
}