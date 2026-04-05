package com.br.wstech.brekfood.identity.application.service;

import com.br.wstech.brekfood.identity.application.command.LoginCommand;
import com.br.wstech.brekfood.identity.application.command.RegisterCommand;
import com.br.wstech.brekfood.identity.application.dto.AuthResponse;

/**
 * Use-case interface for the Identity &amp; Auth bounded context.
 *
 * <p>Defines the two primary auth flows:
 * <ul>
 *   <li>{@link #register} — creates a new user account and returns a ready-to-use JWT</li>
 *   <li>{@link #login}    — verifies credentials and returns a fresh JWT</li>
 * </ul>
 *
 * <p>The concrete implementation is {@link AuthServiceImpl}.
 * Controllers and other callers must depend only on this interface.
 */
public interface AuthService {

    /**
     * Registers a new user and returns a JWT token.
     *
     * @param command validated registration data
     * @return {@link AuthResponse} with token, expiry, and basic user info
     * @throws com.br.wstech.brekfood.identity.domain.exception.EmailAlreadyRegisteredException
     *         if the e-mail is already taken
     */
    AuthResponse register(RegisterCommand command);

    /**
     * Authenticates an existing user by e-mail + password.
     *
     * @param command validated login credentials
     * @return {@link AuthResponse} with a fresh token, expiry, and basic user info
     * @throws com.br.wstech.brekfood.identity.domain.exception.InvalidCredentialsException
     *         if the e-mail does not exist or the password does not match
     */
    AuthResponse login(LoginCommand command);
}

