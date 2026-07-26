package com.learning.inventory_manager.core.usecase.inventory;

import com.learning.inventory_manager.core.domain.Inventory;
import com.learning.inventory_manager.core.usecase.UsecaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetInventoryUsecaseResponse implements UsecaseResponse {
    private List<Inventory> inventories;
}
