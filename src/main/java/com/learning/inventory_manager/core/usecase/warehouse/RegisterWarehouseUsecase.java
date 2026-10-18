package com.learning.inventory_manager.core.usecase.warehouse;

import com.learning.inventory_manager.core.service.warehouse.RegisterWarehouseService;
import com.learning.inventory_manager.core.service.warehouse.RegisterWarehouseServiceRequest;
import com.learning.inventory_manager.core.service.warehouse.RegisterWarehouseServiceResponse;
import com.learning.inventory_manager.core.usecase.Usecase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegisterWarehouseUsecase implements Usecase<RegisterWarehouseUsecaseRequest, RegisterWarehouseUsecaseResponse> {

    private final RegisterWarehouseService registerWarehouseService;

    @Override
    public RegisterWarehouseUsecaseResponse execute(RegisterWarehouseUsecaseRequest request) {
        var serviceResponse = registerWarehouseService.execute(buildServiceRequest(request));
        return buildUsecaseResponse(serviceResponse);
    }

    private RegisterWarehouseServiceRequest buildServiceRequest(RegisterWarehouseUsecaseRequest request) {
        return RegisterWarehouseServiceRequest.builder().warehouse(request.getWarehouse()).build();
    }

    private RegisterWarehouseUsecaseResponse buildUsecaseResponse(RegisterWarehouseServiceResponse serviceResponse) {
        return RegisterWarehouseUsecaseResponse.builder()
                .warehouse(serviceResponse.getWarehouse())
                .created(serviceResponse.isCreated())
                .build();
    }
}
