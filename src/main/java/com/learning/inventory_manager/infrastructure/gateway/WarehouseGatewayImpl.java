package com.learning.inventory_manager.infrastructure.gateway;

import com.learning.inventory_manager.domain.exception.WarehouseNotFoundException;
import com.learning.inventory_manager.domain.gateway.WarehouseGateway;
import com.learning.inventory_manager.infrastructure.persistence.repository.WarehouseRepository;
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
@RequiredArgsConstructor
public class WarehouseGatewayImpl implements WarehouseGateway {
    private static final Logger log = LoggerFactory.getLogger(WarehouseGatewayImpl.class);
    private final WarehouseRepository repository;

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

