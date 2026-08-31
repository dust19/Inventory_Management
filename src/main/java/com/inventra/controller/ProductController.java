package com.inventra.controller;

import com.inventra.dto.request.AdminProductRequestDTO;
import com.inventra.dto.request.ProductRequestDTO;
import com.inventra.dto.response.AdminProductResponseDTO;
import com.inventra.dto.response.ProductResponseDTO;
import com.inventra.dto.response.SupplierProductResponseDTO;
import com.inventra.dto.response.UserProductResponseDTO;
import com.inventra.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
        name = "Product Management",
        description = "Product inventory, SKU management, stock and activation APIs"
)
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

     private final ProductService productService;

     // CREATE
     @PostMapping
     public ResponseEntity<SupplierProductResponseDTO> create(
             @RequestBody ProductRequestDTO dto
     ) {
          return ResponseEntity.status(HttpStatus.CREATED)
                  .body(productService.createProduct(dto));
     }

     //PRODUCT CREATE BY ADMIN
     @PostMapping("/admin")
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<AdminProductResponseDTO> createProductByAdmin(
             @RequestBody AdminProductRequestDTO dto) {

          return ResponseEntity.status(HttpStatus.CREATED)
                  .body(productService.createProductByAdmin(dto));
     }

     // ADMIN VIEW
     @GetMapping("/admin")
     public ResponseEntity<List<AdminProductResponseDTO>> getForAdmin() {
          return ResponseEntity.ok(productService.getProductsForAdmin());
     }

     // SUPPLIER VIEW
     @GetMapping("/supplier/{supplierId}")
     public ResponseEntity<List<SupplierProductResponseDTO>> getForSupplier(
             @PathVariable Long supplierId
     ) {
          return ResponseEntity.ok(productService.getProductsForSupplier(supplierId));
     }

     // USER VIEW
     @GetMapping
     public ResponseEntity<List<UserProductResponseDTO>> getForUser() {
          return ResponseEntity.ok(productService.getProductsForUser());
     }

     // SET PRICE (ADMIN)
     @PutMapping("/{id}/admin-price")
     public ResponseEntity<AdminProductResponseDTO> setAdminPrice(
             @PathVariable Long id,
             @RequestParam BigDecimal price
     ) {
          return ResponseEntity.ok(
                  productService.setAdminPrice(id, price)
          );
     }

     // SET PRICE (SUPPLIER)
     @PutMapping("/{id}/supplier-price")
     public ResponseEntity<SupplierProductResponseDTO> updateSupplierPrice(
             @PathVariable Long id,
             @RequestParam BigDecimal price
     ) {
          return ResponseEntity.ok(
                  productService.updateSupplierPrice(id, price)
          );
     }

     // ADD STOCK
     @PostMapping("/{id}/stock/add")
     public ResponseEntity<SupplierProductResponseDTO> addStock(
             @PathVariable Long id,
             @RequestParam Integer quantity
     ) {
          return ResponseEntity.ok(
                  productService.addStock(id, quantity)
          );
     }
}