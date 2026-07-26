package com.learning.inventory_manager.gateway;

import com.learning.inventory_manager.core.domain.StockReservation;

import java.util.Optional;
import java.util.UUID;

public interface StockReservationGateway {
    Optional<StockReservation> findById(UUID reservationId);
    Optional<StockReservation> findByIdempotencyKey(String idempotencyKey);
    StockReservation save(StockReservation reservation);
    boolean release(UUID reservationId);
    boolean commit(UUID reservationId);
}
