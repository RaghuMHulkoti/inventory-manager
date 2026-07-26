package com.learning.inventory_manager.core.service.inventory;

import com.learning.inventory_manager.core.domain.InventoryLedgerEntry;
import com.learning.inventory_manager.core.service.Service;
import com.learning.inventory_manager.exception.ReservationNotFoundException;
import com.learning.inventory_manager.gateway.InventoryGateway;
import com.learning.inventory_manager.gateway.InventoryLedgerGateway;
import com.learning.inventory_manager.gateway.StockReservationGateway;
import org.springframework.transaction.annotation.Transactional;

public class ReleaseInventoryService implements Service<ReleaseInventoryServiceRequest, ReleaseInventoryServiceResponse> {

    private final StockReservationGateway reservationGateway;
    private final InventoryGateway inventoryGateway;
    private final InventoryLedgerGateway ledgerGateway;

    public ReleaseInventoryService(StockReservationGateway reservationGateway,
                                   InventoryGateway inventoryGateway,
                                   InventoryLedgerGateway ledgerGateway) {
        this.reservationGateway = reservationGateway;
        this.inventoryGateway = inventoryGateway;
        this.ledgerGateway = ledgerGateway;
    }

    @Override
    @Transactional
    public ReleaseInventoryServiceResponse execute(ReleaseInventoryServiceRequest request) {
        var reservation = request.getReservationId() != null
                ? reservationGateway.findById(request.getReservationId()).orElse(null)
                : reservationGateway.findByIdempotencyKey(request.getIdempotencyKey()).orElse(null);

        if (reservation == null) {
            throw new ReservationNotFoundException(
                    request.getReservationId() != null ? request.getReservationId().toString() : request.getIdempotencyKey()
            );
        }

        if (reservation.getStatus() != com.learning.inventory_manager.core.domain.StockReservation.ReservationStatus.HELD) {
            return ReleaseInventoryServiceResponse.builder()
                    .reservationId(reservation.getId())
                    .success(true)
                    .build();
        }

        boolean released = reservationGateway.release(reservation.getId());
        if (!released) {
            return ReleaseInventoryServiceResponse.builder().success(false).build();
        }

        inventoryGateway.releaseStock(reservation.getInventoryId(), reservation.getQuantity());

        var inventory = inventoryGateway.findById(reservation.getInventoryId()).orElseThrow();

        var ledgerEntry = InventoryLedgerEntry.builder()
                .inventoryId(inventory.getId())
                .changeType("RELEASE")
                .quantityDelta(reservation.getQuantity())
                .quantityAfter(inventory.getQuantityOnHand())
                .reservationId(reservation.getId())
                .referenceType("ORDER")
                .referenceId(reservation.getOrderId().toString())
                .actor("order-service")
                .note("Reservation released: " + request.getReason())
                .build();

        ledgerGateway.recordChange(ledgerEntry);

        return ReleaseInventoryServiceResponse.builder()
                .reservationId(reservation.getId())
                .success(true)
                .build();
    }
}
