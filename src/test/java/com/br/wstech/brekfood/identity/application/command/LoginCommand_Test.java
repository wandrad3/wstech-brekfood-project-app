package com.br.wstech.brekfood.identity.application.command;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LoginCommand")
class LoginCommand_Test {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private Set<ConstraintViolation<LoginCommand>> validate(LoginCommand cmd) {
        return validator.validate(cmd);
    }

    @Nested
    @DisplayName("with valid data")
    class WhenValid {

        @Test
        @DisplayName("should have no violations")
        void shouldHaveNoViolations() {
            LoginCommand cmd = new LoginCommand("alice@brekfood.com", "password123");
            assertThat(validate(cmd)).isEmpty();
        }
    }

    @Nested
    @DisplayName("email validation")
    class EmailValidation {

        @Test
        @DisplayName("blank email should fail")
        void blankEmailShouldFail() {
            LoginCommand cmd = new LoginCommand("", "password123");
            assertThat(validate(cmd)).extracting(v -> v.getPropertyPath().toString()).contains("email");
        }

        @Test
        @DisplayName("null email should fail")
        void nullEmailShouldFail() {
            LoginCommand cmd = new LoginCommand(null, "password123");
            assertThat(validate(cmd)).extracting(v -> v.getPropertyPath().toString()).contains("email");
        }

        @Test
        @DisplayName("invalid email format should fail")
        void invalidEmailFormatShouldFail() {
            LoginCommand cmd = new LoginCommand("not-an-email", "password123");
            assertThat(validate(cmd)).extracting(v -> v.getPropertyPath().toString()).contains("email");
        }
    }

    @Nested
    @DisplayName("password validation")
    class PasswordValidation {

        @Test
        @DisplayName("blank password should fail")
        void blankPasswordShouldFail() {
            LoginCommand cmd = new LoginCommand("alice@brekfood.com", "");
            assertThat(validate(cmd)).extracting(v -> v.getPropertyPath().toString()).contains("password");
        }

        @Test
        @DisplayName("null password should fail")
        void nullPasswordShouldFail() {
            LoginCommand cmd = new LoginCommand("alice@brekfood.com", null);
            assertThat(validate(cmd)).extracting(v -> v.getPropertyPath().toString()).contains("password");
        }
    }
}

