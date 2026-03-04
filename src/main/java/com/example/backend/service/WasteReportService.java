package com.example.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.backend.dto.CreateWasteReportRequest;
import com.example.backend.dto.WasteReportResponse;
import com.example.backend.entity.User;
import com.example.backend.entity.UserAddress;
import com.example.backend.entity.WasteCategory;
import com.example.backend.entity.WasteReport;
import com.example.backend.exception.ApiException;
import com.example.backend.repository.UserAddressRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.WasteCategoryRepository;
import com.example.backend.repository.WasteReportRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class WasteReportService {

        private final WasteReportRepository wasteReportRepository;
        private final UserRepository userRepository;
        private final UserAddressRepository userAddressRepository;
        private final WasteCategoryRepository wasteCategoryRepository;

        public WasteReportResponse createReport(String email, CreateWasteReportRequest request) {
                // Tìm user
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

                // Kiểm tra địa chỉ có thuộc user này không và phải đang active
                UserAddress address = userAddressRepository
                                .findByIdAndUserIdAndIsActiveTrue(request.getUserAddressId(), user.getId())
                                .orElseThrow(
                                                () -> new ApiException(HttpStatus.NOT_FOUND,
                                                                "Address not found, does not belong to user, or has been deleted"));

                // Kiểm tra category có tồn tại và active không
                WasteCategory category = wasteCategoryRepository.findById(request.getCategoryId())
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Waste category not found"));

                if (!category.isActive()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Waste category is not active");
                }

                // Tạo báo cáo
                WasteReport report = new WasteReport();
                report.setImageUrl(request.getImageUrl());
                report.setDescription(request.getDescription());
                // Copy tọa độ và thông tin quan trọng từ address (snapshot)
                report.setLatitude(address.getLatitude());
                report.setLongitude(address.getLongitude());
                report.setProvinceCode(address.getProvinceCode());
                report.setWardCode(address.getWardCode());
                report.setReceiverName(address.getReceiverName());
                report.setPhoneNumber(address.getPhoneNumber());
                report.setCitizen(user);
                report.setUserAddress(address);
                report.setCategory(category);
                // status, createdAt, updatedAt sẽ tự động set qua @PrePersist

                WasteReport savedReport = wasteReportRepository.save(report);

                return mapToResponse(savedReport);
        }

        public List<WasteReportResponse> getMyReports(String email) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

                return wasteReportRepository.findByCitizenIdOrderByCreatedAtDesc(user.getId())
                                .stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        public WasteReportResponse getReportById(String email, Long reportId) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

                WasteReport report = wasteReportRepository.findById(reportId)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Report not found"));

                // Kiểm tra quyền xem báo cáo
                if (!report.getCitizen().getId().equals(user.getId())) {
                        throw new ApiException(HttpStatus.FORBIDDEN, "You do not have permission to view this report");
                }

                return mapToResponse(report);
        }

        private WasteReportResponse mapToResponse(WasteReport report) {
                WasteReportResponse response = new WasteReportResponse();
                response.setId(report.getId());
                response.setImageUrl(report.getImageUrl());
                response.setDescription(report.getDescription());
                response.setStatus(report.getStatus().name());
                response.setCreatedAt(report.getCreatedAt());
                response.setUpdatedAt(report.getUpdatedAt());

                // Citizen info
                response.setCitizenId(report.getCitizen().getId());
                response.setCitizenName(report.getCitizen().getFullName());
                response.setCitizenEmail(report.getCitizen().getEmail());

                // Address info
                response.setAddressId(report.getUserAddress().getId());
                response.setAddressDetail(report.getUserAddress().getDetailAddress());
                response.setLatitude(report.getLatitude());
                response.setLongitude(report.getLongitude());
                response.setProvinceCode(report.getProvinceCode());
                response.setWardCode(report.getWardCode());
                response.setReceiverName(report.getReceiverName());
                response.setPhoneNumber(report.getPhoneNumber());

                // Category info
                response.setCategoryId(report.getCategory().getId());
                response.setCategoryName(report.getCategory().getName());

                return response;
        }
}
