DROP SCHEMA order_schema CASCADE;

CREATE SCHEMA IF NOT EXISTS order_schema;

CREATE TABLE IF NOT EXISTS order_schema.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    country_code VARCHAR(5),
    mobile_number VARCHAR(15),
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS order_schema.products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    available_qty INT NOT NULL DEFAULT 0,
    selling_price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS order_schema.orders (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    product_id UUID REFERENCES order_schema.products(id),
    quantity INT,
    amount DECIMAL(10, 2) NOT NULL,
    address TEXT,
    mobile_number VARCHAR(15),
    status VARCHAR(50),
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

INSERT INTO order_schema.products (id, name, available_qty, selling_price)
SELECT gen_random_uuid(), 'iPhone 15 Pro', 50, 999.99
WHERE NOT EXISTS (SELECT 1 FROM order_schema.products WHERE name = 'iPhone 15 Pro');

INSERT INTO order_schema.products (id, name, available_qty, selling_price)
SELECT gen_random_uuid(), 'MacBook Air M3', 30, 1299.99
WHERE NOT EXISTS (SELECT 1 FROM order_schema.products WHERE name = 'MacBook Air M3');

INSERT INTO order_schema.products (id, name, available_qty, selling_price)
SELECT gen_random_uuid(), 'AirPods Pro 2', 100, 249.99
WHERE NOT EXISTS (SELECT 1 FROM order_schema.products WHERE name = 'AirPods Pro 2');

INSERT INTO order_schema.products (id, name, available_qty, selling_price)
SELECT gen_random_uuid(), 'iPad Air', 45, 599.99
WHERE NOT EXISTS (SELECT 1 FROM order_schema.products WHERE name = 'iPad Air');

INSERT INTO order_schema.products (id, name, available_qty, selling_price)
SELECT gen_random_uuid(), 'Apple Watch Ultra', 25, 799.99
WHERE NOT EXISTS (SELECT 1 FROM order_schema.products WHERE name = 'Apple Watch Ultra');

INSERT INTO order_schema.products (id, name, available_qty, selling_price)
SELECT gen_random_uuid(), 'Samsung Galaxy S24', 60, 849.99
WHERE NOT EXISTS (SELECT 1 FROM order_schema.products WHERE name = 'Samsung Galaxy S24');

INSERT INTO order_schema.products (id, name, available_qty, selling_price)
SELECT gen_random_uuid(), 'Sony WH-1000XM5', 80, 349.99
WHERE NOT EXISTS (SELECT 1 FROM order_schema.products WHERE name = 'Sony WH-1000XM5');

INSERT INTO order_schema.products (id, name, available_qty, selling_price)
SELECT gen_random_uuid(), 'Dell XPS 15', 20, 1499.99
WHERE NOT EXISTS (SELECT 1 FROM order_schema.products WHERE name = 'Dell XPS 15');

INSERT INTO order_schema.products (id, name, available_qty, selling_price)
SELECT gen_random_uuid(), 'Nintendo Switch OLED', 70, 349.99
WHERE NOT EXISTS (SELECT 1 FROM order_schema.products WHERE name = 'Nintendo Switch OLED');

INSERT INTO order_schema.products (id, name, available_qty, selling_price)
SELECT gen_random_uuid(), 'Kindle Paperwhite', 120, 139.99
WHERE NOT EXISTS (SELECT 1 FROM order_schema.products WHERE name = 'Kindle Paperwhite');