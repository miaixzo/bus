# 🚀 Bus Mapper: High-Performance MyBatis Enhancement Framework

<p align="center">
<strong>High-Performance, Feature-Rich MyBatis Enhancement Framework</strong>
</p>

-----

## 📖 Project Introduction

**Bus Mapper** is an enterprise-level enhancement framework based on MyBatis. It is designed to **enhance without changing** the core MyBatis functionality, created to simplify development and improve efficiency.

-----

## ✨ Core Features

### 🎯 Basic Capabilities

* **Non-Intrusive Enhancement**: Seamless integration without affecting existing projects.
* **Minimal Performance Overhead**: Automatic CRUD injection upon startup with virtually no performance loss.
* **Powerful CRUD**: Built-in generic Mapper allows for complete single-table operations with minimal configuration.
* **Lambda Type Safety**: Uses the Fn<T, R> functional interface for compile-time safety checks.
* **Flexible Condition Builder**: Chain-style API supporting complex dynamic queries.

### ⚡ Performance Optimization

| Feature | Performance Gain | Description |
| :--- | :--- | :--- |
| **Multi-Values Batch Insert** | $\uparrow 10-20\text{x}$ | Inserts multiple records in a single SQL statement. |
| **Object Pooling Management** | $\text{GC } \downarrow 60\%$ | `StringBuilder` reuse to reduce memory footprint. |
| **Intelligent Caching** | $\text{Hit Rate } 99.5\%$ | Multi-level caching for metadata and SQL. |
| **Lock-Free Concurrency** | $\text{QPS } \uparrow 15\%$ | Lock-free design using `ConcurrentHashMap`. |
| **Column Selection Optimization** | $\text{Network } \downarrow 90\%$ | Loads fields on demand, reducing data transfer. |

### 🛡️ Enterprise Features

* **Audit Log**: Automatically records data changes, operator, SQL statement, and execution time.
* **Multi-Tenancy Support**: Column isolation, Schema isolation, and independent database modes.
* **Permission Control**: Data permission filtering to prevent unauthorized access.
* **Slow SQL Monitoring**: Automatic detection of slow queries, outputting execution time and SQL.
* **Data Masking**: Automatic sensitive data masking for protected fields.

### 🌍 Database Support

Supports **18** mainstream and domestic databases:

**Mainstream Databases**: MySQL / MariaDB, PostgreSQL, Oracle, SQL Server, SQLite, H2, Hsqldb

**Domestic Databases**: Shenzhou General (Oscar), HandGo Database (CirroData), Xugu Database (Xugudb)

**Enterprise Databases**: DB2, Informix, AS400, Firebird, Herddb

-----

## 🚀 Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-mapper</artifactId>
    <version>x.x.x</version>
</dependency>
```

### Spring Boot Integration

#### 1\. Add Starter Dependency

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-starter</artifactId>
    <version>x.x.x</version>
</dependency>
```

#### 2\. Configure DataSource

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

# Bus Mapper Configuration
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

The Spring Boot configuration keys remain unchanged. Internally, `bus-mapper` now exposes `MapperOptions` as the pure
Java/MyBatis option model for mapper properties. Plugin assembly, configuration normalization, and type-resolution
helpers live in separate components. The starter keeps Spring Boot binding, resource resolution, mapper scanning, and
lifecycle wiring, so existing `bus.mapper.*` YAML does not need migration.

#### 3\. Enable Mapper Scanning

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

## 📝 Usage Examples

### 1\. Define Entity Class

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

### 2\. Create Mapper Interface

```java
@Repository
public interface UserMapper extends BasicMapper<User, Long> {
    // Inheriting BasicMapper is sufficient; no methods need to be written.
}
```

### 3\. Basic CRUD Operations

#### Insert

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

#### Query

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

#### Update

```java
// Update by Primary Key (all fields)
userMapper.updateByPrimaryKey(user);

// Update by Primary Key (non-null fields only)
userMapper.updateByPrimaryKeySelective(user);

// Batch Update
userMapper.updateBatch(users);
```

#### Delete

```java
// Delete by Primary Key
userMapper.deleteById(1L);

// Batch Delete
userMapper.deleteBatchByIds(Arrays.asList(1L, 2L, 3L));
```

### 4\. Lambda Condition Query (Type Safe)

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

### 5\. Advanced Query

#### Streaming Query (Large Datasets)

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

### 6\. Audit Log

#### Mark Audit Fields with Annotations

```java
@Table("user")
@TableAudit  // Table-level audit
public class User {
    @Audit  // Field-level audit
    private String email;
}
```

#### Configure Audit Handler

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

### 7\. Multi-Tenancy

#### Basic Configuration

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

#### Quick Configuration (Recommended)

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

#### Full Configuration

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

#### Ignoring Tenant Filtering

To temporarily ignore tenant filtering (e.g., for administrator views):

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

#### Configuration File Approach

```yaml
bus:
  mapper:
    configurationProperties:
      # Data Source 1 Configuration
      dev_db:
        table:
          prefix: dp_
        tenant:
          column: tenant_id
          ignore: sys_config,sys_dict,sys_log

      # Data Source 2 Configuration
      prod_db:
        table:
          prefix: prod_
        tenant:
          column: tenant_id
          ignore: sys_config,sys_dict,sys_log
```

-----

## 📋 Annotation Reference

### Entity Class Annotations

| Annotation | Description | Example |
| :--- | :--- | :--- |
| `@Table` | Specifies the table name | `@Table("user")` |
| `@Id` | Marks the primary key field | `@Id` |
| `@KeyType` | Primary key generation strategy | `@KeyType(KeyType.Type.AUTO)` |
| `@Column` | Specifies the column name | `@Column("user_name")` |
| `@Version` | Optimistic locking version number | `@Version` |
| `@TenantId` | Tenant ID field | `@TenantId` |
| `@CreateTime` | Creation time auto-fill | `@CreateTime` |
| `@UpdateTime` | Update time auto-fill | `@UpdateTime` |
| `@Ignore` | Ignores the field | `@Ignore` |
| `@TableAudit` | Table-level auditing | `@TableAudit` |
| `@Audit` | Field-level auditing | `@Audit` |

### Primary Key Strategies

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

## 💡 Best Practices

### 1\. Use Lambda Expressions for Condition Building

```java
// ✅ Recommended: Type safe, supports refactoring
wrapper.eq(User::getName, "John Doe")
       .gt(User::getAge, 18);

// ❌ Not Recommended: String hardcoding, error prone
wrapper.eq("name", "John Doe")
       .gt("age", 18);
```

### 2\. Use `insertBatch`/`updateBatch` for Bulk Operations

```java
// ✅ Recommended: High-performance batch insert
userMapper.insertBatch(users);  // Single SQL statement, fast

// ❌ Not Recommended: Loop insertion
for (User user : users) {
    userMapper.insert(user);    // Multiple SQL statements, slow
}
```

### 3\. Query Only Necessary Fields

```java
// ✅ Recommended: Reduces network transfer
wrapper.select(User::getId, User::getName)
       .list();

// ❌ Not Recommended: Queries all fields
wrapper.list();  // SELECT *
```

### 4\. Use Streaming Query for Large Datasets

```java
// ✅ Recommended: Uses cursor, low memory footprint
try (Cursor<User> cursor = userMapper.selectCursorByCondition(condition)) {
    cursor.forEach(user -> process(user));
}

// ❌ Not Recommended: Loads all data into memory at once
List<User> users = userMapper.selectByCondition(condition);  // Potential OOM
```

### 5\. Proper Multi-Tenancy Usage

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

## ❓ Frequently Asked Questions

### Q1: How to customize the primary key generation strategy?

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

### Q2: How to handle multi-data source scenarios?

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

### Q3: How to debug generated SQL?

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

### Q4: How to retrieve generated primary keys during batch insertion?

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

### Q5: How to implement soft deletion?

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

### Q6: How to handle scenarios where the Tenant ID is null?

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

## 🔄 Version Compatibility

| Bus Mapper Version | MyBatis Version | Spring Boot Version | JDK Version |
| :--- | :--- | :--- | :--- |
| 8.x | 3.5.x+ | 3.x+ | 17+ |
| 7.x | 3.5.x+ | 2.x+ | 11+ |

-----

## 🚀 Performance Tuning Suggestions

### 1\. Enable Object Pooling

```yaml
bus:
  mapper:
    configurationProperties:
      provider:
        useOnce: false      # Disable single-use, enable object reuse
        initSize: 1024      # Initial pool size
        concurrency: 1000   # Concurrency level
```

### 2\. Enable SQL Caching

```java
TenantConfig config = TenantConfig.builder()
    .enableSqlCache(true)  // Enable SQL caching
    .build();
```

### 3\. Set Reasonable Batch Size

```java
// Recommended batch size for insertion is 500-1000 records
List<List<User>> batches = Lists.partition(users, 500);
for (List<User> batch : batches) {
    userMapper.insertBatch(batch);
}
```

### 4\. Use Column Selection to Reduce Data Transfer

```java
// Only query necessary fields, can reduce network transfer by 50-90%
wrapper.select(User::getId, User::getName)
       .list();
```

-----

## 📊 Performance Comparison

Based on JMH benchmark results:

### Batch Insert Performance (10,000 Records)

| Framework | Time Taken | Performance Increase |
| :--- | :--- | :--- |
| Traditional Loop | $2500\text{ms}$ | - |
| MyBatis Flex | $700\text{ms}$ | $\uparrow 3.6\text{x}$ |
| **Bus Mapper** | **$180\text{ms}$** | **$\uparrow 13.9\text{x}$** |

### Query Performance (1,000 Operations)

| Framework | Average Latency | QPS |
| :--- | :--- | :--- |
| MyBatis Flex | $14.5\text{ms}$ | $68/\text{s}$ |
| **Bus Mapper** | **$12\text{ms}$** | **$83/\text{s}$** |

### Caching Efficiency

| Framework | Hit Rate | Time Saved |
| :--- | :--- | :--- |
| MyBatis Flex | $95\%$ | $520\text{ms}$ |
| **Bus Mapper** | **$99.5\%$** | **$890\text{ms}$** |

### Memory and GC (1 Hour)

| Metric | Bus Mapper | MyBatis Flex |
| :--- | :--- | :--- |
| Full GC Count | $2-3$ | $5-7$ |
| Total GC Time | $120\text{ms}$ | $280\text{ms}$ |

Detailed Report: [可疑链接已删除]

-----

## 🛠️ Mapper Method List

### Insertion Methods

```java
int insert(T entity);                      // Insert (all fields)
int insertSelective(T entity);             // Insert (non-null fields)
int insertBatch(List<T> entities);         // Batch insert
```

### Query Methods

```java
T selectById(I id);                        // Query by primary key
List<T> selectByIds(Collection<I> ids);    // Batch query
List<T> selectAll();                       // Query all
List<T> select(T entity);                  // Query by entity properties
List<T> selectByCondition(Condition<T> c); // Query by condition
long selectCount(T entity);                // Count
Cursor<T> selectCursorByCondition(...);    // Cursor query
```

### Update Methods

```java
int updateByPrimaryKey(T entity);          // Update by primary key
int updateByPrimaryKeySelective(T entity); // Update by primary key (non-null)
int updateBatch(List<T> entities);         // Batch update
```

### Deletion Methods

```java
int deleteById(I id);                      // Delete by primary key
int deleteBatchByIds(Collection<I> ids);   // Batch delete
int delete(T entity);                      // Delete by entity properties
```

### ConditionWrapper Methods (Fluent API)

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

## Entity Schema Initialization

Bus Mapper can initialize database table structures from entity metadata. It can create missing tables, create missing
columns, create primary keys, create foreign keys, create indexes, create unique indexes, and apply explicitly allowed
column type or length changes during application startup.

The capability is implemented by `bus-mapper`. Spring Boot only binds `bus.mapper.schema` configuration and triggers
startup execution. `Dialect` is the single database capability entrypoint and is composed of `PagingBehavior`,
`OptionsBehavior`, `UpsertBehavior`, and `SchemaBehavior`.

The default is disabled:

```yaml
bus:
  mapper:
    schema:
      enabled: false
```

### Supported Schema Changes

| Change | Supported | Required flag |
| :--- | :--- | :--- |
| Create missing table | Yes | `allow-create-table: true` |
| Add missing column | Yes | `allow-add-column: true` |
| Create primary key | Yes | `allow-create-primary-key: true` |
| Create composite primary key | Yes | `allow-create-primary-key: true` |
| Create normal index | Yes | `allow-create-index: true` |
| Create unique index | Yes | `allow-create-unique: true` |
| Create composite index | Yes | `allow-create-index: true` |
| Create foreign key | Yes | `allow-create-foreign-key: true` |
| Change SQL type | Yes | `allow-modify-type: true` |
| Expand varchar length | Yes | `allow-expand-length: true` |
| Shrink varchar length | Blocked by default | `allow-shrink-length: true`, `allow-dangerous: true`, and whitelist |
| Drop column, index, primary key, or foreign key | Blocked by default | matching drop flag, `allow-dangerous: true`, and whitelist |

Supported modes:

| Mode | Behavior |
| :--- | :--- |
| `NONE` | Does not read metadata, generate SQL, or execute SQL. |
| `SCRIPT` | Reads metadata and writes generated SQL; it does not execute DDL. |
| `CREATE` | Creates missing tables only; existing tables are skipped. |
| `VALIDATE` | Reads metadata and reports differences; it does not execute DDL. |
| `UPDATE` | Executes only differences explicitly allowed by configuration. |

### Existing Table Behavior

When a table already exists, Bus Mapper reads current database metadata and compares it with entity metadata. It only
executes differences that are both supported by the active dialect and enabled by configuration. Missing indexes,
unique indexes, primary keys, and foreign keys can be added to existing tables. Existing field type changes require
`allow-modify-type: true`, and length expansion requires `allow-expand-length: true`.

Destructive changes remain blocked unless the matching operation flag, `allow-dangerous: true`, and
`dangerous-whitelist` all allow the same difference.

### Spring Boot Startup Example

```yaml
bus:
  mapper:
    basePackage:
      ai.deepparser.nexus.mapper
    schema:
      enabled: true
      mode: UPDATE
      dry-run: false
      print-sql: true
      fail-fast: true
      entity-packages:
        - ai.deepparser.nexus.entity
      allow-create-table: true
      allow-add-column: true
      allow-create-primary-key: true
      allow-create-index: true
      allow-create-unique: true
      allow-create-foreign-key: true
      allow-modify-type: true
      allow-expand-length: true
```

`entity-packages` scans entity classes annotated with `@Entity` or `@Table`; when `include-entities` is empty, every discovered entity is eligible for initialization.

### Per-Database Initialization

When a project configures multiple databases through `configurationProperties.namespaces`, `schema` can be placed under
the matching namespace. Table prefixes are not duplicated under `schema`; initialization reads the existing
`table.prefix` / `table.ignore` table-prefix plugin configuration from the same namespace.
Table prefixes are compatible with both global and datasource-bound configuration: `namespace.table.prefix` wins first,
then `shared.table.prefix`, `default.table.prefix`, or the top-level `bus.mapper.prefix` is used.

```yaml
bus:
  mapper:
    configurationProperties:
      namespaces:
        - name: com_deepparser
          table:
            prefix: dp_
            ignore: tenant,assets,license
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
              - ai.deepparser.nexus.entity
            include-entities:
              - ai.deepparser.nexus.entity.License
              - ai.deepparser.nexus.entity.Token
            allow-create-table: true
            allow-add-column: true
            allow-create-primary-key: true
            allow-create-index: true
```

`bus.mapper.schema` remains compatible: when no namespace-level `schema` exists, the legacy global configuration runs as
before. When namespace-level `schema` exists, the global `bus.mapper.schema` object is used as the default template for
each namespace so the same entities are not initialized a second time globally. If the datasource bean name differs from
the namespace name, set `schema.datasource-key` in that namespace. `configurationProperties.shared.schema` can also
provide default schema options for all namespaces, with each namespace allowed to override them.

Use `SCRIPT` first in production-like environments to review generated DDL before allowing execution:

```yaml
bus:
  mapper:
    schema:
      enabled: true
      mode: SCRIPT
      dry-run: false
      script-location: ./target/bus-mapper-schema.sql
      entity-packages:
        - ai.deepparser.nexus.entity
      allow-create-table: true
      allow-create-primary-key: true
      allow-create-index: true
      allow-create-unique: true
      allow-create-foreign-key: true
```

### Entity Example

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
@Table(name = "dp_user")
public class UserEntity {

    @Id
    @Column(nullable = false)
    private Long id;

    @Column(length = 128)
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

The example above creates:

* `dp_user` and `dp_order` when they do not exist.
* Primary keys on `id`.
* Normal composite index `idx_dp_order_user_code(user_id, code)`.
* Unique index `uk_dp_order_code(code)`.
* Foreign key `fk_dp_order_user(user_id) references dp_user(id)`.
* `remark VARCHAR(512)` through `@Column(length = 512)`.
* `amount BIGINT` because the Java type is `Long`.

The relationship field annotated with `@JoinColumn` is used to build the foreign key. Keep the physical join column
field, such as `userId`, on the entity when mapper CRUD needs to read or write the column value.

### Composite Primary Key Example

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

This creates `PRIMARY KEY (role_id, permission_id)` and a composite index on the same two columns.

### Type and Length Change Example

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

With `mode: UPDATE`, `allow-modify-type: true`, and `allow-expand-length: true`, Bus Mapper can update an existing table
from `text_value INTEGER` to `VARCHAR(512)`, from `big_value INTEGER` to `BIGINT`, and from `wide_value VARCHAR(32)` to
`VARCHAR(512)`.

Type mapping is Java-type based. `int(4)` and `int(8)` are not Java length mappings and are not generated by default.
Use `@Column(length = 512)` to generate `VARCHAR(512)`. Use `@Column(columnDefinition = "...")` when a native database
definition such as `int(8) unsigned` is required.

### Programmatic Initialization Example

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

### PostgreSQL Integration Test Example

`bus-mapper` includes a PostgreSQL integration test that keeps visible test tables and data:

```bash
BUS_MAPPER_POSTGRES_PASSWORD='your-password' mvn -B -ntp -f bus-mapper/pom.xml \
  -Dtest=PostgreSqlEntitySchemaInitializerIntegrationTest \
  -Dbus.mapper.postgres.it=true \
  -Dbus.mapper.postgres.url='jdbc:postgresql://localhost:5432/example?connectTimeout=8&socketTimeout=30&sslmode=disable' \
  -Dbus.mapper.postgres.user=postgres \
  test
```

The test writes only to `dp_postgres_test_parent`, `dp_postgres_test_child`, `dp_postgres_test_composite`, and
`dp_postgres_test_type`. It does not drop, truncate, or delete data.

### Dialect Coverage

All built-in dialects provide schema operations: MySQL, PostgreSQL, H2, SQLite, Firebird, Oscar, Oracle9i, SQL Server,
Polardb, HerdDB, SQL Server 2012, DB2, AS/400, HSQLDB, CirroData, Informix, Oracle, XuguDB, and Dameng. Each dialect
keeps its existing pagination and UPSERT behavior while exposing schema and metadata capabilities through
`OptionsBehavior.types()` and `SchemaBehavior` methods implemented directly by each dialect. Schema SQL rules belong to
the corresponding dialect class under `org.miaixz.bus.mapper.dialect`, so MySQL DDL is in `MySql`, PostgreSQL DDL is in
`PostgreSql`, and H2 DDL is in `H2`.

This feature does not use Flyway, does not use Liquibase, and does not create a migration history table.

-----

## 🔧 Configuration Example

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

  # Table Prefix
  table:
    value: prod_
    ignore: sys_log,sys_config

  # Per-Database Configuration (overrides global)
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

Would you like to search for a tutorial video on YouTube for getting started with Bus Mapper?
