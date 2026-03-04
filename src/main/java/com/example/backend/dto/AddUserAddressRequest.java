package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddUserAddressRequest {
    private String receiverName;
    private String phoneNumber;
    private String detailAddress;
    private String addressNumber;
    private Double latitude;
    private Double longitude;
    private String provinceCode;
    private String wardCode;
    private Boolean isDefault;
}
