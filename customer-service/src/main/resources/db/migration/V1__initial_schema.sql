CREATE TABLE customer
(
    id                  VARCHAR(255) PRIMARY KEY,
    profile_picture_url VARCHAR(1000),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE address
(
    id          UUID PRIMARY KEY,
    customer_id VARCHAR(255) NOT NULL REFERENCES customer (id),
    name        VARCHAR(200) NOT NULL,
    phone       VARCHAR(32)  NOT NULL,
    line1       VARCHAR(255) NOT NULL,
    line2       VARCHAR(255),
    city        VARCHAR(100) NOT NULL,
    state       VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20)  NOT NULL,
    country     VARCHAR(2)   NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX address_customer_idx ON address (customer_id);
