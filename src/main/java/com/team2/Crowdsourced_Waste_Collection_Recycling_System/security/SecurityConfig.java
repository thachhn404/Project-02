package com.team2.Crowdsourced_Waste_Collection_Recycling_System.security;

import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration; 
import org.springframework.security.authentication.AuthenticationManager; 
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration; 
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; 
import org.springframework.security.config.annotation.web.builders.HttpSecurity; 
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; 
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer; 
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; 
import org.springframework.security.config.http.SessionCreationPolicy; 
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; 
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration; 
import org.springframework.web.cors.CorsConfigurationSource; 
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List; 

/**
 * Cấu hình bảo mật chính của ứng dụng sử dụng Spring Security 6 và OAuth2 Resource Server.
 * Tận dụng các tính năng chuẩn của Spring để xác thực JWT.
 */
@Configuration 
@EnableWebSecurity 
@EnableMethodSecurity 
public class SecurityConfig { 

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    /**
     * Cấu hình chuỗi Filter bảo mật.
     */
    @Bean 
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception { 
        http 
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth 
               .requestMatchers( 
                       "/swagger-ui.html", 
                       "/swagger-ui/**", 
                       "/v3/api-docs/**", 
                       "/v3/api-docs.yaml" 
               ).permitAll() 
               .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/**").permitAll() 
               .requestMatchers("/api/citizen/**").hasRole("CITIZEN") 
               .requestMatchers("/api/enterprise/**").hasAnyRole("ENTERPRISE", "ENTERPRISE_ADMIN") 
               .requestMatchers("/api/collector/**").hasRole("COLLECTOR") 
               .requestMatchers("/api/admin/**").hasRole("ADMIN") 
               .anyRequest().authenticated() 
            ) 
            .sessionManagement(session -> session 
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) 
            )
            // Sử dụng OAuth2 Resource Server để xác thực JWT
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );

        return http.build(); 
    }

    /**
     * Giải mã JWT bằng Secret Key (Symmetric - HS256).
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        // Đảm bảo secret được decode đúng định dạng nếu nó là Base64
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(jwtSecret);
        } catch (Exception e) {
            keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        }
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }

    /**
     * Chuyển đổi claims trong JWT thành Authorities (Roles) của Spring Security.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // Mặc định Spring tìm claim "scope" hoặc "scp".
        // Chúng ta đã lưu quyền vào claim "scope" trong JwtServiceImpl.
        // Giữ nguyên prefix "ROLE_" vì chúng ta đã thêm nó vào Claim hoặc cấu hình ở đây.
        grantedAuthoritiesConverter.setAuthorityPrefix(""); // Không thêm prefix vì trong claim đã có ROLE_
        grantedAuthoritiesConverter.setAuthoritiesClaimName("scope");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean 
    public CorsConfigurationSource corsConfigurationSource() { 
        CorsConfiguration configuration = new CorsConfiguration(); 
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173")); 
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); 
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type")); 
        configuration.setAllowCredentials(true); 
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(); 
        source.registerCorsConfiguration("/**", configuration); 
        return source; 
    } 

    @Bean 
    public PasswordEncoder passwordEncoder() { 
        return new BCryptPasswordEncoder(); 
    } 

    @Bean 
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception { 
        return authenticationConfiguration.getAuthenticationManager(); 
    } 

    @Bean 
    public WebSecurityCustomizer webSecurityCustomizer() { 
        return  webSecurity -> { 
            webSecurity.ignoring().requestMatchers("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**"); 
        }; 
    } 

}
