package com.br.wstech.brekfood.shared.domain.exception;

/**
 * Thrown when a business rule or domain invariant is violated.
 *
 * <p>Maps to HTTP 422 Unprocessable Entity via {@link com.br.wstech.brekfood.shared.interfaces.rest.exception.GlobalExceptionHandler}.
 *
 * <p>Examples:
 * <ul>
 *   <li>Attempting an invalid Order status transition</li>
 *   <li>Creating an order from an inactive restaurant</li>
 *   <li>Assigning a driver who is already delivering</li>
 * </ul>
 */
public class BusinessRuleViolationException extends DomainException {

    public BusinessRuleViolationException(String message) {
        super(message);
    }

    public BusinessRuleViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}

