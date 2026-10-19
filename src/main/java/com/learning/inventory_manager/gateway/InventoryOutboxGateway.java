package com.learning.inventory_manager.gateway;

import java.util.UUID;

/** Writes domain events to {@code inventory_outbox}; the relay publishes them to the bus. */
public interface InventoryOutboxGateway {

    /** Joins the caller's transaction when there is one, so the event exists exactly when the stock change does. */
    void append(UUID aggregateId, String eventType, String payload);
}
