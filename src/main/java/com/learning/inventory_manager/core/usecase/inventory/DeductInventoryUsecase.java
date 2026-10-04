package com.learning.inventory_manager.core.usecase.inventory;

import com.learning.inventory_manager.core.service.inventory.DeductInventoryServiceRequest;
import com.learning.inventory_manager.core.service.inventory.DeductInventoryServiceResponse;
import com.learning.inventory_manager.core.service.inventory.DeductInventoryService;
import com.learning.inventory_manager.core.usecase.Usecase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeductInventoryUsecase implements Usecase<DeductInventoryUsecaseRequest, DeductInventoryUsecaseResponse> {

    private final DeductInventoryService deductInventoryService;

    @Override
    public DeductInventoryUsecaseResponse execute(DeductInventoryUsecaseRequest request) {
        var serviceRequest = buildDeductInventoryServiceRequest(request);
        var serviceResponse = deductInventoryService.execute(serviceRequest);
        return buildDeductInventoryUsecaseResponse(serviceResponse);
    }

    private DeductInventoryServiceRequest buildDeductInventoryServiceRequest(DeductInventoryUsecaseRequest request) {
        return DeductInventoryServiceRequest.builder()
                .orderId(request.getOrderId())
                .items(request.getItems())
                .build();
    }

    private DeductInventoryUsecaseResponse buildDeductInventoryUsecaseResponse(DeductInventoryServiceResponse serviceResponse) {
        return DeductInventoryUsecaseResponse.builder()
                .deductedReservationIds(serviceResponse.getDeductedReservationIds())
                .build();
    }
}
