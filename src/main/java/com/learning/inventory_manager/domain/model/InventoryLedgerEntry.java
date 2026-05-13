package com.learning.inventory_manager.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Inventory Ledger domain model.
 */
public final class InventoryLedgerEntry {
    private final UUID inventoryId;
    private final String changeType;
    private final Integer quantityDelta;
    private final Integer quantityAfter;
    private final String referenceId;
    private final String note;

    public InventoryLedgerEntry(UUID inventoryId, String changeType, Integer quantityDelta,
                                Integer quantityAfter, String referenceId, String note) {
        this.inventoryId = inventoryId;
        this.changeType = changeType;
        this.quantityDelta = quantityDelta;
        this.quantityAfter = quantityAfter;
        this.referenceId = referenceId;
        this.note = note;
    }

    public static InventoryLedgerEntry supplierPush(UUID inventoryId, Integer quantityDelta, 
                                                      Integer quantityAfter, UUID supplierId) {
        return new InventoryLedgerEntry(
            inventoryId,
            "SUPPLIER_PUSH",
            quantityDelta,
            quantityAfter,
            supplierId.toString(),
            "Inventory push from supplier"
        );
    }

    // Getters
    public UUID getInventoryId() { return inventoryId; }
    public String getChangeType() { return changeType; }
    public Integer getQuantityDelta() { return quantityDelta; }
    public Integer getQuantityAfter() { return quantityAfter; }
    public String getReferenceId() { return referenceId; }
    public String getNote() { return note; }
}

