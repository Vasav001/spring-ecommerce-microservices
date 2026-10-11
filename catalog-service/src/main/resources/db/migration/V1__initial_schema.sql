/*

 tables: product,

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
    status      varchar(16)    NOT NULL CHECK (status IN ('DRAFT', 'ACTIVE', 'ARCHIVED')), -- draft is incomplete product, archived is histpry/soft delete, customers only see active products
    version     bigint         NOT NULL, -- hibernate can manage versions on its own; optimistic locking

    created_at  timestamptz    NOT NULL,
    updated_at  timestamptz    NOT NULL,
    created_by  varchar(64)    NOT NULL,
    updated_by  varchar(64)    NOT NULL
);

CREATE INDEX idx_product_status ON product (status, id);

CREATE UNIQUE INDEX idx_product_sku_active ON product (sku) WHERE status = 'ACTIVE';
-- soft deleted / inactive products may have same sku as an active so we can't make sku completely unique

