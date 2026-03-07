package com.example.backend.dto.request;

import lombok.Getter;

@Getter
public class SignupRequest {
    private String email;
    private String fullName;
    private String password;
    private String role;
}
