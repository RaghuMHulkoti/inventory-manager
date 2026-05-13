//package com.learning.inventory_manager;
//
//import com.learning.inventory_manager.presentation.rest.dto.request.BulkInventoryUpsertRequest;
//import com.learning.inventory_manager.presentation.rest.dto.request.InventoryItemRequest;
//import com.learning.inventory_manager.infrastructure.persistence.entity.WarehouseEntity;
//import com.learning.inventory_manager.infrastructure.persistence.repository.WarehouseRepository;
//import org.junit.Ignore;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.UUID;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
///**
// * Integration tests for bulk inventory upsert API.
// * Tests the complete flow from controller through all layers to database.
// *
// * Uses H2 in-memory database for testing.
// * ActiveProfile: test - uses application-test.yaml configuration.
// */
//@SpringBootTest
//@ActiveProfiles("test")
//@Ignore("Integration test - requires application context and database setup")
//class InventoryBulkUpsertIntegrationTest {
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private WarehouseRepository warehouseRepository;
//
//    private UUID warehouseId;
//    private UUID supplierId;
//    private UUID productId;
//
//    @BeforeEach
//    void setUp() {
//        warehouseRepository.deleteAll();
//
//        // Create test warehouse
//        warehouseId = UUID.randomUUID();
//        supplierId = UUID.randomUUID();
//        productId = UUID.randomUUID();
//
//        var warehouse = WarehouseEntity.builder()
//            .id(warehouseId)
//            .supplierId(UUID.randomUUID())
//            .code("WH-001")
//            .name("Main Warehouse")
//            .city("New York")
//            .state("NY")
//            .country("USA")
//            .isActive(true)
//            .createdAt(LocalDateTime.now())
//            .updatedAt(LocalDateTime.now())
//            .build();
//
//        warehouseRepository.save(warehouse);
//    }
//
//    @Test
//    void testSuccessfulBulkInventoryUpsert() throws Exception {
//        // Create request
//        var item = new InventoryItemRequest(productId, warehouseId, 100);
//        var request = new BulkInventoryUpsertRequest(supplierId, List.of(item));
//
//        // Execute
//        mockMvc.perform(post("/api/v1/inventory/bulk-upsert")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(objectMapper.writeValueAsString(request)))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.totalProcessed").value(1))
//            .andExpect(jsonPath("$.successCount").value(1))
//            .andExpect(jsonPath("$.failureCount").value(0))
//            .andExpect(jsonPath("$.results[0].status").value("SUCCESS"))
//            .andExpect(jsonPath("$.results[0].operation").value("CREATE"));
//    }
//
//    @Test
//    void testInvalidWarehouse() throws Exception {
//        // Use non-existent warehouse
//        var invalidWarehouseId = UUID.randomUUID();
//        var item = new InventoryItemRequest(productId, invalidWarehouseId, 100);
//        var request = new BulkInventoryUpsertRequest(supplierId, List.of(item));
//
//        // Execute
//        mockMvc.perform(post("/api/v1/inventory/bulk-upsert")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(objectMapper.writeValueAsString(request)))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.successCount").value(0))
//            .andExpect(jsonPath("$.failureCount").value(1))
//            .andExpect(jsonPath("$.results[0].status").value("FAILED"));
//    }
//
//    @Test
//    void testNegativeQuantity() throws Exception {
//        var item = new InventoryItemRequest(productId, warehouseId, -10);
//        var request = new BulkInventoryUpsertRequest(supplierId, List.of(item));
//
//        mockMvc.perform(post("/api/v1/inventory/bulk-upsert")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(objectMapper.writeValueAsString(request)))
//            .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void testPartialSuccess() throws Exception {
//        var validWarehouse = warehouseId;
//        var invalidWarehouse = UUID.randomUUID();
//
//        var validItem = new InventoryItemRequest(productId, validWarehouse, 100);
//        var invalidItem = new InventoryItemRequest(productId, invalidWarehouse, 50);
//        var request = new BulkInventoryUpsertRequest(supplierId, List.of(validItem, invalidItem));
//
//        mockMvc.perform(post("/api/v1/inventory/bulk-upsert")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(objectMapper.writeValueAsString(request)))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.totalProcessed").value(2))
//            .andExpect(jsonPath("$.successCount").value(1))
//            .andExpect(jsonPath("$.failureCount").value(1));
//    }
//}
//
