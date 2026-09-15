create database if not exists vibe_admin default character set utf8mb4 collate utf8mb4_unicode_ci;
use vibe_admin;

drop table if exists sys_role_menu;
drop table if exists sys_user_role;
drop table if exists sys_menu;
drop table if exists sys_role;
drop table if exists sys_user;

create table sys_user
(
    id          bigint primary key auto_increment comment '用户ID',
    username    varchar(64)  not null comment '用户名',
    password    varchar(100) not null comment '密码',
    nickname    varchar(64)  null comment '昵称',
    email       varchar(128) null comment '邮箱',
    phone       varchar(32)  null comment '手机号',
    status      tinyint      not null default 1 comment '状态：1启用 0禁用',
    create_time datetime     not null default current_timestamp comment '创建时间',
    update_time datetime     not null default current_timestamp on update current_timestamp comment '更新时间',
    deleted     tinyint      not null default 0 comment '逻辑删除：0未删除 1已删除',
    unique key uk_sys_user_username (username)
) engine = innodb default charset = utf8mb4 comment = '系统用户表';

create table sys_role
(
    id          bigint primary key auto_increment comment '角色ID',
    role_name   varchar(64) not null comment '角色名称',
    role_key    varchar(64) not null comment '角色标识',
    sort        int         not null default 0 comment '排序',
    status      tinyint     not null default 1 comment '状态：1启用 0禁用',
    create_time datetime    not null default current_timestamp comment '创建时间',
    update_time datetime    not null default current_timestamp on update current_timestamp comment '更新时间',
    deleted     tinyint     not null default 0 comment '逻辑删除：0未删除 1已删除',
    unique key uk_sys_role_key (role_key)
) engine = innodb default charset = utf8mb4 comment = '系统角色表';

create table sys_menu
(
    id          bigint primary key auto_increment comment '菜单ID',
    parent_id   bigint       not null default 0 comment '父菜单ID',
    menu_name   varchar(64)  not null comment '菜单名称',
    path        varchar(128) null comment '路由路径',
    component   varchar(128) null comment '组件路径',
    perms       varchar(128) null comment '权限标识',
    icon        varchar(64)  null comment '图标',
    menu_type   tinyint      not null default 0 comment '类型：0目录 1菜单 2按钮',
    visible     tinyint      not null default 1 comment '显示：1显示 0隐藏',
    sort        int          not null default 0 comment '排序',
    status      tinyint      not null default 1 comment '状态：1启用 0禁用',
    create_time datetime     not null default current_timestamp comment '创建时间',
    update_time datetime     not null default current_timestamp on update current_timestamp comment '更新时间',
    deleted     tinyint      not null default 0 comment '逻辑删除：0未删除 1已删除',
    key idx_sys_menu_parent (parent_id)
) engine = innodb default charset = utf8mb4 comment = '系统菜单权限表';

create table sys_user_role
(
    user_id bigint not null comment '用户ID',
    role_id bigint not null comment '角色ID',
    primary key (user_id, role_id)
) engine = innodb default charset = utf8mb4 comment = '用户角色关联表';

create table sys_role_menu
(
    role_id bigint not null comment '角色ID',
    menu_id bigint not null comment '菜单ID',
    primary key (role_id, menu_id)
) engine = innodb default charset = utf8mb4 comment = '角色菜单关联表';

insert into sys_user (id, username, password, nickname, email, phone, status, deleted)
values (1, 'admin', '$2a$10$4jUpFzFXnvwCqJFdoo9Rf.IGJz2m8X1ucPSuLcCCwRok8NWbxlnhm', '超级管理员', 'admin@example.com', '13800000000', 1, 0);

insert into sys_role (id, role_name, role_key, sort, status, deleted)
values (1, '超级管理员', 'admin', 1, 1, 0);

insert into sys_menu (id, parent_id, menu_name, path, component, perms, icon, menu_type, visible, sort, status, deleted)
values
    (1, 0, '系统管理', '/system', 'Layout', null, 'setting', 0, 1, 1, 1, 0),
    (2, 1, '用户管理', 'user', 'system/user/index', 'system:user:list', 'user', 1, 1, 1, 1, 0),
    (3, 1, '角色管理', 'role', 'system/role/index', 'system:role:list', 'peoples', 1, 1, 2, 1, 0),
    (4, 1, '菜单管理', 'menu', 'system/menu/index', 'system:menu:list', 'tree-table', 1, 1, 3, 1, 0),
    (101, 2, '用户查询', null, null, 'system:user:query', null, 2, 0, 1, 1, 0),
    (102, 2, '用户新增', null, null, 'system:user:add', null, 2, 0, 2, 1, 0),
    (103, 2, '用户修改', null, null, 'system:user:edit', null, 2, 0, 3, 1, 0),
    (104, 2, '用户删除', null, null, 'system:user:delete', null, 2, 0, 4, 1, 0),
    (201, 3, '角色查询', null, null, 'system:role:query', null, 2, 0, 1, 1, 0),
    (202, 3, '角色新增', null, null, 'system:role:add', null, 2, 0, 2, 1, 0),
    (203, 3, '角色修改', null, null, 'system:role:edit', null, 2, 0, 3, 1, 0),
    (204, 3, '角色删除', null, null, 'system:role:delete', null, 2, 0, 4, 1, 0),
    (301, 4, '菜单查询', null, null, 'system:menu:query', null, 2, 0, 1, 1, 0),
    (302, 4, '菜单新增', null, null, 'system:menu:add', null, 2, 0, 2, 1, 0),
    (303, 4, '菜单修改', null, null, 'system:menu:edit', null, 2, 0, 3, 1, 0),
    (304, 4, '菜单删除', null, null, 'system:menu:delete', null, 2, 0, 4, 1, 0);

insert into sys_user_role (user_id, role_id)
values (1, 1);

insert into sys_role_menu (role_id, menu_id)
select 1, id from sys_menu;
