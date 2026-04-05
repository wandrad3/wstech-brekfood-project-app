package com.br.wstech.brekfood.identity.application.command;

import com.br.wstech.brekfood.identity.domain.model.Role;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RegisterCommand")
class RegisterCommand_Test {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private Set<ConstraintViolation<RegisterCommand>> validate(RegisterCommand cmd) {
        return validator.validate(cmd);
    }

    @Nested
    @DisplayName("with valid data")
    class WhenValid {

        @Test
        @DisplayName("should have no violations")
        void shouldHaveNoViolations() {
            RegisterCommand cmd = new RegisterCommand("alice@brekfood.com", "password123", "Alice", Role.CUSTOMER);
            assertThat(validate(cmd)).isEmpty();
        }
    }

    @Nested
    @DisplayName("email validation")
    class EmailValidation {

        @Test
        @DisplayName("blank email should fail")
        void blankEmailShouldFail() {
            RegisterCommand cmd = new RegisterCommand("", "password123", "Alice", Role.CUSTOMER);
            assertThat(validate(cmd)).extracting(v -> v.getPropertyPath().toString()).contains("email");
        }

        @Test
        @DisplayName("invalid email format should fail")
        void invalidEmailFormatShouldFail() {
            RegisterCommand cmd = new RegisterCommand("not-an-email", "password123", "Alice", Role.CUSTOMER);
            assertThat(validate(cmd)).extracting(v -> v.getPropertyPath().toString()).contains("email");
        }

        @Test
        @DisplayName("null email should fail")
        void nullEmailShouldFail() {
            RegisterCommand cmd = new RegisterCommand(null, "password123", "Alice", Role.CUSTOMER);
            assertThat(validate(cmd)).extracting(v -> v.getPropertyPath().toString()).contains("email");
        }
    }

    @Nested
    @DisplayName("password validation")
    class PasswordValidation {

        @Test
        @DisplayName("blank password should fail")
        void blankPasswordShouldFail() {
            RegisterCommand cmd = new RegisterCommand("alice@brekfood.com", "", "Alice", Role.CUSTOMER);
            assertThat(validate(cmd)).extracting(v -> v.getPropertyPath().toString()).contains("password");
        }

        @Test
        @DisplayName("password shorter than 8 chars should fail")
        void shortPasswordShouldFail() {
            RegisterCommand cmd = new RegisterCommand("alice@brekfood.com", "abc123", "Alice", Role.CUSTOMER);
            assertThat(validate(cmd)).extracting(v -> v.getPropertyPath().toString()).contains("password");
        }
    }

    @Nested
    @DisplayName("name validation")
    class NameValidation {

        @Test
        @DisplayName("blank name should fail")
        void blankNameShouldFail() {
            RegisterCommand cmd = new RegisterCommand("alice@brekfood.com", "password123", "", Role.CUSTOMER);
            assertThat(validate(cmd)).extracting(v -> v.getPropertyPath().toString()).contains("name");
        }

        @Test
        @DisplayName("name exceeding 100 chars should fail")
        void tooLongNameShouldFail() {
            String longName = "A".repeat(101);
            RegisterCommand cmd = new RegisterCommand("alice@brekfood.com", "password123", longName, Role.CUSTOMER);
            assertThat(validate(cmd)).extracting(v -> v.getPropertyPath().toString()).contains("name");
        }
    }

    @Nested
    @DisplayName("role validation")
    class RoleValidation {

        @Test
        @DisplayName("null role should fail")
        void nullRoleShouldFail() {
            RegisterCommand cmd = new RegisterCommand("alice@brekfood.com", "password123", "Alice", null);
            assertThat(validate(cmd)).extracting(v -> v.getPropertyPath().toString()).contains("role");
        }
    }
}

