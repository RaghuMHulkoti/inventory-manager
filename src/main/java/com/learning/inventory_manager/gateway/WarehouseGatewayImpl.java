package com.learning.inventory_manager.gateway;

import com.learning.inventory_manager.exception.WarehouseNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.UUID;

/**
 * Warehouse Gateway implementation.
 * Gateway adapts repository to domain interface, providing warehouse validation.
 *
 * Responsibility:
 * - Bridge domain and infrastructure layers
 * - Convert repository calls to domain exceptions
 * - Provide warehouse validation logic
 */
@Component
public class WarehouseGatewayImpl implements WarehouseGateway {
    private static final Logger log = LoggerFactory.getLogger(WarehouseGatewayImpl.class);
    private final WarehouseRepository repository;

    public WarehouseGatewayImpl(WarehouseRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validateWarehouseExists(UUID warehouseId) {
        log.debug("Validating warehouse exists: {}", warehouseId);
        
        if (!repository.existsByIdAndIsActiveTrue(warehouseId)) {
            log.warn("Warehouse not found or inactive: {}", warehouseId);
            throw new WarehouseNotFoundException(warehouseId);
        }
        
        log.debug("Warehouse validation successful: {}", warehouseId);
    }
}

