package com.ticketing.system.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI ticketingOpenApi() {
        SecurityScheme jwtScheme = new SecurityScheme()
                .name(BEARER_AUTH)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        return new OpenAPI()
                .info(new Info()
                        .title("Enterprise Service Ticketing System API")
                        .version("v1")
                        .description("REST API for customer, admin, and service manager ticketing workflows."))
                .components(new Components().addSecuritySchemes(BEARER_AUTH, jwtScheme))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }
}
