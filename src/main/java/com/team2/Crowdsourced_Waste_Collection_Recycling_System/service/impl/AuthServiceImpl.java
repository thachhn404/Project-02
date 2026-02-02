package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.*;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AuthenticationResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.IntrospectResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Citizen;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Collector;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.InvalidatedToken;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Role;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.User;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.exception.AppException;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.exception.ErrorCode;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.CitizenRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.CollectorRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.InvalidatedTokenRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.RoleRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.UserRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.AuthService;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.util.JWTHelper;
import com.nimbusds.jose.JOSEException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.ParseException;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
/**
 * Hiện thực các nghiệp vụ xác thực/ủy quyền dựa trên JWT (Nimbus JOSE + JWT).
 *
 * Cách hoạt động tổng quát:
 * - Đăng nhập: kiểm tra email/password, sau đó phát hành JWT (HS512) chứa claim "scope".
 * - Logout/Refresh: thu hồi token cũ bằng cách lưu jti vào bảng invalidated_tokens.
 * - Introspect: kiểm tra token hợp lệ + chưa bị thu hồi (CustomJwtDecoder gọi để quyết định cho phép truy cập).
 */
public class AuthServiceImpl implements AuthService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    CitizenRepository citizenRepository;
    CollectorRepository collectorRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    PasswordEncoder passwordEncoder;
    JWTHelper jwtHelper;

    @Override
    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        // Validate dữ liệu đầu vào và tạo user mới, sau đó đăng nhập để trả token.
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email không được để trống");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu không được để trống");
        }
        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Họ tên không được để trống");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã tồn tại trong hệ hệ thống");
        }

        String roleCode = (request.getRoleCode() == null || request.getRoleCode().isBlank()) ? "CITIZEN" : request.getRoleCode();
        if (!"CITIZEN".equalsIgnoreCase(roleCode)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ CITIZEN được tự đăng ký");
        }

        Role role = roleRepository.findByRoleCodeIgnoreCase(roleCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quyền (Role) không tồn tại"));

        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        u.setFullName(request.getFullName());
        u.setPhone(request.getPhone());
        u.setRole(role);
        u.setStatus("active");
        User savedUser = userRepository.save(u);

        if ("CITIZEN".equalsIgnoreCase(role.getRoleCode())) {
            Citizen citizen = new Citizen();
            citizen.setUser(savedUser);
            citizen.setEmail(savedUser.getEmail());
            citizen.setFullName(savedUser.getFullName());
            citizen.setPasswordHash(savedUser.getPasswordHash());
            citizen.setPhone(savedUser.getPhone());
            citizen.setTotalPoints(0);
            citizen.setTotalReports(0);
            citizen.setValidReports(0);
            citizenRepository.save(citizen);
            citizenRepository.flush();
        }

        return login(AuthenticationRequest.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public AuthenticationResponse login(AuthenticationRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu dữ liệu đăng nhập");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email không được để trống");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu không được để trống");
        }

        var user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());

        if (!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);

        Integer citizenId = resolveCitizenId(user);
        Integer collectorId = null;
        Integer enterpriseId = null;

        if (user.getRole() != null && user.getRole().getRoleCode() != null && user.getId() != null) {
            String roleCode = user.getRole().getRoleCode();
            if ("COLLECTOR".equalsIgnoreCase(roleCode)) {
                var collector = collectorRepository.findByUserId(user.getId()).orElse(null);
                if (collector != null) {
                    collectorId = collector.getId();
                    if (collector.getEnterprise() != null) {
                        enterpriseId = collector.getEnterprise().getId();
                    }
                }
            } else if ("ENTERPRISE".equalsIgnoreCase(roleCode) || "ENTERPRISE_ADMIN".equalsIgnoreCase(roleCode)) {
                if (user.getEnterprise() != null) {
                    enterpriseId = user.getEnterprise().getId();
                }
            }
        }

        var token = jwtHelper.issueToken(user, citizenId, collectorId, enterpriseId);
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .citizenId(citizenId)
                .collectorId(collectorId)
                .enterpriseId(enterpriseId)
                .build();
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        // Logout = thu hồi token bằng cách lưu JWT ID (jti) vào invalidated_tokens.
        try {
            if (request == null || request.getToken() == null || request.getToken().isBlank()) {
                SecurityContextHolder.clearContext();
                return;
            }
            var signToken = jwtHelper.verifyToken(request.getToken());

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

            invalidatedTokenRepository.save(invalidatedToken);
        } catch (Exception ignored) {
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Override
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        // Introspect chỉ trả true/false để phía Resource Server quyết định có chấp nhận token.
        var token = request.getToken();
        boolean isValid = true;

        try {
            jwtHelper.verifyToken(token);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).build();
    }

    private Integer resolveCitizenId(User user) {
        if (user.getRole() == null || user.getRole().getRoleCode() == null) {
            return null;
        }
        if (!"CITIZEN".equalsIgnoreCase(user.getRole().getRoleCode())) {
            return null;
        }
        if (user.getId() == null) {
            return null;
        }
        return citizenRepository.findByUserId(user.getId()).map(Citizen::getId).orElse(null);
    }
}
