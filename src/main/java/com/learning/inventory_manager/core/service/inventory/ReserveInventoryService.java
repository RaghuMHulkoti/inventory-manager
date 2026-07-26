package com.learning.inventory_manager.core.service.inventory;

import com.learning.inventory_manager.core.domain.InventoryLedgerEntry;
import com.learning.inventory_manager.core.domain.StockReservation;
import com.learning.inventory_manager.core.service.Service;
import com.learning.inventory_manager.exception.InsufficientStockException;
import com.learning.inventory_manager.gateway.InventoryGateway;
import com.learning.inventory_manager.gateway.InventoryLedgerGateway;
import com.learning.inventory_manager.gateway.StockReservationGateway;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReserveInventoryService implements Service<ReserveInventoryServiceRequest, ReserveInventoryServiceResponse> {

    private final InventoryGateway inventoryGateway;
    private final StockReservationGateway reservationGateway;
    private final InventoryLedgerGateway ledgerGateway;

    public ReserveInventoryService(InventoryGateway inventoryGateway,
                                   StockReservationGateway reservationGateway,
                                   InventoryLedgerGateway ledgerGateway) {
        this.inventoryGateway = inventoryGateway;
        this.reservationGateway = reservationGateway;
        this.ledgerGateway = ledgerGateway;
    }

    @Override
    @Transactional
    public ReserveInventoryServiceResponse execute(ReserveInventoryServiceRequest request) {
        List<StockReservation> createdReservations = new ArrayList<>();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(request.getTtlSeconds());

        try {
            for (var item : request.getItems()) {
                String itemKey = request.getIdempotencyPrefix() + "-" + item.getOrderItemId();

                var existingRes = reservationGateway.findByIdempotencyKey(itemKey).orElse(null);
                if (existingRes != null) {
                    createdReservations.add(existingRes);
                    continue;
                }

                var inventory = inventoryGateway.findBySupplierProductIdAndWarehouseId(
                        item.getSupplierProductId(), item.getWarehouseId()
                ).orElseThrow(() -> new IllegalArgumentException("Inventory not found"));

                boolean reserved = inventoryGateway.tryReserveStock(inventory.getId(), item.getQuantity());
                if (!reserved) {
                    var currentInv = inventoryGateway.findById(inventory.getId()).orElse(inventory);
                    throw new InsufficientStockException(
                            inventory.getSupplierProductId(),
                            inventory.getWarehouseId(),
                            item.getQuantity(),
                            currentInv.getAvailableQuantity()
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
                        .version(0L)
                        .build();

                var savedReservation = reservationGateway.save(stockReservation);
                createdReservations.add(savedReservation);

                var ledgerEntry = InventoryLedgerEntry.builder()
                        .inventoryId(inventory.getId())
                        .changeType("RESERVE")
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
        } catch (Exception e) {
            for (StockReservation res : createdReservations) {
                inventoryGateway.releaseStock(res.getInventoryId(), res.getQuantity());
            }
            throw e;
        }

        return ReserveInventoryServiceResponse.builder()
                .reservations(createdReservations)
                .build();
    }
}
