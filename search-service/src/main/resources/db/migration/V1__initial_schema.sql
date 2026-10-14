CREATE TABLE search_product
(
    product_id     UUID PRIMARY KEY,
    name           VARCHAR(200),
    description    VARCHAR(4000),
    price          NUMERIC(12, 2),
    image_url      VARCHAR(1000),
    status         VARCHAR(16),
    in_stock       BOOLEAN       NOT NULL DEFAULT FALSE,
    average_rating NUMERIC(3, 2) NOT NULL DEFAULT 0,
    review_count   BIGINT        NOT NULL DEFAULT 0,
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX search_product_active_price_idx ON search_product (price) WHERE status = 'ACTIVE';
