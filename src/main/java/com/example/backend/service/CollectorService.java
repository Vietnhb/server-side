package com.example.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dto.request.UpdateCollectionStatusRequest;
import com.example.backend.dto.respone.CollectorResponse;
import com.example.backend.dto.respone.WasteReportResponse;
import com.example.backend.dto.respone.WorkHistoryResponse;
import com.example.backend.dto.respone.WorkStatisticsResponse;
import com.example.backend.entity.Collector;
import com.example.backend.entity.User;
import com.example.backend.entity.WasteReport;
import com.example.backend.entity.WasteReport.ReportStatus;
import com.example.backend.exception.ApiException;
import com.example.backend.repository.CollectorRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.WasteReportRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CollectorService {

        /**
         * COLLECTOR SERVICE
         * 
         * Vai trò: Thực thi việc thu gom rác
         * 
         * Trách nhiệm:
         * 1. Nhận yêu cầu thu gom từ Enterprise
         * 2. Cập nhật trạng thái theo thời gian thực (ASSIGNED → ON_THE_WAY →
         * COLLECTED)
         * 3. Cung cấp dữ liệu xác nhận: ảnh, khối lượng rác
         * 4. TRIGGER việc tính điểm: Khi đánh dấu COLLECTED → gọi PointService
         * 
         * Lưu ý: Collector KHÔNG quyết định số điểm, chỉ cung cấp dữ liệu
         */

        private final CollectorRepository collectorRepository;
        private final UserRepository userRepository;
        private final WasteReportRepository wasteReportRepository;
        private final PointService pointService;

        // Xem thông tin collector của mình
        public CollectorResponse getMyCollectorInfo(String email) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

                Collector collector = collectorRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Collector profile not found"));

                return mapToResponse(collector);
        }

        // Xem danh sách yêu cầu được phân công
        public List<WasteReportResponse> getAssignedReports(String email) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

                Collector collector = collectorRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Collector profile not found"));

                List<WasteReport> reports = wasteReportRepository.findByCollectorId(collector.getId());
                return reports.stream()
                                .map(this::mapReportToResponse)
                                .collect(Collectors.toList());
        }

        // Cập nhật trạng thái thu gom
        @Transactional
        public WasteReportResponse updateCollectionStatus(String email, Long reportId,
                        UpdateCollectionStatusRequest request) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

                Collector collector = collectorRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Collector profile not found"));

                WasteReport report = wasteReportRepository.findById(reportId)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Report not found"));

                // Kiểm tra quyền sở hữu
                if (report.getCollector() == null || !report.getCollector().getId().equals(collector.getId())) {
                        throw new ApiException(HttpStatus.FORBIDDEN, "This report is not assigned to you");
                }

                // Cập nhật trạng thái
                ReportStatus newStatus;
                try {
                        newStatus = ReportStatus.valueOf(request.getStatus());
                } catch (IllegalArgumentException e) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid status");
                }

                // Validate status transition
                if (newStatus == ReportStatus.PENDING || newStatus == ReportStatus.ACCEPTED) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot set status to PENDING or ACCEPTED");
                }

                report.setStatus(newStatus);

                // Nếu status = COLLECTED: Collector hoàn thành công việc
                if (newStatus == ReportStatus.COLLECTED) {
                        // 1. Yêu cầu ảnh xác nhận (bắt buộc)
                        if (request.getCollectedImageUrl() == null || request.getCollectedImageUrl().isEmpty()) {
                                throw new ApiException(HttpStatus.BAD_REQUEST,
                                                "Collected image is required when marking as COLLECTED");
                        }
                        report.setCollectedImageUrl(request.getCollectedImageUrl());
                        report.setCollectedAt(LocalDateTime.now());

                        // 2. Cập nhật khối lượng nếu có (dữ liệu cho việc tính điểm)
                        if (request.getWeight() != null) {
                                report.setWeight(request.getWeight());
                        }

                        // 2.5. Cập nhật trạng thái phân loại đúng/sai (Collector kiểm tra)
                        if (request.getIsCorrectlyClassified() != null) {
                                report.setIsCorrectlyClassified(request.getIsCorrectlyClassified());
                        }

                        // 3. Cập nhật status collector về AVAILABLE
                        collector.setCurrentStatus(Collector.CollectorStatus.AVAILABLE);
                        collectorRepository.save(collector);

                        // 4. TRIGGER: Thực thi việc tính điểm thưởng
                        // - Dữ liệu từ Collector: weight, collectedAt, ảnh xác nhận
                        // - LUẬT từ Enterprise: PointRule (base points, points/kg, fast bonus)
                        // - Kết quả: Citizen nhận điểm tự động
                        WasteReport savedReport = wasteReportRepository.save(report);
                        pointService.calculateAndAwardPoints(savedReport);

                        return mapReportToResponse(savedReport);
                }

                // Cập nhật khối lượng nếu có
                if (request.getWeight() != null) {
                        report.setWeight(request.getWeight());
                }

                WasteReport updated = wasteReportRepository.save(report);

                // Force load lazy relationships within transaction
                if (updated.getCitizen() != null)
                        updated.getCitizen().getFullName();
                if (updated.getUserAddress() != null)
                        updated.getUserAddress().getDetailAddress();
                if (updated.getCategory() != null)
                        updated.getCategory().getName();

                WasteReportResponse response = mapReportToResponse(updated);
                return response;
        }

        // Cập nhật status collector (AVAILABLE/BUSY/ON_THE_WAY/OFFLINE)
        public CollectorResponse updateStatus(String email, String status) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

                Collector collector = collectorRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Collector profile not found"));

                Collector.CollectorStatus newStatus;
                try {
                        newStatus = Collector.CollectorStatus.valueOf(status);
                } catch (IllegalArgumentException e) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid status");
                }

                collector.setCurrentStatus(newStatus);
                Collector updated = collectorRepository.save(collector);
                return mapToResponse(updated);
        }

        // Xem lịch sử công việc
        public List<WorkHistoryResponse> getWorkHistory(String email) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

                Collector collector = collectorRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Collector profile not found"));

                List<WasteReport> completedReports = wasteReportRepository
                                .findByCollectorIdAndStatus(collector.getId(), ReportStatus.COLLECTED);

                return completedReports.stream()
                                .map(this::mapToWorkHistoryResponse)
                                .collect(Collectors.toList());
        }

        // Xem thống kê công việc
        public WorkStatisticsResponse getWorkStatistics(String email) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

                Collector collector = collectorRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Collector profile not found"));

                List<WasteReport> completedReports = wasteReportRepository
                                .findByCollectorIdAndStatus(collector.getId(), ReportStatus.COLLECTED);

                Long totalCompleted = (long) completedReports.size();
                Double totalWeight = completedReports.stream()
                                .mapToDouble(report -> report.getWeight() != null ? report.getWeight() : 0.0)
                                .sum();
                Long correctlyClassified = completedReports.stream()
                                .filter(report -> report.getIsCorrectlyClassified() != null
                                                && report.getIsCorrectlyClassified())
                                .count();

                return new WorkStatisticsResponse(totalCompleted, totalWeight, correctlyClassified);
        }

        private CollectorResponse mapToResponse(Collector collector) {
                return new CollectorResponse(
                                collector.getId(),
                                collector.getUser().getId(),
                                collector.getUser().getFullName(),
                                collector.getUser().getEmail(),
                                collector.getEnterprise().getId(),
                                collector.getEnterprise().getCompanyName(),
                                collector.getCurrentStatus().name());
        }

        private WasteReportResponse mapReportToResponse(WasteReport report) {
                WasteReportResponse response = new WasteReportResponse();
                response.setId(report.getId());
                response.setImageUrl(report.getImageUrl());
                response.setDescription(report.getDescription());
                response.setStatus(report.getStatus().name());
                response.setCreatedAt(report.getCreatedAt());
                response.setUpdatedAt(report.getUpdatedAt());
                response.setCitizenId(report.getCitizen().getId());
                response.setCitizenName(report.getCitizen().getFullName());
                response.setCitizenEmail(report.getCitizen().getEmail());
                response.setAddressId(report.getUserAddress().getId());
                response.setAddressDetail(report.getUserAddress().getDetailAddress());
                response.setAddressNumber(report.getUserAddress().getAddressNumber());
                response.setLatitude(report.getLatitude());
                response.setLongitude(report.getLongitude());
                response.setCategoryId(report.getCategory().getId());
                response.setCategoryName(report.getCategory().getName());
                response.setWeight(report.getWeight());
                response.setIsCorrectlyClassified(report.getIsCorrectlyClassified());
                response.setCollectedImageUrl(report.getCollectedImageUrl());
                return response;
        }

        private WorkHistoryResponse mapToWorkHistoryResponse(WasteReport report) {
                WorkHistoryResponse response = new WorkHistoryResponse();
                response.setReportId(report.getId());
                response.setCategoryName(report.getCategory().getName());
                response.setProvinceCode(report.getProvinceCode());
                response.setWardCode(report.getWardCode());
                response.setAddressDetail(report.getUserAddress().getDetailAddress());
                response.setWeight(report.getWeight());
                response.setIsCorrectlyClassified(report.getIsCorrectlyClassified());
                response.setCollectedAt(report.getCollectedAt());
                response.setCitizenName(report.getCitizen().getFullName());
                response.setCollectedImageUrl(report.getCollectedImageUrl());
                return response;
        }
}
