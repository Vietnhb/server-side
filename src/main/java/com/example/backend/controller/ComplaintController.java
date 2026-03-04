package com.example.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.ComplaintResponse;
import com.example.backend.dto.CreateComplaintRequest;
import com.example.backend.dto.ResolveComplaintRequest;
import com.example.backend.service.ComplaintService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/complaint")
@AllArgsConstructor
public class ComplaintController {

    private ComplaintService complaintService;

    // Citizen tạo khiếu nại
    @PostMapping("/create")
    public ResponseEntity<ComplaintResponse> createComplaint(
            Authentication authentication,
            @RequestBody CreateComplaintRequest request) {
        String email = authentication.getName();
        ComplaintResponse response = complaintService.createComplaint(email, request);
        return ResponseEntity.ok(response);
    }

    // Citizen xem khiếu nại của mình
    @GetMapping("/my-complaints")
    public ResponseEntity<List<ComplaintResponse>> getMyComplaints(Authentication authentication) {
        String email = authentication.getName();
        List<ComplaintResponse> complaints = complaintService.getMyComplaints(email);
        return ResponseEntity.ok(complaints);
    }

    // Admin xem tất cả khiếu nại
    @GetMapping("/all")
    public ResponseEntity<List<ComplaintResponse>> getAllComplaints() {
        List<ComplaintResponse> complaints = complaintService.getAllComplaints();
        return ResponseEntity.ok(complaints);
    }

    // Admin giải quyết khiếu nại
    @PutMapping("/{id}/resolve")
    public ResponseEntity<ComplaintResponse> resolveComplaint(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody ResolveComplaintRequest request) {
        String email = authentication.getName();
        ComplaintResponse response = complaintService.resolveComplaint(email, id, request);
        return ResponseEntity.ok(response);
    }
}
