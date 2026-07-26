package com.learning.inventory_manager.core.domain;

import lombok.Builder;

import java.util.UUID;

/**
 * Append-only ledger entry recording every inventory quantity change.
 *
 * Change types (TDD §9.4):
 *   RESTOCK, RESERVE, RELEASE, DEDUCT, ADJUSTMENT_PLUS, ADJUSTMENT_MINUS, RETURN, SUPPLIER_PUSH
 */
@Builder
public final class InventoryLedgerEntry {

    private final UUID inventoryId;
    private final String changeType;
    private final Integer quantityDelta;
    private final Integer quantityAfter;
    private final UUID reservationId;
    private final String referenceType;
    private final String referenceId;
    private final String actor;
    private final String note;

    public InventoryLedgerEntry(UUID inventoryId, String changeType, Integer quantityDelta,
                                Integer quantityAfter, UUID reservationId, String referenceType,
                                String referenceId, String actor, String note) {
        this.inventoryId = inventoryId;
        this.changeType = changeType;
        this.quantityDelta = quantityDelta;
        this.quantityAfter = quantityAfter;
        this.reservationId = reservationId;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.actor = actor;
        this.note = note;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------
    public UUID getInventoryId() { return inventoryId; }
    public String getChangeType() { return changeType; }
    public Integer getQuantityDelta() { return quantityDelta; }
    public Integer getQuantityAfter() { return quantityAfter; }
    public UUID getReservationId() { return reservationId; }
    public String getReferenceType() { return referenceType; }
    public String getReferenceId() { return referenceId; }
    public String getActor() { return actor; }
    public String getNote() { return note; }
}
