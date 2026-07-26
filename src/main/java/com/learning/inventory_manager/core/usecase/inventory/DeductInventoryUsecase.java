package com.learning.inventory_manager.core.usecase.inventory;

import com.learning.inventory_manager.core.service.inventory.DeductInventoryServiceRequest;
import com.learning.inventory_manager.core.service.inventory.DeductInventoryServiceResponse;
import com.learning.inventory_manager.core.service.inventory.DeductInventoryService;
import lombok.RequiredArgsConstructor;
@RequiredArgsConstructor
public class DeductInventoryUsecase {

    private final DeductInventoryService deductInventoryService;

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
