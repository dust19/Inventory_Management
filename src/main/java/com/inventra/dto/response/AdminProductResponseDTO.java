package com.inventra.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminProductResponseDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal supplierToAdminPrice;
    private BigDecimal adminToUserPrice;
    private Integer availableStock;
    private Long categoryId;
    private String categoryName;
    private String supplierName;
    private String sku;
    private boolean isActive;
    private LocalDateTime createdAt;
}

