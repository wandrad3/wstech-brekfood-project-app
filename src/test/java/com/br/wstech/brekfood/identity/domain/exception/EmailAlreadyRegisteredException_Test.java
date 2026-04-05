package com.br.wstech.brekfood.identity.domain.exception;

import com.br.wstech.brekfood.shared.domain.exception.BusinessRuleViolationException;
import com.br.wstech.brekfood.shared.domain.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EmailAlreadyRegisteredException")
class EmailAlreadyRegisteredException_Test {

    @Test
    @DisplayName("should extend BusinessRuleViolationException")
    void shouldExtendBusinessRuleViolationException() {
        assertThat(new EmailAlreadyRegisteredException("x@x.com"))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("should extend DomainException (unchecked)")
    void shouldExtendDomainException() {
        assertThat(new EmailAlreadyRegisteredException("x@x.com"))
                .isInstanceOf(DomainException.class)
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("should include the duplicate email in the message")
    void shouldIncludeEmailInMessage() {
        String email = "alice@brekfood.com";
        EmailAlreadyRegisteredException ex = new EmailAlreadyRegisteredException(email);

        assertThat(ex.getMessage()).contains(email);
    }

    @Test
    @DisplayName("should produce distinct messages for different emails")
    void shouldProduceDistinctMessagesForDifferentEmails() {
        String msgA = new EmailAlreadyRegisteredException("a@test.com").getMessage();
        String msgB = new EmailAlreadyRegisteredException("b@test.com").getMessage();

        assertThat(msgA).isNotEqualTo(msgB);
    }
}

