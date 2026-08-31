package com.inventra.dto.response;

import lombok.Data;

@Data
public class CategoryResponseDTO {
    private Long id;
    private String name;
    private Long totalProducts;
}
