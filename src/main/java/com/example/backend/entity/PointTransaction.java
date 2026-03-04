package com.example.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point_transactions")
@Data
@NoArgsConstructor
public class PointTransaction {

    public enum TransactionType {
        EARN, // Nhận điểm từ báo cáo
        DEDUCT, // Trừ điểm (penalty)
        BONUS // Điểm thưởng đặc biệt
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User nhận/mất điểm
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Báo cáo liên quan (nullable nếu không liên quan đến báo cáo)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private WasteReport report;

    // Rule áp dụng (nullable nếu không có rule cụ thể)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id")
    private PointRule rule;

    // Loại giao dịch
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    // Số điểm (+/-)
    @Column(nullable = false)
    private Integer points;

    // Mô tả
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
