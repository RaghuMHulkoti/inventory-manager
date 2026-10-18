package com.learning.inventory_manager.core.usecase.warehouse;

import com.learning.inventory_manager.core.domain.Warehouse;
import com.learning.inventory_manager.core.usecase.UsecaseRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterWarehouseUsecaseRequest implements UsecaseRequest {
    private Warehouse warehouse;
}
