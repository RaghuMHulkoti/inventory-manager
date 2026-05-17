package com.learning.inventory_manager.core.usecase.bulkInventory;

import com.learning.inventory_manager.core.usecase.UsecaseRequest;

import java.util.List;
import java.util.UUID;

public record BulkInventoryUpsertUsecaseRequest(
        UUID supplierId,
        List<Item> items
) implements UsecaseRequest {
    public BulkInventoryUpsertUsecaseRequest {
        items = List.copyOf(items);
    }

    public record Item(
            UUID supplierProductId,
            UUID warehouseId,
            Integer quantityOnHand
    ) {
    }
}
