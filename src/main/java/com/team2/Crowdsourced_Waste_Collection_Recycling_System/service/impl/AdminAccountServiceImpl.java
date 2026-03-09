package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.AdminCreateUserRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AdminUserResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Citizen;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Collector;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Enterprise;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Role;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.User;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectorStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.VehicleType;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.exception.AppException;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.exception.ErrorCode;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.authentication.RoleRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.authentication.UserRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.enterprise.EnterpriseRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.profile.CitizenRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.AdminAccountService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAccountServiceImpl implements AdminAccountService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CitizenRepository citizenRepository;
    private final CollectorRepository collectorRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AdminUserResponse createUser(AdminCreateUserRequest request, String adminEmail) {
        validateCreateRequest(request);

        String normalizedRole = request.getRoleCode().trim().toUpperCase(Locale.ROOT);
        if ("ADMIN".equals(normalizedRole) || "ENTERPRISE_ADMIN".equals(normalizedRole)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Admin không được tạo tài khoản role ADMIN hoặc ENTERPRISE_ADMIN");
        }
        if (!"CITIZEN".equals(normalizedRole) && !"COLLECTOR".equals(normalizedRole) && !"ENTERPRISE".equals(normalizedRole)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "roleCode chỉ hỗ trợ CITIZEN, COLLECTOR, ENTERPRISE");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã tồn tại trong hệ hệ thống");
        }

        Role role = roleRepository.findByRoleCodeIgnoreCase(normalizedRole)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quyền (Role) không tồn tại"));

        User user = new User();
        user.setEmail(request.getEmail().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone());
        user.setRole(role);
        user.setStatus("active");
        User savedUser = userRepository.save(user);

        if ("CITIZEN".equals(normalizedRole)) {
            createCitizenProfile(savedUser, request);
        } else if ("COLLECTOR".equals(normalizedRole)) {
            createCollectorProfile(savedUser, request);
        } else {
            createEnterpriseProfile(savedUser, request);
        }

        log.info("Admin {} đã tạo user id={} với role={}", adminEmail, savedUser.getId(), normalizedRole);
        return toResponse(savedUser);
    }

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

    private void validateCreateRequest(AdminCreateUserRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu dữ liệu");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email không được để trống");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu không được để trống");
        }
        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Họ tên không được để trống");
        }
        if (request.getRoleCode() == null || request.getRoleCode().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "roleCode không được để trống");
        }
    }

    private void createCitizenProfile(User user, AdminCreateUserRequest request) {
        Citizen citizen = new Citizen();
        citizen.setUser(user);
        citizen.setEmail(user.getEmail());
        citizen.setFullName(user.getFullName());
        citizen.setPasswordHash(user.getPasswordHash());
        citizen.setPhone(user.getPhone());
        citizen.setAddress(request.getCitizenAddress());
        citizen.setWard(request.getCitizenWard());
        citizen.setCity(request.getCitizenCity());
        citizen.setTotalPoints(0);
        citizen.setTotalReports(0);
        citizen.setValidReports(0);
        citizenRepository.save(citizen);
    }

    private void createCollectorProfile(User user, AdminCreateUserRequest request) {
        if (request.getEnterpriseId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "enterpriseId là bắt buộc với role COLLECTOR");
        }

        Enterprise enterprise = enterpriseRepository.findById(request.getEnterpriseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enterprise không tồn tại"));

        Collector collector = new Collector();
        collector.setUser(user);
        collector.setEnterprise(enterprise);
        collector.setEmail(user.getEmail());
        collector.setFullName(user.getFullName());
        collector.setEmployeeCode(request.getEmployeeCode());
        collector.setVehiclePlate(request.getVehiclePlate());
        collector.setStatus(CollectorStatus.OFFLINE);
        collector.setCreatedAt(LocalDateTime.now());

        if (request.getVehicleType() != null && !request.getVehicleType().isBlank()) {
            VehicleType vehicleType = VehicleType.fromString(request.getVehicleType());
            if (vehicleType == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "vehicleType không hợp lệ (CAR|TRUCK|MOTORBIKE)");
            }
            collector.setVehicleType(vehicleType.name());
        }

        Collector savedCollector = collectorRepository.save(collector);
        if (savedCollector.getEmployeeCode() == null || savedCollector.getEmployeeCode().isBlank()) {
            savedCollector.setEmployeeCode(String.format("C%03d", savedCollector.getId()));
            collectorRepository.save(savedCollector);
        }
    }

    private void createEnterpriseProfile(User user, AdminCreateUserRequest request) {
        if (request.getEnterpriseName() == null || request.getEnterpriseName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "enterpriseName là bắt buộc với role ENTERPRISE");
        }

        Enterprise enterprise = new Enterprise();
        enterprise.setName(request.getEnterpriseName().trim());
        enterprise.setAddress(request.getEnterpriseAddress());
        enterprise.setPhone(request.getEnterprisePhone() != null ? request.getEnterprisePhone() : user.getPhone());
        enterprise.setEmail(request.getEnterpriseEmail() != null ? request.getEnterpriseEmail() : user.getEmail());
        enterprise.setStatus("active");
        enterprise.setCreatedAt(LocalDateTime.now());
        enterprise.setUpdatedAt(LocalDateTime.now());

        Enterprise savedEnterprise = enterpriseRepository.save(enterprise);
        user.setEnterprise(savedEnterprise);
        userRepository.save(user);
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
