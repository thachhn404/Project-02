package com.team2.Crowdsourced_Waste_Collection_Recycling_System.security.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.security.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Triển khai JwtService sử dụng thư viện JJWT để tạo Token.
 * (Việc xác thực Token hiện tại đã được Spring Security OAuth2 Resource Server đảm nhận).
 */
@Service
public class JwtServiceImpl implements JwtService {
    private final Key key;
    private final long expirationMs;
    private final long refreshExpirationMs;

    public JwtServiceImpl(@Value("${security.jwt.secret}") String secret,
                          @Value("${security.jwt.expiration-ms}") long expirationMs,
                          @Value("${security.jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        byte[] keyBytes = Decoders.BASE64.decode(ensureBase64(secret));
        if (keyBytes.length < 32) {
            throw new IllegalStateException("security.jwt.secret phải dài tối thiểu 32 bytes.");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    /**
     * Tạo Access Token và tự động thêm quyền (authorities) vào claims.
     */
    @Override
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
        claims.put("scope", authorities); 
        
        return generateToken(userDetails, claims);
    }

    @Override
    public String generateToken(UserDetails user, Map<String, Object> claims) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String generateRefreshToken(UserDetails userDetails) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + refreshExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Các phương thức extract và validate thủ công không còn cần thiết cho Filter,
     * nhưng vẫn có thể giữ lại để sử dụng ở các nơi khác nếu cần.
     */
    @Override
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    @Override
    public boolean isTokenValid(String token, UserDetails user) {
        try {
            var claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return user.getUsername().equals(claims.getSubject())
                    && claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public long getExpirationMs() {
        return expirationMs;
    }

    private String ensureBase64(String val) {
        try {
            Decoders.BASE64.decode(val);
            return val;
        } catch (Exception e) {
            return io.jsonwebtoken.io.Encoders.BASE64.encode(val.getBytes(StandardCharsets.UTF_8));
        }
    }
}
