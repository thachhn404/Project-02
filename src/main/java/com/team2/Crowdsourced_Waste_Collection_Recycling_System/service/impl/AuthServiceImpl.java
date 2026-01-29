package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.*;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AuthenticationResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.IntrospectResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.InvalidatedToken;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Role;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.User;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.exception.AppException;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.exception.ErrorCode;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.InvalidatedTokenRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.RoleRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.UserRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.AuthService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ResponseStatusException;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

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
    InvalidatedTokenRepository invalidatedTokenRepository;
    PasswordEncoder passwordEncoder;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

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

        // Mặc định gán role CITIZEN nếu không chỉ định
        String roleCode = (request.getRoleCode() == null || request.getRoleCode().isBlank()) ? "CITIZEN" : request.getRoleCode();
        Role role = roleRepository.findByRoleCodeIgnoreCase(roleCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quyền (Role) không tồn tại"));

        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        u.setFullName(request.getFullName());
        u.setPhone(request.getPhone());
        u.setRole(role);
        u.setStatus("active");
        userRepository.save(u);

        return login(AuthenticationRequest.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public AuthenticationResponse login(AuthenticationRequest request) {
        // Xác thực bằng email/password, nếu đúng thì phát hành JWT.
        var user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());

        if (!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);

        var token = generateToken(user);

        return AuthenticationResponse.builder().token(token).authenticated(true).build();
    }

    @Override
    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        // Logout = thu hồi token bằng cách lưu JWT ID (jti) vào invalidated_tokens.
        try {
            var signToken = verifyToken(request.getToken(), true);

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

            invalidatedTokenRepository.save(invalidatedToken);
            SecurityContextHolder.clearContext();
        } catch (AppException exception) {
            log.info("Token already expired");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        // Refresh: kiểm tra token (theo cửa sổ refreshable), thu hồi token cũ, rồi cấp token mới.
        var signedJWT = verifyToken(request.getToken(), true);

        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

        invalidatedTokenRepository.save(invalidatedToken);

        var email = signedJWT.getJWTClaimsSet().getSubject();

        var user =
                userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        var token = generateToken(user);

        return AuthenticationResponse.builder().token(token).authenticated(true).build();
    }

    @Override
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        // Introspect chỉ trả true/false để phía Resource Server quyết định có chấp nhận token.
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token, false);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).build();
    }

    private String generateToken(User user) {
        // Tạo JWT ký bằng HS512; claim "scope" chứa role + permissions để map sang authorities.
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("team2.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        // Verify chữ ký + kiểm tra hạn token (exp hoặc refreshable window) + kiểm tra token đã bị thu hồi (jti).
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = (isRefresh)
                ? new Date(signedJWT
                        .getJWTClaimsSet()
                        .getIssueTime()
                        .toInstant()
                        .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                        .toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    private String buildScope(User user) {
        // Ghép scope theo định dạng "ROLE_X PERMISSION_Y ..." (phân tách bằng khoảng trắng).
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (user.getRole() != null) {
            stringJoiner.add("ROLE_" + user.getRole().getRoleCode().toUpperCase());
            if (!CollectionUtils.isEmpty(user.getRole().getRolePermissions())) {
                user.getRole().getRolePermissions().forEach(rolePermission -> {
                    stringJoiner.add(rolePermission.getPermission().getPermissionCode());
                });
            }
        }

        return stringJoiner.toString();
    }
}
