package com.learning.inventory_manager.core.processor.BulkInventory;

import com.learning.inventory_manager.core.domain.Inventory;
import com.learning.inventory_manager.core.processor.ProcessorRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Processor request for the bulk inventory upsert operation.
 *
 * Carries domain {@link Inventory} objects (already converted from the HTTP request at the
 * controller boundary) along with the supplierId.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkInventoryUpsertProcessorRequest implements ProcessorRequest {

    /** The supplier initiating the inventory push. */
    private UUID supplierId;

    /**
     * Domain inventory objects passed from the controller.
     * Each object already has supplierProductId, warehouseId, and quantityOnHand set.
     */
    private List<Inventory> items;
}
