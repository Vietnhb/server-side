package com.example.backend.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComplaintResponse {
    private Long id;
    private Long reportId;
    private Long userId;
    private String userName;
    private String description;
    private String status;
    private String adminNote;
    private Long adminId;
    private String adminName;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}
