package com.learning.inventory_manager.core.processor.inventory;

import com.learning.inventory_manager.core.processor.ProcessorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseInventoryProcessorResponse implements ProcessorResponse {
    private UUID reservationId;
    private boolean success;
}
