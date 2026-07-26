package com.learning.inventory_manager.core.usecase.inventory;

import com.learning.inventory_manager.core.service.inventory.GetAvailabilityServiceRequest;
import com.learning.inventory_manager.core.service.inventory.GetAvailabilityServiceResponse;
import com.learning.inventory_manager.core.service.inventory.GetAvailabilityService;
import lombok.RequiredArgsConstructor;
@RequiredArgsConstructor
public class GetAvailabilityUsecase {

    private final GetAvailabilityService getAvailabilityService;

    public GetAvailabilityUsecaseResponse execute(GetAvailabilityUsecaseRequest request) {
        var serviceRequest = buildGetAvailabilityServiceRequest(request);
        var serviceResponse = getAvailabilityService.execute(serviceRequest);
        return buildGetAvailabilityUsecaseResponse(serviceResponse);
    }

    private GetAvailabilityServiceRequest buildGetAvailabilityServiceRequest(GetAvailabilityUsecaseRequest request) {
        return GetAvailabilityServiceRequest.builder()
                .productId(request.getProductId())
                .supplierProductIds(request.getSupplierProductIds())
                .build();
    }

    private GetAvailabilityUsecaseResponse buildGetAvailabilityUsecaseResponse(GetAvailabilityServiceResponse serviceResponse) {
        return GetAvailabilityUsecaseResponse.builder()
                .productId(serviceResponse.getProductId())
                .totalAvailable(serviceResponse.getTotalAvailable())
                .breakdown(serviceResponse.getBreakdown())
                .build();
    }
}
