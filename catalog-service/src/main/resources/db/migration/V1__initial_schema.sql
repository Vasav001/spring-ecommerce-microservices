/*

 tables:

 product, (product info, used by admin and other services; no user access)
 review, (comments, ratings)
 product_rating, (rating out of 5 for every product; calculated as avg rating i.e sum / total ratings)
 purchase (used to verity if customer can add review)

 other tables:


 */


CREATE TABLE product
(
    id          uuid PRIMARY KEY,
    sku         varchar(100)   NOT NULL,
    name        varchar(200)   NOT NULL,
    description text,
    brand       varchar(100),
    category    varchar(100)   NOT NULL,
    price       numeric(12, 2) NOT NULL CHECK (price >= 0),
    status      varchar(16)    NOT NULL CHECK (status IN ('DRAFT', 'ACTIVE', 'ARCHIVED')),
--     draft is incomplete product, archived is histpry/soft delete, customers only see active products
    version     bigint         NOT NULL,
--     hibernate can manage versions on its own; optimistic locking

    created_at  timestamptz    NOT NULL,
    updated_at  timestamptz    NOT NULL,
    created_by  varchar(255)   NOT NULL,
    updated_by  varchar(255)   NOT NULL
);

CREATE INDEX idx_product_status ON product (status, id);

CREATE UNIQUE INDEX idx_product_sku_active ON product (sku) WHERE status = 'ACTIVE';
-- soft deleted / inactive products may have same sku as an active so we can't make sku completely unique


CREATE TABLE review
(
    id          uuid PRIMARY KEY,
    product_id  uuid         NOT NULL REFERENCES product (id),
    user_id     varchar(255) NOT NULL,
    author_name varchar(100) NOT NULL,                                           -- current user name
    rating      smallint     NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment     text,
    status      varchar(16)  NOT NULL CHECK (status IN ('PUBLISHED', 'HIDDEN')), -- soft delete
    version     bigint       NOT NULL,
    created_at  timestamptz  NOT NULL,
    updated_at  timestamptz  NOT NULL,
    UNIQUE (product_id, user_id)
);

CREATE INDEX idx_review_product ON review (product_id, created_at DESC, id) WHERE status = 'PUBLISHED';


CREATE TABLE product_rating
(
    product_id   uuid PRIMARY KEY REFERENCES product (id),
    rating_count int         NOT NULL CHECK (rating_count >= 0),
    rating_sum   int         NOT NULL CHECK (rating_sum >= 0),
    version      bigint      NOT NULL,
    updated_at   timestamptz NOT NULL
);


-- TODO negative cache

CREATE TABLE purchase
(
    user_id    varchar(255) NOT NULL,
    product_id uuid         NOT NULL REFERENCES product (id),
    order_id   uuid         NOT NULL,
    created_at timestamptz  NOT NULL,
    PRIMARY KEY (user_id, product_id)
);