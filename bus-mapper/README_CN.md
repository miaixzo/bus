# 🚀 Bus Mapper: 高性能 MyBatis 增强框架

<p align="center">
<strong>高性能、功能丰富的 MyBatis 增强框架</strong>
</p>

-----

## 📖 项目介绍

**Bus Mapper** 是基于 MyBatis 的企业级增强框架。它致力于**在不改变**核心 MyBatis 功能的前提下进行增强,旨在简化开发并提高效率。

-----

## ✨ 核心特性

### 🎯 基本能力

* **非侵入式增强**: 无缝集成,不影响现有项目
* **最小性能开销**: 启动时自动注入 CRUD,几乎无性能损失
* **强大的 CRUD**: 内置通用 Mapper,最小配置即可实现完整的单表操作
* **Lambda 类型安全**: 使用 Fn<T, R> 函数接口实现编译时安全检查
* **灵活的条件构造器**: 链式 API,支持复杂的动态查询

### ⚡ 性能优化

| 特性 | 性能提升 | 描述 |
| :--- | :--- | :--- |
| **多值批量插入** | $\uparrow 10-20\text{x}$ | 单条 SQL 语句插入多条记录 |
| **对象池管理** | $\text{GC } \downarrow 60\%$ | `StringBuilder` 复用减少内存占用 |
| **智能缓存** | $\text{命中率 } 99.5\%$ | 元数据和 SQL 的多级缓存 |
| **无锁并发** | $\text{QPS } \uparrow 15\%$ | 使用 `ConcurrentHashMap` 的无锁设计 |
| **列选择优化** | $\text{网络 } \downarrow 90\%$ | 按需加载字段,减少数据传输 |

### 🛡️ 企业级特性

* **审计日志**: 自动记录数据变更、操作者、SQL 语句和执行时间
* **多租户支持**: 列隔离、Schema 隔离和独立数据库模式
* **权限控制**: 数据权限过滤,防止未授权访问
* **慢 SQL 监控**: 自动检测慢查询,输出执行时间和 SQL
* **数据脱敏**: 自动对受保护字段进行敏感数据脱敏

### 🌍 数据库支持

支持 **18** 种主流和国产数据库:

**主流数据库**: MySQL / MariaDB、PostgreSQL、Oracle、SQL Server、SQLite、H2、Hsqldb

**国产数据库**: 神舟通用(Oscar)、瀚高数据库(CirroData)、虚谷数据库(Xugudb)

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
    name: com_deepparser
    url: jdbc:postgresql://localhost:5432/miaixz?useSSL=false&useUnicode=true&characterEncoding=utf8&allowPublicKeyRetrieval=true
    username: postgres
    password: password
    type: com.zaxxer.hikari.HikariDataSource
    driver-class-name: org.postgresql.Driver

# Bus Mapper 配置
bus:
  mapper:
    basePackage:
      ai.deepparser.nexus.mapper
    mapperLocations: classpath:mapper/**/*.xml
    autoDelimitKeywords: true
    reasonable: false
    supportMethodsArguments: false
    params: count=countSql
    configurationProperties:
      provider:
        useOnce: false
        initSize: 1024
        concurrency: 1000
      dev_db:
        table:
          prefix: dp_
        tenant:
          column: tenant_id
          ignore: tenant,token,user
      test_db:
        table:
          prefix: dev_
        tenant:
          column: tenant_id
          ignore: tenant,token,user
```

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
    // 继承 BasicMapper 即可,无需编写方法
}
```

### 3. 基本 CRUD 操作

#### 插入

```java
// 插入单条记录
User user = new User();
user.setName("张三");
userMapper.insert(user);

// 插入(仅非空字段)
userMapper.insertSelective(user);

// 批量插入 - 高性能
List<User> users = new ArrayList<>();
// ... 添加数据
userMapper.insertBatch(users);  // 10000 条记录仅耗时 150-200ms
```

#### 查询

```java
// 根据主键查询
User user = userMapper.selectById(1L);

// 查询全部
List<User> allUsers = userMapper.selectAll();

// 根据实体属性查询
User queryUser = new User();
queryUser.setAge(25);
List<User> users = userMapper.select(queryUser);

// 批量查询
List<User> users = userMapper.selectByIds(Arrays.asList(1L, 2L, 3L));
```

#### 更新

```java
// 根据主键更新(所有字段)
userMapper.updateByPrimaryKey(user);

// 根据主键更新(仅非空字段)
userMapper.updateByPrimaryKeySelective(user);

// 批量更新
userMapper.updateBatch(users);
```

#### 删除

```java
// 根据主键删除
userMapper.deleteById(1L);

// 批量删除
userMapper.deleteBatchByIds(Arrays.asList(1L, 2L, 3L));
```

### 4. Lambda 条件查询(类型安全)

```java
// 创建条件包装器
ConditionWrapper<User, Long> wrapper = mapper.wrapper();

// 基本条件
List<User> users = wrapper
    .eq(User::getAge, 25)
    .like(User::getName, "%张三%")
    .isNotNull(User::getEmail)
    .orderBy(User::getCreateTime, Sort.ORDER.DESC)
    .list();

// 复杂条件
List<User> users = wrapper
    .eq(User::getStatus, 1)
    .like(User::getName, "%李四%")
    .between(User::getAge, 18, 65)
    .in(User::getRegion, Arrays.asList("北京", "上海", "深圳"))
    .orderBy(User::getCreateTime, Sort.ORDER.DESC)
    .limit(100)
    .list();

// 动态条件
if (StringKit.isNotBlank(name)) {
    wrapper.like(User::getName, "%" + name + "%");
}
if (minAge != null) {
    wrapper.ge(User::getAge, minAge);
}
List<User> users = wrapper.list();

// 列选择(仅查询必要字段)
List<User> users = wrapper
    .select(User::getId, User::getName, User::getEmail)
    .eq(User::getStatus, 1)
    .list();

// 分页查询
Page<User> page = wrapper
    .eq(User::getStatus, 1)
    .orderBy(User::getCreateTime, Sort.ORDER.DESC)
    .page(1, 20);  // 第 1 页,每页 20 条记录

// 计数
long count = wrapper
    .eq(User::getStatus, 1)
    .count();
```

### 5. 高级查询

#### 流式查询(大数据集)

```java
// 使用游标避免一次性加载到内存
try (Cursor<User> cursor = userMapper.selectCursorByCondition(condition)) {
    cursor.forEach(user -> processUser(user));
}

// 使用 Stream API
try (Stream<User> stream = userMapper.selectStreamByCondition(condition)) {
    stream.filter(u -> u.getAge() > 18).forEach(System.out::println);
}
```

### 6. 审计日志

#### 使用注解标记审计字段

```java
@Table("user")
@TableAudit  // 表级审计
public class User {
    @Audit  // 字段级审计
    private String email;
}
```

#### 配置审计处理器

```java
@Configuration
public class MapperConfiguration {

    @Bean
    public AuditHandler auditHandler() {
        // 自定义审计日志记录器
        AuditProvider customLogger = new AuditProvider() {
            @Override
            public void log(AuditRecord record) {
                // 普通 SQL 记录
                System.out.println("SQL 执行: " + record.getSqlId());
            }

            @Override
            public void logSlowSql(AuditRecord record) {
                // 慢 SQL 警告
                System.out.println("慢 SQL: " + record.getSqlId());
                System.out.println("耗时: " + record.getElapsedTime() + "ms");
                System.out.println("SQL: " + record.getSql());
            }

            @Override
            public void logFailure(AuditRecord record) {
                // SQL 执行失败记录
                System.err.println("SQL 失败: " + record.getSqlId());
                System.err.println("异常: " + record.getException());
            }
        };

        // 创建审计配置
        org.miaixz.bus.mapper.support.audit.AuditConfig config =
            org.miaixz.bus.mapper.support.audit.AuditConfig.builder()
                .enabled(true)
                .slowSqlThreshold(1000)  // 慢 SQL 阈值: 1 秒
                .logParameters(true)     // 记录 SQL 参数
                .logResults(false)       // 不记录查询结果
                .logAllSql(false)        // 仅记录慢 SQL
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

// 使用 TenantContext 设置租户 ID
TenantContext.setCurrentTenantId("tenant_001");
try {
    // 所有查询自动添加租户过滤
    userMapper.selectAll();
} finally {
    TenantContext.clear();
}

// 或使用 Lambda 方式(推荐)
TenantContext.runWithTenant("tenant_001", () -> {
    userMapper.selectAll();
});
```

#### 快速配置(推荐)

```java
@Configuration
public class MapperConfiguration {

    @Bean
    public TenantHandler tenantHandler() {
        // 方法 1: 最简单 - 只需提供获取租户 ID 的逻辑
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
        // 方法 2: 完整配置
        TenantConfig config = TenantConfig.builder()
            .mode(TenantMode.COLUMN)
            .column("tenant_id")
            .ignoreTables("sys_config", "sys_dict", "sys_log")
            .provider(() -> {
                // 从 Spring Security 获取租户 ID
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
    // ... MybatisInterceptor 设置保持相同
}
```

#### 忽略租户过滤

要临时忽略租户过滤(例如,管理员视图):

```java
// 临时忽略租户过滤
TenantContext.runIgnoreTenant(() -> {
    // 此处的查询不会包含租户过滤条件
    List<User> allUsers = userMapper.selectAll();
});

// 或手动控制
TenantContext.setIgnoreTenant(true);
try {
    // 查询所有租户的数据
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
      # 数据源 1 配置
      dev_db:
        table:
          prefix: dp_
        tenant:
          column: tenant_id
          ignore: sys_config,sys_dict,sys_log

      # 数据源 2 配置
      prod_db:
        table:
          prefix: prod_
        tenant:
          column: tenant_id
          ignore: sys_config,sys_dict,sys_log
```

-----

## 📋 注解参考

### 实体类注解

| 注解 | 描述 | 示例 |
| :--- | :--- | :--- |
| `@Table` | 指定表名 | `@Table("user")` |
| `@Id` | 标记主键字段 | `@Id` |
| `@KeyType` | 主键生成策略 | `@KeyType(KeyType.Type.AUTO)` |
| `@Column` | 指定列名 | `@Column("user_name")` |
| `@Version` | 乐观锁版本号 | `@Version` |
| `@TenantId` | 租户 ID 字段 | `@TenantId` |
| `@CreateTime` | 创建时间自动填充 | `@CreateTime` |
| `@UpdateTime` | 更新时间自动填充 | `@UpdateTime` |
| `@Ignore` | 忽略字段 | `@Ignore` |
| `@TableAudit` | 表级审计 | `@TableAudit` |
| `@Audit` | 字段级审计 | `@Audit` |

### 主键策略

```java
public enum Type {
    AUTO,        // 数据库自增
    IDENTITY,    // IDENTITY 主键
    UUID,        // UUID
    SNOWFLAKE,   // 雪花算法
    SEQUENCE     // 序列
}
```

-----

## 💡 最佳实践

### 1. 使用 Lambda 表达式构建条件

```java
// ✅ 推荐: 类型安全,支持重构
wrapper.eq(User::getName, "张三")
       .gt(User::getAge, 18);

// ❌ 不推荐: 字符串硬编码,容易出错
wrapper.eq("name", "张三")
       .gt("age", 18);
```

### 2. 批量操作使用 `insertBatch`/`updateBatch`

```java
// ✅ 推荐: 高性能批量插入
userMapper.insertBatch(users);  // 单条 SQL,速度快

// ❌ 不推荐: 循环插入
for (User user : users) {
    userMapper.insert(user);    // 多条 SQL,速度慢
}
```

### 3. 仅查询必要字段

```java
// ✅ 推荐: 减少网络传输
wrapper.select(User::getId, User::getName)
       .list();

// ❌ 不推荐: 查询所有字段
wrapper.list();  // SELECT *
```

### 4. 大数据集使用流式查询

```java
// ✅ 推荐: 使用游标,内存占用低
try (Cursor<User> cursor = userMapper.selectCursorByCondition(condition)) {
    cursor.forEach(user -> process(user));
}

// ❌ 不推荐: 一次性加载所有数据到内存
List<User> users = userMapper.selectByCondition(condition);  // 可能 OOM
```

### 5. 正确使用多租户

```java
// ✅ 推荐: 使用 Lambda 自动管理
TenantContext.runWithTenant("tenant_001", () -> {
    userMapper.selectAll();
});

// ❌ 不推荐: 手动管理可能导致上下文泄漏
TenantContext.setCurrentTenantId("tenant_001");
userMapper.selectAll();
// 容易忘记调用 clear()
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
                // 自定义 ID 生成逻辑
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
                .column("org_id")  // 不同的列名
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

# 或使用审计日志
bus:
  mapper:
    audit:
      enabled: true
      log-all-sql: true
      print-console: true
```

### Q4: 批量插入时如何获取生成的主键?

```java
// 方法 1: 使用 @KeyType(AUTO)
@Id
@KeyType(KeyType.Type.AUTO)
private Long id;

List<User> users = new ArrayList<>();
userMapper.insertBatch(users);
// 'users' 中的 'id' 字段将被自动填充

// 方法 2: 使用自定义键生成器
@Id
@KeyType(KeyType.Type.SNOWFLAKE)
private Long id;
```

### Q5: 如何实现逻辑删除?

```java
@Table("user")
public class User {
    @Logic  // 逻辑删除字段
    private Integer deleted;  // 0-未删除, 1-已删除
}

// 配置
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
// 方法 1: 抛出异常(严格模式)
TenantConfig config = TenantConfig.builder()
    .provider(() -> {
        String tenantId = getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("租户 ID 不能为空");
        }
        return tenantId;
    })
    .build();

// 方法 2: 返回默认值
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
| :--- | :--- | :--- | :--- |
| 8.x | 3.5.x+ | 3.x+ | 17+ |
| 7.x | 3.5.x+ | 2.x+ | 11+ |

-----

## 🚀 性能调优建议

### 1. 启用对象池

```yaml
bus:
  mapper:
    configurationProperties:
      provider:
        useOnce: false      # 禁用单次使用,启用对象复用
        initSize: 1024      # 初始池大小
        concurrency: 1000   # 并发级别
```

### 2. 启用 SQL 缓存

```java
TenantConfig config = TenantConfig.builder()
    .enableSqlCache(true)  // 启用 SQL 缓存
    .build();
```

### 3. 设置合理的批量大小

```java
// 建议批量插入的批量大小为 500-1000 条记录
List<List<User>> batches = Lists.partition(users, 500);
for (List<User> batch : batches) {
    userMapper.insertBatch(batch);
}
```

### 4. 使用列选择减少数据传输

```java
// 仅查询必要字段,可减少网络传输 50-90%
wrapper.select(User::getId, User::getName)
       .list();
```

-----

## 📊 性能对比

基于 JMH 基准测试结果:

### 批量插入性能(10,000 条记录)

| 框架 | 耗时 | 性能提升 |
| :--- | :--- | :--- |
| 传统循环 | $2500\text{ms}$ | - |
| MyBatis Flex | $700\text{ms}$ | $\uparrow 3.6\text{x}$ |
| **Bus Mapper** | **$180\text{ms}$** | **$\uparrow 13.9\text{x}$** |

### 查询性能(1,000 次操作)

| 框架 | 平均延迟 | QPS |
| :--- | :--- | :--- |
| MyBatis Flex | $14.5\text{ms}$ | $68/\text{s}$ |
| **Bus Mapper** | **$12\text{ms}$** | **$83/\text{s}$** |

### 缓存效率

| 框架 | 命中率 | 节省时间 |
| :--- | :--- | :--- |
| MyBatis Flex | $95\%$ | $520\text{ms}$ |
| **Bus Mapper** | **$99.5\%$** | **$890\text{ms}$** |

### 内存和 GC(1 小时)

| 指标 | Bus Mapper | MyBatis Flex |
| :--- | :--- | :--- |
| Full GC 次数 | $2-3$ | $5-7$ |
| GC 总时间 | $120\text{ms}$ | $280\text{ms}$ |

-----

## 🛠️ Mapper 方法列表

### 插入方法

```java
int insert(T entity);                      // 插入(所有字段)
int insertSelective(T entity);             // 插入(非空字段)
int insertBatch(List<T> entities);         // 批量插入
```

### 查询方法

```java
T selectById(I id);                        // 根据主键查询
List<T> selectByIds(Collection<I> ids);    // 批量查询
List<T> selectAll();                       // 查询全部
List<T> select(T entity);                  // 根据实体属性查询
List<T> selectByCondition(Condition<T> c); // 根据条件查询
long selectCount(T entity);                // 计数
Cursor<T> selectCursorByCondition(...);    // 游标查询
```

### 更新方法

```java
int updateByPrimaryKey(T entity);          // 根据主键更新
int updateByPrimaryKeySelective(T entity); // 根据主键更新(非空)
int updateBatch(List<T> entities);         // 批量更新
```

### 删除方法

```java
int deleteById(I id);                      // 根据主键删除
int deleteBatchByIds(Collection<I> ids);   // 批量删除
int delete(T entity);                      // 根据实体属性删除
```

### ConditionWrapper 方法(流式 API)

```java
.eq(User::getName, "张三")             // 等于
.ne(User::getStatus, 0)               // 不等于
.gt(User::getAge, 18)                 // 大于
.like(User::getName, "%张三%")        // 模糊查询
.between(User::getAge, 18, 65)        // 范围查询
.in(User::getRegion, list)            // 在集合中
.isNull(User::getEmail)               // 为空
.orderBy(User::getCreateTime, DESC)   // 排序
.select(User::getId, User::getName)   // 列选择
.limit(10)                            // 限制
.list()                               // 查询列表
.one()                                // 查询单条
.count()                              // 计数
.page(1, 20)                          // 分页
```

-----

## 🔧 配置示例

```yaml
mapper:
  # 全局配置,对所有数据库生效
  # 租户隔离
  tenant:
    column: tenant_id
    ignore: sys_tenant,sys_config,sys_dict

  # SQL 审计
  audit:
    enabled: true
    slow-sql-threshold: 500
    log-parameters: true
    print-console: true

  # 数据填充
  populate:
    created: true
    modified: true
    creator: true
    modifier: true

  # 数据可见性
  visible:
    enabled: true
    ignore: sys_admin_table

  # 表前缀
  table:
    value: prod_
    ignore: sys_log,sys_config

  # 按数据库配置(覆盖全局)
  configurationProperties:
    com_deepparser:
      table:
        prefix: dp_
        ignore: tenant,assets,license
      tenant:
        column: tenant_id
        ignore: tenant,assets,license
    # ......
```

-----

**由 Miaixz 团队用 ❤️ 构建**
