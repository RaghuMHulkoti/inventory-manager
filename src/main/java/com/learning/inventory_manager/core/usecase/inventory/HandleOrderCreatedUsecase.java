package com.learning.inventory_manager.core.usecase.inventory;

import com.learning.inventory_manager.core.domain.InventoryEvents;
import com.learning.inventory_manager.core.domain.ReservationItem;
import com.learning.inventory_manager.core.domain.Tracing;
import com.learning.inventory_manager.core.usecase.Usecase;
import com.learning.inventory_manager.core.usecase.UsecaseRequest;
import com.learning.inventory_manager.core.usecase.UsecaseResponse;
import com.learning.inventory_manager.exception.InsufficientStockException;
import com.learning.inventory_manager.exception.InventoryNotFoundException;
import com.learning.inventory_manager.exception.InventoryValidationException;
import com.learning.inventory_manager.gateway.InventoryOutboxGateway;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;

/**
 * Order Service's ORDER_CREATED asks for stock to be held (TDD §12.6). The hold itself is
 * {@link ReserveInventoryUsecase}, which is all-or-nothing and writes INVENTORY_RESERVED with the stock change.
 * When stock cannot be held, the whole transaction has rolled back, so the refusal is reported here as
 * INVENTORY_RESERVATION_FAILED. Redelivery is safe: the reservation idempotency prefix is the order id, exactly as
 * on the REST path, so a replay returns the same reservations. Other errors propagate for retry, then dead-letter.
 */
public class HandleOrderCreatedUsecase
        implements Usecase<HandleOrderCreatedUsecase.Request, HandleOrderCreatedUsecase.Response> {

    /** {@code payload} is the event envelope JSON exactly as Order Service published it. */
    public record Request(String payload) implements UsecaseRequest {
    }

    public record Response(Outcome outcome) implements UsecaseResponse {
    }

    public enum Outcome { IGNORED, RESERVED, REFUSED }

    private final ReserveInventoryUsecase reserve;
    private final InventoryOutboxGateway outbox;
    private final JsonMapper json;
    private final Clock clock;

    public HandleOrderCreatedUsecase(ReserveInventoryUsecase reserve, InventoryOutboxGateway outbox, JsonMapper json,
                                     Clock clock) {
        this.reserve = reserve;
        this.outbox = outbox;
        this.json = json;
        this.clock = clock;
    }

    /** @throws IllegalArgumentException when an ORDER_CREATED event is malformed: retried, then sent to the dead-letter topic */
    @Override
    public Response execute(Request request) {
        var root = json.readTree(request.payload());
        if (!"ORDER_CREATED".equals(root.path("eventType").asString(""))) {
            return new Response(Outcome.IGNORED);
        }
        try (var ignored = Tracing.continueTrace(root.path("traceId").asString(""))) {
            var data = root.path("data");
            var orderId = UUID.fromString(data.path("orderId").asString(""));
            var items = new ArrayList<ReservationItem>();
            data.path("items").forEach(i -> items.add(ReservationItem.builder()
                    .orderItemId(UUID.fromString(i.path("orderItemId").asString("")))
                    .supplierProductId(UUID.fromString(i.path("supplierProductId").asString("")))
                    .warehouseId(UUID.fromString(i.path("warehouseId").asString("")))
                    .quantity(i.path("quantity").asInt()).build()));
            try {
                reserve.execute(ReserveInventoryUsecaseRequest.builder().orderId(orderId).items(items)
                        .ttlSeconds(data.path("reservationTtlSeconds").asLong(1800)).idempotencyPrefix(orderId.toString())
                        .build());
                return new Response(Outcome.RESERVED);
            } catch (InsufficientStockException e) {
                return refuse(orderId, "INSUFFICIENT_STOCK", e.getMessage());
            } catch (InventoryNotFoundException e) {
                return refuse(orderId, "STOCK_NOT_FOUND", e.getMessage());
            } catch (InventoryValidationException e) {
                return refuse(orderId, "INVALID_RESERVATION", e.getMessage());
            }
        }
    }

    private Response refuse(UUID orderId, String reason, String message) {
        var data = new LinkedHashMap<String, Object>();
        data.put("orderId", orderId.toString());
        data.put("reason", reason);
        data.put("message", message);
        outbox.append(orderId, "INVENTORY_RESERVATION_FAILED",
                InventoryEvents.envelope(json, "INVENTORY_RESERVATION_FAILED", clock.instant(), data));
        return new Response(Outcome.REFUSED);
    }
}
