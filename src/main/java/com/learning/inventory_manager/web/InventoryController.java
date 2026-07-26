package com.learning.inventory_manager.web;

import com.learning.inventory_manager.common.StatusCode;
import com.learning.inventory_manager.core.processor.BulkInventory.BulkInventoryUpsertProcessor;
import com.learning.inventory_manager.core.processor.BulkInventory.BulkInventoryUpsertProcessorRequest;
import com.learning.inventory_manager.core.processor.inventory.*;
import com.learning.inventory_manager.exception.InventoryException;
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
import java.util.Optional;
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

        return Optional.of(request)
                .map(this::buildBulkInventoryUpsertProcessorRequest)
                .map(bulkUpsertProcessor::process)
                .map(InventoryResponseMapper::mapBulkUpsertResponse)
                .map(response -> new Response<>(StatusCode.SUCCESS, "Bulk upsert processed successfully", response))
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new InventoryException("Unable to process bulk upsert request"));
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
        return Optional.of(supplierProductId)
                .map(this::buildGetInventoryProcessorRequest)
                .map(getInventoryProcessor::process)
                .map(InventoryResponseMapper::mapGetInventoryResponse)
                .map(response -> new Response<>(StatusCode.SUCCESS, "Inventory retrieved successfully", response))
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new InventoryException("Unable to retrieve inventory"));
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
        return Optional.of(request)
                .map(req -> buildReserveInventoryProcessorRequest(req, idempotencyKey))
                .map(reserveInventoryProcessor::process)
                .map(InventoryResponseMapper::mapReserveBatchResponse)
                .map(response -> new Response<>(StatusCode.SUCCESS, "Reservations processed", response))
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new InventoryException("Unable to process reservations"));
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
            @RequestBody ReleaseRequest request
    ) {
        return Optional.of(request)
                .map(this::buildReleaseInventoryProcessorRequest)
                .map(releaseInventoryProcessor::process)
                .map(InventoryResponseMapper::mapReleaseResponse)
                .map(response -> new Response<>(StatusCode.SUCCESS, "Release processed", response))
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new InventoryException("Unable to process release"));
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
        return Optional.of(request)
                .map(this::buildDeductInventoryProcessorRequest)
                .map(deductInventoryProcessor::process)
                .map(InventoryResponseMapper::mapDeductResponse)
                .map(response -> new Response<>(StatusCode.SUCCESS, "Deduction processed", response))
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new InventoryException("Unable to process deduction"));
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
        return Optional.of(buildGetAvailabilityProcessorRequest(productId, supplierProductIds))
                .map(getAvailabilityProcessor::process)
                .map(InventoryResponseMapper::mapAvailabilityResponse)
                .map(response -> new Response<>(StatusCode.SUCCESS, "Availability retrieved successfully", response))
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new InventoryException("Unable to retrieve availability"));
    }

    private GetAvailabilityProcessorRequest buildGetAvailabilityProcessorRequest(UUID productId, List<UUID> supplierProductIds) {
        return GetAvailabilityProcessorRequest.builder()
                .productId(productId)
                .supplierProductIds(supplierProductIds)
                .build();
    }
}
