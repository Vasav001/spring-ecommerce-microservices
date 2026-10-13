CREATE TABLE stock_item
(
    product_id UUID PRIMARY KEY,
    on_hand    INT     NOT NULL DEFAULT 0,
    reserved   INT     NOT NULL DEFAULT 0,
    CONSTRAINT stock_item_quantities CHECK (reserved >= 0 AND on_hand >= reserved)
);

CREATE TABLE reservation
(
    id         BIGSERIAL PRIMARY KEY,
    order_id   UUID        NOT NULL,
    product_id UUID        NOT NULL,
    quantity   INT         NOT NULL CHECK (quantity > 0),
    status     VARCHAR(16) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (order_id, product_id)
);

CREATE INDEX reservation_expiry_idx ON reservation (expires_at) WHERE status = 'RESERVED';

CREATE TABLE processed_event
(
    event_id     UUID PRIMARY KEY,
    consumer     VARCHAR(255) NOT NULL,
    processed_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
