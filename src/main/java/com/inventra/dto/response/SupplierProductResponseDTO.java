package com.inventra.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SupplierProductResponseDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal supplierToAdminPrice;
    private Integer availableStock;  // quantity in product
    private String sku;
    private boolean isActive;
    private LocalDateTime createdAt;
}

