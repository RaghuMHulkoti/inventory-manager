package com.learning.inventory_manager.core.service.inventory;

import com.learning.inventory_manager.core.service.Service;
import com.learning.inventory_manager.gateway.InventoryGateway;

public class GetInventoryService implements Service<GetInventoryServiceRequest, GetInventoryServiceResponse> {

    private final InventoryGateway inventoryGateway;

    public GetInventoryService(InventoryGateway inventoryGateway) {
        this.inventoryGateway = inventoryGateway;
    }

    @Override
    public GetInventoryServiceResponse execute(GetInventoryServiceRequest request) {
        var inventories = inventoryGateway.findBySupplierProductId(request.getSupplierProductId());

        return GetInventoryServiceResponse.builder()
                .inventories(inventories)
                .build();
    }
}
