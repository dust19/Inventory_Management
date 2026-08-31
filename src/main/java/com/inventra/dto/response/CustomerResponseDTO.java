package com.inventra.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CustomerResponseDTO {
     private Long id;
     private String name;
     private String phone;
     private String email;
     private String address;
     private LocalDateTime createdAt;
}