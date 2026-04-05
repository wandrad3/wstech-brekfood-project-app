package com.br.wstech.brekfood.identity.domain.exception;

import com.br.wstech.brekfood.shared.domain.exception.DomainException;

/**
 * Thrown when a login attempt fails due to incorrect e-mail or password.
 *
 * <p>Maps to HTTP 401 Unauthorized via
 * {@link com.br.wstech.brekfood.shared.interfaces.rest.exception.GlobalExceptionHandler}.
 *
 * <p><strong>Security note:</strong> the message is intentionally vague —
 * it does not reveal whether the e-mail exists or the password is wrong,
 * which prevents user-enumeration attacks.
 */
public class InvalidCredentialsException extends DomainException {

    public static final String MESSAGE = "Invalid email or password";

    public InvalidCredentialsException() {
        super(MESSAGE);
    }
}

