package com.learning.inventory_manager.core.processor.inventory;

import com.learning.inventory_manager.core.processor.Processor;
import com.learning.inventory_manager.core.usecase.inventory.GetInventoryUsecase;
import com.learning.inventory_manager.core.usecase.inventory.GetInventoryUsecaseRequest;
import com.learning.inventory_manager.core.usecase.inventory.GetInventoryUsecaseResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetInventoryProcessor implements Processor<GetInventoryProcessorRequest, GetInventoryProcessorResponse> {

    private final GetInventoryUsecase getInventoryUsecase;

    @Override
    public GetInventoryProcessorResponse process(GetInventoryProcessorRequest request) {
        var usecaseRequest = buildGetInventoryUsecaseRequest(request);
        var usecaseResponse = getInventoryUsecase.execute(usecaseRequest);
        return buildGetInventoryProcessorResponse(usecaseResponse);
    }

    private GetInventoryUsecaseRequest buildGetInventoryUsecaseRequest(GetInventoryProcessorRequest request) {
        return GetInventoryUsecaseRequest.builder()
                .supplierProductId(request.getSupplierProductId())
                .build();
    }

    private GetInventoryProcessorResponse buildGetInventoryProcessorResponse(GetInventoryUsecaseResponse usecaseResponse) {
        return GetInventoryProcessorResponse.builder()
                .inventories(usecaseResponse.getInventories())
                .build();
    }
}
