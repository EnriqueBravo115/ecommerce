CREATE TABLE shopping_cart
(
    id         BIGSERIAL PRIMARY KEY,
    customer_id BIGINT REFERENCES customer (id) ON DELETE CASCADE,
    product_id BIGINT REFERENCES products (id) ON DELETE CASCADE,
    quantity   INTEGER NOT NULL DEFAULT 1,
    added_at   TIMESTAMP DEFAULT current_timestamp,
    updated_at TIMESTAMP,
    
    UNIQUE(customer_id, product_id)
);

CREATE INDEX idx_shopping_cart_customer_id ON shopping_cart(customer_id);
CREATE INDEX idx_shopping_cart_product_id ON shopping_cart(product_id);
