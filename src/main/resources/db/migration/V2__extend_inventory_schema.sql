-- V2: Extend inventory schema to support full TDD specification
-- Adds: safety_stock/reorder_point to inventory, idempotency_key/version to reservations,
--       extended ledger fields, and inventory_outbox table.

-- ========================================================================
-- 1. Extend inventory table
-- ========================================================================
ALTER TABLE inventory
    ADD COLUMN safety_stock  INT NOT NULL DEFAULT 0,
    ADD COLUMN reorder_point INT NOT NULL DEFAULT 0,
    ADD COLUMN last_restocked_at TIMESTAMP;

-- ========================================================================
-- 2. Extend stock_reservations table
-- ========================================================================
ALTER TABLE stock_reservations
    ADD COLUMN idempotency_key VARCHAR(80),
    ADD COLUMN version         BIGINT NOT NULL DEFAULT 0;

-- Back-fill idempotency_key for any existing rows (dev/test convenience)
UPDATE stock_reservations SET idempotency_key = id::text WHERE idempotency_key IS NULL;
ALTER TABLE stock_reservations ALTER COLUMN idempotency_key SET NOT NULL;
ALTER TABLE stock_reservations
    ADD CONSTRAINT uq_reservation_idempotency_key UNIQUE (idempotency_key);

-- ========================================================================
-- 3. Extend inventory_ledger table
-- ========================================================================
ALTER TABLE inventory_ledger
    ADD COLUMN reservation_id  UUID,
    ADD COLUMN reference_type  VARCHAR(30),
    ADD COLUMN actor           VARCHAR(80);

-- ========================================================================
-- 4. New table: inventory_outbox (Transactional Outbox pattern)
-- ========================================================================
CREATE TABLE inventory_outbox (
    id           BIGSERIAL    PRIMARY KEY,
    aggregate_id UUID         NOT NULL,
    event_type   VARCHAR(60)  NOT NULL,
    payload      TEXT         NOT NULL,  -- JSON; use JSONB in production PostgreSQL
    status       VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at      TIMESTAMP
);
CREATE INDEX idx_inv_outbox_pending ON inventory_outbox (status, created_at) WHERE status = 'PENDING';

-- ========================================================================
-- 5. Indexes for reservations (TDD §9.4)
-- ========================================================================
CREATE INDEX IF NOT EXISTS idx_resv_order      ON stock_reservations (order_id);
CREATE INDEX IF NOT EXISTS idx_resv_status_exp ON stock_reservations (status, expires_at);
CREATE INDEX IF NOT EXISTS idx_resv_inv        ON stock_reservations (inventory_id) WHERE status = 'HELD';

-- ========================================================================
-- 6. Low-stock index (TDD §9.4)
-- ========================================================================
CREATE INDEX IF NOT EXISTS idx_inv_supplier_product ON inventory (supplier_product_id);
CREATE INDEX IF NOT EXISTS idx_inv_low_stock ON inventory (supplier_product_id)
    WHERE (quantity_on_hand - quantity_reserved) <= safety_stock;
