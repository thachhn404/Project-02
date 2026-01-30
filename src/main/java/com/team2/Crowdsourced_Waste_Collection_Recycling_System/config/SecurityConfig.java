package com.team2.Crowdsourced_Waste_Collection_Recycling_System.config;

import org.springframework.web.filter.CorsFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
/**
 * Cấu hình Spring Security (Spring Security 6) cho hệ thống.
 *
 * - Cho phép một số endpoint công khai (đăng nhập/đăng ký/Swagger...).
 * - Các request còn lại bắt buộc phải có JWT hợp lệ (stateless).
 * - Xác thực JWT thông qua cơ chế OAuth2 Resource Server.
 * - Tùy biến cách map claim "scope" trong JWT sang authorities của Spring Security.
 */
public class SecurityConfig {
    private final String[] PUBLIC_ENDPOINTS = {
        "/api/auth/login", "/api/auth/register", "/api/auth/introspect", "/api/auth/logout", "/api/auth/refresh", "/api/auth/token",
        "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs.yaml", "/swagger-resources/**", "/webjars/**"
    };

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String corsAllowedOrigins;

    @Autowired
    private CustomJwtDecoder customJwtDecoder;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/v3/api-docs.yaml",
                "/swagger-resources/**",
                "/webjars/**"
        );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        // Quy tắc phân quyền theo URL:
        // - PUBLIC_ENDPOINTS: không cần xác thực
        // - các endpoint khác: yêu cầu đã xác thực (authenticated)
        httpSecurity.authorizeHttpRequests(request -> request.requestMatchers(PUBLIC_ENDPOINTS)
                .permitAll()
                .anyRequest()
                .authenticated());

        // Cấu hình OAuth2 Resource Server để Spring tự xử lý:
        // - đọc token từ Authorization: Bearer <jwt>
        // - decode/verify JWT
        // - tạo Authentication và đưa vào SecurityContext
        httpSecurity.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtConfigurer -> jwtConfigurer
                        .decoder(customJwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint()));

        // Tắt CSRF vì hệ thống stateless (không dùng session/cookie cho auth)
        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        return httpSecurity.build();
    }

    @Bean
    public CorsFilter corsFilter() {
        // CORS: cho phép frontend gọi API (hiện đang mở toàn bộ origin/method/header)
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        List<String> origins = List.of(corsAllowedOrigins.split("\\s*,\\s*"));
        corsConfiguration.setAllowedOrigins(origins);
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        corsConfiguration.setExposedHeaders(List.of("Authorization"));
        corsConfiguration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(source);
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        // Mặc định JwtGrantedAuthoritiesConverter sẽ prefix "SCOPE_".
        // Ở dự án này, claim "scope" đã chứa sẵn "ROLE_..." và/hoặc permission code,
        // nên bỏ prefix để dùng trực tiếp trong @PreAuthorize.
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        // Password hashing bằng BCrypt (strength = 10)
        return new BCryptPasswordEncoder(10);
    }

}
