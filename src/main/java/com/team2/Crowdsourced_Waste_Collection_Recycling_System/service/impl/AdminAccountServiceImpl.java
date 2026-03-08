package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AdminUserResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.User;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.exception.AppException;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.exception.ErrorCode;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.authentication.UserRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.AdminAccountService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAccountServiceImpl implements AdminAccountService {

    private final UserRepository userRepository;

    // ─────────────────────────────────────────────
    // Xem danh sách tài khoản
    // ─────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserResponse> getAllUsers(String status, String roleCode, String adminEmail) {
        List<User> users;

        boolean hasStatus = status != null && !status.isBlank();
        boolean hasRole = roleCode != null && !roleCode.isBlank();

        if (hasStatus && hasRole) {
            users = userRepository.findAllByStatusAndRole_RoleCodeOrderByCreatedAtDesc(status, roleCode);
        } else if (hasStatus) {
            users = userRepository.findAllByStatusOrderByCreatedAtDesc(status);
        } else if (hasRole) {
            users = userRepository.findAllByRole_RoleCodeOrderByCreatedAtDesc(roleCode);
        } else {
            users = userRepository.findAll();
        }

        return users.stream()
                .filter(user -> adminEmail == null || user.getEmail() == null || !user.getEmail().equalsIgnoreCase(adminEmail))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────
    // Xem chi tiết 1 tài khoản
    // ─────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AdminUserResponse getUserDetail(Integer userId) {
        User user = findUserById(userId);
        return toResponse(user);
    }

    // ─────────────────────────────────────────────
    // Khóa tài khoản
    // ─────────────────────────────────────────────

    @Override
    @Transactional
    public AdminUserResponse suspendUser(Integer userId, String adminEmail) {
        // Guard: Admin không thể tự khóa chính mình
        User adminUser = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (userId.equals(adminUser.getId())) {
            throw new AppException(ErrorCode.CANNOT_SUSPEND_SELF);
        }

        User user = findUserById(userId);

        if ("suspended".equalsIgnoreCase(user.getStatus())) {
            throw new AppException(ErrorCode.USER_ALREADY_SUSPENDED);
        }

        user.setStatus("suspended");
        User saved = userRepository.save(user);

        log.info("Admin {} đã khóa tài khoản user id={}", adminEmail, userId);
        return toResponse(saved);
    }

    // ─────────────────────────────────────────────
    // Mở tài khoản
    // ─────────────────────────────────────────────

    @Override
    @Transactional
    public AdminUserResponse activateUser(Integer userId, String adminEmail) {
        User user = findUserById(userId);

        if ("active".equalsIgnoreCase(user.getStatus())) {
            throw new AppException(ErrorCode.USER_ALREADY_ACTIVE);
        }

        user.setStatus("active");
        User saved = userRepository.save(user);

        log.info("Admin {} đã mở khóa tài khoản user id={}", adminEmail, userId);
        return toResponse(saved);
    }

    // ─────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────

    private User findUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    /**
     * Chuyển đổi User entity → AdminUserResponse DTO.
     */
    private AdminUserResponse toResponse(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .roleCode(user.getRole() != null ? user.getRole().getRoleCode() : null)
                .roleName(user.getRole() != null ? user.getRole().getRoleName() : null)
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
