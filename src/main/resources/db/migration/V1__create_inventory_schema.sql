CREATE TABLE warehouses (
    id UUID PRIMARY KEY,
    supplier_id UUID NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE inventory (
    id UUID PRIMARY KEY,
    supplier_product_id UUID NOT NULL,
    warehouse_id UUID NOT NULL,
    quantity_on_hand INT NOT NULL CHECK (quantity_on_hand >= 0),
    quantity_reserved INT NOT NULL DEFAULT 0 CHECK (quantity_reserved >= 0),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inventory_warehouse
        FOREIGN KEY (warehouse_id)
        REFERENCES warehouses(id),
    CONSTRAINT uq_supplier_product_warehouse
        UNIQUE (supplier_product_id, warehouse_id)
);

CREATE TABLE stock_reservations (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    order_item_id UUID NOT NULL,
    inventory_id UUID NOT NULL,
    supplier_product_id UUID NOT NULL,
    warehouse_id UUID NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    status VARCHAR(20) NOT NULL,
    expires_at TIMESTAMP,
    committed_at TIMESTAMP,
    released_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservation_inventory
        FOREIGN KEY (inventory_id)
        REFERENCES inventory(id)
);

CREATE TABLE inventory_ledger (
    id BIGSERIAL PRIMARY KEY,
    inventory_id UUID NOT NULL,
    change_type VARCHAR(50) NOT NULL,
    quantity_delta INT NOT NULL,
    quantity_after INT NOT NULL,
    reference_id VARCHAR(100),
    note VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ledger_inventory
        FOREIGN KEY (inventory_id)
        REFERENCES inventory(id)
);

