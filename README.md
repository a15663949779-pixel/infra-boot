# Vibe Admin

Java 17 + Spring Boot 3 + Spring Security + JWT + MyBatis-Plus + MySQL + Redis + Vue 3 + Vite + Element Plus 后台管理脚手架。

## 默认账号

- 用户名：`admin`
- 密码：`123456`

## 初始化数据库

使用你本机的 MySQL 环境执行 `sql/vibe_admin.sql`，脚本中包含建库、建表、默认管理员、默认角色和菜单权限初始化数据。

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
