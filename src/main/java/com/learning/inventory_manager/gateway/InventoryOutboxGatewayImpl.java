package com.learning.inventory_manager.gateway;

import com.learning.inventory_manager.gateway.entity.InventoryOutboxEntity;

import java.util.UUID;

public class InventoryOutboxGatewayImpl implements InventoryOutboxGateway {

    private final InventoryOutboxRepository repository;

    public InventoryOutboxGatewayImpl(InventoryOutboxRepository repository) {
        this.repository = repository;
    }

    @Override
    public void append(UUID aggregateId, String eventType, String payload) {
        repository.save(InventoryOutboxEntity.builder().aggregateId(aggregateId).eventType(eventType)
                .payload(payload).status("PENDING").build());
    }
}
