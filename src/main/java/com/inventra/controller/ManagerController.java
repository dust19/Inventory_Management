package com.inventra.controller;

import com.inventra.dto.request.ManagerRequestDTO;
import com.inventra.dto.response.ManagerResponseDTO;
import com.inventra.service.ManagerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "Manager Management", description = "Manager creation, update, activation and lookup APIs")
@RestController
@RequestMapping("/api/managers")
@RequiredArgsConstructor
public class ManagerController {

     private final ManagerService managerService;

     @PostMapping
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<ManagerResponseDTO> create(@RequestBody ManagerRequestDTO dto) {
          return ResponseEntity.status(HttpStatus.CREATED).body(managerService.createManager(dto));
     }

     @GetMapping
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<List<ManagerResponseDTO>> getAll() {
          return ResponseEntity.ok(managerService.getAllManagers());
     }

     @GetMapping("/{id}")
     @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
     public ResponseEntity<ManagerResponseDTO> getById(@PathVariable Long id) {
          return ResponseEntity.ok(managerService.getManagerById(id));
     }

     @GetMapping("/by-user/{userId}")
     @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
     public ResponseEntity<ManagerResponseDTO> getByUserId(@PathVariable Long userId) {
          return ResponseEntity.ok(managerService.getManagerByUserId(userId));
     }

     @PutMapping("/{id}")
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<ManagerResponseDTO> update(@PathVariable Long id,
                                                      @RequestBody ManagerRequestDTO dto) {
          return ResponseEntity.ok(managerService.updateManager(id, dto));
     }

     @PutMapping("/{id}/active")
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<ManagerResponseDTO> setActive(@PathVariable Long id,
                                                         @RequestParam boolean active) {
          return ResponseEntity.ok(managerService.setActive(id, active));
     }

     @DeleteMapping("/{id}")
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<Void> delete(@PathVariable Long id) {
          managerService.deleteManager(id);
          return ResponseEntity.noContent().build();
     }
}