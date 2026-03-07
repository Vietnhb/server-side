package com.example.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.backend.dto.request.AssignCollectorRequest;
import com.example.backend.dto.request.CreateCollectorRequest;
import com.example.backend.dto.request.RegisterEnterpriseRequest;
import com.example.backend.dto.respone.CollectorResponse;
import com.example.backend.dto.respone.EnterpriseResponse;
import com.example.backend.dto.respone.WasteReportResponse;
import com.example.backend.dto.respone.WasteStatisticsResponse;
import com.example.backend.entity.Collector;
import com.example.backend.entity.PointRule;
import com.example.backend.entity.RecyclingEnterprise;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.entity.WasteReport;
import com.example.backend.entity.WasteReport.ReportStatus;
import com.example.backend.exception.ApiException;
import com.example.backend.repository.CollectorRepository;
import com.example.backend.repository.PointRuleRepository;
import com.example.backend.repository.RecyclingEnterpriseRepository;
import com.example.backend.repository.RoleRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.WasteReportRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class EnterpriseService {

        /**
         * RECYCLING ENTERPRISE SERVICE
         * 
         * Vai trò: Quản lý hoạt động của doanh nghiệp tái chế
         * 
         * Quyền hạn:
         * 1. Đăng ký và quản lý thông tin doanh nghiệp
         * 2. Tiếp nhận hoặc từ chối yêu cầu thu gom
         * 3. Gán Collector cho từng yêu cầu
         * 4. Theo dõi tiến độ xử lý
         * 5. Thiết lập LUẬT tính điểm thưởng (qua PointService)
         */

        private final RecyclingEnterpriseRepository enterpriseRepository;
        private final UserRepository userRepository;
        private final WasteReportRepository wasteReportRepository;
        private final CollectorRepository collectorRepository;
        private final RoleRepository roleRepository;
        private final PointRuleRepository pointRuleRepository;
        private final PasswordEncoder passwordEncoder;

        // Đăng ký doanh nghiệp
        public EnterpriseResponse registerEnterprise(String email, RegisterEnterpriseRequest request) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

                // Kiểm tra xem user đã đăng ký enterprise chưa
                if (enterpriseRepository.existsByUserId(user.getId())) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "User already has an enterprise");
                }

                RecyclingEnterprise enterprise = new RecyclingEnterprise();
                enterprise.setUser(user);
                enterprise.setCompanyName(request.getCompanyName());
                enterprise.setAcceptedWasteTypes(request.getAcceptedWasteTypes());
                enterprise.setCapacity(request.getCapacity());
                enterprise.setServiceArea(request.getServiceArea());

                RecyclingEnterprise saved = enterpriseRepository.save(enterprise);
                return mapToResponse(saved);
        }

        // Cập nhật thông tin doanh nghiệp
        public EnterpriseResponse updateEnterprise(String email, RegisterEnterpriseRequest request) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

                RecyclingEnterprise enterprise = enterpriseRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Enterprise not found"));

                if (request.getCompanyName() != null) {
                        enterprise.setCompanyName(request.getCompanyName());
                }
                if (request.getAcceptedWasteTypes() != null) {
                        enterprise.setAcceptedWasteTypes(request.getAcceptedWasteTypes());
                }
                if (request.getCapacity() != null) {
                        enterprise.setCapacity(request.getCapacity());
                }
                if (request.getServiceArea() != null) {
                        enterprise.setServiceArea(request.getServiceArea());
                }

                RecyclingEnterprise updated = enterpriseRepository.save(enterprise);
                return mapToResponse(updated);
        }

        // Xem thông tin doanh nghiệp của mình
        public EnterpriseResponse getMyEnterprise(String email) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

                RecyclingEnterprise enterprise = enterpriseRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Enterprise not found"));

                return mapToResponse(enterprise);
        }

        // Xem danh sách yêu cầu thu gom PENDING (chưa được accept)
        public List<WasteReportResponse> getPendingReports() {
                List<WasteReport> reports = wasteReportRepository.findByStatus(ReportStatus.PENDING);
                return reports.stream()
                                .map(this::mapReportToResponse)
                                .collect(Collectors.toList());
        }

        // Tiếp nhận yêu cầu thu gom (PENDING -> ACCEPTED)
        public WasteReportResponse acceptReport(String email, Long reportId, Long ruleId) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

                RecyclingEnterprise enterprise = enterpriseRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Enterprise not found"));

                WasteReport report = wasteReportRepository.findById(reportId)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Report not found"));

                if (report.getStatus() != ReportStatus.PENDING) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Report is not in PENDING status");
                }

                // Nếu có ruleId, lưu point rule vào report
                if (ruleId != null) {
                        PointRule rule = pointRuleRepository.findById(ruleId)
                                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                        "Point rule not found"));

                        // Kiểm tra rule thuộc về enterprise này
                        if (!rule.getEnterprise().getId().equals(enterprise.getId())) {
                                throw new ApiException(HttpStatus.FORBIDDEN,
                                                "Point rule does not belong to your enterprise");
                        }

                        report.setPointRule(rule);
                }

                report.setEnterprise(enterprise);
                report.setStatus(ReportStatus.ACCEPTED);

                WasteReport updated = wasteReportRepository.save(report);
                return mapReportToResponse(updated);
        }

        // Từ chối yêu cầu thu gom (reset về PENDING và xóa enterprise)
        public void rejectReport(String email, Long reportId) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

                RecyclingEnterprise enterprise = enterpriseRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Enterprise not found"));

                WasteReport report = wasteReportRepository.findById(reportId)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Report not found"));

                // Kiểm tra quyền sở hữu
                if (report.getEnterprise() == null || !report.getEnterprise().getId().equals(enterprise.getId())) {
                        throw new ApiException(HttpStatus.FORBIDDEN, "This report does not belong to your enterprise");
                }

                report.setEnterprise(null);
                report.setStatus(ReportStatus.PENDING);
                wasteReportRepository.save(report);
        }

        // Xem các yêu cầu đã tiếp nhận (ACCEPTED hoặc ASSIGNED)
        public List<WasteReportResponse> getAcceptedReports(String email) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

                RecyclingEnterprise enterprise = enterpriseRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Enterprise not found"));

                List<WasteReport> reports = wasteReportRepository.findByEnterpriseId(enterprise.getId());
                return reports.stream()
                                .map(this::mapReportToResponse)
                                .collect(Collectors.toList());
        }

        // Gán collector cho yêu cầu (ACCEPTED -> ASSIGNED)
        public WasteReportResponse assignCollector(String email, AssignCollectorRequest request) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

                RecyclingEnterprise enterprise = enterpriseRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Enterprise not found"));

                WasteReport report = wasteReportRepository.findById(request.getReportId())
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Report not found"));

                // Kiểm tra quyền sở hữu
                if (report.getEnterprise() == null || !report.getEnterprise().getId().equals(enterprise.getId())) {
                        throw new ApiException(HttpStatus.FORBIDDEN, "This report does not belong to your enterprise");
                }

                if (report.getStatus() != ReportStatus.ACCEPTED) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Report must be in ACCEPTED status");
                }

                // Kiểm tra collector có thuộc enterprise này không
                Collector collector = collectorRepository.findById(request.getCollectorId())
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Collector not found"));

                if (!collector.getEnterprise().getId().equals(enterprise.getId())) {
                        throw new ApiException(HttpStatus.FORBIDDEN, "Collector does not belong to your enterprise");
                }

                report.setCollector(collector);
                report.setStatus(ReportStatus.ASSIGNED);

                // Cập nhật status của collector
                collector.setCurrentStatus(Collector.CollectorStatus.BUSY);
                collectorRepository.save(collector);

                WasteReport updated = wasteReportRepository.save(report);
                return mapReportToResponse(updated);
        }

        // QUẢN LÝ COLLECTOR

        // Tạo collector mới (Enterprise tạo collector trong hệ thống)
        public CollectorResponse createCollector(String enterpriseEmail, CreateCollectorRequest request) {
                // Tìm enterprise
                User enterpriseUser = userRepository.findByEmail(enterpriseEmail)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Enterprise user not found"));

                RecyclingEnterprise enterprise = enterpriseRepository.findByUserId(enterpriseUser.getId())
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Enterprise not found"));

                User collectorUser;

                // Kiểm tra xem có tạo user mới hay sử dụng user cũ
                if (request.getUserId() != null) {
                        // Sử dụng user có sẵn
                        collectorUser = userRepository.findById(request.getUserId())
                                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
                } else if (request.getEmail() != null && request.getFullName() != null
                                && request.getPassword() != null) {
                        // Tạo user mới
                        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                                throw new ApiException(HttpStatus.BAD_REQUEST, "Email already exists");
                        }

                        Role collectorRole = roleRepository.findByName("COLLECTOR")
                                        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                        "COLLECTOR role not found"));

                        collectorUser = new User();
                        collectorUser.setEmail(request.getEmail());
                        collectorUser.setFullName(request.getFullName());
                        collectorUser.setPassword(passwordEncoder.encode(request.getPassword()));
                        collectorUser.setRole(collectorRole);
                        collectorUser = userRepository.save(collectorUser);
                } else {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Must provide either userId OR (email, fullName, password)");
                }

                // Kiểm tra user có role COLLECTOR không
                Role collectorRole = roleRepository.findByName("COLLECTOR")
                                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                "COLLECTOR role not found"));

                if (collectorUser.getRole() == null || !collectorUser.getRole().getId().equals(collectorRole.getId())) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "User does not have COLLECTOR role");
                }

                // Kiểm tra user đã là collector chưa
                if (collectorRepository.findByUserId(collectorUser.getId()).isPresent()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "User is already a collector");
                }

                // Tạo collector
                Collector collector = new Collector();
                collector.setUser(collectorUser);
                collector.setEnterprise(enterprise);
                collector.setCurrentStatus(Collector.CollectorStatus.AVAILABLE);

                Collector saved = collectorRepository.save(collector);
                return mapCollectorToResponse(saved);
        }

        // Lấy danh sách collectors của enterprise
        public List<CollectorResponse> getMyCollectors(String enterpriseEmail) {
                User user = userRepository.findByEmail(enterpriseEmail)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

                RecyclingEnterprise enterprise = enterpriseRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Enterprise not found"));

                return collectorRepository.findByEnterpriseId(enterprise.getId())
                                .stream()
                                .map(this::mapCollectorToResponse)
                                .collect(Collectors.toList());
        }

        // Xóa collector
        public void deleteCollector(String enterpriseEmail, Long collectorId) {
                User user = userRepository.findByEmail(enterpriseEmail)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

                RecyclingEnterprise enterprise = enterpriseRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Enterprise not found"));

                Collector collector = collectorRepository.findById(collectorId)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Collector not found"));

                if (!collector.getEnterprise().getId().equals(enterprise.getId())) {
                        throw new ApiException(HttpStatus.FORBIDDEN, "Collector does not belong to this enterprise");
                }

                collectorRepository.delete(collector);
        }

        private EnterpriseResponse mapToResponse(RecyclingEnterprise enterprise) {
                return new EnterpriseResponse(
                                enterprise.getId(),
                                enterprise.getUser().getId(),
                                enterprise.getCompanyName(),
                                enterprise.getAcceptedWasteTypes(),
                                enterprise.getCapacity(),
                                enterprise.getServiceArea(),
                                enterprise.getRating());
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

        private CollectorResponse mapCollectorToResponse(Collector collector) {
                return new CollectorResponse(
                                collector.getId(),
                                collector.getUser().getId(),
                                collector.getUser().getFullName(),
                                collector.getUser().getEmail(),
                                collector.getEnterprise() != null ? collector.getEnterprise().getId() : null,
                                collector.getEnterprise() != null ? collector.getEnterprise().getCompanyName() : null,
                                collector.getCurrentStatus().name());
        }

        public List<WasteStatisticsResponse> getStatistics(
                        String email,
                        Long categoryId,
                        String provinceCode,
                        String wardCode,
                        LocalDateTime startDateTime,
                        LocalDateTime endDateTime) {

                // Get enterprise by authenticated user
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

                RecyclingEnterprise enterprise = enterpriseRepository.findByUser(user)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Enterprise not found"));

                // Query statistics from repository
                List<Object[]> results = wasteReportRepository.findStatisticsByEnterprise(
                                enterprise.getId(),
                                categoryId,
                                provinceCode,
                                wardCode,
                                startDateTime,
                                endDateTime);

                // Map Object[] to WasteStatisticsResponse DTOs
                return results.stream()
                                .map(row -> {
                                        WasteStatisticsResponse response = new WasteStatisticsResponse();
                                        response.setCategoryName((String) row[0]);
                                        response.setProvinceCode((String) row[1]);
                                        response.setWardCode((String) row[2]);
                                        response.setTotalReports((Long) row[3]);
                                        response.setTotalWeight((Double) row[4]);
                                        response.setCorrectlyClassifiedCount((Long) row[5]);
                                        return response;
                                })
                                .collect(Collectors.toList());
        }
}
