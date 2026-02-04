# 🔐 Bus Auth：企业级身份验证与授权框架

<p align="center">
<strong>统一身份验证和授权解决方案，支持多种协议和身份提供程序</strong>
</p>

-----

## 📖 项目简介

**Bus Auth** 是一个企业级身份验证和授权框架，旨在简化与第三方身份提供程序的集成。它提供统一的 API 来实现 OAuth2、SAML、LDAP 和自定义身份验证协议，支持全球 **40+** 主流平台。

该框架抽象了协议复杂性，使开发人员能够专注于业务逻辑而非身份验证实现细节。无论是社交登录、企业 SSO 还是自定义身份提供程序，Bus Auth 都提供一致、类型安全和可扩展的方法。

-----

## ✨ 核心功能

### 🎯 统一身份验证接口

* **协议无关**：OAuth2、SAML、LDAP 和自定义协议的单一 API
* **提供程序抽象**：40+ 身份提供程序的一致接口
* **构建器模式**：流畅的 API 用于配置身份验证流程
* **类型安全**：强类型配置和响应对象

### 🔐 安全优先

| 功能 | 描述 |
| :--- | :--- |
| **PKCE 支持** | 符合 RFC 7636 的代码交换证明密钥，用于移动/SPA 应用 |
| **状态验证**：内置 CSRF 保护，带状态参数验证 |
| **令牌管理**：安全令牌存储、刷新和撤销 |
| **签名验证**：OAuth1.0a 的 HMAC-SHA256 签名支持 |
| **缓存集成**：分布式状态缓存支持 |

### 🌍 平台覆盖

**社交平台**（15+）
- GitHub、Google、Facebook、Twitter、LinkedIn、Microsoft
- 微信、QQ、微博、抖音、TikTok
- Apple、Amazon、Slack、Line、VK

**企业平台**（10+）
- 钉钉、飞书、Lark、企业微信
- Okta、GitLab、Gitee、Teambition
- 华为、阿里云、百度云

**电商平台**（8+）
- 支付宝、淘宝、京东、美团、饿了么
- 酷家乐、小米、小红书

**国内平台**（中国）
- 喜马拉雅、人人、开源中国、Coding、Programmer
- Stack Overflow、Pinterest、Figma

### ⚡ 开发体验

* **零样板代码**：最少代码实现身份验证
* **自动配置**：约定优于配置，具有合理的默认值
* **灵活作用域**：通过 OAuth 作用域进行细粒度权限控制
* **丰富的元数据**：来自提供程序的综合用户配置文件数据
* **可扩展**：易于添加自定义提供程序或扩展现有提供程序

-----

## 🚀 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-auth</artifactId>
    <version>8.x.x</version>
</dependency>
```

### 基础用法

#### 1. 配置身份验证上下文

```java
// 创建身份验证上下文
Context context = Context.builder()
    .clientId("your_client_id")
    .clientSecret("your_client_secret")
    .redirectUri("https://yourapp.com/callback")
    .scopes(Arrays.asList("user", "repo"))
    .build();
```

#### 2. 创建身份验证提供程序

```java
// 方法 1：直接实例化
Provider github = new GithubProvider(context);

// 方法 2：使用 Authorizer 构建器（推荐）
Provider github = Authorizer.builder()
    .source("GITHUB")
    .context(context)
    .build();
```

#### 3. 生成授权 URL

```java
// 为 CSRF 保护生成状态参数
String state = UUID.randomUUID().toString();

// 获取授权 URL
Message message = github.build(state);
String authUrl = message.getData();

// 重定向用户到 authUrl
// 身份验证后，用户将使用 code 和 state 重定向到 redirectUri
```

#### 4. 处理回调和登录

```java
// 提取回调参数
Callback callback = Callback.builder()
    .code(request.getParameter("code"))
    .state(request.getParameter("state"))
    .build();

// 执行身份验证
Message result = github.authorize(callback);

if (result.isSuccess()) {
    Claims claims = result.getData(Claims.class);
    String uuid = claims.getUuid();
    String username = claims.getUsername();
    String email = claims.getEmail();

    // 登录用户或创建账户
    // 存储 claims.getToken() 用于未来的 API 调用
}
```

-----

## 📝 使用示例

### 1. GitHub OAuth2 身份验证

```java
// 配置
Context context = Context.builder()
    .clientId("github_client_id")
    .clientSecret("github_client_secret")
    .redirectUri("http://localhost:8080/auth/github/callback")
    .build();

Provider github = new GithubProvider(context);

// 步骤 1：重定向到 GitHub
@GetMapping("/auth/github")
public void githubLogin(HttpServletResponse response) throws IOException {
    String state = UUID.randomUUID().toString();
    cache.set(state, "true", 10, TimeUnit.MINUTES); // 在缓存中存储状态

    Message message = github.build(state);
    response.sendRedirect(message.getData());
}

// 步骤 2：处理回调
@GetMapping("/auth/github/callback")
public Message githubCallback(@RequestParam String code, @RequestParam String state) {
    Callback callback = Callback.builder()
        .code(code)
        .state(state)
        .build();

    Message result = github.authorize(callback);

    if (result.isSuccess()) {
        Claims user = result.getData(Claims.class);
        // 处理用户信息
        return Message.success(user);
    }
    return Message.error("身份验证失败");
}
```

### 2. 企业微信身份验证

```java
// 企业微信需要额外的 agentId
Context context = Context.builder()
    .clientId("corp_id")
    .clientSecret("corp_secret")
    .unionId("agent_id")
    .redirectUri("https://yourapp.com/callback/wechat")
    .build();

Provider wechatWork = new WeChatEeWebProvider(context);

// 身份验证流程与 GitHub 相同
Message result = wechatWork.authorize(callback);
Claims user = result.getData(Claims.class);
```

### 3. 移动/SPA 应用的 PKCE 模式

```java
// 启用 PKCE 模式
Context context = Context.builder()
    .clientId("client_id")
    .clientSecret("")  // 公共客户端无客户端密钥
    .redirectUri("myapp://callback")
    .pkce(true)  // 启用 PKCE
    .build();

Provider google = new GoogleProvider(context);

// 生成代码验证器和挑战
String codeVerifier = Builder.codeVerifier();
String codeChallenge = Builder.codeChallenge("S256", codeVerifier);

// 存储 codeVerifier 供稍后使用
cache.set(state, codeVerifier, 10, TimeUnit.MINUTES);

// 在授权 URL 中包含 code_challenge
```

### 4. 自定义 OAuth2 提供程序

```java
// 方法 1：扩展 AbstractProvider
public class CustomProvider extends AbstractProvider {

    public CustomProvider(Context context) {
        super(context, Complex.custom("CUSTOM", Protocol.OIDC));
    }

    @Override
    public Map<Endpoint, String> getEndpoint() {
        Map<Endpoint, String> endpoints = new HashMap<>();
        endpoints.put(Endpoint.AUTHORIZE, "https://api.custom.com/oauth/authorize");
        endpoints.put(Endpoint.TOKEN, "https://api.custom.com/oauth/token");
        endpoints.put(Endpoint.USERINFO, "https://api.custom.com/oauth/userinfo");
        return endpoints;
    }

    @Override
    public Message token(Callback callback) {
        // 自定义令牌交换逻辑
    }

    @Override
    public Message userInfo(Authorization authorization) {
        // 自定义用户信息检索
    }
}

// 方法 2：使用 Registry 和自定义 Complex
Complex customComplex = new Complex() {
    @Override
    public String getName() { return "CUSTOM"; }

    @Override
    public Protocol getProtocol() { return Protocol.OIDC; }

    @Override
    public Class<? extends AbstractProvider> getTargetClass() {
        return CustomProvider.class;
    }

    @Override
    public Map<Endpoint, String> endpoint() {
        // 返回端点映射
    }
};

Provider provider = Authorizer.builder()
    .source("CUSTOM")
    .context(context)
    .complex(customComplex)
    .build();
```

### 5. 令牌管理

```java
// 获取访问令牌
Message tokenMsg = provider.token(callback);
Authorization token = tokenMsg.getData(Authorization.class);

// 访问令牌详情
String accessToken = token.getToken();
int expiresIn = token.getExpireIn();
String refreshToken = token.getRefresh();

// 刷新令牌（如果支持）
Message refreshMsg = provider.refresh(token);
Authorization newToken = refreshMsg.getData(Authorization.class);

// 撤销授权（登出）
Message revokeMsg = provider.revoke(token);
```

### 6. 多提供程序支持

```java
// 提供程序注册表
Map<String, Provider> providers = new HashMap<>();

// 配置多个提供程序
providers.put("github", new GithubProvider(githubContext));
providers.put("google", new GoogleProvider(googleContext));
providers.put("wechat", new WeChatMpProvider(wechatContext));

// 统一身份验证端点
@PostMapping("/auth/{provider}")
public Message authenticate(@PathVariable String provider,
                            @RequestBody Callback callback) {
    Provider authProvider = providers.get(provider);
    if (authProvider == null) {
        return Message.error("不支持的提供程序：" + provider);
    }

    return authProvider.authorize(callback);
}
```

### 7. 自定义缓存实现

```java
// 使用 Redis 进行分布式状态缓存
public class RedisAuthCache implements CacheX {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value.toString(), timeout, unit);
    }

    @Override
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public boolean containsKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void remove(String key) {
        redisTemplate.delete(key);
    }
}

// 使用自定义缓存
RedisAuthCache cache = new RedisAuthCache(redisTemplate);
Provider github = new GithubProvider(context, cache);
```

### 8. 动态端点配置

```java
// 覆盖默认端点
Map<Endpoint, String> customEndpoints = new HashMap<>();
customEndpoints.put(Endpoint.AUTHORIZE, "https://custom.auth.com/authorize");
customEndpoints.put(Endpoint.TOKEN, "https://custom.auth.com/token");
customEndpoints.put(Endpoint.USERINFO, "https://custom.auth.com/userinfo");
customEndpoints.put(Endpoint.REFRESH, "https://custom.auth.com/refresh");

Context context = Context.builder()
    .clientId("client_id")
    .clientSecret("client_secret")
    .redirectUri("https://yourapp.com/callback")
    .endpoint(customEndpoints)  // 自定义端点
    .build();
```

-----

## 📋 配置参考

### 上下文参数

| 参数 | 类型 | 必需 | 描述 |
| :--- | :--- | :--- | :--- |
| `clientId` | String | ✅ | OAuth2 客户端 ID 或 API 密钥 |
| `clientSecret` | String | ✅ | OAuth2 客户端密钥 |
| `unionId` | String | ❌ | 平台特定标识符（例如，微信 agentId） |
| `extId` | String | ❌ | 扩展标识符 |
| `deviceId` | String | ❌ | 某些平台的设备 ID |
| `type` | String | ❌ | 平台特定类型 |
| `flag` | boolean | ❌ | 平台特定标志 |
| `pkce` | boolean | ❌ | 启用 PKCE 模式（默认：false） |
| `prefix` | String | ❌ | 域前缀（用于 Okta、Coding） |
| `redirectUri` | String | ✅ | OAuth2 回调 URL |
| `scopes` | List<String> | ❌ | OAuth2 作用域（权限） |
| `ignoreState` | boolean | ❌ | 跳过状态验证（不推荐） |
| `ignoreRedirectUri` | boolean | ❌ | 跳过重定向 URI 验证 |
| `kid` | String | ❌ | Apple 密钥 ID |
| `teamId` | String | ❌ | Apple 团队 ID |
| `loginType` | String | ❌ | 企业微信登录类型 |
| `lang` | String | ❌ | 语言代码（默认：zh） |
| `extension` | String | ❌ | 扩展属性 |
| `endpoint` | Map<Endpoint, String> | ❌ | 自定义 OAuth 端点 |

### 支持的端点

| 端点 | 描述 |
| :--- | :--- |
| `AUTHORIZE` | 授权端点 URL |
| `TOKEN` | 令牌端点 URL |
| `USERINFO` | 用户信息端点 URL |
| `REFRESH` | 令牌刷新端点 URL |
| `REVOKE` | 令牌撤销端点 URL |

### 提供程序注册表

所有内置提供程序都在 `Registry` 枚举中注册：

```java
// 访问注册表
Registry.GITHUB
Registry.GOOGLE
Registry.WECHAT_MP
Registry.DINGTALK
// ... 40+ 提供程序

// 与 Authorizer 一起使用
Provider provider = Authorizer.builder()
    .source(Registry.GITHUB.getName())
    .context(context)
    .build();
```

-----

## 🔧 高级配置

### 1. 自定义作用域配置

```java
// 每个提供程序提供默认作用域
// 可以为特定需求自定义作用域

Context context = Context.builder()
    .clientId("client_id")
    .clientSecret("client_secret")
    .redirectUri("https://yourapp.com/callback")
    .scopes(Arrays.asList("read", "write", "email"))  // 自定义作用域
    .build();
```

### 2. 状态验证

```java
// 默认启用状态验证
// 禁用（生产环境不推荐）：

Context context = Context.builder()
    .clientId("client_id")
    .clientSecret("client_secret")
    .redirectUri("https://yourapp.com/callback")
    .ignoreState(true)  // ⚠️ 禁用状态验证
    .build();
```

### 3. 自定义用户信息映射

```java
// 扩展 AbstractProvider 以自定义用户信息解析

public class CustomGithubProvider extends GithubProvider {

    @Override
    public Message userInfo(Authorization authorization) {
        // 获取原始用户数据
        Message response = super.userInfo(authorization);

        // 自定义映射逻辑
        Map<String, Object> rawData = response.getData();
        Claims claims = Claims.builder()
            .uuid(rawData.get("id").toString())
            .username(rawData.get("login").toString())
            .email(rawData.get("email").toString())
            .avatar(rawData.get("avatar_url").toString())
            .source("GITHUB")
            .token(authorization)
            .rawJson(JsonKit.toJsonString(rawData))
            .build();

        return Message.success(claims);
    }
}
```

### 4. 错误处理

```java
try {
    Message result = provider.authorize(callback);

    if (result.isSuccess()) {
        Claims user = result.getData(Claims.class);
        // 成功处理
    } else {
        // 错误处理
        String errorCode = result.getErrcode();
        String errorMsg = result.getErrmsg();
    }
} catch (AuthorizedException e) {
    // 处理身份验证异常
    log.error("身份验证失败", e);

    // 常见错误代码：
    // 110001 - 不支持的提供程序或配置无效
    // 110002 - 配置不完整
    // 110005 - 无效的重定向 URI
    // 110007 - 缺少授权代码
    // 110008 - 状态无效或已过期
}
```

-----

## 💡 最佳实践

### 1. 始终使用 HTTPS

```java
// ❌ 不推荐
Context context = Context.builder()
    .redirectUri("http://yourapp.com/callback")
    .build();

// ✅ 推荐
Context context = Context.builder()
    .redirectUri("https://yourapp.com/callback")
    .build();
```

### 2. 启用状态验证

```java
// ✅ 始终在生产环境启用状态验证
Context context = Context.builder()
    .ignoreState(false)  // 默认：false
    .build();
```

### 3. 使用分布式缓存

```java
// ✅ 使用 Redis 或其他分布式缓存存储状态
RedisAuthCache cache = new RedisAuthCache(redisTemplate);
Provider provider = new GithubProvider(context, cache);
```

### 4. 实现适当的错误处理

```java
// ✅ 全面的错误处理
try {
    Message result = provider.authorize(callback);
    // 处理结果
} catch (AuthorizedException e) {
    // 记录错误详情
    log.error("身份验证失败：code={}, message={}",
        e.getCode(), e.getMessage());

    // 返回用户友好的错误消息
    return Message.error("登录失败，请重试");
}
```

### 5. 验证重定向 URI

```java
// ✅ 始终验证重定向 URI 与配置匹配
String redirectUri = request.getParameter("redirect_uri");
if (!context.getRedirectUri().equals(redirectUri)) {
    throw new SecurityException("无效的重定向 URI");
}
```

### 6. 安全存储令牌

```java
// ✅ 在存储到数据库之前加密令牌
String encryptedToken = crypto.encrypt(claims.getToken().getToken());

User user = new User();
user.setAccessToken(encryptedToken);
user.setRefreshToken(crypto.encrypt(claims.getToken().getRefresh()));
user.setTokenExpiry(LocalDateTime.now().plusSeconds(claims.getToken().getExpireIn()));
```

### 7. 实现令牌刷新

```java
// ✅ 自动刷新过期令牌
if (user.isTokenExpired()) {
    Message refreshMsg = provider.refresh(claims.getToken());
    Authorization newToken = refreshMsg.getData(Authorization.class);

    // 更新存储的令牌
    user.setAccessToken(crypto.encrypt(newToken.getToken()));
    user.setTokenExpiry(LocalDateTime.now().plusSeconds(newToken.getExpireIn()));
}
```

### 8. 处理速率限制

```java
// ✅ 为身份验证端点实现速率限制
@RateLimit(requests = 10, period = 1, timeUnit = TimeUnit.MINUTES)
@PostMapping("/auth/github")
public Message authenticate(@RequestBody Callback callback) {
    return githubProvider.authorize(callback);
}
```

-----

## ❓ 常见问题

### Q1: 如何添加自定义 OAuth 提供程序？

```java
// 方法 1：扩展 AbstractProvider
public class MyProvider extends AbstractProvider {
    public MyProvider(Context context) {
        super(context, new Complex() {
            @Override public String getName() { return "MYPROVIDER"; }
            @Override public Protocol getProtocol() { return Protocol.OIDC; }
            @Override public Class<? extends AbstractProvider> getTargetClass() {
                return MyProvider.class;
            }
            @Override public Map<Endpoint, String> endpoint() {
                // 返回端点映射
            }
        });
    }

    @Override
    public Message token(Callback callback) { /* 实现 */ }

    @Override
    public Message userInfo(Authorization authorization) { /* 实现 */ }
}
```

### Q2: 如何为移动应用处理 PKCE？

```java
// 客户端（移动应用）：
String codeVerifier = Builder.codeVerifier();
String codeChallenge = Builder.codeChallenge("S256", codeVerifier);

// 在授权 URL 中包含 code_challenge

// 服务器端：
// 从回调中提取 code_verifier 并使用它交换令牌
```

### Q3: 如何为 Spring Boot 应用实现"使用 GitHub 登录"？

```java
@Controller
public class AuthController {

    @Autowired
    private Provider githubProvider;

    @GetMapping("/login/github")
    public String loginGithub(HttpSession session) {
        String state = UUID.randomUUID().toString();
        session.setAttribute("oauth_state", state);

        Message message = githubProvider.build(state);
        return "redirect:" + message.getData();
    }

    @GetMapping("/auth/github/callback")
    public String callback(@RequestParam String code,
                          @RequestParam String state,
                          HttpSession session) {
        // 验证状态
        String savedState = (String) session.getAttribute("oauth_state");
        if (!state.equals(savedState)) {
            throw new SecurityException("无效状态");
        }

        // 身份验证
        Callback callback = Callback.builder()
            .code(code)
            .state(state)
            .build();

        Message result = githubProvider.authorize(callback);
        Claims user = result.getData(Claims.class);

        // 创建用户会话
        session.setAttribute("user", user);

        return "redirect:/dashboard";
    }
}
```

### Q4: 如何调试身份验证问题？

```java
// 启用调试日志
logging.level.org.miaixz.bus.auth=DEBUG

// 检查配置
Checker.check(context, Registry.GITHUB);

// 验证回调
Checker.check(Registry.GITHUB, callback);

// 记录令牌响应
Message tokenMsg = provider.token(callback);
log.debug("令牌响应：{}", tokenMsg);
```

### Q5: 如何支持多个重定向 URI？

```java
// 根据请求动态上下文
Context getContextForRequest(HttpServletRequest request) {
    String redirectUri = determineRedirectUri(request);

    return Context.builder()
        .clientId(clientId)
        .clientSecret(clientSecret)
        .redirectUri(redirectUri)
        .build();
}
```

### Q6: 如何实现单点登出？

```java
@PostMapping("/logout")
public Message logout(HttpSession session) {
    Claims user = (Claims) session.getAttribute("user");

    // 撤销令牌
    provider.revoke(user.getToken());

    // 清除会话
    session.invalidate();

    return Message.success("登出成功");
}
```

### Q7: 如何处理令牌过期？

```java
// 在 API 调用前检查令牌过期
public void callApi(Claims user) {
    Authorization token = user.getToken();

    if (isTokenExpired(token)) {
        // 刷新令牌
        Message refreshMsg = provider.refresh(token);
        Authorization newToken = refreshMsg.getData(Authorization.class);
        user.setToken(newToken);
    }

    // 使用新令牌
    callApiWithToken(newToken.getToken());
}
```

### Q8: 支持哪些提供程序？

Bus Auth 支持 **40+** 提供程序，包括：
- 社交：GitHub、Google、Facebook、Twitter、LinkedIn、Microsoft、Apple
- 中国：微信、QQ、微博、抖音、钉钉、飞书
- 企业：Okta、GitLab、Gitee、企业微信
- 更多！查看 `Registry` 枚举获取完整列表。

-----

## 🔄 版本兼容性

| Bus Auth 版本 | JDK 版本 | Spring Boot | 说明 |
| :--- | :--- | :--- | :--- |
| 8.x | 17+ | 3.x | 当前稳定版本 |
| 7.x | 11+ | 2.x | 旧版本 |

-----

## 🚀 路线图

- [ ] 其他 OAuth2 提供程序
- [ ] OpenID Connect 发现支持
- [ ] JWT 令牌验证工具
- [ ] 增强的令牌缓存策略
- [ ] 多租户身份验证支持
- [ ] 身份验证事件钩子
- [ ] 全面的测试覆盖

-----

## 📊 支持的提供程序

### 社交登录
- GitHub、GitLab、Gitee
- Google、Facebook、Twitter、LinkedIn、Microsoft
- Apple、Amazon、Slack、Line、VK
- Stack Overflow、Pinterest、Figma

### 中国平台
- 微信（公众号、开放平台、小程序、企业）
- QQ、微博、抖音、今日头条
- 钉钉、飞书、百度、小米
- 支付宝、淘宝、京东、美团

### 企业
- Okta、GitLab、Coding
- 企业微信
- 阿里云、华为云

### 国内（中国）
- 喜马拉雅、人人、开源中国
- 酷家乐、程序员客栈、小红书
- Teambition、饿了么

完整列表请参见 `Registry` 枚举。

-----

## 🤝 贡献

欢迎贡献！请随时提交拉取请求。

-----

## 📄 许可证

[Apache License Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)

版权所有 (c) 2015-2026 miaixz.org 及其他贡献者。

-----

## 🔗 链接

- [GitHub 仓库](https://github.com/818000/bus)
- [问题追踪](https://github.com/818000/bus/issues)
- [Maven Central](https://central.sonatype.com/artifact/org.miaixz/bus-auth)
