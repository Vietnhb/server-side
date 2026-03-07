package com.example.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserAddressRequest {
    private String receiverName;
    private String phoneNumber;
    private String detailAddress;
    private String addressNumber;
    private Double latitude;
    private Double longitude;
    private Boolean isDefault;
}
