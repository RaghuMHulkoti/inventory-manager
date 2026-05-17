//package com.learning.inventory_manager.application.service;
//
//import com.learning.inventory_manager.exception.InventoryValidationException;
//import com.learning.inventory_manager.gateway.InventoryGateway;
//import com.learning.inventory_manager.gateway.InventoryLedgerGateway;
//import com.learning.inventory_manager.core.domain.Inventory;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
///**
// * Unit tests for InventoryService business logic.
// */
//@ExtendWith(MockitoExtension.class)
//class InventoryServiceTest {
//    @Mock
//    private InventoryGateway inventoryGateway;
//
//    @Mock
//    private InventoryLedgerGateway ledgerGateway;
//
//    @InjectMocks
//    private InventoryService service;
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
//    void testCreateInventorySuccess() {
//        // Arrange
//        when(inventoryGateway.create(any(Inventory.class)))
//            .thenAnswer(invocation -> invocation.getArgument(0));
//
//        // Act
//        var result = service.createInventory(productId, warehouseId, 100, supplierId);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(100, result.getQuantityOnHand());
//        assertEquals(0, result.getQuantityReserved());
//
//        verify(inventoryGateway).create(any(Inventory.class));
//        verify(ledgerGateway).recordChange(any());
//    }
//
//    @Test
//    void testCreateInventoryWithNegativeQuantity() {
//        // Act & Assert
//        assertThrows(InventoryValidationException.class,
//            () -> service.createInventory(productId, warehouseId, -10, supplierId));
//
//        verify(inventoryGateway, never()).create(any());
//        verify(ledgerGateway, never()).recordChange(any());
//    }
//
//    @Test
//    void testUpdateInventorySuccess() {
//        // Arrange
//        var inventory = new Inventory(
//            UUID.randomUUID(), productId, warehouseId,
//            100, 0, 0L, null, null
//        );
//
//        when(inventoryGateway.save(any(Inventory.class)))
//            .thenAnswer(invocation -> invocation.getArgument(0));
//
//        // Act
//        var result = service.updateInventory(inventory, 150, supplierId);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(150, result.getQuantityOnHand());
//
//        verify(inventoryGateway).save(any(Inventory.class));
//        verify(ledgerGateway).recordChange(any());
//    }
//
//    @Test
//    void testUpdateInventoryExceedsReserved() {
//        // Arrange
//        var inventory = new Inventory(
//            UUID.randomUUID(), productId, warehouseId,
//            100, 80, 0L, null, null
//        );
//
//        // Act & Assert
//        assertThrows(InventoryValidationException.class,
//            () -> service.updateInventory(inventory, 50, supplierId));
//
//        verify(inventoryGateway, never()).save(any(Inventory.class));
//    }
//}
//
