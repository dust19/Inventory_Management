package com.inventra.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesReportDTO {
     private String label;
     private Long totalOrders;
     private Double revenue;
     private Double profit;
     private Double cost;
     private Double marginPercent;     // revenue - purchase cost (approximated)
}