CREATE TABLE product
(
    id                BIGSERIAL PRIMARY KEY,
    seller_id         BIGINT REFERENCES seller (id) ON DELETE CASCADE,
    category_id       BIGINT REFERENCES category (id) ON DELETE SET NULL,
    sku               VARCHAR(100) NOT NULL UNIQUE,
    name              VARCHAR(255) NOT NULL,
    description       TEXT,
    short_description TEXT,
    price             DECIMAL(10, 2) NOT NULL,
    compare_at_price  DECIMAL(10, 2),
    cost_price        DECIMAL(10, 2),
    brand             VARCHAR(100),
    weight            DECIMAL(8, 2),
    weight_unit       VARCHAR(10),
    status            VARCHAR(50) NOT NULL DEFAULT 'draft',
    condition         VARCHAR(50) DEFAULT 'new',
    tags              VARCHAR(500),
    view_count        INTEGER DEFAULT 0,
    average_rating    DECIMAL(3, 2) DEFAULT 0,
    review_count      INTEGER DEFAULT 0,
    created_at        TIMESTAMP DEFAULT current_timestamp,
    updated_at        TIMESTAMP
);
