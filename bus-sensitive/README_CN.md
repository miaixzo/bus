# bus-sensitive

`bus-sensitive` 负责 Bus 的敏感数据算法，包括基于注解的对象脱敏、可扩展的条件与策略 Provider、加解密处理
支持，以及结构化日志 `Sanitizer`。本组件不负责 Spring 激活，Spring 装配统一由 `bus-starter` 完成。

## 依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-sensitive</artifactId>
    <version>${revision}</version>
</dependency>
```

## 功能边界

| 范围 | 主要类型 | 职责 |
|---|---|---|
| 对象处理 | `Builder`、`Provider`、`Context` | 对对象和 JSON 应用脱敏或安全规则。 |
| 规则注解 | `@Sensitive`、`@Shield`、`@NShield`、`@Privacy` | 声明处理模式、方向、字段及脱敏策略。 |
| 扩展 SPI | `StrategyProvider`、`ConditionProvider` | 提供自定义脱敏算法和运行时条件。 |
| 内置策略 | `nimble.*Provider` | 处理姓名、电话、邮箱、证件、银行卡、地址和凭据等常见值。 |
| 结构化日志 | `Sanitizer` | 在日志 Provider 接收到参数前替换受保护的具名值。 |

## 对象脱敏

```java
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.sensitive.Builder;
import org.miaixz.bus.sensitive.magic.annotation.Shield;

public class Account {

    @Shield(type = EnumValue.Masking.CHINESE_NAME)
    private String name;

    @Shield(type = EnumValue.Masking.MOBILE_PHONE)
    private String mobile;

    @Shield(type = EnumValue.Masking.EMAIL)
    private String email;
}

Account masked = Builder.on(account, true);
String json = Builder.json(account);
```

`Builder.on(object)` 直接处理传入对象。调用方需要返回克隆结果而不是修改传入对象图时，使用
`Builder.on(object, true)`。`Builder.json(object)` 会处理并序列化结果。

`@Sensitive` 控制处理模式和方向：

- `Builder.SENS`：仅脱敏。
- `Builder.SAFE`：仅加密或解密。
- `Builder.ALL`：同时启用两种能力。
- `Builder.IN`：输入或写入方向。
- `Builder.OUT`：输出或读取方向。

`@Shield` 可以选择内置 `EnumValue.Masking` 类型或自定义 `StrategyProvider`。自定义
`ConditionProvider` 可以根据当前 `Context` 决定规则是否执行。

## 结构化日志脱敏

`Sanitizer` 实现 `bus-logger` 的 `Operator` 接口。它根据标准化字段名识别 Authorization Header、Cookie、
密码、Secret、Token、API Key、Private Key 和 Credential 等受保护值。

```java
import org.miaixz.bus.sensitive.Sanitizer;

Sanitizer sanitizer = new Sanitizer();

Object value = sanitizer.sanitize("accessToken", token);
// [REDACTED]
```

处理日志事件时，字段名来自 Provider 占位符之前的赋值表达式：

```java
Logger.info("登录失败：username={}, password={}", username, password);
```

用户名保持不变，密码参数在 Provider 格式化前被替换。Map、Iterable 和对象数组会被复制并在固定安全深度内
递归处理；Sanitizer 不会反射遍历任意业务对象，避免意外对象图扫描和副作用。

敏感数据策略不得放入 `bus-logger`。Logger 保持内容中立，分类和替换规则由本组件统一负责。

## Spring 集成

使用 `bus-starter` 时必须显式开启：

```yaml
bus:
  sensitive:
    enabled: true
    debug: false
```

`SensitiveConfiguration` 会为每个 ApplicationContext 创建一个 `Sanitizer` 和一个 `SensitiveBinding`。
Binding 将 Sanitizer 注册到日志 `Executor`，并在 Context 关闭时解除绑定。在 Servlet MVC 应用中，同一个
Configuration 还会提供 Request/Response Body Advice，不再存在独立的 `SensitiveWebConfiguration`。

加解密算法仅允许 `AES`、`DES` 和 `SM4`。Key 值必须由应用受保护的配置源提供，诊断 `toString()` 输出会对其
进行遮蔽。

## Native Image

静态 Metadata 只声明确实需要反射的精确构造函数和成员。禁止使用 `allDeclaredConstructors`、
`allDeclaredMethods`、`allDeclaredFields` 等宽泛授权。

## 验证边界

Bus 不承载也不运行测试。敏感算法、Spring 集成、生命周期、日志、AOT 和 Native Image 测试均位于相邻的
Abarth 仓库。Bus 构建必须显式跳过测试。
