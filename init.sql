create table currency
(
    id                bigserial
        constraint currencies_pk
            primary key,
    uuid              uuid                                   not null,
    symbol            varchar(255)                           not null,
    contract          varchar(255) default NULL::character varying,
    decimal           integer      default 18                not null,
    created_at        timestamp    default CURRENT_TIMESTAMP not null,
    updated_at        timestamp    default CURRENT_TIMESTAMP not null,
    deleted_at        date,
    last_block_number bigint       default 0                 not null
);

alter table currency
    owner to root;

create table wallet
(
    id                bigserial
        constraint wallets_pk
            primary key,
    uuid              uuid,
    address           varchar(255)                          not null,
    balance           numeric(30) default 0                 not null,
    share             varchar(255)                          not null,
    confirmed_balance numeric(30) default 0                 not null,
    spendable_balance numeric(30) default 0                 not null,
    created_at        timestamp   default CURRENT_TIMESTAMP not null,
    updated_at        timestamp   default CURRENT_TIMESTAMP not null,
    deleted_at        date,
    label             varchar(255)                          not null,
    currency_id       bigint                                not null
        constraint wallet_currency_id_fk
            references currency,
    nonce             bigint      default 0                 not null,
    path              integer     default 0                 not null
);

alter table wallet
    owner to root;

create unique index currencies_contract_uindex
    on currency (contract);

create unique index currencies_symbol_uindex
    on currency (symbol);

create unique index currencies_uuid_uindex
    on currency (uuid);

create table transaction
(
    id           bigserial
        constraint transaction_pk
            primary key,
    hash         varchar(255)                        not null,
    block_number bigint,
    block_hash   varchar(255),
    confirmation integer   default 0                 not null,
    created_at   timestamp default CURRENT_TIMESTAMP not null,
    updated_at   timestamp default CURRENT_TIMESTAMP not null,
    deleted_at   timestamp
);

alter table transaction
    owner to root;

create unique index transaction_hash_uindex
    on transaction (hash);

create unique index transaction_id_uindex
    on transaction (id);

create table withdrawal_transfer
(
    id             bigserial
        constraint withdrawal_transfer_pk
            primary key,
    uuid           uuid                                not null,
    amount         numeric(30)                         not null,
    gas_price      bigint                              not null,
    gas_limit      bigint    default 21000             not null,
    address        varchar(255),
    wallet_id      bigint                              not null
        constraint withdrawal_transfer_wallet_id_fk
            references wallet,
    transaction_id bigint
        constraint withdrawal_transfer_transaction_id_fk
            references transaction,
    currency_id    bigint                              not null
        constraint withdrawal_transfer_currency_id_fk
            references currency,
    created_at     timestamp default CURRENT_TIMESTAMP not null,
    updated_at     timestamp default CURRENT_TIMESTAMP,
    deleted_at     timestamp,
    status         text                                not null
        constraint withdrawal_transfer_status_check
            check (status = ANY (ARRAY ['pending'::text, 'confirmed'::text, 'failed'::text]))
);

alter table withdrawal_transfer
    owner to root;

create unique index withdrawal_transfer_id_uindex
    on withdrawal_transfer (id);

create table deposit_transfer
(
    id             bigserial
        constraint deposit_transfer_pk
            primary key,
    uuid           uuid                                not null,
    amount         numeric(30)                         not null,
    wallet_id      bigint                              not null
        constraint deposit_transfer_wallet_id_fk
            references wallet,
    transaction_id bigint
        constraint deposit_transfer_transaction_id_fk
            references transaction,
    currency_id    bigint                              not null
        constraint deposit_transfer_currency_id_fk
            references currency,
    created_at     timestamp default CURRENT_TIMESTAMP not null,
    updated_at     timestamp default CURRENT_TIMESTAMP not null,
    deleted_at     timestamp,
    status         text                                not null
        constraint deposit_transfer_status_check
            check (status = ANY (ARRAY ['pending'::text, 'confirmed'::text, 'failed'::text]))
);

alter table deposit_transfer
    owner to root;

create unique index deposit_transfer_id_uindex
    on deposit_transfer (id);

create unique index deposit_transfer_uindex
    on deposit_transfer (currency_id, wallet_id, transaction_id, amount);