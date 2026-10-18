package com.learning.inventory_manager.web;

import com.learning.inventory_manager.common.StatusCode;
import com.learning.inventory_manager.core.domain.Warehouse;
import com.learning.inventory_manager.core.processor.warehouse.RegisterWarehouseProcessor;
import com.learning.inventory_manager.core.processor.warehouse.RegisterWarehouseProcessorRequest;
import com.learning.inventory_manager.web.model.RegisterWarehouseRequest;
import com.learning.inventory_manager.web.response.Response;
import com.learning.inventory_manager.web.response.WarehouseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Registers a supplier's warehouse with Inventory, which validates every stock write against it. Idempotent, so
 * this call can be replaced by a consumer of Supplier's warehouse events without changing what it does.
 */
@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final RegisterWarehouseProcessor registerWarehouseProcessor;

    @PostMapping
    public ResponseEntity<Response<WarehouseResponse>> register(@Valid @RequestBody RegisterWarehouseRequest request) {
        var result = registerWarehouseProcessor.process(RegisterWarehouseProcessorRequest.builder()
                .warehouse(new Warehouse(request.getWarehouseId(), request.getSupplierId(), request.getCode(),
                        request.getName(), request.getCity(), request.getState(), request.getCountry(),
                        request.getActive() == null || request.getActive()))
                .build());
        var w = result.getWarehouse();
        var body = WarehouseResponse.builder().warehouseId(w.id()).supplierId(w.supplierId()).code(w.code())
                .name(w.name()).city(w.city()).state(w.state()).country(w.country()).active(w.active()).build();
        return ResponseEntity.status(result.isCreated() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(new Response<>(StatusCode.SUCCESS,
                        result.isCreated() ? "Warehouse registered" : "Warehouse updated", body));
    }
}
