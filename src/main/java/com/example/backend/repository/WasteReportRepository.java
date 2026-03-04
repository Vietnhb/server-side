package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.entity.WasteReport;

@Repository
public interface WasteReportRepository extends JpaRepository<WasteReport, Long> {
    List<WasteReport> findByCitizenId(Long citizenId);

    List<WasteReport> findByCitizenIdOrderByCreatedAtDesc(Long citizenId);

    List<WasteReport> findByStatus(WasteReport.ReportStatus status);

    List<WasteReport> findByEnterpriseId(Long enterpriseId);

    List<WasteReport> findByCollectorId(Long collectorId);

    // Lấy danh sách report đã hoàn thành của collector
    List<WasteReport> findByCollectorIdAndStatus(Long collectorId, WasteReport.ReportStatus status);

    // Đếm số lượng report đã hoàn thành của collector
    Long countByCollectorIdAndStatus(Long collectorId, WasteReport.ReportStatus status);

    // Query để lấy ranking theo khu vực
    @Query("SELECT w.citizen.id as userId, w.citizen.fullName as userName, " +
            "SUM(COALESCE(pt.points, 0)) as totalPoints, COUNT(w.id) as totalReports, " +
            "w.provinceCode as provinceCode, w.wardCode as wardCode " +
            "FROM WasteReport w " +
            "LEFT JOIN PointTransaction pt ON pt.report.id = w.id " +
            "WHERE w.status = 'COLLECTED' AND w.wardCode = :wardCode " +
            "GROUP BY w.citizen.id, w.citizen.fullName, w.provinceCode, w.wardCode " +
            "ORDER BY totalPoints DESC")
    List<Object[]> findRankingByWard(@Param("wardCode") String wardCode);

    @Query("SELECT w.citizen.id as userId, w.citizen.fullName as userName, " +
            "SUM(COALESCE(pt.points, 0)) as totalPoints, COUNT(w.id) as totalReports, " +
            "w.provinceCode as provinceCode, w.wardCode as wardCode " +
            "FROM WasteReport w " +
            "LEFT JOIN PointTransaction pt ON pt.report.id = w.id " +
            "WHERE w.status = 'COLLECTED' AND w.provinceCode = :provinceCode " +
            "GROUP BY w.citizen.id, w.citizen.fullName, w.provinceCode, w.wardCode " +
            "ORDER BY totalPoints DESC")
    List<Object[]> findRankingByProvince(@Param("provinceCode") String provinceCode);

    // Query thống kê khối lượng rác cho Enterprise
    @Query("SELECT w.category.name as categoryName, w.provinceCode, w.wardCode, " +
            "COUNT(w.id) as totalReports, SUM(w.weight) as totalWeight, " +
            "SUM(CASE WHEN w.isCorrectlyClassified = true THEN 1 ELSE 0 END) as correctlyClassifiedCount " +
            "FROM WasteReport w " +
            "WHERE w.enterprise.id = :enterpriseId " +
            "AND w.status = 'COLLECTED' " +
            "AND (:categoryId IS NULL OR w.category.id = :categoryId) " +
            "AND (:provinceCode IS NULL OR w.provinceCode = :provinceCode) " +
            "AND (:wardCode IS NULL OR w.wardCode = :wardCode) " +
            "AND (CAST(:startDate AS timestamp) IS NULL OR w.createdAt >= :startDate) " +
            "AND (CAST(:endDate AS timestamp) IS NULL OR w.createdAt <= :endDate) " +
            "GROUP BY w.category.name, w.provinceCode, w.wardCode " +
            "ORDER BY totalWeight DESC")
    List<Object[]> findStatisticsByEnterprise(
            @Param("enterpriseId") Long enterpriseId,
            @Param("categoryId") Long categoryId,
            @Param("provinceCode") String provinceCode,
            @Param("wardCode") String wardCode,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);
}
