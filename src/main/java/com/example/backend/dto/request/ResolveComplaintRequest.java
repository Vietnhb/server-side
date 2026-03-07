package com.example.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResolveComplaintRequest {
    private String status; // RESOLVED or REJECTED
    private String adminNote;
}
