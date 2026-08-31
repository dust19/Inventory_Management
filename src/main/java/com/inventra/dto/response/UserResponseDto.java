package com.inventra.dto.response;

import com.inventra.entity.enums.Role;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponseDto {

     private Long id;
     private String name;
     private String email;
     private String phone;
     private String address;
     private boolean active;
     private LocalDateTime createdAt;
     private LocalDateTime updatedAt;
     private Role role;
}