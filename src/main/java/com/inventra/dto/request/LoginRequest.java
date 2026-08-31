package com.inventra.dto.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;     // email OR phone
    private String password;
}
