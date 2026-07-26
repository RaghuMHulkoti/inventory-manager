package com.learning.inventory_manager.web.response;

import com.learning.inventory_manager.core.domain.Inventory;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GetInventoryResponse {
    private List<Inventory> inventories;
}
