package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.User;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller dành cho Quản trị viên (Admin).
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;

    /**
     * Lấy danh sách toàn bộ người dùng trong hệ thống.
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    /**
     * Khóa hoặc mở khóa tài khoản người dùng.
     */
    @PatchMapping("/users/{id}/status")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<User> updateUserStatus(@PathVariable Integer id, @RequestParam String status) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setStatus(status);
                    return ResponseEntity.ok(userRepository.save(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
