package com.team2.Crowdsourced_Waste_Collection_Recycling_System.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình Swagger/OpenAPI cho dự án.
 * Cho phép kiểm thử API trực tiếp qua giao diện UI và hỗ trợ xác thực Bearer Token (JWT).
 */
@Configuration
public class SwaggerConfig {

    @Value("${open.api.title:Crowdsourced Waste System}")
    private String title;

    @Value("${open.api.version:1.0.0}")
    private String version;

    @Value("${open.api.description:API Documentation}")
    private String description;

    @Bean
    public OpenAPI customOpenAPI() {
        String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info().title(title).version(version).description(description))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components().addSecuritySchemes(
                        securitySchemeName,
                        new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                ));
    }
}
