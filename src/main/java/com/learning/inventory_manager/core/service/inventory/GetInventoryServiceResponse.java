package com.learning.inventory_manager.core.service.inventory;

import com.learning.inventory_manager.core.service.ServiceResponse;
import com.learning.inventory_manager.core.domain.Inventory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetInventoryServiceResponse implements ServiceResponse {
    private List<Inventory> inventories;
}
