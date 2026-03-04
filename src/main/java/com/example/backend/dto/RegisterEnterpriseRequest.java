package com.example.backend.dto;

import lombok.Data;

@Data
public class RegisterEnterpriseRequest {
    private String companyName;
    private String acceptedWasteTypes; // Comma-separated: "ORGANIC,RECYCLABLE,HAZARDOUS"
    private Double capacity; // Công suất xử lý (kg/ngày)
    private String serviceArea; // Khu vực phục vụ
}
