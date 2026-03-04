package com.example.backend.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.backend.dto.SignupRequest;
import com.example.backend.dto.UpdateUserRequest;
import com.example.backend.dto.UserResponse;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.exception.ApiException;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.RoleRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AdminService {
    UserRepository userRepository;
    RoleRepository roleRepository;

    public List<UserResponse> getAllUser() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getRole().getName()))
                .toList();
    }

    public UserResponse updateUser(Long id, UpdateUserRequest rq) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        if (rq.getFullName() != null)
            user.setFullName(rq.getFullName());
        if (rq.getEmail() != null)
            user.setEmail(rq.getEmail());
        if (rq.getRole() != null) {
            Role role = roleRepository.findByName(rq.getRole())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Role not found"));
            user.setRole(role);
        }
        userRepository.save(user);
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().getName());
    }

    public void deleteUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "User not found"));

        userRepository.delete(user);
    }

    public void signup(SignupRequest request) {
        Role role;
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Email đã tồn tại");
        }
        if (request.getRole() != null) {
            role = roleRepository.findByName(request.getRole())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Role not found"));
        } else {
            role = roleRepository.findByName("CITIZEN")
                    .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi server"));
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPassword(request.getPassword());
        user.setRole(role);
        userRepository.save(user);
    }
}
