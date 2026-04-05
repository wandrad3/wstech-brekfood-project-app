package com.br.wstech.brekfood.shared.infrastructure.config;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
@DisplayName("JwtProperties")
class JwtProperties_Test {
    private static Validator validator;
    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }
    @Nested
    @DisplayName("when configured correctly")
    class ValidConfiguration {
        @Test
        @DisplayName("should pass validation with valid secret and expiration")
        void shouldPassValidation() {
            JwtProperties props = new JwtProperties(
                    "this-is-a-valid-secret-with-32-chars-minimum!!",
                    86400000L
            );
            Set<ConstraintViolation<JwtProperties>> violations = validator.validate(props);
            assertThat(violations).isEmpty();
        }
        @Test
        @DisplayName("should expose secret and expirationMs via record accessors")
        void shouldExposeProperties() {
            String secret = "valid-secret-that-is-long-enough-for-hs256-algorithm";
            long expMs = 3600000L;
            JwtProperties props = new JwtProperties(secret, expMs);
            assertThat(props.secret()).isEqualTo(secret);
            assertThat(props.expirationMs()).isEqualTo(expMs);
        }
    }
    @Nested
    @DisplayName("when misconfigured")
    class InvalidConfiguration {
        @Test
        @DisplayName("should fail validation when secret is blank")
        void shouldFailWhenSecretBlank() {
            JwtProperties props = new JwtProperties("", 86400000L);
            Set<ConstraintViolation<JwtProperties>> violations = validator.validate(props);
            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("secret");
        }
        @Test
        @DisplayName("should fail validation when secret is shorter than 32 chars")
        void shouldFailWhenSecretTooShort() {
            JwtProperties props = new JwtProperties("short", 86400000L);
            Set<ConstraintViolation<JwtProperties>> violations = validator.validate(props);
            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("secret");
        }
        @Test
        @DisplayName("should fail validation when expiration is less than 60000ms")
        void shouldFailWhenExpirationTooShort() {
            JwtProperties props = new JwtProperties(
                    "valid-secret-that-is-long-enough-for-hs256!",
                    30000L
            );
            Set<ConstraintViolation<JwtProperties>> violations = validator.validate(props);
            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("expirationMs");
        }
    }
}
