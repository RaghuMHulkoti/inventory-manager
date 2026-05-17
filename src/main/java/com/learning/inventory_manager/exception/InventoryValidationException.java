package com.learning.inventory_manager.exception;

/**
 * Thrown when inventory operation fails validation.
 * Examples: negative quantity, invalid state transitions, etc.
 */
public class InventoryValidationException extends InventoryException {
    public InventoryValidationException(String message) {
        super(message);
    }

    public InventoryValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

