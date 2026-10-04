package com.learning.inventory_manager.gateway;

import com.learning.inventory_manager.exception.WarehouseNotFoundException;

import java.util.UUID;

public class WarehouseGatewayImpl implements WarehouseGateway {

    private final WarehouseRepository repository;

    public WarehouseGatewayImpl(WarehouseRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validateWarehouseExists(UUID warehouseId) {
        if (!repository.existsByIdAndIsActiveTrue(warehouseId)) {
            throw new WarehouseNotFoundException(warehouseId);
        }
    }
}
