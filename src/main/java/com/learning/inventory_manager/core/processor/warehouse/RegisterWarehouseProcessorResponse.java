package com.learning.inventory_manager.core.processor.warehouse;

import com.learning.inventory_manager.core.domain.Warehouse;
import com.learning.inventory_manager.core.processor.ProcessorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterWarehouseProcessorResponse implements ProcessorResponse {
    private Warehouse warehouse;
    private boolean created;
}
