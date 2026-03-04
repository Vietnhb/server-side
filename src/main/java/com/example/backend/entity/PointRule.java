package com.example.backend.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point_rules")
@Data
@NoArgsConstructor
public class PointRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Doanh nghiệp tạo rule này
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enterprise_id", nullable = false)
    private RecyclingEnterprise enterprise;

    // Các loại rác áp dụng (empty = áp dụng cho tất cả)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "point_rule_categories", joinColumns = @JoinColumn(name = "rule_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
    private java.util.Set<WasteCategory> categories = new java.util.HashSet<>();

    // Tên rule
    @Column(nullable = false)
    private String ruleName;

    // Mô tả
    @Column(columnDefinition = "TEXT")
    private String description;

    // Điểm cơ bản cho mỗi báo cáo hợp lệ
    @Column(name = "base_points", nullable = false)
    private Integer basePoints;

    // Điểm thưởng theo khối lượng (điểm/kg)
    @Column(name = "points_per_kg")
    private Double pointsPerKg;

    // Điểm thưởng nếu phân loại đúng
    @Column(name = "correct_classification_bonus")
    private Integer correctClassificationBonus;

    // Rule có active không
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
