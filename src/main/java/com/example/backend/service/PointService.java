package com.example.backend.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dto.CreatePointRuleRequest;
import com.example.backend.dto.PointRuleResponse;
import com.example.backend.dto.PointTransactionResponse;
import com.example.backend.entity.PointRule;
import com.example.backend.entity.PointTransaction;
import com.example.backend.entity.RecyclingEnterprise;
import com.example.backend.entity.User;
import com.example.backend.entity.WasteCategory;
import com.example.backend.entity.WasteReport;
import com.example.backend.exception.ApiException;
import com.example.backend.repository.PointRuleRepository;
import com.example.backend.repository.PointTransactionRepository;
import com.example.backend.repository.RecyclingEnterpriseRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.WasteCategoryRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PointService {

    /**
     * HỆ THỐNG ĐIỂM THƯỞNG - VAI TRÒ:
     * 
     * 1. ENTERPRISE (Doanh nghiệp):
     * - Quyết định LUẬT: Tạo và cấu hình quy tắc tính điểm (PointRule)
     * - Định nghĩa: điểm cơ bản, điểm/kg, bonus xử lý nhanh...
     * 
     * 2. COLLECTOR (Người thu gom):
     * - Thực THI: Khi hoàn thành thu gom (COLLECTED), trigger việc tính điểm
     * - Cung cấp dữ liệu: khối lượng rác, thời gian hoàn thành, ảnh xác nhận
     * 
     * 3. CITIZEN (Người dân):
     * - Nhận THƯỞNG: Passive receiver, chỉ xem điểm tích lũy
     * 
     * 4. HỆ THỐNG:
     * - Công cụ hỗ trợ: Tính toán tự động dựa trên rule của Enterprise
     * - KHÔNG tự quyết định, chỉ áp dụng luật đã được Enterprise thiết lập
     */

    private final PointRuleRepository pointRuleRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final RecyclingEnterpriseRepository enterpriseRepository;
    private final WasteCategoryRepository wasteCategoryRepository;
    private final UserRepository userRepository;

    // Enterprise tạo rule mới
    @Transactional
    public PointRuleResponse createRule(String email, CreatePointRuleRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        RecyclingEnterprise enterprise = enterpriseRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Enterprise not found"));

        PointRule rule = new PointRule();
        rule.setEnterprise(enterprise);
        rule.setRuleName(request.getRuleName());
        rule.setDescription(request.getDescription());
        rule.setBasePoints(request.getBasePoints());
        rule.setPointsPerKg(request.getPointsPerKg());
        rule.setCorrectClassificationBonus(request.getCorrectClassificationBonus());

        // Nếu có categoryIds, gán categories
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            for (Long categoryId : request.getCategoryIds()) {
                WasteCategory category = wasteCategoryRepository.findById(categoryId)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category not found: " + categoryId));
                rule.getCategories().add(category);
            }
        }

        PointRule saved = pointRuleRepository.save(rule);
        return mapRuleToResponse(saved);
    }

    // Enterprise cập nhật rule
    @Transactional
    public PointRuleResponse updateRule(String email, Long ruleId, CreatePointRuleRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        RecyclingEnterprise enterprise = enterpriseRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Enterprise not found"));

        PointRule rule = pointRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Rule not found"));

        // Kiểm tra quyền sở hữu
        if (!rule.getEnterprise().getId().equals(enterprise.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not own this rule");
        }

        if (request.getRuleName() != null) {
            rule.setRuleName(request.getRuleName());
        }
        if (request.getDescription() != null) {
            rule.setDescription(request.getDescription());
        }
        if (request.getBasePoints() != null) {
            rule.setBasePoints(request.getBasePoints());
        }
        if (request.getPointsPerKg() != null) {
            rule.setPointsPerKg(request.getPointsPerKg());
        }
        if (request.getCorrectClassificationBonus() != null) {
            rule.setCorrectClassificationBonus(request.getCorrectClassificationBonus());
        }

        // Cập nhật categories
        if (request.getCategoryIds() != null) {
            rule.getCategories().clear();
            if (!request.getCategoryIds().isEmpty()) {
                for (Long categoryId : request.getCategoryIds()) {
                    WasteCategory category = wasteCategoryRepository.findById(categoryId)
                            .orElseThrow(
                                    () -> new ApiException(HttpStatus.NOT_FOUND, "Category not found: " + categoryId));
                    rule.getCategories().add(category);
                }
            }
        }

        PointRule updated = pointRuleRepository.save(rule);
        return mapRuleToResponse(updated);
    }

    // Enterprise xem danh sách rules của mình
    public List<PointRuleResponse> getMyRules(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        RecyclingEnterprise enterprise = enterpriseRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Enterprise not found"));

        return pointRuleRepository.findByEnterpriseId(enterprise.getId())
                .stream()
                .map(this::mapRuleToResponse)
                .collect(Collectors.toList());
    }

    // Toggle active/inactive rule
    @Transactional
    public PointRuleResponse toggleRuleStatus(String email, Long ruleId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        RecyclingEnterprise enterprise = enterpriseRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Enterprise not found"));

        PointRule rule = pointRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Rule not found"));

        if (!rule.getEnterprise().getId().equals(enterprise.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not own this rule");
        }

        rule.setIsActive(!rule.getIsActive());
        PointRule updated = pointRuleRepository.save(rule);
        return mapRuleToResponse(updated);
    }

    // Xóa rule
    @Transactional
    public void deleteRule(String email, Long ruleId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        RecyclingEnterprise enterprise = enterpriseRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Enterprise not found"));

        PointRule rule = pointRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Rule not found"));

        if (!rule.getEnterprise().getId().equals(enterprise.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not own this rule");
        }

        pointRuleRepository.delete(rule);
    }

    /**
     * Tính điểm và trao thưởng cho Citizen
     * 
     * TRIGGER: Được gọi bởi CollectorService khi Collector đánh dấu COLLECTED
     * LOGIC:
     * 1. Tìm LUẬT do Enterprise thiết lập (rule theo loại rác hoặc rule mặc định)
     * 2. Tính điểm dựa trên LUẬT: base + weight + fast processing
     * 3. Cộng điểm cho Citizen
     * 4. Ghi lại lịch sử giao dịch
     * 
     * LƯU Ý: Hệ thống KHÔNG tự quyết định số điểm, mà tuân theo rule của Enterprise
     */
    @Transactional
    public void calculateAndAwardPoints(WasteReport report) {
        if (report.getEnterprise() == null) {
            return; // Không có enterprise, không tính điểm
        }

        User citizen = report.getCitizen();
        RecyclingEnterprise enterprise = report.getEnterprise();

        // Tìm LUẬT do Enterprise thiết lập
        // Tìm rule có chứa category cụ thể hoặc áp dụng cho tất cả categories (empty set)
        PointRule rule = pointRuleRepository
                .findApplicableRule(enterprise.getId(), report.getCategory())
                .orElse(null);

        if (rule == null) {
            return; // Enterprise chưa thiết lập rule, không tính điểm
        }

        int totalPoints = 0;
        StringBuilder description = new StringBuilder();
        description.append("Collected by ").append(report.getCollector().getUser().getFullName())
                .append(" | Rule: ").append(rule.getRuleName()).append(" | ");

        // 1. Điểm cơ bản (do Enterprise quy định)
        totalPoints += rule.getBasePoints();
        description.append("Base: ").append(rule.getBasePoints());

        // 2. Điểm theo khối lượng (Collector cung cấp dữ liệu weight)
        if (rule.getPointsPerKg() != null && report.getWeight() != null && report.getWeight() > 0) {
            int weightPoints = (int) (report.getWeight() * rule.getPointsPerKg());
            totalPoints += weightPoints;
            description.append(" + Weight: ").append(weightPoints)
                    .append(" (").append(report.getWeight()).append("kg × ")
                    .append(rule.getPointsPerKg()).append(")");
        }

        // 3. Điểm thưởng phân loại đúng (nếu category của report nằm trong categories của rule)
        if (rule.getCorrectClassificationBonus() != null && !rule.getCategories().isEmpty()) {
            boolean categoryMatches = rule.getCategories().stream()
                .anyMatch(cat -> cat.getId().equals(report.getCategory().getId()));
            
            if (categoryMatches) {
                totalPoints += rule.getCorrectClassificationBonus();
                description.append(" + Correct Classification: ").append(rule.getCorrectClassificationBonus());
            }
        }

        description.append(" = TOTAL: ").append(totalPoints).append(" points");

        // Cộng điểm cho Citizen (nhận thưởng)
        if (citizen.getPoints() == null) {
            citizen.setPoints(0);
        }
        citizen.setPoints(citizen.getPoints() + totalPoints);
        userRepository.save(citizen);

        // Tạo transaction record
        PointTransaction transaction = new PointTransaction();
        transaction.setUser(citizen);
        transaction.setReport(report);
        transaction.setRule(rule);
        transaction.setType(PointTransaction.TransactionType.EARN);
        transaction.setPoints(totalPoints);
        transaction.setDescription(description.toString());
        pointTransactionRepository.save(transaction);
    }

    // Citizen xem lịch sử giao dịch điểm
    public List<PointTransactionResponse> getMyTransactions(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        return pointTransactionRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapTransactionToResponse)
                .collect(Collectors.toList());
    }

    private PointRuleResponse mapRuleToResponse(PointRule rule) {
        List<Long> categoryIds = rule.getCategories().stream()
                .map(WasteCategory::getId)
                .collect(Collectors.toList());

        String categoryNames = rule.getCategories().isEmpty()
                ? null
                : rule.getCategories().stream()
                        .map(WasteCategory::getName)
                        .collect(Collectors.joining(", "));

        return new PointRuleResponse(
                rule.getId(),
                rule.getEnterprise().getId(),
                rule.getEnterprise().getCompanyName(),
                categoryIds,
                categoryNames,
                rule.getRuleName(),
                rule.getDescription(),
                rule.getBasePoints(),
                rule.getPointsPerKg(),
                rule.getCorrectClassificationBonus(),
                rule.getIsActive(),
                rule.getCreatedAt());
    }

    private PointTransactionResponse mapTransactionToResponse(PointTransaction transaction) {
        return new PointTransactionResponse(
                transaction.getId(),
                transaction.getUser().getId(),
                transaction.getReport() != null ? transaction.getReport().getId() : null,
                transaction.getType().name(),
                transaction.getPoints(),
                transaction.getDescription(),
                transaction.getCreatedAt());
    }
}
