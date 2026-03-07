package com.example.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.request.CreateWasteReportRequest;
import com.example.backend.dto.respone.WasteReportResponse;
import com.example.backend.service.WasteReportService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/report/")
@AllArgsConstructor
public class UserReportController {

    private final WasteReportService wasteReportService;

    @PostMapping("create")
    public ResponseEntity<WasteReportResponse> createReport(
            Authentication authentication,
            @RequestBody CreateWasteReportRequest request) {
        String email = authentication.getName();
        WasteReportResponse response = wasteReportService.createReport(email, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("my-reports")
    public ResponseEntity<List<WasteReportResponse>> getMyReports(Authentication authentication) {
        String email = authentication.getName();
        List<WasteReportResponse> reports = wasteReportService.getMyReports(email);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("{id}")
    public ResponseEntity<WasteReportResponse> getReportById(
            Authentication authentication,
            @PathVariable Long id) {
        String email = authentication.getName();
        WasteReportResponse report = wasteReportService.getReportById(email, id);
        return ResponseEntity.ok(report);
    }
}
