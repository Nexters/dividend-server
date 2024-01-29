create table if not exists stock
(
    id               binary(16) primary key,
    price            double,
    volume           int,
    created_at       datetime(6),
    last_modified_at  datetime(6),
    exchange         varchar(10),
    ticker           varchar(50) not null unique,
    industry         varchar(255),
    name             varchar(255),
    sector           enum ('TECHNOLOGY', 'COMMUNICATION_SERVICES', 'HEALTHCARE', 'CONSUMER_CYCLICAL', 'CONSUMER_DEFENSIVE', 'BASIC_MATERIALS', 'FINANCIAL_SERVICES', 'INDUSTRIALS', 'REAL_ESTATE', 'ENERGY', 'UTILITIES', 'ETC')
) engine = innodb
  default charset = utf8mb4;

create table if not exists dividend
(
    id               binary(16)  not null,
    created_at       datetime(6),
    last_modified_at datetime(6),
    declaration_date datetime(6) not null,
    dividend         integer     not null,
    ex_dividend_date datetime(6) not null,
    payment_date     datetime(6) not null,
    stock_id         binary(16)  not null,
    FOREIGN KEY (stock_id) REFERENCES stock (id),
    primary key (id)
) engine = innodb
  default charset = utf8mb4;