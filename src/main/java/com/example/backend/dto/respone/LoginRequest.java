package com.example.backend.dto.respone;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
