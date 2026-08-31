package com.inventra.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class PurchaseRequestDTO {
    private Long supplierId;
    private List<PurchaseItemRequestDTO> items;
}
