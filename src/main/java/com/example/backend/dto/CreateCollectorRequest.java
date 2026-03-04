package com.example.backend.dto;

import lombok.Data;

@Data
public class CreateCollectorRequest {
    // Dùng khi tạo collector từ user đã tồn tại
    private Long userId;
    
    // Dùng khi tạo collector mới (tạo cả tài khoản)
    private String email;
    private String fullName;
    private String password;
    
    private Long enterpriseId; // Enterprise ID (optional, sẽ lấy từ current user nếu không có)
}
