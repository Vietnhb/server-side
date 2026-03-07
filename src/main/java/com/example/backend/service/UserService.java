package com.example.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.backend.dto.request.AddUserAddressRequest;
import com.example.backend.dto.request.UpdateUserRequest;
import com.example.backend.dto.respone.PointHistoryResponse;
import com.example.backend.dto.respone.RankingUserResponse;
import com.example.backend.dto.respone.UserAddressResponse;
import com.example.backend.dto.respone.UserResponse;
import com.example.backend.entity.User;
import com.example.backend.entity.UserAddress;
import com.example.backend.entity.PointTransaction;
import com.example.backend.exception.ApiException;
import com.example.backend.repository.UserAddressRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.PointTransactionRepository;
import com.example.backend.repository.WasteReportRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {
    UserRepository userRepository;
    UserAddressRepository userAddressRepository;
    PointTransactionRepository pointTransactionRepository;
    WasteReportRepository wasteReportRepository;

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Khong Thay User"));
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().getName(),
                user.getPoints() != null ? user.getPoints() : 0);
    }

    public UserResponse updateCurrentUser(String email, UpdateUserRequest rq) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "User not found"));

        if (rq.getFullName() != null) {
            user.setFullName(rq.getFullName());
        }

        if (rq.getEmail() != null && !rq.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(rq.getEmail()).isPresent()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Email đã tồn tại");
            }
            user.setEmail(rq.getEmail());
        }

        userRepository.save(user);

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().getName(),
                user.getPoints() != null ? user.getPoints() : 0);
    }

    // Address Management Methods
    public UserAddressResponse addAddress(String email, AddUserAddressRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        // Kiểm tra xem user có địa chỉ active nào chưa
        List<UserAddress> existingAddresses = userAddressRepository.findByUserIdAndIsActiveTrue(user.getId());

        // Nếu chưa có địa chỉ nào, tự động đặt làm mặc định
        if (existingAddresses.isEmpty()) {
            request.setIsDefault(true);
        } else {
            // Nếu không chỉ định, mặc định là false
            if (request.getIsDefault() == null) {
                request.setIsDefault(false);
            }
        }

        // Nếu đặt làm mặc định, bỏ mặc định của địa chỉ cũ
        if (request.getIsDefault()) {
            userAddressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                    .ifPresent(address -> {
                        address.setIsDefault(false);
                        userAddressRepository.save(address);
                    });
        }

        UserAddress address = new UserAddress();
        address.setUser(user);
        address.setReceiverName(request.getReceiverName());
        address.setPhoneNumber(request.getPhoneNumber());
        address.setDetailAddress(request.getDetailAddress());
        address.setAddressNumber(request.getAddressNumber());
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());
        address.setProvinceCode(request.getProvinceCode());
        address.setWardCode(request.getWardCode());
        address.setIsDefault(request.getIsDefault());
        address.setIsActive(true); // Mặc định là active

        UserAddress savedAddress = userAddressRepository.save(address);

        return mapToAddressResponse(savedAddress);
    }

    public List<UserAddressResponse> getUserAddresses(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        // Chỉ lấy địa chỉ đang active
        return userAddressRepository.findByUserIdAndIsActiveTrue(user.getId())
                .stream()
                .map(this::mapToAddressResponse)
                .collect(Collectors.toList());
    }

    public UserAddressResponse updateAddress(String email, Long addressId, AddUserAddressRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        UserAddress address = userAddressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Address not found"));

        // Nếu đặt làm mặc định, bỏ mặc định của địa chỉ cũ
        if (request.getIsDefault() != null && request.getIsDefault()) {
            userAddressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                    .ifPresent(addr -> {
                        if (!addr.getId().equals(addressId)) {
                            addr.setIsDefault(false);
                            userAddressRepository.save(addr);
                        }
                    });
        }

        if (request.getReceiverName() != null) {
            address.setReceiverName(request.getReceiverName());
        }
        if (request.getPhoneNumber() != null) {
            address.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getDetailAddress() != null) {
            address.setDetailAddress(request.getDetailAddress());
        }
        if (request.getAddressNumber() != null) {
            address.setAddressNumber(request.getAddressNumber());
        }
        if (request.getLatitude() != null) {
            address.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            address.setLongitude(request.getLongitude());
        }
        if (request.getProvinceCode() != null) {
            address.setProvinceCode(request.getProvinceCode());
        }
        if (request.getWardCode() != null) {
            address.setWardCode(request.getWardCode());
        }
        if (request.getIsDefault() != null) {
            address.setIsDefault(request.getIsDefault());
        }

        UserAddress updatedAddress = userAddressRepository.save(address);

        return mapToAddressResponse(updatedAddress);
    }

    public void deleteAddress(String email, Long addressId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        UserAddress address = userAddressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Address not found"));

        boolean wasDefault = address.getIsDefault();

        // Soft delete: chỉ đánh dấu isActive = false
        address.setIsActive(false);
        address.setIsDefault(false); // Bỏ default trước khi xóa
        userAddressRepository.save(address);

        // Nếu địa chỉ vừa xóa là default, tự động chọn địa chỉ active đầu tiên làm
        // default
        if (wasDefault) {
            List<UserAddress> remainingAddresses = userAddressRepository.findByUserIdAndIsActiveTrue(user.getId());
            if (!remainingAddresses.isEmpty()) {
                UserAddress newDefault = remainingAddresses.get(0);
                newDefault.setIsDefault(true);
                userAddressRepository.save(newDefault);
            }
        }
    }

    private UserAddressResponse mapToAddressResponse(UserAddress address) {
        return new UserAddressResponse(
                address.getId(),
                address.getReceiverName(),
                address.getPhoneNumber(),
                address.getDetailAddress(),
                address.getAddressNumber(),
                address.getLatitude(),
                address.getLongitude(),
                address.getProvinceCode(),
                address.getWardCode(),
                address.getIsDefault());
    }

    // Ranking & Point History Methods
    public List<PointHistoryResponse> getPointHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        System.out.println("🔍 Getting point history for user ID: " + user.getId() + ", email: " + email);

        List<PointTransaction> transactions = pointTransactionRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId());

        System.out.println("📊 Found " + transactions.size() + " transactions");

        return transactions.stream()
                .map(this::mapToPointHistoryResponse)
                .collect(Collectors.toList());
    }

    public List<RankingUserResponse> getRankingByArea(String areaType, String areaCode) {
        List<Object[]> rankingData;

        if ("ward".equalsIgnoreCase(areaType)) {
            if (areaCode == null || areaCode.isEmpty()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Ward code is required for ward ranking");
            }
            rankingData = wasteReportRepository.findRankingByWard(areaCode);
        } else if ("province".equalsIgnoreCase(areaType)) {
            if (areaCode == null || areaCode.isEmpty()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Province code is required for province ranking");
            }
            rankingData = wasteReportRepository.findRankingByProvince(areaCode);
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid areaType. Use 'ward' or 'province'");
        }

        // Convert to DTO with ranking
        List<RankingUserResponse> ranking = new java.util.ArrayList<>();
        int rank = 1;
        for (Object[] row : rankingData) {
            Long userId = ((Number) row[0]).longValue();
            String userName = (String) row[1];
            Integer totalPoints = row[2] != null ? ((Number) row[2]).intValue() : 0;
            Long totalReports = ((Number) row[3]).longValue();
            String provinceCode = (String) row[4];
            String wardCode = (String) row[5];

            ranking.add(new RankingUserResponse(userId, userName, totalPoints, totalReports, rank++, provinceCode,
                    wardCode));
        }

        return ranking;
    }

    private PointHistoryResponse mapToPointHistoryResponse(PointTransaction transaction) {
        return new PointHistoryResponse(
                transaction.getId(),
                transaction.getPoints(),
                transaction.getReport().getId(),
                transaction.getCreatedAt(),
                transaction.getReport().getCategory().getName(),
                transaction.getReport().getWeight(),
                transaction.getReport().getIsCorrectlyClassified());
    }

}
