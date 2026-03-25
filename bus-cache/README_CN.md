# ⚡ Bus Cache：高性能多级缓存框架

<p align="center">
<strong>支持多种存储后端的统一缓存接口</strong>
</p>

-----

## 📖 项目简介

**Bus Cache** 是一个企业级缓存框架，提供统一的缓存抽象，支持多种存储实现。它在保持一致 API 的同时与各种缓存提供程序无缝集成，使开发者能够在不更改代码的情况下切换不同的缓存技术。

该框架支持通过注解进行声明式缓存、自动缓存键生成、缓存穿透预防以及全面的指标监控。

-----

## ✨ 核心功能

### 🎯 统一缓存接口

* **提供程序无关**：所有缓存实现的单一 API（Redis、Caffeine、Memcached 等）
* **零迁移成本**：仅通过配置即可在缓存提供程序之间切换
* **类型安全泛型**：完全支持泛型键值对，具有编译时安全性
* **批量操作**：优化的批量读/写操作，提高性能

### ⚡ 多种缓存实现

| 实现 | 使用场景 | 性能 |
| :--- | :--- | :--- |
| **MemoryCache** | 本地内存缓存 | $\text{延迟 } < 1\text{ms}$ |
| **CaffeineCache** | 高性能本地缓存 | 命中率 $> 95\%$ |
| **GuavaCache** | 基于 Guava 的本地缓存 | 命中率 $> 90\%$ |
| **RedisCache** | 分布式单节点 Redis | 网络延迟 |
| **RedisClusterCache** | 分布式 Redis 集群 | 网络延迟 |
| **MemcachedCache** | Memcached 分布式缓存 | 网络延迟 |
| **NoOpCache** | 测试/无操作缓存 | N/A |

### 🚀 声明式缓存

* **@Cached**：读通缓存，自动写入未命中
* **@CachedGet**：只读缓存访问，不自动更新
* **@Invalid**：基于方法执行的缓存失效
* **@CacheKey**：使用 SpEL 表达式自定义缓存键生成

### 🛡️ 高级功能

* **缓存穿透预防**：自动为 null 结果插入占位符
* **SpEL 支持**：使用 Spring 表达语言动态生成缓存键
* **条件缓存**：基于运行时条件通过 SpEL 进行缓存
* **灵活过期**：支持条目级或全局 TTL 配置，`CacheExpire.FOREVER`（`0`）表示永不过期
* **TTL 续期**：`renew(key, expire)` 可在不改写键的前提下刷新已有条目的过期时间
* **原子计数器**：`increment(key)` 可用于限流、序列号和轻量指标统计
* **指标集成**：内置缓存命中率和监控
* **多键缓存**：基于集合的缓存键批量操作

-----

## 🚀 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-cache</artifactId>
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

#### 2. 配置缓存

`CacheProperties`（`bus.cache.*`）支持以下字段：

| 配置项 | 类型 | 说明 |
| :--- | :--- | :--- |
| `type` | `String` | `Collector` 实现类的全限定名 |
| `prefix` | `String` | 应用于所有 Key 的全局缓存 Key 前缀 |
| `timeout` | `String` | 默认过期时间（如 `"3600000"`，主要用于 Redis） |
| `provider.url` | `String` | 数据库型 Collector 的 JDBC URL（MySQL、H2、SQLite 等） |
| `provider.username` | `String` | 数据库用户名 |
| `provider.password` | `String` | 数据库密码 |

缓存实例（`MemoryCache`、`CaffeineCache`、`RedisCache` 等）是**接口类型**，必须注册为 Spring Bean，无法通过 YAML 直接绑定。

**示例：内存 Collector（进程内，重启清零）**

```yaml
bus:
  cache:
    type: org.miaixz.bus.cache.collect.MemoryCollector
    prefix: myapp
```

**示例：MySQL 持久化 Collector（重启不丢数据）**

```yaml
bus:
  cache:
    type: org.miaixz.bus.cache.collect.MySQLCollector
    prefix: myapp
    provider:
      url: jdbc:mysql://localhost:3306/mydb
      username: root
      password: secret
```

**示例：bus-metrics 适配器（Prometheus / Micrometer / OTel）**

```yaml
bus:
  cache:
    type: org.miaixz.bus.metrics.builtin.CacheMetricsAdapter
    prefix: myapp
```

#### 3. 启用缓存

```java
@SpringBootApplication
@EnableCache
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CacheX<String, Object> memoryCache() {
        return new MemoryCache<>(1000, 180000);
    }

    @Bean
    public CacheX<String, Object> caffeineCache() {
        return new CaffeineCache<>(10000, 3600000);
    }

    @Bean
    public CacheX<String, Object> redisCache(JedisPool jedisPool) {
        return new RedisCache<>(jedisPool, new Hessian2Serializer());
    }
}
```

-----

## 📝 使用示例

### 1. 基础缓存操作

#### 使用 MemoryCache

```java
@Service
public class UserService {

    private final CacheX<String, User> cache;

    public UserService() {
        // 创建本地内存缓存
        this.cache = new MemoryCache<>(1000, 180000);
    }

    public User getUser(String userId) {
        // 尝试从缓存读取
        User user = cache.read(userId);

        if (user == null) {
            // 缓存未命中 - 从数据库获取
            user = userRepository.findById(userId);

            // 写入缓存，30 分钟过期
            cache.write(userId, user, 30 * 60 * 1000);
        }

        return user;
    }

    public void updateUser(String userId, User user) {
        // 更新数据库
        userRepository.save(user);

        // 更新缓存
        cache.write(userId, user, 30 * 60 * 1000);
    }

    public void deleteUser(String userId) {
        // 从数据库删除
        userRepository.deleteById(userId);

        // 从缓存移除
        cache.remove(userId);
    }
}
```

#### 使用 CaffeineCache

```java
@Service
public class ProductService {

    private final CacheX<String, Product> cache;

    public ProductService() {
        // 高性能 Caffeine 缓存
        this.cache = new CaffeineCache<>(
            10000,                    // 最多 10,000 个条目
            60 * 60 * 1000           // 1 小时过期
        );
    }

    public Product getProduct(String productId) {
        return cache.read(productId);
    }

    public Map<String, Product> getProducts(List<String> productIds) {
        // 批量读取 - 性能优化
        return cache.read(productIds);
    }

    public void cacheProducts(Map<String, Product> products) {
        // 批量写入
        cache.write(products, 60 * 60 * 1000);
    }
}
```

### 2. 使用注解的声明式缓存

#### @Cached - 读通缓存

```java
@Service
public class OrderService {

    @Cached(name = "redis", prefix = "order:", expire = 30 * 60 * 1000)
    public Order getOrderById(String orderId) {
        // 此方法的结果被缓存
        // 缓存键：order:{orderId}
        return orderRepository.findById(orderId);
    }

    @Cached(
        name = "caffeine",
        prefix = "user:",
        expire = 60 * 60 * 1000,
        condition = "#userId != null"
    )
    public User getUserById(String userId) {
        // 仅在 userId 不为 null 时缓存
        // 缓存键：user:{userId}
        return userRepository.findById(userId);
    }
}
```

#### @CachedGet - 只读缓存

```java
@Service
public class ConfigurationService {

    @CachedGet(name = "memory", prefix = "config:")
    public String getConfig(String key) {
        // 从缓存读取，未命中时不自动写入
        return configRepository.findByKey(key);
    }
}
```

#### @Invalid - 缓存失效

```java
@Service
public class UserService {

    @Cached(name = "redis", prefix = "user:", expire = 60 * 60 * 1000)
    public User getUser(String userId) {
        return userRepository.findById(userId);
    }

    @Invalid(name = "redis", prefix = "user:")
    public void updateUser(String userId, User user) {
        // 更新时移除缓存的用户
        userRepository.save(user);
    }

    @Invalid(name = "redis", prefix = "user:")
    public void deleteUser(String userId) {
        // 删除时移除缓存的用户
        userRepository.deleteById(userId);
    }
}
```

### 3. 高级缓存键生成

#### 使用 @CacheKey 和 SpEL

```java
@Service
public class ProductService {

    @Cached(name = "redis", prefix = "product:")
    public Product getProduct(
        @CacheKey String productId,
        @CacheKey("#region.toUpperCase()") String region
    ) {
        // 缓存键：product:{productId}{region}
        // 示例：product:12345US
        return productRepository.findByIdAndRegion(productId, region);
    }

    @Cached(name = "caffeine", prefix = "user:")
    public User getUser(
        @CacheKey("#userId") String userId,
        @CacheKey("#type.name()") UserType type
    ) {
        // 缓存键：user:{userId}{type}
        // 示例：user:12345PREMIUM
        return userRepository.findByIdAndType(userId, type);
    }
}
```

#### 多键批量操作

```java
@Service
public class UserService {

    @Cached(name = "redis", prefix = "user:")
    public Map<String, User> getUsers(@CacheKey Collection<String> userIds) {
        // 为每个用户 ID 生成单独的缓存键
        // 示例：user:12345, user:67890
        return userRepository.findByIds(userIds);
    }

    @Cached(name = "caffeine", prefix = "product:")
    public List<Product> getProductsByIds(@CacheKey List<String> productIds) {
        // 批量缓存查找，自动键生成
        return repository.findAllById(productIds);
    }
}
```

### 4. 分布式 Redis 缓存

#### 单节点 Redis

```java
@Configuration
public class RedisCacheConfig {

    @Bean
    public JedisPool jedisPool() {
        return new JedisPool("localhost", 6379);
    }

    @Bean
    public CacheX<String, Object> redisCache(JedisPool jedisPool) {
        // 使用 Hessian2 序列化器获得更好性能
        return new RedisCache<>(jedisPool, new Hessian2Serializer());
    }

    @Bean
    public CacheX<String, Object> redisJsonCache(JedisPool jedisPool) {
        // 使用 JSON 序列化器获得更好兼容性
        return new RedisCache<>(jedisPool, new FastJsonSerializer());
    }
}
```

`RedisCache` 实现了 `AutoCloseable`。调用 `close()` 或使用 try-with-resources 可归还所有连接池资源。
在 Spring 容器中，`@PreDestroy` 注解会自动触发关闭。

#### Redis 集群

```java
@Configuration
public class RedisClusterConfig {

    @Bean
    public JedisCluster jedisCluster() {
        Set<HostAndPort> nodes = new HashSet<>();
        nodes.add(new HostAndPort("localhost", 7000));
        nodes.add(new HostAndPort("localhost", 7001));
        nodes.add(new HostAndPort("localhost", 7002));

        return new JedisCluster(nodes);
    }

    @Bean
    public CacheX<String, Object> redisClusterCache(JedisCluster jedisCluster) {
        return new RedisClusterCache<>(jedisCluster, new Hessian2Serializer());
    }
}
```

`RedisClusterCache` 同样实现了 `AutoCloseable`。

> **集群 scan 限制**：Redis Cluster 不支持通过单一游标进行跨槽 `SCAN`。
> `RedisClusterCache.scan(prefix)` 只能覆盖从游标 `"0"` 可达的分片。
> 如需完整覆盖所有分片，请通过哈希标签（如 `{ns}:key`）将所有键路由到同一槽。

### 5. CacheX 高级操作

#### 永不过期条目

将 `CacheExpire.FOREVER`（`0`）作为过期时间参数，可持久化保存条目：

```java
// 存储永不过期的配置
cache.write("global:config", config, CacheExpire.FOREVER);
```

各实现对此合约的处理方式：
- `MemoryCache` — TTL 检查中永不驱逐该条目
- `RedisCache` / `RedisClusterCache` — 使用不带 `PX` 的普通 `SET` 命令
- `MemcachedCache` — 退化为 Memcached 协议的最大 TTL（30 天）

#### TTL 续期（`renew`）

在不重写值的前提下延长已有条目的生存时间：

```java
// 每次用户操作时刷新 Session TTL
boolean refreshed = cache.renew(sessionKey, CacheExpire.HALF_HOUR);
if (!refreshed) {
    // 键已过期 — 重建 Session
}
```

`RedisCache` 和 `RedisClusterCache` 通过单条 `PEXPIRE` 命令实现 `renew`。
`MemoryCache` 及其他实现则采用先读后写的默认逻辑。

#### 原子计数器（`increment`）

原子地递增计数器并返回新值：

```java
// 限流：按客户端和分钟跟踪请求次数
long count = cache.increment("ratelimit:" + clientId + ":" + minute);
if (count > MAX_REQUESTS_PER_MINUTE) {
    throw new RateLimitException();
}
```

- Redis：基于原生 `INCR` 命令 — 高并发下原子性有保障。
- MemoryCache：基于 `AtomicLong` — 同样线程安全。
- 计数器首次调用时从 `1` 开始（键以 `0` 创建后递增）。
- 计数器键默认无 TTL；如需重置，请显式调用 `remove()`。

#### 前缀扫描（`scan`）

检索所有键以指定前缀开头的条目：

```java
// 获取某命名空间下所有缓存条目
Map<String, Object> items = cache.scan("product:category:electronics:");
```

> **注意**：对于 `RedisClusterCache`，不完全支持跨分片扫描。请参见上方集群 scan 限制说明。

### 6. 缓存指标和监控

#### 访问缓存统计

```java
@Service
public class CacheMonitorService {

    @Autowired
    private Collector cacheCollector;

    public void printCacheStats() {
        Map<String, Snapshot> stats = cacheCollector.getHitting();

        stats.forEach((pattern, snapshot) -> {
            System.out.println("缓存模式：" + pattern);
            System.out.println("  命中：" + snapshot.getHit());
            System.out.println("  请求：" + snapshot.getRequired());
            System.out.println("  命中率：" + snapshot.getRate());
        });
    }

    public void resetCacheStats(String pattern) {
        cacheCollector.reset(pattern);
    }
}
```

#### MemoryCache 统计

```java
@Service
public class LocalCacheService {

    private final MemoryCache<String, User> cache = new MemoryCache<>(1000, 180000);

    public void printStats() {
        // 获取内置统计
        String stats = cache.getStats();
        System.out.println(stats);

        // 输出示例：
        // MemoryCacheStats[requests=10000, hits=9500, hitRate=95.00%, size=800]

        // 获取估计大小
        long size = cache.estimatedSize();
        System.out.println("当前缓存大小：" + size);
    }
}
```

#### CaffeineCache 统计

```java
@Service
public class CaffeineCacheService {

    private final CaffeineCache<String, Product> cache =
        new CaffeineCache<>(10000, 3600000);

    public void printStats() {
        // 获取 Caffeine 原生统计
        String stats = cache.getStats();
        System.out.println(stats);

        // 访问原生缓存进行高级操作
        com.github.benmanes.caffeine.cache.Cache<String, Product> nativeCache =
            cache.getNativeCache();

        CacheStats caffeineStats = nativeCache.stats();
        System.out.println("命中率：" + caffeineStats.hitRate());
        System.out.println("驱逐计数：" + caffeineStats.evictionCount());
    }
}
```

### 7. 缓存穿透预防

```java
@Service
public class SecureUserService {

    @Cached(
        name = "redis",
        prefix = "user:",
        expire = 60 * 60 * 1000,
        enablePenetrationProtect = true
    )
    public User getUser(String userId) {
        User user = userRepository.findById(userId);

        if (user == null) {
            // 缓存穿透预防：
            // 自动缓存占位符对象
            // 防止对不存在用户的重复数据库查询
            return null;
        }

        return user;
    }
}
```

### 8. 条件缓存

```java
@Service
public class ConditionalCacheService {

    @Cached(
        name = "caffeine",
        prefix = "premium_user:",
        condition = "#user.type == T(com.example.UserType).PREMIUM"
    )
    public UserProfile getPremiumProfile(User user) {
        // 仅缓存高级用户
        return profileRepository.findByUserId(user.getId());
    }

    @Cached(
        name = "redis",
        prefix = "product:",
        condition = "#price > 0 && #price < 10000"
    )
    public Product getProductInRange(String productId, BigDecimal price) {
        // 仅缓存价格范围内的产品
        return productRepository.findById(productId);
    }

    @Cached(
        name = "memory",
        prefix = "config:",
        condition = "#env == 'production'"
    )
    public String getProductionConfig(String key, String env) {
        // 仅在生产环境缓存
        return configRepository.get(key);
    }
}
```

### 9. 自定义序列化器

```java
@Configuration
public class SerializerConfig {

    @Bean
    public CacheX<String, Object> customRedisCache(JedisPool jedisPool) {
        // 特定序列化需求的自定义序列化器
        BaseSerializer serializer = new BaseSerializer() {
            @Override
            public byte[] serialize(Object obj) {
                // 自定义序列化逻辑
                return CustomSerializer.encode(obj);
            }

            @Override
            public Object deserialize(byte[] bytes) {
                // 自定义反序列化逻辑
                return CustomSerializer.decode(bytes);
            }
        };

        return new RedisCache<>(jedisPool, serializer);
    }
}
```

-----

## 💡 最佳实践

### 1. 选择合适的缓存实现

```java
// ✅ 推荐：对短期频繁访问的数据使用 MemoryCache
CacheX<String, Session> sessionCache = new MemoryCache<>(1000, 180000);

// ✅ 推荐：对高性能本地缓存使用 CaffeineCache
CacheX<String, Product> productCache = new CaffeineCache<>(10000, 3600000);

// ✅ 推荐：对分布式缓存使用 RedisCache
CacheX<String, User> userCache = new RedisCache<>(jedisPool, new Hessian2Serializer());

// ❌ 不推荐：对本地会话数据使用分布式缓存
// 不必要的网络开销
```

### 2. 设置适当的过期时间

```java
// ✅ 推荐：频繁变化的数据使用短过期时间
@Cached(name = "redis", prefix = "stock:", expire = 60 * 1000)  // 1 分钟
public Stock getStock(String symbol) {
    return stockRepository.getRealtime(symbol);
}

// ✅ 推荐：很少变化的数据使用长过期时间
@Cached(name = "caffeine", prefix = "config:", expire = 24 * 60 * 60 * 1000)  // 24 小时
public Config getConfig(String key) {
    return configRepository.findByKey(key);
}

// ❌ 不推荐：动态数据无过期时间
@Cached(name = "redis", prefix = "price:", expire = CacheExpire.FOREVER)
public Price getPrice(String productId) {
    // FOREVER = 0：条目持久保存直到被显式移除 — 将永久提供陈旧数据
}
```

### 3. 使用批量操作

```java
// ✅ 推荐：批量读取提高性能
public Map<String, User> getUsers(List<String> userIds) {
    return cache.read(userIds);  // 单次操作
}

// ❌ 不推荐：循环读取
public Map<String, User> getUsers(List<String> userIds) {
    Map<String, User> result = new HashMap<>();
    for (String userId : userIds) {
        result.put(userId, cache.read(userId));  // 多次操作
    }
    return result;
}
```

### 4. 正确实现 Cache Aside 模式

```java
// ✅ 推荐：正确的 cache-aside 实现
public Product getProduct(String productId) {
    Product product = cache.read(productId);

    if (product == null) {
        product = productRepository.findById(productId);

        if (product != null) {
            cache.write(productId, product, 3600000);
        }
    }

    return product;
}

// ❌ 不推荐：无 null 检查导致重复 DB 查询
public Product getProduct(String productId) {
    Product product = cache.read(productId);

    if (product == null) {
        // 缓存 null 结果 - 对不存在的产品重复
        product = productRepository.findById(productId);
        cache.write(productId, product, 3600000);
    }

    return product;
}
```

### 5. 监控缓存性能

```java
// ✅ 推荐：定期监控和告警
@Scheduled(fixedRate = 60000)  // 每分钟
public void monitorCache() {
    Map<String, Snapshot> stats = cacheCollector.getHitting();

    stats.forEach((pattern, snapshot) -> {
        double hitRate = Double.parseDouble(snapshot.getRate().replace("%", ""));

        if (hitRate < 80.0) {
            logger.warn("{} 的缓存命中率低：{}", pattern, snapshot.getRate());
            // 发送告警或调整缓存配置
        }
    });
}
```

### 6. 使用适当的缓存键设计

```java
// ✅ 推荐：分层缓存键
@Cached(prefix = "user:profile:")
public UserProfile getUserProfile(String userId) {
    // 缓存键：user:profile:{userId}
}

@Cached(prefix = "user:settings:")
public UserSettings getUserSettings(String userId) {
    // 缓存键：user:settings:{userId}
}

// ❌ 不推荐：平面命名空间
@Cached(prefix = "cache_")
public Object getData(String type, String id) {
    // 容易冲突，难以失效
}
```

### 7. 始终关闭缓存资源

所有持有外部连接的缓存实现（`RedisCache`、`RedisClusterCache`、`MemcachedCache`）均实现了
`AutoCloseable`。不再使用时请释放资源：

```java
// ✅ Spring bean：@PreDestroy 会自动触发 — 无需手动调用 close()
@Bean
public CacheX<String, Object> redisCache(JedisPool jedisPool) {
    return new RedisCache<>(jedisPool);  // 上下文关闭时自动释放
}

// ✅ 非 Spring 场景：使用 try-with-resources
try (RedisCache<String, Object> cache = new RedisCache<>(jedisPool)) {
    cache.write("key", value, CacheExpire.ONE_HOUR);
}

// ✅ 仅需续期时，优先使用 renew() 而非先读后写
boolean extended = cache.renew(sessionKey, CacheExpire.HALF_HOUR);
```

-----

## ❓ 常见问题

### Q1: 如何在不同缓存实现之间切换？

**A**：只需更改配置或 bean 定义：

```java
// 之前：MemoryCache
@Bean
public CacheX<String, Object> cache() {
    return new MemoryCache<>(1000, 180000);
}

// 之后：RedisCache（服务层无需更改代码）
@Bean
public CacheX<String, Object> cache(JedisPool jedisPool) {
    return new RedisCache<>(jedisPool);
}
```

### Q2: 如何处理缓存序列化错误？

**A**：使用适当的序列化器：

```java
// 对于 Java Serializable 对象
CacheX<String, Object> cache = new RedisCache<>(jedisPool, new JdkSerializer());

// 为了更好性能（Hessian）
CacheX<String, Object> cache = new RedisCache<>(jedisPool, new Hessian2Serializer());

// 为了 JSON 兼容性
CacheX<String, Object> cache = new RedisCache<>(jedisPool, new FastJsonSerializer());

// 为了压缩（减少内存使用）
CacheX<String, Object> cache = new RedisCache<>(jedisPool, new JdkGzipSerializer());
```

### Q3: 如何防止缓存击穿（惊群效应）？

**A**：使用缓存锁机制或提前刷新：

```java
@Service
public class SafeUserService {

    private final Lock lock = new ReentrantLock();

    public User getUser(String userId) {
        User user = cache.read(userId);

        if (user == null) {
            lock.lock();
            try {
                // 双重检查锁定
                user = cache.read(userId);
                if (user == null) {
                    user = userRepository.findById(userId);
                    cache.write(userId, user, 3600000);
                }
            } finally {
                lock.unlock();
            }
        }

        return user;
    }
}
```

### Q4: 如何实现多级缓存？

**A**：使用 L1（本地）+ L2（分布式）缓存模式：

```java
@Service
public class MultiLevelCacheService {

    private final CacheX<String, User> l1Cache = new CaffeineCache<>(1000, 60000);
    private final CacheX<String, User> l2Cache = new RedisCache<>(jedisPool);

    public User getUser(String userId) {
        // 首先尝试 L1 缓存
        User user = l1Cache.read(userId);

        if (user == null) {
            // 尝试 L2 缓存
            user = l2Cache.read(userId);

            if (user == null) {
                // 从数据库获取
                user = userRepository.findById(userId);

                // 填充两个缓存
                l2Cache.write(userId, user, 3600000);
                l1Cache.write(userId, user, 60000);
            } else {
                // 从 L2 填充 L1
                l1Cache.write(userId, user, 60000);
            }
        }

        return user;
    }
}
```

### Q5: 如何使相关缓存条目失效？

**A**：使用带有适当键模式的 @Invalid 注解：

```java
@Service
public class CacheInvalidationService {

    @Cached(prefix = "user:")
    public User getUser(String userId) {
        return userRepository.findById(userId);
    }

    @Cached(prefix = "user:orders:")
    public List<Order> getUserOrders(String userId) {
        return orderRepository.findByUserId(userId);
    }

    @Invalid(prefix = "user:")
    @Invalid(prefix = "user:orders:")
    public void updateUser(String userId, User user) {
        // 使用户和用户订单缓存都失效
        userRepository.save(user);
    }
}
```

### Q6: 如何调试缓存键生成？

**A**：启用调试日志并检查生成的键：

```yaml
# application.yml
logging:
  level:
    org.miaixz.bus.cache: DEBUG
```

```java
// 编程式键生成
@Component
public class CacheKeyDebugger {

    public void debugKeyGeneration(Method method, Object[] args) {
        AnnoHolder holder = CacheInfoContainer.getCacheInfo(method).getLeft();

        if (holder.isMulti()) {
            Map[] maps = Builder.generateMultiKey(holder, args);
            System.out.println("生成的键：" + maps[1].keySet());
        } else {
            String key = Builder.generateSingleKey(holder, args);
            System.out.println("生成的键：" + key);
        }
    }
}
```

### Q7: 如何处理缓存预热？

**A**：在启动时实现缓存预加载：

```java
@Component
public class CacheWarmupService implements ApplicationRunner {

    @Autowired
    private CacheX<String, Product> productCache;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void run(ApplicationArguments args) {
        // 将热数据预加载到缓存
        List<Product> hotProducts = productRepository.findHotProducts();

        Map<String, Product> productMap = hotProducts.stream()
            .collect(Collectors.toMap(
                Product::getId,
                Function.identity()
            ));

        productCache.write(productMap, 3600000);

        logger.info("缓存预热完成：加载了 {} 个产品", hotProducts.size());
    }
}
```

-----

## 🔄 版本兼容性

| Bus Cache 版本 | JDK 版本 | Spring Boot 版本 |
| :--- | :--- | :--- |
| 8.x | 17+ | 3.x+ |
| 7.x | 11+ | 2.x+ |

-----

## 📊 性能对比

基于基准测试（10,000 次操作）：

| 缓存实现 | 平均延迟 | 吞吐量 | 命中率 |
| :--- | :--- | :--- | :--- |
| **MemoryCache** | $0.5\text{ms}$ | $20,000\text{ ops/s}$ | $95\%$ |
| **CaffeineCache** | $0.3\text{ms}$ | $33,000\text{ ops/s}$ | $98\%$ |
| **GuavaCache** | $0.6\text{ms}$ | $16,000\text{ ops/s}$ | $92\%$ |
| **RedisCache** | $2.5\text{ms}$ | $4,000\text{ ops/s}$ | N/A |
| **MemcachedCache** | $2.0\text{ms}$ | $5,000\text{ ops/s}$ | N/A |

-----

## 🛠️ 配置参考

### MemoryCache 属性

| 属性 | 类型 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- |
| `maximumSize` | `long` | `1000` | 最大条目数 |
| `expireAfterWrite` | `long` | `180000` | TTL（毫秒） |
| `expireAfterAccess` | `long` | `0` | TTI（毫秒，0 = 禁用） |
| `initialCapacity` | `int` | `16` | 初始映射容量 |

### CaffeineCache 属性

| 属性 | 类型 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- |
| `maximumSize` | `long` | `1000` | 最大条目数 |
| `expireAfterWrite` | `long` | - | TTL（毫秒） |
| `expireAfterAccess` | `long` | - | TTI（毫秒） |
| `initialCapacity` | `int` | - | 初始映射容量 |

### RedisCache 属性

| 属性 | 类型 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- |
| `host` | `String` | `localhost` | Redis 服务器主机 |
| `port` | `int` | `6379` | Redis 服务器端口 |
| `timeout` | `int` | `2000` | 连接超时（毫秒） |
| `serializer` | `BaseSerializer` | `Hessian2Serializer` | 值序列化器 |

-----

## 🔗 相关文档

- [Bus Core 文档](https://github.com/818000/bus/tree/main/bus-core)
- [Bus Starter 指南](https://github.com/818000/bus/tree/main/bus-starter)
- [官方文档](https://www.miaixz.org)

-----

## 📄 许可证

[Apache License Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)

-----

是否要在 YouTube 上搜索 Bus Cache 入门教程视频？
