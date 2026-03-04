package com.example.backend.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointHistoryResponse {
    private Long id;
    private Integer points;
    private Long reportId;
    private LocalDateTime createdAt;
    private String categoryName;
    private Double weight;
    private Boolean isCorrectlyClassified;
}
