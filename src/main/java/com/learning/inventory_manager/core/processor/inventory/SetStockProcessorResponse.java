package com.learning.inventory_manager.core.processor.inventory;

import com.learning.inventory_manager.core.domain.Inventory;
import com.learning.inventory_manager.core.processor.ProcessorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetStockProcessorResponse implements ProcessorResponse {
    private Inventory inventory;
    private int quantityDelta;
}
