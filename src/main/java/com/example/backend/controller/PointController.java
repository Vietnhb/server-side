package com.example.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.request.CreatePointRuleRequest;
import com.example.backend.dto.respone.PointRuleResponse;
import com.example.backend.dto.respone.PointTransactionResponse;
import com.example.backend.service.PointService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/points")
@AllArgsConstructor
public class PointController {

    private final PointService pointService;

    // Enterprise tạo rule mới
    @PostMapping("/rules")
    public ResponseEntity<PointRuleResponse> createRule(
            Authentication authentication,
            @RequestBody CreatePointRuleRequest request) {
        String email = authentication.getName();
        PointRuleResponse response = pointService.createRule(email, request);
        return ResponseEntity.ok(response);
    }

    // Enterprise cập nhật rule
    @PutMapping("/rules/{ruleId}")
    public ResponseEntity<PointRuleResponse> updateRule(
            Authentication authentication,
            @PathVariable Long ruleId,
            @RequestBody CreatePointRuleRequest request) {
        String email = authentication.getName();
        PointRuleResponse response = pointService.updateRule(email, ruleId, request);
        return ResponseEntity.ok(response);
    }

    // Enterprise xem danh sách rules
    @GetMapping("/rules/my-rules")
    public ResponseEntity<List<PointRuleResponse>> getMyRules(Authentication authentication) {
        String email = authentication.getName();
        List<PointRuleResponse> rules = pointService.getMyRules(email);
        return ResponseEntity.ok(rules);
    }

    // Enterprise toggle rule active/inactive
    @PutMapping("/rules/{ruleId}/toggle")
    public ResponseEntity<PointRuleResponse> toggleRuleStatus(
            Authentication authentication,
            @PathVariable Long ruleId) {
        String email = authentication.getName();
        PointRuleResponse response = pointService.toggleRuleStatus(email, ruleId);
        return ResponseEntity.ok(response);
    }

    // Enterprise xóa rule
    @DeleteMapping("/rules/{ruleId}")
    public ResponseEntity<Void> deleteRule(
            Authentication authentication,
            @PathVariable Long ruleId) {
        String email = authentication.getName();
        pointService.deleteRule(email, ruleId);
        return ResponseEntity.ok().build();
    }

    // Citizen xem lịch sử giao dịch điểm
    @GetMapping("/transactions")
    public ResponseEntity<List<PointTransactionResponse>> getMyTransactions(Authentication authentication) {
        String email = authentication.getName();
        List<PointTransactionResponse> transactions = pointService.getMyTransactions(email);
        return ResponseEntity.ok(transactions);
    }
}
