package com.br.wstech.brekfood.identity.domain.exception;

import com.br.wstech.brekfood.shared.domain.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InvalidCredentialsException")
class InvalidCredentialsException_Test {

    @Test
    @DisplayName("should extend DomainException (unchecked)")
    void shouldExtendDomainException() {
        assertThat(new InvalidCredentialsException())
                .isInstanceOf(DomainException.class)
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("should carry the constant vague message (no user-specific detail)")
    void shouldCarryVagueMessage() {
        InvalidCredentialsException ex = new InvalidCredentialsException();

        assertThat(ex.getMessage())
                .isEqualTo(InvalidCredentialsException.MESSAGE)
                .doesNotContain("not found")
                .doesNotContain("does not exist")
                .doesNotContain("@");  // no specific email address leaked
    }

    @Test
    @DisplayName("MESSAGE constant should match the exception message")
    void constantShouldMatchMessage() {
        assertThat(InvalidCredentialsException.MESSAGE)
                .isEqualTo(new InvalidCredentialsException().getMessage());
    }
}

