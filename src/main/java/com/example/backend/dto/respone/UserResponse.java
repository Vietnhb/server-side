package com.example.backend.dto.respone;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {
    private Long Id;
    private String email;
    private String fullName;
    private String role;
    private Integer points; // Điểm thưởng (cho Citizen)

    public UserResponse(Long id, String email, String fullName) {
        this.Id = id;
        this.email = email;
        this.fullName = fullName;
    }

    public UserResponse(Long id, String email, String fullName, String role) {
        this.Id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }
}
