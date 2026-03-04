package com.example.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.entity.PointRule;
import com.example.backend.entity.WasteCategory;

@Repository
public interface PointRuleRepository extends JpaRepository<PointRule, Long> {

    List<PointRule> findByEnterpriseId(Long enterpriseId);

    List<PointRule> findByEnterpriseIdAndIsActiveTrue(Long enterpriseId);

    // Tìm rule có chứa category cụ thể hoặc không có category nào (áp dụng cho tất
    // cả)
    @Query("SELECT pr FROM PointRule pr WHERE pr.enterprise.id = :enterpriseId AND pr.isActive = true " +
            "AND (SIZE(pr.categories) = 0 OR :category MEMBER OF pr.categories)")
    Optional<PointRule> findApplicableRule(@Param("enterpriseId") Long enterpriseId,
            @Param("category") WasteCategory category);
}
