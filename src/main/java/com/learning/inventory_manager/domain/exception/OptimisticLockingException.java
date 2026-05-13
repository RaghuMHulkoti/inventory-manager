package com.learning.inventory_manager.domain.exception;

/**
 * Thrown when inventory update fails due to optimistic locking conflict.
 * Indicates that the inventory version has changed since the last read.
 */
public class OptimisticLockingException extends InventoryException {
    public OptimisticLockingException(String message) {
        super(message);
    }

    public OptimisticLockingException(String message, Throwable cause) {
        super(message, cause);
    }
}

