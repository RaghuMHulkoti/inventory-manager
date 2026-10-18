package com.learning.inventory_manager.gateway;

import com.learning.inventory_manager.exception.WarehouseNotFoundException;

import java.util.UUID;

public class WarehouseGatewayImpl implements WarehouseGateway {

    private final WarehouseRepository repository;

    public WarehouseGatewayImpl(WarehouseRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validateWarehouseBelongsToSupplier(UUID warehouseId, UUID supplierId) {
        if (!repository.existsByIdAndSupplierIdAndIsActiveTrue(warehouseId, supplierId)) {
            throw new WarehouseNotFoundException(warehouseId);
        }
    }
}
