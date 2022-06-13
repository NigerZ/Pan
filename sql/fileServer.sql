create database fileServer;
use fileServer;
create table user(
                     id bigint primary key comment '主键id',
                     name varchar(50) null comment '昵称',
                     account varchar(50) not null comment '账号',
                     password varchar(255) not null comment '密码',
                     createTime datetime null comment '创建时间',
                     updateTime datetime null comment '修改时间',
                     is_Deleted int null comment '逻辑删除'
);