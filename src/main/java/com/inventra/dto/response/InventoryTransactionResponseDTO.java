package com.inventra.dto.response;

import com.inventra.entity.enums.TransactionType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventoryTransactionResponseDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String sku;

    private TransactionType transactionType;
    private Integer quantity;

    private LocalDateTime createdAt;
}