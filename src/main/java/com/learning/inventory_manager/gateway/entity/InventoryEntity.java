package com.learning.inventory_manager.gateway.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Inventory entity — stock levels per (supplierProduct, warehouse).
 * Implements optimistic locking via {@code @Version}.
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

    @Column(name = "safety_stock", nullable = false)
    private Integer safetyStock;

    @Column(name = "reorder_point", nullable = false)
    private Integer reorderPoint;

    @Column(name = "last_restocked_at")
    private LocalDateTime lastRestockedAt;

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

    public Integer getAvailableQuantity() {
        return quantityOnHand - quantityReserved;
    }
}
