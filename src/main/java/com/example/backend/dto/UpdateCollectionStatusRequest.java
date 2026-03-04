package com.example.backend.dto;

import lombok.Data;

@Data
public class UpdateCollectionStatusRequest {
    private String status; // "ASSIGNED", "ON_THE_WAY", "COLLECTED"
    private String collectedImageUrl; // Required khi status = COLLECTED
    private Double weight; // Khối lượng rác (kg) - optional
    private Boolean isCorrectlyClassified; // Citizen có phân loại đúng rác không - collector check
}
