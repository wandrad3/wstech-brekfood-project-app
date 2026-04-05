package com.br.wstech.brekfood.shared.domain.exception;

import java.util.UUID;

/**
 * Thrown when a requested entity cannot be found in the repository.
 *
 * <p>Maps to HTTP 404 Not Found via {@link com.br.wstech.brekfood.shared.interfaces.rest.exception.GlobalExceptionHandler}.
 *
 * <p>Usage:
 * <pre>
 *   throw new EntityNotFoundException("Order", orderId);
 * </pre>
 */
public class EntityNotFoundException extends DomainException {

    public EntityNotFoundException(String entityName, UUID id) {
        super(String.format("%s not found with id: %s", entityName, id));
    }

    public EntityNotFoundException(String entityName, String identifier) {
        super(String.format("%s not found: %s", entityName, identifier));
    }
}

