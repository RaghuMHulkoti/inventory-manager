package com.learning.inventory_manager.core.processor.inventory;

import com.learning.inventory_manager.core.processor.Processor;
import com.learning.inventory_manager.core.usecase.inventory.DeductInventoryUsecase;
import com.learning.inventory_manager.core.usecase.inventory.DeductInventoryUsecaseRequest;
import com.learning.inventory_manager.core.usecase.inventory.DeductInventoryUsecaseResponse;
import lombok.RequiredArgsConstructor;
@RequiredArgsConstructor
public class DeductInventoryProcessor implements Processor<DeductInventoryProcessorRequest, DeductInventoryProcessorResponse> {

    private final DeductInventoryUsecase deductInventoryUsecase;

    @Override
    public DeductInventoryProcessorResponse process(DeductInventoryProcessorRequest request) {
        var usecaseRequest = buildDeductInventoryUsecaseRequest(request);
        var usecaseResponse = deductInventoryUsecase.execute(usecaseRequest);
        return buildDeductInventoryProcessorResponse(usecaseResponse);
    }

    private DeductInventoryUsecaseRequest buildDeductInventoryUsecaseRequest(DeductInventoryProcessorRequest request) {
        return DeductInventoryUsecaseRequest.builder()
                .orderId(request.getOrderId())
                .items(request.getItems())
                .build();
    }

    private DeductInventoryProcessorResponse buildDeductInventoryProcessorResponse(DeductInventoryUsecaseResponse usecaseResponse) {
        return DeductInventoryProcessorResponse.builder()
                .deductedReservationIds(usecaseResponse.getDeductedReservationIds())
                .build();
    }
}
