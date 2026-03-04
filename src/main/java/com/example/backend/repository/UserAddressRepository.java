package com.example.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.entity.UserAddress;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    List<UserAddress> findByUserId(Long userId);

    // Chỉ lấy địa chỉ đang active
    List<UserAddress> findByUserIdAndIsActiveTrue(Long userId);

    Optional<UserAddress> findByIdAndUserId(Long id, Long userId);

    // Tìm address active của user
    Optional<UserAddress> findByIdAndUserIdAndIsActiveTrue(Long id, Long userId);

    Optional<UserAddress> findByUserIdAndIsDefaultTrue(Long userId);
}
