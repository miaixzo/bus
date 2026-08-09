# 🎯 Bus Proxy: 动态代理和 AOP 框架

<p align="center">
<strong>让动态代理变得简单 - JDK 动态代理和 AOP 支持</strong>
</p>

-----

## 📖 项目介绍

**Bus Proxy** 提供了一种简单而强大的方法来处理 Java 中的动态代理。它封装了 JDK 动态代理功能，使实现 AOP (面向切面编程)
模式变得简单，无需 IoC 容器。

代理设计模式允许您"为另一个对象提供代理或占位符以控制对它的访问"。代理可以以多种方式使用:

* **延迟初始化**: 代理作为实际实现的替身，仅在绝对必要时实例化它
* **安全性**: 代理对象可以验证用户是否拥有执行方法所需的权限 (如 EJB)
* **日志记录**: 代理可以记录每个方法调用，提供有价值的调试信息
* **性能监控**: 代理可以将每个方法调用记录到性能监视器，允许系统管理员查看系统的哪些部分可能会变慢

-----

## ✨ 核心特性

* **JDK 动态代理封装**: 用于创建动态代理的简化 API
* **非 IoC AOP 支持**: 无需 Spring 或其他 IoC 容器的 AOP 功能
* **拦截器支持**: 链接多个拦截器以处理横切关注点
* **内置切面**: 常见场景的预构建切面
* **方法拦截**: 在执行前后拦截方法调用
* **灵活配置**: 易于配置和自定义代理行为

-----

## 🚀 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-proxy</artifactId>
    <version>8.x.x</version>
</dependency>
```

### 基本用法

#### 1. 创建简单代理

```java
// 定义接口
public interface UserService {
    String getUserName(Long userId);
    void updateUser(User user);
}

// 实现接口
public class UserServiceImpl implements UserService {
    @Override
    public String getUserName(Long userId) {
        return "John Doe";
    }

    @Override
    public void updateUser(User user) {
        // 更新逻辑
    }
}

// 创建带有日志记录的代理
UserService proxy = Builder.proxy(UserServiceImpl.class)
    .addAspect(new SimpleAspect() {
        @Override
        public void before(Object target, Method method, Object[] args) {
            System.out.println("调用: " + method.getName());
        }

        @Override
        public void after(Object target, Method method, Object[] args, Object result) {
            System.out.println("已调用: " + method.getName());
        }
    })
    .build();
```

-----

## 📝 使用示例

### 示例 1: 简单切面

```java
// 扩展 SimpleAspect 并覆盖所需方法
public class LoggingAspect extends SimpleAspect {
    @Override
    public void before(Object target, Method method, Object[] args) {
        System.out.println("前置: " + method.getName());
    }

    @Override
    public void after(Object target, Method method, Object[] args, Object result) {
        System.out.println("后置: " + method.getName());
    }
}

// 使用切面
UserService proxy = Builder.proxy(UserServiceImpl.class)
    .addAspect(new LoggingAspect())
    .build();
```

### 示例 2: 时间间隔切面

```java
// 测量方法执行时间
UserService proxy = Builder.proxy(UserServiceImpl.class)
    .addAspect(new TimeIntervalAspect())
    .build();

proxy.getUserName(1L);
// 输出: 方法 [getUserName] 执行时间 [5] 毫秒
```

### 示例 3: 多个切面

```java
// 链接多个切面
UserService proxy = Builder.proxy(UserServiceImpl.class)
    .addAspect(new LoggingAspect())
    .addAspect(new TimeIntervalAspect())
    .addAspect(new SecurityAspect())
    .build();
```

### 示例 4: 具有完全控制的自定义切面

```java
public class TransactionAspect implements Aspect {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Object[] args = invocation.getArgs();

        // 开始事务
        System.out.println("开始事务");

        try {
            // 执行实际方法
            Object result = invocation.proceed();

            // 提交事务
            System.out.println("提交事务");

            return result;
        } catch (Exception e) {
            // 回滚事务
            System.out.println("回滚事务");
            throw e;
        }
    }
}

// 应用事务切面
UserService proxy = Builder.proxy(UserServiceImpl.class)
    .addAspect(new TransactionAspect())
    .build();
```

### 示例 5: 性能监控

```java
public class PerformanceAspect extends SimpleAspect {
    @Override
    public void after(Object target, Method method, Object[] args, Object result) {
        long endTime = System.currentTimeMillis();
        long startTime = (long) ThreadLocal.get("startTime");
        long duration = endTime - startTime;

        if (duration > 1000) {
            System.err.println("慢方法: " + method.getName() + " 耗时 " + duration + "毫秒");
        }
    }

    @Override
    public void before(Object target, Method method, Object[] args) {
        ThreadLocal.put("startTime", System.currentTimeMillis());
    }
}
```

### 示例 6: 安全检查

```java
public class SecurityAspect extends SimpleAspect {
    @Override
    public void before(Object target, Method method, Object[] args) {
        // 检查用户是否拥有权限
        String currentUser = SecurityContextHolder.getCurrentUser();

        if (!hasPermission(currentUser, method)) {
            throw new AccessDeniedException("用户 " + currentUser + " 没有权限");
        }
    }

    private boolean hasPermission(String user, Method method) {
        // 权限检查逻辑
        return true;
    }
}
```

### 示例 7: 代理工厂模式

```java
// 使用 ProxyFactory 获得更多控制
ProxyFactory factory = new ProxyFactory();

factory.setTarget(new UserServiceImpl());
factory.setInterfaces(UserService.class);
factory.addAspect(new LoggingAspect());
factory.addAspect(new TimeIntervalAspect());

UserService proxy = factory.getProxy();
```

-----

## 🔧 内置切面

### SimpleAspect

一个空操作切面，您可以扩展它以仅覆盖需要的方法:

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
        // 覆盖我
    }

    protected void after(Object target, Method method, Object[] args, Object result) {
        // 覆盖我
    }

    protected void onError(Object target, Method method, Object[] args, Throwable e) {
        // 覆盖我
    }
}
```

### TimeIntervalAspect

测量并记录方法执行时间:

```java
UserService proxy = Builder.proxy(UserServiceImpl.class)
    .addAspect(new TimeIntervalAspect())
    .build();
```

输出:

```
方法 [getUserName] 执行时间 [5] 毫秒
```

-----

## 💡 高级用法

### 方法过滤

```java
// 仅拦截特定方法
UserService proxy = Builder.proxy(UserServiceImpl.class)
    .addAspect(new LoggingAspect())
    .filter(method -> method.getName().startsWith("get"))
    .build();
```

### 条件拦截

```java
public class ConditionalAspect extends SimpleAspect {
    @Override
    protected void before(Object target, Method method, Object[] args) {
        if (shouldIntercept(method)) {
            doIntercept(method);
        }
    }

    private boolean shouldIntercept(Method method) {
        // 您的条件逻辑
        return method.isAnnotationPresent(Auditable.class);
    }
}
```

### 访问目标对象

```java
public class TargetAwareAspect extends SimpleAspect {
    @Override
    protected void before(Object target, Method method, Object[] args) {
        // 访问目标对象
        if (target instanceof UserServiceImpl) {
            UserServiceImpl impl = (UserServiceImpl) target;
            // 对目标执行某些操作
        }
    }
}
```

### 修改参数

```java
public class ArgumentModifierAspect implements Aspect {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();

        // 在执行前修改参数
        if (args.length > 0 && args[0] instanceof String) {
            args[0] = ((String) args[0]).trim();
        }

        return invocation.proceed();
    }
}
```

### 修改返回值

```java
public class ReturnValueAspect implements Aspect {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object result = invocation.proceed();

        // 修改返回值
        if (result instanceof String) {
            result = ((String) result).toUpperCase();
        }

        return result;
    }
}
```

-----

## 🔍 工作原理

### 动态代理创建原理

创建名为 `$Proxy0` 的动态代理对象时:

1. **生成类**: 根据传递的 `interfaces` 动态生成一个实现指定接口的类
2. **加载类**: 使用传递的 `classloader` 将生成的类加载到 JVM 中 (即加载 `$Proxy0` 类)
3. **创建实例**: 调用 `$Proxy0(InvocationHandler)` 构造函数创建 `$Proxy0` 对象
4. **实现方法**: 遍历所有接口方法并生成实现。这些实现本质上通过反射调用目标对象的方法
5. **返回代理**: 将 `$Proxy0` 实例返回给客户端
6. **方法调用**: 在代理类上调用方法时，相当于调用 `InvocationHandler.invoke(Object, Method, Object[])`

### 调用流程

```
客户端代码
    |
    v
代理对象 ($Proxy0)
    |
    v
InvocationHandler.invoke()
    |
    v
Aspect.before() [如果已配置]
    |
    v
目标方法 (通过反射)
    |
    v
Aspect.after() [如果已配置]
    |
    v
返回客户端
```

-----

## 💡 最佳实践

### 1. 使用特定拦截器

```java
// ✅ 推荐: 为特定关注点创建特定拦截器
public class LoggingInterceptor implements Interceptor {
    // 仅日志记录逻辑
}

public class TransactionInterceptor implements Interceptor {
    // 仅事务逻辑
}

// ❌ 不推荐: 在一个切面中混合多个关注点
public class MegaAspect implements Aspect {
    // 日志记录 + 事务 + 安全 + ... (太多了)
}
```

### 2. 保持拦截器无状态

```java
// ✅ 推荐: 无状态拦截器
public class LoggingInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 无实例变量
        log.info("调用: " + invocation.getMethod().getName());
        return invocation.proceed();
    }
}

// ❌ 不推荐: 有状态拦截器(线程不安全)
public class StatefulInterceptor implements Interceptor {
    private Method currentMethod;  // 不线程安全!

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        this.currentMethod = invocation.getMethod();
        return invocation.proceed();
    }
}
```

### 3. 顺序很重要

```java
// 切面按添加顺序执行
UserService proxy = Builder.proxy(UserServiceImpl.class)
    .addAspect(new TransactionAspect())      // 最外层
    .addAspect(new SecurityAspect())         // 中间层
    .addAspect(new LoggingAspect())          // 最内层
    .build();

// 执行顺序:
// 1. TransactionAspect.before()
// 2. SecurityAspect.before()
// 3. LoggingAspect.before()
// 4. 目标方法
// 5. LoggingAspect.after()
// 6. SecurityAspect.after()
// 7. TransactionAspect.after()
```

### 4. 正确处理异常

```java
public class ExceptionHandlingAspect extends SimpleAspect {
    @Override
    protected void onError(Object target, Method method, Object[] args, Throwable e) {
        // 记录异常
        logger.error("方法 " + method.getName() + " 中的错误", e);

        // 除非必要，否则不要吞掉异常
        // 考虑将其包装在更具体的异常中
        if (e instanceof SQLException) {
            throw new DataAccessException("数据库错误", e);
        }
    }
}
```

### 5. 使用方法注解

```java
// 定义自定义注解
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cacheable {
    int ttl() default 300;
}

// 在切面中检查注解
public class CacheAspect extends SimpleAspect {
    @Override
    protected Object intercept(Invocation invocation) throws Throwable {
        Method method = invocation.getMethod();

        if (method.isAnnotationPresent(Cacheable.class)) {
            Cacheable cacheable = method.getAnnotation(Cacheable.class);
            // 缓存逻辑
        }

        return invocation.proceed();
    }
}
```

-----

## ❓ 常见问题

### Q1: 可以代理类而不是接口吗？

不可以，JDK 动态代理仅适用于接口。对于类代理，请考虑使用 CGLIB 或 Byte Buddy。

### Q2: 如何处理代理性能？

动态代理的开销最小 (通常每次调用 < 1 毫秒)。如果需要最大性能:

```java
// 缓存代理实例
private static final UserService PROXY_CACHE =
    Builder.proxy(UserServiceImpl.class)
        .addAspect(new LoggingAspect())
        .build();
```

### Q3: 可以与 Spring 一起使用吗？

可以！Bus Proxy 可以与 Spring AOP 一起工作:

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

### Q4: 如何调试代理问题？

启用调试日志记录:

```java
public class DebugAspect extends SimpleAspect {
    @Override
    protected void before(Object target, Method method, Object[] args) {
        System.out.println("目标: " + target.getClass().getName());
        System.out.println("方法: " + method.getName());
        System.out.println("参数: " + Arrays.toString(args));
    }
}
```

-----

## 🔄 版本兼容性

| Bus Proxy 版本 | JDK 版本 | Spring 版本 | 状态   |
|:---------------|:---------|:------------|:-------|
| **8.x**        | 17+      | 6.x (可选)  | 当前   |
| 7.x            | 11+      | 5.x (可选)  | 维护中 |

-----

## 🔗 相关模块

* **[bus-core](../bus-core)**: 核心工具和反射助手
* **[bus-aop](../bus-aop)**: 高级 AOP 支持
* **[bus-starter](../bus-starter)**: Spring Boot 集成

-----

## 📚 其他资源

* [GitHub 仓库](https://github.com/818000/bus)
* [Java 动态代理教程](https://docs.oracle.com/javase/8/docs/technotes/guides/reflection/proxy.html)
