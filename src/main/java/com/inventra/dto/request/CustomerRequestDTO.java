package com.inventra.dto.request;

import lombok.Data;

@Data
public class CustomerRequestDTO {
     private String name;
     private String phone;
     private String email;
     private String address;
}