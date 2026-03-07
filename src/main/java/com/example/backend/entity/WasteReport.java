package com.example.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "waste_report")
@Data
@NoArgsConstructor
public class WasteReport {

    public enum ReportStatus {
        PENDING,
        ACCEPTED,
        ASSIGNED,
        ON_THE_WAY,
        COLLECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ảnh báo cáo
    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    // Mô tả
    @Column(columnDefinition = "TEXT")
    private String description;

    // Tọa độ snapshot tại thời điểm báo cáo (copy từ UserAddress)
    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    // Thông tin snapshot từ UserAddress tại thời điểm tạo báo cáo
    @Column(name = "province_code")
    private String provinceCode;

    @Column(name = "ward_code")
    private String wardCode;

    @Column(name = "receiver_name")
    private String receiverName;

    @Column(name = "phone_number")
    private String phoneNumber;

    // Trạng thái
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    // Thời gian
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Citizen tạo báo cáo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User citizen;

    // Địa chỉ được chọn từ user_address
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_address_id", nullable = false)
    private UserAddress userAddress;

    // Loại rác
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private WasteCategory category;

    // Doanh nghiệp tiếp nhận yêu cầu (nullable - chưa được accept)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enterprise_id")
    private RecyclingEnterprise enterprise;

    // Collector được phân công (nullable - chưa được assign)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collector_id")
    private Collector collector;

    // Quy tắc điểm thưởng được áp dụng (nullable - chưa được accept)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_rule_id")
    private PointRule pointRule;

    // Ảnh xác nhận hoàn tất thu gom (nullable - chưa hoàn thành)
    @Column(name = "collected_image_url")
    private String collectedImageUrl;

    // Thời gian hoàn thành thu gom
    @Column(name = "collected_at")
    private LocalDateTime collectedAt;

    // Khối lượng rác thu được (kg)
    @Column(name = "weight")
    private Double weight;

    // Citizen có phân loại đúng loại rác không (Collector kiểm tra)
    @Column(name = "is_correctly_classified")
    private Boolean isCorrectlyClassified;

    // Tự set thời gian + trạng thái
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = ReportStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
