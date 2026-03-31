package com.br.wstech.brekfood.shared.infrastructure.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "brekfood.security.jwt")
public record JwtProperties(
        @NotBlank(message = "JWT secret must not be blank")
        @Size(min = 32, message = "JWT secret must be at least 32 characters (256 bits)")
        String secret,
        @Min(value = 60000, message = "JWT expiration must be at least 60 seconds")
        long expirationMs
) {
}
