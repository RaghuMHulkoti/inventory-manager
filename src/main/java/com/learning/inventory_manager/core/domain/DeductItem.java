package com.learning.inventory_manager.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Domain command representing a single item to deduct.
 * Converted from the web request model by {@link com.learning.inventory_manager.web.mapper.InventoryWebMapper}
 * at the controller boundary.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeductItem {
    private UUID orderItemId;
    private UUID reservationId;
}
