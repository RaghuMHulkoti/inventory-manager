package com.learning.inventory_manager.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Inventory domain model.
 * Represents inventory state without JPA mapping.
 */
public final class Inventory {
    private final UUID id;
    private final UUID supplierProductId;
    private final UUID warehouseId;
    private Integer quantityOnHand;
    private Integer quantityReserved;
    private Long version;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Inventory(UUID id, UUID supplierProductId, UUID warehouseId, 
                     Integer quantityOnHand, Integer quantityReserved, 
                     Long version, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.supplierProductId = supplierProductId;
        this.warehouseId = warehouseId;
        this.quantityOnHand = quantityOnHand;
        this.quantityReserved = quantityReserved;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Inventory create(UUID supplierProductId, UUID warehouseId, Integer quantityOnHand) {
        return new Inventory(
            UUID.randomUUID(),
            supplierProductId,
            warehouseId,
            quantityOnHand,
            0,
            0L,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    public void setQuantityOnHand(Integer quantity) {
        this.quantityOnHand = quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getAvailableQuantity() {
        return quantityOnHand - quantityReserved;
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getSupplierProductId() { return supplierProductId; }
    public UUID getWarehouseId() { return warehouseId; }
    public Integer getQuantityOnHand() { return quantityOnHand; }
    public Integer getQuantityReserved() { return quantityReserved; }
    public Long getVersion() { return version; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

