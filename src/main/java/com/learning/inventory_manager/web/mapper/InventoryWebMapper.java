package com.learning.inventory_manager.web.mapper;

import com.learning.inventory_manager.core.domain.DeductItem;
import com.learning.inventory_manager.core.domain.Inventory;
import com.learning.inventory_manager.core.domain.ReservationItem;
import com.learning.inventory_manager.web.model.DeductItemRequest;
import com.learning.inventory_manager.web.model.InventoryItemRequest;
import com.learning.inventory_manager.web.model.ReserveItemRequest;

import java.time.LocalDateTime;

/**
 * Web-layer mapper.
 * Converts incoming HTTP request models into domain objects at the controller boundary.
 *
 * This is the single point where web models cross into the domain layer.
 * All other layers (processor, use case, service) work exclusively with domain objects.
 */
public class InventoryWebMapper {

    /**
     * Converts a single inventory item request into a domain Inventory object describing the
     * desired stock level. Id and version stay null: the service decides whether to create or update.
     * Thresholds stay null so an update never overwrites stored thresholds the caller did not send.
     */
    public Inventory toDomain(InventoryItemRequest item) {
        var now = LocalDateTime.now();
        return new Inventory(
                null,
                item.getSupplierProductId(),
                item.getWarehouseId(),
                item.getQuantityOnHand(),
                0,
                null,
                null,
                null,
                now,
                now,
                null
        );
    }

    public ReservationItem toDomain(ReserveItemRequest item) {
        return ReservationItem.builder()
                .orderItemId(item.getOrderItemId())
                .supplierProductId(item.getSupplierProductId())
                .warehouseId(item.getWarehouseId())
                .quantity(item.getQuantity())
                .build();
    }

    public DeductItem toDomain(DeductItemRequest item) {
        return DeductItem.builder()
                .orderItemId(item.getOrderItemId())
                .reservationId(item.getReservationId())
                .build();
    }
}
