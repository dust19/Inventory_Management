package com.inventra.controller;

import com.inventra.dto.request.CustomerRequestDTO;
import com.inventra.dto.response.CustomerResponseDTO;
import com.inventra.service.CustomerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "Customer Management", description = "Customer creation, lookup by phone and update APIs")
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

     private final CustomerService customerService;

     @PostMapping
     @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
     public ResponseEntity<CustomerResponseDTO> create(@RequestBody CustomerRequestDTO dto) {
          return ResponseEntity.status(HttpStatus.CREATED).body(customerService.createCustomer(dto));
     }

     @GetMapping
     @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
     public ResponseEntity<List<CustomerResponseDTO>> getAll() {
          return ResponseEntity.ok(customerService.getAllCustomers());
     }

     @GetMapping("/{id}")
     @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
     public ResponseEntity<CustomerResponseDTO> getById(@PathVariable Long id) {
          return ResponseEntity.ok(customerService.getCustomerById(id));
     }

     // staff uses this to look up a returning customer at billing time
     @GetMapping("/by-phone/{phone}")
     @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
     public ResponseEntity<CustomerResponseDTO> getByPhone(@PathVariable String phone) {
          return ResponseEntity.ok(customerService.getCustomerByPhone(phone));
     }

     @PutMapping("/{id}")
     @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
     public ResponseEntity<CustomerResponseDTO> update(@PathVariable Long id,
                                                       @RequestBody CustomerRequestDTO dto) {
          return ResponseEntity.ok(customerService.updateCustomer(id, dto));
     }

     @DeleteMapping("/{id}")
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<Void> delete(@PathVariable Long id) {
          customerService.deleteCustomer(id);
          return ResponseEntity.noContent().build();
     }
}