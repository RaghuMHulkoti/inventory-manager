package com.learning.inventory_manager.core.processor.inventory;

import com.learning.inventory_manager.core.processor.ProcessorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAvailabilityProcessorResponse implements ProcessorResponse {
    private UUID productId;
    private int totalAvailable;
    private Map<UUID, Map<UUID, Integer>> breakdown;
}
