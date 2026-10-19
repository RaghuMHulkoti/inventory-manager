package com.learning.inventory_manager.core.service.inventory;

import com.learning.inventory_manager.core.domain.InventoryEvents;
import com.learning.inventory_manager.core.domain.InventoryLedgerEntry;
import com.learning.inventory_manager.core.domain.InventoryLedgerEntry.ChangeType;
import com.learning.inventory_manager.core.domain.StockReservation;
import com.learning.inventory_manager.core.service.Service;
import com.learning.inventory_manager.exception.InsufficientStockException;
import com.learning.inventory_manager.exception.InventoryNotFoundException;
import com.learning.inventory_manager.gateway.InventoryGateway;
import com.learning.inventory_manager.gateway.InventoryLedgerGateway;
import com.learning.inventory_manager.gateway.InventoryOutboxGateway;
import com.learning.inventory_manager.gateway.StockReservationGateway;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public class ReserveInventoryService implements Service<ReserveInventoryServiceRequest, ReserveInventoryServiceResponse> {

    private final InventoryGateway inventoryGateway;
    private final StockReservationGateway reservationGateway;
    private final InventoryLedgerGateway ledgerGateway;
    private final InventoryOutboxGateway outboxGateway;
    private final JsonMapper json;

    public ReserveInventoryService(InventoryGateway inventoryGateway,
                                   StockReservationGateway reservationGateway,
                                   InventoryLedgerGateway ledgerGateway,
                                   InventoryOutboxGateway outboxGateway,
                                   JsonMapper json) {
        this.inventoryGateway = inventoryGateway;
        this.reservationGateway = reservationGateway;
        this.ledgerGateway = ledgerGateway;
        this.outboxGateway = outboxGateway;
        this.json = json;
    }

    @Override
    @Transactional
    public ReserveInventoryServiceResponse execute(ReserveInventoryServiceRequest request) {
        List<StockReservation> createdReservations = new ArrayList<>();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(request.getTtlSeconds());

        // No manual compensation needed: any exception rolls back the whole transaction,
        // including stock already reserved for earlier items in this batch.
        for (var item : request.getItems()) {
            String itemKey = request.getIdempotencyPrefix() + "-" + item.getOrderItemId();

            var existingRes = reservationGateway.findByIdempotencyKey(itemKey).orElse(null);
            if (existingRes != null) {
                createdReservations.add(existingRes);
                continue;
            }

            var inventory = inventoryGateway.findBySupplierProductIdAndWarehouseId(
                    item.getSupplierProductId(), item.getWarehouseId()
            ).orElseThrow(() -> InventoryNotFoundException.byProductAndWarehouse(
                    item.getSupplierProductId(), item.getWarehouseId()));

            boolean reserved = inventoryGateway.tryReserveStock(inventory.getId(), item.getQuantity());
            if (!reserved) {
                var currentInv = inventoryGateway.findById(inventory.getId()).orElse(inventory);
                throw new InsufficientStockException(
                        inventory.getSupplierProductId(),
                        inventory.getWarehouseId(),
                        currentInv.getAvailableQuantity(),
                        item.getQuantity()
                );
            }

            var stockReservation = StockReservation.builder()
                    .id(UUID.randomUUID())
                    .orderId(request.getOrderId())
                    .orderItemId(item.getOrderItemId())
                    .inventoryId(inventory.getId())
                    .supplierProductId(inventory.getSupplierProductId())
                    .warehouseId(inventory.getWarehouseId())
                    .quantity(item.getQuantity())
                    .status(StockReservation.ReservationStatus.HELD)
                    .expiresAt(expiresAt)
                    .idempotencyKey(itemKey)
                    .build();

            var savedReservation = reservationGateway.save(stockReservation);
            createdReservations.add(savedReservation);

            var ledgerEntry = InventoryLedgerEntry.builder()
                    .inventoryId(inventory.getId())
                    .changeType(ChangeType.RESERVE)
                    .quantityDelta(item.getQuantity())
                    .quantityAfter(inventory.getQuantityOnHand())
                    .reservationId(savedReservation.getId())
                    .referenceType("ORDER")
                    .referenceId(request.getOrderId().toString())
                    .actor("order-service")
                    .note("Stock reserved for order")
                    .build();

            ledgerGateway.recordChange(ledgerEntry);
        }

        // Written in the same transaction as the stock change; replays re-emit it, which Order Service tolerates.
        outboxGateway.append(request.getOrderId(), "INVENTORY_RESERVED", reservedEvent(request.getOrderId(), createdReservations));

        return ReserveInventoryServiceResponse.builder()
                .reservations(createdReservations)
                .build();
    }

    private String reservedEvent(UUID orderId, List<StockReservation> reservations) {
        var data = new LinkedHashMap<String, Object>();
        data.put("orderId", orderId.toString());
        data.put("reservations", reservations.stream().map(r -> {
            var line = new LinkedHashMap<String, Object>();
            line.put("orderItemId", r.getOrderItemId().toString());
            line.put("reservationId", r.getId().toString());
            line.put("supplierProductId", r.getSupplierProductId().toString());
            line.put("warehouseId", r.getWarehouseId().toString());
            line.put("quantity", r.getQuantity());
            line.put("expiresAt", String.valueOf(r.getExpiresAt()));
            return line;
        }).toList());
        return InventoryEvents.envelope(json, "INVENTORY_RESERVED", Instant.now(), data);
    }
}
