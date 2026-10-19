# Vibe Admin

Java 17 + Spring Boot 3 + Spring Security + JWT + MyBatis-Plus + MySQL + Redis + Vue 3 + Vite + Element Plus 后台管理脚手架。

## 默认账号

- 用户名：`admin`
- 密码：`123456`

## 初始化数据库

使用你本机的 MySQL 环境执行 `sql/vibe_admin.sql`，脚本中包含建库、建表、默认管理员、默认角色和菜单权限初始化数据。

完整安装还需在同一数据库依次执行 `sql/sys_file.sql`、`sql/sys_oper_log.sql`、`sql/sys_dict.sql`，最后执行 `sql/migrations/20261019_file_security.sql`。
前三个补充安装脚本包含重建表语句，仅用于首次安装；已有环境升级时只执行增量脚本，具体行为见 [安全修复升级说明](docs/security-fixes-2026-10-19.md)。

## 启动后端

```bash
cd admin
mvn spring-boot:run
```

后端默认地址：`http://localhost:8080/api`

## 启动前端

```bash
cd web
npm install
npm run dev
```

前端默认地址：`http://localhost:5173`

## 常用接口

- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/system/user/profile`
- `GET /api/system/menu/tree`
- `GET /api/system/user`
- `GET /api/system/role`
- `GET /api/system/menu`
