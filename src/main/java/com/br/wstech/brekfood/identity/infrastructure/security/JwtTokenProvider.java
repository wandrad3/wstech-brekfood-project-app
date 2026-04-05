package com.br.wstech.brekfood.identity.infrastructure.security;

import com.br.wstech.brekfood.identity.application.port.out.TokenDetails;
import com.br.wstech.brekfood.identity.application.port.out.TokenProvider;
import com.br.wstech.brekfood.identity.domain.model.User;
import com.br.wstech.brekfood.shared.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * JJWT 0.12.x implementation of {@link TokenProvider}.
 *
 * <p>Reads JWT configuration from {@link JwtProperties}
 * ({@code brekfood.security.jwt.*} in {@code application.properties}).
 *
 * <p>Token structure (claims):
 * <ul>
 *   <li>{@code sub}   — user UUID (string)</li>
 *   <li>{@code email} — user e-mail</li>
 *   <li>{@code role}  — Role enum name (e.g., {@code "CUSTOMER"})</li>
 *   <li>{@code iat}   — issued-at timestamp</li>
 *   <li>{@code exp}   — expiration timestamp</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider implements TokenProvider {

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLE  = "role";

    private final JwtProperties jwtProperties;

    /** Derived once from the configured secret; reused for all sign/verify operations. */
    private SecretKey secretKey;

    @PostConstruct
    void init() {
        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.secret().getBytes(StandardCharsets.UTF_8)
        );
    }

    // -------------------------------------------------------------------------
    // TokenProvider implementation
    // -------------------------------------------------------------------------

    @Override
    public TokenDetails generateToken(User user) {
        Instant now      = Instant.now();
        Instant expiresAt = now.plusMillis(jwtProperties.expirationMs());

        String token = Jwts.builder()
                .subject(user.getId().toString())
                .claim(CLAIM_EMAIL, user.getEmail())
                .claim(CLAIM_ROLE, user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();

        return new TokenDetails(token, expiresAt);
    }

    @Override
    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }

    @Override
    public UUID extractUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    @Override
    public String extractEmail(String token) {
        return parseClaims(token).get(CLAIM_EMAIL, String.class);
    }

    @Override
    public String extractRole(String token) {
        return parseClaims(token).get(CLAIM_ROLE, String.class);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

