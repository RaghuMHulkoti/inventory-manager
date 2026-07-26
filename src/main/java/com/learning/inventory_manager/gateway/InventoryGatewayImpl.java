package com.learning.inventory_manager.gateway;

import com.learning.inventory_manager.core.domain.Inventory;
import com.learning.inventory_manager.exception.OptimisticLockingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class InventoryGatewayImpl implements InventoryGateway {

    private static final Logger log = LoggerFactory.getLogger(InventoryGatewayImpl.class);

    private final InventoryRepository repository;
    private final InventoryEntityMapper mapper;

    public InventoryGatewayImpl(InventoryRepository repository, InventoryEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Inventory> findBySupplierProductIdAndWarehouseId(UUID supplierProductId, UUID warehouseId) {
        return repository.findBySupplierProductIdAndWarehouseId(supplierProductId, warehouseId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Inventory> findBySupplierProductId(UUID supplierProductId) {
        return repository.findBySupplierProductId(supplierProductId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Inventory> findById(UUID inventoryId) {
        return repository.findById(inventoryId).map(mapper::toDomain);
    }

    @Override
    public Inventory save(Inventory inventory) {
        try {
            var entity = mapper.toEntity(inventory);
            var saved = repository.save(entity);
            return mapper.toDomain(saved);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.error("Optimistic locking failure for inventory: {}", inventory.getId(), e);
            throw new OptimisticLockingException(
                    "Inventory version conflict. Please retry.",
                    e
            );
        }
    }

    @Override
    public Inventory create(Inventory inventory) {
        log.debug("Creating new inventory for product {} and warehouse {}",
                inventory.getSupplierProductId(), inventory.getWarehouseId());
        var entity = mapper.toEntity(inventory);
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public boolean tryReserveStock(UUID inventoryId, int qty) {
        int rowsAffected = repository.tryReserveStock(inventoryId, qty);
        log.debug("tryReserveStock inventory={} qty={} rowsAffected={}", inventoryId, qty, rowsAffected);
        return rowsAffected == 1;
    }

    @Override
    public void releaseStock(UUID inventoryId, int qty) {
        int rowsAffected = repository.releaseStock(inventoryId, qty);
        log.debug("releaseStock inventory={} qty={} rowsAffected={}", inventoryId, qty, rowsAffected);
    }

    @Override
    public void deductStock(UUID inventoryId, int qty) {
        int rowsAffected = repository.deductStock(inventoryId, qty);
        log.debug("deductStock inventory={} qty={} rowsAffected={}", inventoryId, qty, rowsAffected);
    }
}
