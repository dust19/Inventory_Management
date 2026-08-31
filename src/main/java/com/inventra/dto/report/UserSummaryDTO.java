package com.inventra.dto.report;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSummaryDTO {
     private Long id;
     private String name;
     private String email;
     private String role;
     private Boolean active;
     private LocalDateTime createdAt;
}
