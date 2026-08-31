package com.inventra.dto.request;

import com.inventra.entity.enums.SalePaymentMode;
import lombok.Data;
import java.util.List;

@Data
public class SaleRequestDTO {
     private Long soldById;      // userId of whoever is creating the sale
     private Long customerId;
     private SalePaymentMode paymentMode;
     private List<SaleItemRequestDTO> items;
}