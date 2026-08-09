# bus-logger

`bus-logger` 是 Bus 的日志框架无关门面。它负责发现可用日志 Provider、保留 Provider 侧的消息格式化，并在
日志写出前提供一条精简的事件处理链。敏感数据判定不属于本组件，该策略由 `bus-sensitive` 负责。

## 依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-logger</artifactId>
    <version>${revision}</version>
</dependency>
```

## 核心模型

| 类型       | 职责                                                     |
|------------|----------------------------------------------------------|
| `Logger`   | 提供调用者识别、普通日志及对齐日志的静态门面。           |
| `Provider` | 定义各日志后端共同实现的日志操作。                       |
| `Factory`  | 创建并缓存指定后端的 Provider。                          |
| `Registry` | 选择当前 Factory，并按名称或类解析 Provider。            |
| `Level`    | 统一日志级别模型。                                       |
| `Loggable` | 保存级别、异常、格式串和防御性复制参数的不可变事件快照。 |
| `Operator` | 在不写日志的前提下处理事件或单个具名诊断值。             |
| `Executor` | 按注册顺序执行 Operator，再将事件交给 Provider。         |

事件链路如下：

```text
Logger -> Loggable -> Executor -> Provider -> 日志后端
```

Operator 发生异常时不会导致日志系统不可用。`Executor` 会使用固定的处理失败消息替换当前事件，并在不暴露 被拒绝参数的情况下继续交给
Provider。

## 支持的 Provider

- SLF4J
- Log4j 2
- Apache Commons Logging
- JBoss Logging
- `java.util.logging`
- tinylog
- 普通或彩色控制台回退实现

Factory 通过 `META-INF/services/org.miaixz.bus.logger.Factory` 发现。扩展日志后端时，需要实现 `Factory` 和
`Provider`，并在该 Service 文件中声明 Factory。

## 使用方式

```java
import org.miaixz.bus.logger.Logger;

Logger.info("订单已接收：orderId={}", orderId);
Logger.warn("请求正在重试：attempt={}", attempt);
Logger.error(failure, "订单处理失败：orderId={}", orderId);
```

需要重复进行级别判断或使用固定日志器名称时，可以显式获取 Provider：

```java
import org.miaixz.bus.logger.Provider;
import org.miaixz.bus.logger.Registry;

private static final Provider LOG = Registry.get(OrderService.class);

if (LOG.isDebugEnabled()) {
    LOG.debug("已加载 {} 个订单项", items.size());
}
```

## 事件处理扩展

只有确实需要在所有日志后端之前执行横切转换时，才注册 `Operator`：

```java
import org.miaixz.bus.logger.Executor;
import org.miaixz.bus.logger.Loggable;
import org.miaixz.bus.logger.Operator;

Operator operator = event -> new Loggable(
        event.level(),
        event.throwable(),
        "[tenant] " + event.format(),
        event.arguments());

Executor.register(operator);
try {
    Logger.info("订单已接收：orderId={}", orderId);
} finally {
    Executor.unregister(operator);
}
```

注册按照对象身份识别并进行引用计数。同一个 Operator 实例可以由多个 ApplicationContext 注册，只有完成 相同次数的注销后才会从处理链移除。

### 日志敏感数据处理

`bus-logger` 保持内容中立，不包含密码、令牌、Cookie 或 Authorization Header 等判定规则。
`bus-sensitive` 提供 `Sanitizer`，`bus-starter` 会在 Sensitive 功能启用后，为每个 ApplicationContext 将一个 Sanitizer 绑定到
`Executor`。使用 Starter 的应用应配置 `bus.sensitive.enabled=true`，不应手工注册全局 Sanitizer。

需要脱敏时应使用 `password={}` 这类具名占位符或结构化 Map。没有字段名的纯位置参数无法被安全分类。

## Native Image

Reachability Metadata 使用精确成员覆盖动态 Provider 发现和运行时门面兼容。禁止使用
`allDeclaredConstructors`、`allDeclaredMethods`、`allDeclaredFields` 等宽泛反射授权。

## 验证边界

Bus 不承载也不运行测试。集成、Provider、Metadata、AOT 和 Native Image 测试均位于相邻的 Abarth 仓库。 Bus 构建必须显式跳过测试。
