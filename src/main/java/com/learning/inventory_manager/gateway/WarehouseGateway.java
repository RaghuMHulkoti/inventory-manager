package com.learning.inventory_manager.gateway;

import java.util.UUID;

public interface WarehouseGateway {

    /**
     * Ensures the warehouse exists and is active.
     *
     * @throws com.learning.inventory_manager.exception.WarehouseNotFoundException if not
     */
    void validateWarehouseExists(UUID warehouseId);
}
