package com.learning.inventory_manager;

import com.learning.inventory_manager.core.domain.Warehouse;
import com.learning.inventory_manager.core.processor.warehouse.RegisterWarehouseProcessor;
import com.learning.inventory_manager.core.processor.warehouse.RegisterWarehouseProcessorRequest;
import com.learning.inventory_manager.core.processor.warehouse.RegisterWarehouseProcessorResponse;
import com.learning.inventory_manager.exception.InventoryValidationException;
import com.learning.inventory_manager.gateway.WarehouseGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class RegisterWarehouseTests {

    @Autowired
    RegisterWarehouseProcessor processor;

    @Autowired
    WarehouseGateway warehouses;

    Warehouse warehouse(UUID id, UUID supplier, String code) {
        return new Warehouse(id, supplier, code, "Bengaluru Main", "Bengaluru", "KA", "IN", true);
    }

    RegisterWarehouseProcessorResponse register(Warehouse w) {
        return processor.process(RegisterWarehouseProcessorRequest.builder().warehouse(w).build());
    }

    @Test
    void registeredWarehouseIsValidForItsSupplierOnly() {
        var id = UUID.randomUUID();
        var supplier = UUID.randomUUID();

        var result = register(warehouse(id, supplier, "W-" + id));

        assertThat(result.isCreated()).isTrue();
        warehouses.validateWarehouseBelongsToSupplier(id, supplier);
        assertThatThrownBy(() -> warehouses.validateWarehouseBelongsToSupplier(id, UUID.randomUUID()))
                .isInstanceOf(com.learning.inventory_manager.exception.WarehouseNotFoundException.class);
    }

    @Test
    void registeringAgainRefreshesDetailsAndCanDeactivate() {
        var id = UUID.randomUUID();
        var supplier = UUID.randomUUID();
        register(warehouse(id, supplier, "W-" + id));

        var again = register(new Warehouse(id, supplier, "W-" + id, "Renamed", "Mysuru", "KA", "IN", false));

        assertThat(again.isCreated()).isFalse();
        assertThat(warehouses.findById(id)).get().satisfies(w -> {
            assertThat(w.name()).isEqualTo("Renamed");
            assertThat(w.active()).isFalse();
        });
        assertThatThrownBy(() -> warehouses.validateWarehouseBelongsToSupplier(id, supplier))
                .isInstanceOf(com.learning.inventory_manager.exception.WarehouseNotFoundException.class);
    }

    @Test
    void warehouseCannotMoveToAnotherSupplier() {
        var id = UUID.randomUUID();
        register(warehouse(id, UUID.randomUUID(), "W-" + id));

        assertThatThrownBy(() -> register(warehouse(id, UUID.randomUUID(), "W-" + id)))
                .isInstanceOf(InventoryValidationException.class).hasMessageContaining("another supplier");
    }

    @Test
    void codeUsedByAnotherWarehouseIsRejected() {
        var first = UUID.randomUUID();
        register(warehouse(first, UUID.randomUUID(), "SHARED-" + first));

        assertThatThrownBy(() -> register(warehouse(UUID.randomUUID(), UUID.randomUUID(), "SHARED-" + first)))
                .isInstanceOf(InventoryValidationException.class).hasMessageContaining("already used");
    }
}
