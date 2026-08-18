# rcs-system

RCS 系统后端项目，基于 Spring Boot + Kotlin 构建。

## 技术栈

| 组件 | 版本 |
| --- | --- |
| Spring Boot | 4.1.0 |
| Kotlin | 2.3.21 |
| JDK | 17 |
| 构建工具 | Maven（自带 mvnw wrapper） |

## 已集成依赖

- Spring Web MVC（REST API）
- Spring Data JPA + MySQL
- Spring Data Redis
- Validation（参数校验）
- Jackson 3（Kotlin 序列化）

## 项目结构

```
src/main/kotlin/com/rcs/
├── RcsSystemApplication.kt           # 启动类
└── controller/
    └── HelloController.kt            # 示例 REST 接口
src/main/resources/
└── application.yml                   # 数据源 / Redis / 服务配置
```

## 运行方式

1. 确保本机 MySQL 已启动，且 `rcs_system` 库存在（`ddl-auto: update` 会自动建表）：

```sql
CREATE DATABASE IF NOT EXISTS rcs_system DEFAULT CHARACTER SET utf8mb4;
```

2. 编译打包：

```bash
./mvnw -DskipTests package
```

3. 运行：

```bash
./mvnw spring-boot:run
```

4. 验证接口：

```bash
curl http://localhost:8080/api/hello
```

## 说明

- 默认连接本机 MySQL（`localhost:3306`，账号 `root/abc123`，库 `rcs_system`）和 Redis（`localhost:6379`），按需修改 `application.yml`。
- Redis 未安装不影响启动（懒连接），用到缓存相关功能时才需要。
- `ddl-auto: update` 会自动根据实体建表，上线前建议改为 `validate` 或 `none`。
