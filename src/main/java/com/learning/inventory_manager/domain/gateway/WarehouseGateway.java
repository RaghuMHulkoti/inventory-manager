package com.learning.inventory_manager.domain.gateway;

import java.util.UUID;

/**
 * Warehouse Gateway - domain abstraction for warehouse persistence.
 */
public interface WarehouseGateway {
    void validateWarehouseExists(UUID warehouseId);
}

