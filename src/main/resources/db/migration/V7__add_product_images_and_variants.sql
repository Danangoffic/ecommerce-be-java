CREATE TABLE product_images (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE INDEX idx_product_images_product ON product_images (product_id, sort_order);

CREATE TABLE product_variants (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    sku VARCHAR(80) NOT NULL,
    size VARCHAR(50),
    color VARCHAR(50),
    price DECIMAL(19, 2),
    stock INT NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_variants_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT uk_product_variants_sku UNIQUE (sku)
);

CREATE INDEX idx_product_variants_product ON product_variants (product_id, status);

ALTER TABLE cart_items ADD COLUMN variant_id BIGINT NULL;
ALTER TABLE cart_items
    ADD CONSTRAINT fk_cart_items_variant
    FOREIGN KEY (variant_id) REFERENCES product_variants (id);

ALTER TABLE order_items ADD COLUMN variant_id BIGINT NULL;
ALTER TABLE order_items ADD COLUMN variant_sku VARCHAR(80) NULL;
ALTER TABLE order_items ADD COLUMN variant_label VARCHAR(120) NULL;
