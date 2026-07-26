package com.learning.inventory_manager.core.usecase.inventory;

import com.learning.inventory_manager.core.domain.StockReservation;
import com.learning.inventory_manager.core.usecase.UsecaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReserveInventoryUsecaseResponse implements UsecaseResponse {
    private List<StockReservation> reservations;
}
