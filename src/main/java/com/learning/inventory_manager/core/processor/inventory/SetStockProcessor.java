package com.learning.inventory_manager.core.processor.inventory;

import com.learning.inventory_manager.core.processor.Processor;
import com.learning.inventory_manager.core.usecase.inventory.SetStockUsecase;
import com.learning.inventory_manager.core.usecase.inventory.SetStockUsecaseRequest;
import com.learning.inventory_manager.core.usecase.inventory.SetStockUsecaseResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SetStockProcessor implements Processor<SetStockProcessorRequest, SetStockProcessorResponse> {

    private final SetStockUsecase setStockUsecase;

    @Override
    public SetStockProcessorResponse process(SetStockProcessorRequest request) {
        var usecaseRequest = buildSetStockUsecaseRequest(request);
        var usecaseResponse = setStockUsecase.execute(usecaseRequest);
        return buildSetStockProcessorResponse(usecaseResponse);
    }

    private SetStockUsecaseRequest buildSetStockUsecaseRequest(SetStockProcessorRequest request) {
        return SetStockUsecaseRequest.builder()
                .inventory(request.getInventory())
                .build();
    }

    private SetStockProcessorResponse buildSetStockProcessorResponse(SetStockUsecaseResponse usecaseResponse) {
        return SetStockProcessorResponse.builder()
                .inventory(usecaseResponse.getInventory())
                .quantityDelta(usecaseResponse.getQuantityDelta())
                .build();
    }
}
