package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.entity.Complaint;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByUserId(Long userId);

    List<Complaint> findByReportId(Long reportId);

    List<Complaint> findByStatus(Complaint.ComplaintStatus status);

    List<Complaint> findAllByOrderByCreatedAtDesc();
}
