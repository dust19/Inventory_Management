package com.inventra.controller;

import com.inventra.dto.request.InventoryTransactionRequestDTO;
import com.inventra.dto.response.InventoryTransactionResponseDTO;
import com.inventra.service.InventoryTransactionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Inventory Transactions",
        description = "Stock IN/OUT transaction APIs with automatic inventory updates"
)
@RestController
@RequestMapping("/api/inventory-transactions")
@RequiredArgsConstructor
public class InventoryTransactionController {

    private final InventoryTransactionService txService;

    // CREATE (manual adjustment if needed)
    @PostMapping
    public ResponseEntity<Void> create(
            @RequestBody InventoryTransactionRequestDTO dto
    ) {
        txService.process(dto.getProductId(), dto.getTransactionType(), dto.getQuantity());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<InventoryTransactionResponseDTO>> getAll() {
        return ResponseEntity.ok(txService.getAllTransactions());
    }

    // GET BY PRODUCT
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<InventoryTransactionResponseDTO>> getByProduct(
            @PathVariable Long productId
    ) {
        return ResponseEntity.ok(txService.getTransactionsByProduct(productId));
    }
}