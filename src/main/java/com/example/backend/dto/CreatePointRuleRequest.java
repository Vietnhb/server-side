package com.example.backend.dto;

import java.util.List;
import lombok.Data;

@Data
public class CreatePointRuleRequest {
    private List<Long> categoryIds; // empty/null = rule áp dụng cho tất cả loại rác
    private String ruleName;
    private String description;
    private Integer basePoints;
    private Double pointsPerKg;
    private Integer correctClassificationBonus;
}
