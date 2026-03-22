CREATE SCHEMA IF NOT EXISTS order_schema;

CREATE TABLE IF NOT EXISTS order_schema.orders (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    status VARCHAR(50),
    amount NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMP
);