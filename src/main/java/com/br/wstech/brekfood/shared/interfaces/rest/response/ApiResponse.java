package com.br.wstech.brekfood.shared.interfaces.rest.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.Instant;

/**
 * Standard API success response wrapper for all BrekFood endpoints.
 *
 * <p>Ensures a consistent response envelope across the entire API:
 * <pre>
 * {
 *   "timestamp": "2026-03-31T12:00:00Z",
 *   "data": { ... }
 * }
 * </pre>
 *
 * @param <T> the type of the response payload
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final Instant timestamp;
    private final T data;

    private ApiResponse(T data) {
        this.timestamp = Instant.now();
        this.data = data;
    }

    /**
     * Wraps the given payload in a standard API response.
     */
    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data);
    }
}

