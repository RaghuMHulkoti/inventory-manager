//package com.learning.inventory_manager.application.usecase;
//
//import com.learning.inventory_manager.application.service.InventoryService;
//import com.learning.inventory_manager.domain.exception.WarehouseNotFoundException;
//import com.learning.inventory_manager.domain.gateway.InventoryGateway;
//import com.learning.inventory_manager.domain.gateway.WarehouseGateway;
//import com.learning.inventory_manager.domain.model.Inventory;
//import com.learning.inventory_manager.presentation.rest.dto.request.InventoryItemRequest;
//import com.learning.inventory_manager.presentation.rest.dto.response.InventoryUpsertItemResult;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
///**
// * Unit tests for BulkInventoryUpsertUseCase.
// */
//@ExtendWith(MockitoExtension.class)
//class BulkInventoryUpsertUseCaseTest {
//    @Mock
//    private WarehouseGateway warehouseGateway;
//
//    @Mock
//    private InventoryGateway inventoryGateway;
//
//    @Mock
//    private InventoryService inventoryService;
//
//    @InjectMocks
//    private BulkInventoryUpsertUseCase useCase;
//
//    private UUID supplierId;
//    private UUID productId;
//    private UUID warehouseId;
//
//    @BeforeEach
//    void setUp() {
//        supplierId = UUID.randomUUID();
//        productId = UUID.randomUUID();
//        warehouseId = UUID.randomUUID();
//    }
//
//    @Test
//    void testExecuteCreateNewInventory() {
//        // Arrange
//        var item = new InventoryItemRequest(productId, warehouseId, 100);
//        var items = List.of(item);
//
//        when(inventoryGateway.findBySupplierProductIdAndWarehouseId(productId, warehouseId))
//            .thenReturn(Optional.empty());
//
//        var newInventory = Inventory.create(productId, warehouseId, 100);
//        when(inventoryService.createInventory(productId, warehouseId, 100, supplierId))
//            .thenReturn(newInventory);
//
//        // Act
//        var results = useCase.execute(supplierId, items);
//
//        // Assert
//        assertEquals(1, results.size());
//        assertEquals("SUCCESS", results.get(0).status());
//        assertEquals("CREATE", results.get(0).operation());
//
//        verify(warehouseGateway).validateWarehouseExists(warehouseId);
//        verify(inventoryService).createInventory(productId, warehouseId, 100, supplierId);
//    }
//
//    @Test
//    void testExecuteUpdateExistingInventory() {
//        // Arrange
//        var item = new InventoryItemRequest(productId, warehouseId, 150);
//        var items = List.of(item);
//
//        var existingInventory = Inventory.create(productId, warehouseId, 100);
//        when(inventoryGateway.findBySupplierProductIdAndWarehouseId(productId, warehouseId))
//            .thenReturn(Optional.of(existingInventory));
//
//        var updatedInventory = Inventory.create(productId, warehouseId, 150);
//        when(inventoryService.updateInventory(existingInventory, 150, supplierId))
//            .thenReturn(updatedInventory);
//
//        // Act
//        var results = useCase.execute(supplierId, items);
//
//        // Assert
//        assertEquals(1, results.size());
//        assertEquals("SUCCESS", results.get(0).status());
//        assertEquals("UPDATE", results.get(0).operation());
//
//        verify(inventoryService).updateInventory(existingInventory, 150, supplierId);
//    }
//
//    @Test
//    void testExecutePartialSuccess() {
//        // Arrange
//        var validItem = new InventoryItemRequest(productId, warehouseId, 100);
//        var invalidItem = new InventoryItemRequest(productId, UUID.randomUUID(), 50);
//        var items = List.of(validItem, invalidItem);
//
//        // First item succeeds
//        when(inventoryGateway.findBySupplierProductIdAndWarehouseId(productId, warehouseId))
//            .thenReturn(Optional.empty());
//        var newInventory = Inventory.create(productId, warehouseId, 100);
//        when(inventoryService.createInventory(productId, warehouseId, 100, supplierId))
//            .thenReturn(newInventory);
//
//        // Second item fails - warehouse not found
//        doThrow(new WarehouseNotFoundException(invalidItem.warehouseId()))
//            .when(warehouseGateway).validateWarehouseExists(invalidItem.warehouseId());
//
//        // Act
//        var results = useCase.execute(supplierId, items);
//
//        // Assert
//        assertEquals(2, results.size());
//        assertEquals("SUCCESS", results.get(0).status());
//        assertEquals("FAILED", results.get(1).status());
//    }
//
//    @Test
//    void testExecuteMultipleItems() {
//        // Arrange
//        var item1 = new InventoryItemRequest(UUID.randomUUID(), warehouseId, 100);
//        var item2 = new InventoryItemRequest(UUID.randomUUID(), warehouseId, 200);
//        var items = List.of(item1, item2);
//
//        when(inventoryGateway.findBySupplierProductIdAndWarehouseId(any(), any()))
//            .thenReturn(Optional.empty());
//
//        when(inventoryService.createInventory(any(), any(), anyInt(), any()))
//            .thenAnswer(invocation -> Inventory.create(
//                invocation.getArgument(0),
//                invocation.getArgument(1),
//                invocation.getArgument(2)
//            ));
//
//        // Act
//        var results = useCase.execute(supplierId, items);
//
//        // Assert
//        assertEquals(2, results.size());
//        assertTrue(results.stream().allMatch(r -> "SUCCESS".equals(r.status())));
//
//        verify(inventoryService, times(2)).createInventory(any(), any(), anyInt(), any());
//    }
//}
//
