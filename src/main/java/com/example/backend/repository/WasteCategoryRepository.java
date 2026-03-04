package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.entity.WasteCategory;

@Repository
public interface WasteCategoryRepository extends JpaRepository<WasteCategory, Long> {
    List<WasteCategory> findByIsActiveTrue();
}
