package com.team2.Crowdsourced_Waste_Collection_Recycling_System.config;

import java.text.ParseException;
import java.util.Objects;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.IntrospectRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.AuthService;
import com.nimbusds.jose.JOSEException;
import org.springframework.context.annotation.Lazy;

@Component
/**
 * JwtDecoder tùy biến dùng cho OAuth2 Resource Server.
 *
 * Luồng xử lý:
 * 1) Gọi AuthService.introspect(token) để kiểm tra token còn hợp lệ và chưa bị thu hồi (logout/refresh).
 * 2) Nếu hợp lệ, dùng NimbusJwtDecoder để decode và trả về Jwt cho Spring Security dựng Authentication.
 *
 * Lưu ý: decoder này chỉ phục vụ xác thực request (server nhận token), không phải để phát hành token.
 */
public class CustomJwtDecoder implements JwtDecoder {
    @Value("${jwt.signerKey}")
    private String signerKey;

    @Autowired
    @Lazy
    private AuthService authService;

    private NimbusJwtDecoder nimbusJwtDecoder = null;

    @Override
    public Jwt decode(String token) throws JwtException {

        try {
            // Introspect để quyết định token có bị thu hồi/không hợp lệ trước khi decode.
            var response = authService.introspect(
                    IntrospectRequest.builder().token(token).build());

            if (!response.isValid()) throw new JwtException("Token invalid");
        } catch (JOSEException | ParseException e) {
            throw new JwtException(e.getMessage());
        }

        if (Objects.isNull(nimbusJwtDecoder)) {
            // Khởi tạo lazily để tránh tạo decoder nhiều lần
            SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS512");
            nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();
        }

        return nimbusJwtDecoder.decode(token);
    }
}
