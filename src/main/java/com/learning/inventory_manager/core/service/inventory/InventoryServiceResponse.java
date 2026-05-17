package com.learning.inventory_manager.core.service.inventory;

import com.learning.inventory_manager.core.domain.Inventory;
import com.learning.inventory_manager.core.service.ServiceResponse;

public record InventoryServiceResponse(
        Inventory inventory,
        Operation operation,
        Integer quantityDelta
) implements ServiceResponse {
    public enum Operation {
        CREATE,
        UPDATE
    }
}
