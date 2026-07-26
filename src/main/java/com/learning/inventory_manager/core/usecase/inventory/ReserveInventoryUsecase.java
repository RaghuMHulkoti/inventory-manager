package com.learning.inventory_manager.core.usecase.inventory;

import com.learning.inventory_manager.core.service.inventory.ReserveInventoryServiceRequest;
import com.learning.inventory_manager.core.service.inventory.ReserveInventoryServiceResponse;
import com.learning.inventory_manager.core.service.inventory.ReserveInventoryService;
import lombok.RequiredArgsConstructor;
@RequiredArgsConstructor
public class ReserveInventoryUsecase {

    private final ReserveInventoryService reserveInventoryService;

    public ReserveInventoryUsecaseResponse execute(ReserveInventoryUsecaseRequest request) {
        var serviceRequest = buildReserveInventoryServiceRequest(request);
        var serviceResponse = reserveInventoryService.execute(serviceRequest);
        return buildReserveInventoryUsecaseResponse(serviceResponse);
    }

    private ReserveInventoryServiceRequest buildReserveInventoryServiceRequest(ReserveInventoryUsecaseRequest request) {
        return ReserveInventoryServiceRequest.builder()
                .orderId(request.getOrderId())
                .idempotencyPrefix(request.getIdempotencyPrefix())
                .ttlSeconds(request.getTtlSeconds())
                .items(request.getItems())
                .build();
    }

    private ReserveInventoryUsecaseResponse buildReserveInventoryUsecaseResponse(ReserveInventoryServiceResponse serviceResponse) {
        return ReserveInventoryUsecaseResponse.builder()
                .reservations(serviceResponse.getReservations())
                .build();
    }
}
