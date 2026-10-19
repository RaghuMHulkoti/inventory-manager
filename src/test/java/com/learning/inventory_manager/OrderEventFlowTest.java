package com.learning.inventory_manager;

import com.learning.inventory_manager.core.usecase.inventory.HandleOrderCreatedUsecase;
import com.learning.inventory_manager.gateway.InventoryOutboxPoller;
import com.learning.inventory_manager.gateway.InventoryOutboxRepository;
import com.learning.inventory_manager.gateway.WarehouseRepository;
import com.learning.inventory_manager.gateway.entity.WarehouseEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Inventory's side of the saga events: ORDER_CREATED in, INVENTORY_RESERVED / INVENTORY_RESERVATION_FAILED out. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderEventFlowTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    WarehouseRepository warehouseRepository;
    @Autowired
    HandleOrderCreatedUsecase handler;
    @Autowired
    InventoryOutboxRepository outboxRepository;
    @Autowired
    PlatformTransactionManager txManager;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    JsonMapper json;

    UUID supplierId;
    UUID warehouseId;
    UUID supplierProductId;
    UUID orderId;
    UUID orderItemId;

    @BeforeEach
    void setUp() throws Exception {
        jdbc.update("DELETE FROM INVENTORY.inventory_outbox");
        supplierId = UUID.randomUUID();
        warehouseId = UUID.randomUUID();
        supplierProductId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        orderItemId = UUID.randomUUID();
        warehouseRepository.save(WarehouseEntity.builder().id(warehouseId).supplierId(supplierId)
                .code("WH-" + warehouseId).name("Main Warehouse").isActive(true).build());
        mockMvc.perform(post("/api/v1/inventory/bulk-upsert").contentType(MediaType.APPLICATION_JSON).content("""
                {"supplierId":"%s","items":[{"supplierProductId":"%s","warehouseId":"%s","quantityOnHand":5}]}
                """.formatted(supplierId, supplierProductId, warehouseId))).andExpect(status().isOk());
    }

    @Test
    void orderCreatedHoldsTheStockAndWritesInventoryReserved() {
        var result = handler.execute(new HandleOrderCreatedUsecase.Request(orderCreated(2)));

        assertThat(result.outcome()).isEqualTo(HandleOrderCreatedUsecase.Outcome.RESERVED);
        assertThat(types()).containsExactly("INVENTORY_RESERVED");
        var event = json.readTree(jdbc.queryForObject("SELECT payload FROM INVENTORY.inventory_outbox", String.class));
        assertThat(event.path("producer").asString("")).isEqualTo("inventory-service");
        assertThat(event.path("data").path("orderId").asString("")).isEqualTo(orderId.toString());
        var line = event.path("data").path("reservations").get(0);
        assertThat(line.path("orderItemId").asString("")).isEqualTo(orderItemId.toString());
        assertThat(line.path("reservationId").asString("")).isNotEmpty();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM INVENTORY.stock_reservations WHERE status = 'HELD' AND order_id = ?", Integer.class, orderId)).isEqualTo(1);
    }

    @Test
    void aRedeliveredOrderCreatedReturnsTheSameReservation() {
        handler.execute(new HandleOrderCreatedUsecase.Request(orderCreated(2)));
        handler.execute(new HandleOrderCreatedUsecase.Request(orderCreated(2)));

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM INVENTORY.stock_reservations WHERE order_id = ?", Integer.class, orderId)).isEqualTo(1);
        var ids = jdbc.queryForList("SELECT payload FROM INVENTORY.inventory_outbox ORDER BY id", String.class).stream()
                .map(p -> json.readTree(p).path("data").path("reservations").get(0).path("reservationId").asString("")).toList();
        assertThat(ids).hasSize(2).containsOnly(ids.get(0));
    }

    @Test
    void notEnoughStockWritesReservationFailedAndHoldsNothing() {
        var result = handler.execute(new HandleOrderCreatedUsecase.Request(orderCreated(9)));

        assertThat(result.outcome()).isEqualTo(HandleOrderCreatedUsecase.Outcome.REFUSED);
        assertThat(types()).containsExactly("INVENTORY_RESERVATION_FAILED");
        var data = json.readTree(jdbc.queryForObject("SELECT payload FROM INVENTORY.inventory_outbox", String.class)).path("data");
        assertThat(data.path("orderId").asString("")).isEqualTo(orderId.toString());
        assertThat(data.path("reason").asString("")).isEqualTo("INSUFFICIENT_STOCK");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM INVENTORY.stock_reservations WHERE order_id = ?", Integer.class, orderId)).isZero();
    }

    @Test
    void otherEventsAreIgnoredAndMalformedOnesRejected() {
        assertThat(handler.execute(new HandleOrderCreatedUsecase.Request(
                "{\"eventType\":\"ORDER_CONFIRMED\",\"data\":{}}")).outcome())
                .isEqualTo(HandleOrderCreatedUsecase.Outcome.IGNORED);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> handler.execute(new HandleOrderCreatedUsecase.Request(
                "{\"eventType\":\"ORDER_CREATED\",\"data\":{\"orderId\":\"nope\"}}")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void relayPublishesInventoryEventsKeyedByOrderIdAndKeepsThemPendingWhenTheBrokerIsDown() {
        handler.execute(new HandleOrderCreatedUsecase.Request(orderCreated(2)));
        var down = poller((key, type, body) -> {
            throw new IllegalStateException("broker down");
        });
        assertThat(down.runOnce()).isZero();
        assertThat(jdbc.queryForObject("SELECT attempts FROM INVENTORY.inventory_outbox", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT status FROM INVENTORY.inventory_outbox", String.class)).isEqualTo("PENDING");

        var sent = new ArrayList<String>();
        assertThat(poller((key, type, body) -> sent.add(key + "|" + type)).runOnce()).isEqualTo(1);

        assertThat(sent).containsExactly(orderId + "|INVENTORY_RESERVED");
        assertThat(jdbc.queryForObject("SELECT status FROM INVENTORY.inventory_outbox", String.class)).isEqualTo("SENT");
    }

    InventoryOutboxPoller poller(com.learning.inventory_manager.gateway.messaging.EventPublisher publisher) {
        return new InventoryOutboxPoller(outboxRepository, publisher, new TransactionTemplate(txManager), Clock.systemUTC(), 100);
    }

    List<String> types() {
        return jdbc.queryForList("SELECT event_type FROM INVENTORY.inventory_outbox ORDER BY id", String.class);
    }

    String orderCreated(int quantity) {
        return """
                {"eventId":"%s","eventType":"ORDER_CREATED","producer":"order-service","data":{"orderId":"%s",
                "reservationTtlSeconds":1800,"items":[{"orderItemId":"%s","supplierProductId":"%s","warehouseId":"%s","quantity":%d}]}}
                """.formatted(UUID.randomUUID(), orderId, orderItemId, supplierProductId, warehouseId, quantity);
    }
}
