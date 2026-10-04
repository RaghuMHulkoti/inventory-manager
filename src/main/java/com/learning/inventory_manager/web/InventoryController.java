package com.learning.inventory_manager.web;

import com.learning.inventory_manager.common.StatusCode;
import com.learning.inventory_manager.core.processor.bulkinventory.BulkInventoryUpsertProcessor;
import com.learning.inventory_manager.core.processor.bulkinventory.BulkInventoryUpsertProcessorRequest;
import com.learning.inventory_manager.core.processor.inventory.*;
import com.learning.inventory_manager.web.mapper.InventoryResponseMapper;
import com.learning.inventory_manager.web.mapper.InventoryWebMapper;
import com.learning.inventory_manager.web.model.*;
import com.learning.inventory_manager.web.response.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Inventory REST Controller.
 */
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private static final Logger log = LoggerFactory.getLogger(InventoryController.class);

    private final BulkInventoryUpsertProcessor bulkUpsertProcessor;
    private final GetInventoryProcessor getInventoryProcessor;
    private final ReserveInventoryProcessor reserveInventoryProcessor;
    private final ReleaseInventoryProcessor releaseInventoryProcessor;
    private final DeductInventoryProcessor deductInventoryProcessor;
    private final GetAvailabilityProcessor getAvailabilityProcessor;
    private final InventoryWebMapper webMapper;

    @PostMapping("/bulk-upsert")
    public ResponseEntity<Response<BulkInventoryUpsertResponse>> bulkUpsert(
            @Valid @RequestBody BulkInventoryUpsertRequest request
    ) {
        log.info("Received bulk inventory upsert request from supplier: {}", request.getSupplierId());

        var result = bulkUpsertProcessor.process(buildBulkInventoryUpsertProcessorRequest(request));
        return ok("Bulk upsert processed successfully", InventoryResponseMapper.mapBulkUpsertResponse(result));
    }

    private BulkInventoryUpsertProcessorRequest buildBulkInventoryUpsertProcessorRequest(BulkInventoryUpsertRequest request) {
        var domainItems = request.getItems().stream()
                .map(webMapper::toDomain)
                .toList();
        return BulkInventoryUpsertProcessorRequest.builder()
                .supplierId(request.getSupplierId())
                .items(domainItems)
                .build();
    }

    @GetMapping("/{supplierProductId}")
    public ResponseEntity<Response<GetInventoryResponse>> getInventory(
            @PathVariable UUID supplierProductId
    ) {
        var result = getInventoryProcessor.process(buildGetInventoryProcessorRequest(supplierProductId));
        return ok("Inventory retrieved successfully", InventoryResponseMapper.mapGetInventoryResponse(result));
    }

    private GetInventoryProcessorRequest buildGetInventoryProcessorRequest(UUID supplierProductId) {
        return GetInventoryProcessorRequest.builder()
                .supplierProductId(supplierProductId)
                .build();
    }

    @PostMapping("/reserve")
    public ResponseEntity<Response<ReserveBatchResponse>> reserve(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody ReserveRequest request
    ) {
        var result = reserveInventoryProcessor.process(buildReserveInventoryProcessorRequest(request, idempotencyKey));
        return ok("Reservations processed", InventoryResponseMapper.mapReserveBatchResponse(result));
    }

    private ReserveInventoryProcessorRequest buildReserveInventoryProcessorRequest(ReserveRequest request, String idempotencyKey) {
        String idempotencyPrefix = idempotencyKey != null ? idempotencyKey : request.getOrderId().toString();
        var domainItems = request.getItems().stream()
                .map(webMapper::toDomain)
                .toList();
        return ReserveInventoryProcessorRequest.builder()
                .orderId(request.getOrderId())
                .items(domainItems)
                .ttlSeconds(request.getTtlSeconds())
                .idempotencyPrefix(idempotencyPrefix)
                .build();
    }

    @PostMapping("/release")
    public ResponseEntity<Response<ReleaseResponse>> release(
            @Valid @RequestBody ReleaseRequest request
    ) {
        var result = releaseInventoryProcessor.process(buildReleaseInventoryProcessorRequest(request));
        return ok("Release processed", InventoryResponseMapper.mapReleaseResponse(result));
    }

    private ReleaseInventoryProcessorRequest buildReleaseInventoryProcessorRequest(ReleaseRequest request) {
        return ReleaseInventoryProcessorRequest.builder()
                .reservationId(request.getReservationId())
                .idempotencyKey(request.getIdempotencyKey())
                .reason(request.getReason())
                .build();
    }

    @PostMapping("/deduct")
    public ResponseEntity<Response<DeductResponse>> deduct(
            @Valid @RequestBody DeductRequest request
    ) {
        var result = deductInventoryProcessor.process(buildDeductInventoryProcessorRequest(request));
        return ok("Deduction processed", InventoryResponseMapper.mapDeductResponse(result));
    }

    private DeductInventoryProcessorRequest buildDeductInventoryProcessorRequest(DeductRequest request) {
        var domainItems = request.getItems().stream()
                .map(webMapper::toDomain)
                .toList();
        return DeductInventoryProcessorRequest.builder()
                .orderId(request.getOrderId())
                .items(domainItems)
                .build();
    }

    @GetMapping("/availability")
    public ResponseEntity<Response<AvailabilityResponse>> getAvailability(
            @RequestParam UUID productId,
            @RequestParam List<UUID> supplierProductIds
    ) {
        var result = getAvailabilityProcessor.process(buildGetAvailabilityProcessorRequest(productId, supplierProductIds));
        return ok("Availability retrieved successfully", InventoryResponseMapper.mapAvailabilityResponse(result));
    }

    private GetAvailabilityProcessorRequest buildGetAvailabilityProcessorRequest(UUID productId, List<UUID> supplierProductIds) {
        return GetAvailabilityProcessorRequest.builder()
                .productId(productId)
                .supplierProductIds(supplierProductIds)
                .build();
    }

    private static <T> ResponseEntity<Response<T>> ok(String message, T body) {
        return ResponseEntity.ok(new Response<>(StatusCode.SUCCESS, message, body));
    }
}
