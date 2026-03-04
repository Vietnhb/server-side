package com.example.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recycling_enterprises")
@Data
@NoArgsConstructor
public class RecyclingEnterprise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User tài khoản doanh nghiệp
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    // Loại rác tiếp nhận (JSON hoặc comma-separated string)
    // Ví dụ: "ORGANIC,RECYCLABLE,HAZARDOUS"
    @Column(name = "accepted_waste_types", columnDefinition = "TEXT")
    private String acceptedWasteTypes;

    // Công suất xử lý (tấn/ngày hoặc kg/ngày)
    @Column(name = "capacity")
    private Double capacity;

    // Khu vực phục vụ (JSON hoặc text description)
    // Ví dụ: "District 1, District 2, Binh Thanh"
    @Column(name = "service_area", columnDefinition = "TEXT")
    private String serviceArea;

    // Đánh giá trung bình (0-5)
    @Column(name = "rating")
    private Double rating;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.rating == null) {
            this.rating = 0.0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
