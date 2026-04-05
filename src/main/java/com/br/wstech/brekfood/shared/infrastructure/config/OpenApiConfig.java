package com.br.wstech.brekfood.shared.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * SpringDoc OpenAPI configuration for the BrekFood API.
 *
 * <p>Generates interactive API documentation at:
 * <ul>
 *   <li>Swagger UI: {@code /swagger-ui.html}</li>
 *   <li>OpenAPI JSON: {@code /api-docs}</li>
 * </ul>
 *
 * <p>Security: All protected endpoints require a Bearer JWT in the Authorization header.
 * Use {@code POST /api/v1/auth/login} to obtain a token.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI brekFoodOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH_SCHEME))
                .components(securityComponents())
                .tags(apiTags());
    }

    private Info apiInfo() {
        return new Info()
                .title("BrekFood API")
                .description("""
                        **BrekFood** — Fairness-first delivery platform.
                        
                        ## Core Differentiators
                        - **Pricing Engine**: Transparent fees (5–15%), surge pricing, loyalty discounts
                        - **Earnings Engine**: Fair driver earnings with minimum guarantees and anti-starvation rules
                        - **Dispatch Algorithm**: Score-based assignment balancing proximity, idle time, and earnings equity
                        
                        ## Authentication
                        All protected endpoints require a Bearer JWT token.
                        Obtain a token via `POST /api/v1/auth/login` and include it as:
                        ```
                        Authorization: Bearer <token>
                        ```
                        """)
                .version("v1.0.0")
                .contact(new Contact()
                        .name("WSTech Solutions")
                        .email("dev@wstech.com.br")
                        .url("https://wstech.com.br"))
                .license(new License()
                        .name("Proprietary")
                        .url("https://wstech.com.br/license"));
    }

    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes(BEARER_AUTH_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT token obtained from POST /api/v1/auth/login"));
    }

    private List<Tag> apiTags() {
        return List.of(
                new Tag().name("Auth")
                        .description("User registration and authentication (public endpoints)"),
                new Tag().name("Customers")
                        .description("Customer profile management"),
                new Tag().name("Restaurants")
                        .description("Restaurant and menu management"),
                new Tag().name("Orders")
                        .description("Order lifecycle — create, track, and manage status transitions"),
                new Tag().name("Deliveries")
                        .description("Driver assignment and delivery tracking — includes dispatch algorithm"),
                new Tag().name("Payments")
                        .description("Payment processing and gateway webhooks"),
                new Tag().name("Earnings")
                        .description("Driver earnings — fairness engine and earnings history"),
                new Tag().name("Pricing")
                        .description("Fee calculation — pricing engine and surge pricing")
        );
    }
}

