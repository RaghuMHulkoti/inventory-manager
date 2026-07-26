package com.learning.inventory_manager.core.processor.inventory;

import com.learning.inventory_manager.core.domain.DeductItem;
import com.learning.inventory_manager.core.processor.ProcessorRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeductInventoryProcessorRequest implements ProcessorRequest {
    private UUID orderId;
    private List<DeductItem> items;
}
