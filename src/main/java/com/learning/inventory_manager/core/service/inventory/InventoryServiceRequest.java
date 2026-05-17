package com.learning.inventory_manager.core.service.inventory;

import com.learning.inventory_manager.core.service.ServiceRequest;

import java.util.UUID;

public record InventoryServiceRequest(
        UUID supplierProductId,
        UUID warehouseId,
        Integer quantityOnHand,
        UUID supplierId
) implements ServiceRequest {
}
