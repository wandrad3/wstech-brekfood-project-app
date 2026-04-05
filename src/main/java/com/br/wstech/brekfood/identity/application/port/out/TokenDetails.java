package com.br.wstech.brekfood.identity.application.port.out;

import java.time.Instant;

/**
 * Immutable value object representing a generated JWT token and its expiry.
 *
 * <p>Returned by {@link TokenProvider#generateToken} so that the application
 * service can include both values in the {@link com.br.wstech.brekfood.identity.application.dto.AuthResponse}.
 */
public record TokenDetails(

        /** The signed JWT compact serialization string. */
        String token,

        /** The moment at which this token expires (UTC). */
        Instant expiresAt
) {
}

