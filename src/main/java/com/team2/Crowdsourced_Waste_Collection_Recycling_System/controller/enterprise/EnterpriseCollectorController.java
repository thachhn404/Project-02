package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller.enterprise;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CreateCollectorRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ApiResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CreateCollectorResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Collector;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Enterprise;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectorStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.VehicleType;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Role;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.User;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.enterprise.EnterpriseRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.authentication.RoleRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.authentication.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/enterprise/collectors")
@RequiredArgsConstructor
@Tag(name = "Enterprise Collectors", description = "Quản lý nhân viên thu gom của doanh nghiệp")
public class EnterpriseCollectorController extends EnterpriseControllerSupport {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final CollectorRepository collectorRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping
    @PreAuthorize("hasAnyRole('ENTERPRISE', 'ENTERPRISE_ADMIN')")
    @Transactional
    @Operation(summary = "Tạo Collector", description = "Tạo mới nhân viên thu gom thuộc doanh nghiệp hiện tại")
    public ApiResponse<CreateCollectorResponse> createCollector(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CreateCollectorRequest request) {
        Integer enterpriseId = extractEnterpriseId(jwt, "Chỉ ENTERPRISE mới được tạo collector");
        validateCreateCollectorRequest(request);
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã tồn tại trong hệ hệ thống");
        }

        Role collectorRole = requireCollectorRole();
        var enterprise = requireEnterprise(enterpriseId);

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(collectorRole);
        user.setStatus("active");
        User savedUser = userRepository.save(user);

        Collector collector = new Collector();
        collector.setUser(savedUser);
        collector.setEnterprise(enterprise);
        collector.setEmail(savedUser.getEmail());
        collector.setFullName(savedUser.getFullName());
        collector.setEmployeeCode(request.getEmployeeCode());
        if (request.getVehicleType() != null && !request.getVehicleType().isBlank()) {
            VehicleType vt = VehicleType.fromString(request.getVehicleType());
            if (vt == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "vehicleType không hợp lệ (CAR|TRUCK|MOTORBIKE)");
            }
            collector.setVehicleType(vt.name());
        }
        collector.setVehiclePlate(request.getVehiclePlate());
        collector.setStatus(CollectorStatus.AVAILABLE);
        collector.setCreatedAt(LocalDateTime.now());
        Collector savedCollector = collectorRepository.save(collector);
        savedCollector.setEmployeeCode(String.format("C%03d", savedCollector.getId()));
        savedCollector = collectorRepository.save(savedCollector);

        return ok(CreateCollectorResponse.builder()
                .userId(savedUser.getId())
                .collectorId(savedCollector.getId())
                .enterpriseId(enterpriseId)
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .phone(savedUser.getPhone())
                .employeeCode(savedCollector.getEmployeeCode())
                .status(savedCollector.getStatus() != null ? savedCollector.getStatus().name().toLowerCase() : null)
                .vehicleType(savedCollector.getVehicleType())
                .vehiclePlate(savedCollector.getVehiclePlate())
                .build());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ENTERPRISE', 'ENTERPRISE_ADMIN')")
    @Transactional(readOnly = true)
    @Operation(summary = "Danh sách Collector", description = "Liệt kê nhân viên thu gom, hỗ trợ lọc theo status")
    public ApiResponse<List<CreateCollectorResponse>> getCollectors(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "status", required = false) String status) {
        Integer enterpriseId = extractEnterpriseId(jwt, "Chỉ ENTERPRISE mới được tạo collector");

        List<Collector> collectors;
        if (status == null || status.isBlank()) {
            collectors = collectorRepository.findByEnterprise_IdOrderByCreatedAtDesc(enterpriseId);
        } else {
            CollectorStatus collectorStatus = CollectorStatus.fromString(status);
            if (collectorStatus == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status không hợp lệ");
            }
            collectors = collectorRepository.findByEnterprise_IdAndStatusOrderByCreatedAtDesc(enterpriseId, collectorStatus);
        }

        List<CreateCollectorResponse> result = collectors.stream()
                .map(c -> toResponse(c, enterpriseId))
                .toList();

        return ok(result);
    }

    private static void validateCreateCollectorRequest(CreateCollectorRequest request) {
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
    }

    private Role requireCollectorRole() {
        return roleRepository.findByRoleCodeIgnoreCase("COLLECTOR")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quyền (Role) không tồn tại"));
    }

    private Enterprise requireEnterprise(Integer enterpriseId) {
        return enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enterprise không tồn tại"));
    }

    private static CreateCollectorResponse toResponse(Collector collector, Integer enterpriseId) {
        Integer userId = collector.getUser() != null ? collector.getUser().getId() : null;
        String phone = collector.getUser() != null ? collector.getUser().getPhone() : null;
        return CreateCollectorResponse.builder()
                .userId(userId)
                .collectorId(collector.getId())
                .enterpriseId(enterpriseId)
                .email(collector.getEmail())
                .fullName(collector.getFullName())
                .phone(phone)
                .employeeCode(collector.getEmployeeCode())
                .status(collector.getStatus() != null ? collector.getStatus().name().toLowerCase() : null)
                .vehicleType(collector.getVehicleType())
                .vehiclePlate(collector.getVehiclePlate())
                .violationCount(collector.getViolationCount())
                .build();
    }
}
