package com.inventra.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ManagerResponseDTO {
     private Long id;
     private Long userId;
     private String userName;
     private String userEmail;
     private String department;
     private boolean active;
     private LocalDateTime createdAt;
}