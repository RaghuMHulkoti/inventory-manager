package com.learning.inventory_manager.core.processor.inventory;

import com.learning.inventory_manager.core.processor.ProcessorRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseInventoryProcessorRequest implements ProcessorRequest {
    private UUID reservationId;
    private String idempotencyKey;
    private String reason;
}
