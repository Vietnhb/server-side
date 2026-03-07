package com.example.backend.dto.respone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RankingUserResponse {
    private Long userId;
    private String userName;
    private Integer totalPoints;
    private Long totalReports;
    private Integer rank;
    private String provinceCode;
    private String wardCode;
}
