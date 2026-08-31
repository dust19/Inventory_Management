package com.inventra.dto.response;

import com.inventra.entity.enums.SalePaymentMode;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SaleResponseDTO {
     private Long id;
     private Long soldById;
     private String soldByName;
     private String soldByRole;
     private Long customerId;
     private String customerName;
     private String customerPhone;
     private BigDecimal totalAmount;
     private SalePaymentMode paymentMode;
     private LocalDateTime saleDate;
     private List<SaleItemResponseDTO> items;
}