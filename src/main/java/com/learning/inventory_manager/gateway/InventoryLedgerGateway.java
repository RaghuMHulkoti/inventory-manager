package com.learning.inventory_manager.gateway;

import com.learning.inventory_manager.core.domain.InventoryLedgerEntry;

public interface InventoryLedgerGateway {
    void recordChange(InventoryLedgerEntry entry);
}

