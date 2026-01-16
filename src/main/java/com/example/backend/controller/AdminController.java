package com.example.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.UpdateUserRequest;
import com.example.backend.dto.UserResponse;
import com.example.backend.service.AdminService;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/api/admin/")
@AllArgsConstructor
public class AdminController {
    AdminService adminService;
    @GetMapping("all")
    public ResponseEntity<List<UserResponse>> getUsers() {
        List<UserResponse> users = adminService.getAllUser();
        return ResponseEntity.ok(users);
    }
    @PutMapping("update/user/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest rq) {
        return ResponseEntity.ok(adminService.updateUser(id, rq));
    }
}
