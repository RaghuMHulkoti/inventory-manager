package com.learning.inventory_manager.core.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Inventory domain object — single source of truth for stock per (supplierProduct, warehouse).
 *
 * Design rule: product descriptions live in Product Service; prices live in Supplier Service.
 * This domain object knows ONLY about quantities and thresholds.
 */
public final class Inventory {

    private final UUID id;
    private final UUID supplierProductId;
    private final UUID warehouseId;
    private Integer quantityOnHand;
    private Integer quantityReserved;
    private Integer safetyStock;
    private Integer reorderPoint;
    private Long version;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastRestockedAt;

    // -------------------------------------------------------------------------
    // Full constructor (all fields)
    // -------------------------------------------------------------------------
    public Inventory(UUID id, UUID supplierProductId, UUID warehouseId,
                     Integer quantityOnHand, Integer quantityReserved,
                     Integer safetyStock, Integer reorderPoint,
                     Long version, LocalDateTime createdAt, LocalDateTime updatedAt,
                     LocalDateTime lastRestockedAt) {
        this.id = id;
        this.supplierProductId = supplierProductId;
        this.warehouseId = warehouseId;
        this.quantityOnHand = quantityOnHand;
        this.quantityReserved = quantityReserved;
        this.safetyStock = safetyStock;
        this.reorderPoint = reorderPoint;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastRestockedAt = lastRestockedAt;
    }

    /**
     * Backward-compatible 8-arg constructor (used by existing tests).
     * Defaults safetyStock = 0, reorderPoint = 0, lastRestockedAt = null.
     */
    public Inventory(UUID id, UUID supplierProductId, UUID warehouseId,
                     Integer quantityOnHand, Integer quantityReserved,
                     Long version, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(id, supplierProductId, warehouseId, quantityOnHand, quantityReserved,
             0, 0, version, createdAt, updatedAt, null);
    }

    // -------------------------------------------------------------------------
    // Domain logic
    // -------------------------------------------------------------------------

    public Integer getAvailableQuantity() {
        return quantityOnHand - quantityReserved;
    }

    public boolean isBelowSafetyStock() {
        return getAvailableQuantity() <= safetyStock;
    }

    public boolean isBelowReorderPoint() {
        return getAvailableQuantity() <= reorderPoint;
    }

    public void setQuantityOnHand(Integer quantity) {
        var now = LocalDateTime.now();
        if (quantity > this.quantityOnHand) {
            this.lastRestockedAt = now;
        }
        this.quantityOnHand = quantity;
        this.updatedAt = now;
    }

    public void setThresholds(Integer safetyStock, Integer reorderPoint) {
        if (safetyStock != null) this.safetyStock = safetyStock;
        if (reorderPoint != null) this.reorderPoint = reorderPoint;
        this.updatedAt = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------
    public UUID getId() { return id; }
    public UUID getSupplierProductId() { return supplierProductId; }
    public UUID getWarehouseId() { return warehouseId; }
    public Integer getQuantityOnHand() { return quantityOnHand; }
    public Integer getQuantityReserved() { return quantityReserved; }
    public Integer getSafetyStock() { return safetyStock; }
    public Integer getReorderPoint() { return reorderPoint; }
    public Long getVersion() { return version; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getLastRestockedAt() { return lastRestockedAt; }
}