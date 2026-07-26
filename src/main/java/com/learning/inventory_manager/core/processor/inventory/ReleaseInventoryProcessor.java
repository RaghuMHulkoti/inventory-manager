package com.learning.inventory_manager.core.processor.inventory;

import com.learning.inventory_manager.core.processor.Processor;
import com.learning.inventory_manager.core.usecase.inventory.ReleaseInventoryUsecase;
import com.learning.inventory_manager.core.usecase.inventory.ReleaseInventoryUsecaseRequest;
import com.learning.inventory_manager.core.usecase.inventory.ReleaseInventoryUsecaseResponse;
import lombok.RequiredArgsConstructor;
@RequiredArgsConstructor
public class ReleaseInventoryProcessor implements Processor<ReleaseInventoryProcessorRequest, ReleaseInventoryProcessorResponse> {

    private final ReleaseInventoryUsecase releaseInventoryUsecase;

    @Override
    public ReleaseInventoryProcessorResponse process(ReleaseInventoryProcessorRequest request) {
        var usecaseRequest = buildReleaseInventoryUsecaseRequest(request);
        var usecaseResponse = releaseInventoryUsecase.execute(usecaseRequest);
        return buildReleaseInventoryProcessorResponse(usecaseResponse);
    }

    private ReleaseInventoryUsecaseRequest buildReleaseInventoryUsecaseRequest(ReleaseInventoryProcessorRequest request) {
        return ReleaseInventoryUsecaseRequest.builder()
                .reservationId(request.getReservationId())
                .idempotencyKey(request.getIdempotencyKey())
                .reason(request.getReason())
                .build();
    }

    private ReleaseInventoryProcessorResponse buildReleaseInventoryProcessorResponse(ReleaseInventoryUsecaseResponse usecaseResponse) {
        return ReleaseInventoryProcessorResponse.builder()
                .reservationId(usecaseResponse.getReservationId())
                .success(usecaseResponse.isSuccess())
                .build();
    }
}
