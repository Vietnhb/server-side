package com.example.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.AddUserAddressRequest;
import com.example.backend.dto.UpdateUserRequest;
import com.example.backend.dto.UserAddressResponse;
import com.example.backend.dto.UserResponse;
import com.example.backend.dto.PointHistoryResponse;
import com.example.backend.dto.RankingUserResponse;
import com.example.backend.service.UserService;

import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/user/")
@AllArgsConstructor
public class UserController {
    UserService userService;

    @GetMapping("me")
    public UserResponse getMe(Authentication authentication) {
        String email = authentication.getName();
        return userService.getCurrentUser(email);
    }

    @PutMapping("me")
    public ResponseEntity<UserResponse> updateMe(Authentication authentication, @RequestBody UpdateUserRequest rq) {
        String email = authentication.getName();
        return ResponseEntity.ok(userService.updateCurrentUser(email, rq));
    }

    // Address Management Endpoints
    @PostMapping("address")
    public ResponseEntity<UserAddressResponse> addAddress(
            Authentication authentication,
            @RequestBody AddUserAddressRequest request) {
        String email = authentication.getName();
        UserAddressResponse response = userService.addAddress(email, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("addresses")
    public ResponseEntity<List<UserAddressResponse>> getUserAddresses(Authentication authentication) {
        String email = authentication.getName();
        List<UserAddressResponse> addresses = userService.getUserAddresses(email);
        return ResponseEntity.ok(addresses);
    }

    @PutMapping("address/{id}")
    public ResponseEntity<UserAddressResponse> updateAddress(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody AddUserAddressRequest request) {
        String email = authentication.getName();
        UserAddressResponse response = userService.updateAddress(email, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("address/{id}")
    public ResponseEntity<Void> deleteAddress(Authentication authentication, @PathVariable Long id) {
        String email = authentication.getName();
        userService.deleteAddress(email, id);
        return ResponseEntity.noContent().build();
    }

    // Ranking & Point History Endpoints
    @GetMapping("point-history")
    public ResponseEntity<List<PointHistoryResponse>> getMyPointHistory(Authentication authentication) {
        String email = authentication.getName();
        List<PointHistoryResponse> history = userService.getPointHistory(email);
        return ResponseEntity.ok(history);
    }

    @GetMapping("ranking")
    public ResponseEntity<List<RankingUserResponse>> getRankingByArea(
            @RequestParam String areaType,
            @RequestParam(required = false) String areaCode) {
        List<RankingUserResponse> ranking = userService.getRankingByArea(areaType, areaCode);
        return ResponseEntity.ok(ranking);
    }

}
