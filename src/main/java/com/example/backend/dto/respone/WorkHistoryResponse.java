package com.example.backend.dto.respone;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkHistoryResponse {
    private Long reportId;
    private String categoryName;
    private String provinceCode;
    private String wardCode;
    private String addressDetail;
    private Double weight;
    private Boolean isCorrectlyClassified;
    private LocalDateTime collectedAt;
    private String citizenName;
    private String collectedImageUrl;
}
