package com.br.wstech.brekfood.shared.domain.exception;

/**
 * Base class for all BrekFood domain exceptions.
 *
 * <p>Domain exceptions represent violations of business rules and invariants.
 * They are unchecked by design — domain logic should not need to declare throws.
 *
 * <p>All context-specific exceptions should extend this class, e.g.:
 * <pre>
 *   public class OrderNotFoundException extends DomainException { ... }
 * </pre>
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}

