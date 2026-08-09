# 🚀 Bus Mapper: 高性能 MyBatis 增强框架

<p align="center">
<strong>高性能、功能丰富的 MyBatis 增强框架</strong>
</p>

-----

## 📖 项目介绍

**Bus Mapper** 是基于 MyBatis 的企业级增强框架。它致力于 **在不改变**核心 MyBatis 功能的前提下进行增强,旨在简化开发并提高效率。

-----

## ✨ 核心特性

### 🎯 基本能力

* **非侵入式增强**: 无缝集成,不影响现有项目
* **最小性能开销**: 启动时自动注入 CRUD,几乎无性能损失
* **强大的 CRUD**: 内置通用 Mapper,最小配置即可实现完整的单表操作
* **Lambda 类型安全**: 使用 Fn<T, R> 函数接口实现编译时安全检查
* **灵活的条件构造器**: 链式 API,支持复杂的动态查询

### ⚡ 性能优化

| 特性             | 性能提升                       | 描述                                |
|:-----------------|:-------------------------------|:------------------------------------|
| **多值批量插入** | $\uparrow 10-20\text{x}$       | 单条 SQL 语句插入多条记录           |
| **对象池管理**   | $\text{GC } \downarrow 60\%$   | `StringBuilder` 复用减少内存占用    |
| **智能缓存**     | $\text{命中率 } 99.5\%$        | 元数据和 SQL 的多级缓存             |
| **无锁并发**     | $\text{QPS } \uparrow 15\%$    | 使用 `ConcurrentHashMap` 的无锁设计 |
| **列选择优化**   | $\text{网络 } \downarrow 90\%$ | 按需加载字段,减少数据传输           |

### 🛡️ 企业级特性

* **审计日志**: 自动记录数据变更、操作者、SQL 语句和执行时间
* **多租户支持**: 列隔离、Schema 隔离和独立数据库模式
* **权限控制**: 数据权限过滤,防止未授权访问
* **慢 SQL 监控**: 自动检测慢查询,输出执行时间和 SQL
* **数据脱敏**: 自动对受保护字段进行敏感数据脱敏

### 🌍 数据库支持

支持 **18** 种主流和国产数据库:

**主流数据库**: MySQL / MariaDB、PostgreSQL、Oracle、SQL Server、SQLite、H2、Hsqldb

**国产数据库**: 神舟通用 (Oscar)、瀚高数据库 (CirroData)、虚谷数据库 (Xugudb)

**企业数据库**: DB2、Informix、AS400、Firebird、Herddb

-----

## 🚀 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-mapper</artifactId>
    <version>x.x.x</version>
</dependency>
```

### Spring Boot 集成

#### 1. 添加 Starter 依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-starter</artifactId>
    <version>x.x.x</version>
</dependency>
```

#### 2. 配置数据源

```yaml
# application.yml
spring:
  datasource:
    name: com_miaixz
    url: jdbc:postgresql://localhost:5432/miaixz?useSSL=false&useUnicode=true&characterEncoding=utf8&allowPublicKeyRetrieval=true
    username: postgres
    password: password
    type: com.zaxxer.hikari.HikariDataSource
    driver-class-name: org.postgresql.Driver

# Bus Mapper Configuration
bus:
  mapper:
    basePackage:
      com.miaixz.nexus.mapper
    mapperLocations: classpath:mapper/**/*.xml
    identifier:
      enabled: true # Enabled by default; set false to disable strict identifier validation.
    reasonable: false
    supportMethodsArguments: false
    params: count=countSql
    configurationProperties:
      provider:
        useOnce: false
        initSize: 1024
        concurrency: 1000
      dev_db:
        affix:
          prefix:
            value: dp_
        tenant:
          column: tenant_id
          ignore: tenant,token,user
      test_db:
        affix:
          prefix:
            value: dev_
        tenant:
          column: tenant_id
          ignore: tenant,token,user
```

Spring Boot 配置模型使用 `affix` 作为前缀与后缀改写作用域。内部实现上，`bus-mapper` 提供 `MapperOptions` 作为纯
Java/MyBatis 配置模型，承载 mapper 属性；插件装配、配置归一化和类型解析由独立组件负责。starter 继续只负责 Spring Boot
绑定、资源解析、Mapper 扫描和生命周期装配。

#### 3. 启用 Mapper 扫描

```java
@SpringBootApplication
@EnableMapper
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

-----

## 📝 使用示例

### 1. 定义实体类

```java
@Data
@Table("user")
public class User {
    @Id
    @KeyType(KeyType.Type.AUTO)
    private Long id;

    @Column("user_name")
    private String name;

    private String email;
    private Integer age;

    @Version
    private Integer version;

    @TenantId
    private String tenantId;

    @CreateTime
    private LocalDateTime createTime;
}
```

### 2. 创建 Mapper 接口

```java
@Repository
public interface UserMapper extends BasicMapper<User, Long> {
    // Inheriting BasicMapper is sufficient; no methods need to be written.
}
```

### 3. 基本 CRUD 操作

#### 插入

```java
// Insert single record
User user = new User();
user.setName("John Doe");
userMapper.insert(user);

// Insert (non-null fields only)
userMapper.insertSelective(user);

// Batch Insert - High Performance
List<User> users = new ArrayList<>();
// ... Add data
userMapper.insertBatch(users);  // 10,000 records only take 150-200ms
```

#### 查询

```java
// Query by Primary Key
User user = userMapper.selectById(1L);

// Query all
List<User> allUsers = userMapper.selectAll();

// Query by Entity Properties
User queryUser = new User();
queryUser.setAge(25);
List<User> users = userMapper.select(queryUser);

// Batch Query
List<User> users = userMapper.selectByIds(Arrays.asList(1L, 2L, 3L));
```

#### 更新

```java
// Update by Primary Key (all fields)
userMapper.updateByPrimaryKey(user);

// Update by Primary Key (non-null fields only)
userMapper.updateByPrimaryKeySelective(user);

// Batch Update
userMapper.updateBatch(users);
```

#### 删除

```java
// Delete by Primary Key
userMapper.deleteById(1L);

// Batch Delete
userMapper.deleteBatchByIds(Arrays.asList(1L, 2L, 3L));
```

### 4. Lambda 条件查询 (类型安全)

```java
// Create condition wrapper
ConditionWrapper<User, Long> wrapper = mapper.wrapper();

// Basic Conditions
List<User> users = wrapper
    .eq(User::getAge, 25)
    .like(User::getName, "%John%")
    .isNotNull(User::getEmail)
    .orderBy(User::getCreateTime, Sort.ORDER.DESC)
    .list();

// Complex Conditions
List<User> users = wrapper
    .eq(User::getStatus, 1)
    .like(User::getName, "%Jane%")
    .between(User::getAge, 18, 65)
    .in(User::getRegion, Arrays.asList("Beijing", "Shanghai", "Shenzhen"))
    .orderBy(User::getCreateTime, Sort.ORDER.DESC)
    .limit(100)
    .list();

// Dynamic Conditions
if (StringKit.isNotBlank(name)) {
    wrapper.like(User::getName, "%" + name + "%");
}
if (minAge != null) {
    wrapper.ge(User::getAge, minAge);
}
List<User> users = wrapper.list();

// Column Selection (only query necessary fields)
List<User> users = wrapper
    .select(User::getId, User::getName, User::getEmail)
    .eq(User::getStatus, 1)
    .list();

// Paging Query
Page<User> page = wrapper
    .eq(User::getStatus, 1)
    .orderBy(User::getCreateTime, Sort.ORDER.DESC)
    .page(1, 20);  // Page 1, 20 records per page

// Count
long count = wrapper
    .eq(User::getStatus, 1)
    .count();
```

### 5. 高级查询

#### 流式查询 (大数据集)

```java
// Use a cursor to avoid loading into memory all at once
try (Cursor<User> cursor = userMapper.selectCursorByCondition(condition)) {
    cursor.forEach(user -> processUser(user));
}

// Use Stream API
try (Stream<User> stream = userMapper.selectStreamByCondition(condition)) {
    stream.filter(u -> u.getAge() > 18).forEach(System.out::println);
}
```

### 6. 审计日志

#### 使用注解标记审计字段

```java
@Table("user")
@TableAudit  // Table-level audit
public class User {
    @Audit  // Field-level audit
    private String email;
}
```

#### 配置审计处理器

```java
@Configuration
public class MapperConfiguration {

    @Bean
    public AuditHandler auditHandler() {
        // Custom Audit Log Recorder
        AuditProvider customLogger = new AuditProvider() {
            @Override
            public void log(AuditRecord record) {
                // Regular SQL record
                System.out.println("SQL Execution: " + record.getSqlId());
            }

            @Override
            public void logSlowSql(AuditRecord record) {
                // Slow SQL warning
                System.out.println("Slow SQL: " + record.getSqlId());
                System.out.println("Elapsed Time: " + record.getElapsedTime() + "ms");
                System.out.println("SQL: " + record.getSql());
            }

            @Override
            public void logFailure(AuditRecord record) {
                // SQL execution failure record
                System.err.println("SQL Failure: " + record.getSqlId());
                System.err.println("Exception: " + record.getException());
            }
        };

        // Create Audit Configuration
        org.miaixz.bus.mapper.feature.audit.AuditConfig config =
            org.miaixz.bus.mapper.feature.audit.AuditConfig.builder()
                .enabled(true)
                .slowSqlThreshold(1000)  // Slow SQL threshold: 1 second
                .logParameters(true)     // Record SQL parameters
                .logResults(false)       // Do not record query results
                .logAllSql(false)        // Only record slow SQL
                .auditLogger(customLogger)
                .build();

        return new AuditHandler(config);
    }

    @Bean
    public MybatisInterceptor mybatisInterceptor(AuditHandler auditHandler) {
        MybatisInterceptor interceptor = new MybatisInterceptor();
        interceptor.addHandler(auditHandler);
        return interceptor;
    }
}
```

### 7. 多租户

#### 基本配置

```java
@Table("user")
public class User {
    @TenantId
    private String tenantId;
}

// Use TenantContext to set Tenant ID
TenantContext.setCurrentTenantId("tenant_001");
try {
    // All queries automatically add tenant filtering
    userMapper.selectAll();
} finally {
    TenantContext.clear();
}

// Or use Lambda approach (recommended)
TenantContext.runWithTenant("tenant_001", () -> {
    userMapper.selectAll();
});
```

#### 快速配置 (推荐)

```java
@Configuration
public class MapperConfiguration {

    @Bean
    public TenantHandler tenantHandler() {
        // Method 1: Simplest - only need to provide logic to get tenant ID
        TenantConfig config = TenantConfig.of(() ->
            SecurityContextHolder.getTenantId()
        );
        return new TenantHandler(config);
    }

    @Bean
    public MybatisInterceptor mybatisInterceptor(TenantHandler tenantHandler) {
        MybatisInterceptor interceptor = new MybatisInterceptor();
        interceptor.addHandler(tenantHandler);
        return interceptor;
    }
}
```

#### 完整配置

```java
@Configuration
public class MapperConfiguration {

    @Bean
    public TenantHandler tenantHandler() {
        // Method 2: Complete Configuration
        TenantConfig config = TenantConfig.builder()
            .mode(TenantMode.COLUMN)
            .column("tenant_id")
            .ignoreTables("sys_config", "sys_dict", "sys_log")
            .provider(() -> {
                // Get Tenant ID from Spring Security
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getPrincipal() instanceof UserDetails) {
                    return ((CustomUserDetails) auth.getPrincipal()).getTenantId();
                }
                return null;
            })
            .enabled(true)
            .build();

        return new TenantHandler(config);
    }
    // ... MybatisInterceptor setup remains the same
}
```

#### 忽略租户过滤

要临时忽略租户过滤 (例如,管理员视图):

```java
// Temporarily ignore tenant filtering
TenantContext.runIgnoreTenant(() -> {
    // Queries here will not include the tenant filter condition
    List<User> allUsers = userMapper.selectAll();
});

// Or manually control
TenantContext.setIgnoreTenant(true);
try {
    // Query data from all tenants
    List<User> allUsers = userMapper.selectAll();
} finally {
    TenantContext.setIgnoreTenant(false);
}
```

#### 配置文件方式

```yaml
bus:
  mapper:
    configurationProperties:
      # Data Source 1 Configuration
      dev_db:
        affix:
          prefix:
            value: dp_
        tenant:
          column: tenant_id
          ignore: sys_config,sys_dict,sys_log

      # Data Source 2 Configuration
      prod_db:
        affix:
          prefix:
            value: prod_
        tenant:
          column: tenant_id
          ignore: sys_config,sys_dict,sys_log
```

-----

## 📋 注解参考

### 实体类注解

| 注解          | 描述             | 示例                          |
|:--------------|:-----------------|:------------------------------|
| `@Table`      | 指定表名         | `@Table("user")`              |
| `@Id`         | 标记主键字段     | `@Id`                         |
| `@KeyType`    | 主键生成策略     | `@KeyType(KeyType.Type.AUTO)` |
| `@Column`     | 指定列名         | `@Column("user_name")`        |
| `@Version`    | 乐观锁版本号     | `@Version`                    |
| `@TenantId`   | 租户 ID 字段     | `@TenantId`                   |
| `@CreateTime` | 创建时间自动填充 | `@CreateTime`                 |
| `@UpdateTime` | 更新时间自动填充 | `@UpdateTime`                 |
| `@Ignore`     | 忽略字段         | `@Ignore`                     |
| `@TableAudit` | 表级审计         | `@TableAudit`                 |
| `@Audit`      | 字段级审计       | `@Audit`                      |

### 主键策略

```java
public enum Type {
    AUTO,        // Database auto-increment
    IDENTITY,    // IDENTITY primary key
    UUID,        // UUID
    SNOWFLAKE,   // Snowflake algorithm
    SEQUENCE     // Sequence
}
```

-----

## 💡 最佳实践

### 1. 使用 Lambda 表达式构建条件

```java
// ✅ Recommended: Type safe, supports refactoring
wrapper.eq(User::getName, "John Doe")
       .gt(User::getAge, 18);

// ❌ Not Recommended: String hardcoding, error prone
wrapper.eq("name", "John Doe")
       .gt("age", 18);
```

### 2. 批量操作使用 `insertBatch`/`updateBatch`

```java
// ✅ Recommended: High-performance batch insert
userMapper.insertBatch(users);  // Single SQL statement, fast

// ❌ Not Recommended: Loop insertion
for (User user : users) {
    userMapper.insert(user);    // Multiple SQL statements, slow
}
```

### 3. 仅查询必要字段

```java
// ✅ Recommended: Reduces network transfer
wrapper.select(User::getId, User::getName)
       .list();

// ❌ Not Recommended: Queries all fields
wrapper.list();  // SELECT *
```

### 4. 大数据集使用流式查询

```java
// ✅ Recommended: Uses cursor, low memory footprint
try (Cursor<User> cursor = userMapper.selectCursorByCondition(condition)) {
    cursor.forEach(user -> process(user));
}

// ❌ Not Recommended: Loads all data into memory at once
List<User> users = userMapper.selectByCondition(condition);  // Potential OOM
```

### 5. 正确使用多租户

```java
// ✅ Recommended: Use Lambda for automatic management
TenantContext.runWithTenant("tenant_001", () -> {
    userMapper.selectAll();
});

// ❌ Not Recommended: Manual management can lead to context leakage
TenantContext.setCurrentTenantId("tenant_001");
userMapper.selectAll();
// Easy to forget to call clear()
```

-----

## ❓ 常见问题

### Q1: 如何自定义主键生成策略?

```java
@Configuration
public class KeyGeneratorConfig {
    @Bean
    public KeyGenerator customKeyGenerator() {
        return new KeyGenerator() {
            @Override
            public Object generateKey() {
                // Custom ID generation logic
                return IdWorker.getId();
            }
        };
    }
}
```

### Q2: 如何处理多数据源场景?

```java
@Configuration
public class MultiDataSourceConfig {

    @Bean
    @Primary
    public TenantHandler primaryTenantHandler() {
        return new TenantHandler(
            TenantConfig.builder()
                .column("tenant_id")
                .provider(() -> getTenantId())
                .build()
        );
    }

    @Bean
    public TenantHandler secondaryTenantHandler() {
        return new TenantHandler(
            TenantConfig.builder()
                .column("org_id")  // Different column name
                .provider(() -> getOrgId())
                .build()
        );
    }
}
```

### Q3: 如何调试生成的 SQL?

```yaml
# application.yml
logging:
  level:
    org.miaixz.bus.mapper: DEBUG

# Or use audit log
bus:
  mapper:
    audit:
      enabled: true
      log-all-sql: true
      print-console: true
```

### Q4: 批量插入时如何获取生成的主键?

```java
// Method 1: Using @KeyType(AUTO)
@Id
@KeyType(KeyType.Type.AUTO)
private Long id;

List<User> users = new ArrayList<>();
userMapper.insertBatch(users);
// The 'id' field in 'users' will be automatically populated.

// Method 2: Using a custom key generator
@Id
@KeyType(KeyType.Type.SNOWFLAKE)
private Long id;
```

### Q5: 如何实现逻辑删除?

```java
@Table("user")
public class User {
    @Logic  // Logical deletion field
    private Integer deleted;  // 0-Not deleted, 1-Deleted
}

// Configuration
@Configuration
public class LogicDeleteConfig {
    @Bean
    public LogicDeleteHandler logicDeleteHandler() {
        return LogicDeleteHandler.builder()
            .deletedValue(1)
            .notDeletedValue(0)
            .build();
    }
}
```

### Q6: 如何处理租户 ID 为 null 的场景?

```java
// Method 1: Throw an exception (strict mode)
TenantConfig config = TenantConfig.builder()
    .provider(() -> {
        String tenantId = getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant ID cannot be null");
        }
        return tenantId;
    })
    .build();

// Method 2: Return a default value
TenantConfig config = TenantConfig.builder()
    .provider(() -> {
        String tenantId = getTenantId();
        return tenantId != null ? tenantId : "default";
    })
    .build();
```

-----

## 🔄 版本兼容性

| Bus Mapper 版本 | MyBatis 版本 | Spring Boot 版本 | JDK 版本 |
|:----------------|:-------------|:-----------------|:---------|
| 8.x             | 3.5.x+       | 3.x+             | 17+      |
| 7.x             | 3.5.x+       | 2.x+             | 11+      |

-----

## 🚀 性能调优建议

### 1. 启用对象池

```yaml
bus:
  mapper:
    configurationProperties:
      provider:
        useOnce: false      # Disable single-use, enable object reuse
        initSize: 1024      # Initial pool size
        concurrency: 1000   # Concurrency level
```

### 2. 启用 SQL 缓存

```java
TenantConfig config = TenantConfig.builder()
    .enableSqlCache(true)  // Enable SQL caching
    .build();
```

### 3. 设置合理的批量大小

```java
// Recommended batch size for insertion is 500-1000 records
List<List<User>> batches = Lists.partition(users, 500);
for (List<User> batch : batches) {
    userMapper.insertBatch(batch);
}
```

### 4. 使用列选择减少数据传输

```java
// Only query necessary fields, can reduce network transfer by 50-90%
wrapper.select(User::getId, User::getName)
       .list();
```

-----

## 📊 性能对比

基于 JMH 基准测试结果:

### 批量插入性能 (10,000 条记录)

| 框架           | 耗时               | 性能提升                    |
|:---------------|:-------------------|:----------------------------|
| 传统循环       | $2500\text{ms}$    | -                           |
| MyBatis Flex   | $700\text{ms}$     | $\uparrow 3.6\text{x}$      |
| **Bus Mapper** | **$180\text{ms}$** | **$\uparrow 13.9\text{x}$** |

### 查询性能 (1,000 次操作)

| 框架           | 平均延迟          | QPS               |
|:---------------|:------------------|:------------------|
| MyBatis Flex   | $14.5\text{ms}$   | $68/\text{s}$     |
| **Bus Mapper** | **$12\text{ms}$** | **$83/\text{s}$** |

### 缓存效率

| 框架           | 命中率       | 节省时间           |
|:---------------|:-------------|:-------------------|
| MyBatis Flex   | $95\%$       | $520\text{ms}$     |
| **Bus Mapper** | **$99.5\%$** | **$890\text{ms}$** |

### 内存和 GC (1 小时)

| 指标         | Bus Mapper     | MyBatis Flex   |
|:-------------|:---------------|:---------------|
| Full GC 次数 | $2-3$          | $5-7$          |
| GC 总时间    | $120\text{ms}$ | $280\text{ms}$ |

详细报告：[可疑链接已删除]

-----

## 🛠️ Mapper 方法列表

### 插入方法

```java
int insert(T entity);                      // Insert (all fields)
int insertSelective(T entity);             // Insert (non-null fields)
int insertBatch(List<T> entities);         // Batch insert
```

### 查询方法

```java
T selectById(I id);                        // Query by primary key
List<T> selectByIds(Collection<I> ids);    // Batch query
List<T> selectAll();                       // Query all
List<T> select(T entity);                  // Query by entity properties
List<T> selectByCondition(Condition<T> c); // Query by condition
long selectCount(T entity);                // Count
Cursor<T> selectCursorByCondition(...);    // Cursor query
```

### 更新方法

```java
int updateByPrimaryKey(T entity);          // Update by primary key
int updateByPrimaryKeySelective(T entity); // Update by primary key (non-null)
int updateBatch(List<T> entities);         // Batch update
```

### 删除方法

```java
int deleteById(I id);                      // Delete by primary key
int deleteBatchByIds(Collection<I> ids);   // Batch delete
int delete(T entity);                      // Delete by entity properties
```

### ConditionWrapper 方法 (流式 API)

```java
.eq(User::getName, "John Doe")             // Equals
.ne(User::getStatus, 0)                   // Not Equals
.gt(User::getAge, 18)                     // Greater Than
.like(User::getName, "%John%")            // Like
.between(User::getAge, 18, 65)            // Range Between
.in(User::getRegion, list)                // In collection
.isNull(User::getEmail)                   // Is Null
.orderBy(User::getCreateTime, DESC)       // Ordering
.select(User::getId, User::getName)       // Column selection
.limit(10)                                // Limit
.list()                                   // Query list
.one()                                    // Query single record
.count()                                  // Count
.page(1, 20)                              // Paging
```

-----

## 实体表结构初始化

Bus Mapper 支持根据实体元数据初始化数据库表结构。启动时可以创建缺失表、创建缺失字段、创建主键、创建外键、创建普通索引、创建唯一索引，并执行明确放行的字段类型或字段长度变更。

该能力由 `bus-mapper` 实现。Spring Boot 只负责绑定 `bus.mapper.schema` 配置并在启动时触发执行。`Dialect` 是唯一数据库能力入口，由
`PagingBehavior`、`OptionsBehavior`、`UpsertBehavior`、`SchemaBehavior` 组成。

默认关闭：

```yaml
bus:
  mapper:
    schema:
      enabled: false
```

### 支持的表结构变更

| 变更                       | 是否支持 | 必须开启的配置                                               |
|:---------------------------|:---------|:-------------------------------------------------------------|
| 创建缺失表                 | 支持     | `allow-create-table: true`                                   |
| 添加缺失字段               | 支持     | `allow-add-column: true`                                     |
| 创建主键                   | 支持     | `allow-create-primary-key: true`                             |
| 创建组合主键               | 支持     | `allow-create-primary-key: true`                             |
| 创建普通索引               | 支持     | `allow-create-index: true`                                   |
| 创建唯一索引               | 支持     | `allow-create-unique: true`                                  |
| 创建组合索引               | 支持     | `allow-create-index: true`                                   |
| 创建外键                   | 支持     | `allow-create-foreign-key: true`                             |
| 修正表/字段注释            | 支持     | `allow-modify-comment: true`                                 |
| 修改 SQL 类型              | 支持     | `allow-modify-type: true`                                    |
| 扩大 varchar 长度          | 支持     | `allow-expand-length: true`                                  |
| 缩小 varchar 长度          | 默认阻止 | `allow-shrink-length: true`、`allow-dangerous: true`、白名单 |
| 删除字段、索引、主键、外键 | 默认阻止 | 对应 drop 开关、`allow-dangerous: true`、白名单              |

支持模式：

| 模式       | 行为                                    |
|:-----------|:----------------------------------------|
| `NONE`     | 不读取元数据，不生成 SQL，不执行 SQL。  |
| `SCRIPT`   | 读取元数据并输出 SQL 脚本，不执行 DDL。 |
| `CREATE`   | 只创建缺失表，已存在表直接跳过。        |
| `VALIDATE` | 读取元数据并输出差异报告，不执行 DDL。  |
| `UPDATE`   | 只执行配置明确放行的结构差异。          |

### 已存在表的处理规则

当数据表已经存在时，Bus Mapper
会读取当前数据库元数据，并与实体元数据进行差异比较。只有同时满足“当前方言支持”和“配置明确放行”的差异才会执行。已有表缺少索引、唯一索引、主键、外键时，可以在启动初始化时补齐。已有字段类型变更必须开启
`allow-modify-type: true`，字段长度扩展必须开启 `allow-expand-length: true`。所有表/字段注释 DDL 都必须开启
`allow-modify-comment: true`；当该配置为 `false` 时，新建表、新增字段、已有表和已有字段都不会生成注释 SQL。

破坏性变更默认全部阻止。删除字段、删除索引、删除主键、删除外键、缩小字段长度等操作，必须同时满足对应操作开关、
`allow-dangerous: true` 和
`dangerous-whitelist` 白名单。

### Spring Boot 启动配置示例

```yaml
bus:
  mapper:
    basePackage:
      com.miaixz.nexus.mapper
    schema:
      enabled: true
      mode: UPDATE
      dry-run: false
      print-sql: true
      fail-fast: true
      entity-packages:
        - com.miaixz.nexus.entity
      allow-create-table: true
      allow-add-column: true
      allow-create-primary-key: true
      allow-create-index: true
      allow-create-unique: true
      allow-create-foreign-key: true
      allow-modify-comment: true
      allow-modify-type: true
      allow-expand-length: true
```

`entity-packages` 会扫描包下标注 `@Entity` 或 `@Table` 的实体类；`include-entities` 为空时，扫描到的实体都会参与初始化。

### 按数据库初始化

当项目使用 `configurationProperties.namespaces` 配置多个数据库时，可以把 `schema` 放在对应 namespace 下。词缀规则不在
`schema` 下重复配置，初始化时会读取同一个 namespace 的 `affix.prefix.value/ignore` 和
`affix.suffix.value/ignore` 配置。前缀与后缀的忽略表相互独立：忽略其中一端不会阻止另一端生效。 词缀支持全局和按数据库两种配置：数据源级
`affix.*` 优先，其次使用 `shared.affix.*`、`default.affix.*` 或顶层
`bus.mapper.affix`。

```yaml
bus:
  mapper:
    configurationProperties:
      namespaces:
        - name: com_miaixz
          affix:
            prefix:
              value: dp_
              ignore: tenant,assets
            suffix:
              value: _2026
              ignore: license
          tenant:
            column: tenant_id
            ignore: tenant,assets,license,token,user
          schema:
            enabled: true
            mode: UPDATE
            dry-run: false
            print-sql: true
            fail-fast: true
            entity-packages:
              - com.miaixz.nexus.entity
            include-entities:
              - com.miaixz.nexus.entity.License
              - com.miaixz.nexus.entity.Token
            allow-create-table: true
            allow-add-column: true
            allow-create-primary-key: true
            allow-create-index: true
            allow-modify-comment: true
```

`bus.mapper.schema` 仍然保持兼容：没有 namespace 级 `schema` 时按原全局配置执行；存在 namespace 级 `schema` 时，全局
`bus.mapper.schema` 会作为每个 namespace 的默认模板，避免同一批实体再被全局初始化一次。namespace 的 `name` 同时也是 schema
初始化使用的唯一数据源路由键，必须与具名数据源 Bean 或动态数据源中注册的路由名称一致。也可以使用
`configurationProperties.shared.schema` 作为所有 namespace 的默认 schema 配置，再由具体 namespace 覆盖。

生产环境或类生产环境首次启用时，先使用 `SCRIPT` 模式生成 SQL 脚本并人工确认：

```yaml
bus:
  mapper:
    schema:
      enabled: true
      mode: SCRIPT
      dry-run: false
      script-location: ./target/bus-mapper-schema.sql
      entity-packages:
        - com.miaixz.nexus.entity
      allow-create-table: true
      allow-create-primary-key: true
      allow-create-index: true
      allow-create-unique: true
      allow-create-foreign-key: true
```

### 实体示例

```java
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.Data;

@Data
@Entity
@Table(name = "dp_user", comment = "User table")
public class UserEntity {

    @Id
    @Column(nullable = false, comment = "Primary key ID")
    private Long id;

    @Column(length = 128, comment = "User name")
    private String name;
}
```

```java
@Data
@Entity
@Table(
    name = "dp_order",
    indexes = @Index(name = "idx_dp_order_user_code", columnList = "user_id, code"),
    uniqueConstraints = @UniqueConstraint(name = "uk_dp_order_code", columnNames = "code")
)
public class OrderEntity {

    @Id
    @Column(nullable = false)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(length = 64, unique = true)
    private String code;

    @Column(length = 512)
    private String remark;

    private Long amount;

    @ManyToOne
    @JoinColumn(
        name = "user_id",
        referencedColumnName = "id",
        foreignKey = @ForeignKey(name = "fk_dp_order_user")
    )
    private UserEntity user;
}
```

上述实体会生成或补齐：

* `dp_user` 和 `dp_order` 表。
* `id` 主键。
* 普通组合索引 `idx_dp_order_user_code(user_id, code)`。
* 唯一索引 `uk_dp_order_code(code)`。
* 外键 `fk_dp_order_user(user_id) references dp_user(id)`。
* 通过 `@Table(comment = "...")` 和 `@Column(comment = "...")` 生成或补齐表、字段注释。
* `remark VARCHAR(512)`，来源于 `@Column(length = 512)`。
* `amount BIGINT`，来源于 Java 类型 `Long`。

`@JoinColumn` 标注的关系字段用于生成外键。实体中仍然保留真实物理字段，例如 `userId`，用于 mapper 读写数据库列值。

### 组合主键示例

```java
@Data
@Entity
@Table(
    name = "dp_role_permission",
    indexes = @Index(name = "idx_dp_role_permission_role_perm", columnList = "role_id, permission_id")
)
public class RolePermissionEntity {

    @Id
    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Id
    @Column(name = "permission_id", nullable = false)
    private Long permissionId;
}
```

该实体会生成 `PRIMARY KEY (role_id, permission_id)`，并在相同两个字段上生成组合索引。

### 字段类型和长度变更示例

```java
@Data
@Entity
@Table(name = "dp_schema_type")
public class SchemaTypeEntity {

    @Id
    @Column(nullable = false)
    private Integer id;

    @Column(name = "text_value", length = 512)
    private String textValue;

    @Column(name = "big_value")
    private Long bigValue;

    @Column(name = "wide_value", length = 512)
    private String wideValue;
}
```

在 `mode: UPDATE`、`allow-modify-type: true`、`allow-expand-length: true` 同时开启时，Bus Mapper 可以将已有表中的
`text_value INTEGER` 修改为 `VARCHAR(512)`，将 `big_value INTEGER` 修改为 `BIGINT`，将 `wide_value VARCHAR(32)` 扩展为
`VARCHAR(512)`。

类型映射基于 Java 类型。`int(4)`、`int(8)` 不属于 Java 类型长度映射，默认不会生成。`VARCHAR(512)` 通过
`@Column(length = 512)` 生成。需要数据库原生类型时使用 `@Column(columnDefinition = "...")`，例如
`int(8) unsigned`。

### 程序化初始化示例

```java
import java.util.List;

import javax.sql.DataSource;

import org.miaixz.bus.mapper.Charter.Schema;
import org.miaixz.bus.mapper.feature.schema.EntitySchemaInitializer;
import org.miaixz.bus.mapper.feature.schema.SchemaConfig;
import org.miaixz.bus.mapper.feature.schema.SchemaReport;

public class SchemaBootstrap {

    public SchemaReport initialize(DataSource dataSource) throws Exception {
        SchemaConfig config = new SchemaConfig()
            .enabled(true)
            .mode(Schema.UPDATE)
            .dryRun(false)
            .printSql(true)
            .allowCreateTable(true)
            .allowAddColumn(true)
            .allowCreatePrimaryKey(true)
            .allowCreateIndex(true)
            .allowCreateUnique(true)
            .allowCreateForeignKey(true)
            .allowModifyComment(true)
            .allowModifyType(true)
            .allowExpandLength(true);

        return new EntitySchemaInitializer().initialize(
            dataSource,
            List.of(UserEntity.class, OrderEntity.class, RolePermissionEntity.class, SchemaTypeEntity.class),
            config
        );
    }
}
```

### PostgreSQL 集成测试示例

`bus-mapper` 已包含 PostgreSQL 集成测试，该测试会保留可见测试表和测试数据：

```bash
BUS_MAPPER_POSTGRES_PASSWORD='your-password' mvn -B -ntp -f bus-mapper/pom.xml \
  -Dtest=PostgreSqlEntitySchemaInitializerIntegrationTest \
  -Dbus.mapper.postgres.it=true \
  -Dbus.mapper.postgres.url='jdbc:postgresql://localhost:5432/example?connectTimeout=8&socketTimeout=30&sslmode=disable' \
  -Dbus.mapper.postgres.user=postgres \
  test
```

该测试只写入 `dp_postgres_test_parent`、`dp_postgres_test_child`、`dp_postgres_test_composite`、`dp_postgres_test_type`，
不会删表、不会清表、不会删除数据。

### 方言覆盖

所有内置方言均提供 schema 操作：MySQL、PostgreSQL、H2、SQLite、Firebird、Oscar、Oracle9i、SQL Server、 Polardb、HerdDB、SQL Server
2012、DB2、AS/400、HSQLDB、CirroData、Informix、Oracle、XuguDB、Dameng。每个方言保留原有分页与 UPSERT 行为，同时通过
`OptionsBehavior.types()` 与各方言直接实现的 `SchemaBehavior` 方法暴露 schema 和 metadata 能力。schema SQL 规则直接归属于
`org.miaixz.bus.mapper.dialect` 下的对应方言类，例如 MySQL DDL 位于 `MySql`，PostgreSQL DDL 位于
`PostgreSql`，H2 DDL 位于 `H2`。

本方案不使用 Flyway，不使用 Liquibase，也不创建迁移记录表。

-----

## 🔧 配置示例

```yaml
mapper:
  # Global configuration, effective for all databases
  # Tenant Isolation
  tenant:
    column: tenant_id
    ignore: sys_tenant,sys_config,sys_dict

  # SQL Auditing
  audit:
    enabled: true
    slow-sql-threshold: 500
    log-parameters: true
    print-console: true

  # Data Population
  populate:
    created: true
    modified: true
    creator: true
    modifier: true

  # Data Visibility
  visible:
    enabled: true
    ignore: sys_admin_table

  # Affix Rules
  affix:
    enabled: true
    prefix:
      value: prod_
      ignore: sys_log,sys_config
    suffix:
      value: _archive
      ignore: sys_log

  # Per-Database Configuration (overrides global)
  configurationProperties:
    com_miaixz:
      affix:
        prefix:
          value: dp_
          ignore: tenant,assets,license
        suffix:
          value: _archive
          ignore: tenant,assets
      tenant:
        column: tenant_id
        ignore: tenant,assets,license
    # ......
```

-----

是否需要在 YouTube 上搜索 Bus Mapper 入门教程视频？
