package com.learning.inventory_manager.domain.exception;

/**
 * Base exception for inventory-related errors.
 * All inventory operation failures should extend this exception.
 */
public class InventoryException extends RuntimeException {
    public InventoryException(String message) {
        super(message);
    }

    public InventoryException(String message, Throwable cause) {
        super(message, cause);
    }
}

