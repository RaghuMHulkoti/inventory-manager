package com.learning.inventory_manager.core.usecase.inventory;

import com.learning.inventory_manager.core.service.inventory.ReleaseInventoryServiceRequest;
import com.learning.inventory_manager.core.service.inventory.ReleaseInventoryServiceResponse;
import com.learning.inventory_manager.core.service.inventory.ReleaseInventoryService;
import lombok.RequiredArgsConstructor;
@RequiredArgsConstructor
public class ReleaseInventoryUsecase {

    private final ReleaseInventoryService releaseInventoryService;

    public ReleaseInventoryUsecaseResponse execute(ReleaseInventoryUsecaseRequest request) {
        var serviceRequest = buildReleaseInventoryServiceRequest(request);
        var serviceResponse = releaseInventoryService.execute(serviceRequest);
        return buildReleaseInventoryUsecaseResponse(serviceResponse);
    }

    private ReleaseInventoryServiceRequest buildReleaseInventoryServiceRequest(ReleaseInventoryUsecaseRequest request) {
        return ReleaseInventoryServiceRequest.builder()
                .reservationId(request.getReservationId())
                .idempotencyKey(request.getIdempotencyKey())
                .reason(request.getReason())
                .build();
    }

    private ReleaseInventoryUsecaseResponse buildReleaseInventoryUsecaseResponse(ReleaseInventoryServiceResponse serviceResponse) {
        return ReleaseInventoryUsecaseResponse.builder()
                .reservationId(serviceResponse.getReservationId())
                .success(serviceResponse.isSuccess())
                .build();
    }
}
