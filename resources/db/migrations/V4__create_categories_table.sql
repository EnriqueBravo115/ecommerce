CREATE TABLE categories
(
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(255) NOT NULL UNIQUE,
    parent_id        BIGINT REFERENCES categories (id) ON DELETE SET NULL,
    active           BOOLEAN   DEFAULT TRUE,
    created_at       TIMESTAMP DEFAULT current_timestamp,
    updated_at       TIMESTAMP
);

CREATE INDEX idx_categories_parent_id ON categories(parent_id);
