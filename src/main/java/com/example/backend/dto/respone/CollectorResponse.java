package com.example.backend.dto.respone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectorResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long enterpriseId;
    private String enterpriseName;
    private String currentStatus;
}
