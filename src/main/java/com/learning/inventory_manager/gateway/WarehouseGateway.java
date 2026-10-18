package com.learning.inventory_manager.gateway;

import java.util.UUID;

public interface WarehouseGateway {

    /**
     * Ensures the warehouse exists, is active and belongs to the given supplier.
     * A warehouse owned by another supplier is reported the same way as a missing one,
     * so callers cannot probe for other suppliers' warehouses.
     *
     * @throws com.learning.inventory_manager.exception.WarehouseNotFoundException if not
     */
    void validateWarehouseBelongsToSupplier(UUID warehouseId, UUID supplierId);
}
