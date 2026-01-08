# 🔍 Bus Validate：轻量级 Java 验证框架

<p align="center">
<strong>强大、灵活且易于使用的验证框架</strong>
</p>

-----

## 📖 项目简介

**Bus Validate** 是一个轻量级、高性能的 Java 验证框架，旨在简化企业应用程序中的数据验证。它提供丰富的内置验证器，同时支持通过基于注解的配置自定义验证逻辑。

**核心特性**：
- **基于注解**：使用 Java 注解进行声明式验证
- **可扩展**：易于创建自定义验证器
- **零依赖**：轻量级设计，最小依赖
- **类型安全**：编译时验证支持
- **嵌套验证**：支持验证复杂对象图
- **分组验证**：使用验证组验证不同场景
- **灵活的错误处理**：可自定义错误消息和代码

-----

## ✨ 核心功能

### 🎯 验证能力

* **丰富的内置验证器**：20+ 开箱即用的验证注解
* **空安全处理**：整个框架中适当的空值处理
* **嵌套对象验证**：深度验证复杂对象结构
* **集合验证**：验证数组、集合和映射
* **条件验证**：基于不同场景的基于分组的验证
* **自定义验证器**：轻松创建自己的验证逻辑
* **消息模板**：灵活的错误消息格式，支持占位符

### ⚡ 性能优化

| 功能 | 好处 | 描述 |
| :--- | :--- | :--- |
| **延迟求值** | 低开销 | 仅在显式调用时验证 |
| **反射缓存** | 快速启动 | 缓存反射元数据以提高性能 |
| **最小依赖** | 小占用 | 仅依赖 bus-logger 和 Lombok |
| **线程安全** | 并发使用 | 在多线程环境中安全使用 |

### 🛡️ 验证覆盖

* **字符串验证**：邮箱、电话、URL、IP 地址、正则表达式、长度、大小
* **数字验证**：范围、倍数、正/负
* **类型验证**：NotNull、NotEmpty、NotBlank
* **集合验证**：使用 @Each 进行大小、元素验证
* **自定义逻辑**：条件、比较、等于、In/NotIn
* **区域支持**：中文字符、公民身份证、手机号码

-----

## 🚀 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-validate</artifactId>
    <version>8.x.x</version>
</dependency>
```

### 基础验证

#### 1. 定义带有验证注解的 Bean

```java
import lombok.Data;
import org.miaixz.bus.validate.magic.annotation.*;

@Data
public class User {

    @NotNull(errmsg = "用户 ID 不能为空")
    private Long id;

    @NotBlank(errmsg = "用户名不能为空")
    @Size(min = 3, max = 20, errmsg = "用户名长度必须在 3 到 20 个字符之间")
    private String username;

    @Email(errmsg = "邮箱格式无效")
    private String email;

    @Mobile(errmsg = "手机号码无效")
    private String phone;

    @Chinese(errmsg = "姓名必须仅包含中文字符")
    private String realName;

    @Date(errmsg = "日期格式无效")
    private String birthday;

    @NotNull
    @Compare(field = "password", errmsg = "密码不匹配")
    private String confirmPassword;

    private String password;
}
```

#### 2. 执行验证

```java
import org.miaixz.bus.validate.Builder;
import org.miaixz.bus.validate.Collector;

public class UserService {

    public void createUser(User user) {
        // 执行验证
        Collector result = Builder.on(user).access();

        // 检查验证是否通过
        if (!result.isPass()) {
            // 处理验证错误
            result.getResult().forEach(error -> {
                Criterion criterion = error.getCriterion();
                System.err.println("验证失败：" + criterion.getErrmsg());
                System.err.println("字段：" + criterion.getField());
                System.err.println("值：" + criterion.getParam("value"));
            });
            throw new ValidationException("用户验证失败");
        }

        // 继续创建用户
        saveUser(user);
    }
}
```

-----

## 📝 内置验证器

### 空/空验证器

| 注解 | 描述 | 示例 |
| :--- | :--- | :--- |
| `@NotNull` | 验证值不为 null | `@NotNull(errmsg = "ID 不能为空")` |
| `@Null` | 验证值为 null | `@Null(errmsg = "必须为 null")` |
| `@NotEmpty` | 验证值不为空（数组、集合、映射） | `@NotEmpty(errmsg = "列表不能为空")` |
| `@NotBlank` | 验证字符串不为空（去除空格后长度 > 0） | `@NotBlank(errmsg = "名称不能为空")` |
| `@Blank` | 验证字符串为空 | `@Blank(errmsg = "必须为空")` |

### 字符串验证器

| 注解 | 描述 | 示例 |
| :--- | :--- | :--- |
| `@Email` | 验证邮箱格式 | `@Email(errmsg = "邮箱无效")` |
| `@Mobile` | 验证手机号码 | `@Mobile(errmsg = "手机无效")` |
| `@Phone` | 验证电话号码 | `@Phone(errmsg = "电话无效")` |
| `@Chinese` | 验证中文字符 | `@Chinese(errmsg = "必须为中文")` |
| `@English` | 验证英文字符 | `@English(errmsg = "必须为英文")` |
| `@URL` | 验证 URL 格式 | `@URL(errmsg = "URL 无效")` |
| `@IPAddress` | 验证 IP 地址 | `@IPAddress(errmsg = "IP 无效")` |
| `@CitizenId` | 验证中国公民身份证 | `@CitizenId(errmsg = "身份证无效")` |

### 大小/长度验证器

| 注解 | 描述 | 示例 |
| :--- | :--- | :--- |
| `@Size` | 验证数组、集合、映射或字符串长度的大小 | `@Size(min = 1, max = 10)` |
| `@Length` | 验证字符串长度 | `@Length(min = 5, max = 100)` |
| `@Date` | 验证日期格式 | `@Date(format = "yyyy-MM-dd")` |

### 数字验证器

| 注解 | 描述 | 示例 |
| :--- | :--- | :--- |
| `@IntRange` | 验证整数范围 | `@IntRange(min = 0, max = 100)` |
| `@Multiple` | 验证值是否为指定数字的倍数 | `@Multiple(value = 5)` |

### 比较验证器

| 注解 | 描述 | 示例 |
| :--- | :--- | :--- |
| `@Compare` | 比较两个字段 | `@Compare(field = "password")` |
| `@Equals` | 检查值是否等于指定值 | `@Equals(value = "ACTIVE")` |
| `@In` | 检查值是否在指定值中 | `@In(values = {"A", "B", "C"})` |
| `@NotIn` | 检查值是否不在指定值中 | `@NotIn(values = {"X", "Y", "Z"})` |
| `@InEnum` | 检查值是否为有效枚举值 | `@InEnum(StatusEnum.class)` |

### 布尔验证器

| 注解 | 描述 | 示例 |
| :--- | :--- | :--- |
| `@True` | 验证值为 true | `@True(errmsg = "必须为 true")` |
| `@False` | 验证值为 false | `@False(errmsg = "必须为 false")` |

### 特殊验证器

| 注解 | 描述 | 示例 |
| :--- | :--- | :--- |
| `@Regex` | 使用正则表达式验证 | `@Regex(value = "^[A-Z]\\d{5}$")` |
| `@Each` | 验证集合中的每个元素 | `@Each @NotNull` |
| `@Valid` | 启用嵌套验证 | `@Valid(inside = true)` |
| `@Group` | 指定验证组 | `@Group({"create", "update"})` |

-----

## 🔧 高级用法

### 嵌套对象验证

```java
@Data
public class Order {

    @NotNull
    private Long id;

    @Valid
    @NotNull
    private User user;

    @Valid
    @NotEmpty
    private List<OrderItem> items;
}

@Data
public class OrderItem {

    @NotNull
    private Long productId;

    @IntRange(min = 1, max = 100)
    private Integer quantity;

    @NotNull
    private BigDecimal price;
}

// 验证
Collector result = Builder.on(order).access();
```

### 验证组

```java
@Data
public class Product {

    @NotNull(group = {"create", "update"})
    private Long id;

    @NotBlank(group = "create")
    @Size(min = 3, max = 50, group = {"create", "update"})
    private String name;

    @Null(group = "create")
    @NotNull(group = "update")
    private LocalDateTime updateTime;
}

// 使用特定组验证
Context context = Context.newInstance();
context.addGroups("create");

Collector result = Builder.on(product, context).access();
```

### 自定义验证器

#### 步骤 1：创建验证器类

```java
import org.miaixz.bus.validate.magic.Matcher;

public class AgeValidator implements Matcher {

    @Override
    public boolean on(Object value, Context context) {
        if (value == null) {
            return true; // null 值被视为有效
        }

        int age = (Integer) value;
        return age >= 18 && age <= 120;
    }
}
```

#### 步骤 2：创建注解

```java
import org.miaixz.bus.validate.Builder;
import org.miaixz.bus.validate.magic.annotation.Complex;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
@Complex(value = "AgeValidator", clazz = AgeValidator.class)
public @interface ValidAge {

    String errcode() default Builder.DEFAULT_ERRCODE;
    String errmsg() default "年龄必须在 18 到 120 岁之间";
    String[] group() default {};
    String field() default Builder.DEFAULT_FIELD;
}
```

#### 步骤 3：使用自定义验证器

```java
public class Person {

    @ValidAge
    private Integer age;
}
```

### 集合元素验证

```java
public class BatchRequest {

    @Each @NotNull
    @Each @Size(min = 36, max = 36)
    private List<String> userIds;

    @Valid
    private List<User> users;
}
```

### 条件验证

```java
@Data
public class RegistrationForm {

    private String userType; // "individual" 或 "company"

    @NotBlank
    private String username;

    // 仅在 userType 为 "company" 时验证
    @NotBlank(errmsg = "公司名称必填")
    @Reflect(condition = "userType == 'company'")
    private String companyName;
}
```

-----

## 💡 最佳实践

### 1. 使用有意义的错误消息

```java
// ✅ 推荐：清晰且具体
@NotBlank(errmsg = "邮箱地址必填")
@Email(errmsg = "邮箱地址必须为有效格式（例如：user@example.com）")
private String email;

// ❌ 不推荐：模糊且无帮助
@NotBlank(errmsg = "无效")
private String email;
```

### 2. 组合多个验证器

```java
// ✅ 推荐：全面验证
@NotBlank
@Size(min = 8, max = 20)
@Regex(value = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]+$",
        errmsg = "密码必须包含字母和数字")
private String password;
```

### 3. 对不同场景使用验证组

```java
public class User {

    @NotNull(group = {"create"})
    @Null(group = {"update"})
    private Long id;

    @NotBlank(group = {"create", "update"})
    private String username;
}
```

### 4. 适当处理空值

```java
// 大多数验证器默认跳过空值
// 显式使用 @NotNull 当空值无效时
@NotNull
@Size(min = 1, max = 100)
private String name;

// 对于可选字段，避免使用 @NotNull
@Email  // 允许空，但如果非空则验证
private String optionalEmail;
```

### 5. 复杂对象的嵌套验证

```java
// 始终对嵌套对象使用 @Valid
@Valid
@NotNull
private Address address;

@Valid
@NotEmpty
private List<Phone> phones;
```

### 6. 使用占位符的自定义错误消息

```java
// 在错误消息中使用占位符
@Size(min = 3, max = 20, errmsg = "用户名长度必须在 ${min} 到 ${max} 个字符之间")
private String username;

// 可用占位符：${field}、${value}、${min}、${max} 等
```

-----

## ❓ 常见问题

### Q1: 如何优雅地处理验证失败？

```java
Collector result = Builder.on(user).access();
if (!result.isPass()) {
    Map<String, String> errors = new HashMap<>();
    result.getResult().forEach(error -> {
        Criterion c = error.getCriterion();
        errors.put(c.getField(), c.getErrmsg());
    });
    return ResponseEntity.badRequest().body(errors);
}
```

### Q2: 可以在没有注解的情况下验证吗？

```java
// 是的，使用编程式验证
Context context = Context.newInstance();
Collector result = Builder.on(value, new Annotation[]{}, context).access();
```

### Q3: 如何国际化自定义验证消息？

```java
// 使用消息代码并通过 MessageSource 解析
@Email(errcode = "validation.email.invalid")
private String email;

// 在服务中
String message = messageSource.getMessage(
    criterion.getErrcode(),
    new Object[]{criterion.getField()},
    LocaleContextHolder.getLocale()
);
```

### Q4: 如何在验证期间跳过某些字段？

```java
@Valid(skip = {"internalField", "tempField"})
private MyClass object;
```

### Q5: 可以验证方法参数吗？

```java
public void updateUser(@Valid User user) {
    // 验证将自动触发
}
```

### Q6: 如何优化大集合的性能？

```java
// 使用 @Each 进行元素级验证
@Each @NotNull
@Each @Size(min = 1, max = 100)
private List<String> items;

// 对于非常大的集合，考虑批量验证
List<List<String>> batches = Lists.partition(items, 1000);
for (List<String> batch : batches) {
    Builder.on(batch).access();
}
```

### Q7: 如何禁用嵌套验证？

```java
@Valid(inside = false)
private User user;  // 仅验证用户本身，不验证嵌套字段
```

### Q8: 可以使用多个验证组吗？

```java
@Data
public class Product {

    @NotBlank(group = {"create", "update"})
    private String name;

    @Null(group = "create")
    @NotNull(group = "update")
    private Long id;
}

// 验证多个组
Context context = Context.newInstance();
context.addGroups("create", "update");
Builder.on(product, context).access();
```

-----

## 🔄 版本兼容性

| Bus Validate 版本 | JDK 版本 | 说明 |
| :--- | :--- | :--- |
| 8.x | 17+ | 需要 Java 17 或更高版本 |
| 7.x | 11+ | 支持 Java 11 及更高版本 |

-----

## 📊 验证器参考表

### 完整注解列表

| 注解 | 目标类型 | 空值处理 | 描述 |
| :--- | :--- | :--- | :--- |
| `@NotNull` | 所有 | ❌ 无效 | 值不能为 null |
| `@Null` | 所有 | ✅ 有效 | 值必须为 null |
| `@NotEmpty` | 数组、集合、映射、字符串 | ❌ 无效 | 值不能为空 |
| `@NotBlank` | CharSequence | ❌ 无效 | 去除空格后的长度必须 > 0 |
| `@Blank` | CharSequence | ✅ 有效 | 去除空格后的长度必须为 0 |
| `@Email` | CharSequence | ✅ 有效 | 必须为有效邮箱格式 |
| `@Mobile` | CharSequence | ✅ 有效 | 必须为有效手机号码 |
| `@Phone` | CharSequence | ✅ 有效 | 必须为有效电话号码 |
| `@Chinese` | CharSequence | ✅ 有效 | 必须包含中文字符 |
| `@English` | CharSequence | ✅ 有效 | 必须包含英文字符 |
| `@IPAddress` | CharSequence | ✅ 有效 | 必须为有效 IP 地址 |
| `@CitizenId` | CharSequence | ✅ 有效 | 必须为有效公民身份证 |
| `@Size` | 数组、集合、映射、CharSequence | ✅ 有效 | 大小/长度必须在范围内 |
| `@Length` | CharSequence | ✅ 有效 | 长度必须在范围内 |
| `@Date` | CharSequence | ✅ 有效 | 必须匹配日期格式 |
| `@IntRange` | 数字、CharSequence | ✅ 有效 | 必须在整数范围内 |
| `@Multiple` | 数字、CharSequence | ✅ 有效 | 必须为值的倍数 |
| `@True` | 布尔 | ✅ 有效 | 必须为 true |
| `@False` | 布尔 | ✅ 有效 | 必须为 false |
| `@Compare` | 所有 | ✅ 有效 | 必须与其他字段比较 |
| `@Equals` | 所有 | ✅ 有效 | 必须等于指定值 |
| `@In` | 所有 | ✅ 有效 | 必须在指定值中 |
| `@NotIn` | 所有 | ✅ 有效 | 必须不在指定值中 |
| `@InEnum` | CharSequence | ✅ 有效 | 必须为有效枚举值 |
| `@Regex` | CharSequence | ✅ 有效 | 必须匹配正则表达式模式 |
| `@Each` | 数组、集合 | - | 验证每个元素 |
| `@Valid` | 所有 | - | 启用嵌套验证 |

-----

## 🎯 常见用例

### 表单验证

```java
@Data
public class LoginForm {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, max = 32)
    private String password;

    @True(errmsg = "您必须接受条款和条件")
    private Boolean acceptTerms;
}
```

### API 请求验证

```java
@Data
public class CreateOrderRequest {

    @NotNull
    private Long userId;

    @Valid
    @NotEmpty
    private List<OrderItem> items;

    @NotNull
    @Compare(field = "totalAmount", errmsg = "支付金额必须与总计匹配")
    private BigDecimal paymentAmount;

    private BigDecimal totalAmount;
}
```

### 数据导入验证

```java
@Data
public class UserDataImport {

    @Each @NotNull
    @Each @CitizenId
    private List<String> citizenIds;

    @Each @Date(format = "yyyy-MM-dd")
    private List<String> birthDates;

    @Valid
    private List<User> users;
}
```

-----

## 🔗 链接

- **项目主页**：[https://github.com/818000/bus](https://github.com/818000/bus)
- **问题**：[https://github.com/818000/bus/issues](https://github.com/818000/bus/issues)
- **Bus 框架模块**：
  - [bus-core](https://github.com/818000/bus/tree/main/bus-core) - 核心工具
  - [bus-mapper](https://github.com/818000/bus/tree/main/bus-mapper) - MyBatis 增强
  - [bus-logger](https://github.com/818000/bus/tree/main/bus-logger) - 日志框架
  - [bus-crypto](https://github.com/818000/bus/tree/main/bus-crypto) - 加密
  - [bus-extra](https://github.com/818000/bus/tree/main/bus-extra) - 额外工具
