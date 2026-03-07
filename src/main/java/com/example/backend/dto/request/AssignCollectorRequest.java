package com.example.backend.dto.request;

import lombok.Data;

@Data
public class AssignCollectorRequest {
    private Long reportId;
    private Long collectorId;
}
