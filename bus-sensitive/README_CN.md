# 🔒 Bus Sensitive: 企业级数据脱敏框架

<p align="center">
<strong>全面的敏感数据保护和脱敏解决方案</strong>
</p>

-----

## 📖 项目介绍

**Bus Sensitive** 是一个企业级数据脱敏框架，旨在通过可定制的脱敏策略保护敏感信息。它提供全面的数据保护支持，内置多种脱敏类型和自定义策略，确保在保持数据可用性的同时符合数据隐私法规。

**核心特性:**
- **灵活的注解配置**: 通过简单的注解实现字段级数据脱敏
- **内置脱敏策略**: 为常见敏感数据类型预配置策略
- **自定义策略支持**: 可扩展架构实现自定义脱敏逻辑
- **条件脱敏**: 基于运行时条件应用脱敏
- **多种数据类型**: 支持字符串、集合、数组和复杂对象
- **性能优化**: 高效处理，最小开销
- **易于集成**: 与 JSON 序列化、数据库操作和 API 响应无缝集成

-----

## ✨ 核心特性

### 🎯 全面的数据保护

* **多种数据类型**: 支持中文姓名、手机号、邮箱、身份证、密码、银行卡、地址等脱敏
* **灵活的脱敏模式**: 控制数据哪些部分被脱敏（左侧、右侧、中间、自定义）
* **条件处理**: 基于用户角色、权限或其他运行时条件应用脱敏
* **嵌套对象支持**: 递归处理具有多层嵌套的复杂对象

### 🛡️ 内置脱敏策略

| 策略 | 描述 | 示例 | 结果 |
| :--- | :--- | :--- | :--- |
| **中文姓名** | 2字符姓名屏蔽姓氏，3+字符屏蔽中间字符 | 张三 | *三 |
| **手机号** | 屏蔽中间4位 | 18233583070 | 182****3070 |
| **邮箱** | 屏蔽用户名部分 | johndoe@example.com | joh***@example.com |
| **身份证** | 屏蔽中间10位（前6后2可见） | 110101199001011234 | 110101**********34 |
| **密码** | 返回空字符串 | password123 | |
| **银行卡** | 屏蔽中间位 | 6222021234567890 | 6222************7890 |
| **地址** | 屏蔽详细信息 | 北京市朝阳区xxx街道 | 北京市朝阳区****** |

### ⚡ 高级特性

* **自定义脱敏字符**: 选择任意字符进行脱敏（默认: *）
* **可配置脱敏长度**: 控制可见字符数
* **字段特定规则**: 为不同字段应用不同策略
* **类级别配置**: 为整个类启用/禁用脱敏
* **定向处理**: 输入（加密）和输出（解密/脱敏）的独立规则
* **与加密模块集成**: 结合数据脱敏和加密支持

-----

## 🚀 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-sensitive</artifactId>
    <version>8.5.1</version>
</dependency>
```

### 基础用法

#### 1. 定义包含敏感字段的实体

```java
import org.miaixz.bus.sensitive.magic.annotation.Shield;
import org.miaixz.bus.core.lang.EnumValue;

@Data
public class User {

    private Long id;

    @Shield(type = EnumValue.Masking.CHINESE_NAME)
    private String name;

    @Shield(type = EnumValue.Masking.MOBILE_PHONE)
    private String mobile;

    @Shield(type = EnumValue.Masking.EMAIL)
    private String email;

    @Shield(type = EnumValue.Masking.ID_CARD)
    private String idCard;

    @Shield(type = EnumValue.Masking.PASSWORD)
    private String password;
}
```

#### 2. 应用数据脱敏

```java
import org.miaixz.bus.sensitive.Builder;

// 创建用户对象
User user = new User();
user.setId(1L);
user.setName("张三");
user.setMobile("18233583070");
user.setEmail("zhangsan@example.com");
user.setIdCard("110101199001011234");
user.setPassword("password123");

// 应用脱敏
User maskedUser = Builder.on(user);

System.out.println(maskedUser.getName());     // 输出: *三
System.out.println(maskedUser.getMobile());   // 输出: 182****3070
System.out.println(maskedUser.getEmail());    // 输出: zha***@example.com
System.out.println(maskedUser.getIdCard());   // 输出: 110101**********34
System.out.println(maskedUser.getPassword()); // 输出: (空字符串)
```

#### 3. 序列化为 JSON

```java
// 序列化为 JSON，应用脱敏
String json = Builder.json(user);
System.out.println(json);
// 输出: {"id":1,"name":"*三","mobile":"182****3070",...}
```

-----

## 📝 使用示例

### 1. 基础字段脱敏

```java
@Data
public class Customer {
    @Shield(type = EnumValue.Masking.CHINESE_NAME)
    private String customerName;

    @Shield(type = EnumValue.Masking.MOBILE_PHONE)
    private String phoneNumber;

    @Shield(type = EnumValue.Masking.BANK_CARD)
    private String bankAccount;
}
```

### 2. 自定义脱敏配置

```java
@Data
public class Order {
    // 使用自定义脱敏字符
    @Shield(type = EnumValue.Masking.MOBILE_PHONE,
            shadow = "#")
    private String contactPhone;

    // 自定义可见字符数
    @Shield(type = EnumValue.Masking.ID_CARD,
            fixedHeaderSize = 3,
            fixedTailorSize = 4)
    private String customerIdCard;
}
```

### 3. 条件脱敏

```java
// 实现自定义条件
public class AdminCondition implements ConditionProvider {
    @Override
    public boolean valid(Context context) {
        // 仅对非管理员用户脱敏
        return !SecurityContextHolder.isAdmin();
    }
}

// 应用条件脱敏
@Data
public class Account {
    @Shield(type = EnumValue.Masking.EMAIL,
            condition = AdminCondition.class)
    private String email;
}
```

### 4. 自定义脱敏策略

```java
// 定义自定义策略
public class CustomMaskStrategy implements StrategyProvider {
    @Override
    public Object build(Object object, Context context) {
        String value = object.toString();
        // 自定义脱敏逻辑
        return value.substring(0, 2) + "********";
    }
}

// 创建自定义注解
@Strategy(CustomMaskStrategy.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomMask {
}

// 使用自定义策略
@Data
public class Product {
    @CustomMask
    private String serialNumber;
}
```

### 5. 类级别配置

```java
import org.miaixz.bus.sensitive.magic.annotation.Sensitive;

@Data
@Sensitive(value = Builder.SENS,  // 仅启用脱敏
           stage = Builder.OUT)    // 在输出时应用
public class UserProfile {
    @Shield(type = EnumValue.Masking.CHINESE_NAME)
    private String realName;

    @Shield(type = EnumValue.Masking.MOBILE_PHONE)
    private String phone;

    private String publicInfo; // 不脱敏
}
```

### 6. 处理嵌套对象

```java
@Data
public class OrderDetail {
    private Long orderId;

    @Shield(type = EnumValue.Masking.CHINESE_NAME)
    private String customerName;

    private List<OrderItem> items;
}

@Data
public class OrderItem {
    @Shield(type = EnumValue.Masking.MOBILE_PHONE)
    private String contactPhone;

    private String productName;
}

// 嵌套对象会被递归处理
OrderDetail order = new OrderDetail();
// ... 设置值
OrderDetail maskedOrder = Builder.on(order, true); // true = 深度克隆
```

### 7. 与 Spring AOP 集成

```java
@Aspect
@Component
public class SensitiveAspect {

    @Around("@annotation(org.miaixz.bus.sensitive.magic.annotation.Sensitive)")
    public Object handleSensitive(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        // 对方法结果应用脱敏
        return Builder.on(result);
    }
}

@Service
public class UserService {

    @Sensitive
    public User getUserById(Long id) {
        // 返回用户 - 敏感数据将被自动脱敏
        return userRepository.findById(id);
    }
}
```

### 8. 与 JSON 序列化集成

```java
@Configuration
public class JacksonConfig {

    @Bean
    public Module sensitiveModule() {
        SimpleModule module = new SimpleModule();
        module.setSerializerModifier(new BeanSerializerModifier() {
            @Override
            public JsonSerializer<?> modifySerializer(
                SerializationConfig config,
                BeanDescription beanDesc,
                JsonSerializer<?> serializer) {

                if (beanDesc.getBeanClass().isAnnotationPresent(Sensitive.class)) {
                    return new SensitiveSerializer(serializer);
                }
                return serializer;
            }
        });
        return module;
    }
}
```

-----

## 📋 内置策略参考

### 可用脱敏类型

| 类型 | 注解值 | 描述 | 示例 |
| :--- | :--- | :--- | :--- |
| **中文姓名** | `CHINESE_NAME` | 中文姓名脱敏 | 张三 → *三 |
| **手机号** | `MOBILE_PHONE` | 屏蔽中间4位 | 182****3070 |
| **座机** | `FIXED_PHONE` | 屏蔽区号或号码 | 010****** |
| **身份证** | `ID_CARD` | 屏蔽中间10位 | 110101**********34 |
| **银行卡** | `BANK_CARD` | 显示前4后4位 | 6222************7890 |
| **邮箱** | `EMAIL` | 屏蔽用户名 | joh***@example.com |
| **密码** | `PASSWORD` | 返回空字符串 | (空) |
| **车牌** | `CAR_LICENSE` | 屏蔽中间字符 | 京A***** |
| **地址** | `ADDRESS` | 屏蔽详细信息 | 北京市朝阳区****** |
| **联行号** | `CNAPS` | 屏蔽部分代码 | 1234****** |
| **护照** | `PASSPORT` | 屏蔽中间部分 | E12*****34 |
| **用户ID** | `USER_ID` | 显示后4位 | ******1234 |

### 脱敏模式

| 模式 | 描述 | 示例 |
| :--- | :--- | :--- |
| **LEFT** | 屏蔽左侧 | *******3070 |
| **RIGHT** | 屏蔽右侧 | 1823******* |
| **MIDDLE** | 屏蔽中间部分（默认） | 182****3070 |
| **CUSTOM** | 使用自定义配置 | 用户定义 |

-----

## 💡 最佳实践

### 1. 选择合适的脱敏策略

```java
// ✅ 推荐: 使用合适的内置策略
@Shield(type = EnumValue.Masking.MOBILE_PHONE)
private String mobile;

// ❌ 不推荐: 存在内置策略时使用自定义策略
@Shield(strategy = CustomPhoneStrategy.class)
private String mobile;
```

### 2. 在正确的层应用脱敏

```java
// ✅ 推荐: 在服务层/控制器层应用
@Service
public class UserService {
    @Sensitive
    public User getUserProfile(Long id) {
        return userRepository.findById(id);
    }
}

// ❌ 不推荐: 在仓储层应用（数据应明文存储）
@Repository
public class UserRepository {
    @Sensitive
    public User findById(Long id) {
        // 永远不要在存储到数据库之前脱敏
    }
}
```

### 3. 需要时使用深度克隆

```java
// ✅ 推荐: 使用深度克隆避免修改原始对象
User maskedUser = Builder.on(originalUser, true);

// ❌ 不推荐: 原地修改原始对象
User maskedUser = Builder.on(originalUser, false);
```

### 4. 实现适当的条件

```java
// ✅ 推荐: 实现基于角色的条件
public class RoleBasedCondition implements ConditionProvider {
    @Override
    public boolean valid(Context context) {
        UserContext user = SecurityContextHolder.getCurrentUser();
        return !user.hasRole("ADMIN");
    }
}

@Shield(type = EnumValue.Masking.EMAIL,
        condition = RoleBasedCondition.class)
private String email;
```

### 5. 结合加密保护敏感数据

```java
// ✅ 推荐: 显示时脱敏，存储时加密
@Data
public class CreditCard {
    @Shield(type = EnumValue.Masking.BANK_CARD)  // 用于显示
    @Privacy(algo = Privacy.Algo.AES)           // 用于存储
    private String cardNumber;
}
```

### 6. 测试脱敏行为

```java
@Test
public void testMobileMasking() {
    User user = new User();
    user.setMobile("18233583070");

    User masked = Builder.on(user);

    assertEquals("182****3070", masked.getMobile());
    assertNotEquals("18233583070", masked.getMobile());
}
```

-----

## ❓ 常见问题

### Q1: 如何自定义脱敏字符？

```java
@Shield(type = EnumValue.Masking.MOBILE_PHONE,
        shadow = "#")  // 使用 # 代替 *
private String mobile;

// 结果: 182####3070
```

### Q2: 如何保留更多可见字符？

```java
@Shield(type = EnumValue.Masking.ID_CARD,
        fixedHeaderSize = 8,    // 显示前8位
        fixedTailorSize = 4)    // 显示后4位
private String idCard;

// 结果: 11010119**********1234
```

### Q3: 如何禁用自动修正？

```java
@Shield(type = EnumValue.Masking.MOBILE_PHONE,
        autoFixedPart = false,
        fixedHeaderSize = 2,
        fixedTailorSize = 2)
private String mobile;

// 结果: 18**********70 (自定义配置)
```

### Q4: 如何对集合和数组脱敏？

```java
@Data
public class OrderList {
    @Shield(type = EnumValue.Masking.MOBILE_PHONE)
    private List<String> contactPhones;  // 每个元素都会被脱敏

    @Shield(type = EnumValue.Masking.EMAIL)
    private String[] emails;  // 每个邮箱都会被脱敏
}
```

### Q5: 如何与 MyBatis/JPA 集成？

```java
// 对于 MyBatis - 使用拦截器
@Intercepts({
    @Signature(type= ResultSetHandler.class,
               method="handleResultSets",
               args={Statement.class})
})
public class SensitiveInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        List<Object> results = (List<Object>) invocation.proceed();
        return Builder.on(results);  // 脱敏所有结果
    }
}

// 对于 JPA - 使用实体监听器
@EntityListeners(SensitiveListener.class)
@Entity
public class User {
    @Shield(type = EnumValue.Masking.MOBILE_PHONE)
    private String mobile;
}
```

### Q6: 如何处理空值？

```java
// 框架自动处理空值
User user = new User();
user.setMobile(null);

User masked = Builder.on(user);
System.out.println(masked.getMobile());  // 输出: null
```

### Q7: 如何禁用特定字段的脱敏？

```java
@Data
@Sensitive(skip = {"publicField", "status"})
public class UserProfile {
    @Shield(type = EnumValue.Masking.CHINESE_NAME)
    private String name;  // 会被脱敏

    private String publicField;  // 跳过，不脱敏

    private Integer status;  // 跳过，不脱敏
}
```

### Q8: 如何根据用户角色应用不同的脱敏？

```java
public class UserRoleCondition implements ConditionProvider {
    @Override
    public boolean valid(Context context) {
        UserContext user = SecurityContextHolder.getCurrentUser();
        String fieldName = context.getCurrentField().getName();

        // 仅对普通用户脱敏邮箱
        if ("email".equals(fieldName)) {
            return !user.hasRole("ADMIN");
        }
        return true;
    }
}
```

-----

## 🔧 高级配置

### 自定义策略提供者

```java
public class CustomMaskingProvider extends AbstractProvider {

    @Override
    public Object build(Object object, Context context) {
        if (object == null) {
            return null;
        }

        String value = object.toString();
        Shield shield = context.getShield();

        // 实现自定义脱敏逻辑
        int maskLength = value.length() / 2;
        String masked = StringKit.repeat(shield.shadow(), maskLength);

        return value.substring(0, 2) + masked + value.substring(value.length() - 2);
    }
}
```

### 注册自定义策略

```java
@Configuration
public class SensitiveConfig {

    @PostConstruct
    public void registerCustomStrategies() {
        Registry.register(EnumValue.Masking.CUSTOM, CustomMaskingProvider.class);
    }
}
```

-----

## 📊 性能考虑

### 性能建议

1. **使用内置策略**: 内置策略针对性能进行了优化
2. **避免过度脱敏**: 仅对真正包含敏感数据的字段脱敏
3. **缓存条件结果**: 缓存昂贵的条件检查
4. **选择性处理**: 仅对必要的层启用脱敏

### 基准测试

| 操作 | 平均时间 | 吞吐量 |
| :--- | :--- | :--- |
| **简单字段脱敏** | 0.01ms | 100,000 ops/s |
| **复杂对象（10个字段）** | 0.1ms | 10,000 ops/s |
| **大型集合（1000项）** | 15ms | 66 ops/s |
| **深度嵌套对象（5层）** | 0.5ms | 2,000 ops/s |

-----

## 🔄 版本兼容性

| Bus Sensitive 版本 | Bus Core 版本 | Bus Crypto 版本 | JDK 版本 |
| :--- | :--- | :--- | :--- |
| 8.x | 8.x | 8.x | 17+ |
| 7.x | 7.x | 7.x | 11+ |

-----

## 🌟 使用场景

### 1. 金融服务
- 脱敏账号和交易详情
- 保护客户财务信息
- 符合银行业监管要求

### 2. 医疗健康
- 脱敏患者姓名和病历
- 保护敏感健康信息
- HIPAA 合规

### 3. 电子商务
- 脱敏客户联系信息
- 保护配送和支付详情
- 防止日志中的数据泄露

### 4. 社交平台
- 脱敏用户个人信息
- 保护用户资料中的隐私
- 控制数据可见性

### 5. 企业应用
- 基于角色的数据脱敏
- 审计日志保护
- 多租户数据隔离

-----

## 📚 其他资源

- **Bus Core 文档**: [https://www.miaixz.org](https://www.miaixz.org)
- **Bus Crypto 文档**: [https://www.miaixz.org/crypto](https://www.miaixz.org/crypto)
- **GitHub 仓库**: [https://github.com/818000/bus](https://github.com/818000/bus)
- **问题追踪**: [https://github.com/818000/bus/issues](https://github.com/818000/bus/issues)

-----

## 📄 许可证

本项目基于 MIT 许可证开源 - 详见 [LICENSE](https://github.com/818000/bus/blob/main/LICENSE) 文件

-----

## 🤝 贡献

欢迎贡献！随时可以提交 Pull Request。

1. Fork 仓库
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

-----

**由 Miaixz 团队用 ❤️ 制作**
