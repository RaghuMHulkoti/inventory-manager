package com.learning.inventory_manager.core.service.inventory;

import com.learning.inventory_manager.core.domain.InventoryLedgerEntry;
import com.learning.inventory_manager.core.domain.StockReservation;
import com.learning.inventory_manager.core.service.Service;
import com.learning.inventory_manager.exception.ReservationNotFoundException;
import com.learning.inventory_manager.gateway.InventoryGateway;
import com.learning.inventory_manager.gateway.InventoryLedgerGateway;
import com.learning.inventory_manager.gateway.StockReservationGateway;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeductInventoryService implements Service<DeductInventoryServiceRequest, DeductInventoryServiceResponse> {

    private final StockReservationGateway reservationGateway;
    private final InventoryGateway inventoryGateway;
    private final InventoryLedgerGateway ledgerGateway;

    public DeductInventoryService(StockReservationGateway reservationGateway,
                                  InventoryGateway inventoryGateway,
                                  InventoryLedgerGateway ledgerGateway) {
        this.reservationGateway = reservationGateway;
        this.inventoryGateway = inventoryGateway;
        this.ledgerGateway = ledgerGateway;
    }

    @Override
    @Transactional
    public DeductInventoryServiceResponse execute(DeductInventoryServiceRequest request) {
        List<UUID> deductedIds = new ArrayList<>();

        for (var item : request.getItems()) {
            var reservation = reservationGateway.findById(item.getReservationId())
                    .orElseThrow(() -> new ReservationNotFoundException(item.getReservationId().toString()));

            if (!reservation.getOrderId().equals(request.getOrderId())) {
                throw new IllegalStateException("Reservation does not belong to order " + request.getOrderId());
            }

            if (reservation.getStatus() != StockReservation.ReservationStatus.HELD) {
                continue;
            }

            boolean committed = reservationGateway.commit(reservation.getId());
            if (!committed) {
                throw new IllegalStateException("Could not commit reservation " + reservation.getId());
            }

            inventoryGateway.deductStock(reservation.getInventoryId(), reservation.getQuantity());

            var inventory = inventoryGateway.findById(reservation.getInventoryId()).orElseThrow();

            var ledgerEntry = InventoryLedgerEntry.builder()
                .inventoryId(inventory.getId())
                .changeType("DEDUCT")
                .quantityDelta(-reservation.getQuantity())
                .quantityAfter(inventory.getQuantityOnHand())
                .reservationId(reservation.getId())
                .referenceType("ORDER")
                .referenceId(request.getOrderId().toString())
                .actor("order-service")
                .note("Stock deducted on payment success")
                .build();

            ledgerGateway.recordChange(ledgerEntry);

            deductedIds.add(reservation.getId());
        }

        return DeductInventoryServiceResponse.builder()
                .deductedReservationIds(deductedIds)
                .build();
    }
}
