package com.br.wstech.brekfood.shared.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EntityNotFoundException")
class EntityNotFoundException_Test {

    @Nested
    @DisplayName("when created with UUID id")
    class WithUUID {

        @Test
        @DisplayName("should format message as '<EntityName> not found with id: <uuid>'")
        void shouldFormatMessageWithUUID() {
            UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

            EntityNotFoundException ex = new EntityNotFoundException("Order", id);

            assertThat(ex.getMessage())
                    .isEqualTo("Order not found with id: 550e8400-e29b-41d4-a716-446655440000");
        }

        @Test
        @DisplayName("should be a DomainException")
        void shouldBeDomainException() {
            EntityNotFoundException ex = new EntityNotFoundException("Driver", UUID.randomUUID());

            assertThat(ex).isInstanceOf(DomainException.class);
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("when created with string identifier")
    class WithStringIdentifier {

        @Test
        @DisplayName("should format message as '<EntityName> not found: <identifier>'")
        void shouldFormatMessageWithStringIdentifier() {
            EntityNotFoundException ex = new EntityNotFoundException("User", "john@brekfood.com");

            assertThat(ex.getMessage())
                    .isEqualTo("User not found: john@brekfood.com");
        }
    }
}

