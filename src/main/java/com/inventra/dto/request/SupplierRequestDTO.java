package com.inventra.dto.request;

import lombok.Data;

@Data
public class SupplierRequestDTO {
    private Long userId;
    private Long categoryId;
    private String companyName;
    private String address;
}