# ⚡ Bus Limiter: 轻量级热点检测和降级框架

<p align="center">
<strong>大规模应用的高性能流量控制</strong>
</p>

-----

## 📖 项目介绍

**Bus Limiter** 是一个为高流量场景设计的轻量级、本地化热点检测和降级框架。它轻松解决业务应用中的超高并发查询问题。集成极其简单 - 只需 10 秒即可开始使用!

-----

## ✨ 核心特性

### 🎯 热点检测

- **自动检测**: 实时自动识别热点
- **智能缓存**: 短期缓存热点数据
- **自动移除**: 流量减少时自动移除热点
- **零侵入**: 简单的基于注解的实现
- **性能优化**: 流量高峰期间减少下游压力

### 🛡️ 服务降级

- **基于线程的检测**: 监控并发线程数
- **基于 QPS 的检测**: 监控每秒查询数
- **自动回退**: 超过阈值时触发回退方法
- **自动恢复**: 流量正常时自动恢复正常处理
- **灵活配置**: 可自定义阈值和持续时间

### ⚡ 关键优势

| 特性 | 性能提升 | 描述 |
|:---|:---|:---|
| **热点检测** | $\text{负载 } \downarrow 90\%$ | 识别并缓存频繁访问的数据 |
| **自动降级** | $\text{稳定性 } \uparrow$ | 流量高峰期间防止系统过载 |
| **零配置** | $\text{设置时间 } 10\text{s}$ | 添加注解立即可用 |
| **智能恢复** | $\text{自动愈合}$ | 流量减少时自动恢复正常 |

-----

## 🚀 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-limiter</artifactId>
    <version>x.x.x</version>
</dependency>
```

### 基本要求

**重要**: 仅适用于 Spring Boot 和 Spring 环境。所有使用 `@Hotspot` 或 `@Downgrade` 注解的类必须在 Spring 上下文中注册。

-----

## 💡 使用场景

### 使用场景 1: 热点检测

在任何方法上添加 `@Hotspot` 注解以启用自动热点检测并在热点期间返回缓存数据。热点期结束后,它自动返回正常业务逻辑。

#### 实际示例

对于产品查询业务,当提供 `tid` 时,它返回产品信息。当产品促销时,访问量显著增加。然而,对于相同的 `tid`,在短时间内返回的信息保持一致。

该框架可以:
1. 在短时间内自动识别此 `tid` 为热点
2. 缓存结果以减少下游压力
3. 流量减少时自动移除热点
4. 返回正常查询处理

这本质上是实时热点监控和热点数据的短期缓存。

#### 基于 QPS 的热点检测

以下示例演示: 当相同的 `tid` 在 5 秒内被调用超过 50 次时,它自动成为热点并返回最后缓存的值。当调用在 5 秒内降至 50 次以下时,框架自动移除热点并返回正常代码执行。所有这一切都是自动的。

```java
@Hotspot(grade = FlowGrade.FLOW_GRADE_QPS, count = 50, duration = 5)
public Object get(String tid) {
    return tid;
}
```

**参数**:
- `grade`: FlowGrade.FLOW_GRADE_QPS - 使用 QPS(每秒查询数)作为测量维度
- `count`: 50 - 阈值(50 QPS)
- `duration`: 5 - 时间窗口(秒)

#### 基于线程的热点检测

`grade` 参数也可以使用线程数作为测量维度:

```java
@Hotspot(grade = FlowGrade.FLOW_GRADE_THREAD, count = 50, duration = 5)
public Object get(String tid) {
    return tid;
}
```

这意味着: 如果特定的 `tid` 在 5 秒内有超过 50 个线程同时运行,它将成为热点并直接返回缓存数据。

-----

### 使用场景 2: 服务降级

在任何方法上添加 `@Downgrade` 注解以启用自动降级功能。

#### 实际示例

一个方法需要调用性能较差且高延迟的外部接口。当并发增加时,线程池填满,线程池队列逐渐积累,导致超时或丢弃请求,可能使整个系统崩溃。

通过添加 `@Downgrade` 注解,可以解决此问题:

```java
@Downgrade(grade = FlowGrade.FLOW_GRADE_THREAD, count = 100)
public String get(String name) {
    return name;
}

public String getFallback(String name) {
    return "fallback";
}
```

**工作原理**:
- 当该方法的同时运行线程数超过 100 时,触发降级
- 降级自动调用名为 `originalMethodName + Fallback` 的方法(参数必须匹配)
- 触发降级后,直接返回 `"fallback"`
- 当线程数降至 100 以下时,框架自动移除降级并返回 `"name"`

**回退方法要求**:
- 方法名必须是 `originalMethodName + Fallback`
- 参数必须与原始方法匹配
- 如果未定义回退方法,触发降级时将抛出错误
- 您可以在回退方法中抛出错误,让上游系统知道方法已达到瓶颈

**参数**:
- `grade`: FlowGrade.FLOW_GRADE_THREAD - 使用线程数作为测量维度
- `count`: 100 - 阈值(100 个线程)

-----

## 📋 配置参考

### @Hotspot 注解

| 参数 | 类型 | 描述 |
|:---|:---|:---|
| grade | FlowGrade | 测量维度(QPS 或 THREAD)|
| count | int | 阈值 |
| duration | int | 时间窗口(秒)|

### @Downgrade 注解

| 参数 | 类型 | 描述 |
|:---|:---|:---|
| grade | FlowGrade | 测量维度(QPS 或 THREAD)|
| count | int | 阈值 |
| fallbackMethod | String | 自定义回退方法名(可选)|

### FlowGrade 枚举

| 值 | 描述 |
|:---|:---|
| FLOW_GRADE_QPS | 基于每秒查询数 |
| FLOW_GRADE_THREAD | 基于并发线程数 |

-----

## 🔧 高级配置

### 自定义回退方法

如果您不想使用默认命名约定,指定自定义回退方法:

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
    return "自定义回退: " + name;
}
```

### 多个阈值

您可以为不同场景配置多个注解:

```java
@Hotspot(grade = FlowGrade.FLOW_GRADE_QPS, count = 50, duration = 5)
@Downgrade(grade = FlowGrade.FLOW_GRADE_THREAD, count = 100)
public Object getData(String key) {
    // 业务逻辑
}
```

-----

## 💡 最佳实践

### 1. 选择合适的测量维度

**基于 QPS**: 适合读多场景,特别是可缓存的查询
**基于线程**: 适合执行时间长或外部调用的场景

### 2. 设置合理的阈值

- **过低**: 可能过于频繁地触发热点/降级
- **过高**: 可能无法有效保护系统
- **推荐**: 从正常峰值流量的 2-3 倍开始,根据监控调整

### 3. 结合监控使用

结合日志和监控以观察:
- 热点激活频率
- 降级触发率
- 回退方法执行次数

### 4. 回退策略

设计您的回退方法以:
- 返回缓存或默认值
- 提供简化的功能
- 即使功能减少也能保持系统稳定

-----

## 📊 性能影响

### 资源使用

| 指标 | 影响 |
|:---|:---|
| **内存开销** | 最小(仅缓存热点数据)|
| **CPU 使用** | 可忽略不计(高效计数)|
| **响应时间** | 热点期间减少 |

### 好处

- **下游保护**: 将数据库和外部服务的负载减少 80-95%
- **系统稳定性**: 防止级联故障
- **用户体验**: 在流量高峰期间保持快速响应时间

-----

## 🔄 版本兼容性

| Bus Limiter 版本 | Spring Boot 版本 | JDK 版本 |
|:---|:---|:---|
| 8.x | 3.x+ | 17+ |
| 7.x | 2.x+ | 11+ |

-----

## ❓ FAQ

### Q1: 热点和降级有什么区别?

**A**:
- **热点**: 自动缓存频繁访问的数据以减少下游负载
- **降级**: 系统过载时自动切换到回退逻辑

### Q2: 可以同时使用两个注解吗?

**A**: 可以,您可以在同一方法上使用 `@Hotspot` 和 `@Downgrade` 进行多级保护。

### Q3: 如何知道热点/降级是否被触发?

**A**: 在您的方法中添加日志并监控:
- 热点的缓存命中率
- 降级的回退方法执行次数
- 响应时间和错误率

### Q4: 这会影响我的正常业务逻辑吗?

**A**: 不会。框架仅在超过阈值时激活,并在流量减少时自动恢复正常。

### Q5: 如果回退方法失败会怎样?

**A**: 回退方法的异常将抛出给调用者。设计您的回退方法简单可靠。

-----

## 🎯 真实场景

### 电商: 闪购

```java
@Hotspot(grade = FlowGrade.FLOW_GRADE_QPS, count = 1000, duration = 10)
public Product getProduct(String productId) {
    // 闪购期间的产品查询
}
```

### API 网关: 限流

```java
@Downgrade(grade = FlowGrade.FLOW_GRADE_THREAD, count = 200)
public Response callExternalAPI(String request) {
    // 带有降级保护的外部 API 调用
}

public Response callExternalAPIFallback(String request) {
    return Response.cache();
}
```

### 数据库查询优化

```java
@Hotspot(grade = FlowGrade.FLOW_GRADE_QPS, count = 100, duration = 5)
public List<User> getUsers(String query) {
    // 频繁执行的数据库查询
}
```

-----

## 🤝 贡献

欢迎贡献!请随时提交拉取请求。

-----

## 📄 许可证

[许可证信息]

-----

## 🔗 相关文档

- [Spring Boot 文档](https://spring.io/projects/spring-boot)

-----

**由 Miaixz 团队用 ❤️ 构建**
