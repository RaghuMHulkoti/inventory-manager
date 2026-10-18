package com.learning.inventory_manager.core.service.warehouse;

import com.learning.inventory_manager.core.domain.Warehouse;
import com.learning.inventory_manager.core.service.ServiceResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterWarehouseServiceResponse implements ServiceResponse {
    private Warehouse warehouse;
    /** False when the warehouse was already known and only its details were refreshed. */
    private boolean created;
}
