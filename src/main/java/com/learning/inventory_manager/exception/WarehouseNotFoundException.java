package com.learning.inventory_manager.exception;

import java.util.UUID;

/**
 * Thrown when a warehouse is not found or is inactive.
 * Used during validation phase before processing inventory updates.
 */
public class WarehouseNotFoundException extends InventoryException {
    private final UUID warehouseId;

    public WarehouseNotFoundException(UUID warehouseId) {
        super("Warehouse not found or is inactive: " + warehouseId);
        this.warehouseId = warehouseId;
    }

    public UUID getWarehouseId() {
        return warehouseId;
    }
}

