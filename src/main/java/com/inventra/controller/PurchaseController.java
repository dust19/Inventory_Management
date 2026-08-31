package com.inventra.controller;

import com.inventra.dto.request.PurchaseRequestDTO;
import com.inventra.dto.response.PurchaseResponseDTO;
import com.inventra.entity.enums.PurchaseStatus;
import com.inventra.service.PurchaseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.inventra.repository.UserRepository;
import com.inventra.entity.User;
import com.inventra.exception.ResourceNotFoundException;

import java.util.List;

@Tag(name = "Purchase Management",
        description = "Admin purchases from suppliers, stock intake and delivery APIs")
@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {

     private final PurchaseService purchaseService;
     private final UserRepository userRepo;

     // ADMIN creates purchase order
     @PostMapping
//     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<PurchaseResponseDTO> create(@RequestBody PurchaseRequestDTO dto) {
          return ResponseEntity.ok(purchaseService.createPurchase(dto));
     }

     // SUPPLIER marks as delivered
     @PutMapping("/{id}/deliver")
//     @PreAuthorize("hasRole('SUPPLIER')")
     public ResponseEntity<PurchaseResponseDTO> deliver(@PathVariable Long id) {
          return ResponseEntity.ok(purchaseService.markAsDelivered(id));
     }

     // ADMIN or MANAGER confirms delivery → inventory updated
     @PutMapping("/{id}/confirm")
//     @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
     public ResponseEntity<PurchaseResponseDTO> confirm(@PathVariable Long id) {
          Authentication auth = SecurityContextHolder.getContext().getAuthentication();
          String email = auth.getName();
          User user = userRepo.findByEmail(email)
                  .orElseThrow(() -> new ResourceNotFoundException("User not found"));
          return ResponseEntity.ok(purchaseService.confirmDelivery(id, user.getId()));
     }

     // ADMIN or SUPPLIER cancels
     @PutMapping("/{id}/cancel")
//     @PreAuthorize("hasAnyRole('ADMIN', 'SUPPLIER')")
     public ResponseEntity<PurchaseResponseDTO> cancel(
             @PathVariable Long id,
             @RequestParam Long userId) {

          return ResponseEntity.ok(
                  purchaseService.cancelPurchase(id, userId)
          );
     }

     @GetMapping
//     @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
     public ResponseEntity<List<PurchaseResponseDTO>> getAll() {
          return ResponseEntity.ok(purchaseService.getAllPurchases());
     }

     @GetMapping("/supplier/{supplierId}")
//     @PreAuthorize("hasAnyRole('ADMIN', 'SUPPLIER', 'MANAGER')")
     public ResponseEntity<List<PurchaseResponseDTO>> getBySupplier(@PathVariable Long supplierId) {
          return ResponseEntity.ok(purchaseService.getBySupplier(supplierId));
     }

     @GetMapping("/status/{status}")
//     @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
     public ResponseEntity<List<PurchaseResponseDTO>> getByStatus(@PathVariable PurchaseStatus status) {
          return ResponseEntity.ok(purchaseService.getByStatus(status));
     }

     @GetMapping("/{id}")
//     @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPPLIER')")
     public ResponseEntity<PurchaseResponseDTO> getById(@PathVariable Long id) {
          return ResponseEntity.ok(purchaseService.getById(id));
     }
}