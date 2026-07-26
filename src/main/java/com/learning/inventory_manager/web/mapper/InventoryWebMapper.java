package com.learning.inventory_manager.web.mapper;

import com.learning.inventory_manager.core.domain.Inventory;
import com.learning.inventory_manager.web.model.InventoryItemRequest;
/**
 * Web-layer mapper.
 * Converts incoming HTTP request models into domain objects at the controller boundary.
 *
 * Responsibility:
 * - InventoryItemRequest → Inventory domain
 *
 * This is the single point where web models cross into the domain layer.
 * All other layers (processor, use case, service) work exclusively with domain objects.
 */
public class InventoryWebMapper {

    /**
     * Converts a single inventory item request into a domain Inventory object.
     * Uses {@link Inventory#create} to generate a new UUID and set initial state.
     *
     * @param item the incoming web request item
     * @return an Inventory domain object ready for processing
     */
    public Inventory toDomain(InventoryItemRequest item) {
        return new Inventory(
                java.util.UUID.randomUUID(),
                item.getSupplierProductId(),
                item.getWarehouseId(),
                item.getQuantityOnHand(),
                0,
                0,
                0,
                0L,
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now(),
                null
        );
    }

    public com.learning.inventory_manager.core.domain.ReservationItem toDomain(com.learning.inventory_manager.web.model.ReserveItemRequest item) {
        return com.learning.inventory_manager.core.domain.ReservationItem.builder()
                .orderItemId(item.getOrderItemId())
                .supplierProductId(item.getSupplierProductId())
                .warehouseId(item.getWarehouseId())
                .quantity(item.getQuantity())
                .build();
    }

    public com.learning.inventory_manager.core.domain.DeductItem toDomain(com.learning.inventory_manager.web.model.DeductItemRequest item) {
        return com.learning.inventory_manager.core.domain.DeductItem.builder()
                .orderItemId(item.getOrderItemId())
                .reservationId(item.getReservationId())
                .build();
    }
}
