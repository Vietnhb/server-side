package com.example.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.entity.RecyclingEnterprise;
import com.example.backend.entity.User;

@Repository
public interface RecyclingEnterpriseRepository extends JpaRepository<RecyclingEnterprise, Long> {

    Optional<RecyclingEnterprise> findByUserId(Long userId);

    Optional<RecyclingEnterprise> findByUser(User user);

    boolean existsByUserId(Long userId);
}
