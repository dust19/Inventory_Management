package com.inventra.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PurchaseItemResponseDTO {
     private Long productId;
     private String productName;
     private Integer quantity;
     private BigDecimal unitPrice;
     private BigDecimal subTotal;
}