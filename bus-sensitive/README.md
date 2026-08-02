# bus-sensitive

`bus-sensitive` is the Bus data-protection engine. It provides annotation-driven object masking, conditional and custom
strategies, encryption/decryption processing, JSON output, deep-copy processing, and structured log sanitization. The
module is framework-neutral: it owns protection algorithms, while Spring activation and Web advice belong to
`bus-starter`.

## Module responsibilities

The module provides two related but independent protection paths:

```text
domain object -> Builder -> Provider -> Filter -> StrategyProvider -> protected object or JSON

log event -> Sanitizer -> bus-logger Operator -> protected arguments -> logging provider
```

It owns:

- field and type-level protection annotations;
- built-in masking strategies;
- conditional masking and custom strategy SPIs;
- traversal context and recursive object processing;
- optional cloned-result processing;
- encryption/decryption selection through privacy annotations;
- structured log key classification and recursive value sanitization.

It does not own:

- Spring configuration properties or feature activation;
- Servlet request/response advice;
- logging backend selection;
- storage of encryption keys;
- application authorization decisions.

## Dependency

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-sensitive</artifactId>
    <version>${revision}</version>
</dependency>
```

For automatic Spring MVC and logging integration, use `bus-starter` together with this module.

## Core model

| Type | Responsibility |
|---|---|
| `Builder` | Static entry point and processing mode/direction constants. |
| `Provider<T>` | Traverses and processes an object, optionally cloning it first. |
| `Context` | Carries the current object, field, annotations, and processing state. |
| `Filter` | Selects and applies field rules during traversal. |
| `Registry` | Holds built-in masking providers and resolves annotation strategies. |
| `StrategyProvider` | SPI for a masking transformation. |
| `ConditionProvider` | SPI deciding whether a rule applies to the current context. |
| `Sanitizer` | `bus-logger` operator for structured log values. |

## Protection annotations

| Annotation | Scope | Purpose |
|---|---|---|
| `@Sensitive` | Type or processing entry | Selects masking/security mode, direction, included fields, skipped fields, and nested traversal. |
| `@Shield` | Field | Declares one masking rule and its visible-prefix/suffix behavior. |
| `@NShield` | Field | Groups multiple `@Shield` rules and optional include/filter expressions. |
| `@Privacy` | Field | Selects security processing such as encryption or decryption. |
| `@Strategy` | Annotation type | Associates a custom annotation with a strategy provider. |
| `@Condition` | Annotation type | Marks conditional protection metadata. |
| `@Entry` | Annotation type | Marks a protection entry annotation. |

### `@Sensitive` options

| Attribute | Default | Meaning |
|---|---|---|
| `value` | `Builder.ALL` | Processing capability: masking, security, both, or neither. |
| `stage` | `Builder.ALL` | Processing direction such as input or output. |
| `field` | empty | Explicit field inclusion list. |
| `skip` | empty | Fields excluded from processing. |
| `inside` | `true` | Whether nested values are traversed. |

Processing constants are:

- `Builder.SENS`: masking only;
- `Builder.SAFE`: encryption/decryption only;
- `Builder.ALL`: masking and security processing;
- `Builder.IN`: input/write direction;
- `Builder.OUT`: output/read direction;
- `Builder.NOTHING`: no processing;
- `Builder.OVERALL`: complete traversal scope.

### `@Shield` options

| Attribute | Default | Meaning |
|---|---|---|
| `type` | `EnumValue.Masking.NONE` | Built-in masking strategy. |
| `mode` | `EnumValue.Mode.MIDDLE` | Visible/masked placement mode. |
| `shadow` | `*` | Replacement character or text. |
| `fixedHeaderSize` | `0` | Fixed visible prefix length. |
| `fixedTailorSize` | `3` | Fixed visible suffix length. |
| `autoFixedPart` | `true` | Allows strategy-specific automatic visible lengths. |
| `condition` | `ConditionProvider.class` | Optional runtime condition. |
| `strategy` | `DafaultProvider.class` | Optional custom strategy implementation. |
| `key` / `field` | empty | Rule-specific lookup metadata. |

## Quick start

### Define protected fields

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

### Process an object

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

Use clone mode when the original object must remain unchanged. The object's complete graph still needs to be compatible
with the module's clone and traversal rules.

### Apply an explicit processing annotation

```java
Sensitive policy = AccountView.class.getAnnotation(Sensitive.class);
Account result = Builder.on(account, policy, true);
```

The overload accepting an annotation is intended for infrastructure that has already resolved the effective policy.
Normal domain code should prefer annotations on the protected model and the simpler entry points.

## Built-in masking strategies

`Registry` initializes these `EnumValue.Masking` strategies:

| Masking value | Provider | Typical data |
|---|---|---|
| `ADDRESS` | `AddressProvider` | Postal address. |
| `BANK_CARD` | `BandCardProvider` | Bank card number. |
| `CITIZENID` | `CitizenIdProvider` | Citizen identity number. |
| `CNAPS_CODE` | `CnapsProvider` | Bank routing code. |
| `DEFAUL` | `DafaultProvider` | General-purpose masking. |
| `EMAIL` | `EmailProvider` | Email address. |
| `MOBILE` | `MobileProvider` | Mobile number. |
| `NAME` | `NameProvider` | Person name. |
| `NONE` | `NoneProvider` | No masking. |
| `PASSWORD` | `PasswordProvider` | Password or secret text. |
| `PAY_SIGN_NO` | `CardProvider` | Payment signing identifier. |
| `PHONE` | `PhoneProvider` | Telephone number. |

The exact visible characters depend on the selected strategy and the `@Shield` options. Do not assume every strategy
uses the same prefix and suffix lengths.

## Custom strategy

Implement `StrategyProvider` when a built-in masking type cannot express the rule:

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

Reference it from the field rule:

```java
@Shield(strategy = ContractCodeProvider.class)
private String contractCode;
```

`StrategyProvider` instances must be stateless or thread-safe. The `Context` supplies the currently processed field and
value; it must not be retained after the call.

## Conditional masking

Implement `ConditionProvider` when masking depends on the active processing context:

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

Authorization-dependent conditions should consume authorization information supplied by the caller's integration layer;
the core sensitive module does not read a Spring or Servlet context.

## Strategy registry

`Registry` installs the built-in providers during class initialization. `Registry.require(...)` resolves a built-in or
annotation-associated provider and fails when the requested strategy is unavailable. `Registry.register(...)` rejects
duplicate masking identifiers, so application-specific behavior should normally use `@Shield(strategy = ...)` or a
custom annotation marked with `@Strategy` rather than attempting to replace a built-in provider globally.

## Structured log sanitization

`Sanitizer` implements `bus-logger`'s `Operator` contract. It normalizes keys and protects values associated with common
sensitive names, including authorization headers, cookies, passwords, secrets, tokens, API keys, private keys, and
credentials.

```java
import org.miaixz.bus.sensitive.Sanitizer;

Sanitizer sanitizer = new Sanitizer();

Object protectedToken = sanitizer.sanitize("accessToken", token);
boolean protectedKey = sanitizer.isSensitive("Authorization");
```

Protected scalar values become `Sanitizer.REDACTED`, currently `[REDACTED]`. Maps, iterables, and object arrays are copied
and sanitized recursively to a fixed safety depth. Arbitrary application objects are not reflected over, preventing
unexpected graph traversal, lazy loading, and side effects.

For complete log events, key names are inferred from assignments immediately before provider placeholders:

```java
Logger.warn("Login rejected: username={}, password={}", username, password);
```

Here `username` remains visible while `password` can be replaced. A message such as `"Login rejected: {}"` does not
provide a classifiable key and therefore cannot guarantee sanitization.

Do not move these rules into `bus-logger`. The logging facade remains content-neutral; this module owns classification
and replacement policy.

## Spring integration

With `bus-starter`, enable the feature explicitly:

```yaml
bus:
  sensitive:
    enabled: true
    debug: false
```

The integration consists of:

```text
SensitiveConfiguration
  +-- Sanitizer
  +-- SensitiveBinding -> logger Executor lifecycle
  `-- ServletConfiguration (Servlet MVC only)
        +-- SensitiveRequestBodyAdvice
        `-- SensitiveResponseBodyAdvice
```

`SensitiveBinding` registers one sanitizer per application context and unregisters it when that context closes. Servlet
request/response advice is contributed from the nested configuration; there is no separate
`SensitiveWebConfiguration`.

Encryption and decryption configuration is limited to the algorithms supported by the current Starter properties. Key
material must come from a protected external configuration source and must never be committed to an application file.

## Package layout

| Package | Content |
|---|---|
| `org.miaixz.bus.sensitive` | Processing entry points, traversal state, registry, and sanitizer. |
| `org.miaixz.bus.sensitive.magic.annotation` | Protection annotations and meta-annotations. |
| `org.miaixz.bus.sensitive.nimble` | Strategy contracts and built-in providers. |

All three packages are exported by the JPMS module. Spring-specific packages are intentionally absent.

## Best practices

- Protect data at a stable output or transport boundary rather than repeatedly throughout business logic.
- Use clone mode when callers still need the unmodified object.
- Select the narrowest built-in strategy before implementing a custom one.
- Keep custom strategy and condition implementations stateless.
- Avoid reflection over arbitrary objects in log sanitization.
- Use named log fields for values that require redaction.
- Keep encryption keys outside source control and diagnostic output.
- Treat masking as presentation protection, not as a replacement for authorization or encryption.

## Native Image

Reachability metadata lists only exact constructors and members needed by dynamic processing. Entries are sorted A–Z.
Broad reflection grants such as `allDeclaredConstructors`, `allDeclaredMethods`, and `allDeclaredFields` are prohibited.

## Verification boundary

Bus contains and runs no tests. Masking, cloning, custom strategy, Spring lifecycle, logging, metadata, AOT, and Native
Image tests are maintained in the sibling Abarth repository. Bus builds must skip tests explicitly.
