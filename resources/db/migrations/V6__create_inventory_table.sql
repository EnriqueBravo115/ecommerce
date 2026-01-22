CREATE TABLE inventory
(
    id             BIGSERIAL PRIMARY KEY,
    product_id     BIGINT REFERENCES product (id) ON DELETE CASCADE,
    sku            VARCHAR(100) UNIQUE,
    quantity       INTEGER NOT NULL DEFAULT 0,
    reserved       INTEGER NOT NULL DEFAULT 0,
    location       VARCHAR(100),
    reorder_point  INTEGER DEFAULT 10,
    low_stock_threshold INTEGER DEFAULT 5,
    last_restocked TIMESTAMP,
    created_at     TIMESTAMP DEFAULT current_timestamp,
    updated_at     TIMESTAMP
);
