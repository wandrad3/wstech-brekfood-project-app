package com.br.wstech.brekfood.identity.domain.exception;

import com.br.wstech.brekfood.shared.domain.exception.BusinessRuleViolationException;

/**
 * Thrown when a registration attempt is made with an e-mail address
 * that already belongs to an existing {@link com.br.wstech.brekfood.identity.domain.model.User}.
 *
 * <p>Maps to HTTP 422 Unprocessable Entity via
 * {@link com.br.wstech.brekfood.shared.interfaces.rest.exception.GlobalExceptionHandler}.
 *
 * <p>The error message intentionally includes the e-mail to aid debugging on the client side,
 * because e-mail uniqueness is not a security-sensitive constraint.
 */
public class EmailAlreadyRegisteredException extends BusinessRuleViolationException {

    /**
     * @param email the duplicate e-mail address attempted during registration
     */
    public EmailAlreadyRegisteredException(String email) {
        super("E-mail already registered: " + email);
    }
}

