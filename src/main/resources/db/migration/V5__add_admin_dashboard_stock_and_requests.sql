CREATE TABLE warehouses (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    location VARCHAR(255),
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_warehouses_code UNIQUE (code)
);

INSERT INTO warehouses (code, name, location, status)
VALUES ('MAIN', 'Main Warehouse', 'Primary fulfillment center', 'ACTIVE');

ALTER TABLE products ADD COLUMN warehouse_id BIGINT NULL;
ALTER TABLE products ADD COLUMN minimum_stock_level INT NOT NULL DEFAULT 0;

UPDATE products
SET warehouse_id = (SELECT id FROM warehouses WHERE code = 'MAIN' ORDER BY id LIMIT 1);

ALTER TABLE products
    ADD CONSTRAINT fk_products_warehouse
    FOREIGN KEY (warehouse_id) REFERENCES warehouses (id);

CREATE INDEX idx_products_warehouse_stock ON products (warehouse_id, stock, minimum_stock_level);

CREATE TABLE order_requests (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    request_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    notes VARCHAR(1000),
    requested_amount DECIMAL(19, 2),
    admin_notes VARCHAR(1000),
    resolved_by BIGINT NULL,
    resolved_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_requests_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_order_requests_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_order_requests_resolved_by FOREIGN KEY (resolved_by) REFERENCES users (id)
);

CREATE INDEX idx_order_requests_user_status ON order_requests (user_id, status, request_type);
CREATE INDEX idx_order_requests_order ON order_requests (order_id);
