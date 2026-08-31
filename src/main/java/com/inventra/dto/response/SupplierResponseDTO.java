package com.inventra.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupplierResponseDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long categoryId;
    private String categoryName;
    private String companyName;
    private String address;
    private boolean active;
    private LocalDateTime createdAt;   //auto generated
}