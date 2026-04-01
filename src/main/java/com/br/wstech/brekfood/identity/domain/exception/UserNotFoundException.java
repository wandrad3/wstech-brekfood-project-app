package com.br.wstech.brekfood.identity.domain.exception;

import com.br.wstech.brekfood.shared.domain.exception.EntityNotFoundException;

import java.util.UUID;

/**
 * Thrown when a {@link com.br.wstech.brekfood.identity.domain.model.User}
 * cannot be found by the given identifier.
 *
 * <p>Maps to HTTP 404 Not Found via {@link com.br.wstech.brekfood.shared.interfaces.rest.exception.GlobalExceptionHandler}.
 *
 * <p>Usage:
 * <pre>
 *   throw new UserNotFoundException(userId);
 *   throw new UserNotFoundException("alice@brekfood.com");
 * </pre>
 */
public class UserNotFoundException extends EntityNotFoundException {

    /**
     * Creates the exception for a user lookup by UUID.
     *
     * @param id the UUID of the user that was not found
     */
    public UserNotFoundException(UUID id) {
        super("User", id);
    }

    /**
     * Creates the exception for a user lookup by email.
     *
     * @param email the email address that was not found
     */
    public UserNotFoundException(String email) {
        super("User", email);
    }
}

