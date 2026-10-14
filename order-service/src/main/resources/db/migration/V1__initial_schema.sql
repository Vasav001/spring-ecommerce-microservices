-- table used by outbox lib
CREATE TABLE outbox_record
(
    id           BIGSERIAL PRIMARY KEY,
    topic        VARCHAR(249) NOT NULL,
    record_key   VARCHAR(255),
    value        BYTEA        NOT NULL,
    headers      JSONB        NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    processed_at TIMESTAMPTZ
);

CREATE INDEX outbox_record_pending_idx ON outbox_record (id) WHERE processed_at IS NULL;

-- consumer idempotency
CREATE TABLE processed_event
(
    event_id     UUID PRIMARY KEY,
    consumer     VARCHAR(255) NOT NULL,
    processed_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE orders
(
    id               UUID PRIMARY KEY,
    customer_id      VARCHAR(255)   NOT NULL,
    status           VARCHAR(16)    NOT NULL,
    failure_reason   VARCHAR(64),
    total_amount     NUMERIC(12, 2) NOT NULL CHECK (total_amount >= 0),
    idempotency_key  VARCHAR(64)    NOT NULL,

    ship_name        VARCHAR(200)   NOT NULL,
    ship_phone       VARCHAR(32)    NOT NULL,
    ship_line1       VARCHAR(255)   NOT NULL,
    ship_line2       VARCHAR(255),
    ship_city        VARCHAR(100)   NOT NULL,
    ship_state       VARCHAR(100)   NOT NULL,
    ship_postal_code VARCHAR(20)    NOT NULL,
    ship_country     VARCHAR(2)     NOT NULL,

    created_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    UNIQUE (customer_id, idempotency_key)
);

CREATE INDEX orders_customer_idx ON orders (customer_id, created_at DESC);

CREATE TABLE order_item
(
    id           BIGSERIAL PRIMARY KEY,
    order_id     UUID           NOT NULL REFERENCES orders (id),
    product_id   UUID           NOT NULL,
    product_name VARCHAR(200)   NOT NULL,
    unit_price   NUMERIC(12, 2) NOT NULL CHECK (unit_price >= 0),
    quantity     INT            NOT NULL CHECK (quantity > 0),
    UNIQUE (order_id, product_id)
);

CREATE TABLE order_saga
(
    order_id      UUID PRIMARY KEY REFERENCES orders (id),
    step          VARCHAR(32),
    step_deadline TIMESTAMPTZ,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX order_saga_deadline_idx ON order_saga (step_deadline) WHERE step IS NOT NULL;
