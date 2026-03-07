package com.example.backend.dto.respone;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointRuleResponse {
    private Long id;
    private Long enterpriseId;
    private String enterpriseName;
    private java.util.List<Long> categoryIds;
    private String categoryNames; // Tên các loại rác ngăn cách bằng dấu phẩy
    private String ruleName;
    private String description;
    private Integer basePoints;
    private Double pointsPerKg;
    private Integer correctClassificationBonus;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
