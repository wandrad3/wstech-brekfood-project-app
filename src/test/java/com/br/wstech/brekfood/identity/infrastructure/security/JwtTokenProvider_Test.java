package com.br.wstech.brekfood.identity.infrastructure.security;

import com.br.wstech.brekfood.identity.application.port.out.TokenDetails;
import com.br.wstech.brekfood.identity.domain.model.Role;
import com.br.wstech.brekfood.identity.domain.model.User;
import com.br.wstech.brekfood.shared.domain.model.BaseEntity;
import com.br.wstech.brekfood.shared.infrastructure.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtTokenProvider")
class JwtTokenProvider_Test {

    private static final String SECRET     = "test-secret-key-for-brekfood-unit-tests-min-32-chars";
    private static final long   EXPIRATION = 3_600_000L;
    private static final UUID   USER_ID    = UUID.randomUUID();

    private JwtTokenProvider provider;
    private User             testUser;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties(SECRET, EXPIRATION);
        provider = new JwtTokenProvider(props);
        provider.init();

        testUser = User.create("alice@brekfood.com", "$2a$10$hash", "Alice", Role.CUSTOMER);
        setId(testUser, USER_ID);
    }

    /** Sets the inherited BaseEntity.id via reflection (simulates JPA @GeneratedValue). */
    private static void setId(User user, UUID id) {
        try {
            Field idField = BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException("Cannot set user id via reflection", e);
        }
    }

    // -------------------------------------------------------------------------
    // generateToken
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("generateToken()")
    class GenerateToken {

        @Test
        @DisplayName("should return a non-null, non-blank token string")
        void shouldReturnNonBlankToken() {
            TokenDetails details = provider.generateToken(testUser);
            assertThat(details.token()).isNotBlank();
        }

        @Test
        @DisplayName("should return an expiry in the future")
        void shouldReturnFutureExpiry() {
            TokenDetails details = provider.generateToken(testUser);
            assertThat(details.expiresAt()).isAfter(Instant.now());
        }

        @Test
        @DisplayName("expiry should be approximately now + expirationMs")
        void expiryShouldMatchConfiguration() {
            Instant before  = Instant.now();
            TokenDetails details = provider.generateToken(testUser);
            Instant after   = Instant.now();

            assertThat(details.expiresAt())
                    .isAfterOrEqualTo(before.plusMillis(EXPIRATION - 1000))
                    .isBeforeOrEqualTo(after.plusMillis(EXPIRATION + 1000));
        }
    }

    // -------------------------------------------------------------------------
    // validateToken
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("validateToken()")
    class ValidateToken {

        @Test
        @DisplayName("should return true for a freshly generated token")
        void shouldReturnTrueForFreshToken() {
            String token = provider.generateToken(testUser).token();
            assertThat(provider.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("should return false for a tampered token")
        void shouldReturnFalseForTamperedToken() {
            String token    = provider.generateToken(testUser).token();
            String tampered = token.substring(0, token.length() - 5) + "XXXXX";
            assertThat(provider.validateToken(tampered)).isFalse();
        }

        @Test
        @DisplayName("should return false for null")
        void shouldReturnFalseForNull() {
            assertThat(provider.validateToken(null)).isFalse();
        }

        @Test
        @DisplayName("should return false for blank string")
        void shouldReturnFalseForBlank() {
            assertThat(provider.validateToken("   ")).isFalse();
        }

        @Test
        @DisplayName("should return false for a random string")
        void shouldReturnFalseForRandomString() {
            assertThat(provider.validateToken("this.is.not.a.jwt")).isFalse();
        }

        @Test
        @DisplayName("should return false for an expired token")
        void shouldReturnFalseForExpiredToken() throws InterruptedException {
            // Create a provider with 1 ms expiry (bypasses @Min validation — direct instantiation)
            JwtProperties shortProps = new JwtProperties(SECRET, 1L);
            JwtTokenProvider shortProvider = new JwtTokenProvider(shortProps);
            shortProvider.init();

            String expiredToken = shortProvider.generateToken(testUser).token();
            Thread.sleep(5); // ensure token is expired

            assertThat(shortProvider.validateToken(expiredToken)).isFalse();
        }
    }

    // -------------------------------------------------------------------------
    // Extract claims
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("claim extraction")
    class ClaimExtraction {

        private String token;

        @BeforeEach
        void generateToken() {
            token = provider.generateToken(testUser).token();
        }

        @Test
        @DisplayName("extractUserId() should return the user UUID")
        void shouldExtractUserId() {
            assertThat(provider.extractUserId(token)).isEqualTo(USER_ID);
        }

        @Test
        @DisplayName("extractEmail() should return the user email")
        void shouldExtractEmail() {
            assertThat(provider.extractEmail(token)).isEqualTo(testUser.getEmail());
        }

        @Test
        @DisplayName("extractRole() should return the role name")
        void shouldExtractRole() {
            assertThat(provider.extractRole(token)).isEqualTo(Role.CUSTOMER.name());
        }
    }
}

