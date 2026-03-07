package com.example.backend.dto.respone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnterpriseResponse {
    private Long id;
    private Long userId;
    private String companyName;
    private String acceptedWasteTypes;
    private Double capacity;
    private String serviceArea;
    private Double rating;
}
