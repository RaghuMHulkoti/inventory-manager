package com.learning.inventory_manager.core.processor.BulkInventory;

import com.learning.inventory_manager.core.processor.ProcessorRequest;

import java.util.List;
import java.util.UUID;

public record BulkInventoryUpsertProcessorRequest(
        UUID supplierId,
        List<Item> items
) implements ProcessorRequest {
    public BulkInventoryUpsertProcessorRequest {
        items = List.copyOf(items);
    }

    public record Item(
            UUID supplierProductId,
            UUID warehouseId,
            Integer quantityOnHand
    ) {
    }
}
