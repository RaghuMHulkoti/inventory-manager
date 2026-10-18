package com.learning.inventory_manager;

import com.learning.inventory_manager.gateway.WarehouseRepository;
import com.learning.inventory_manager.gateway.entity.WarehouseEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end tests for the inventory API: HTTP → processor → use case → service → JPA (H2).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InventoryFlowIntegrationTest {

    private static final String BASE = "/api/v1/inventory";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WarehouseRepository warehouseRepository;

    private UUID supplierId;
    private UUID warehouseId;
    private UUID supplierProductId;

    @BeforeEach
    void setUp() {
        supplierId = UUID.randomUUID();
        warehouseId = UUID.randomUUID();
        supplierProductId = UUID.randomUUID();
        warehouseRepository.save(WarehouseEntity.builder()
                .id(warehouseId)
                .supplierId(supplierId)
                .code("WH-" + warehouseId)
                .name("Main Warehouse")
                .isActive(true)
                .build());
    }

    @Test
    void bulkUpsertCreatesThenUpdatesInventory() throws Exception {
        bulkUpsert(warehouseId, 10)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.successCount").value(1))
                .andExpect(jsonPath("$.body.results[0].operation").value("CREATE"));

        bulkUpsert(warehouseId, 15)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.successCount").value(1))
                .andExpect(jsonPath("$.body.results[0].operation").value("UPDATE"))
                .andExpect(jsonPath("$.body.results[0].quantityDelta").value(5));

        mockMvc.perform(get(BASE + "/" + supplierProductId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.inventories", hasSize(1)))
                .andExpect(jsonPath("$.body.inventories[0].quantityOnHand").value(15))
                .andExpect(jsonPath("$.body.inventories[0].availableQuantity").value(15));
    }

    @Test
    void bulkUpsertReportsUnknownWarehouseAsItemFailure() throws Exception {
        bulkUpsert(UUID.randomUUID(), 10)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.successCount").value(0))
                .andExpect(jsonPath("$.body.failureCount").value(1));
    }

    @Test
    void bulkUpsertRejectsWarehouseOwnedByAnotherSupplier() throws Exception {
        bulkUpsertAs(UUID.randomUUID(), warehouseId, 10)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.successCount").value(0))
                .andExpect(jsonPath("$.body.failureCount").value(1));

        mockMvc.perform(get(BASE + "/" + supplierProductId))
                .andExpect(jsonPath("$.body.inventories", hasSize(0)));
    }

    @Test
    void reserveIsIdempotentAndDeductCommitsStock() throws Exception {
        bulkUpsert(warehouseId, 10).andExpect(status().isOk());
        var orderId = UUID.randomUUID();
        var orderItemId = UUID.randomUUID();

        String reservationId = reserve(orderId, orderItemId, 3)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.reservations[0].status").value("HELD"))
                .andReturn().getResponse().getContentAsString()
                .replaceAll(".*\"reservationId\":\"([^\"]+)\".*", "$1");

        // Same order + item again must return the same reservation, not hold more stock
        reserve(orderId, orderItemId, 3)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.reservations[0].reservationId").value(reservationId));

        mockMvc.perform(post(BASE + "/deduct").contentType(MediaType.APPLICATION_JSON).content("""
                        {"orderId":"%s","items":[{"orderItemId":"%s","reservationId":"%s"}]}
                        """.formatted(orderId, orderItemId, reservationId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.deductedReservationIds[0]").value(reservationId));

        mockMvc.perform(get(BASE + "/" + supplierProductId))
                .andExpect(jsonPath("$.body.inventories[0].quantityOnHand").value(7))
                .andExpect(jsonPath("$.body.inventories[0].quantityReserved").value(0));
    }

    @Test
    void releaseReturnsHeldStock() throws Exception {
        bulkUpsert(warehouseId, 10).andExpect(status().isOk());

        String reservationId = reserve(UUID.randomUUID(), UUID.randomUUID(), 4)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()
                .replaceAll(".*\"reservationId\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(post(BASE + "/release").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reservationId\":\"%s\",\"reason\":\"PAYMENT_FAILED\"}".formatted(reservationId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.success").value(true));

        mockMvc.perform(get(BASE + "/" + supplierProductId))
                .andExpect(jsonPath("$.body.inventories[0].quantityReserved").value(0));
    }

    @Test
    void reserveMoreThanAvailableReturns422WithCorrectNumbers() throws Exception {
        bulkUpsert(warehouseId, 5).andExpect(status().isOk());

        reserve(UUID.randomUUID(), UUID.randomUUID(), 8)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.body.errorCode").value("INSUFFICIENT_STOCK"))
                .andExpect(jsonPath("$.body.details.available").value("5"))
                .andExpect(jsonPath("$.body.details.requested").value("8"));
    }

    @Test
    void failedItemRollsBackStockHeldForEarlierItemsInSameBatch() throws Exception {
        bulkUpsert(warehouseId, 10).andExpect(status().isOk());

        mockMvc.perform(post(BASE + "/reserve").contentType(MediaType.APPLICATION_JSON).content("""
                        {"orderId":"%s","items":[
                          {"orderItemId":"%s","supplierProductId":"%s","warehouseId":"%s","quantity":3},
                          {"orderItemId":"%s","supplierProductId":"%s","warehouseId":"%s","quantity":1}]}
                        """.formatted(UUID.randomUUID(),
                        UUID.randomUUID(), supplierProductId, warehouseId,
                        UUID.randomUUID(), UUID.randomUUID(), warehouseId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.body.errorCode").value("INVENTORY_NOT_FOUND"));

        mockMvc.perform(get(BASE + "/" + supplierProductId))
                .andExpect(jsonPath("$.body.inventories[0].quantityReserved").value(0));
    }

    @Test
    void reserveUnknownInventoryReturns404() throws Exception {
        reserve(UUID.randomUUID(), UUID.randomUUID(), 1)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.body.errorCode").value("INVENTORY_NOT_FOUND"));
    }

    @Test
    void releaseWithoutIdOrKeyReturns400() throws Exception {
        mockMvc.perform(post(BASE + "/release").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        mockMvc.perform(post(BASE + "/reserve").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void availabilityWithoutRequiredParamReturns400() throws Exception {
        mockMvc.perform(get(BASE + "/availability"))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ResultActions bulkUpsert(UUID targetWarehouseId, int quantity) throws Exception {
        return bulkUpsertAs(supplierId, targetWarehouseId, quantity);
    }

    private ResultActions bulkUpsertAs(UUID requestingSupplierId, UUID targetWarehouseId, int quantity) throws Exception {
        return mockMvc.perform(post(BASE + "/bulk-upsert").contentType(MediaType.APPLICATION_JSON).content("""
                {"supplierId":"%s","items":[{"supplierProductId":"%s","warehouseId":"%s","quantityOnHand":%d}]}
                """.formatted(requestingSupplierId, supplierProductId, targetWarehouseId, quantity)));
    }

    private ResultActions reserve(UUID orderId, UUID orderItemId, int quantity) throws Exception {
        return mockMvc.perform(post(BASE + "/reserve").contentType(MediaType.APPLICATION_JSON).content("""
                {"orderId":"%s","items":[{"orderItemId":"%s","supplierProductId":"%s","warehouseId":"%s","quantity":%d}]}
                """.formatted(orderId, orderItemId, supplierProductId, warehouseId, quantity)));
    }
}
