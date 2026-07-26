package com.learning.inventory_manager.core.processor.inventory;

import com.learning.inventory_manager.core.processor.ProcessorResponse;
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
public class DeductInventoryProcessorResponse implements ProcessorResponse {
    private List<UUID> deductedReservationIds;
}
