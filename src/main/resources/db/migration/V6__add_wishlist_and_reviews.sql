CREATE TABLE wishlists (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_wishlists_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_wishlists_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT uk_wishlists_user_product UNIQUE (user_id, product_id)
);

CREATE TABLE product_reviews (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating INT NOT NULL,
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_reviews_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_product_reviews_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_product_reviews_product ON product_reviews (product_id);
CREATE INDEX idx_product_reviews_user ON product_reviews (user_id);
