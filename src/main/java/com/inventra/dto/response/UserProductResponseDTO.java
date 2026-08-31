package com.inventra.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UserProductResponseDTO {
    private String name;
    private String description;
    private BigDecimal adminToUserPrice;
}

