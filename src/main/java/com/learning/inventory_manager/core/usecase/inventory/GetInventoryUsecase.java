package com.learning.inventory_manager.core.usecase.inventory;

import com.learning.inventory_manager.core.service.inventory.GetInventoryServiceRequest;
import com.learning.inventory_manager.core.service.inventory.GetInventoryServiceResponse;
import com.learning.inventory_manager.core.service.inventory.GetInventoryService;
import com.learning.inventory_manager.core.usecase.Usecase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetInventoryUsecase implements Usecase<GetInventoryUsecaseRequest, GetInventoryUsecaseResponse> {

    private final GetInventoryService getInventoryService;

    @Override
    public GetInventoryUsecaseResponse execute(GetInventoryUsecaseRequest request) {
        var serviceRequest = buildGetInventoryServiceRequest(request);
        var serviceResponse = getInventoryService.execute(serviceRequest);
        return buildGetInventoryUsecaseResponse(serviceResponse);
    }

    private GetInventoryServiceRequest buildGetInventoryServiceRequest(GetInventoryUsecaseRequest request) {
        return GetInventoryServiceRequest.builder()
                .supplierProductId(request.getSupplierProductId())
                .build();
    }

    private GetInventoryUsecaseResponse buildGetInventoryUsecaseResponse(GetInventoryServiceResponse serviceResponse) {
        return GetInventoryUsecaseResponse.builder()
                .inventories(serviceResponse.getInventories())
                .build();
    }
}
