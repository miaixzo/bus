# Bus 公共错误组件

`bus-errors` 提供跨组件使用的错误契约，并通过 `bus-logger` 输出错误诊断。

| 类型 | 职责 |
|---|---|
| `ErrorType` | 稳定的语义标识。适用时复用公共类型，组件专属类型可使用 `cache.backend-timeout` 一类带命名空间的标识。 |
| `ErrorDescriptor` | 不可变的失败描述，包含类型、可选的旧业务码、公开消息键和经过防御性复制的公开参数。 |
| `BusException` | 在异常链中传递错误描述和诊断原因的非受检异常。 |

通过 Bus BOM 使用 `org.miaixz:bus-errors`。JPMS 模块名为 `bus.errors`，导出包为 `org.miaixz.bus.errors`。

```java
var rejection = new ErrorDescriptor(ErrorType.RATE_LIMITED, "limits.rate");

var failure = new ErrorDescriptor(ErrorType.DEPENDENCY_UNAVAILABLE, "storage.unavailable");
throw new BusException(failure, cause);
```

预期拒绝可直接返回描述。需要沿异常链传播时使用 `BusException`。接收方根据应用或协议契约决定状态、响应头和响应体，并解析公开消息键。`publicParameters` 只接收确认可以公开的值。异常消息是消息键，原始原因用于诊断。JVM 上的 Java 对象序列化可保留描述及可序列化的诊断信息，跨进程响应由各协议明确定义。

现有 Bus 异常、错误码和响应封装继续可用。改造顺序见[实施方案](../docs/bus-errors-architecture_CN.md)和[组件迁移与验收矩阵](../docs/bus-errors-component-migration_CN.md)。
