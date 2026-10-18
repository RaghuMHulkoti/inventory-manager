package com.learning.inventory_manager.gateway;

import com.learning.inventory_manager.exception.WarehouseNotFoundException;

import com.learning.inventory_manager.core.domain.Warehouse;
import com.learning.inventory_manager.exception.InventoryValidationException;
import com.learning.inventory_manager.gateway.entity.WarehouseEntity;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
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

    @Override
    public Optional<Warehouse> findById(UUID warehouseId) {
        return repository.findById(warehouseId).map(WarehouseGatewayImpl::toDomain);
    }

    @Override
    public Warehouse save(Warehouse warehouse) {
        // load-then-modify keeps created_at, which is not updatable
        var entity = repository.findById(warehouse.id()).orElseGet(() -> WarehouseEntity.builder().id(warehouse.id()).build());
        entity.setSupplierId(warehouse.supplierId());
        entity.setCode(warehouse.code());
        entity.setName(warehouse.name());
        entity.setCity(warehouse.city());
        entity.setState(warehouse.state());
        entity.setCountry(warehouse.country());
        entity.setIsActive(warehouse.active());
        try {
            return toDomain(repository.saveAndFlush(entity));
        } catch (DataIntegrityViolationException e) {
            throw new InventoryValidationException("Warehouse code " + warehouse.code() + " is already used by another warehouse");
        }
    }

    private static Warehouse toDomain(WarehouseEntity e) {
        return new Warehouse(e.getId(), e.getSupplierId(), e.getCode(), e.getName(), e.getCity(), e.getState(),
                e.getCountry(), Boolean.TRUE.equals(e.getIsActive()));
    }
}
