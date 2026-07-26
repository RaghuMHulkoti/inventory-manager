package com.learning.inventory_manager.core.usecase.inventory;

import com.learning.inventory_manager.core.domain.Inventory;
import com.learning.inventory_manager.core.usecase.UsecaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetStockUsecaseResponse implements UsecaseResponse {
    private Inventory inventory;
    private int quantityDelta;
}
