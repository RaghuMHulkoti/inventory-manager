package com.learning.inventory_manager.gateway;

import com.learning.inventory_manager.gateway.entity.WarehouseEntity;

import java.util.Optional;
import java.util.UUID;

public interface WarehouseGateway {
    boolean validateWarehouseExists(UUID warehouseId);
}
