package com.inventra.service;

import com.inventra.dto.request.SaleItemRequestDTO;
import com.inventra.dto.request.SaleRequestDTO;
import com.inventra.dto.response.SaleItemResponseDTO;
import com.inventra.dto.response.SaleResponseDTO;
import com.inventra.entity.*;
import com.inventra.entity.enums.Role;
import com.inventra.entity.enums.SalePaymentMode;
import com.inventra.entity.enums.TransactionType;
import com.inventra.exception.ResourceNotFoundException;
import com.inventra.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleService {

     private final SaleRepository saleRepo;
     private final ProductRepository productRepo;
     private final UserRepository userRepo;
     private final CustomerRepository customerRepo;
     private final InventoryTransactionService inventoryTxService;
     private final ManagerRepository managerRepo;

     // ================= CREATE =================
     @Transactional
     public SaleResponseDTO createSale(SaleRequestDTO dto) {

          if (dto.getSoldById() == null)
               throw new IllegalArgumentException("soldById is required");
          if (dto.getCustomerId() == null)
               throw new IllegalArgumentException("customerId is required");
          if (dto.getItems() == null || dto.getItems().isEmpty())
               throw new IllegalArgumentException("Items are required");

          User soldBy = userRepo.findById(dto.getSoldById())
                  .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + dto.getSoldById()));

          // only STAFF, MANAGER, ADMIN are allowed to create sales
          if (soldBy.getRole() != Role.ROLE_STAFF
                  && soldBy.getRole() != Role.ROLE_MANAGER
                  && soldBy.getRole() != Role.ROLE_ADMIN) {
               throw new IllegalArgumentException("User role " + soldBy.getRole() + " is not allowed to create sales");
          }

          Customer customer = customerRepo.findById(dto.getCustomerId())
                  .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + dto.getCustomerId()));

          Sale sale = new Sale();
          sale.setSoldBy(soldBy);
          sale.setCustomer(customer);
          sale.setPaymentMode(dto.getPaymentMode());

          sale.setSaleDate(LocalDateTime.now());

          BigDecimal total = BigDecimal.ZERO;

          for (SaleItemRequestDTO itemDto : dto.getItems()) {

               if (itemDto.getProductId() == null || itemDto.getQuantity() == null || itemDto.getQuantity() <= 0)
                    throw new IllegalArgumentException("Invalid sale item");

               Product product = productRepo.findById(itemDto.getProductId())
                       .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

               if (product.getAdminToUserPrice() == null)
                    throw new IllegalStateException("Selling price not set for product: " + product.getSku());

               BigDecimal price = product.getAdminToUserPrice();

               SaleItem item = new SaleItem();
               item.setSale(sale);
               item.setProduct(product);
               item.setQuantity(itemDto.getQuantity());
               item.setPrice(price);

               BigDecimal subTotal = price.multiply(BigDecimal.valueOf(itemDto.getQuantity()));
               item.setSubtotal(subTotal);

               sale.getItems().add(item);
               total = total.add(subTotal);

               // decrease inventory + log STOCK_OUT + triggers reorder check inside
               inventoryTxService.process(product.getId(), TransactionType.STOCK_OUT, itemDto.getQuantity());
          }

          sale.setTotalAmount(total);
          return toDTO(saleRepo.save(sale));
     }

     // ================= GET ALL =================
     public List<SaleResponseDTO> getAllSales() {
          return saleRepo.findAll().stream().map(this::toDTO).toList();
     }

     // ================= GET BY SOLD BY =================
     public List<SaleResponseDTO> getBySoldBy(Long userId) {
          return saleRepo.findBySoldByIdOrderBySaleDateDesc(userId)
                  .stream().map(this::toDTO).toList();
     }

     public List<SaleResponseDTO> getSalesByManagerUser(Long userId) {

          Manager manager = managerRepo.findByUserId(userId)
                  .orElseThrow(() ->
                          new ResourceNotFoundException("Manager not found"));

          return saleRepo.findSalesByManagerId(manager.getId())
                  .stream()
                  .map(this::toDTO)
                  .toList();
     }

     // ================= GET BY ID =================
     public SaleResponseDTO getById(Long id) {
          return toDTO(saleRepo.findById(id)
                  .orElseThrow(() -> new ResourceNotFoundException("Sale not found")));
     }

     // ================= MAPPER =================
     private SaleResponseDTO toDTO(Sale sale) {
          SaleResponseDTO dto = new SaleResponseDTO();
          dto.setId(sale.getId());
          dto.setTotalAmount(sale.getTotalAmount());
          dto.setPaymentMode(sale.getPaymentMode());
          dto.setSaleDate(sale.getSaleDate());

          if (sale.getSoldBy() != null) {
               dto.setSoldById(sale.getSoldBy().getId());
               dto.setSoldByName(sale.getSoldBy().getName());
               dto.setSoldByRole(sale.getSoldBy().getRole().name());
          }

          if (sale.getCustomer() != null) {
               dto.setCustomerId(sale.getCustomer().getId());
               dto.setCustomerName(sale.getCustomer().getName());
               dto.setCustomerPhone(sale.getCustomer().getPhone());
          }

          dto.setItems(
                  sale.getItems().stream().map(item -> {
                       SaleItemResponseDTO i = new SaleItemResponseDTO();
                       i.setId(item.getId());
                       i.setProductId(item.getProduct().getId());
                       i.setProductName(item.getProduct().getName());
                       i.setQuantity(item.getQuantity());
                       i.setPrice(item.getPrice());
                       i.setSubtotal(item.getSubtotal());
                       return i;
                  }).toList()
          );

          return dto;
     }
}