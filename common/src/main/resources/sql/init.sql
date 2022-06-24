create table user
(
    id           bigint      not null primary key,
    user_name    varchar(16) not null unique,
    password     varchar(64) not null,
    access_token varchar(64),
    create_time  datetime default current_timestamp,
    update_time  datetime default current_timestamp on update current_timestamp
);

create table user_fish
(
    id          bigint primary key auto_increment,
    user_id     bigint not null,
    fish_id     bigint not null,
    fish_status int    not null,
    create_time datetime default current_timestamp,
    unique key uf (user_id, fish_id)
);

create table user_property
(
    id             bigint primary key,
    exp            bigint not null default 0 comment '经验值，吃鱼获得',
    money          bigint not null default 0 comment '用来买鱼造鱼,提升鱼持有量',
    fish_max_count int    not null default 0 comment '最大持有鱼数量',
    update_time    datetime        default current_timestamp on update current_timestamp
);

create table fish_sell_log
(
    id          bigint primary key,
    seller_id   bigint      not null,
    seller_name varchar(16) not null,
    fish_id     bigint      not null,
    fish_name   varchar(16) not null,
    fish_detail json        not null,
    price       bigint      not null,
    create_time datetime default current_timestamp,
    update_time datetime default current_timestamp on update current_timestamp
);

create table fish_sold_out_log
(
    id          bigint primary key,
    order_id    bigint unique key,
    create_time datetime default current_timestamp,
    update_time datetime default current_timestamp on update current_timestamp
);

create table fish_deal_history
(
    id          bigint primary key,
    seller_id   bigint      not null,
    seller_name varchar(16) not null,
    buyer_id    bigint      not null,
    buyer_name  varchar(16) not null,
    fish_id     bigint      not null,
    fish_name   varchar(16) not null,
    fish_detail json        not null,
    price       bigint      not null,
    create_time datetime default current_timestamp
);
