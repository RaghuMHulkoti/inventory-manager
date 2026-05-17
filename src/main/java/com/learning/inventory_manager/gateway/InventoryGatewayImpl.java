package com.learning.inventory_manager.gateway;

import com.learning.inventory_manager.exception.OptimisticLockingException;
import com.learning.inventory_manager.core.domain.Inventory;
import com.learning.inventory_manager.web.mapper.InventoryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
public class InventoryGatewayImpl implements InventoryGateway {
    private static final Logger log = LoggerFactory.getLogger(InventoryGatewayImpl.class);

    private final InventoryRepository repository;
    private final InventoryMapper mapper;
    public InventoryGatewayImpl(InventoryRepository repository, InventoryMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }


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

