package com.learning.inventory_manager.gateway;

import com.learning.inventory_manager.gateway.entity.InventoryOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryOutboxRepository extends JpaRepository<InventoryOutboxEntity, Long> {
}
