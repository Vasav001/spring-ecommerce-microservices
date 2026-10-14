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

CREATE TABLE processed_event
(
    event_id     UUID PRIMARY KEY,
    consumer     VARCHAR(255) NOT NULL,
    processed_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE payment
(
    order_id       UUID PRIMARY KEY,
    customer_id    VARCHAR(255)   NOT NULL,
    amount         NUMERIC(12, 2) NOT NULL CHECK (amount >= 0),
    status         VARCHAR(16)    NOT NULL,
    failure_reason VARCHAR(64),
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE TABLE shipment
(
    order_id       UUID PRIMARY KEY,
    customer_id    VARCHAR(255) NOT NULL,
    status         VARCHAR(16)  NOT NULL,
    failure_reason VARCHAR(64),
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);
