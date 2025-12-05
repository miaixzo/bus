# Bus Mapper

<p align="center">
    <strong>高性能、功能丰富的 MyBatis 增强框架</strong>
</p>

---

## 📖 项目简介

**Bus Mapper** 是一个基于 MyBatis 的企业级增强框架，在 MyBatis 的基础上**只做增强不做改变**，为简化开发、提高效率而生。

---

## ✨ 核心特性

### 🎯 基础功能

- **无侵入增强**：引入后不影响现有工程，如丝般顺滑
- **低性能损耗**：启动即自动注入 CRUD，性能几乎无损耗
- **强大的 CRUD**：内置通用 Mapper，少量配置实现单表全部操作
- **Lambda 类型安全**：使用 `Fn<T, R>` 函数式接口，编译时检查
- **灵活条件构造**：链式 API，支持复杂动态查询

### ⚡ 性能优化

| 特性 | 性能提升 | 说明 |
|-----|---------|------|
| **Multi-Values 批量插入** | ↑ 10-20x | 单次 SQL 插入多条记录 |
| **对象池化管理** | GC ↓ 60% | StringBuilder 复用，内存占用降低 |
| **智能缓存** | 命中率 99.5% | 元数据和 SQL 多级缓存 |
| **无锁并发** | QPS ↑ 15% | ConcurrentHashMap 无锁设计 |
| **列选择优化** | 网络 ↓ 90% | 按需加载字段，减少数据传输 |

### 🛡️ 企业功能

- **审计日志**：自动记录数据变更、操作人、SQL 语句和执行时间
- **多租户支持**：列隔离、Schema 隔离、独立数据库三种模式
- **权限控制**：数据权限过滤，防止越权访问
- **慢 SQL 监控**：自动检测慢查询，输出执行时间和 SQL
- **数据脱敏**：敏感字段自动脱敏处理

### 🌍 数据库支持

支持 **18 种** 主流和国产数据库：

**主流数据库**：MySQL / MariaDB, PostgreSQL, Oracle, SQL Server, SQLite, H2, Hsqldb

**国产数据库**：神舟通用（Oscar）, 瀚高数据库（CirroData）, 虚谷数据库（Xugudb）

**企业级数据库**：DB2, Informix, AS400, Firebird, HerdDB

---

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

#### 1. 添加依赖

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

---

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
    // 继承 BasicMapper 即可，无需编写任何方法
}
```

### 3. 基础 CRUD 操作

#### 插入

```java
// 插入单条
User user = new User();
user.setName("张三");
userMapper.insert(user);

// 插入（仅非空字段）
userMapper.insertSelective(user);

// 批量插入 - 高性能
List<User> users = new ArrayList<>();
// ... 添加数据
userMapper.insertBatch(users);  // 10,000 条仅需 150-200ms
```

#### 查询

```java
// 根据主键查询
User user = userMapper.selectById(1L);

// 查询所有
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
// 根据主键更新（所有字段）
userMapper.updateByPrimaryKey(user);

// 根据主键更新（仅非空字段）
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

### 4. Lambda 条件查询（类型安全）

```java
// 创建条件包装器
ConditionWrapper<User, Long> wrapper = mapper.wrapper();

// 基础条件查询
List<User> users = wrapper
    .eq(User::getAge, 25)
    .like(User::getName, "%张%")
    .isNotNull(User::getEmail)
    .orderBy(User::getCreateTime, Sort.ORDER.DESC)
    .list();

// 复杂条件
List<User> users = wrapper
    .eq(User::getStatus, 1)
    .like(User::getName, "%张%")
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

// 列选择（只查询需要的字段）
List<User> users = wrapper
    .select(User::getId, User::getName, User::getEmail)
    .eq(User::getStatus, 1)
    .list();

// 分页查询
Page<User> page = wrapper
    .eq(User::getStatus, 1)
    .orderBy(User::getCreateTime, Sort.ORDER.DESC)
    .page(1, 20);  // 第 1 页，每页 20 条

// 统计数量
long count = wrapper
    .eq(User::getStatus, 1)
    .count();
```

### 5. 高级查询

#### 流式查询（大数据集）

```java
// 使用游标，避免一次性加载到内存
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
@TableAudit  // 表级别审计
public class User {
    @Audit  // 字段级别审计
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
                // 普通SQL记录
                System.out.println("SQL执行: " + record.getSqlId());
            }

            @Override
            public void logSlowSql(AuditRecord record) {
                // 慢SQL告警
                System.out.println("慢SQL: " + record.getSqlId());
                System.out.println("耗时: " + record.getElapsedTime() + "ms");
                System.out.println("SQL: " + record.getSql());
            }

            @Override
            public void logFailure(AuditRecord record) {
                // SQL执行失败记录
                System.err.println("SQL失败: " + record.getSqlId());
                System.err.println("异常: " + record.getException());
            }
        };

        // 创建审计配置
        org.miaixz.bus.mapper.support.audit.AuditConfig config =
            org.miaixz.bus.mapper.support.audit.AuditConfig.builder()
                .enabled(true)
                .slowSqlThreshold(1000)  // 慢SQL阈值: 1秒
                .logParameters(true)     // 记录SQL参数
                .logResults(false)       // 不记录查询结果
                .logAllSql(false)        // 仅记录慢SQL
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

// 或使用 Lambda 方式
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
        // 方式1: 最简单 - 只需提供租户ID获取逻辑
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
        // 方式2: 完整配置
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

    @Bean
    public MybatisInterceptor mybatisInterceptor(TenantHandler tenantHandler) {
        MybatisInterceptor interceptor = new MybatisInterceptor();
        interceptor.addHandler(tenantHandler);
        return interceptor;
    }
}
```

#### 忽略租户过滤

某些场景下需要忽略租户过滤（如管理员查看所有数据）:

```java
// 临时忽略租户过滤
TenantContext.runIgnoreTenant(() -> {
    // 这里的查询不会添加租户过滤条件
    List<User> allUsers = userMapper.selectAll();
});

// 或者手动控制
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
      # 数据源1配置
      dev_db:
        table:
          prefix: dp_
        tenant:
          column: tenant_id
          ignore: sys_config,sys_dict,sys_log

      # 数据源2配置
      prod_db:
        table:
          prefix: prod_
        tenant:
          column: tenant_id
          ignore: sys_config,sys_dict,sys_log
```

---

## 📋 注解说明

### 实体类注解

| 注解 | 说明 | 示例 |
|-----|------|------|
| `@Table` | 指定表名 | `@Table("user")` |
| `@Id` | 标记主键字段 | `@Id` |
| `@KeyType` | 主键生成策略 | `@KeyType(KeyType.Type.AUTO)` |
| `@Column` | 指定列名 | `@Column("user_name")` |
| `@Version` | 乐观锁版本号 | `@Version` |
| `@TenantId` | 租户ID字段 | `@TenantId` |
| `@CreateTime` | 创建时间自动填充 | `@CreateTime` |
| `@UpdateTime` | 更新时间自动填充 | `@UpdateTime` |
| `@Ignore` | 忽略字段 | `@Ignore` |
| `@TableAudit` | 表级别审计 | `@TableAudit` |
| `@Audit` | 字段级别审计 | `@Audit` |

### 主键策略

```java
public enum Type {
    AUTO,        // 数据库自增
    IDENTITY,    // IDENTITY主键
    UUID,        // UUID
    SNOWFLAKE,   // 雪花算法
    SEQUENCE     // 序列
}
```

---

## 💡 最佳实践

### 1. 使用 Lambda 表达式构建条件

```java
// ✅ 推荐：类型安全，支持重构
wrapper.eq(User::getName, "张三")
       .gt(User::getAge, 18);

// ❌ 不推荐：字符串硬编码，容易出错
wrapper.eq("name", "张三")
       .gt("age", 18);
```

### 2. 批量操作使用 insertBatch/updateBatch

```java
// ✅ 推荐：高性能批量插入
userMapper.insertBatch(users);  // 单次SQL，速度快

// ❌ 不推荐：循环插入
for (User user : users) {
    userMapper.insert(user);    // 多次SQL，速度慢
}
```

### 3. 只查询需要的字段

```java
// ✅ 推荐：减少网络传输
wrapper.select(User::getId, User::getName)
       .list();

// ❌ 不推荐：查询所有字段
wrapper.list();  // SELECT *
```

### 4. 大数据集使用流式查询

```java
// ✅ 推荐：使用游标，内存占用低
try (Cursor<User> cursor = userMapper.selectCursorByCondition(condition)) {
    cursor.forEach(user -> process(user));
}

// ❌ 不推荐：一次性加载到内存
List<User> users = userMapper.selectByCondition(condition);  // 可能OOM
```

### 5. 合理使用多租户

```java
// ✅ 推荐：使用 Lambda 自动管理
TenantContext.runWithTenant("tenant_001", () -> {
    userMapper.selectAll();
});

// ❌ 不推荐：手动管理容易忘记清理
TenantContext.setCurrentTenantId("tenant_001");
userMapper.selectAll();
// 忘记调用 clear() 可能导致租户ID泄露
```

---

## ❓ 常见问题

### Q1: 如何自定义主键生成策略？

```java
@Configuration
public class KeyGeneratorConfig {
    @Bean
    public KeyGenerator customKeyGenerator() {
        return new KeyGenerator() {
            @Override
            public Object generateKey() {
                // 自定义ID生成逻辑
                return IdWorker.getId();
            }
        };
    }
}
```

### Q2: 如何处理多数据源场景？

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

### Q3: 如何调试生成的SQL？

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

### Q4: 批量插入时如何获取生成的主键？

```java
// 方式1: 使用 @KeyType(AUTO)
@Id
@KeyType(KeyType.Type.AUTO)
private Long id;

List<User> users = new ArrayList<>();
userMapper.insertBatch(users);
// users 中的 id 会自动回填

// 方式2: 使用自定义主键生成器
@Id
@KeyType(KeyType.Type.SNOWFLAKE)
private Long id;
```

### Q5: 如何实现软删除？

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

### Q6: 租户ID为空时如何处理？

```java
// 方式1: 抛出异常（严格模式）
TenantConfig config = TenantConfig.builder()
    .provider(() -> {
        String tenantId = getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("租户ID不能为空");
        }
        return tenantId;
    })
    .build();

// 方式2: 返回默认值
TenantConfig config = TenantConfig.builder()
    .provider(() -> {
        String tenantId = getTenantId();
        return tenantId != null ? tenantId : "default";
    })
    .build();
```

---

## 🔄 版本兼容性

| Bus Mapper 版本 | MyBatis 版本 | Spring Boot 版本 | JDK 版本 |
|----------------|-------------|------------------|---------|
| 8.x | 3.5.x+ | 3.x+ | 17+ |
| 7.x | 3.5.x+ | 2.x+ | 11+ |

---

## 🚀 性能调优建议

### 1. 启用对象池化

```yaml
bus:
  mapper:
    configurationProperties:
      provider:
        useOnce: false      # 关闭单次使用，启用对象复用
        initSize: 1024      # 初始池大小
        concurrency: 1000   # 并发级别
```

### 2. 启用SQL缓存

```java
TenantConfig config = TenantConfig.builder()
    .enableSqlCache(true)  // 启用SQL缓存
    .build();
```

### 3. 合理设置批量大小

```java
// 批量插入建议每批 500-1000 条
List<List<User>> batches = Lists.partition(users, 500);
for (List<User> batch : batches) {
    userMapper.insertBatch(batch);
}
```

### 4. 使用列选择减少数据传输

```java
// 只查询需要的字段，可减少 50-90% 的网络传输
wrapper.select(User::getId, User::getName)
       .list();
```

---

## 📊 性能对比

基于 JMH 基准测试结果：

### 批量插入性能（10,000 条）

| 框架 | 耗时 | 性能提升 |
|-----|------|---------|
| 传统循环 | 2500ms | - |
| MyBatis Flex | 700ms | ↑ 3.6x |
| **Bus Mapper** | **180ms** | **↑ 13.9x** |

### 查询性能（1,000 次）

| 框架 | 平均延迟 | QPS |
|-----|---------|-----|
| MyBatis Flex | 14.5ms | 68/s |
| **Bus Mapper** | **12ms** | **83/s** |

### 缓存效率

| 框架 | 命中率 | 节省时间 |
|-----|--------|---------|
| MyBatis Flex | 95% | 520ms |
| **Bus Mapper** | **99.5%** | **890ms** |

### 内存与 GC（1 小时）

| 指标 | Bus Mapper | MyBatis Flex |
|-----|-----------|--------------|
| Full GC 次数 | 2-3 | 5-7 |
| GC 总耗时 | 120ms | 280ms |

详细报告：[性能评估文档](../../docs/bus-mapper-performance-evaluation.md)

---

## 🛠️ Mapper 方法清单

### 插入方法

```java
int insert(T entity);                      // 插入（所有字段）
int insertSelective(T entity);             // 插入（非空字段）
int insertBatch(List<T> entities);         // 批量插入
```

### 查询方法

```java
T selectById(I id);                        // 根据主键查询
List<T> selectByIds(Collection<I> ids);    // 批量查询
List<T> selectAll();                       // 查询所有
List<T> select(T entity);                  // 根据实体查询
List<T> selectByCondition(Condition<T> c); // 根据条件查询
long selectCount(T entity);                // 统计数量
Cursor<T> selectCursorByCondition(...);    // 游标查询
```

### 更新方法

```java
int updateByPrimaryKey(T entity);          // 根据主键更新
int updateByPrimaryKeySelective(T entity); // 根据主键更新（非空）
int updateBatch(List<T> entities);         // 批量更新
```

### 删除方法

```java
int deleteById(I id);                      // 根据主键删除
int deleteBatchByIds(Collection<I> ids);   // 批量删除
int delete(T entity);                      // 根据实体删除
```

### ConditionWrapper 方法

```java
.eq(User::getName, "张三")                 // 等于
.ne(User::getStatus, 0)                   // 不等于
.gt(User::getAge, 18)                     // 大于
.like(User::getName, "%张%")              // 模糊
.between(User::getAge, 18, 65)            // 范围
.in(User::getRegion, list)                // 包含
.isNull(User::getEmail)                   // 为空
.orderBy(User::getCreateTime, DESC)       // 排序
.select(User::getId, User::getName)       // 列选择
.limit(10)                                // 限制
.list()                                   // 查询列表
.one()                                    // 查询单个
.count()                                  // 统计
.page(1, 20)                              // 分页
```

---

## 🔧 配置示例

```yaml
mapper:
  # 全局配置，所有数据库生效
  # 租户隔离
  tenant:
    column: tenant_id
    ignore: sys_tenant,sys_config,sys_dict

  # SQL审计
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

  # 数据表前缀
  table:
    value: prod_
    ignore: sys_log,sys_config

  # 按数据库配置
  configurationProperties:
    com_deepparser:
      table:
        prefix: dp_
        ignore: tenant,assets,license
      tenant:
        column: tenant_id
        ignore: tenant,assets,license
    ......
```

---
