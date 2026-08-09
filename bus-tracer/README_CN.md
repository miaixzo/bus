# 🔍 Bus Tracer: 分布式追踪框架

<p align="center">
<strong>微服务的高性能分布式追踪</strong>
</p>

-----

## 📖 项目介绍

**Bus Tracer** 是一个分布式追踪框架,为微服务架构提供实时监控和性能分析。它帮助开发者跟踪请求在分布式系统中的传播,更容易诊断性能瓶颈和错误。

-----

## ✨ 核心特性

### 🎯 分布式追踪

- **请求跟踪**: 跨服务边界跟踪请求
- **Span 管理**: 自动创建和传播 span
- **上下文传播**: 服务之间无缝上下文传递
- **事务关联**: 关联跨服务的相关操作
- **性能指标**: 捕获每个操作的时序数据

### 🌊 可视化

- **调用图**: 服务调用的可视化表示
- **时间线视图**: 基于时间的请求执行视图
- **服务地图**: 服务依赖的拓扑视图
- **性能热图**: 可视化识别瓶颈
- **错误跟踪**: 跟踪和可视化错误传播

### 🔍 分析

- **延迟分析**: 识别慢操作和服务
- **依赖分析**: 理解服务依赖关系
- **错误分析**: 跟踪错误模式和根本原因
- **吞吐量分析**: 监控请求率和容量
- **资源使用**: 跟踪每个请求的资源消耗

-----

## 🚀 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-tracer</artifactId>
    <version>x.x.x</version>
</dependency>
```

### 启用追踪

```java
@EnableTracing
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 创建自定义 Span

```java
import org.miaixz.bus.tracer.Tracer;
import org.miaixz.bus.tracer.Span;

@Service
public class UserService {

    @Autowired
    private Tracer tracer;

    public User getUser(String userId) {
        // 创建新 span
        Span span = tracer.createSpan("getUser");
        try {
            // 您的业务逻辑
            User user = userRepository.findById(userId);
            return user;
        } finally {
            // 关闭 span
            span.close();
        }
    }
}
```

### 基于注解的追踪

```java
import org.miaixz.bus.tracer.annotation.Trace;

@Service
public class OrderService {

    @Trace(operationName = "createOrder")
    public Order createOrder(OrderRequest request) {
        // 方法自动被追踪
        return orderRepository.save(request);
    }

    @Trace
    public void processPayment(Order order) {
        // 方法使用默认操作名称自动被追踪
        paymentService.charge(order);
    }
}
```

-----

## 💡 高级用法

### 自定义标签

```java
@Service
public class ProductService {

    @Trace(operationName = "getProduct")
    public Product getProduct(String productId) {
        Span span = tracer.getCurrentSpan();
        if (span != null) {
            // 添加自定义标签
            span.tag("product.id", productId);
            span.tag("product.category", "电子产品");
        }

        return productRepository.findById(productId);
    }
}
```

### 上下文传播

```java
@Service
public class CartService {

    @Trace(operationName = "addToCart")
    public void addToCart(String userId, String productId) {
        Span span = tracer.getCurrentSpan();
        if (span != null) {
            // 添加 baggage(传播到下游服务)
            span.setBaggageItem("user.id", userId);
            span.setBaggageItem("session.id", getSessionId());
        }

        cartService.addItem(productId);
    }
}
```

### 条件追踪

```java
@Service
public class ApiService {

    @Trace(operationName = "externalApiCall", sampler = ProbabilisticSampler.class)
    public Response callExternalAPI(Request request) {
        // 仅在采样器允许时被追踪
        return restTemplate.postForObject(apiUrl, request, Response.class);
    }
}
```

### 错误处理

```java
@Service
public class PaymentService {

    @Trace(operationName = "processPayment")
    public PaymentResult processPayment(Payment payment) {
        Span span = tracer.getCurrentSpan();
        try {
            return paymentGateway.charge(payment);
        } catch (PaymentException e) {
            if (span != null) {
                span.tag("error", "true");
                span.tag("error.message", e.getMessage());
                span.log("payment_failed");
            }
            throw e;
        }
    }
}
```

-----

## 🔧 配置

### 基本配置

```yaml
extend:
  tracer:
    enabled: true
    application-name: my-application
    sampler-type: probabilistic
    sampler-rate: 0.1  # 10% 的请求
```

### 高级配置

```yaml
extend:
  tracer:
    enabled: true
    application-name: my-application

    # 采样配置
    sampler-type: rate-limiting
    sampler-rate: 100  # 每秒最多 100 个追踪

    # 导出器配置
    exporter-type: zipkin
    zipkin-url: http://localhost:9411

    # Span 配置
    max-span-count: 1000
    span-timeout: 30000  # 30 秒

    # Baggage 配置
    baggage-limit: 10
    baggage-max-length: 256
```

### 与 Zipkin 集成

```yaml
extend:
  tracer:
    exporter-type: zipkin
    zipkin-url: http://zipkin:9411
    zipkin-sender: http
```

### 与 Jaeger 集成

```yaml
extend:
  tracer:
    exporter-type: jaeger
    jaeger-url: http://jaeger:14268
    jaeger-sender: http
```

-----

## 📊 监控与分析

### 指标集成

```java
@Component
public class TracingMetrics {

    @EventListener
    public void handleSpanFinished(SpanFinishedEvent event) {
        Span span = event.getSpan();

        // 记录指标
        meterRegistry.timer("tracer.span.duration",
            "operation", span.getOperationName(),
            "status", span.getStatus().name()
        ).record(span.getDuration(), TimeUnit.MICROSECONDS);

        // 计数错误
        if (span.getStatus() == Status.ERROR) {
            meterRegistry.counter("tracer.span.errors",
                "operation", span.getOperationName()
            ).increment();
        }
    }
}
```

### 自定义导出器

```java
@Component
public class CustomExporter implements SpanExporter {

    @Override
    public void export(List<Span> spans) {
        // 导出到您的后端
        for (Span span : spans) {
            // 处理 span 数据
            tracingRepository.save(span);
        }
    }
}
```

-----

## 💡 最佳实践

### 1. 有意义的操作名称

```java
// 好
@Trace(operationName = "user.login")
public User login(String username, String password) {
    // ...
}

// 避免
@Trace  // 使用方法名
public User login(String username, String password) {
    // ...
}
```

### 2. 添加相关标签

```java
@Trace(operationName = "database.query")
public List<User> getUsersByStatus(UserStatus status) {
    Span span = tracer.getCurrentSpan();
    span.tag("db.type", "postgresql");
    span.tag("db.operation", "select");
    span.tag("db.table", "users");
    span.tag("query.status", status.name());

    return userRepository.findByStatus(status);
}
```

### 3. 正确处理错误

```java
@Trace(operationName = "payment.process")
public PaymentResult processPayment(Payment payment) {
    Span span = tracer.getCurrentSpan();
    try {
        return paymentGateway.charge(payment);
    } catch (Exception e) {
        span.tag("error", "true");
        span.tag("error.type", e.getClass().getSimpleName());
        span.tag("error.message", e.getMessage());
        span.log(Collections.singletonMap("event", "error"));
        throw e;
    }
}
```

### 4. 使用适当的采样

对于高流量服务,使用采样来减少开销:

```yaml
extend:
  tracer:
    sampler-type: probabilistic
    sampler-rate: 0.01  # 追踪 1% 的请求
```

-----

## 🔄 版本兼容性

| Bus Tracer 版本 | Spring Boot 版本 | JDK 版本 |
|:----------------|:-----------------|:---------|
| 8.x             | 3.x+             | 17+      |
| 7.x             | 2.x+             | 11+      |

-----

## ❓ FAQ

### Q: 性能开销是多少?

A: 使用采样时通常小于 5% 的开销。可以根据采样率进行调整。

### Q: 可以追踪异步操作吗?

A: 可以,追踪器正确处理异步上下文传播。

### Q: 如何过滤敏感数据?

A: 使用标签清理器或配置特定标签被编辑。

### Q: 可以使用多个导出器吗?

A: 可以,您可以配置多个导出器将追踪发送到不同的后端。

-----

## 🤝 贡献

欢迎贡献!请随时提交拉取请求。

-----

**由 Miaixz 团队用 ❤️ 构建**
