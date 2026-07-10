package com.example.backend.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.request.AssignCollectorRequest;
import com.example.backend.dto.request.CreateCollectorRequest;
import com.example.backend.dto.request.RegisterEnterpriseRequest;
import com.example.backend.dto.respone.CollectorResponse;
import com.example.backend.dto.respone.EnterpriseResponse;
import com.example.backend.dto.respone.WasteReportResponse;
import com.example.backend.dto.respone.WasteStatisticsResponse;
import com.example.backend.service.EnterpriseService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/enterprise")
@AllArgsConstructor
public class EnterpriseController {

    private final EnterpriseService enterpriseService;

    // Đăng ký doanh nghiệp
    @PostMapping("/register")
    public ResponseEntity<EnterpriseResponse> registerEnterprise(
            Authentication authentication,
            @RequestBody RegisterEnterpriseRequest request) {
        String email = authentication.getName();
        EnterpriseResponse response = enterpriseService.registerEnterprise(email, request);
        return ResponseEntity.ok(response);
    }

    // Cập nhật thông tin doanh nghiệp
    @PutMapping("/profile")
    public ResponseEntity<EnterpriseResponse> updateEnterprise(
            Authentication authentication,
            @RequestBody RegisterEnterpriseRequest request) {
        String email = authentication.getName();
        EnterpriseResponse response = enterpriseService.updateEnterprise(email, request);
        return ResponseEntity.ok(response);
    }

    // Xem thông tin doanh nghiệp của mình
    @GetMapping("/profile")
    public ResponseEntity<EnterpriseResponse> getMyEnterprise(Authentication authentication) {
        String email = authentication.getName();
        EnterpriseResponse response = enterpriseService.getMyEnterprise(email);
        return ResponseEntity.ok(response);
    }

    // Xem danh sách yêu cầu PENDING (chưa được accept)
    @GetMapping("/reports/pending")
    public ResponseEntity<List<WasteReportResponse>> getPendingReports(Authentication authentication) {
        String email = authentication.getName();
        List<WasteReportResponse> reports = enterpriseService.getPendingReports(email);
        return ResponseEntity.ok(reports);
    }

    // Tiếp nhận yêu cầu thu gom
    @PostMapping("/reports/{reportId}/accept")
    public ResponseEntity<WasteReportResponse> acceptReport(
            Authentication authentication,
            @PathVariable Long reportId,
            @RequestParam(required = false) Long ruleId) {
        String email = authentication.getName();
        WasteReportResponse response = enterpriseService.acceptReport(email, reportId, ruleId);
        return ResponseEntity.ok(response);
    }

    // Từ chối yêu cầu thu gom
    @PostMapping("/reports/{reportId}/reject")
    public ResponseEntity<Void> rejectReport(
            Authentication authentication,
            @PathVariable Long reportId) {
        String email = authentication.getName();
        enterpriseService.rejectReport(email, reportId);
        return ResponseEntity.ok().build();
    }

    // Xem các yêu cầu đã tiếp nhận
    @GetMapping("/reports/accepted")
    public ResponseEntity<List<WasteReportResponse>> getAcceptedReports(Authentication authentication) {
        String email = authentication.getName();
        List<WasteReportResponse> reports = enterpriseService.getAcceptedReports(email);
        return ResponseEntity.ok(reports);
    }

    // Gán collector cho yêu cầu
    @PostMapping("/reports/assign")
    public ResponseEntity<WasteReportResponse> assignCollector(
            Authentication authentication,
            @RequestBody AssignCollectorRequest request) {
        String email = authentication.getName();
        WasteReportResponse response = enterpriseService.assignCollector(email, request);
        return ResponseEntity.ok(response);
    }

    // QUẢN LÝ COLLECTOR

    // Tạo collector mới
    @PostMapping("/collectors")
    public ResponseEntity<CollectorResponse> createCollector(
            Authentication authentication,
            @RequestBody CreateCollectorRequest request) {
        String email = authentication.getName();
        CollectorResponse response = enterpriseService.createCollector(email, request);
        return ResponseEntity.ok(response);
    }

    // Lấy danh sách collectors của enterprise
    @GetMapping("/collectors")
    public ResponseEntity<List<CollectorResponse>> getMyCollectors(Authentication authentication) {
        String email = authentication.getName();
        List<CollectorResponse> collectors = enterpriseService.getMyCollectors(email);
        return ResponseEntity.ok(collectors);
    }

    // Xóa collector
    @DeleteMapping("/collectors/{collectorId}")
    public ResponseEntity<Void> deleteCollector(
            Authentication authentication,
            @PathVariable Long collectorId) {
        String email = authentication.getName();
        enterpriseService.deleteCollector(email, collectorId);
        return ResponseEntity.ok().build();
    }

    // Thống kê khối lượng rác
    @GetMapping("/statistics")
    public ResponseEntity<List<WasteStatisticsResponse>> getStatistics(
            Authentication authentication,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String provinceCode,
            @RequestParam(required = false) String wardCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String email = authentication.getName();

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        List<WasteStatisticsResponse> stats = enterpriseService.getStatistics(
                email, categoryId, provinceCode, wardCode, startDateTime, endDateTime);
        return ResponseEntity.ok(stats);
    }
}
