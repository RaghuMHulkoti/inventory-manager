package com.learning.inventory_manager.web.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ReserveBatchResponse {
    private List<ReserveResponse> reservations;
}
