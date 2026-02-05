package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller.enterprise;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CreateCollectorRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ApiResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CreateCollectorResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Collector;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectorStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Role;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.User;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.enterprise.EnterpriseRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.authentication.RoleRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.authentication.UserRepository;
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
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/enterprise/collectors")
@RequiredArgsConstructor
public class EnterpriseCollectorController {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final CollectorRepository collectorRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping
    @PreAuthorize("hasAnyRole('ENTERPRISE', 'ENTERPRISE_ADMIN')")
    @Transactional
    public ApiResponse<CreateCollectorResponse> createCollector(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CreateCollectorRequest request) {
        Integer enterpriseId = extractEnterpriseId(jwt);

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
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã tồn tại trong hệ hệ thống");
        }

        Role collectorRole = roleRepository.findByRoleCodeIgnoreCase("COLLECTOR")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quyền (Role) không tồn tại"));

        var enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enterprise không tồn tại"));

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
        collector.setVehicleType(request.getVehicleType());
        collector.setVehiclePlate(request.getVehiclePlate());
        collector.setStatus(CollectorStatus.AVAILABLE);
        collector.setCreatedAt(LocalDateTime.now());
        Collector savedCollector = collectorRepository.save(collector);

        return ApiResponse.<CreateCollectorResponse>builder()
                .result(CreateCollectorResponse.builder()
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
                        .build())
                .build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ENTERPRISE', 'ENTERPRISE_ADMIN')")
    @Transactional(readOnly = true)
    public ApiResponse<List<CreateCollectorResponse>> getCollectors(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "status", required = false) String status) {
        Integer enterpriseId = extractEnterpriseId(jwt);

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

        List<CreateCollectorResponse> result = new ArrayList<>();
        for (Collector c : collectors) {
            Integer userId = c.getUser() != null ? c.getUser().getId() : null;
            String phone = c.getUser() != null ? c.getUser().getPhone() : null;

            result.add(CreateCollectorResponse.builder()
                    .userId(userId)
                    .collectorId(c.getId())
                    .enterpriseId(enterpriseId)
                    .email(c.getEmail())
                    .fullName(c.getFullName())
                    .phone(phone)
                    .employeeCode(c.getEmployeeCode())
                    .status(c.getStatus() != null ? c.getStatus().name().toLowerCase() : null)
                    .vehicleType(c.getVehicleType())
                    .vehiclePlate(c.getVehiclePlate())
                    .build());
        }

        return ApiResponse.<List<CreateCollectorResponse>>builder()
                .result(result)
                .build();
    }

    private Integer extractEnterpriseId(Jwt jwt) {
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Thiếu token");
        }
        Object value = jwt.getClaims().get("enterpriseId");
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ ENTERPRISE mới được tạo collector");
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "enterpriseId không hợp lệ");
    }
}
