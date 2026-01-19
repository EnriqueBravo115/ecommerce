CREATE TABLE address
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES customer (id) ON DELETE CASCADE,
    country     VARCHAR(100) NOT NULL,
    state       VARCHAR(100) NOT NULL,
    city        VARCHAR(100) NOT NULL,
    street      VARCHAR(255) NOT NULL,
    postal_code VARCHAR(20),
    is_primary  BOOLEAN   DEFAULT FALSE,
    created_at  TIMESTAMP DEFAULT current_timestamp
);
