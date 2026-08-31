package com.inventra.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupplierReportDTO {
     private Long supplierId;
     private String companyName;
     private Integer totalProducts;
     private Long totalStockSupplied;
     private Double totalPurchaseValue;
}