package com.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_address")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    private String receiverName;
    private String phoneNumber;

    @Column(columnDefinition = "TEXT")
    private String detailAddress;

    private String addressNumber;

    private Double latitude;
    private Double longitude;

    private String provinceCode;
    private String wardCode;

    private Boolean isDefault = false;

    // Soft delete: false = đã xóa, true = đang hoạt động
    @Column(nullable = false)
    private Boolean isActive = true;
}
