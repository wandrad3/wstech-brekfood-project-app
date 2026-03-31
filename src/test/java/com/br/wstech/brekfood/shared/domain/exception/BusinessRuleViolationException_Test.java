package com.br.wstech.brekfood.shared.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BusinessRuleViolationException")
class BusinessRuleViolationException_Test {

    @Nested
    @DisplayName("when created with message only")
    class WithMessageOnly {

        @Test
        @DisplayName("should preserve the message")
        void shouldPreserveMessage() {
            String msg = "Order status transition from PENDING to DELIVERED is not allowed";

            BusinessRuleViolationException ex = new BusinessRuleViolationException(msg);

            assertThat(ex.getMessage()).isEqualTo(msg);
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("should be a DomainException")
        void shouldBeDomainException() {
            BusinessRuleViolationException ex = new BusinessRuleViolationException("rule violated");

            assertThat(ex).isInstanceOf(DomainException.class);
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("when created with message and cause")
    class WithMessageAndCause {

        @Test
        @DisplayName("should preserve message and cause")
        void shouldPreserveMessageAndCause() {
            Throwable cause = new IllegalStateException("root cause");
            BusinessRuleViolationException ex =
                    new BusinessRuleViolationException("business rule failed", cause);

            assertThat(ex.getMessage()).isEqualTo("business rule failed");
            assertThat(ex.getCause()).isSameAs(cause);
        }
    }
}

