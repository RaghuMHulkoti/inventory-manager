package com.learning.inventory_manager.web.response;

import java.util.UUID;

public record InventoryUpsertItemResult(
        UUID supplierProductId,
        UUID warehouseId,
        String status,
        String operation,
        String errorMessage,
        Integer quantityDelta
) {
}
