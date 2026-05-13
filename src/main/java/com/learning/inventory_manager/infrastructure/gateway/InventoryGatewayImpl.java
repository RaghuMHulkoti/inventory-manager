package com.learning.inventory_manager.infrastructure.gateway;

import com.learning.inventory_manager.domain.exception.OptimisticLockingException;
import com.learning.inventory_manager.domain.gateway.InventoryGateway;
import com.learning.inventory_manager.domain.model.Inventory;
import com.learning.inventory_manager.infrastructure.mapper.InventoryMapper;
import com.learning.inventory_manager.infrastructure.persistence.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

/**
 * Inventory Gateway implementation.
 * Bridges domain logic and persistence layer with optimistic locking handling.
 *
 * Responsibility:
 * - Convert domain models to/from entities
 * - Handle optimistic locking exceptions
 * - Provide repository abstraction to domain layer
 */
@Component
@RequiredArgsConstructor
public class InventoryGatewayImpl implements InventoryGateway {
    private static final Logger log = LoggerFactory.getLogger(InventoryGatewayImpl.class);
    private final InventoryRepository repository;
    private final InventoryMapper mapper;

    @Override
    public Optional<Inventory> findBySupplierProductIdAndWarehouseId(UUID supplierProductId, UUID warehouseId) {
        log.debug("Finding inventory for product {} and warehouse {}", supplierProductId, warehouseId);
        
        return repository.findBySupplierProductIdAndWarehouseId(supplierProductId, warehouseId)
            .map(mapper::toDomain);
    }

    @Override
    public Inventory save(Inventory inventory) {
        log.debug("Updating inventory: {}", inventory.getId());
        
        try {
            var entity = mapper.toEntity(inventory);
            var saved = repository.save(entity);
            return mapper.toDomain(saved);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.error("Optimistic locking failure for inventory: {}", inventory.getId(), e);
            throw new OptimisticLockingException(
                "Inventory version conflict. Inventory may have been updated by another process.",
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
}

