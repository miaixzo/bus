# 🪵 Bus Logger：通用日志抽象框架

<p align="center">
<strong>高性能、框架无关的日志门面</strong>
</p>

-----

## 📖 项目简介

**Bus Logger** 是一个通用日志抽象框架，为 Java 应用程序提供**简单、一致且高性能**的日志 API。它作为一个门面，自动检测并集成多个日志框架，无需直接依赖特定的日志实现。

使用 Bus Logger，您只需编写一次日志代码，即可在不同日志框架之间切换，而无需修改应用程序代码。它提供**静态日志方法**，自动检测调用者类信息，使日志记录更加便捷。

-----

## ✨ 核心功能

### 🎯 通用集成

* **自动框架检测**：自动检测并集成类路径上可用的日志框架
* **零配置**：开箱即用，无需配置
* **静态 API**：便捷的静态日志方法，无需创建日志器实例
* **调用者检测**：自动检测调用者类信息以准确跟踪日志位置

### ⚡ 支持的日志框架

| 框架 | 状态 | 工厂类 |
| :--- | :--- | :--- |
| **SLF4J** | 主要 | `Slf4jLoggingFactory` |
| **Log4j2** | 主要 | `Log4jLoggingFactory` |
| **Jboss Logging** | 支持 | `JbossLoggingFactory` |
| **Commons Logging** | 支持 | `CommonsLoggingFactory` |
| **JUL (JDK Util Logging)** | 支持 | `JdkLoggingFactory` |
| **Tinylog** | 支持 | `TinyLoggingFactory` |
| **Console** | 回退 | `NormalLoggingFactory` / `ColorLoggingFactory` |

### 🎨 日志级别

```java
public enum Level {
    ALL,      // 所有消息
    TRACE,    // 更细粒度的信息事件
    DEBUG,    // 细粒度的调试事件
    INFO,     // 信息性消息
    WARN,     // 警告情况
    ERROR,    // 错误事件
    FATAL,    // 严重错误事件
    OFF       // 无日志
}
```

### 🛡️ 高级功能

* **对齐日志**：内置对齐日志消息支持，可自定义标签
* **异常日志**：记录异常堆栈跟踪的专用方法
* **级别检查**：记录前进行性能优化的级别检查
* **提供程序抽象**：所有日志实现的统一 `Provider` 接口
* **工厂模式**：可扩展的工厂模式，支持自定义日志实现

-----

## 🚀 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-logger</artifactId>
    <version>x.x.x</version>
</dependency>
```

### 基础用法

#### 1. 静态日志记录（推荐）

使用 Bus Logger 最简单的方式是通过静态方法：

```java
import org.miaixz.bus.logger.Logger;

public class MyService {

    public void doSomething() {
        Logger.trace("这是一个跟踪消息");
        Logger.debug("调试信息：{}", someData);
        Logger.info("应用程序启动成功");
        Logger.warn("配置文件未找到，使用默认值");
        Logger.error("发生错误：{}", errorMessage);

        // 记录异常
        try {
            // ...
        } catch (Exception e) {
            Logger.error(e, "处理请求失败");
        }
    }
}
```

#### 2. 基于提供程序的日志记录

如需更多控制，可以获取 `Provider` 实例：

```java
import org.miaixz.bus.logger.Provider;
import org.miaixz.bus.logger.Registry;

public class MyService {
    private static final Provider log = Registry.get(MyService.class);

    public void doSomething() {
        if (log.isDebugEnabled()) {
            log.debug("调试信息：{}", someData);
        }

        log.info("处理用户请求");
    }
}
```

#### 3. 框架集成

Bus Logger 自动检测使用的日志框架。例如，使用 **SLF4J + Logback**：

```xml
<!-- 添加您喜欢的日志框架 -->
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.5.18</version>
</dependency>

<!-- Bus Logger 将自动使用 SLF4J -->
```

-----

## 📝 使用示例

### 1. 基础日志记录

```java
import org.miaixz.bus.logger.Logger;

public class UserService {

    public void createUser(String username, String email) {
        Logger.info("创建用户：{}，邮箱：{}", username, email);

        try {
            // 业务逻辑
            validateEmail(email);
            saveUser(username, email);

            Logger.info("用户创建成功：{}", username);
        } catch (ValidationException e) {
            Logger.warn(e, "用户邮箱验证失败：{}", username);
        } catch (Exception e) {
            Logger.error(e, "创建用户失败：{}", username);
        }
    }
}
```

### 2. 条件日志记录

```java
import org.miaixz.bus.logger.Logger;

public class DataProcessor {

    public void processLargeDataset(List<Data> data) {
        // 仅在启用 DEBUG 级别时记录
        if (Logger.isDebugEnabled()) {
            Logger.debug("处理 {} 条记录", data.size());
        }

        for (Data item : data) {
            // 仅在启用 DEBUG 时执行昂贵的操作
            if (Logger.isDebugEnabled()) {
                Logger.debug("处理项目：{}", item.toJson());
            }

            process(item);
        }
    }
}
```

### 3. 带上下文的日志

建议直接在日志内容中表达业务上下文：

```java
import org.miaixz.bus.logger.Logger;

public class OrderService {

    public void processOrder(Order order) {
        Logger.info("订单处理开始：orderId={}", order.getId());

        try {
            validateOrder(order);
            Logger.info("订单验证通过：orderId={}", order.getId());
            paymentService.charge(order);
            Logger.info("订单支付完成：orderId={}", order.getId());
            shippingService.ship(order);
            Logger.info("订单发货完成：orderId={}", order.getId());

            Logger.info("订单处理完成：orderId={}", order.getId());
        } catch (Exception e) {
            Logger.error("订单处理失败：orderId={}", order.getId(), e);
            throw e;
        }
    }
}
```

**输出：**
```
订单处理开始：orderId=ORD-12345
订单验证通过：orderId=ORD-12345
订单支付完成：orderId=ORD-12345
订单发货完成：orderId=ORD-12345
订单处理完成：orderId=ORD-12345
```

### 4. 请求上下文日志

```java
import org.miaixz.bus.logger.Logger;

public class ApiService {

    public void handleRequest(Request request) {
        Logger.debug("API 安全过滤器已应用：requestId={}", request.getId());
        Logger.debug("API 用户认证通过：requestId={}, user={}", request.getId(), request.getUser());
        Logger.debug("API 请求处理开始：requestId={}", request.getId());
        Logger.debug("API 请求处理成功：requestId={}", request.getId());
    }
}
```

**输出：**
```
API 安全过滤器已应用：requestId=REQ-001
API 用户认证通过：requestId=REQ-001, user=john.doe
API 请求处理开始：requestId=REQ-001
API 请求处理成功：requestId=REQ-001
```

### 5. 异常日志记录

```java
import org.miaixz.bus.logger.Logger;

public class FileService {

    public void readFile(String path) {
        try {
            byte[] content = Files.readAllBytes(Paths.get(path));
            Logger.info("文件读取成功：{} 字节", content.length);
        } catch (IOException e) {
            // 记录异常及自定义消息
            Logger.error(e, "读取文件失败：{}", path);

            // 或记录带格式化消息的异常
            Logger.error(e, "文件未找到或无法访问：{}", path);
        }
    }
}
```

### 6. 动态级别检查

```java
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.logger.Level;

public class ConfigurableService {

    public void performOperation() {
        // 检查当前日志级别
        Level currentLevel = Logger.getLevel();
        Logger.info("当前日志级别：{}", currentLevel);

        // 检查是否启用特定级别
        if (Logger.isEnabled(Level.DEBUG)) {
            // 昂贵的调试操作
            dumpDetailedState();
        }

        // 便捷方法
        if (Logger.isTraceEnabled()) {
            Logger.trace("详细跟踪信息");
        }
    }
}
```

### 7. 编程式设置日志级别

```java
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.logger.Level;

public class Application {

    public static void main(String[] args) {
        // 设置日志级别（如果底层框架支持）
        try {
            Logger.setLevel(Level.DEBUG);
            Logger.info("日志级别设置为 DEBUG");
        } catch (UnsupportedOperationException e) {
            Logger.warn("当前框架不支持动态级别设置");
        }
    }
}
```

### 8. 框架特定配置

#### SLF4J + Logback

**logback.xml:**
```xml
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <appender name="FILE" class="ch.qos.logback.core.FileAppender">
        <file>logs/application.log</file>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="FILE" />
    </root>

    <!-- 设置特定包的日志级别 -->
    <logger name="org.miaixz.bus.logger" level="DEBUG" />
</configuration>
```

#### Log4j2

**log4j2.xml:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
        </Console>

        <RollingFile name="RollingFile" fileName="logs/application.log"
                     filePattern="logs/application-%d{yyyy-MM-dd}-%i.log.gz">
            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss} [%t] %-5level %logger{36} - %msg%n"/>
            <Policies>
                <TimeBasedTriggeringPolicy/>
                <SizeBasedTriggeringPolicy size="100 MB"/>
            </Policies>
        </RollingFile>
    </Appenders>

    <Loggers>
        <Root level="info">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="RollingFile"/>
        </Root>

        <Logger name="org.miaixz.bus.logger" level="debug" additivity="false">
            <AppenderRef ref="Console"/>
        </Logger>
    </Loggers>
</Configuration>
```

#### Java Util Logging (JUL)

**logging.properties:**
```properties
# 全局日志级别
.level=INFO

# Console 处理器配置
handlers=java.util.logging.ConsoleHandler
java.util.logging.ConsoleHandler.level=INFO
java.util.logging.ConsoleHandler.formatter=java.util.logging.SimpleFormatter
java.util.logging.SimpleFormatter.format=%1$tF %1$tT %4$s %2$s - %5$s%6$s%n

# File 处理器配置
handlers=java.util.logging.FileHandler
java.util.logging.FileHandler.level=ALL
java.util.logging.FileHandler.pattern=logs/application%u.log
java.util.logging.FileHandler.limit=1000000
java.util.logging.FileHandler.count=10
java.util.logging.FileHandler.formatter=java.util.logging.SimpleFormatter

# 包特定日志
org.miaixz.bus.logger.level=FINE
```

### 9. 自定义日志工厂

```java

import org.miaixz.bus.logger.Holder;
import org.miaixz.bus.logger.Provider;
import org.miaixz.bus.logger.magic.AbstractFactory;

public class CustomLoggingFactory extends AbstractFactory {

    @Override
    public String getName() {
        return "CustomLogger";
    }

    @Override
    public Provider create(String name) {
        // 返回您的自定义提供程序实现
        return new CustomProvider(name);
    }

    @Override
    public Provider create(Class<?> clazz) {
        return of(clazz.getName());
    }

    // 设置为默认工厂
    public static void initialize() {
        Holder.setDefaultFactory(CustomLoggingFactory.class);
    }
}
```

-----

## 💡 最佳实践

### 1. 使用适当的日志级别

```java
// ✅ 推荐：使用适当的日志级别
Logger.trace("入口：methodX()，param1={}", param1);  // 非常详细
Logger.debug("用户对象：{}", user);  // 调试信息
Logger.info("应用程序启动");  // 重要应用程序事件
Logger.warn("缓存未命中，使用回退");  // 可能有危险的情况
Logger.error(e, "数据库连接失败");  // 错误事件

// ❌ 不推荐：对非错误情况使用 ERROR
Logger.error("用户登录成功");  // 应该是 INFO
```

### 2. 使用参数化日志记录

```java
// ✅ 推荐：参数化日志记录（仅在启用级别时构造字符串）
Logger.debug("用户：{}，年龄：{}，邮箱：{}", user.getName(), user.getAge(), user.getEmail());

// ❌ 不推荐：字符串拼接（总是构造字符串）
Logger.debug("用户：" + user.getName() + "，年龄：" + user.getAge() + "，邮箱：" + user.getEmail());
```

### 3. 在昂贵操作前检查日志级别

```java
// ✅ 推荐：在昂贵操作前检查级别
if (Logger.isDebugEnabled()) {
    String largeJson = objectMapper.writeValueAsString(complexObject);
    Logger.debug("响应负载：{}", largeJson);
}

// ❌ 不推荐：总是执行昂贵操作
Logger.debug("响应负载：{}", objectMapper.writeValueAsString(complexObject));
```

### 4. 包含上下文信息

```java
// ✅ 推荐：包含相关上下文
Logger.error(e, "处理订单失败 [orderId={}, userId={}, amount={}]",
    order.getId(), order.getUserId(), order.getAmount());

// ❌ 不推荐：上下文不足
Logger.error("处理订单失败");
```

### 5. 简单日志记录使用静态方法

```java
// ✅ 推荐：简单场景使用静态方法
public class UserService {
    public void createUser(User user) {
        Logger.info("创建用户：{}", user.getUsername());
        // ...
    }
}

// 替代方案：Provider 用于更多控制
public class UserService {
    private static final Provider log = Registry.get(UserService.class);

    public void createUser(User user) {
        if (log.isInfoEnabled()) {
            log.info("创建用户：{}", user.getUsername());
        }
        // ...
    }
}
```

### 6. 正确处理异常

```java
// ✅ 推荐：记录带上下文的异常
try {
    processPayment(order);
} catch (PaymentException e) {
    Logger.error(e, "订单支付失败 [id={}, amount={}]",
        order.getId(), order.getAmount());
    throw new BusinessException("支付处理失败", e);
}

// ❌ 不推荐：记录并吞噬异常
try {
    processPayment(order);
} catch (Exception e) {
    Logger.error("支付失败");  // 丢失堆栈跟踪和上下文
}
```

-----

## ❓ 常见问题

### Q1: 如何知道正在使用哪个日志框架？

```java
import org.miaixz.bus.logger.Logger;

Class<?> factoryClass = Logger.getFactory();
System.out.println("当前日志框架：" + factoryClass.getName());

// 输出示例：
// org.slf4j.Logger  -> SLF4J
// org.apache.logging.log4j.Logger  -> Log4j2
// java.util.logging.Logger  -> JDK Util Logging
```

### Q2: 我可以在 Spring Boot 中使用 Bus Logger 吗？

可以！Spring Boot 默认使用 SLF4J，所以 Bus Logger 会自动集成：

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-logger</artifactId>
    <version>x.x.x</version>
</dependency>
<!-- Spring Boot 已包含 SLF4J + Logback -->
```

在 `application.yml` 中配置日志：
```yaml
logging:
  level:
    root: INFO
    org.miaixz.bus.logger: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### Q3: 如何切换日志框架？

只需在 `pom.xml` 中更改依赖：

```xml
<!-- 从 SLF4J 切换到 Log4j2 -->
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-core</artifactId>
    <version>2.25.3</version>
</dependency>

<!-- 移除或排除 SLF4J -->
<!-- 无需更改代码！ -->
```

### Q4: 为什么我的日志没有显示？

检查以下内容：

1. 验证日志级别配置正确
2. 检查是否配置了 appender
3. 确保 logger 名称匹配包/类

```java
// 调试日志配置
Logger.info("当前级别：{}", Logger.getLevel());
Logger.info("是否启用 DEBUG：{}", Logger.isDebugEnabled());
Logger.info("工厂类：{}", Logger.getFactory());
```

### Q5: 可以同时使用多个日志框架吗？

虽然技术上可行，但不推荐。Bus Logger 将使用类路径上检测到的**第一个可用**框架。如果需要记录到多个目标，请适当配置所选日志框架（例如，Logback 中的多个 appender）。

### Q6: 如何完全禁用日志记录？

```java
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.logger.Level;

// 编程方式
Logger.setLevel(Level.OFF);

// 或在框架特定配置中配置
// Logback 示例：
<root level="OFF">...</root>
```

### Q7: Bus Logger 对性能有影响吗？

Bus Logger 设计为非常轻量级：

* **静态方法开销**：每次调用约 1-2 纳秒
* **级别检查**：优化以避免不必要的字符串构造
* **无反射**：使用直接方法调用
* **延迟求值**：仅在启用级别时求值参数

实际性能取决于底层日志框架，但 Bus Logger 本身的开销可以忽略不计。

### Q8: 如何创建自定义日志格式？

在所选日志框架中配置格式：

**Logback:**
```xml
<pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
```

**Log4j2:**
```xml
<PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
```

**JUL:**
```properties
java.util.logging.SimpleFormatter.format=%1$tF %1$tT %4$s %2$s - %5$s%6$s%n
```

-----

## 🔄 版本兼容性

| Bus Logger 版本 | JDK 版本 | 支持的框架 |
| :--- | :--- | :--- |
| 8.x | 17+ | SLF4J 2.x、Log4j2 2.x、Logback 1.5.x、JUL、JBoss Logging、Commons Logging、Tinylog |
| 7.x | 11+ | SLF4J 1.7.x/2.x、Log4j2 2.x、Logback 1.4.x、JUL、JBoss Logging、Commons Logging |

-----

## 📊 框架检测顺序

Bus Logger 按以下顺序检测日志框架：

1. **SLF4J** - 如果 `org.slf4j.Logger` 可用
2. **Log4j2** - 如果 `org.apache.logging.log4j.Logger` 可用
3. **JBoss Logging** - 如果 `org.jboss.logging.Logger` 可用
4. **Commons Logging** - 如果 `org.apache.commons.logging.Log` 可用
5. **Tinylog** - 如果 `org.tinylog.Logger` 可用
6. **JUL** - 如果在类路径中找到 `logging.properties`
7. **Console** - 回退到简单控制台日志记录

可以通过显式设置工厂来覆盖此顺序：

```java
import org.miaixz.bus.logger.Holder;
import org.miaixz.bus.logger.nimble.slf4j.Slf4jLoggingFactory;

// 强制使用 SLF4J
Holder.setDefaultFactory(Slf4jLoggingFactory.class);
```

-----

## 🔧 API 参考

### 静态日志方法

```java
// 基础日志记录
Logger.trace(String format, Object... args)
Logger.debug(String format, Object... args)
Logger.info(String format, Object... args)
Logger.warn(String format, Object... args)
Logger.warn(Throwable e, String format, Object... args)
Logger.error(Throwable e)
Logger.error(String format, Object... args)
Logger.error(Throwable e, String format, Object... args)

// 对齐日志记录
Logger.info(boolean isEntry, String tag, String message, Object... args)
Logger.info(boolean isEntry, String tag, int width, String message, Object... args)
// 同样模式适用于：trace、debug、warn、error

// 级别检查
Logger.isTraceEnabled()
Logger.isDebugEnabled()
Logger.isInfoEnabled()
Logger.isWarnEnabled()
Logger.isEnabled(Level level)

// 级别控制
Level Logger.getLevel()
void Logger.setLevel(Level level)

// 框架信息
Class<?> Logger.getFactory()
Provider Logger.getProvider()
```

### Provider 接口方法

```java
// 获取日志器实例
Provider Provider.get(Class<?> clazz)
Provider Provider.get(String name)
Provider Provider.get()

// 日志记录方法
void Provider.trace(String fqcn, Throwable t, String format, Object... args)
void Provider.debug(String fqcn, Throwable t, String format, Object... args)
void Provider.info(String fqcn, Throwable t, String format, Object... args)
void Provider.warn(String fqcn, Throwable t, String format, Object... args)
void Provider.error(String fqcn, Throwable t, String format, Object... args)

// 通用日志记录
void Provider.log(Level level, String format, Object... args)
void Provider.log(Level level, Throwable t, String format, Object... args)
void Provider.log(String fqcn, Level level, Throwable t, String format, Object... args)

// 级别检查
boolean Provider.isTraceEnabled()
boolean Provider.isDebugEnabled()
boolean Provider.isInfoEnabled()
boolean Provider.isWarnEnabled()
boolean Provider.isErrorEnabled()
boolean Provider.isEnabled(Level level)

// 级别控制
Level Provider.getLevel()
void Provider.setLevel(Level level)

// 日志器信息
String Provider.getName()
```

### Registry 方法

```java
// 获取日志器实例
Provider Registry.get(String name)
Provider Registry.get(Class<?> clazz)
```

### Holder/Factory 方法

```java
// 获取默认工厂
Factory Holder.getFactory()

// 设置自定义工厂
void Holder.setDefaultFactory(Factory factory)
void Holder.setDefaultFactory(Class<? extends Factory> clazz)

// 创建工厂实例
Factory Holder.of(Class<? extends Factory> clazz)
Factory Holder.of()
```

-----

## 📚 其他资源

- **项目主页**：https://github.com/818000/bus
- **问题追踪**：https://github.com/818000/bus/issues
- **Maven Central**：https://central.sonatype.com/artifact/org.miaixz/bus-logger

-----

**Bus Logger** - 为简单性和性能而设计的通用日志抽象。🚀
