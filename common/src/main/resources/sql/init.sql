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