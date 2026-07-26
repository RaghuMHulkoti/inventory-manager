package com.learning.inventory_manager.core.processor.inventory;

import com.learning.inventory_manager.core.processor.Processor;
import com.learning.inventory_manager.core.usecase.inventory.ReserveInventoryUsecase;
import com.learning.inventory_manager.core.usecase.inventory.ReserveInventoryUsecaseRequest;
import com.learning.inventory_manager.core.usecase.inventory.ReserveInventoryUsecaseResponse;
import lombok.RequiredArgsConstructor;
@RequiredArgsConstructor
public class ReserveInventoryProcessor implements Processor<ReserveInventoryProcessorRequest, ReserveInventoryProcessorResponse> {

    private final ReserveInventoryUsecase reserveInventoryUsecase;

    @Override
    public ReserveInventoryProcessorResponse process(ReserveInventoryProcessorRequest request) {
        var usecaseRequest = buildReserveInventoryUsecaseRequest(request);
        var usecaseResponse = reserveInventoryUsecase.execute(usecaseRequest);
        return buildReserveInventoryProcessorResponse(usecaseResponse);
    }

    private ReserveInventoryUsecaseRequest buildReserveInventoryUsecaseRequest(ReserveInventoryProcessorRequest request) {
        return ReserveInventoryUsecaseRequest.builder()
                .orderId(request.getOrderId())
                .idempotencyPrefix(request.getIdempotencyPrefix())
                .ttlSeconds(request.getTtlSeconds())
                .items(request.getItems())
                .build();
    }

    private ReserveInventoryProcessorResponse buildReserveInventoryProcessorResponse(ReserveInventoryUsecaseResponse usecaseResponse) {
        return ReserveInventoryProcessorResponse.builder()
                .reservations(usecaseResponse.getReservations())
                .build();
    }
}
