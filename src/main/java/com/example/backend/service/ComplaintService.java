package com.example.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.backend.dto.request.CreateComplaintRequest;
import com.example.backend.dto.request.ResolveComplaintRequest;
import com.example.backend.dto.respone.ComplaintResponse;
import com.example.backend.entity.Complaint;
import com.example.backend.entity.User;
import com.example.backend.entity.WasteReport;
import com.example.backend.exception.ApiException;
import com.example.backend.repository.ComplaintRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.WasteReportRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ComplaintService {

    private ComplaintRepository complaintRepository;
    private UserRepository userRepository;
    private WasteReportRepository wasteReportRepository;

    public ComplaintResponse createComplaint(String email, CreateComplaintRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        WasteReport report = wasteReportRepository.findById(request.getReportId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Report not found"));

        // Kiểm tra report đã completed chưa
        if (report.getStatus() != WasteReport.ReportStatus.COLLECTED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only completed reports can be complained");
        }

        // Kiểm tra report có phải của user không
        if (!report.getCitizen().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only complain about your own reports");
        }

        Complaint complaint = new Complaint();
        complaint.setReport(report);
        complaint.setUser(user);
        complaint.setDescription(request.getDescription());
        complaint.setStatus(Complaint.ComplaintStatus.PENDING);

        Complaint saved = complaintRepository.save(complaint);

        return mapToResponse(saved);
    }

    public List<ComplaintResponse> getMyComplaints(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        return complaintRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ComplaintResponse> getAllComplaints() {
        return complaintRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ComplaintResponse resolveComplaint(String adminEmail, Long complaintId, ResolveComplaintRequest request) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Admin not found"));

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Complaint not found"));

        if (complaint.getStatus() != Complaint.ComplaintStatus.PENDING) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Complaint already resolved");
        }

        Complaint.ComplaintStatus status;
        try {
            status = Complaint.ComplaintStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid status");
        }

        complaint.setStatus(status);
        complaint.setAdminNote(request.getAdminNote());
        complaint.setAdmin(admin);
        complaint.setResolvedAt(LocalDateTime.now());

        Complaint updated = complaintRepository.save(complaint);

        return mapToResponse(updated);
    }

    private ComplaintResponse mapToResponse(Complaint complaint) {
        return new ComplaintResponse(
                complaint.getId(),
                complaint.getReport().getId(),
                complaint.getUser().getId(),
                complaint.getUser().getFullName(),
                complaint.getDescription(),
                complaint.getStatus().name(),
                complaint.getAdminNote(),
                complaint.getAdmin() != null ? complaint.getAdmin().getId() : null,
                complaint.getAdmin() != null ? complaint.getAdmin().getFullName() : null,
                complaint.getCreatedAt(),
                complaint.getResolvedAt());
    }
}
