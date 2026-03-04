package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WasteStatisticsResponse {
    private String categoryName;
    private String provinceCode;
    private String wardCode;
    private Long totalReports;
    private Double totalWeight;
    private Long correctlyClassifiedCount;
}
