package com.inventra.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequestDTO {
    private String name;
    private String description;
    private BigDecimal supplierToAdminPrice;
    private Integer quantity;
    private Long supplierId;
    private String sku;
}