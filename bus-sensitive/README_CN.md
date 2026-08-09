# bus-sensitive

`bus-sensitive` 是Bus数据保护引擎。它提供注释驱动的对象屏蔽、条件和自定义
策略、加密/解密处理、JSON 输出、深复制处理和结构化日志清理。这
模块与框架无关：它拥有保护算法，而 Spring 激活和 Web 建议属于
`bus-starter`。

## 模块职责

该模块提供两条相关但独立的保护路径：

```text
domain object -> Builder -> Provider -> Filter -> StrategyProvider -> protected object or JSON

log event -> Sanitizer -> bus-logger Operator -> protected arguments -> logging provider
```

它拥有：

- 字段和类型级保护注释；
- 内置屏蔽策略；
- 条件屏蔽和自定义策略 SPI；
- 遍历上下文和递归对象处理；
- 可选的克隆结果处理；
- 通过隐私注释进行加密/解密选择；
- 结构化日志键分类和递归值清理。

它不拥有：

- Spring配置属性或功能激活；
- Servlet 请求/响应建议；
- 记录后端选择；
- 加密密钥的存储；
- 应用程序授权决策。

## 依赖关系

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-sensitive</artifactId>
    <version>${revision}</version>
</dependency>
```

对于自动 Spring MVC 和日志记录集成，请与此模块一起使用 `bus-starter`。

## 核心模型

| 类型 | 责任 |
|---------------------|-----------------------------------------------------------------------|
| `Builder` | 静态入口点和处理模式/方向常量。           |
| `Provider<T>` | 遍历并处理对象，可以选择先克隆它。       |
| `Context` | 携带当前对象、字段、注释和处理状态。 |
| `Filter` | 在遍历期间选择并应用字段规则。                     |
| `Registry` | 保存内置屏蔽提供程序并解析注释策略。  |
| `StrategyProvider` | 用于掩蔽转换的 SPI。                                     |
| `ConditionProvider` | SPI 决定规则是否适用于当前上下文。           |
| `Sanitizer` | `bus-logger` 用于结构化日志值的运算符。                      |

## 保护注解

| 注释 | 范围 | 目的 |
|--------------|--------------------------|--------------------------------------------------------------------------------------------------|
| `@Sensitive` | 键入或处理条目 | 选择屏蔽/安全模式、方向、包含字段、跳过字段和嵌套遍历。 |
| `@Shield` | 字段 | 声明一个屏蔽规则及其可见前缀/后缀行为。                                |
| `@NShield` | 字段 | 对多个 `@Shield` 规则和可选包含/过滤表达式进行分组。                         |
| `@Privacy` | 字段 | 选择加密或解密等安全处理。                                    |
| `@Strategy` | 注释类型 | 将自定义注释与策略提供程序关联。                                         |
| `@Condition` | 注释类型 | 标记条件保护元数据。                                                           |
| `@Entry` | 注解类型 | 标记保护条目注解。                                                             |

### `@Sensitive` 选项

| 属性 | 默认 | 含义 |
|-----------|---------------|-------------------------------------------------------------|
| `value` | `Builder.ALL` | 处理能力：屏蔽、安全、两者或两者都不。 |
| `stage` | `Builder.ALL` | 输入或输出等处理方向。               |
| `field` | 空 | 显式字段包含列表。                              |
| `skip` | 空 | 从处理中排除的字段。                            |
| `inside` | `true` | 是否遍历嵌套值。                        |

处理常数为：

- `Builder.SENS`：仅屏蔽；
- `Builder.SAFE`：仅加密/解密；
- `Builder.ALL`：遮蔽和安全处理；
- `Builder.IN`：输入/写入方向；
- `Builder.OUT`：输出/读取方向；
- `Builder.NOTHING`：未处理；
- `Builder.OVERALL`：完整的遍历范围。

### `@Shield` 选项

| 属性 | 默认 | 含义 |
|-------------------|---------------------------|-----------------------------------------------------|
| `type` | `EnumValue.Masking.NONE` | 内置屏蔽策略。                          |
| `mode` | `EnumValue.Mode.MIDDLE` | 可见/屏蔽放置模式。                      |
| `shadow` | `*` | 替换字符或文本。                      |
| `fixedHeaderSize` | `0` | 固定可见前缀长度。                        |
| `fixedTailorSize` | `3` | 固定可见后缀长度。                        |
| `autoFixedPart` | `true` | 允许策略特定的自动可见长度。 |
| `condition` | `ConditionProvider.class` | 可选运行时条件。                         |
| `strategy` | `DafaultProvider.class` | 可选的自定义策略实施。            |
| `key` / `field` | 空 | 规则特定的查找元数据。                      |

## 快速开始

### 定义受保护的字段

```java
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.sensitive.magic.annotation.Shield;

public class Account {

    @Shield(type = EnumValue.Masking.NAME)
    private String name;

    @Shield(type = EnumValue.Masking.MOBILE)
    private String mobile;

    @Shield(type = EnumValue.Masking.EMAIL)
    private String email;

    @Shield(type = EnumValue.Masking.BANK_CARD)
    private String bankCard;

}
```

### 处理一个对象

```java
import org.miaixz.bus.sensitive.Builder;

Account account = loadAccount();

// Processes the supplied object graph.
Account masked = Builder.on(account);

// Clones first, then processes the cloned graph.
Account safeCopy = Builder.on(account, true);

// Processes and serializes the result.
String json = Builder.json(account);
```

当原始对象必须保持不变时，请使用克隆模式。对象的完整图仍然需要兼容
与模块的克隆和遍历规则。

### 应用显式处理注解

```java
Sensitive policy = AccountView.class.getAnnotation(Sensitive.class);
Account result = Builder.on(account, policy, true);
```

接受注释的重载适用于已经解决了有效策略的基础设施。
正常的域代码应该更喜欢受保护模型上的注释和更简单的入口点。

## 内置屏蔽策略

`Registry` 初始化这些 `EnumValue.Masking` 策略：

| 掩蔽值 | 提供商 | 典型数据 |
|---------------|---------------------|-----------------------------|
| `ADDRESS` | `AddressProvider` | 邮政地址。             |
| `BANK_CARD` | `BandCardProvider` | 银行卡号。           |
| `CITIZENID` | `CitizenIdProvider` | 公民身份号码。    |
| `CNAPS_CODE` | `CnapsProvider` | 银行路由代码。          |
| `DEFAUL` | `DafaultProvider` | 通用屏蔽。    |
| `EMAIL` | `EmailProvider` | 电子邮件地址。              |
| `MOBILE` | `MobileProvider` | 手机号码。              |
| `NAME` | `NameProvider` | 人名。                |
| `NONE` | `NoneProvider` | 无屏蔽。                 |
| `PASSWORD` | `PasswordProvider` | 密码或秘密文本。    |
| `PAY_SIGN_NO` | `CardProvider` | 支付签名标识符。 |
| `PHONE` | `PhoneProvider` | 电话号码。           |

确切的可见字符取决于所选策略和 `@Shield` 选项。不要假设每一个策略
使用相同的前缀和后缀长度。

## 自定义策略

当内置掩码类型无法表达规则时，实现 `StrategyProvider`：

```java
import org.miaixz.bus.sensitive.Context;
import org.miaixz.bus.sensitive.nimble.StrategyProvider;

public final class ContractCodeProvider implements StrategyProvider {

    @Override
    public Object build(Object value, Context context) {
        if (value == null) {
            return null;
        }
        String text = value.toString();
        return text.length() <= 4 ? "****" : text.substring(0, 2) + "****" + text.substring(text.length() - 2);
    }

}
```

从字段规则中引用它：

```java
@Shield(strategy = ContractCodeProvider.class)
private String contractCode;
```

`StrategyProvider` 实例必须是无状态或线程安全的。 `Context` 提供当前处理的字段和
价值;调用后不得保留它。

## 条件屏蔽

当屏蔽取决于活动处理上下文时，实现 `ConditionProvider`：

```java
public final class NonEmptyCondition implements ConditionProvider {

    @Override
    public boolean valid(Context context) {
        Object value = context.getCurrentFieldValue();
        return value != null && !value.toString().isBlank();
    }

}
```

```java
@Shield(type = EnumValue.Masking.EMAIL, condition = NonEmptyCondition.class)
private String email;
```

授权相关条件应使用调用者集成层提供的授权信息；
核心敏感模块不读取 Spring 或 Servlet 上下文。

## 策略注册

`Registry` 在类初始化期间安装内置提供程序。 `Registry.require(...)` 解决了内置或
与注释相关的提供程序，当请求的策略不可用时会失败。 `Registry.register(...)` 拒绝
重复的屏蔽标识符，因此特定于应用程序的行为通常应使用 `@Shield(strategy = ...)` 或
自定义注释标记为 `@Strategy`，而不是尝试全局替换内置提供程序。

## 结构化日志清理

`Sanitizer` 执行 `bus-logger` 的 `Operator` 合约。它规范化密钥并保护与常见相关的值
敏感名称，包括授权标头、cookie、密码、秘密、令牌、API 密钥、私钥和
证书。

```java
import org.miaixz.bus.sensitive.Sanitizer;

Sanitizer sanitizer = new Sanitizer();

Object protectedToken = sanitizer.sanitize("accessToken", token);
boolean protectedKey = sanitizer.isSensitive("Authorization");
```

受保护的标量值变为 `Sanitizer.REDACTED`，当前为 `[REDACTED]`。映射、迭代和对象数组是
递归地复制和清理到固定的安全深度。任意应用对象都不会反射过来，
防止意外的图形遍历、延迟加载和副作用。

对于完整的日志事件，键名称是从提供程序占位符之前的分配中推断出来的：

```java
Logger.warn("Login rejected: username={}, password={}", username, password);
```

这里 `username` 仍然可见，而 `password` 可以被替换。诸如 `"Login rejected: {}"` 之类的消息不会
提供可分类的密钥，因此不能保证清理。

不要将这些规则移至 `bus-logger` 中。日志立面保持内容中立；该模块拥有分类
和更换政策。

## Spring 集成

使用 `bus-starter`，显式启用该功能：

```yaml
bus:
  sensitive:
    enabled: true
    debug: false
```

集成包括：

```text
SensitiveConfiguration
  +-- Sanitizer
  +-- SensitiveBinding -> logger Executor lifecycle
  `-- ServletConfiguration (Servlet MVC only)
        +-- SensitiveRequestBodyAdvice
        `-- SensitiveResponseBodyAdvice
```

`SensitiveBinding` 为每个应用程序上下文注册一个清理程序，并在该上下文关闭时取消注册它。服务程序
请求/响应建议由嵌套配置提供；没有单独的
`SensitiveWebConfiguration`。

加密和解密配置仅限于当前 Starter 属性支持的算法。钥匙
材料必须来自受保护的外部配置源，并且绝不能提交到应用程序文件。

## 包结构

| 包装 | 内容 |
|---------------------------------------------|--------------------------------------------------------------------|
| `org.miaixz.bus.sensitive` | 处理入口点、遍历状态、注册表和清理程序。 |
| `org.miaixz.bus.sensitive.magic.annotation` | 保护注释和元注释。                       |
| `org.miaixz.bus.sensitive.nimble` | 策略合约和内置提供商。                         |

所有三个包均由 JPMS 模块导出。 Spring 特定的包是故意缺失的。

## 最佳实践

- 在稳定的输出或传输边界保护数据，而不是在整个业务逻辑中重复保护数据。
- 当调用者仍然需要未修改的对象时，使用克隆模式。
- 在实施自定义策略之前选择最窄的内置策略。
- 保持自定义策略和条件实现无状态。
- 避免在日志清理中反射任意对象。
- 对需要编辑的值使用命名日志字段。
- 将加密密钥保留在源代码控制和诊断输出之外。
- 将屏蔽视为表示保护，而不是授权或加密的替代品。

## 原生镜像

可达性元数据仅列出动态处理所需的确切构造函数和成员。条目按 A-Z 排序。
禁止诸如 `allDeclaredConstructors`、`allDeclaredMethods` 和 `allDeclaredFields` 之类的广泛反射补助金。

## 验证边界

Bus包含且不运行任何测试。屏蔽、克隆、自定义策略、Spring 生命周期、日志记录、元数据、AOT 和 Native
Image 测试在同级 Abarth 存储库中维护。Bus 构建必须明确跳过测试。
