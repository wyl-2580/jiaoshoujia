# 脚手架管理系统

基于 Spring Boot 3 + Vue 3 的通用单体项目脚手架，开箱即用，安全可靠。

## 技术栈

### 后端

| 技术 | 说明 |
|---|---|
| Spring Boot 3.3.x | 基础框架 |
| Spring Security 6.x + RSA-JWT | 认证鉴权 (RS256 非对称签名) |
| MyBatis-Plus 3.5.x | ORM 框架 |
| Knife4j 4.x | API 文档 (OpenAPI 3) |
| Caffeine / Redis | 缓存 (默认 Caffeine，可选 Redis) |
| Sentinel 1.8.x | 接口限流 |
| EasyExcel 4.x | Excel 导入导出 |
| Quartz | 定时任务 |
| Hutool + MapStruct | 工具库 |

### 前端

| 技术 | 说明 |
|---|---|
| Vue 3.5+ | 核心框架 (Composition API) |
| TypeScript | 开发语言 |
| Vite 6.x | 构建工具 |
| Element Plus | UI 组件库 |
| Pinia | 状态管理 |
| Vue Router 4 | 路由 |
| Axios | HTTP 客户端 |
| vue-i18n | 国际化 |

## 项目结构

```
jiaoshoujia/
├── jiaoshoujia-web/              # 后端 (多模块 Maven)
│   ├── jiaoshoujia-common/       #   公共基础 (工具类、常量、异常、注解)
│   ├── jiaoshoujia-framework/    #   框架核心 (Security、缓存、限流、AOP)
│   ├── jiaoshoujia-system/       #   系统管理 (用户、角色、菜单、部门、字典等)
│   ├── jiaoshoujia-generator/    #   代码生成器
│   └── jiaoshoujia-admin/        #   启动入口 (Controller + 配置文件)
├── jiaoshoujia-ui/               # 前端 (Vue 3)
├── sql/                          # 数据库脚本
│   ├── mysql/init.sql
│   └── postgresql/init.sql
└── README.md
```

## 模块详解

### jiaoshoujia-common（公共基础模块）

最底层的公共依赖，不依赖任何业务模块。

| 目录 | 内容 |
|---|---|
| `annotation/` | 自定义注解：`@Log`（操作日志）、`@DataScope`（数据权限）、`@RateLimiter`（限流）、`@Anonymous`（匿名访问） |
| `constant/` | 全局常量：`Constants`、`HttpStatus`、`UserConstants` |
| `core/` | 基础类：`R<T>`（统一响应体）、`BaseEntity`（基类）、`PageResult`（分页结果） |
| `enums/` | 枚举：`BusinessType`（业务类型）、`DataScopeType`、`OperatorType` |
| `exception/` | 异常体系：`BusinessException`、`UnauthorizedException` |
| `utils/` | 工具类：`SecurityUtils`、`StringUtils`、`IpUtils`、`ServletUtils`、`ExcelUtils`、`MessageUtils`（i18n） |

### jiaoshoujia-framework（框架核心模块）

横切关注点集中管理，为上层业务提供基础设施。

| 目录 | 内容 |
|---|---|
| `security/` | Spring Security 配置、JWT 过滤器、RSA 密钥加载、`LoginUser` 认证主体 |
| `cache/` | 缓存抽象：`CacheService` 接口 + `CaffeineCacheService` / `RedisCacheService` 双实现 |
| `aspectj/` | AOP 切面：`LogAspect`（操作日志）、`DataScopeAspect`+`DataScopeInnerInterceptor`（数据权限 SQL 拦截）、`RateLimiterAspect`（限流） |
| `web/` | Web 组件：`GlobalExceptionHandler`（全局异常处理）、`SecurityHeaderFilter`（安全响应头） |
| `xss/` | XSS 防护：请求参数自动过滤 |
| `config/` | 配置类：`MybatisPlusConfig`、`CorsConfig`、`Knife4jConfig`、`SentinelConfig`、`QuartzConfig`、`I18nConfig`（国际化） |

### jiaoshoujia-system（系统管理模块）

核心业务代码，包含完整的后台管理功能。

| 功能 | 关键类 |
|---|---|
| 用户管理 | `SysUser` / `ISysUserService` — 增删改查、角色分配、密码重置 |
| 角色管理 | `SysRole` / `ISysRoleService` — 增删改查、菜单权限分配 |
| 菜单管理 | `SysMenu` / `ISysMenuService` — 树形菜单、按钮权限 |
| 部门管理 | `SysDept` / `ISysDeptService` — 组织架构树 |
| 字典管理 | `SysDictType` / `SysDictData` — 下拉选项维护 |
| 操作日志 | `SysOperLog` + `OperLogEventListener` — 异步落库，支持导出 |
| 定时任务 | `SysJob` — Quartz 可视化管理 |

### jiaoshoujia-generator（代码生成模块）

读取数据库表结构，使用 Velocity 模板引擎生成前后端 CRUD 代码。

生成的代码自动包含 `@Log`、`@PreAuthorize` 等注解，遵循项目规范。

### jiaoshoujia-admin（启动入口模块）

Spring Boot 启动类、所有 REST Controller 和配置文件（`application.yml`、RSA 密钥、i18n 资源）所在模块。

**依赖方向**：`admin` → `system` + `generator` → `framework` → `common`

## 环境要求

| 环境 | 版本 |
|---|---|
| JDK | 17+ |
| Maven | 3.8+ |
| Node.js | 18+ |
| MySQL | 8.0+ 或 PostgreSQL 14+ |

## 快速开始

### 1. 初始化数据库

```bash
# MySQL
mysql -u root -p < sql/mysql/init.sql

# 或 PostgreSQL
psql -U postgres -f sql/postgresql/init.sql
```

### 2. 配置后端

数据库连接通过环境变量配置（也可直接修改 application-mysql.yml）：

```bash
# Linux/Mac
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=jiaoshoujia
export DB_USER=root
export DB_PASSWORD=your_password

# Windows PowerShell
$env:DB_HOST="localhost"
$env:DB_PORT="3306"
$env:DB_NAME="jiaoshoujia"
$env:DB_USER="root"
$env:DB_PASSWORD="your_password"
```

切换数据库类型：修改 `application.yml` 中的 `spring.profiles.active`：

```yaml
spring:
  profiles:
    active: dev,mysql    # 使用 MySQL
    # active: dev,pg     # 使用 PostgreSQL
```

### 3. 启动后端

```bash
cd jiaoshoujia-web
mvn clean install
cd jiaoshoujia-admin
mvn spring-boot:run
```

后端启动后访问：
- API: http://localhost:8080/api
- 接口文档: http://localhost:8080/api/doc.html

### 4. 启动前端

```bash
cd jiaoshoujia-ui
npm install
npm run dev
```

前端访问地址: http://localhost:3000

### 5. 默认账号

| 账号 | 密码 | 角色 |
|---|---|---|
| admin | admin123 | 超级管理员 |
| user | admin123 | 普通用户 |

## 内置功能

- **用户管理**: 用户的增删改查、角色分配、密码重置
- **角色管理**: 角色的增删改查、菜单权限分配、数据权限设置
- **菜单管理**: 目录/菜单/按钮的树形管理
- **部门管理**: 组织架构的树形管理
- **字典管理**: 系统字典的维护（如性别、状态等下拉选项）
- **操作日志**: 系统操作的审计日志记录与查询，支持导出
- **定时任务**: Quartz 定时任务的可视化管理
- **代码生成**: 读取数据库表结构，一键生成前后端代码

## 安全特性

- **RSA-JWT (RS256)**: 非对称签名，私钥签发 / 公钥验证，防 Token 伪造
- **双 Token**: accessToken (30min) + refreshToken (7d)
- **RBAC 权限**: 用户-角色-菜单三级权限控制，支持按钮级权限
- **数据权限**: @DataScope 注解，部门级数据过滤
- **XSS 过滤**: 自动清洗请求参数中的危险脚本
- **Sentinel 限流**: 接口级 QPS 控制，防恶意刷接口
- **登录保护**: 连续失败锁定账户，支持验证码
- **密码安全**: BCrypt 加密存储
- **操作审计**: @Log 注解自动记录敏感操作
- **安全响应头**: X-Frame-Options, CSP, X-Content-Type-Options 等

## 国际化 (i18n)

项目支持中英文切换，通过 HTTP 请求头 `Accept-Language` 控制后端语言。

### 后端

- 资源文件位于 `jiaoshoujia-admin/src/main/resources/i18n/`
- `messages.properties` — 中文（默认）
- `messages_en.properties` — 英文
- 使用方式：`MessageUtils.message("user.username.exists", username)`

### 前端

- 使用 `vue-i18n`，语言文件位于 `jiaoshoujia-ui/src/i18n/lang/`
- 使用方式：`{{ $t('common.add') }}`
- 通过 `localStorage` 持久化用户语言选择

### 添加新语言

1. 后端：新建 `messages_xx.properties`（如 `messages_ja.properties`）
2. 前端：在 `src/i18n/lang/` 下新建语言文件，在 `src/i18n/index.ts` 中注册

## 二次开发指南

### 新增业务模块

1. **创建数据库表**并在 `sql/` 下更新初始化脚本
2. **使用代码生成器**：访问管理界面「代码生成」，导入表结构，一键生成 CRUD 代码
3. **复制生成的代码**到对应模块目录
4. **添加菜单权限**：在「菜单管理」中添加对应的目录/菜单/按钮

### 新增自定义注解

参考现有注解实现（`@Log`、`@DataScope`、`@RateLimiter`），步骤：

1. 在 `jiaoshoujia-common/annotation/` 下定义注解
2. 在 `jiaoshoujia-framework/aspectj/` 下实现 AOP 切面
3. 在业务 Controller/Service 上使用注解

### 切换缓存实现

框架提供 `CacheService` 接口的两套实现，通过配置切换：

```yaml
app:
  cache:
    type: caffeine   # 默认，零依赖
    # type: redis    # 需要 Redis 服务
```

自定义实现：实现 `CacheService` 接口并注册为 Spring Bean 即可。

### 数据权限扩展

在 Service 方法上添加 `@DataScope` 注解：

```java
@DataScope(deptAlias = "your_table")
public List<YourEntity> selectList(YourEntity query) {
    // 框架自动在 SQL 中追加部门过滤条件
}
```

支持的数据范围类型在 `DataScopeType` 枚举中定义。

### 运行单元测试

```bash
cd jiaoshoujia-web
mvn test
```

测试文件位于各模块的 `src/test/java/` 目录，使用 JUnit 5 + Mockito。

## 缓存配置

默认使用 Caffeine 本地缓存（零中间件依赖）。如需切换到 Redis：

1. 修改 `application.yml`：
```yaml
app:
  cache:
    type: redis  # 改为 redis
```

2. 添加 Redis 配置：
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:
```

## RSA 密钥管理

项目内置了开发用密钥对（`resources/keys/`）。**生产环境必须重新生成**：

```bash
# 生成 RSA 2048 位私钥
openssl genpkey -algorithm RSA -out private.pem -pkeyopt rsa_keygen_bits:2048

# 导出公钥
openssl rsa -pubout -in private.pem -out public.pem
```

将生成的文件替换 `jiaoshoujia-admin/src/main/resources/keys/` 下的对应文件。

## 配置加密 (可选)

如需在配置文件中加密敏感信息，可启用 Jasypt：

1. 在 `jiaoshoujia-admin/pom.xml` 中取消 jasypt 依赖的 optional 标记
2. 使用 Jasypt 加密工具生成密文
3. 在 yml 中使用 `ENC(密文)` 格式
4. 启动时传入加密密钥：`--jasypt.encryptor.password=your-secret`

## 生产部署

### JAR 部署

```bash
cd jiaoshoujia-web
mvn clean package -DskipTests

# 启动
java -jar jiaoshoujia-admin/target/jiaoshoujia-admin.jar \
  --spring.profiles.active=prod,mysql
```

### Docker 部署

```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY jiaoshoujia-admin/target/jiaoshoujia-admin.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
docker build -t jiaoshoujia:latest .
docker run -d -p 8080:8080 \
  -e DB_HOST=your-db-host \
  -e DB_PORT=3306 \
  -e DB_NAME=jiaoshoujia \
  -e DB_USER=root \
  -e DB_PASSWORD=your_password \
  --name jiaoshoujia jiaoshoujia:latest
```

### 前端部署

```bash
cd jiaoshoujia-ui
npm run build
```

将 `dist/` 目录部署到 Nginx，配置反向代理：

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 生产环境清单

| 项目 | 说明 |
|---|---|
| RSA 密钥 | 必须重新生成，替换 `resources/keys/` |
| 数据库密码 | 使用环境变量或 Jasypt 加密，禁止明文 |
| Knife4j | 生产环境建议关闭：`knife4j.enable: false` |
| 日志级别 | 调整为 WARN/ERROR |
| CORS | 配置允许的具体域名，禁止 `*` |
| HTTPS | 使用 Nginx 反向代理 + TLS 证书 |

## License

MIT
