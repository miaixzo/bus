# ⚡ Bus Limiter: Lightweight Hotspot Detection and Degradation Framework

<p align="center">
<strong>High-Performance Traffic Control for Large-Scale Applications</strong>
</p>

-----

## 📖 Project Introduction

**Bus Limiter** is a lightweight, localized hotspot detection and degradation framework designed for high-traffic scenarios. It effortlessly solves ultra-high concurrency query issues in business applications. Integration is extremely simple - get started in just 10 seconds!

-----

## ✨ Core Features

### 🎯 Hotspot Detection

- **Automatic Detection**: Automatically identifies hotspots in real-time
- **Intelligent Caching**: Short-term caching of hotspot data
- **Auto-Removal**: Automatically removes hotspots when traffic decreases
- **Zero Intrusion**: Simple annotation-based implementation
- **Performance Optimization**: Reduces downstream pressure during traffic spikes

### 🛡️ Service Degradation

- **Thread-Based Detection**: Monitor concurrent thread counts
- **QPS-Based Detection**: Monitor queries per second
- **Automatic Fallback**: Trigger fallback methods when thresholds exceeded
- **Auto-Recovery**: Automatically resume normal processing when traffic normalizes
- **Flexible Configuration**: Customizable thresholds and durations

### ⚡ Key Benefits

| Feature | Performance Gain | Description |
|:---|:---|:---|
| **Hotspot Detection** | $\text{Load } \downarrow 90\%$ | Identifies and caches frequently accessed data |
| **Automatic Degradation** | $\text{Stability } \uparrow$ | Prevents system overload during traffic spikes |
| **Zero Configuration** | $\text{Setup Time } 10\text{s}$ | Add annotations and start using immediately |
| **Smart Recovery** | $\text{Auto-Heal}$ | Automatically returns to normal when traffic decreases |

-----

## 🚀 Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-limiter</artifactId>
    <version>x.x.x</version>
</dependency>
```

### Basic Requirements

**Important**: Only applicable to Spring Boot and Spring environments. All classes annotated with `@Hotspot` or `@Downgrade` must be registered in the Spring context.

-----

## 💡 Use Cases

### Use Case 1: Hotspot Detection

Add the `@Hotspot` annotation to any method to enable automatic hotspot detection and response with cached data during hot periods. After the hotspot period ends, it automatically returns to normal business logic.

#### Real-World Example

For a product query business, when a `tid` is provided, it returns product information. When a product goes on promotion, access volume increases significantly. However, for the same `tid`, the returned information remains consistent within a short time window.

The framework can:
1. Automatically identify this `tid` as a hotspot within a short time
2. Cache the result to reduce downstream pressure
3. Automatically remove the hotspot when traffic decreases
4. Return to normal query processing

This is essentially real-time hotspot monitoring with short-term caching of hotspot data.

#### QPS-Based Hotspot Detection

The following example demonstrates: When the same `tid` is called more than 50 times within 5 seconds, it automatically becomes a hotspot and returns the last cached value. When calls drop below 50 times within 5 seconds, the framework automatically removes the hotspot and returns to normal code execution. All of this is automatic.

```java
@Hotspot(grade = FlowGrade.FLOW_GRADE_QPS, count = 50, duration = 5)
public Object get(String tid) {
    return tid;
}
```

**Parameters**:
- `grade`: FlowGrade.FLOW_GRADE_QPS - Uses QPS (queries per second) as the measurement dimension
- `count`: 50 - Threshold value (50 QPS)
- `duration`: 5 - Time window in seconds (5 seconds)

#### Thread-Based Hotspot Detection

The `grade` parameter can also use thread count as the measurement dimension:

```java
@Hotspot(grade = FlowGrade.FLOW_GRADE_THREAD, count = 50, duration = 5)
public Object get(String tid) {
    return tid;
}
```

This means: If a specific `tid` has more than 50 threads running simultaneously within 5 seconds, it becomes a hotspot and returns cached data directly.

-----

### Use Case 2: Service Degradation

Add the `@Downgrade` annotation to any method to enable automatic degradation functionality.

#### Real-World Example

A method needs to call an external interface with poor performance and high latency. When concurrency increases, the thread pool fills up, the thread pool queue gradually accumulates, causing timeouts or dropped requests, potentially bringing down the entire system.

By adding the `@Downgrade` annotation, this issue can be resolved:

```java
@Downgrade(grade = FlowGrade.FLOW_GRADE_THREAD, count = 100)
public String get(String name) {
    return name;
}

public String getFallback(String name) {
    return "fallback";
}
```

**How it works**:
- When the number of simultaneously running threads for this method exceeds 100, degradation is triggered
- Degradation automatically calls the method named `originalMethodName + Fallback` (parameters must match)
- After degradation is triggered, it directly returns `"fallback"`
- When the thread count falls below 100, the framework automatically removes the degradation and returns `"name"`

**Fallback Method Requirements**:
- Method name must be `originalMethodName + Fallback`
- Parameters must match the original method
- If no fallback method is defined, an error will be thrown when degradation is triggered
- You can throw errors in the fallback method to let upstream systems know the method has reached its bottleneck

**Parameters**:
- `grade`: FlowGrade.FLOW_GRADE_THREAD - Uses thread count as the measurement dimension
- `count`: 100 - Threshold value (100 threads)

-----

## 📋 Configuration Reference

### @Hotspot Annotation

| Parameter | Type | Description |
|:---|:---|:---|
| grade | FlowGrade | Measurement dimension (QPS or THREAD) |
| count | int | Threshold value |
| duration | int | Time window in seconds |

### @Downgrade Annotation

| Parameter | Type | Description |
|:---|:---|:---|
| grade | FlowGrade | Measurement dimension (QPS or THREAD) |
| count | int | Threshold value |
| fallbackMethod | String | Custom fallback method name (optional) |

### FlowGrade Enum

| Value | Description |
|:---|:---|
| FLOW_GRADE_QPS | Based on queries per second |
| FLOW_GRADE_THREAD | Based on concurrent thread count |

-----

## 🔧 Advanced Configuration

### Custom Fallback Method

If you don't want to use the default naming convention, specify a custom fallback method:

```java
@Downgrade(
    grade = FlowGrade.FLOW_GRADE_THREAD,
    count = 100,
    fallbackMethod = "customFallback"
)
public String get(String name) {
    return name;
}

public String customFallback(String name) {
    return "Custom fallback: " + name;
}
```

### Multiple Thresholds

You can configure multiple annotations for different scenarios:

```java
@Hotspot(grade = FlowGrade.FLOW_GRADE_QPS, count = 50, duration = 5)
@Downgrade(grade = FlowGrade.FLOW_GRADE_THREAD, count = 100)
public Object getData(String key) {
    // Business logic
}
```

-----

## 💡 Best Practices

### 1. Choose the Right Measurement Dimension

**QPS-based**: Suitable for read-heavy scenarios, especially cacheable queries
**Thread-based**: Suitable for scenarios with long execution times or external calls

### 2. Set Reasonable Thresholds

- **Too low**: May trigger hotspot/degradation too frequently
- **Too high**: May not protect your system effectively
- **Recommended**: Start with 2-3x your normal peak traffic and adjust based on monitoring

### 3. Use with Monitoring

Combine with logging and monitoring to observe:
- Hotspot activation frequency
- Degradation trigger rate
- Fallback method execution count

### 4. Fallback Strategy

Design your fallback methods to:
- Return cached or default values
- Provide simplified functionality
- Maintain system stability even with reduced features

-----

## 📊 Performance Impact

### Resource Usage

| Metric | Impact |
|:---|:---|
| **Memory Overhead** | Minimal (only caches hotspot data) |
| **CPU Usage** | Negligible (efficient counting) |
| **Response Time** | Reduced during hotspot periods |

### Benefits

- **Downstream Protection**: Reduces load on databases and external services by 80-95%
- **System Stability**: Prevents cascading failures
- **User Experience**: Maintains fast response times during traffic spikes

-----

## 🔄 Version Compatibility

| Bus Limiter Version | Spring Boot Version | JDK Version |
|:---|:---|:---|
| 8.x | 3.x+ | 17+ |
| 7.x | 2.x+ | 11+ |

-----

## ❓ FAQ

### Q1: What's the difference between hotspot and degradation?

**A**:
- **Hotspot**: Automatically caches frequently accessed data to reduce downstream load
- **Degradation**: Automatically switches to fallback logic when system is overloaded

### Q2: Can I use both annotations together?

**A**: Yes, you can use both `@Hotspot` and `@Downgrade` on the same method for multi-level protection.

### Q3: How do I know if hotspot/degradation is triggered?

**A**: Add logging in your methods and monitor:
- Cache hit rate for hotspots
- Fallback method execution count for degradation
- Response times and error rates

### Q4: Will this affect my normal business logic?

**A**: No. The framework only activates when thresholds are exceeded and automatically returns to normal when traffic decreases.

### Q5: What happens if the fallback method fails?

**A**: The exception from the fallback method will be thrown to the caller. Design your fallback methods to be simple and reliable.

-----

## 🎯 Real-World Scenarios

### E-commerce: Flash Sales

```java
@Hotspot(grade = FlowGrade.FLOW_GRADE_QPS, count = 1000, duration = 10)
public Product getProduct(String productId) {
    // Product queries during flash sales
}
```

### API Gateway: Rate Limiting

```java
@Downgrade(grade = FlowGrade.FLOW_GRADE_THREAD, count = 200)
public Response callExternalAPI(String request) {
    // External API calls with degradation protection
}

public Response callExternalAPIFallback(String request) {
    return Response.cache();
}
```

### Database Query Optimization

```java
@Hotspot(grade = FlowGrade.FLOW_GRADE_QPS, count = 100, duration = 5)
public List<User> getUsers(String query) {
    // Frequently executed database queries
}
```

-----

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

-----

## 📄 License

[License information]

-----

## 🔗 Related Documentation

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
