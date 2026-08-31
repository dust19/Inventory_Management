package com.inventra.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SaleItemResponseDTO {
     private Long id;
     private Long productId;
     private String productName;
     private Integer quantity;
     private BigDecimal price;
     private BigDecimal subtotal;
}