package com.inventra.controller;

import com.inventra.dto.request.StaffRequestDTO;
import com.inventra.dto.response.StaffResponseDTO;
import com.inventra.service.StaffService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "Staff Management", description = "Staff creation, manager assignment, activation and lookup APIs")
@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

     private final StaffService staffService;

     @PostMapping
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<StaffResponseDTO> create(@RequestBody StaffRequestDTO dto) {
          return ResponseEntity.status(HttpStatus.CREATED).body(staffService.createStaff(dto));
     }

     @GetMapping
     @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
     public ResponseEntity<List<StaffResponseDTO>> getAll() {
          return ResponseEntity.ok(staffService.getAllStaff());
     }

     @GetMapping("/{id}")
     @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
     public ResponseEntity<StaffResponseDTO> getById(@PathVariable Long id) {
          return ResponseEntity.ok(staffService.getStaffById(id));
     }

     @GetMapping("/by-user/{userId}")
     @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
     public ResponseEntity<StaffResponseDTO> getByUserId(@PathVariable Long userId) {
          return ResponseEntity.ok(staffService.getStaffByUserId(userId));
     }

     @GetMapping("/by-manager/{managerId}")
     @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
     public ResponseEntity<List<StaffResponseDTO>> getByManager(@PathVariable Long managerId) {
          return ResponseEntity.ok(staffService.getStaffByManager(managerId));
     }

     @PutMapping("/{staffId}/assign-manager/{managerId}")
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<StaffResponseDTO> assignManager(@PathVariable Long staffId,
                                                           @PathVariable Long managerId) {
          return ResponseEntity.ok(staffService.assignManager(staffId, managerId));
     }

     @PutMapping("/{id}")
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<StaffResponseDTO> update(@PathVariable Long id,
                                                    @RequestBody StaffRequestDTO dto) {
          return ResponseEntity.ok(staffService.updateStaff(id, dto));
     }

     @PutMapping("/{id}/active")
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<StaffResponseDTO> setActive(@PathVariable Long id,
                                                       @RequestParam boolean active) {
          return ResponseEntity.ok(staffService.setActive(id, active));
     }

     @DeleteMapping("/{id}")
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<Void> delete(@PathVariable Long id) {
          staffService.deleteStaff(id);
          return ResponseEntity.noContent().build();
     }
}