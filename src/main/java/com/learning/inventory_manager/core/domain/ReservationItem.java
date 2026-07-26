package com.learning.inventory_manager.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Domain command representing a single item to reserve.
 * Converted from the web request model by {@link com.learning.inventory_manager.web.mapper.InventoryWebMapper}
 * at the controller boundary — never crosses back to the web layer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationItem {
    private UUID orderItemId;
    private UUID supplierProductId;
    private UUID warehouseId;
    private Integer quantity;
}
