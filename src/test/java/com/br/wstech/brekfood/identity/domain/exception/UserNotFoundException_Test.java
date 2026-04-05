package com.br.wstech.brekfood.identity.domain.exception;

import com.br.wstech.brekfood.shared.domain.exception.DomainException;
import com.br.wstech.brekfood.shared.domain.exception.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserNotFoundException")
class UserNotFoundException_Test {

    @Nested
    @DisplayName("when constructed with a UUID")
    class WithUUID {

        @Test
        @DisplayName("should produce message 'User not found with id: <uuid>'")
        void shouldFormatMessageWithUUID() {
            UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

            UserNotFoundException ex = new UserNotFoundException(id);

            assertThat(ex.getMessage())
                    .isEqualTo("User not found with id: 550e8400-e29b-41d4-a716-446655440000");
        }

        @Test
        @DisplayName("should extend EntityNotFoundException")
        void shouldExtendEntityNotFoundException() {
            UserNotFoundException ex = new UserNotFoundException(UUID.randomUUID());
            assertThat(ex).isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("should extend DomainException")
        void shouldExtendDomainException() {
            UserNotFoundException ex = new UserNotFoundException(UUID.randomUUID());
            assertThat(ex).isInstanceOf(DomainException.class);
        }

        @Test
        @DisplayName("should extend RuntimeException (unchecked)")
        void shouldBeUnchecked() {
            UserNotFoundException ex = new UserNotFoundException(UUID.randomUUID());
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("when constructed with an email")
    class WithEmail {

        @Test
        @DisplayName("should produce message 'User not found: <email>'")
        void shouldFormatMessageWithEmail() {
            UserNotFoundException ex = new UserNotFoundException("alice@brekfood.com");

            assertThat(ex.getMessage())
                    .isEqualTo("User not found: alice@brekfood.com");
        }

        @Test
        @DisplayName("should extend EntityNotFoundException")
        void shouldExtendEntityNotFoundException() {
            UserNotFoundException ex = new UserNotFoundException("alice@brekfood.com");
            assertThat(ex).isInstanceOf(EntityNotFoundException.class);
        }
    }
}

