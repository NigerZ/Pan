create database fileServer;
use fileServer;
create table user
(
    id          bigint       not null comment '主键id'
        primary key,
    name        varchar(50)  null comment '昵称',
    account     varchar(50)  not null comment '账号',
    password    varchar(255) not null comment '密码',
    create_time datetime     null comment '创建时间',
    update_time datetime     null comment '修改时间',
    is_deleted  int          null comment '逻辑删除'
)
    comment '用户表';