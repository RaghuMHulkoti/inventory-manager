package com.learning.inventory_manager.core.domain;

import java.util.UUID;

/**
 * Inventory's copy of a supplier's warehouse. {@code id} is the id Supplier Service assigned, so stock written
 * against it stays valid when this copy is later fed by events instead of the registration API.
 */
public record Warehouse(UUID id, UUID supplierId, String code, String name, String city, String state, String country,
                        boolean active) {
}
