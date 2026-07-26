package com.learning.inventory_manager.gateway;

import com.learning.inventory_manager.core.domain.Inventory;
import com.learning.inventory_manager.core.domain.InventoryLedgerEntry;
import com.learning.inventory_manager.core.domain.StockReservation;
import com.learning.inventory_manager.gateway.entity.InventoryEntity;
import com.learning.inventory_manager.gateway.entity.InventoryLedgerEntity;
import com.learning.inventory_manager.gateway.entity.StockReservationEntity;
/**
 * Persistence-layer mapper — the ONLY place where domain objects and JPA entities cross each other.
 *
 * Responsibilities:
 * - Inventory  (domain) ↔ InventoryEntity  (JPA)
 * - StockReservation (domain) ↔ StockReservationEntity (JPA)
 * - InventoryLedgerEntry (domain) → InventoryLedgerEntity (JPA, write-only)
 */
public class InventoryEntityMapper {

    // =========================================================================
    // Inventory
    // =========================================================================

    public Inventory toDomain(InventoryEntity entity) {
        if (entity == null) return null;
        return new Inventory(
                entity.getId(),
                entity.getSupplierProductId(),
                entity.getWarehouseId(),
                entity.getQuantityOnHand(),
                entity.getQuantityReserved(),
                entity.getSafetyStock() != null ? entity.getSafetyStock() : 0,
                entity.getReorderPoint() != null ? entity.getReorderPoint() : 0,
                entity.getVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getLastRestockedAt()
        );
    }

    public InventoryEntity toEntity(Inventory domain) {
        if (domain == null) return null;
        return InventoryEntity.builder()
                .id(domain.getId())
                .supplierProductId(domain.getSupplierProductId())
                .warehouseId(domain.getWarehouseId())
                .quantityOnHand(domain.getQuantityOnHand())
                .quantityReserved(domain.getQuantityReserved())
                .safetyStock(domain.getSafetyStock() != null ? domain.getSafetyStock() : 0)
                .reorderPoint(domain.getReorderPoint() != null ? domain.getReorderPoint() : 0)
                .lastRestockedAt(domain.getLastRestockedAt())
                .version(domain.getVersion())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    // =========================================================================
    // StockReservation
    // =========================================================================

    public StockReservation toDomain(StockReservationEntity entity) {
        if (entity == null) return null;
        return StockReservation.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .orderItemId(entity.getOrderItemId())
                .inventoryId(entity.getInventoryId())
                .supplierProductId(entity.getSupplierProductId())
                .warehouseId(entity.getWarehouseId())
                .quantity(entity.getQuantity())
                .status(StockReservation.ReservationStatus.valueOf(entity.getStatus()))
                .expiresAt(entity.getExpiresAt())
                .committedAt(entity.getCommittedAt())
                .releasedAt(entity.getReleasedAt())
                .idempotencyKey(entity.getIdempotencyKey())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public StockReservationEntity toEntity(StockReservation domain) {
        if (domain == null) return null;
        return StockReservationEntity.builder()
                .id(domain.getId())
                .orderId(domain.getOrderId())
                .orderItemId(domain.getOrderItemId())
                .inventoryId(domain.getInventoryId())
                .supplierProductId(domain.getSupplierProductId())
                .warehouseId(domain.getWarehouseId())
                .quantity(domain.getQuantity())
                .status(domain.getStatus().name())
                .expiresAt(domain.getExpiresAt())
                .committedAt(domain.getCommittedAt())
                .releasedAt(domain.getReleasedAt())
                .idempotencyKey(domain.getIdempotencyKey())
                .version(domain.getVersion())
                .build();
    }

    // =========================================================================
    // InventoryLedgerEntry (write-only — no domain read path needed)
    // =========================================================================

    public InventoryLedgerEntity toEntity(InventoryLedgerEntry domain) {
        if (domain == null) return null;
        return InventoryLedgerEntity.builder()
                .inventoryId(domain.getInventoryId())
                .changeType(domain.getChangeType())
                .quantityDelta(domain.getQuantityDelta())
                .quantityAfter(domain.getQuantityAfter())
                .reservationId(domain.getReservationId())
                .referenceType(domain.getReferenceType())
                .referenceId(domain.getReferenceId())
                .actor(domain.getActor())
                .note(domain.getNote())
                .build();
    }
}
