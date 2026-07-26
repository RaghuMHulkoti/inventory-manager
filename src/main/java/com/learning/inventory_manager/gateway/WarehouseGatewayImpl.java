package com.learning.inventory_manager.gateway;

import java.util.UUID;

public class WarehouseGatewayImpl implements WarehouseGateway {

    @Override
    public boolean validateWarehouseExists(UUID warehouseId) {
        // Just mock it as returning true for now
        return true;
    }
}
