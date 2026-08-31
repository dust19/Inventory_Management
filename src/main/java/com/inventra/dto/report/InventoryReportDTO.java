package com.inventra.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryReportDTO {
     private Long productId;
     private String productName;
     private String sku;
     private Double unitPrice;        // adminToUserPrice
     private Long soldQty;
     private Double revenue;
     private Integer currentStock;
     private Double stockValue;       // currentStock * unitPrice
     private String status;           // IN_STOCK / LOW_STOCK / OUT_OF_STOCK / INACTIVE
     private LocalDateTime lastUpdated;
}