package com.br.wstech.brekfood.identity.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable DTO returned by both register and login use cases.
 *
 * <p>Carries the JWT token together with enough user context for the client
 * to bootstrap its session without requiring an immediate profile call.
 */
public record AuthResponse(

        /** Signed JWT compact serialization string. */
        String token,

        /** UTC instant at which the token expires. */
        Instant expiresAt,

        /** UUID of the authenticated user. */
        UUID userId,

        /** Normalized (lower-cased) e-mail of the authenticated user. */
        String email,

        /** Display name of the authenticated user. */
        String name,

        /** Human-readable role label (e.g., {@code "Customer"}). */
        String roleDisplayName
) {
}

