package com.br.wstech.brekfood.shared.interfaces.rest.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

/**
 * Standard API error response for all BrekFood error scenarios.
 *
 * <p>Provides a consistent error envelope:
 * <pre>
 * {
 *   "timestamp": "2026-03-31T12:00:00Z",
 *   "status": 422,
 *   "error": "BUSINESS_RULE_VIOLATION",
 *   "message": "Order status transition from PENDING to DELIVERED is not allowed",
 *   "path": "/api/v1/orders/123/status",
 *   "fieldErrors": [
 *     { "field": "email", "message": "must not be blank" }
 *   ]
 * }
 * </pre>
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final List<FieldError> fieldErrors;

    /**
     * Represents a single field-level validation error.
     */
    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FieldError {
        private final String field;
        private final String message;
        private final Object rejectedValue;
    }
}

