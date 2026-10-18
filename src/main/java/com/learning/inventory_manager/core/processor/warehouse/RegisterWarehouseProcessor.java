package com.learning.inventory_manager.core.processor.warehouse;

import com.learning.inventory_manager.core.processor.Processor;
import com.learning.inventory_manager.core.usecase.warehouse.RegisterWarehouseUsecase;
import com.learning.inventory_manager.core.usecase.warehouse.RegisterWarehouseUsecaseRequest;
import com.learning.inventory_manager.core.usecase.warehouse.RegisterWarehouseUsecaseResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegisterWarehouseProcessor implements Processor<RegisterWarehouseProcessorRequest, RegisterWarehouseProcessorResponse> {

    private final RegisterWarehouseUsecase registerWarehouseUsecase;

    @Override
    public RegisterWarehouseProcessorResponse process(RegisterWarehouseProcessorRequest request) {
        var usecaseResponse = registerWarehouseUsecase.execute(buildUsecaseRequest(request));
        return buildProcessorResponse(usecaseResponse);
    }

    private RegisterWarehouseUsecaseRequest buildUsecaseRequest(RegisterWarehouseProcessorRequest request) {
        return RegisterWarehouseUsecaseRequest.builder().warehouse(request.getWarehouse()).build();
    }

    private RegisterWarehouseProcessorResponse buildProcessorResponse(RegisterWarehouseUsecaseResponse usecaseResponse) {
        return RegisterWarehouseProcessorResponse.builder()
                .warehouse(usecaseResponse.getWarehouse())
                .created(usecaseResponse.isCreated())
                .build();
    }
}
