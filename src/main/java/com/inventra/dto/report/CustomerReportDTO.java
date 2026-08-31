package com.inventra.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerReportDTO {
     private Long userId;
     private String name;
     private String email;
     private Long totalOrders;
     private Double totalSpent;
     private LocalDate lastPurchase;
}