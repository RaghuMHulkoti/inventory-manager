package com.learning.inventory_manager.gateway;

import com.learning.inventory_manager.gateway.entity.InventoryLedgerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Inventory Ledger entity.
 *
 * Responsibility:
 * - Provide append-only access to inventory ledger
 * - Enable audit trail queries and reports
 */
@Repository
public interface InventoryLedgerRepository extends JpaRepository<InventoryLedgerEntity, Long> {
    /**
     * Find all ledger entries for a specific inventory.
     * Used to retrieve complete history of changes for auditing.
     *
     * @param inventoryId the inventory ID
     * @return list of ledger entries in creation order
     */
    List<InventoryLedgerEntity> findByInventoryIdOrderByCreatedAtAsc(UUID inventoryId);
}

