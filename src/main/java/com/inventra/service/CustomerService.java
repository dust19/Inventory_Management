package com.inventra.service;

import com.inventra.dto.request.CustomerRequestDTO;
import com.inventra.dto.response.CustomerResponseDTO;
import com.inventra.entity.Customer;
import com.inventra.exception.ResourceNotFoundException;
import com.inventra.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

     private final CustomerRepository customerRepo;

     @Transactional
     public CustomerResponseDTO createCustomer(CustomerRequestDTO dto) {
          if (dto.getName() == null || dto.getName().isBlank())
               throw new IllegalArgumentException("Customer name is required");
          if (dto.getPhone() == null || dto.getPhone().isBlank())
               throw new IllegalArgumentException("Customer phone is required");
          if (customerRepo.existsByPhone(dto.getPhone()))
               throw new IllegalArgumentException("Customer with this phone already exists");

          Customer customer = new Customer();
          customer.setName(dto.getName().trim());
          customer.setPhone(dto.getPhone().trim());
          customer.setEmail(dto.getEmail());
          customer.setAddress(dto.getAddress());

          return toDTO(customerRepo.save(customer));
     }

     public List<CustomerResponseDTO> getAllCustomers() {
          return customerRepo.findAll().stream().map(this::toDTO).toList();
     }

     public CustomerResponseDTO getCustomerById(Long id) {
          return toDTO(customerRepo.findById(id)
                  .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + id)));
     }

     public CustomerResponseDTO getCustomerByPhone(String phone) {
          return toDTO(customerRepo.findByPhone(phone)
                  .orElseThrow(() -> new ResourceNotFoundException("Customer not found with phone " + phone)));
     }

     @Transactional
     public CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO dto) {
          Customer existing = customerRepo.findById(id)
                  .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + id));

          if (dto.getName() != null && !dto.getName().isBlank())
               existing.setName(dto.getName().trim());

          if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
               customerRepo.findByPhone(dto.getPhone().trim()).ifPresent(c -> {
                    if (!c.getId().equals(existing.getId()))
                         throw new IllegalArgumentException("Phone number already in use");
               });
               existing.setPhone(dto.getPhone().trim());
          }

          if (dto.getEmail() != null) existing.setEmail(dto.getEmail());
          if (dto.getAddress() != null) existing.setAddress(dto.getAddress());

          return toDTO(customerRepo.save(existing));
     }

     @Transactional
     public void deleteCustomer(Long id) {
          Customer customer = customerRepo.findById(id)
                  .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + id));
          customerRepo.delete(customer);
     }

     private CustomerResponseDTO toDTO(Customer c) {
          CustomerResponseDTO dto = new CustomerResponseDTO();
          dto.setId(c.getId());
          dto.setName(c.getName());
          dto.setPhone(c.getPhone());
          dto.setEmail(c.getEmail());
          dto.setAddress(c.getAddress());
          dto.setCreatedAt(c.getCreatedAt());
          return dto;
     }
}