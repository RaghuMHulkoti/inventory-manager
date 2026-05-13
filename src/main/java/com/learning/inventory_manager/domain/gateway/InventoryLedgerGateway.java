package com.learning.inventory_manager.domain.gateway;

import com.learning.inventory_manager.domain.model.InventoryLedgerEntry;
import java.util.UUID;

public interface InventoryLedgerGateway {
    void recordChange(InventoryLedgerEntry entry);
}

