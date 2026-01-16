package com.example.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.UserResponse;
import com.example.backend.service.UserService;

import lombok.AllArgsConstructor;


import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;


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

}
