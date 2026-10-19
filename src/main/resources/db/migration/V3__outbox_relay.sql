-- The outbox relay records how often a row failed to publish and why.
ALTER TABLE inventory_outbox ADD (
    attempts   NUMBER(10)      DEFAULT 0 NOT NULL,
    last_error VARCHAR2(1000)
);
