# bus-logger

`bus-logger` 是Bus模块使用的框架中立的日志外观。它通过日志记录后端提供一个 API
在运行时可用，保持后端本机占位符格式，解析调用者身份，缓存提供程序，以及
提供小型预输出处理管道。它不拥有应用程序日志记录配置或敏感数据
政策。

## 模块职责

该模块负责：

- 选择可用的日志记录实现；
- 解析和缓存命名或基于类的日志提供程序；
- 公开 TRACE、DEBUG、INFO、WARN 和 ERROR API；
- 转发参数化消息而不急于格式化它们；
- 在提供程序输出之前应用已注册的 `Operator` 实例；
- 提供正常和彩色控制台后备；
- 公开 SPI 合约以获取额外的日志记录后端。

该模块不负责：

- 配置 Logback、Log4j 2、JUL 或其他后端；
- 选择应用程序的日志文件、模式、轮换或保留策略；
- 对密码、令牌、cookie 或其他受保护数据进行分类；
- 保留请求或 Spring 应用程序上下文状态。

敏感数据分类属于 `bus-sensitive`。脱敏器的 Spring 生命周期注册属于
`bus-starter`。

## 依赖关系

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-logger</artifactId>
    <version>${revision}</version>
</dependency>
```

后端依赖项是可选的。应用程序应该添加并配置它想要使用的后端。

## 架构

```text
application code
      |
      v
   Logger --------------------------+
      |                             |
      v                             v
  Loggable -> Executor -> Operator(s)
      |
      v
   Provider -> backend adapter -> logging framework
      ^
      |
 Registry -> Holder -> Factory -> SPI discovery
```

| 类型 | 责任 |
|------------|--------------------------------------------------------------------------------------------------------|
| `Logger` | 静态外观、调用者解析、级别检查、对齐诊断输出和通用 `log` 调度。 |
| `Level` | 常见 `TRACE`、`DEBUG`、`INFO`、`WARN` 和 `ERROR` 级别型号。                                      |
| `Provider` | 后端中立日志记录操作和级别检查。                                                   |
| `Factory` | 创建并缓存名称和类的提供程序。                                                    |
| `Holder` | 选择或显式安装默认工厂。                                                    |
| `Registry` | 解析名称或类的缓存提供程序。                                                      |
| `Loggable` | 不可变事件快照及其参数数组的防御副本。                                  |
| `Operator` | 转换完整事件或命名诊断值，而不写入输出。                        |
| `Executor` | 按注册顺序应用运算符，并将日志记录与运算符故障隔离。                   |

## 支持的后端

该模块包括用于以下用途的适配器：

| 后端 | 工厂 | 提供商 |
|------------------------|-------------------------|--------------------------|
| SLF4J | `Slf4jLoggingFactory` | `Slf4jLoggingProvider` |
| Log4j 2 | `Log4jLoggingFactory` | `Log4jLoggingProvider` |
| Apache Commons 日志记录 | `CommonsLoggingFactory` | `CommonsLoggingProvider` |
| JBoss 日志记录 | `JbossLoggingFactory` | `JbossLoggingProvider` |
| `java.util.logging` | `JdkLoggingFactory` | `JdkLoggingProvider` |
| tinylog | `TinyLoggingFactory` | `TinyLoggingProvider` |
| 彩色控制台 | `ColorLoggingFactory` | `ColorLoggingProvider` |
| 普通控制台 | `NormalLoggingFactory` | `NormalLoggingProvider` |

工厂是通过`META-INF/services/org.miaixz.bus.logger.Factory`发现的。发现选择第一个
可用的 SPI 实现。如果没有可用的受支持提供程序，则类路径 `logging.properties` 选择 JUL；
否则使用普通控制台提供程序。

使用`Logger.getFactory()`检查所选工厂类。仅当以下情况时才使用 `Holder.setDefaultFactory(...)`
应用程序必须在第一个提供者解析之前强制后端。

## 基本用法

### 静态外观

```java
import org.miaixz.bus.logger.Logger;

Logger.trace("Loading order: orderId={}", orderId);
Logger.debug("Resolved {} order lines", lines.size());
Logger.info("Order accepted: orderId={}", orderId);
Logger.warn("Retrying request: attempt={}", attempt);
Logger.error(failure, "Order processing failed: orderId={}", orderId);
```

`{}` 参数在到达提供者之前保持独立。不要通过字符串连接来构建消息
当占位符格式足够时。

### 可重复使用的提供者

当类执行频繁的日志记录或必须保护昂贵的诊断工作时解析提供程序：

```java
import org.miaixz.bus.logger.Provider;
import org.miaixz.bus.logger.Registry;

private static final Provider LOG = Registry.get(OrderService.class);

if (LOG.isDebugEnabled()) {
    LOG.debug("Loaded order graph: {}", buildExpensiveDiagnostic(order));
}
```

`Registry.get(Class<?>)` 和 `Registry.get(String)` 返回由所选工厂缓存的提供程序。

### 异常

```java
try {
    repository.save(order);
} catch (RuntimeException failure) {
    Logger.error(failure, "Unable to persist order: orderId={}", order.getId());
    throw failure;
}
```

通过可抛出重载传递异常。不要将异常插入消息并丢失其堆栈
痕迹。

### 动态级别

```java
import org.miaixz.bus.logger.Level;
import org.miaixz.bus.logger.Logger;

if (Logger.isEnabled(Level.DEBUG)) {
    Logger.log(Level.DEBUG, null, "Cache state: key={}, value={}", key, value);
}

Level previous = Logger.getLevel();
Logger.setLevel(Level.INFO);
```

编程级别的更改会影响支持运行时级别控制的提供程序。正常的后端配置仍然是
首选应用程序级机制。

### 对齐的诊断输出

`Logger` 重载接受 `isEntry`、`tag` 和可选的 `width`，旨在用于结构化启动和
生命周期诊断：

```java
Logger.info(true, "Storage", "Initializing provider: type={}", providerType);
Logger.info(false, "Storage", 24, "Provider ready: type={}", providerType);
```

对普通业务日志使用普通占位符重载。

## 事件处理

`Operator` 可以在提供商调度之前立即转换每个事件：

```java
import org.miaixz.bus.logger.Executor;
import org.miaixz.bus.logger.Loggable;
import org.miaixz.bus.logger.Operator;

Operator tenantTag = event -> new Loggable(
        event.level(),
        event.throwable(),
        "[tenant-a] " + event.format(),
        event.arguments());

Executor.register(tenantTag);
try {
    Logger.info("Order accepted: orderId={}", orderId);
} finally {
    Executor.unregister(tenantTag);
}
```

操作员的行为受到刻意限制：

- 注册基于对象身份；
- 同一实例的重复注册是引用计数的；
- 执行顺序为登记顺序；
- 返回 `null` 使当前事件保持不变；
- 运算符异常产生 `[LOG PROCESSING FAILED]` 而不暴露被拒绝的参数；
- 热路径读取不可变的操作员快照，无需注册锁定。

`Executor.processValue(key, value)` 将相同的运算符应用于外观外部生成的命名值。很有用
对于仍然需要应用程序注册保护策略的结构化诊断。

## 敏感日志记录

`bus-logger` 故意不包含敏感字段名称列表。当使用`bus-sensitive`和`bus-starter`时，
`SensitiveConfiguration` 创建一个 `Sanitizer`，`SensitiveBinding` 将其注册到 `Executor` 的生命周期中
Spring 应用程序上下文。

```yaml
bus:
  sensitive:
    enabled: true
```

当值需要分类时使用命名占位符：

```java
Logger.warn("Login rejected: username={}, password={}", username, password);
```

占位符之前的名称为脱敏器提供了足够的上下文来保护 `password`。位置值
没有有意义的字段名称就无法安全分类。

## 自定义后端

实现`Factory`和`Provider`，然后通过Java服务加载器发布工厂：

```java
public final class AcmeFactory implements Factory {

    @Override
    public String getName() {
        return "Acme";
    }

    @Override
    public Provider of(String name) {
        return new AcmeProvider(name);
    }

    @Override
    public Provider of(Class<?> type) {
        return of(type.getName());
    }

}
```

资源文件：

```text
META-INF/services/org.miaixz.bus.logger.Factory
```

它的内容是完全合格的工厂类名称。适配器应保留占位符参数并实现
所有级别检查均准确。

## 包结构

| 包装 | 内容 |
|-------------------------------------|--------------------------------------------------------------------|
| `org.miaixz.bus.logger` | 公共外观、事件模型、注册表、执行器和 SPI 合约。 |
| `org.miaixz.bus.logger.magic` | 共享抽象工厂和提供程序实现。              |
| `org.miaixz.bus.logger.magic.level` | 可重复使用的特定级别合约。                                 |
| `org.miaixz.bus.logger.nimble.*` | 后端适配器。                                                  |

所有这些包均由 JPMS 模块导出。后端模块是可选的静态要求。

## 最佳实践

- 优先选择参数化日志记录而不是字符串连接。
- 通过匹配级别检查来保护昂贵的诊断结构。
- 使用基于类的提供程序来获得稳定的记录器身份。
- 将失败传递给可抛出的重载。
- 每个生命周期所有者注册一次横切操作员并始终注销它们。
- 禁止伐木工厂和提供商接触业务屏蔽规则。
- 在所选后端中配置格式、文件、旋转和保留。

## 原生镜像

可访问性元数据涵盖服务加载工厂和精确的动态访问成员。条目按 A-Z 排序。
禁止诸如 `allDeclaredConstructors`、`allDeclaredMethods` 和 `allDeclaredFields` 之类的广泛反射补助金。

## 验证边界

Bus包含且不运行任何测试。提供程序、管道、生命周期、元数据、AOT 和本机映像测试均维护在
兄弟 Abarth 存储库。Bus构建必须明确跳过测试。
