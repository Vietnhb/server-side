package com.example.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateWasteReportRequest {
    private String imageUrl;
    private String description;
    private Long userAddressId;
    private Long categoryId;
    private Double estimatedWeight; // Khối lượng ước tính (kg)
}
