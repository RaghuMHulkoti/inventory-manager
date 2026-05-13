package com.learning.inventory_manager.infrastructure.gateway;

import com.learning.inventory_manager.domain.gateway.InventoryLedgerGateway;
import com.learning.inventory_manager.domain.model.InventoryLedgerEntry;
import com.learning.inventory_manager.infrastructure.persistence.repository.InventoryLedgerRepository;
import com.learning.inventory_manager.infrastructure.persistence.entity.InventoryLedgerEntity;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

/**
 * Inventory Ledger Gateway implementation.
 * Appends immutable ledger entries for inventory change tracking.
 *
 * Responsibility:
 * - Convert domain ledger entries to entities
 * - Persist immutable audit trail
 * - Handle ledger-specific logic
 */
@Component
@RequiredArgsConstructor
public class InventoryLedgerGatewayImpl implements InventoryLedgerGateway {
    private static final Logger log = LoggerFactory.getLogger(InventoryLedgerGatewayImpl.class);
    private final InventoryLedgerRepository repository;

    @Override
    public void recordChange(InventoryLedgerEntry entry) {
        log.debug("Recording inventory change for inventory: {}, type: {}, delta: {}", 
            entry.getInventoryId(), entry.getChangeType(), entry.getQuantityDelta());
        
        var entity = InventoryLedgerEntity.builder()
            .inventoryId(entry.getInventoryId())
            .changeType(entry.getChangeType())
            .quantityDelta(entry.getQuantityDelta())
            .quantityAfter(entry.getQuantityAfter())
            .referenceId(entry.getReferenceId())
            .note(entry.getNote())
            .createdAt(LocalDateTime.now())
            .build();
        
        repository.save(entity);
        log.debug("Ledger entry recorded successfully");
    }
}

