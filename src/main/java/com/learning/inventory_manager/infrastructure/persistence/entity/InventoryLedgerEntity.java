package com.learning.inventory_manager.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Inventory Ledger entity providing audit trail for all inventory changes.
 * Immutable record of every inventory mutation for traceability and reconciliation.
 *
 * Responsibility:
 * - Log all inventory mutations
 * - Track change type, quantity delta, and resulting quantity
 * - Maintain reference to source (order, supplier push, etc.)
 * - Enable inventory reconciliation and auditing
 *
 * Change Types:
 * - SUPPLIER_PUSH: Stock received from supplier
 * - ORDER_RESERVATION: Stock reserved for order
 * - ORDER_RELEASE: Stock released back to available
 * - ADJUSTMENT: Manual inventory adjustment
 */
@Entity
@Table(name = "inventory_ledger")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryLedgerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inventory_id", nullable = false)
    private UUID inventoryId;

    @Column(name = "change_type", nullable = false, length = 50)
    private String changeType;

    @Column(name = "quantity_delta", nullable = false)
    private Integer quantityDelta;

    @Column(name = "quantity_after", nullable = false)
    private Integer quantityAfter;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

