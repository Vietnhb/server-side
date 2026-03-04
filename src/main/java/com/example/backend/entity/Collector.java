package com.example.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "collectors")
@Data
@NoArgsConstructor
public class Collector {

    public enum CollectorStatus {
        AVAILABLE, // Sẵn sàng nhận việc
        BUSY, // Đang thực hiện công việc
        ON_THE_WAY, // Đang trên đường đến điểm thu gom
        OFFLINE // Không làm việc
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User tài khoản collector
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Doanh nghiệp quản lý collector này
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enterprise_id", nullable = false)
    private RecyclingEnterprise enterprise;

    // Trạng thái hiện tại
    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false)
    private CollectorStatus currentStatus;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.currentStatus == null) {
            this.currentStatus = CollectorStatus.OFFLINE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
