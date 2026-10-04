package com.learning.inventory_manager.exception;

import java.util.UUID;

/**
 * Thrown when no inventory row exists for a (supplierProduct, warehouse) pair or an inventory id.
 */
public class InventoryNotFoundException extends InventoryException {
    public InventoryNotFoundException(String message) {
        super(message);
    }

    public static InventoryNotFoundException byId(UUID inventoryId) {
        return new InventoryNotFoundException("Inventory not found with id: " + inventoryId);
    }

    public static InventoryNotFoundException byProductAndWarehouse(UUID supplierProductId, UUID warehouseId) {
        return new InventoryNotFoundException(
                "Inventory not found for supplierProduct=" + supplierProductId + " warehouse=" + warehouseId);
    }
}
