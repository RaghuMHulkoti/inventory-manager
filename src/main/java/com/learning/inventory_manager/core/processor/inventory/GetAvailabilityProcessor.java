package com.learning.inventory_manager.core.processor.inventory;

import com.learning.inventory_manager.core.processor.Processor;
import com.learning.inventory_manager.core.usecase.inventory.GetAvailabilityUsecase;
import com.learning.inventory_manager.core.usecase.inventory.GetAvailabilityUsecaseRequest;
import com.learning.inventory_manager.core.usecase.inventory.GetAvailabilityUsecaseResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetAvailabilityProcessor implements Processor<GetAvailabilityProcessorRequest, GetAvailabilityProcessorResponse> {

    private final GetAvailabilityUsecase getAvailabilityUsecase;

    @Override
    public GetAvailabilityProcessorResponse process(GetAvailabilityProcessorRequest request) {
        var usecaseRequest = buildGetAvailabilityUsecaseRequest(request);
        var usecaseResponse = getAvailabilityUsecase.execute(usecaseRequest);
        return buildGetAvailabilityProcessorResponse(usecaseResponse);
    }

    private GetAvailabilityUsecaseRequest buildGetAvailabilityUsecaseRequest(GetAvailabilityProcessorRequest request) {
        return GetAvailabilityUsecaseRequest.builder()
                .productId(request.getProductId())
                .supplierProductIds(request.getSupplierProductIds())
                .build();
    }

    private GetAvailabilityProcessorResponse buildGetAvailabilityProcessorResponse(GetAvailabilityUsecaseResponse usecaseResponse) {
        return GetAvailabilityProcessorResponse.builder()
                .productId(usecaseResponse.getProductId())
                .totalAvailable(usecaseResponse.getTotalAvailable())
                .breakdown(usecaseResponse.getBreakdown())
                .build();
    }
}
