package com.inventra.controller;

import com.inventra.dto.request.SaleRequestDTO;
import com.inventra.dto.response.SaleResponseDTO;
import com.inventra.service.SaleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Sales",
        description = "Staff, Manager, or Admin billing — sale creation, status update and invoice APIs"
)
@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    // STAFF, MANAGER, ADMIN can create a sale
    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<SaleResponseDTO> create(@RequestBody SaleRequestDTO dto) {
        return ResponseEntity.ok(saleService.createSale(dto));
    }

    // ADMIN and MANAGER can view all sales
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<SaleResponseDTO>> getAll() {
        return ResponseEntity.ok(saleService.getAllSales());
    }

    // get sales by whoever created them — path variable is userId
    @GetMapping("/sold-by/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<List<SaleResponseDTO>> getBySoldBy(@PathVariable Long userId) {
        return ResponseEntity.ok(saleService.getBySoldBy(userId));
    }

    @GetMapping("/manager/user/{userId}")
    public ResponseEntity<List<SaleResponseDTO>> getSalesByManagerUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                saleService.getSalesByManagerUser(userId)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<SaleResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.getById(id));
    }
}