package com.inventra.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StaffResponseDTO {
     private Long id;
     private Long userId;
     private String userName;
     private String userEmail;
     private Long managerId;
     private String managerName;
     private boolean active;
     private LocalDateTime createdAt;
}