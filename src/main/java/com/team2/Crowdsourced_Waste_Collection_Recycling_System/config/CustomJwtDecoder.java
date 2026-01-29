package com.team2.Crowdsourced_Waste_Collection_Recycling_System.config;

import java.text.ParseException;
import java.util.Objects;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.IntrospectRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.AuthService;
import com.nimbusds.jose.JOSEException;

/**
 * CustomJwtDecoder thực hiện việc giải mã và kiểm tra tính hợp lệ của JWT.
 * Nó sử dụng AuthService để kiểm tra xem token có nằm trong danh sách bị vô hiệu hóa (logout) hay không
 * trước khi thực hiện giải mã bằng NimbusJwtDecoder.
 */
@Component
public class CustomJwtDecoder implements JwtDecoder {
    @Value("${jwt.signerKey}")
    private String signerKey;

    @Autowired
    @Lazy
    private AuthService authService;

    private NimbusJwtDecoder nimbusJwtDecoder = null;

    @Override
    public Jwt decode(String token) throws JwtException {
        // Bước 1: Kiểm tra tính hợp lệ của token (bao gồm cả việc kiểm tra xem đã logout chưa)
        try {
            var response = authService.introspect(
                    IntrospectRequest.builder().token(token).build());

            if (!response.isValid()) {
                throw new JwtException("Token không hợp lệ hoặc đã bị vô hiệu hóa");
            }
        } catch (JOSEException | ParseException e) {
            throw new JwtException("Lỗi khi kiểm tra token: " + e.getMessage());
        }

        // Bước 2: Khởi tạo NimbusJwtDecoder nếu chưa có
        if (Objects.isNull(nimbusJwtDecoder)) {
            SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS512");
            nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();
        }

        // Bước 3: Giải mã token
        try {
            return nimbusJwtDecoder.decode(token);
        } catch (JwtException e) {
            throw new JwtException("Lỗi khi giải mã token: " + e.getMessage());
        }
    }
}
