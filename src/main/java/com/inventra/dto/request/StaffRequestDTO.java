package com.inventra.dto.request;

import lombok.Data;

@Data
public class StaffRequestDTO {
     private Long userId;
     private Long managerId; // nullable
}