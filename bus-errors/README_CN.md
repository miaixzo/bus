# Bus 公共错误组件

`bus-errors` 负责分类失败、控制 MVC 和 Vortex 的对外错误响应，并通过 `bus-logger` 输出结构化诊断。

| 类型 | 职责 |
|---|---|
| `ErrorType` | 稳定的语义标识。适用时复用公共类型，组件专属类型可使用 `cache.backend-timeout` 一类带命名空间的标识。 |
| `ErrorDescriptor` | 不可变的失败描述，包含类型、可选的旧业务码、公开消息键和经过防御性复制的公开参数。 |
| `ClassifiedException` | 在异常链中传递错误描述和诊断原因的非受检异常。 |

通过 Bus BOM 使用 `org.miaixz:bus-errors`。JPMS 模块 `bus.errors` 导出核心 API、策略、恢复、SPI 和传输适配包。

```java
var rejection = new ErrorDescriptor(ErrorType.RATE_LIMITED, "limits.rate");

var failure = new ErrorDescriptor(ErrorType.DEPENDENCY_UNAVAILABLE, "storage.unavailable");
throw new ClassifiedException(failure, cause);
```

预期拒绝可直接返回错误描述，需要传播的失败可使用 `ClassifiedException`。`publicParameters` 只携带确认可以公开的值。异常消息是消息键，原始原因用于诊断。

## 对外 HTTP 响应

`ErrorBoundary` 负责分类和记录结果。MVC、Servlet 和响应式适配器使用已确认的状态和路由信息、公开文案以及经过校验的响应头。协议渲染器保留旧 `Message`、JSON-RPC 错误和 SSE 错误帧等格式。上游 HTTP 错误经公开错误映射后再由网关输出。

Starter 的 `bus.errors.routing` 按监听入口、路径和已确认的数字状态码选择空响应体的原生 HTTP 响应。其他错误使用对应协议或兼容格式。响应已提交时，按协议发送安全错误帧或终结连接。连接器在应用分派前拒绝的请求，以及上游透明转发的任意成功数据，属于应用错误分类的边界之外。

## 错误诊断

`ErrorLogPolicy` 决定日志级别和可用字段，标准观察器委托 `bus-logger` 输出，日志提供者的阈值决定哪些记录实际写出。常见客户端错误通常使用 `DEBUG`，请求超限使用 `INFO`，网关过载使用 `WARN`，意外终态失败使用 `ERROR`。`TRACE` 提供有界的诊断详情。`bus.errors.logging.enabled` 控制 Starter 的标准错误日志订阅。详见 [Starter 配置](../bus-starter/README_CN.md#统一-http-错误响应)。
