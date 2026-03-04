package com.example.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.CollectorResponse;
import com.example.backend.dto.UpdateCollectionStatusRequest;
import com.example.backend.dto.WasteReportResponse;
import com.example.backend.dto.WorkHistoryResponse;
import com.example.backend.dto.WorkStatisticsResponse;
import com.example.backend.service.CollectorService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/collector")
@AllArgsConstructor
public class CollectorController {

    private final CollectorService collectorService;

    // Xem thông tin collector của mình
    @GetMapping("/profile")
    public ResponseEntity<CollectorResponse> getMyCollectorInfo(Authentication authentication) {
        String email = authentication.getName();
        CollectorResponse response = collectorService.getMyCollectorInfo(email);
        return ResponseEntity.ok(response);
    }

    // Xem danh sách yêu cầu được phân công
    @GetMapping("/reports")
    public ResponseEntity<List<WasteReportResponse>> getAssignedReports(Authentication authentication) {
        String email = authentication.getName();
        List<WasteReportResponse> reports = collectorService.getAssignedReports(email);
        return ResponseEntity.ok(reports);
    }

    // Cập nhật trạng thái thu gom
    @PutMapping("/reports/{reportId}/status")
    public ResponseEntity<WasteReportResponse> updateCollectionStatus(
            Authentication authentication,
            @PathVariable Long reportId,
            @RequestBody UpdateCollectionStatusRequest request) {
        String email = authentication.getName();
        WasteReportResponse response = collectorService.updateCollectionStatus(email, reportId, request);
        return ResponseEntity.ok(response);
    }

    // Cập nhật status collector
    @PutMapping("/status")
    public ResponseEntity<CollectorResponse> updateStatus(
            Authentication authentication,
            @RequestParam String status) {
        String email = authentication.getName();
        CollectorResponse response = collectorService.updateStatus(email, status);
        return ResponseEntity.ok(response);
    }

    // Xem lịch sử công việc
    @GetMapping("/work-history")
    public ResponseEntity<List<WorkHistoryResponse>> getWorkHistory(Authentication authentication) {
        String email = authentication.getName();
        List<WorkHistoryResponse> history = collectorService.getWorkHistory(email);
        return ResponseEntity.ok(history);
    }

    // Xem thống kê công việc
    @GetMapping("/work-statistics")
    public ResponseEntity<WorkStatisticsResponse> getWorkStatistics(Authentication authentication) {
        String email = authentication.getName();
        WorkStatisticsResponse statistics = collectorService.getWorkStatistics(email);
        return ResponseEntity.ok(statistics);
    }
}
