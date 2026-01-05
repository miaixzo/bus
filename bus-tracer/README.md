# 🔍 Bus Tracer: Distributed Tracing Framework

<p align="center">
<strong>High-Performance Distributed Tracing for Microservices</strong>
</p>

-----

## 📖 Project Introduction

**Bus Tracer** is a distributed tracing framework that provides real-time monitoring and performance analysis for microservices architectures. It helps developers track requests as they propagate through distributed systems, making it easier to diagnose performance bottlenecks and errors.

-----

## ✨ Core Features

### 🎯 Distributed Tracing

- **Request Tracking**: Trace requests across service boundaries
- **Span Management**: Automatic span creation and propagation
- **Context Propagation**: Seamless context passing between services
- **Transaction Correlation**: Correlate related operations across services
- **Performance Metrics**: Capture timing data for each operation

### 🌊 Visualization

- **Call Graphs**: Visual representation of service calls
- **Timeline Views**: Time-based view of request execution
- **Service Maps**: Topology view of service dependencies
- **Performance Heatmaps**: Identify bottlenecks visually
- **Error Tracking**: Track and visualize error propagation

### 🔍 Analysis

- **Latency Analysis**: Identify slow operations and services
- **Dependency Analysis**: Understand service dependencies
- **Error Analysis**: Track error patterns and root causes
- **Throughput Analysis**: Monitor request rates and capacities
- **Resource Usage**: Track resource consumption per request

-----

## 🚀 Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-tracer</artifactId>
    <version>x.x.x</version>
</dependency>
```

### Enable Tracing

```java
@EnableTracing
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### Create Custom Spans

```java
import org.miaixz.bus.tracer.Tracer;
import org.miaixz.bus.tracer.Span;

@Service
public class UserService {

    @Autowired
    private Tracer tracer;

    public User getUser(String userId) {
        // Create a new span
        Span span = tracer.createSpan("getUser");
        try {
            // Your business logic
            User user = userRepository.findById(userId);
            return user;
        } finally {
            // Close the span
            span.close();
        }
    }
}
```

### Annotation-Based Tracing

```java
import org.miaixz.bus.tracer.annotation.Trace;

@Service
public class OrderService {

    @Trace(operationName = "createOrder")
    public Order createOrder(OrderRequest request) {
        // Method is automatically traced
        return orderRepository.save(request);
    }

    @Trace
    public void processPayment(Order order) {
        // Method is automatically traced with default operation name
        paymentService.charge(order);
    }
}
```

-----

## 💡 Advanced Usage

### Custom Tags

```java
@Service
public class ProductService {

    @Trace(operationName = "getProduct")
    public Product getProduct(String productId) {
        Span span = tracer.getCurrentSpan();
        if (span != null) {
            // Add custom tags
            span.tag("product.id", productId);
            span.tag("product.category", "electronics");
        }

        return productRepository.findById(productId);
    }
}
```

### Baggage Propagation

```java
@Service
public class CartService {

    @Trace(operationName = "addToCart")
    public void addToCart(String userId, String productId) {
        Span span = tracer.getCurrentSpan();
        if (span != null) {
            // Add baggage (propagates to downstream services)
            span.setBaggageItem("user.id", userId);
            span.setBaggageItem("session.id", getSessionId());
        }

        cartService.addItem(productId);
    }
}
```

### Conditional Tracing

```java
@Service
public class ApiService {

    @Trace(operationName = "externalApiCall", sampler = ProbabilisticSampler.class)
    public Response callExternalAPI(Request request) {
        // Only traced if sampler allows
        return restTemplate.postForObject(apiUrl, request, Response.class);
    }
}
```

### Error Handling

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

## 🔧 Configuration

### Basic Configuration

```yaml
extend:
  tracer:
    enabled: true
    application-name: my-application
    sampler-type: probabilistic
    sampler-rate: 0.1  # 10% of requests
```

### Advanced Configuration

```yaml
extend:
  tracer:
    enabled: true
    application-name: my-application

    # Sampling configuration
    sampler-type: rate-limiting
    sampler-rate: 100  # max 100 traces per second

    # Exporter configuration
    exporter-type: zipkin
    zipkin-url: http://localhost:9411

    # Span configuration
    max-span-count: 1000
    span-timeout: 30000  # 30 seconds

    # Baggage configuration
    baggage-limit: 10
    baggage-max-length: 256
```

### Integration with Zipkin

```yaml
extend:
  tracer:
    exporter-type: zipkin
    zipkin-url: http://zipkin:9411
    zipkin-sender: http
```

### Integration with Jaeger

```yaml
extend:
  tracer:
    exporter-type: jaeger
    jaeger-url: http://jaeger:14268
    jaeger-sender: http
```

-----

## 📊 Monitoring & Analysis

### Metrics Integration

```java
@Component
public class TracingMetrics {

    @EventListener
    public void handleSpanFinished(SpanFinishedEvent event) {
        Span span = event.getSpan();

        // Record metrics
        meterRegistry.timer("tracer.span.duration",
            "operation", span.getOperationName(),
            "status", span.getStatus().name()
        ).record(span.getDuration(), TimeUnit.MICROSECONDS);

        // Count errors
        if (span.getStatus() == Status.ERROR) {
            meterRegistry.counter("tracer.span.errors",
                "operation", span.getOperationName()
            ).increment();
        }
    }
}
```

### Custom Exporter

```java
@Component
public class CustomExporter implements SpanExporter {

    @Override
    public void export(List<Span> spans) {
        // Export to your backend
        for (Span span : spans) {
            // Process span data
            tracingRepository.save(span);
        }
    }
}
```

-----

## 💡 Best Practices

### 1. Meaningful Operation Names

```java
// Good
@Trace(operationName = "user.login")
public User login(String username, String password) {
    // ...
}

// Avoid
@Trace  // Uses method name
public User login(String username, String password) {
    // ...
}
```

### 2. Add Relevant Tags

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

### 3. Handle Errors Properly

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

### 4. Use Appropriate Sampling

For high-traffic services, use sampling to reduce overhead:

```yaml
extend:
  tracer:
    sampler-type: probabilistic
    sampler-rate: 0.01  # Trace 1% of requests
```

-----

## 🔄 Version Compatibility

| Bus Tracer Version | Spring Boot Version | JDK Version |
|:---|:---|:---|
| 8.x | 3.x+ | 17+ |
| 7.x | 2.x+ | 11+ |

-----

## ❓ FAQ

### Q: What's the performance overhead?

A: Typically less than 5% overhead when using sampling. Can be adjusted based on sampling rate.

### Q: Can I trace async operations?

A: Yes, the tracer properly handles async context propagation.

### Q: How do I filter sensitive data?

A: Use tag sanitizers or configure specific tags to be redacted.

### Q: Can I use multiple exporters?

A: Yes, you can configure multiple exporters to send traces to different backends.

-----

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
