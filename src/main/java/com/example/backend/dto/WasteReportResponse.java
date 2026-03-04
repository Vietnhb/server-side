package com.example.backend.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WasteReportResponse {
    private Long id;
    private String imageUrl;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Thông tin citizen
    private Long citizenId;
    private String citizenName;
    private String citizenEmail;

    // Thông tin địa chỉ
    private Long addressId;
    private String addressDetail;
    private Double latitude;
    private Double longitude;
    private String provinceCode;
    private String wardCode;
    private String receiverName;
    private String phoneNumber;

    // Thông tin loại rác
    private Long categoryId;
    private String categoryName;
}
