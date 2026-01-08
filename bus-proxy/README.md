# 🎯 Bus Proxy: Dynamic Proxy and AOP Framework

<p align="center">
<strong>Make Dynamic Proxy Simple - JDK Dynamic Proxy and AOP Support</strong>
</p>

-----

## 📖 Project Introduction

**Bus Proxy** provides a simplified and powerful approach to working with dynamic proxies in Java. It encapsulates JDK dynamic proxy functionality, making it easy to implement AOP (Aspect-Oriented Programming) patterns without requiring an IoC container.

The proxy design pattern allows you to provide a "surrogate or placeholder for another object to control access to it." Proxies can be used in various ways:

* **Lazy Initialization**: Proxy acts as a stand-in for the actual implementation, instantiating it only when absolutely necessary
* **Security**: Proxy objects can verify that the user has the permissions required to execute a method (like EJB)
* **Logging**: Proxy can log every method call, providing valuable debugging information
* **Performance Monitoring**: Proxy can log each method call to a performance monitor, allowing system administrators to see which parts of the system might be bogged down

-----

## ✨ Core Features

* **JDK Dynamic Proxy Encapsulation**: Simplified API for creating dynamic proxies
* **Non-IoC AOP Support**: AOP functionality without requiring Spring or other IoC containers
* **Interceptor Support**: Chain multiple interceptors for cross-cutting concerns
* **Built-in Aspects**: Pre-built aspects for common scenarios
* **Method Interception**: Intercept method calls before and after execution
* **Flexible Configuration**: Easy to configure and customize proxy behavior

-----

## 🚀 Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-proxy</artifactId>
    <version>8.x.x</version>
</dependency>
```

### Basic Usage

#### 1. Create a Simple Proxy

```java
// Define an interface
public interface UserService {
    String getUserName(Long userId);
    void updateUser(User user);
}

// Implement the interface
public class UserServiceImpl implements UserService {
    @Override
    public String getUserName(Long userId) {
        return "John Doe";
    }

    @Override
    public void updateUser(User user) {
        // Update logic
    }
}

// Create a proxy with logging
UserService proxy = Builder.proxy(UserServiceImpl.class)
    .addAspect(new SimpleAspect() {
        @Override
        public void before(Object target, Method method, Object[] args) {
            System.out.println("Calling: " + method.getName());
        }

        @Override
        public void after(Object target, Method method, Object[] args, Object result) {
            System.out.println("Called: " + method.getName());
        }
    })
    .build();
```

-----

## 📝 Usage Examples

### Example 1: Simple Aspect

```java
// Extend SimpleAspect and override needed methods
public class LoggingAspect extends SimpleAspect {
    @Override
    public void before(Object target, Method method, Object[] args) {
        System.out.println("Before: " + method.getName());
    }

    @Override
    public void after(Object target, Method method, Object[] args, Object result) {
        System.out.println("After: " + method.getName());
    }
}

// Use the aspect
UserService proxy = Builder.proxy(UserServiceImpl.class)
    .addAspect(new LoggingAspect())
    .build();
```

### Example 2: Time Interval Aspect

```java
// Measure method execution time
UserService proxy = Builder.proxy(UserServiceImpl.class)
    .addAspect(new TimeIntervalAspect())
    .build();

proxy.getUserName(1L);
// Output: Method [getUserName] executed in [5] ms
```

### Example 3: Multiple Aspects

```java
// Chain multiple aspects
UserService proxy = Builder.proxy(UserServiceImpl.class)
    .addAspect(new LoggingAspect())
    .addAspect(new TimeIntervalAspect())
    .addAspect(new SecurityAspect())
    .build();
```

### Example 4: Custom Aspect with Full Control

```java
public class TransactionAspect implements Aspect {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Object[] args = invocation.getArgs();

        // Begin transaction
        System.out.println("Begin transaction");

        try {
            // Execute the actual method
            Object result = invocation.proceed();

            // Commit transaction
            System.out.println("Commit transaction");

            return result;
        } catch (Exception e) {
            // Rollback transaction
            System.out.println("Rollback transaction");
            throw e;
        }
    }
}

// Apply transaction aspect
UserService proxy = Builder.proxy(UserServiceImpl.class)
    .addAspect(new TransactionAspect())
    .build();
```

### Example 5: Performance Monitoring

```java
public class PerformanceAspect extends SimpleAspect {
    @Override
    public void after(Object target, Method method, Object[] args, Object result) {
        long endTime = System.currentTimeMillis();
        long startTime = (long) ThreadLocal.get("startTime");
        long duration = endTime - startTime;

        if (duration > 1000) {
            System.err.println("SLOW METHOD: " + method.getName() + " took " + duration + "ms");
        }
    }

    @Override
    public void before(Object target, Method method, Object[] args) {
        ThreadLocal.put("startTime", System.currentTimeMillis());
    }
}
```

### Example 6: Security Check

```java
public class SecurityAspect extends SimpleAspect {
    @Override
    public void before(Object target, Method method, Object[] args) {
        // Check if user has permission
        String currentUser = SecurityContextHolder.getCurrentUser();

        if (!hasPermission(currentUser, method)) {
            throw new AccessDeniedException("User " + currentUser + " has no permission");
        }
    }

    private boolean hasPermission(String user, Method method) {
        // Permission check logic
        return true;
    }
}
```

### Example 7: Proxy Factory Pattern

```java
// Using ProxyFactory for more control
ProxyFactory factory = new ProxyFactory();

factory.setTarget(new UserServiceImpl());
factory.setInterfaces(UserService.class);
factory.addAspect(new LoggingAspect());
factory.addAspect(new TimeIntervalAspect());

UserService proxy = factory.getProxy();
```

-----

## 🔧 Built-in Aspects

### SimpleAspect

A no-op aspect that you can extend to override only the methods you need:

```java
public abstract class SimpleAspect implements Aspect {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        before(invocation.getTarget(), invocation.getMethod(), invocation.getArgs());

        Object result;
        try {
            result = invocation.proceed();
            after(invocation.getTarget(), invocation.getMethod(),
                  invocation.getArgs(), result);
            return result;
        } catch (Exception e) {
            onError(invocation.getTarget(), invocation.getMethod(),
                    invocation.getArgs(), e);
            throw e;
        }
    }

    protected void before(Object target, Method method, Object[] args) {
        // Override me
    }

    protected void after(Object target, Method method, Object[] args, Object result) {
        // Override me
    }

    protected void onError(Object target, Method method, Object[] args, Throwable e) {
        // Override me
    }
}
```

### TimeIntervalAspect

Measures and logs method execution time:

```java
UserService proxy = Builder.proxy(UserServiceImpl.class)
    .addAspect(new TimeIntervalAspect())
    .build();
```

Output:
```
Method [getUserName] executed in [5] ms
```

-----

## 💡 Advanced Usage

### Method Filtering

```java
// Only intercept specific methods
UserService proxy = Builder.proxy(UserServiceImpl.class)
    .addAspect(new LoggingAspect())
    .filter(method -> method.getName().startsWith("get"))
    .build();
```

### Conditional Interception

```java
public class ConditionalAspect extends SimpleAspect {
    @Override
    protected void before(Object target, Method method, Object[] args) {
        if (shouldIntercept(method)) {
            doIntercept(method);
        }
    }

    private boolean shouldIntercept(Method method) {
        // Your condition logic
        return method.isAnnotationPresent(Auditable.class);
    }
}
```

### Accessing Target Object

```java
public class TargetAwareAspect extends SimpleAspect {
    @Override
    protected void before(Object target, Method method, Object[] args) {
        // Access the target object
        if (target instanceof UserServiceImpl) {
            UserServiceImpl impl = (UserServiceImpl) target;
            // Do something with the target
        }
    }
}
```

### Modifying Arguments

```java
public class ArgumentModifierAspect implements Aspect {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();

        // Modify arguments before execution
        if (args.length > 0 && args[0] instanceof String) {
            args[0] = ((String) args[0]).trim();
        }

        return invocation.proceed();
    }
}
```

### Modifying Return Value

```java
public class ReturnValueAspect implements Aspect {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object result = invocation.proceed();

        // Modify return value
        if (result instanceof String) {
            result = ((String) result).toUpperCase();
        }

        return result;
    }
}
```

-----

## 🔍 How It Works

### Dynamic Proxy Creation Principle

When creating a dynamic proxy object named `$Proxy0`:

1. **Generate Class**: Dynamically generate a class that implements the specified interfaces based on the passed `interfaces`
2. **Load Class**: Use the passed `classloader` to load the generated class into the JVM (i.e., load `$Proxy0` class)
3. **Create Instance**: Call the `$Proxy0(InvocationHandler)` constructor to create the `$Proxy0` object
4. **Implement Methods**: Iterate through all interface methods and generate implementations. These implementations essentially invoke the target object's methods via reflection
5. **Return Proxy**: Return the `$Proxy0` instance to the client
6. **Method Invocation**: When calling methods on the proxy class, it's equivalent to calling `InvocationHandler.invoke(Object, Method, Object[])`

### Invocation Flow

```
Client Code
    |
    v
Proxy Object ($Proxy0)
    |
    v
InvocationHandler.invoke()
    |
    v
Aspect.before() [if configured]
    |
    v
Target Method (via reflection)
    |
    v
Aspect.after() [if configured]
    |
    v
Return to Client
```

-----

## 💡 Best Practices

### 1. Use Specific Interceptors

```java
// ✅ Recommended: Create specific interceptors for specific concerns
public class LoggingInterceptor implements Interceptor {
    // Logging logic only
}

public class TransactionInterceptor implements Interceptor {
    // Transaction logic only
}

// ❌ Not Recommended: Mix multiple concerns in one aspect
public class MegaAspect implements Aspect {
    // Logging + Transaction + Security + ... (too much)
}
```

### 2. Keep Interceptors Stateless

```java
// ✅ Recommended: Stateless interceptor
public class LoggingInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // No instance variables
        log.info("Calling: " + invocation.getMethod().getName());
        return invocation.proceed();
    }
}

// ❌ Not Recommended: Stateful interceptor (thread-unsafe)
public class StatefulInterceptor implements Interceptor {
    private Method currentMethod;  // Not thread-safe!

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        this.currentMethod = invocation.getMethod();
        return invocation.proceed();
    }
}
```

### 3. Order Matters

```java
// Aspects are executed in the order they are added
UserService proxy = Builder.proxy(UserServiceImpl.class)
    .addAspect(new TransactionAspect())      // Outermost
    .addAspect(new SecurityAspect())         // Middle
    .addAspect(new LoggingAspect())          // Innermost
    .build();

// Execution order:
// 1. TransactionAspect.before()
// 2. SecurityAspect.before()
// 3. LoggingAspect.before()
// 4. Target method
// 5. LoggingAspect.after()
// 6. SecurityAspect.after()
// 7. TransactionAspect.after()
```

### 4. Handle Exceptions Properly

```java
public class ExceptionHandlingAspect extends SimpleAspect {
    @Override
    protected void onError(Object target, Method method, Object[] args, Throwable e) {
        // Log exception
        logger.error("Error in " + method.getName(), e);

        // Don't swallow the exception unless necessary
        // Consider wrapping it in a more specific exception
        if (e instanceof SQLException) {
            throw new DataAccessException("Database error", e);
        }
    }
}
```

### 5. Use Method Annotations

```java
// Define custom annotation
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cacheable {
    int ttl() default 300;
}

// Check annotation in aspect
public class CacheAspect extends SimpleAspect {
    @Override
    protected Object intercept(Invocation invocation) throws Throwable {
        Method method = invocation.getMethod();

        if (method.isAnnotationPresent(Cacheable.class)) {
            Cacheable cacheable = method.getAnnotation(Cacheable.class);
            // Cache logic
        }

        return invocation.proceed();
    }
}
```

-----

## ❓ Frequently Asked Questions

### Q1: Can I proxy classes instead of interfaces?

No, JDK dynamic proxy only works with interfaces. For class proxying, consider using CGLIB or Byte Buddy.

### Q2: How do I handle proxy performance?

Dynamic proxy has minimal overhead (typically < 1ms per call). If you need maximum performance:

```java
// Cache proxy instances
private static final UserService PROXY_CACHE =
    Builder.proxy(UserServiceImpl.class)
        .addAspect(new LoggingAspect())
        .build();
```

### Q3: Can I use this with Spring?

Yes! Bus Proxy can work alongside Spring AOP:

```java
@Configuration
public class ProxyConfig {
    @Bean
    public UserService userService() {
        UserServiceImpl impl = new UserServiceImpl();
        return Builder.proxy(impl)
            .addAspect(new LoggingAspect())
            .build();
    }
}
```

### Q4: How do I debug proxy issues?

Enable debug logging:

```java
public class DebugAspect extends SimpleAspect {
    @Override
    protected void before(Object target, Method method, Object[] args) {
        System.out.println("Target: " + target.getClass().getName());
        System.out.println("Method: " + method.getName());
        System.out.println("Args: " + Arrays.toString(args));
    }
}
```

-----

## 🔄 Version Compatibility

| Bus Proxy Version | JDK Version | Spring Version | Status |
| :--- | :--- | :--- | :--- |
| **8.x** | 17+ | 6.x (optional) | Current |
| 7.x | 11+ | 5.x (optional) | Maintenance |

-----

## 🔗 Related Modules

* **[bus-core](../bus-core)**: Core utilities and reflection helpers
* **[bus-aop](../bus-aop)**: Advanced AOP support
* **[bus-starter](../bus-starter)**: Spring Boot integration

-----

## 📚 Additional Resources

* [GitHub Repository](https://github.com/818000/bus)
* [Java Dynamic Proxy Tutorial](https://docs.oracle.com/javase/8/docs/technotes/guides/reflection/proxy.html)
