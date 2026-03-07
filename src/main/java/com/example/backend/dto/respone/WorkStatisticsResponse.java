package com.example.backend.dto.respone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkStatisticsResponse {
    private Long totalCompletedReports;
    private Double totalWeight;
    private Long correctlyClassifiedCount;
}
