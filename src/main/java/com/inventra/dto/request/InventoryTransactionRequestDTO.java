package com.inventra.dto.request;

import com.inventra.entity.enums.TransactionType;
import lombok.Data;

@Data
public class InventoryTransactionRequestDTO {
    private Long productId;
    private TransactionType transactionType;
    private Integer quantity;
}