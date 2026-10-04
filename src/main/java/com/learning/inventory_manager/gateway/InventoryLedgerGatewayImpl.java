package com.learning.inventory_manager.gateway;

import com.learning.inventory_manager.core.domain.InventoryLedgerEntry;

public class InventoryLedgerGatewayImpl implements InventoryLedgerGateway {

    private final InventoryLedgerRepository repository;
    private final InventoryEntityMapper mapper;

    public InventoryLedgerGatewayImpl(InventoryLedgerRepository repository, InventoryEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public void recordChange(InventoryLedgerEntry ledgerEntry) {
        repository.save(mapper.toEntity(ledgerEntry));
    }
}
