package com.example.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.entity.WasteCategory;
import com.example.backend.repository.WasteCategoryRepository;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/category/")
@AllArgsConstructor
public class WasteCategoryController {

    private final WasteCategoryRepository wasteCategoryRepository;

    @GetMapping("list")
    public ResponseEntity<List<WasteCategory>> getActiveCategories() {
        List<WasteCategory> categories = wasteCategoryRepository.findByIsActiveTrue();
        return ResponseEntity.ok(categories);
    }
}
