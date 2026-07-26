package com.learning.inventory_manager.exception;

import java.util.UUID;

/**
 * Thrown when a reservation cannot be found by its ID or idempotency key.
 */
public class ReservationNotFoundException extends InventoryException {
    public ReservationNotFoundException(String message) {
        super(message);
    }

    public static ReservationNotFoundException byId(UUID reservationId) {
        return new ReservationNotFoundException("Reservation not found with id: " + reservationId);
    }

    public static ReservationNotFoundException byIdempotencyKey(String key) {
        return new ReservationNotFoundException("Reservation not found with idempotency key: " + key);
    }
}
