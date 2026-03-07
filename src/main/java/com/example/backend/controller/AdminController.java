package com.example.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.request.SignupRequest;
import com.example.backend.dto.request.UpdateUserRequest;
import com.example.backend.dto.respone.UserResponse;
import com.example.backend.service.AdminService;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/admin/")
@AllArgsConstructor
public class AdminController {
    AdminService adminService;

    @PostMapping("account")
    public ResponseEntity<?> postMethodName(@RequestBody SignupRequest rq) {
        adminService.signup(rq);
        return ResponseEntity.ok("Success");
    }

    @GetMapping("all")
    public ResponseEntity<List<UserResponse>> getUsers() {
        List<UserResponse> users = adminService.getAllUser();
        return ResponseEntity.ok(users);
    }

    @PutMapping("user/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest rq) {
        return ResponseEntity.ok(adminService.updateUser(id, rq));
    }

    @DeleteMapping("user/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
