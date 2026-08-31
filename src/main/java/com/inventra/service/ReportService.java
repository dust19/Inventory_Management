package com.inventra.service;

import com.inventra.dto.report.*;
import com.inventra.entity.Product;
import com.inventra.entity.Purchase;
import com.inventra.entity.Sale;
import com.inventra.entity.User;
import com.inventra.entity.enums.Role;
import com.inventra.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class ReportService {

     private final SaleRepository saleRepository;
     private final SaleItemRepository saleItemRepository;
     private final ProductRepository productRepository;
     private final InventoryRepository inventoryRepository;
     private final SupplierRepository supplierRepository;
     private final PurchaseRepository purchaseRepository;
     private final UserRepository userRepository;
     private final CustomerRepository customerRepository;

     public List<SalesReportDTO> getSalesReport(LocalDate start, LocalDate end) {
          List<Sale> sales = (start == null || end == null)
                  ? saleRepository.findAllWithItems()
                  : saleRepository.findWithItemsBetween(
                  start.atStartOfDay(), end.atTime(23, 59, 59));
          return buildReport(sales);
     }

     public SalesReportDTO getSalesSummary(LocalDate start, LocalDate end) {
          List<Sale> sales = (start == null || end == null)
                  ? saleRepository.findAllWithItems()
                  : saleRepository.findWithItemsBetween(
                  start.atStartOfDay(), end.atTime(23, 59, 59));
          return buildSummary(sales);
     }

     private List<SalesReportDTO> buildReport(List<Sale> sales) {
          Map<LocalDate, List<Sale>> grouped = sales.stream()
                  .collect(Collectors.groupingBy(s -> s.getSaleDate().toLocalDate()));

          return grouped.entrySet().stream()
                  .sorted(Map.Entry.comparingByKey())
                  .map(e -> {
                       List<Sale> daySales = e.getValue();
                       double revenue = sumRevenue(daySales);
                       double cost = sumCost(daySales);
                       double profit = revenue - cost;
                       return new SalesReportDTO(
                               e.getKey().toString(),
                               (long) daySales.size(),
                               revenue,
                               profit,
                               cost,
                               revenue > 0 ? (profit / revenue) * 100 : 0
                       );
                  })
                  .collect(Collectors.toList());
     }

     private SalesReportDTO buildSummary(List<Sale> sales) {
          double revenue = sumRevenue(sales);
          double cost = sumCost(sales);
          double profit = revenue - cost;
          return new SalesReportDTO(
                  "Summary", (long) sales.size(), revenue, profit, cost,
                  revenue > 0 ? (profit / revenue) * 100 : 0
          );
     }

     private double sumRevenue(List<Sale> sales) {
          return sales.stream()
                  .map(Sale::getTotalAmount)
                  .filter(Objects::nonNull)
                  .mapToDouble(BigDecimal::doubleValue)
                  .sum();
     }

     private double sumCost(List<Sale> sales) {
          // actual cost = sum of (supplierToAdminPrice * qty) for each sale item
          return sales.stream()
                  .flatMap(s -> s.getItems().stream())
                  .mapToDouble(item -> {
                       BigDecimal cost = item.getProduct().getSupplierToAdminPrice();
                       return cost != null ? cost.doubleValue() * item.getQuantity() : 0;
                  })
                  .sum();
     }

     public List<InventoryReportDTO> getInventoryReport() {
          return inventoryRepository.findAll().stream().map(inv -> {
               Product p = inv.getProduct();

               double sellingPrice = p.getAdminToUserPrice() != null
                       ? p.getAdminToUserPrice().doubleValue() : 0.0;

               double costPrice = p.getSupplierToAdminPrice() != null
                       ? p.getSupplierToAdminPrice().doubleValue() : 0.0;

               long soldQty    = saleItemRepository.sumQuantityByProductId(p.getId());
               double revenue  = saleItemRepository.sumRevenueByProductId(p.getId());
               double stockVal = inv.getAvailableStock() * sellingPrice;

               String status;
               if (!p.isActive()) {
                    status = "INACTIVE";
               } else if (inv.getAvailableStock() == 0) {
                    status = "OUT_OF_STOCK";
               } else if (inv.getAvailableStock() <= inv.getReorderLevel()) {
                    status = "LOW_STOCK";
               } else {
                    status = "IN_STOCK";
               }

               return new InventoryReportDTO(
                       p.getId(),
                       p.getName(),
                       p.getSku(),
                       sellingPrice,
                       soldQty,
                       revenue,
                       inv.getAvailableStock(),
                       stockVal,
                       status,
                       inv.getLastUpdated()
               );
          }).collect(Collectors.toList());
     }

     public List<SupplierReportDTO> getSupplierReport() {
          return supplierRepository.findAll().stream().map(s -> {
               List<Product> products = productRepository.findBySupplierId(s.getId());
               long totalStock = products.stream().mapToLong(Product::getQuantity).sum();
               double purchaseValue = purchaseRepository.findBySupplierId(s.getId()).stream()
                       .map(Purchase::getTotalAmount)
                       .filter(Objects::nonNull)
                       .mapToDouble(BigDecimal::doubleValue)
                       .sum();
               return new SupplierReportDTO(
                       s.getId(), s.getCompanyName(),
                       products.size(), totalStock, purchaseValue
               );
          }).collect(Collectors.toList());
     }

     public List<CustomerReportDTO> getCustomerReport() {
          return customerRepository.findAll().stream().map(c -> {
               List<Sale> sales = saleRepository.findByCustomerId(c.getId());
               double spent = sales.stream()
                       .map(Sale::getTotalAmount)
                       .filter(Objects::nonNull)
                       .mapToDouble(BigDecimal::doubleValue)
                       .sum();
               LocalDate lastPurchase = sales.stream()
                       .map(s -> s.getSaleDate().toLocalDate())
                       .max(Comparator.naturalOrder()).orElse(null);
               return new CustomerReportDTO(
                       c.getId(), c.getName(), c.getEmail(),
                       (long) sales.size(), spent, lastPurchase
               );
          }).collect(Collectors.toList());
     }

     public UserActivityReportDTO getUserActivityReport() {
          List<User> all = userRepository.findAll();
          List<UserSummaryDTO> summaries = all.stream().map(u ->
                  new UserSummaryDTO(u.getId(), u.getName(), u.getEmail(),
                          u.getRole().name(), u.isActive(), u.getCreatedAt())
          ).collect(Collectors.toList());

          return new UserActivityReportDTO(
                  (long) all.size(),
                  all.stream().filter(User::isActive).count(),
                  all.stream().filter(u -> !u.isActive()).count(),
                  all.stream().filter(u -> u.getRole() == Role.ROLE_ADMIN).count(),
                  all.stream().filter(u -> u.getRole() == Role.ROLE_SUPPLIER).count(),
                  all.stream().filter(u -> u.getRole() == Role.ROLE_USER).count(),
                  summaries
          );
     }
}
