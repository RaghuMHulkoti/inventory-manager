package com.learning.inventory_manager.exception;

import java.util.UUID;

/**
 * Thrown when a reservation attempt fails because the available stock
 * is less than the requested quantity.
 */
public class InsufficientStockException extends InventoryException {

    private final UUID supplierProductId;
    private final UUID warehouseId;
    private final int available;
    private final int requested;

    public InsufficientStockException(UUID supplierProductId, UUID warehouseId,
                                       int available, int requested) {
        super(String.format(
            "Insufficient stock for supplierProduct=%s warehouse=%s: available=%d requested=%d",
            supplierProductId, warehouseId, available, requested
        ));
        this.supplierProductId = supplierProductId;
        this.warehouseId = warehouseId;
        this.available = available;
        this.requested = requested;
    }

    public UUID getSupplierProductId() { return supplierProductId; }
    public UUID getWarehouseId() { return warehouseId; }
    public int getAvailable() { return available; }
    public int getRequested() { return requested; }
}
