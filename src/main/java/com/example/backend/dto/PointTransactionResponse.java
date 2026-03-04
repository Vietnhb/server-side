package com.example.backend.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointTransactionResponse {
    private Long id;
    private Long userId;
    private Long reportId;
    private String type;
    private Integer points;
    private String description;
    private LocalDateTime createdAt;
}
