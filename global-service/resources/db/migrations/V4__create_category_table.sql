CREATE TABLE category
(
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(255) NOT NULL UNIQUE,
    parent_id        BIGINT REFERENCES category (id) ON DELETE SET NULL,
    active           BOOLEAN   DEFAULT TRUE,
    created_at       TIMESTAMP DEFAULT current_timestamp,
    updated_at       TIMESTAMP
);
