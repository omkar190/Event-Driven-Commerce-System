CREATE SCHEMA IF NOT EXISTS order_schema;

CREATE TABLE IF NOT EXISTS order_schema.orders (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    status VARCHAR(50),
    amount NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_schema.order_outbox_event (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_order_outbox_status_created
ON order_schema.order_outbox_event(status, created_at);