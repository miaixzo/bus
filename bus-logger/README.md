# 🪵 Bus Logger: Universal Logging Abstraction Framework

<p align="center">
<strong>High-Performance, Framework-Agnostic Logging Facade</strong>
</p>

-----

## 📖 Project Introduction

**Bus Logger** is a universal logging abstraction framework that provides a **simple, consistent, and high-performance** logging API for Java applications. It acts as a facade that automatically detects and integrates with multiple logging frameworks, eliminating the need for direct dependencies on specific logging implementations.

With Bus Logger, you can write logging code once and switch between different logging frameworks without changing your application code. It provides **static logging methods** that automatically detect the caller's class information, making logging even more convenient.

-----

## ✨ Core Features

### 🎯 Universal Integration

* **Automatic Framework Detection**: Automatically detects and integrates with available logging frameworks on the classpath
* **Zero Configuration**: Works out of the box with no required configuration
* **Static API**: Convenient static logging methods that don't require logger instance creation
* **Caller Detection**: Automatically detects caller class information for accurate log location tracking

### ⚡ Supported Logging Frameworks

| Framework | Status | Factory Class |
| :--- | :--- | :--- |
| **SLF4J** | Primary | `Slf4jLoggingFactory` |
| **Log4j2** | Primary | `Log4jLoggingFactory` |
| **Jboss Logging** | Supported | `JbossLoggingFactory` |
| **Commons Logging** | Supported | `CommonsLoggingFactory` |
| **JUL (JDK Util Logging)** | Supported | `JdkLoggingFactory` |
| **Tinylog** | Supported | `TinyLoggingFactory` |
| **Console** | Fallback | `NormalLoggingFactory` / `ColorLoggingFactory` |

### 🎨 Logging Levels

```java
public enum Level {
    ALL,      // All messages
    TRACE,    // Finer-grained informational events
    DEBUG,    // Fine-grained debugging events
    INFO,     // Informational messages
    WARN,     // Warning situations
    ERROR,    // Error events
    FATAL,    // Severe error events
    OFF       // No logging
}
```

### 🛡️ Advanced Features

* **Aligned Logging**: Built-in support for aligned log messages with customizable tags
* **Exception Logging**: Dedicated methods for logging exceptions with stack traces
* **Level Checking**: Performance-optimized level checking before logging
* **Provider Abstraction**: Unified `Provider` interface for all logging implementations
* **Factory Pattern**: Extensible factory pattern for custom logging implementations

-----

## 🚀 Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-logger</artifactId>
    <version>x.x.x</version>
</dependency>
```

### Basic Usage

#### 1. Static Logging (Recommended)

The simplest way to use Bus Logger is through static methods:

```java
import org.miaixz.bus.logger.Logger;

public class MyService {

    public void doSomething() {
        Logger.trace("This is a trace message");
        Logger.debug("Debug information: {}", someData);
        Logger.info("Application started successfully");
        Logger.warn("Configuration file not found, using defaults");
        Logger.error("An error occurred: {}", errorMessage);

        // Log with exception
        try {
            // ...
        } catch (Exception e) {
            Logger.error(e, "Failed to process request");
        }
    }
}
```

#### 2. Provider-Based Logging

For more control, you can obtain a `Provider` instance:

```java
import org.miaixz.bus.logger.Provider;
import org.miaixz.bus.logger.Registry;

public class MyService {
    private static final Provider log = Registry.get(MyService.class);

    public void doSomething() {
        if (log.isDebugEnabled()) {
            log.debug("Debug information: {}", someData);
        }

        log.info("Processing user request");
    }
}
```

#### 3. Framework Integration

Bus Logger automatically detects the logging framework in use. For example, with **SLF4J + Logback**:

```xml
<!-- Add your preferred logging framework -->
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.5.18</version>
</dependency>

<!-- Bus Logger will automatically use SLF4J -->
```

-----

## 📝 Usage Examples

### 1. Basic Logging

```java
import org.miaixz.bus.logger.Logger;

public class UserService {

    public void createUser(String username, String email) {
        Logger.info("Creating user: {}, email: {}", username, email);

        try {
            // Business logic
            validateEmail(email);
            saveUser(username, email);

            Logger.info("User created successfully: {}", username);
        } catch (ValidationException e) {
            Logger.warn(e, "Email validation failed for user: {}", username);
        } catch (Exception e) {
            Logger.error(e, "Failed to create user: {}", username);
        }
    }
}
```

### 2. Conditional Logging

```java
import org.miaixz.bus.logger.Logger;

public class DataProcessor {

    public void processLargeDataset(List<Data> data) {
        // Only log when DEBUG level is enabled
        if (Logger.isDebugEnabled()) {
            Logger.debug("Processing {} records", data.size());
        }

        for (Data item : data) {
            // Expensive operation only performed when DEBUG is enabled
            if (Logger.isDebugEnabled()) {
                Logger.debug("Processing item: {}", item.toJson());
            }

            process(item);
        }
    }
}
```

### 3. Aligned Logging with Tags

Bus Logger supports aligned logging for better readability:

```java
import org.miaixz.bus.logger.Logger;

public class OrderService {

    public void processOrder(Order order) {
        // Entry log with alignment (default width: 15)
        Logger.info(true, "Order", "Processing order: {}", order.getId());

        try {
            validateOrder(order);
            Logger.info(true, "Validate", "Order validation passed");
            paymentService.charge(order);
            Logger.info(true, "Payment", "Payment completed");
            shippingService.ship(order);
            Logger.info(true, "Shipping", "Order shipped successfully");

            // Exit log
            Logger.info(false, "Order", "Order processing completed: {}", order.getId());
        } catch (Exception e) {
            Logger.error(false, "Order", "Failed to process order: {}", order.getId());
            throw e;
        }
    }
}
```

**Output:**
```
===>     Order: Processing order: ORD-12345
===>   Validate: Order validation passed
===>   Payment: Payment completed
===>   Shipping: Order shipped successfully
<==     Order: Order processing completed: ORD-12345
```

### 4. Custom Width Alignment

```java
import org.miaixz.bus.logger.Logger;

public class ApiService {

    public void handleRequest(Request request) {
        // Custom width (20 characters)
        Logger.debug(true, "Filter", 20, "Applying security filter");
        Logger.debug(true, "Auth", 20, "Authenticating user: {}", request.getUser());
        Logger.debug(true, "Process", 20, "Processing request: {}", request.getId());
        Logger.debug(false, "Process", 20, "Request processed successfully");
    }
}
```

**Output:**
```
===>            Filter: Applying security filter
===>              Auth: Authenticating user: john.doe
===>           Process: Processing request: REQ-001
<==           Process: Request processed successfully
```

### 5. Exception Logging

```java
import org.miaixz.bus.logger.Logger;

public class FileService {

    public void readFile(String path) {
        try {
            byte[] content = Files.readAllBytes(Paths.get(path));
            Logger.info("File read successfully: {} bytes", content.length);
        } catch (IOException e) {
            // Log exception with custom message
            Logger.error(e, "Failed to read file: {}", path);

            // Or log exception with formatted message
            Logger.error(e, "File not found or inaccessible: {}", path);
        }
    }
}
```

### 6. Dynamic Level Checking

```java
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.logger.Level;

public class ConfigurableService {

    public void performOperation() {
        // Check current logging level
        Level currentLevel = Logger.getLevel();
        Logger.info("Current logging level: {}", currentLevel);

        // Check if specific level is enabled
        if (Logger.isEnabled(Level.DEBUG)) {
            // Expensive debug operation
            dumpDetailedState();
        }

        // Convenience methods
        if (Logger.isTraceEnabled()) {
            Logger.trace("Detailed trace information");
        }
    }
}
```

### 7. Setting Logging Level Programmatically

```java
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.logger.Level;

public class Application {

    public static void main(String[] args) {
        // Set logging level (if supported by underlying framework)
        try {
            Logger.setLevel(Level.DEBUG);
            Logger.info("Logging level set to DEBUG");
        } catch (UnsupportedOperationException e) {
            Logger.warn("Dynamic level setting not supported by current framework");
        }
    }
}
```

### 8. Framework-Specific Configuration

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

    <!-- Set specific package logging level -->
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
# Global logging level
.level=INFO

# Console handler configuration
handlers=java.util.logging.ConsoleHandler
java.util.logging.ConsoleHandler.level=INFO
java.util.logging.ConsoleHandler.formatter=java.util.logging.SimpleFormatter
java.util.logging.SimpleFormatter.format=%1$tF %1$tT %4$s %2$s - %5$s%6$s%n

# File handler configuration
handlers=java.util.logging.FileHandler
java.util.logging.FileHandler.level=ALL
java.util.logging.FileHandler.pattern=logs/application%u.log
java.util.logging.FileHandler.limit=1000000
java.util.logging.FileHandler.count=10
java.util.logging.FileHandler.formatter=java.util.logging.SimpleFormatter

# Package-specific logging
org.miaixz.bus.logger.level=FINE
```

### 9. Custom Logging Factory

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
        // Return your custom provider implementation
        return new CustomProvider(name);
    }

    @Override
    public Provider create(Class<?> clazz) {
        return of(clazz.getName());
    }

    // Set as default factory
    public static void initialize() {
        Holder.setDefaultFactory(CustomLoggingFactory.class);
    }
}
```

-----

## 💡 Best Practices

### 1. Use Appropriate Log Levels

```java
// ✅ Recommended: Use appropriate log levels
Logger.trace("Entry: methodX(), param1={}", param1);  // Very detailed
Logger.debug("User object: {}", user);  // Debugging information
Logger.info("Application started");  // Important application events
Logger.warn("Cache miss, using fallback");  // Potentially harmful situations
Logger.error(e, "Database connection failed");  // Error events

// ❌ Not Recommended: Using ERROR for non-error situations
Logger.error("User logged in successfully");  // Should be INFO
```

### 2. Use Parameterized Logging

```java
// ✅ Recommended: Parameterized logging (only constructs string if level enabled)
Logger.debug("User: {}, Age: {}, Email: {}", user.getName(), user.getAge(), user.getEmail());

// ❌ Not Recommended: String concatenation (always constructs string)
Logger.debug("User: " + user.getName() + ", Age: " + user.getAge() + ", Email: " + user.getEmail());
```

### 3. Check Log Level Before Expensive Operations

```java
// ✅ Recommended: Check level before expensive operations
if (Logger.isDebugEnabled()) {
    String largeJson = objectMapper.writeValueAsString(complexObject);
    Logger.debug("Response payload: {}", largeJson);
}

// ❌ Not Recommended: Expensive operation always executed
Logger.debug("Response payload: {}", objectMapper.writeValueAsString(complexObject));
```

### 4. Include Context Information

```java
// ✅ Recommended: Include relevant context
Logger.error(e, "Failed to process order [orderId={}, userId={}, amount={}]",
    order.getId(), order.getUserId(), order.getAmount());

// ❌ Not Recommended: Insufficient context
Logger.error("Failed to process order");
```

### 5. Use Static Methods for Simple Logging

```java
// ✅ Recommended: Static methods for simple use cases
public class UserService {
    public void createUser(User user) {
        Logger.info("Creating user: {}", user.getUsername());
        // ...
    }
}

// Alternative: Provider for more control
public class UserService {
    private static final Provider log = Registry.get(UserService.class);

    public void createUser(User user) {
        if (log.isInfoEnabled()) {
            log.info("Creating user: {}", user.getUsername());
        }
        // ...
    }
}
```

### 6. Handle Exceptions Properly

```java
// ✅ Recommended: Log exception with context
try {
    processPayment(order);
} catch (PaymentException e) {
    Logger.error(e, "Payment failed for order [id={}, amount={}]",
        order.getId(), order.getAmount());
    throw new BusinessException("Payment processing failed", e);
}

// ❌ Not Recommended: Log and swallow exception
try {
    processPayment(order);
} catch (Exception e) {
    Logger.error("Payment failed");  // Lost stack trace and context
}
```

-----

## ❓ Frequently Asked Questions

### Q1: How do I know which logging framework is being used?

```java
import org.miaixz.bus.logger.Logger;

Class<?> factoryClass = Logger.getFactory();
System.out.println("Current logging framework: " + factoryClass.getName());

// Output examples:
// org.slf4j.Logger  -> SLF4J
// org.apache.logging.log4j.Logger  -> Log4j2
// java.util.logging.Logger  -> JDK Util Logging
```

### Q2: Can I use Bus Logger with Spring Boot?

Yes! Spring Boot uses SLF4J by default, so Bus Logger will automatically integrate:

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-logger</artifactId>
    <version>x.x.x</version>
</dependency>
<!-- Spring Boot already includes SLF4J + Logback -->
```

Configure logging in `application.yml`:
```yaml
logging:
  level:
    root: INFO
    org.miaixz.bus.logger: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### Q3: How do I switch logging frameworks?

Simply change the dependency in your `pom.xml`:

```xml
<!-- Switch from SLF4J to Log4j2 -->
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-core</artifactId>
    <version>2.25.3</version>
</dependency>

<!-- Remove or exclude SLF4J -->
<!-- No code changes needed! -->
```

### Q4: Why are my logs not appearing?

Check the following:

1. Verify the logging level is configured correctly
2. Check if the appender is configured
3. Ensure the logger name matches the package/class

```java
// Debug logging configuration
Logger.info("Current level: {}", Logger.getLevel());
Logger.info("Is DEBUG enabled: {}", Logger.isDebugEnabled());
Logger.info("Factory class: {}", Logger.getFactory());
```

### Q5: Can I use multiple logging frameworks simultaneously?

While technically possible, it's not recommended. Bus Logger will use the **first available** framework detected on the classpath. If you need to log to multiple destinations, configure your chosen logging framework appropriately (e.g., multiple appenders in Logback).

### Q6: How do I disable logging entirely?

```java
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.logger.Level;

// Programmatically
Logger.setLevel(Level.OFF);

// Or configure in framework-specific config
// Logback example:
<root level="OFF">...</root>
```

### Q7: Does Bus Logger have any performance impact?

Bus Logger is designed to be very lightweight:

* **Static method overhead**: ~1-2 nanoseconds per call
* **Level checking**: Optimized to avoid unnecessary string construction
* **No reflection**: Uses direct method calls
* **Lazy evaluation**: Parameters only evaluated if level is enabled

The actual performance depends on the underlying logging framework, but Bus Logger itself adds negligible overhead.

### Q8: How can I create custom log formats?

Configure the format in your chosen logging framework:

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

## 🔄 Version Compatibility

| Bus Logger Version | JDK Version | Supported Frameworks |
| :--- | :--- | :--- |
| 8.x | 17+ | SLF4J 2.x, Log4j2 2.x, Logback 1.5.x, JUL, JBoss Logging, Commons Logging, Tinylog |
| 7.x | 11+ | SLF4J 1.7.x/2.x, Log4j2 2.x, Logback 1.4.x, JUL, JBoss Logging, Commons Logging |

-----

## 📊 Framework Detection Order

Bus Logger detects logging frameworks in the following order:

1. **SLF4J** - If `org.slf4j.Logger` is available
2. **Log4j2** - If `org.apache.logging.log4j.Logger` is available
3. **JBoss Logging** - If `org.jboss.logging.Logger` is available
4. **Commons Logging** - If `org.apache.commons.logging.Log` is available
5. **Tinylog** - If `org.tinylog.Logger` is available
6. **JUL** - If `logging.properties` is found in classpath
7. **Console** - Fallback to simple console logging

You can override this by explicitly setting the factory:

```java
import org.miaixz.bus.logger.Holder;
import org.miaixz.bus.logger.metric.slf4j.Slf4jLoggingFactory;

// Force SLF4J usage
Holder.setDefaultFactory(Slf4jLoggingFactory.class);
```

-----

## 🔧 API Reference

### Static Logger Methods

```java
// Basic logging
Logger.trace(String format, Object... args)
Logger.debug(String format, Object... args)
Logger.info(String format, Object... args)
Logger.warn(String format, Object... args)
Logger.warn(Throwable e, String format, Object... args)
Logger.error(Throwable e)
Logger.error(String format, Object... args)
Logger.error(Throwable e, String format, Object... args)

// Aligned logging
Logger.info(boolean isEntry, String tag, String message, Object... args)
Logger.info(boolean isEntry, String tag, int width, String message, Object... args)
// Same pattern exists for: trace, debug, warn, error

// Level checking
Logger.isTraceEnabled()
Logger.isDebugEnabled()
Logger.isInfoEnabled()
Logger.isWarnEnabled()
Logger.isEnabled(Level level)

// Level control
Level Logger.getLevel()
void Logger.setLevel(Level level)

// Framework information
Class<?> Logger.getFactory()
Provider Logger.getProvider()
```

### Provider Interface Methods

```java
// Get logger instances
Provider Provider.get(Class<?> clazz)
Provider Provider.get(String name)
Provider Provider.get()

// Logging methods
void Provider.trace(String fqcn, Throwable t, String format, Object... args)
void Provider.debug(String fqcn, Throwable t, String format, Object... args)
void Provider.info(String fqcn, Throwable t, String format, Object... args)
void Provider.warn(String fqcn, Throwable t, String format, Object... args)
void Provider.error(String fqcn, Throwable t, String format, Object... args)

// General logging
void Provider.log(Level level, String format, Object... args)
void Provider.log(Level level, Throwable t, String format, Object... args)
void Provider.log(String fqcn, Level level, Throwable t, String format, Object... args)

// Level checking
boolean Provider.isTraceEnabled()
boolean Provider.isDebugEnabled()
boolean Provider.isInfoEnabled()
boolean Provider.isWarnEnabled()
boolean Provider.isErrorEnabled()
boolean Provider.isEnabled(Level level)

// Level control
Level Provider.getLevel()
void Provider.setLevel(Level level)

// Logger information
String Provider.getName()
```

### Registry Methods

```java
// Get logger instances
Provider Registry.get(String name)
Provider Registry.get(Class<?> clazz)
```

### Holder/Factory Methods

```java
// Get default factory
Factory Holder.getFactory()

// Set custom factory
void Holder.setDefaultFactory(Factory factory)
void Holder.setDefaultFactory(Class<? extends Factory> clazz)

// Create factory instance
Factory Holder.of(Class<? extends Factory> clazz)
Factory Holder.of()
```

-----

## 📚 Additional Resources

- **Project Homepage**: https://github.com/818000/bus
- **Issue Tracker**: https://github.com/818000/bus/issues
- **Maven Central**: https://central.sonatype.com/artifact/org.miaixz/bus-logger

-----

**Bus Logger** - Universal logging abstraction designed for simplicity and performance. 🚀
