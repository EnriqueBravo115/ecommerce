CREATE TABLE addresses (
    id          BIGSERIAL PRIMARY KEY,
    user_id     INTEGER REFERENCES users(id),
    country     VARCHAR(100),
    state       VARCHAR(100),
    city        VARCHAR(100) NOT NULL,
    street      VARCHAR(255) NOT NULL,
    postal_code VARCHAR(20)
);
