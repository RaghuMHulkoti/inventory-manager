package com.learning.inventory_manager.core.service.warehouse;

import com.learning.inventory_manager.core.domain.Warehouse;
import com.learning.inventory_manager.core.service.ServiceRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterWarehouseServiceRequest implements ServiceRequest {
    private Warehouse warehouse;
}
