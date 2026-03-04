package com.example.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.entity.Collector;
import com.example.backend.entity.Collector.CollectorStatus;

@Repository
public interface CollectorRepository extends JpaRepository<Collector, Long> {

    Optional<Collector> findByUserId(Long userId);

    List<Collector> findByEnterpriseId(Long enterpriseId);

    List<Collector> findByEnterpriseIdAndCurrentStatus(Long enterpriseId, CollectorStatus status);

    boolean existsByUserId(Long userId);

    List<Collector> findByEnterpriseIsNull();
}
