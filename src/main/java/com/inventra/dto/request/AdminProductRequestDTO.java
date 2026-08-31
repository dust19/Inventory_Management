package com.inventra.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminProductRequestDTO {

     private String name;
     private String description;
     private String sku;

     private Long supplierId; // optional

     private BigDecimal costPrice;
     private BigDecimal sellingPrice;

     private Integer quantity;
}
