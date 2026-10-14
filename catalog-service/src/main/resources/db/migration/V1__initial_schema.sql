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
CREATE TABLE product
(
    id          UUID PRIMARY KEY,
    name        VARCHAR(200)   NOT NULL,
    description VARCHAR(4000),
    price       NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    image_url   VARCHAR(1000),
    status      VARCHAR(16)    NOT NULL,
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE TABLE purchased_item
(
    order_id    UUID         NOT NULL,
    product_id  UUID         NOT NULL,
    customer_id VARCHAR(255) NOT NULL,
    delivered   BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (order_id, product_id)
);

CREATE INDEX purchased_item_customer_product_idx ON purchased_item (customer_id, product_id);

CREATE TABLE review
(
    id          UUID PRIMARY KEY,
    product_id  UUID         NOT NULL REFERENCES product (id),
    customer_id VARCHAR(255) NOT NULL,
    author_name VARCHAR(200) NOT NULL,
    rating      INT          NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment     VARCHAR(2000),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (product_id, customer_id)
);
