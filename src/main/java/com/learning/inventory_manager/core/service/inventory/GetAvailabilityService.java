package com.learning.inventory_manager.core.service.inventory;

import com.learning.inventory_manager.core.domain.Inventory;
import com.learning.inventory_manager.core.service.Service;
import com.learning.inventory_manager.gateway.InventoryGateway;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GetAvailabilityService implements Service<GetAvailabilityServiceRequest, GetAvailabilityServiceResponse> {

    private final InventoryGateway inventoryGateway;

    public GetAvailabilityService(InventoryGateway inventoryGateway) {
        this.inventoryGateway = inventoryGateway;
    }

    @Override
    public GetAvailabilityServiceResponse execute(GetAvailabilityServiceRequest request) {
        int totalAvailable = 0;
        Map<UUID, Map<UUID, Integer>> breakdown = new HashMap<>();

        for (UUID supplierProductId : request.getSupplierProductIds()) {
            List<Inventory> inventories = inventoryGateway.findBySupplierProductId(supplierProductId);

            Map<UUID, Integer> warehouseAvailability = new HashMap<>();

            for (Inventory inv : inventories) {
                int available = inv.getAvailableQuantity();
                if (available > 0) {
                    warehouseAvailability.put(inv.getWarehouseId(), available);
                    totalAvailable += available;
                }
            }
            if (!warehouseAvailability.isEmpty()) {
                breakdown.put(supplierProductId, warehouseAvailability);
            }
        }

        return GetAvailabilityServiceResponse.builder()
                .productId(request.getProductId())
                .totalAvailable(totalAvailable)
                .breakdown(breakdown)
                .build();
    }
}
