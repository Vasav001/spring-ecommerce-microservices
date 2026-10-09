alter table customer
    add column version bigint not null default 0;

alter table shipping_address
    add column version bigint not null default 0;
