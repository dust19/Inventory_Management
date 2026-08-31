package com.inventra.controller;

import com.inventra.dto.request.SupplierRequestDTO;
import com.inventra.dto.response.SupplierResponseDTO;
import com.inventra.service.SupplierService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
        name = "Supplier Management",
        description = "Supplier onboarding, update, activation and lookup APIs"
)
@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    // POST /api/suppliers
    @PostMapping
    public ResponseEntity<SupplierResponseDTO> create(@RequestBody SupplierRequestDTO dto) {
        SupplierResponseDTO saved = supplierService.createSupplier(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // GET /api/suppliers
    @GetMapping
    public ResponseEntity<List<SupplierResponseDTO>> getAll() {
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }

    // GET /api/suppliers/{id}
    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getSupplierById(id));
    }

    // GET /api/suppliers/by-user/{userId}
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<SupplierResponseDTO> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(supplierService.getSupplierByUserId(userId));
    }

    // PUT /api/suppliers/{id}
    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponseDTO> update(@PathVariable Long id,
                                                      @RequestBody SupplierRequestDTO dto) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, dto));
    }

    // PUT /api/suppliers/{id}/active?active=true|false
    @PutMapping("/{id}/active")
    public ResponseEntity<SupplierResponseDTO> setActive(@PathVariable Long id,
                                                         @RequestParam boolean active) {
        return ResponseEntity.ok(supplierService.setActive(id, active));
    }

    // DELETE /api/suppliers/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }
}