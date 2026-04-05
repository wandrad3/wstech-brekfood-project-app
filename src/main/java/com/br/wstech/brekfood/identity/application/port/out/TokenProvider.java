package com.br.wstech.brekfood.identity.application.port.out;

import com.br.wstech.brekfood.identity.domain.model.User;

import java.util.UUID;

/**
 * Output port for JWT token operations.
 *
 * <p>Defined in the application layer so that {@code AuthServiceImpl} depends only
 * on this interface, not on JJWT or any infrastructure detail.
 * The concrete implementation {@code JwtTokenProvider} lives in the infrastructure layer.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Generate a signed JWT from a {@link User} aggregate</li>
 *   <li>Validate the signature and expiry of an incoming token string</li>
 *   <li>Extract individual claims from a valid token</li>
 * </ul>
 */
public interface TokenProvider {

    /**
     * Generates a signed JWT token for the given user.
     *
     * @param user the authenticated / registered user
     * @return a {@link TokenDetails} containing the compact token and its expiry
     */
    TokenDetails generateToken(User user);

    /**
     * Validates the token's signature and expiry.
     *
     * @param token the JWT compact serialization string
     * @return {@code true} if the token is well-formed, signed correctly, and not expired
     */
    boolean validateToken(String token);

    /**
     * Extracts the {@code sub} claim (user UUID) from a valid token.
     *
     * @param token a valid JWT
     * @return the user's UUID
     */
    UUID extractUserId(String token);

    /**
     * Extracts the {@code email} claim from a valid token.
     *
     * @param token a valid JWT
     * @return the user's e-mail address
     */
    String extractEmail(String token);

    /**
     * Extracts the {@code role} claim from a valid token.
     *
     * @param token a valid JWT
     * @return the role name (e.g., {@code "CUSTOMER"})
     */
    String extractRole(String token);
}

