package com.inventra.controller;

import com.inventra.dto.report.SalesReportDTO;
import com.inventra.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ReportController {

     private final ReportService reportService;

     @GetMapping("/sales")
     public ResponseEntity<List<SalesReportDTO>> getSalesReport(
             @RequestParam(required = false) String start,
             @RequestParam(required = false) String end) {
          LocalDate s = start != null ? LocalDate.parse(start) : null;
          LocalDate e = end != null ? LocalDate.parse(end) : null;
          return ResponseEntity.ok(reportService.getSalesReport(s, e));
     }

     @GetMapping("/sales/summary")
     public ResponseEntity<SalesReportDTO> getSalesSummary(
             @RequestParam(required = false) String start,
             @RequestParam(required = false) String end) {
          LocalDate s = start != null ? LocalDate.parse(start) : null;
          LocalDate e = end != null ? LocalDate.parse(end) : null;
          return ResponseEntity.ok(reportService.getSalesSummary(s, e));
     }

     @GetMapping("/inventory")
     public ResponseEntity<?> inventoryReport() {
          return ResponseEntity.ok(reportService.getInventoryReport());
     }

     @GetMapping("/suppliers")
     public ResponseEntity<?> supplierReport() {
          return ResponseEntity.ok(reportService.getSupplierReport());
     }

     @GetMapping("/customers")
     public ResponseEntity<?> customerReport() {
          return ResponseEntity.ok(reportService.getCustomerReport());
     }

     @GetMapping("/users")
     public ResponseEntity<?> userActivityReport() {
          return ResponseEntity.ok(reportService.getUserActivityReport());
     }
}