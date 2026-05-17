package com.learning.inventory_manager.gateway.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Inventory entity representing stock levels per supplier product and warehouse.
 * Core entity implementing optimistic locking strategy using version column.
 * Enforces business constraints via database CHECK constraints.
 *
 * Responsibility:
 * - Persist inventory state with version tracking
 * - Maintain quantity on hand and reserved quantity
 * - Support optimistic locking for concurrent updates
 * - Enforce constraint: quantity_on_hand >= 0 and quantity_reserved >= 0
 *
 * Optimistic Locking Strategy:
 * - Version column incremented on every update
 * - JPA @Version annotation handles version management
 * - Update statement includes version check: WHERE version = ?
 * - StaleObjectStateException thrown if version mismatch occurs
 */
@Entity
@Table(name = "inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryEntity {

    @Id
    private UUID id;

    @Column(name = "supplier_product_id", nullable = false)
    private UUID supplierProductId;

    @Column(name = "warehouse_id", nullable = false)
    private UUID warehouseId;

    @Column(name = "quantity_on_hand", nullable = false)
    private Integer quantityOnHand;

    @Column(name = "quantity_reserved", nullable = false)
    private Integer quantityReserved;

    /**
     * Version column for optimistic locking.
     * Auto-incremented by JPA on every update.
     * Used to detect concurrent modifications.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Calculates available quantity (on hand - reserved).
     * Used for validation and reporting.
     */
    public Integer getAvailableQuantity() {
        return quantityOnHand - quantityReserved;
    }

    /**
     * Validates that inventory state is valid before persistence.
     * Ensures business rule: available quantity >= 0
     *
     * @throws IllegalStateException if inventory is in invalid state
     */
    public void validateInventoryState() {
        if (quantityOnHand < 0) {
            throw new IllegalStateException("Quantity on hand cannot be negative");
        }
        if (quantityReserved < 0) {
            throw new IllegalStateException("Quantity reserved cannot be negative");
        }
        if (getAvailableQuantity() < 0) {
            throw new IllegalStateException("Available quantity cannot be negative");
        }
    }
}



