package com.inventra.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseResponseDTO {
     private Long id;
     private Long supplierId;
     private String supplierName;
     private String status;
     private BigDecimal totalAmount;
     private Long confirmedById;
     private String confirmedByName;
     private LocalDateTime confirmedAt;
     private List<PurchaseItemResponseDTO> items;
}