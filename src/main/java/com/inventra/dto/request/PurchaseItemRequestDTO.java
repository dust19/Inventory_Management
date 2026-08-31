package com.inventra.dto.request;

import lombok.Data;

@Data
public class PurchaseItemRequestDTO {
    private Long productId;
    private Integer quantity;
}
