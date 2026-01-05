# 🔍 Bus Validate: Lightweight Java Validation Framework

<p align="center">
<strong>Powerful, Flexible, and Easy-to-Use Validation Framework</strong>
</p>

-----

## 📖 Project Introduction

**Bus Validate** is a lightweight, high-performance Java validation framework designed to simplify data validation in enterprise applications. It provides a rich set of built-in validators while supporting custom validation logic through annotation-based configuration.

**Key Features:**
- **Annotation-Based**: Declarative validation using Java annotations
- **Extensible**: Easy to create custom validators
- **Zero Dependencies**: Lightweight design with minimal dependencies
- **Type-Safe**: Compile-time validation support
- **Nested Validation**: Support for validating complex object graphs
- **Group Validation**: Validate different scenarios with validation groups
- **Flexible Error Handling**: Customizable error messages and codes

-----

## ✨ Core Features

### 🎯 Validation Capabilities

* **Rich Built-in Validators**: 20+ ready-to-use validation annotations
* **Null-Safe Handling**: Proper null value handling throughout the framework
* **Nested Object Validation**: Deep validation of complex object structures
* **Collection Validation**: Validate arrays, collections, and maps
* **Conditional Validation**: Group-based validation for different scenarios
* **Custom Validators**: Create your own validation logic with ease
* **Message Templates**: Flexible error message formatting with placeholders

### ⚡ Performance Optimization

| Feature | Benefit | Description |
| :--- | :--- | :--- |
| **Lazy Evaluation** | Low Overhead | Only validates when explicitly called |
| **Reflection Caching** | Fast Startup | Caches reflection metadata for better performance |
| **Minimal Dependencies** | Small Footprint | Only depends on bus-logger and Lombok |
| **Thread-Safe** | Concurrent Use | Safe to use in multi-threaded environments |

### 🛡️ Validation Coverage

* **String Validation**: Email, Phone, URL, IP Address, Regex, Length, Size
* **Number Validation**: Range, Multiple, Positive/Negative
* **Type Validation**: NotNull, NotEmpty, NotBlank
* **Collection Validation**: Size, Element validation with @Each
* **Custom Logic**: Conditional, Compare, Equals, In/NotIn
* **Regional Support**: Chinese characters, Citizen ID, Mobile numbers

-----

## 🚀 Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-validate</artifactId>
    <version>8.5.1</version>
</dependency>
```

### Basic Validation

#### 1. Define a Bean with Validation Annotations

```java
import lombok.Data;
import org.miaixz.bus.validate.magic.annotation.*;

@Data
public class User {

    @NotNull(errmsg = "User ID cannot be null")
    private Long id;

    @NotBlank(errmsg = "Username cannot be blank")
    @Size(min = 3, max = 20, errmsg = "Username must be between 3 and 20 characters")
    private String username;

    @Email(errmsg = "Invalid email format")
    private String email;

    @Mobile(errmsg = "Invalid mobile phone number")
    private String phone;

    @Chinese(errmsg = "Name must contain Chinese characters only")
    private String realName;

    @Date(errmsg = "Invalid date format")
    private String birthday;

    @NotNull
    @Compare(field = "password", errmsg = "Passwords do not match")
    private String confirmPassword;

    private String password;
}
```

#### 2. Perform Validation

```java
import org.miaixz.bus.validate.Builder;
import org.miaixz.bus.validate.Collector;

public class UserService {

    public void createUser(User user) {
        // Perform validation
        Collector result = Builder.on(user).access();

        // Check if validation passed
        if (!result.isPass()) {
            // Handle validation errors
            result.getResult().forEach(error -> {
                Criterion criterion = error.getCriterion();
                System.err.println("Validation failed: " + criterion.getErrmsg());
                System.err.println("Field: " + criterion.getField());
                System.err.println("Value: " + criterion.getParam("value"));
            });
            throw new ValidationException("User validation failed");
        }

        // Proceed with user creation
        saveUser(user);
    }
}
```

-----

## 📝 Built-in Validators

### Null/Empty Validators

| Annotation | Description | Example |
| :--- | :--- | :--- |
| `@NotNull` | Validates that the value is not null | `@NotNull(errmsg = "ID cannot be null")` |
| `@Null` | Validates that the value is null | `@Null(errmsg = "Must be null")` |
| `@NotEmpty` | Validates that the value is not empty (array, collection, map) | `@NotEmpty(errmsg = "List cannot be empty")` |
| `@NotBlank` | Validates that the string is not blank (trimmed length > 0) | `@NotBlank(errmsg = "Name cannot be blank")` |
| `@Blank` | Validates that the string is blank | `@Blank(errmsg = "Must be blank")` |

### String Validators

| Annotation | Description | Example |
| :--- | :--- | :--- |
| `@Email` | Validates email format | `@Email(errmsg = "Invalid email")` |
| `@Mobile` | Validates mobile phone number | `@Mobile(errmsg = "Invalid mobile")` |
| `@Phone` | Validates phone number | `@Phone(errmsg = "Invalid phone")` |
| `@Chinese` | Validates Chinese characters | `@Chinese(errmsg = "Must be Chinese")` |
| `@English` | Validates English characters | `@English(errmsg = "Must be English")` |
| `@URL` | Validates URL format | `@URL(errmsg = "Invalid URL")` |
| `@IPAddress` | Validates IP address | `@IPAddress(errmsg = "Invalid IP")` |
| `@CitizenId` | Validates Chinese citizen ID | `@CitizenId(errmsg = "Invalid ID")` |

### Size/Length Validators

| Annotation | Description | Example |
| :--- | :--- | :--- |
| `@Size` | Validates size of array, collection, map, or string length | `@Size(min = 1, max = 10)` |
| `@Length` | Validates string length | `@Length(min = 5, max = 100)` |
| `@Date` | Validates date format | `@Date(format = "yyyy-MM-dd")` |

### Numeric Validators

| Annotation | Description | Example |
| :--- | :--- | :--- |
| `@IntRange` | Validates integer range | `@IntRange(min = 0, max = 100)` |
| `@Multiple` | Validates if value is a multiple of specified number | `@Multiple(value = 5)` |

### Comparison Validators

| Annotation | Description | Example |
| :--- | :--- | :--- |
| `@Compare` | Compares two fields | `@Compare(field = "password")` |
| `@Equals` | Checks if value equals specified value | `@Equals(value = "ACTIVE")` |
| `@In` | Checks if value is in specified values | `@In(values = {"A", "B", "C"})` |
| `@NotIn` | Checks if value is not in specified values | `@NotIn(values = {"X", "Y", "Z"})` |
| `@InEnum` | Checks if value is valid enum value | `@InEnum(StatusEnum.class)` |

### Boolean Validators

| Annotation | Description | Example |
| :--- | :--- | :--- |
| `@True` | Validates that value is true | `@True(errmsg = "Must be true")` |
| `@False` | Validates that value is false | `@False(errmsg = "Must be false")` |

### Special Validators

| Annotation | Description | Example |
| :--- | :--- | :--- |
| `@Regex` | Validates using regular expression | `@Regex(value = "^[A-Z]\\d{5}$")` |
| `@Each` | Validates each element in a collection | `@Each @NotNull` |
| `@Valid` | Enables nested validation | `@Valid(inside = true)` |
| `@Group` | Specifies validation groups | `@Group({"create", "update"})` |

-----

## 🔧 Advanced Usage

### Nested Object Validation

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

// Validation
Collector result = Builder.on(order).access();
```

### Validation Groups

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

// Validate with specific group
Context context = Context.newInstance();
context.addGroups("create");

Collector result = Builder.on(product, context).access();
```

### Custom Validator

#### Step 1: Create Validator Class

```java
import org.miaixz.bus.validate.magic.Matcher;

public class AgeValidator implements Matcher {

    @Override
    public boolean on(Object value, Context context) {
        if (value == null) {
            return true; // null values are considered valid
        }

        int age = (Integer) value;
        return age >= 18 && age <= 120;
    }
}
```

#### Step 2: Create Annotation

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
    String errmsg() default "Age must be between 18 and 120";
    String[] group() default {};
    String field() default Builder.DEFAULT_FIELD;
}
```

#### Step 3: Use Custom Validator

```java
public class Person {

    @ValidAge
    private Integer age;
}
```

### Collection Element Validation

```java
public class BatchRequest {

    @Each @NotNull
    @Each @Size(min = 36, max = 36)
    private List<String> userIds;

    @Valid
    private List<User> users;
}
```

### Conditional Validation

```java
@Data
public class RegistrationForm {

    private String userType; // "individual" or "company"

    @NotBlank
    private String username;

    // Only validate if userType is "company"
    @NotBlank(errmsg = "Company name is required")
    @Reflect(condition = "userType == 'company'")
    private String companyName;
}
```

-----

## 💡 Best Practices

### 1. Use Meaningful Error Messages

```java
// ✅ Good: Clear and specific
@NotBlank(errmsg = "Email address is required")
@Email(errmsg = "Email address must be in valid format (e.g., user@example.com)")
private String email;

// ❌ Bad: Vague and unhelpful
@NotBlank(errmsg = "Invalid")
private String email;
```

### 2. Combine Multiple Validators

```java
// ✅ Good: Comprehensive validation
@NotBlank
@Size(min = 8, max = 20)
@Regex(value = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]+$",
        errmsg = "Password must contain letters and numbers")
private String password;
```

### 3. Use Validation Groups for Different Scenarios

```java
public class User {

    @NotNull(group = {"create"})
    @Null(group = {"update"})
    private Long id;

    @NotBlank(group = {"create", "update"})
    private String username;
}
```

### 4. Handle Null Values Appropriately

```java
// Most validators skip null values by default
// Use @NotNull explicitly when null is invalid
@NotNull
@Size(min = 1, max = 100)
private String name;

// For optional fields, avoid @NotNull
@Email  // Allows null, but validates if not null
private String optionalEmail;
```

### 5. Nested Validation for Complex Objects

```java
// Always use @Valid for nested objects
@Valid
@NotNull
private Address address;

@Valid
@NotEmpty
private List<Phone> phones;
```

### 6. Custom Error Messages with Placeholders

```java
// Use placeholders in error messages
@Size(min = 3, max = 20, errmsg = "Username must be between ${min} and ${max} characters")
private String username;

// Available placeholders: ${field}, ${value}, ${min}, ${max}, etc.
```

-----

## ❓ Frequently Asked Questions

### Q1: How to handle validation failures gracefully?

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

### Q2: Can I validate without annotations?

```java
// Yes, use programmatic validation
Context context = Context.newInstance();
Collector result = Builder.on(value, new Annotation[]{}, context).access();
```

### Q3: How to customize validation messages internationally?

```java
// Use message codes and resolve with MessageSource
@Email(errcode = "validation.email.invalid")
private String email;

// In your service
String message = messageSource.getMessage(
    criterion.getErrcode(),
    new Object[]{criterion.getField()},
    LocaleContextHolder.getLocale()
);
```

### Q4: How to skip certain fields during validation?

```java
@Valid(skip = {"internalField", "tempField"})
private MyClass object;
```

### Q5: Can I validate method parameters?

```java
public void updateUser(@Valid User user) {
    // Validation will be triggered automatically
}
```

### Q6: How to optimize performance for large collections?

```java
// Use @Each for element-level validation
@Each @NotNull
@Each @Size(min = 1, max = 100)
private List<String> items;

// For very large collections, consider batch validation
List<List<String>> batches = Lists.partition(items, 1000);
for (List<String> batch : batches) {
    Builder.on(batch).access();
}
```

### Q7: How to disable nested validation?

```java
@Valid(inside = false)
private User user;  // Only validates user itself, not nested fields
```

### Q8: Can I use multiple validation groups?

```java
@Data
public class Product {

    @NotBlank(group = {"create", "update"})
    private String name;

    @Null(group = "create")
    @NotNull(group = "update")
    private Long id;
}

// Validate multiple groups
Context context = Context.newInstance();
context.addGroups("create", "update");
Builder.on(product, context).access();
```

-----

## 🔄 Version Compatibility

| Bus Validate Version | JDK Version | Notes |
| :--- | :--- | :--- |
| 8.x | 17+ | Requires Java 17 or higher |
| 7.x | 11+ | Supports Java 11 and higher |

-----

## 📊 Validator Reference Table

### Complete Annotation List

| Annotation | Target Types | Null Handling | Description |
| :--- | :--- | :--- | :--- |
| `@NotNull` | All | ❌ Invalid | Value must not be null |
| `@Null` | All | ✅ Valid | Value must be null |
| `@NotEmpty` | Array, Collection, Map, String | ❌ Invalid | Value must not be empty |
| `@NotBlank` | CharSequence | ❌ Invalid | Trimmed length must be > 0 |
| `@Blank` | CharSequence | ✅ Valid | Trimmed length must be 0 |
| `@Email` | CharSequence | ✅ Valid | Must be valid email format |
| `@Mobile` | CharSequence | ✅ Valid | Must be valid mobile number |
| `@Phone` | CharSequence | ✅ Valid | Must be valid phone number |
| `@Chinese` | CharSequence | ✅ Valid | Must contain Chinese characters |
| `@English` | CharSequence | ✅ Valid | Must contain English characters |
| `@IPAddress` | CharSequence | ✅ Valid | Must be valid IP address |
| `@CitizenId` | CharSequence | ✅ Valid | Must be valid citizen ID |
| `@Size` | Array, Collection, Map, CharSequence | ✅ Valid | Size/length must be in range |
| `@Length` | CharSequence | ✅ Valid | Length must be in range |
| `@Date` | CharSequence | ✅ Valid | Must match date format |
| `@IntRange` | Number, CharSequence | ✅ Valid | Must be in integer range |
| `@Multiple` | Number, CharSequence | ✅ Valid | Must be multiple of value |
| `@True` | Boolean | ✅ Valid | Must be true |
| `@False` | Boolean | ✅ Valid | Must be false |
| `@Compare` | All | ✅ Valid | Must compare to other field |
| `@Equals` | All | ✅ Valid | Must equal specified value |
| `@In` | All | ✅ Valid | Must be in specified values |
| `@NotIn` | All | ✅ Valid | Must not be in specified values |
| `@InEnum` | CharSequence | ✅ Valid | Must be valid enum value |
| `@Regex` | CharSequence | ✅ Valid | Must match regex pattern |
| `@Each` | Array, Collection | - | Validates each element |
| `@Valid` | All | - | Enables nested validation |

-----

## 🎯 Common Use Cases

### Form Validation

```java
@Data
public class LoginForm {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, max = 32)
    private String password;

    @True(errmsg = "You must accept the terms and conditions")
    private Boolean acceptTerms;
}
```

### API Request Validation

```java
@Data
public class CreateOrderRequest {

    @NotNull
    private Long userId;

    @Valid
    @NotEmpty
    private List<OrderItem> items;

    @NotNull
    @Compare(field = "totalAmount", errmsg = "Payment amount must match total")
    private BigDecimal paymentAmount;

    private BigDecimal totalAmount;
}
```

### Data Import Validation

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

## 🔗 Links

- **Project Homepage**: [https://github.com/818000/bus](https://github.com/818000/bus)
- **Issues**: [https://github.com/818000/bus/issues](https://github.com/818000/bus/issues)
- **Bus Framework Modules**:
  - [bus-core](https://github.com/818000/bus/tree/main/bus-core) - Core utilities
  - [bus-mapper](https://github.com/818000/bus/tree/main/bus-mapper) - MyBatis enhancement
  - [bus-logger](https://github.com/818000/bus/tree/main/bus-logger) - Logging framework
  - [bus-crypto](https://github.com/818000/bus/tree/main/bus-crypto) - Cryptography
  - [bus-extra](https://github.com/818000/bus/tree/main/bus-extra) - Extra utilities
