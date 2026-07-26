package com.learning.inventory_manager.core.usecase.inventory;

import com.learning.inventory_manager.core.service.inventory.InventoryServiceRequest;
import com.learning.inventory_manager.core.service.inventory.InventoryServiceResponse;
import com.learning.inventory_manager.core.service.inventory.InventoryService;
import lombok.RequiredArgsConstructor;
@RequiredArgsConstructor
public class SetStockUsecase {

    private final InventoryService inventoryService;

    public SetStockUsecaseResponse execute(SetStockUsecaseRequest request) {
        var serviceRequest = buildInventoryServiceRequest(request);
        var serviceResponse = inventoryService.execute(serviceRequest);
        return buildSetStockUsecaseResponse(serviceResponse);
    }

    private InventoryServiceRequest buildInventoryServiceRequest(SetStockUsecaseRequest request) {
        return InventoryServiceRequest.builder()
                .inventory(request.getInventory())
                .build();
    }

    private SetStockUsecaseResponse buildSetStockUsecaseResponse(InventoryServiceResponse serviceResponse) {
        return SetStockUsecaseResponse.builder()
                .inventory(serviceResponse.getInventory())
                .quantityDelta(serviceResponse.getQuantityDelta())
                .build();
    }
}
