package com.tpms.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

// JWT Bearer Token Auth
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)

@OpenAPIDefinition(
        info = @Info(
                title = "TPMS API",
                version = "v1.0",
                description = "Training & Placement Management System API Documentation"
        ),
        security = {
                @SecurityRequirement(name = "bearerAuth")
        }
)
@Configuration
public class SwaggerConfig {
}
