create table users
(
    id               uuid primary key      default gen_random_uuid(),
    username         varchar(255) not null unique,
    email            varchar(255) not null unique,
    first_name       varchar(100) not null,
    middle_name      varchar(100),
    last_name        varchar(100) not null,
    keycloak_user_id varchar(255) not null unique,
    created_at       timestamptz  not null default current_timestamp,
    updated_at       timestamptz  not null default current_timestamp
);

create table customer
(
    id uuid primary key     default gen_random_uuid(),
    user_id                          uuid        not null unique,
    phone                            varchar(30),
    additional_phones                text[],
    default_communication_preference varchar(20) not null,
    active                           boolean     not null default true,

    created_at                       timestamptz not null default current_timestamp,
    updated_at                       timestamptz not null default current_timestamp,

    constraint fk_customer_user
        foreign key (user_id) references users (id) on delete cascade
);

create table shipping_address
(
    id            uuid primary key      default gen_random_uuid(),
    customer_id   uuid         not null,
    first_name    varchar(100) not null,
    last_name     varchar(100) not null,
    company_name  varchar(255),
    address_line1 varchar(255) not null,
    address_line2 varchar(255),
    address_line3 varchar(255),
    city          varchar(100) not null,
    state         varchar(100),
    postal_code   varchar(20)  not null,
    country       varchar(100) not null,
    phone         varchar(30),
    email         varchar(255),

    created_at    timestamptz  not null default current_timestamp,
    updated_at    timestamptz  not null default current_timestamp,

    constraint fk_shipping_address_customer
        foreign key (customer_id) references customer (id) on delete cascade
);

create index idx_shipping_address_customer_id
    on shipping_address (customer_id);