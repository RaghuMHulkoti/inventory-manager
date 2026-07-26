package com.learning.inventory_manager.gateway.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Inventory ledger entity — append-only audit trail for every inventory quantity change.
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

    /** Optional link back to the reservation that caused this change. */
    @Column(name = "reservation_id")
    private UUID reservationId;

    /** SUPPLIER, ORDER, MANUAL, RETURN, etc. */
    @Column(name = "reference_type", length = 30)
    private String referenceType;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    /** Service or user that triggered the change. */
    @Column(name = "actor", length = 80)
    private String actor;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
