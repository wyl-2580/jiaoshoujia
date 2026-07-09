# 脚手架管理系统

基于 Spring Boot 3 + Vue 3 的后台管理脚手架，内置**等保三权分立**（系统管理员 / 安全管理员 / 审计管理员），开箱即用，安全可靠。

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|---|---|---|
| Spring Boot | 3.3.6 | 基础框架 |
| Spring Security | 6.x | 认证鉴权 |
| RSA-JWT (RS256) | JJWT 0.12.6 | 非对称签名，双 Token（access + refresh） |
| MyBatis-Plus | 3.5.9 | ORM 框架 |
| Knife4j | 4.5.0 | API 文档 (OpenAPI 3) |
| Caffeine / Redis | 3.1.8 | 缓存（默认 Caffeine，可选 Redis） |
| Sentinel | 1.8.8 | 接口限流 |
| EasyExcel | 4.0.3 | Excel 导入导出 |
| Quartz | — | 定时任务 |
| Hutool | 5.8.32 | 工具库 |
| MapStruct | 1.6.3 | 对象映射 |

### 前端

| 技术 | 版本 | 说明 |
|---|---|---|
| Vue | 3.5+ | 核心框架 (Composition API) |
| TypeScript | 5.6 | 开发语言 |
| Vite | 6.x | 构建工具 |
| Element Plus | 2.9+ | UI 组件库 |
| Pinia | 2.3+ | 状态管理 |
| Vue Router | 4.5+ | 路由（动态路由 + 路由守卫） |
| Axios | 1.7+ | HTTP 客户端（自动刷新 Token） |
| vue-i18n | — | 国际化 |

## 项目结构

```
jiaoshoujia/
├── jiaoshoujia-web/              # 后端（多模块 Maven，Java 17）
│   ├── jiaoshoujia-common/       #   公共基础（工具类、常量、异常、注解）
│   ├── jiaoshoujia-framework/    #   框架核心（Security、缓存、限流、AOP）
│   └── jiaoshoujia-admin/        #   业务模块（Controller + Service + Domain + Mapper + 启动类）
├── jiaoshoujia-ui/               # 前端（Vue 3 + Element Plus）
├── sql/                          # 数据库脚本
│   └── mysql/init.sql
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
| `enums/` | 枚举：`BusinessType`（业务类型）、`OperatorType`（操作人类型） |
| `exception/` | 异常体系：`BusinessException`、`UnauthorizedException` |
| `utils/` | 工具类：`SecurityUtils`、`StringUtils`、`IpUtils`、`ServletUtils`、`ExcelUtils`、`MessageUtils`（i18n） |

### jiaoshoujia-framework（框架核心模块）

横切关注点集中管理，为上层业务提供基础设施。

| 目录 | 内容 |
|---|---|
| `security/` | Spring Security 配置、JWT 过滤器（`JwtAuthenticationFilter`）、RSA 密钥加载、`LoginUser` 认证主体、认证入口点 |
| `cache/` | 缓存抽象：`CacheService` 接口 + `CaffeineCacheService` / `RedisCacheService` 双实现，通过配置一键切换 |
| `aspectj/` | AOP 切面：`LogAspect`（操作日志自动记录）、`DataScopeAspect` + `DataScopeInnerInterceptor`（数据权限 SQL 拦截）、`RateLimiterAspect`（限流） |
| `web/` | Web 组件：`GlobalExceptionHandler`（全局异常处理）、`SecurityHeaderFilter`（安全响应头：X-Frame-Options、CSP 等） |
| `xss/` | XSS 防护：请求参数自动过滤 |
| `config/` | 配置类：`MybatisPlusConfig`（分页+数据权限插件）、`CorsConfig`、`Knife4jConfig`、`SentinelConfig`、`QuartzConfig`、`I18nConfig` |

### jiaoshoujia-admin（业务模块）

完整的业务模块，包含 Controller、Service、Domain、Mapper 等所有业务分层，同时也是 Spring Boot 启动入口。

#### 包结构

| 目录 | 内容 |
|---|---|
| `controller/` | REST Controller：认证、用户、角色、菜单、部门、字典、日志、任务、文件 |
| `service/` | 业务接口：`ISysUserService`、`ISysRoleService`、`ISysMenuService` 等 |
| `service/impl/` | 业务实现：`SysUserServiceImpl`、`SysRoleServiceImpl` 等 |
| `domain/` | 实体类：`SysUser`、`SysRole`、`SysMenu`、`SysDept`、`SysDictType`、`SysDictData`、`SysOperLog`、`SysLoginInfor`、`SysJob` 等 |
| `domain/dto/` | 传输对象：`SysUserQuery` |
| `mapper/` | MyBatis Mapper 接口 |
| `quartz/` | Quartz 定时任务调度：`ScheduleUtils`、`QuartzJobExecution` 等 |
| `quartz/task/` | 具体定时任务实现：`SampleTask` |
| `listener/` | 事件监听：`OperLogEventListener`（操作日志异步落库） |
| `security/` | 安全认证桥接：`UserDetailsServiceImpl`（Spring Security UserDetailsService 实现） |

#### 业务功能

| 功能 | 关键类 | 说明 |
|---|---|---|
| 用户管理 | `SysUserController` / `ISysUserService` | 增删改查、角色分配、密码重置、状态切换、导出、三员账号保护 |
| 角色管理 | `SysRoleController` / `ISysRoleService` | 增删改查、菜单权限分配、数据权限设置、三权分立校验 |
| 菜单管理 | `SysMenuController` / `ISysMenuService` | 树形菜单、按钮权限、动态路由，无万能超管——权限严格来自角色 |
| 部门管理 | `SysDeptController` / `ISysDeptService` | 组织架构树、祖级链维护、循环引用检测 |
| 字典管理 | `SysDictTypeController` / `SysDictDataController` | 下拉选项维护 |
| 操作日志 | `SysOperLogController` + `OperLogEventListener` | 异步落库，记录操作人/时间/IP/参数/耗时，支持导出 |
| 登录日志 | `SysLoginInforController` / `ISysLoginInforService` | 登录/登出自动记录，含浏览器/OS/IP，支持导出 |
| 定时任务 | `SysJobController` / `ISysJobService` | Quartz 可视化管理，启停、手动执行 |
| 认证授权 | `AuthController` | 登录、登出、刷新 Token、获取用户信息、获取动态路由、个人资料、修改密码 |
| 文件管理 | `FileController` | 文件上传、文件下载 |

**依赖方向**：`admin` → `framework` → `common`

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

| 账号 | 密码 | 角色 | 职责 |
|---|---|---|---|
| admin | admin123 | 系统管理员 | 用户/部门/字典/定时任务管理 |
| secadmin | admin123 | 安全管理员（配置管理员） | 角色/菜单授权配置 |
| auditadmin | admin123 | 审计管理员 | 操作日志/登录日志审计 |
| user | admin123 | 普通用户 | 只读查看 |

> 三员账号职责互斥：系统管理员不能配置角色权限，安全管理员不能查看日志，审计管理员不能管理用户——不存在万能超管。

## 内置功能

### 系统管理

- **用户管理**: 增删改查、角色分配、密码重置、状态切换、导出、三员账号保护
- **角色管理**: 增删改查、菜单权限分配、数据权限设置、三权分立校验
- **菜单管理**: 目录/菜单/按钮的树形管理，动态路由
- **部门管理**: 组织架构的树形管理
- **字典管理**: 系统字典的维护（如性别、状态等下拉选项）

### 系统监控

- **操作日志**: `@Log` 注解自动记录变更操作，含操作人/时间/IP/参数/耗时，支持导出
- **登录日志**: 登录/登出自动记录，含浏览器/操作系统/IP 信息，支持导出
- **定时任务**: Quartz 定时任务的可视化管理，支持启停和手动执行

### 其他

- **文件管理**: 文件上传/下载，扩展名白名单，大小限制
- **个人中心**: 修改个人资料、修改密码

## 三权分立（等保三员）

系统按照等保要求实现了三权分立：

| 角色 | 权限范围 | 不可访问 |
|---|---|---|
| **系统管理员** (sysadmin) | 用户管理、部门管理、字典管理、定时任务 | 角色/菜单授权、日志审计 |
| **安全管理员** (secadmin) | 角色管理、菜单管理（授权配置） | 业务管理、日志审计 |
| **审计管理员** (auditadmin) | 操作日志、登录日志（审计查看） | 业务管理、授权配置 |

保护机制：
- 三员账号不可删除/停用
- 非三员不能重置三员密码
- 不能修改自己的角色关联
- 不能删除/停用自己
- 菜单权限严格来自角色-菜单授权，无万能超管绕过
- 角色菜单分配范围校验，防止越权

## 安全特性

- **RSA-JWT (RS256)**: 非对称签名，私钥签发 / 公钥验证，防 Token 伪造
- **双 Token**: accessToken (30min) + refreshToken (7d)，前端自动静默刷新
- **RBAC 权限**: 用户-角色-菜单三级权限控制，支持按钮级权限（后端 `@PreAuthorize` + 前端 `v-hasPermi`）
- **数据权限**: `@DataScope` 注解，支持全部/自定义/本部门/本部门及以下/仅本人五种范围
- **XSS 过滤**: 自动清洗请求参数中的危险脚本
- **Sentinel 限流**: 接口级 QPS 控制，`@RateLimiter` 注解
- **登录保护**: 连续 5 次失败锁定 30 分钟，支持验证码开关
- **密码安全**: BCrypt 加密存储，密码强度校验
- **操作审计**: `@Log` 注解自动记录敏感操作，异步落库
- **安全响应头**: X-Frame-Options、CSP、X-Content-Type-Options 等

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

## 前端功能清单

### 已实现页面

| 页面 | 路径 | 说明 |
|---|---|---|
| 登录 | `/login` | 用户名/密码登录，记住用户名 |
| 首页仪表盘 | `/index` | 欢迎语、统计卡片、近期操作、快捷入口（按权限过滤） |
| 用户管理 | `/system/user` | 部门树过滤、用户 CRUD、角色选择、导出、重置密码 |
| 角色管理 | `/system/role` | 角色 CRUD、菜单权限树、数据权限配置、导出 |
| 菜单管理 | `/system/menu` | 树形表格、目录/菜单/按钮类型 |
| 部门管理 | `/system/dept` | 树形表格、组织架构管理 |
| 字典管理 | `/system/dict` | 字典类型 + 字典数据两级管理 |
| 操作日志 | `/monitor/operlog` | 按模块/人员/类型/状态/时间查询、查看详情、导出 |
| 登录日志 | `/monitor/logininfor` | 按用户/IP/状态查询、导出 |
| 定时任务 | `/monitor/job` | 任务 CRUD、启停、立即执行 |
| 个人中心 | `/user/profile` | 个人资料修改、密码修改 |
| 404 | `/*` | 未匹配路由 |

### 前端核心能力

- **动态路由**: 登录后从后端获取菜单，自动生成 Vue Router 路由
- **按钮权限**: `v-hasPermi` 指令，按权限码控制按钮显隐
- **Token 自动刷新**: 401 时自动用 refresh token 刷新，并发请求排队重放
- **请求封装**: 统一错误处理、Blob 下载、分页数据自动展开

## 二次开发指南

### 新增业务模块

1. **创建数据库表**并在 `sql/` 下更新初始化脚本
2. **后端**：在 `jiaoshoujia-admin` 中新建 Domain / Mapper / Service / Controller（同一模块内按包分层）
3. **前端**：在 `src/api/` 中新建 API 文件，在 `src/views/` 中新建页面
4. **添加菜单权限**：在「菜单管理」中添加对应的目录/菜单/按钮，或在 `init.sql` 中追加

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

支持的数据范围类型包括：全部数据、自定义数据、本部门数据、本部门及以下数据、仅本人数据。

### 运行单元测试

```bash
cd jiaoshoujia-web
mvn test
```

测试文件位于 `jiaoshoujia-admin/src/test/java/` 目录，使用 JUnit 5 + Mockito。

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

## 数据库表结构

| 表名 | 说明 |
|---|---|
| `sys_user` | 用户表 |
| `sys_role` | 角色表（含 data_scope 数据权限范围） |
| `sys_menu` | 菜单权限表（目录/菜单/按钮） |
| `sys_dept` | 部门表（树形结构） |
| `sys_user_role` | 用户-角色关联表 |
| `sys_role_menu` | 角色-菜单关联表 |
| `sys_role_dept` | 角色-部门关联表（自定义数据权限） |
| `sys_dict_type` | 字典类型表 |
| `sys_dict_data` | 字典数据表 |
| `sys_oper_log` | 操作日志表 |
| `sys_login_infor` | 登录日志表 |
| `sys_job` | 定时任务表 |

## 配置加密（可选）

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
