# 🔐 Bus Auth: Enterprise-Grade Authentication & Authorization Framework

<p align="center">
<strong>Unified Authentication and Authorization Solution Supporting Multiple Protocols and Identity Providers</strong>
</p>

-----

## 📖 Project Introduction

**Bus Auth** is an enterprise-grade authentication and authorization framework designed to simplify integration with third-party identity providers. It provides a unified API for implementing OAuth2, SAML, LDAP, and custom authentication protocols, supporting **40+** mainstream platforms worldwide.

The framework abstracts away protocol complexities, allowing developers to focus on business logic rather than authentication implementation details. Whether it's social logins, enterprise SSO, or custom identity providers, Bus Auth provides a consistent, type-safe, and extensible approach.

-----

## ✨ Core Features

### 🎯 Unified Authentication Interface

* **Protocol Agnostic**: Single API for OAuth2, SAML, LDAP, and custom protocols
* **Provider Abstraction**: Consistent interface across 40+ identity providers
* **Builder Pattern**: Fluent API for configuring authentication flows
* **Type Safety**: Strongly typed configuration and response objects

### 🔐 Security First

| Feature | Description |
| :--- | :--- |
| **PKCE Support** | RFC 7636 compliant Proof Key for Code Exchange for mobile/spa apps |
| **State Validation** | Built-in CSRF protection with state parameter validation |
| **Token Management** | Secure token storage, refresh, and revocation |
| **Signature Verification** | HMAC-SHA256 signature support for OAuth1.0a |
| **Cache Integration** | Distributed state caching support |

### 🌍 Platform Coverage

**Social Platforms** (15+)
- GitHub, Google, Facebook, Twitter, LinkedIn, Microsoft
- WeChat, QQ, Weibo, Douyin, TikTok
- Apple, Amazon, Slack, Line, VK

**Enterprise Platforms** (10+)
- DingTalk, Feishu, Lark, WeChat Work
- Okta, GitLab, Gitee, Teambition
- Huawei, Aliyun, Baidu Cloud

**E-Commerce** (8+)
- Alipay, Taobao, JD, Meituan, Eleme
- Kujiale, Xiaomi, RedNote

**Domestic Platforms** (China)
- Ximalaya, Renren, OSChina, Coding, Proginn
- Stack Overflow, Pinterest, Figma

### ⚡ Developer Experience

* **Zero Boilerplate**: Minimal code required for authentication
* **Auto Configuration**: Convention over configuration with sensible defaults
* **Flexible Scoping**: Fine-grained permission control via OAuth scopes
* **Rich Metadata**: Comprehensive user profile data from providers
* **Extensible**: Easy to add custom providers or extend existing ones

-----

## 🚀 Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-auth</artifactId>
    <version>8.x.x</version>
</dependency>
```

### Basic Usage

#### 1. Configure Authentication Context

```java
// Create authentication context
Context context = Context.builder()
    .clientId("your_client_id")
    .clientSecret("your_client_secret")
    .redirectUri("https://yourapp.com/callback")
    .scopes(Arrays.asList("user", "repo"))
    .build();
```

#### 2. Create Authentication Provider

```java
// Method 1: Direct instantiation
Provider github = new GithubProvider(context);

// Method 2: Using Authorizer builder (recommended)
Provider github = Authorizer.builder()
    .source("GITHUB")
    .context(context)
    .build();
```

#### 3. Generate Authorization URL

```java
// Generate state parameter for CSRF protection
String state = UUID.randomUUID().toString();

// Get authorization URL
Message message = github.build(state);
String authUrl = message.getData();

// Redirect user to authUrl
// After authentication, user will be redirected to redirectUri with code and state
```

#### 4. Handle Callback and Login

```java
// Extract callback parameters
Callback callback = Callback.builder()
    .code(request.getParameter("code"))
    .state(request.getParameter("state"))
    .build();

// Perform authentication
Message result = github.authorize(callback);

if (result.isSuccess()) {
    Claims claims = result.getData(Claims.class);
    String uuid = claims.getUuid();
    String username = claims.getUsername();
    String email = claims.getEmail();

    // Log user in or create account
    // Store claims.getToken() for future API calls
}
```

-----

## 📝 Usage Examples

### 1. GitHub OAuth2 Authentication

```java
// Configuration
Context context = Context.builder()
    .clientId("github_client_id")
    .clientSecret("github_client_secret")
    .redirectUri("http://localhost:8080/auth/github/callback")
    .build();

Provider github = new GithubProvider(context);

// Step 1: Redirect to GitHub
@GetMapping("/auth/github")
public void githubLogin(HttpServletResponse response) throws IOException {
    String state = UUID.randomUUID().toString();
    cache.set(state, "true", 10, TimeUnit.MINUTES); // Store state in cache

    Message message = github.build(state);
    response.sendRedirect(message.getData());
}

// Step 2: Handle callback
@GetMapping("/auth/github/callback")
public Message githubCallback(@RequestParam String code, @RequestParam String state) {
    Callback callback = Callback.builder()
        .code(code)
        .state(state)
        .build();

    Message result = github.authorize(callback);

    if (result.isSuccess()) {
        Claims user = result.getData(Claims.class);
        // Process user information
        return Message.success(user);
    }
    return Message.error("Authentication failed");
}
```

### 2. WeChat Work (Enterprise WeChat) Authentication

```java
// WeChat Work requires additional agentId
Context context = Context.builder()
    .clientId("corp_id")
    .clientSecret("corp_secret")
    .unionId("agent_id")
    .redirectUri("https://yourapp.com/callback/wechat")
    .build();

Provider wechatWork = new WeChatEeWebProvider(context);

// Authentication flow is the same as GitHub
Message result = wechatWork.authorize(callback);
Claims user = result.getData(Claims.class);
```

### 3. PKCE Mode for Mobile/SPA Applications

```java
// Enable PKCE mode
Context context = Context.builder()
    .clientId("client_id")
    .clientSecret("")  // No client secret for public clients
    .redirectUri("myapp://callback")
    .pkce(true)  // Enable PKCE
    .build();

Provider google = new GoogleProvider(context);

// Generate code verifier and challenge
String codeVerifier = Builder.codeVerifier();
String codeChallenge = Builder.codeChallenge("S256", codeVerifier);

// Store codeVerifier for later use
cache.set(state, codeVerifier, 10, TimeUnit.MINUTES);

// Include code_challenge in authorization URL
```

### 4. Custom OAuth2 Provider

```java
// Method 1: Extend AbstractProvider
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
        // Custom token exchange logic
    }

    @Override
    public Message userInfo(Authorization authorization) {
        // Custom user info retrieval
    }
}

// Method 2: Use Registry with custom Complex
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
        // Return endpoint mappings
    }
};

Provider provider = Authorizer.builder()
    .source("CUSTOM")
    .context(context)
    .complex(customComplex)
    .build();
```

### 5. Token Management

```java
// Get access token
Message tokenMsg = provider.token(callback);
Authorization token = tokenMsg.getData(Authorization.class);

// Access token details
String accessToken = token.getToken();
int expiresIn = token.getExpireIn();
String refreshToken = token.getRefresh();

// Refresh token (if supported)
Message refreshMsg = provider.refresh(token);
Authorization newToken = refreshMsg.getData(Authorization.class);

// Revoke authorization (logout)
Message revokeMsg = provider.revoke(token);
```

### 6. Multiple Provider Support

```java
// Provider registry
Map<String, Provider> providers = new HashMap<>();

// Configure multiple providers
providers.put("github", new GithubProvider(githubContext));
providers.put("google", new GoogleProvider(googleContext));
providers.put("wechat", new WeChatMpProvider(wechatContext));

// Unified authentication endpoint
@PostMapping("/auth/{provider}")
public Message authenticate(@PathVariable String provider,
                            @RequestBody Callback callback) {
    Provider authProvider = providers.get(provider);
    if (authProvider == null) {
        return Message.error("Unsupported provider: " + provider);
    }

    return authProvider.authorize(callback);
}
```

### 7. Custom Cache Implementation

```java
// Use Redis for distributed state caching
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

// Use custom cache
RedisAuthCache cache = new RedisAuthCache(redisTemplate);
Provider github = new GithubProvider(context, cache);
```

### 8. Dynamic Endpoint Configuration

```java
// Override default endpoints
Map<Endpoint, String> customEndpoints = new HashMap<>();
customEndpoints.put(Endpoint.AUTHORIZE, "https://custom.auth.com/authorize");
customEndpoints.put(Endpoint.TOKEN, "https://custom.auth.com/token");
customEndpoints.put(Endpoint.USERINFO, "https://custom.auth.com/userinfo");
customEndpoints.put(Endpoint.REFRESH, "https://custom.auth.com/refresh");

Context context = Context.builder()
    .clientId("client_id")
    .clientSecret("client_secret")
    .redirectUri("https://yourapp.com/callback")
    .endpoint(customEndpoints)  // Custom endpoints
    .build();
```

-----

## 📋 Configuration Reference

### Context Parameters

| Parameter | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `clientId` | String | ✅ | OAuth2 client ID or API key |
| `clientSecret` | String | ✅ | OAuth2 client secret |
| `unionId` | String | ❌ | Platform-specific identifier (e.g., WeChat agentId) |
| `extId` | String | ❌ | Extended identifier |
| `deviceId` | String | ❌ | Device ID for some platforms |
| `type` | String | ❌ | Platform-specific type |
| `flag` | boolean | ❌ | Platform-specific flag |
| `pkce` | boolean | ❌ | Enable PKCE mode (default: false) |
| `prefix` | String | ❌ | Domain prefix (for Okta, Coding) |
| `redirectUri` | String | ✅ | OAuth2 callback URL |
| `scopes` | List<String> | ❌ | OAuth2 scopes (permissions) |
| `ignoreState` | boolean | ❌ | Skip state validation (not recommended) |
| `ignoreRedirectUri` | boolean | ❌ | Skip redirect URI validation |
| `kid` | String | ❌ | Apple Key ID |
| `teamId` | String | ❌ | Apple Team ID |
| `loginType` | String | ❌ | WeChat Work login type |
| `lang` | String | ❌ | Language code (default: zh) |
| `extension` | String | ❌ | Extension properties |
| `endpoint` | Map<Endpoint, String> | ❌ | Custom OAuth endpoints |

### Supported Endpoints

| Endpoint | Description |
| :--- | :--- |
| `AUTHORIZE` | Authorization endpoint URL |
| `TOKEN` | Token endpoint URL |
| `USERINFO` | User information endpoint URL |
| `REFRESH` | Token refresh endpoint URL |
| `REVOKE` | Token revocation endpoint URL |

### Provider Registry

All built-in providers are registered in the `Registry` enum:

```java
// Access registry
Registry.GITHUB
Registry.GOOGLE
Registry.WECHAT_MP
Registry.DINGTALK
// ... 40+ providers

// Use with Authorizer
Provider provider = Authorizer.builder()
    .source(Registry.GITHUB.getName())
    .context(context)
    .build();
```

-----

## 🔧 Advanced Configuration

### 1. Custom Scope Configuration

```java
// Default scopes are provided by each provider
// You can customize scopes for specific needs

Context context = Context.builder()
    .clientId("client_id")
    .clientSecret("client_secret")
    .redirectUri("https://yourapp.com/callback")
    .scopes(Arrays.asList("read", "write", "email"))  // Custom scopes
    .build();
```

### 2. State Validation

```java
// State validation is enabled by default
// To disable (not recommended for production):

Context context = Context.builder()
    .clientId("client_id")
    .clientSecret("client_secret")
    .redirectUri("https://yourapp.com/callback")
    .ignoreState(true)  // ⚠️ Disable state validation
    .build();
```

### 3. Custom User Info Mapping

```java
// Extend AbstractProvider to customize user info parsing

public class CustomGithubProvider extends GithubProvider {

    @Override
    public Message userInfo(Authorization authorization) {
        // Get raw user data
        Message response = super.userInfo(authorization);

        // Custom mapping logic
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

### 4. Error Handling

```java
try {
    Message result = provider.authorize(callback);

    if (result.isSuccess()) {
        Claims user = result.getData(Claims.class);
        // Success handling
    } else {
        // Error handling
        String errorCode = result.getErrcode();
        String errorMsg = result.getErrmsg();
    }
} catch (AuthorizedException e) {
    // Handle authentication exceptions
    log.error("Authentication failed", e);

    // Common error codes:
    // 110001 - Unsupported provider or invalid configuration
    // 110002 - Incomplete configuration
    // 110005 - Invalid redirect URI
    // 110007 - Missing authorization code
    // 110008 - Invalid or expired state
}
```

-----

## 💡 Best Practices

### 1. Always Use HTTPS

```java
// ❌ Not recommended
Context context = Context.builder()
    .redirectUri("http://yourapp.com/callback")
    .build();

// ✅ Recommended
Context context = Context.builder()
    .redirectUri("https://yourapp.com/callback")
    .build();
```

### 2. Enable State Validation

```java
// ✅ Always enable state validation in production
Context context = Context.builder()
    .ignoreState(false)  // Default: false
    .build();
```

### 3. Use Distributed Cache

```java
// ✅ Use Redis or other distributed cache for state
RedisAuthCache cache = new RedisAuthCache(redisTemplate);
Provider provider = new GithubProvider(context, cache);
```

### 4. Implement Proper Error Handling

```java
// ✅ Comprehensive error handling
try {
    Message result = provider.authorize(callback);
    // Process result
} catch (AuthorizedException e) {
    // Log error details
    log.error("Auth failed: code={}, message={}",
        e.getCode(), e.getMessage());

    // Return user-friendly error message
    return Message.error("Login failed, please try again");
}
```

### 5. Validate Redirect URI

```java
// ✅ Always validate redirect URI matches configuration
String redirectUri = request.getParameter("redirect_uri");
if (!context.getRedirectUri().equals(redirectUri)) {
    throw new SecurityException("Invalid redirect URI");
}
```

### 6. Store Tokens Securely

```java
// ✅ Encrypt tokens before storing in database
String encryptedToken = crypto.encrypt(claims.getToken().getToken());

User user = new User();
user.setAccessToken(encryptedToken);
user.setRefreshToken(crypto.encrypt(claims.getToken().getRefresh()));
user.setTokenExpiry(LocalDateTime.now().plusSeconds(claims.getToken().getExpireIn()));
```

### 7. Implement Token Refresh

```java
// ✅ Automatically refresh expired tokens
if (user.isTokenExpired()) {
    Message refreshMsg = provider.refresh(claims.getToken());
    Authorization newToken = refreshMsg.getData(Authorization.class);

    // Update stored token
    user.setAccessToken(crypto.encrypt(newToken.getToken()));
    user.setTokenExpiry(LocalDateTime.now().plusSeconds(newToken.getExpireIn()));
}
```

### 8. Handle Rate Limiting

```java
// ✅ Implement rate limiting for auth endpoints
@RateLimit(requests = 10, period = 1, timeUnit = TimeUnit.MINUTES)
@PostMapping("/auth/github")
public Message authenticate(@RequestBody Callback callback) {
    return githubProvider.authorize(callback);
}
```

-----

## ❓ Frequently Asked Questions

### Q1: How do I add a custom OAuth provider?

```java
// Method 1: Extend AbstractProvider
public class MyProvider extends AbstractProvider {
    public MyProvider(Context context) {
        super(context, new Complex() {
            @Override public String getName() { return "MYPROVIDER"; }
            @Override public Protocol getProtocol() { return Protocol.OIDC; }
            @Override public Class<? extends AbstractProvider> getTargetClass() {
                return MyProvider.class;
            }
            @Override public Map<Endpoint, String> endpoint() {
                // Return endpoint mappings
            }
        });
    }

    @Override
    public Message token(Callback callback) { /* implementation */ }

    @Override
    public Message userInfo(Authorization authorization) { /* implementation */ }
}
```

### Q2: How do I handle PKCE for mobile apps?

```java
// Client-side (mobile app):
String codeVerifier = Builder.codeVerifier();
String codeChallenge = Builder.codeChallenge("S256", codeVerifier);

// Include code_challenge in authorization URL

// Server-side:
// Extract code_verifier from callback and use it to exchange token
```

### Q3: How do I implement "Login with GitHub" for a Spring Boot app?

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
        // Validate state
        String savedState = (String) session.getAttribute("oauth_state");
        if (!state.equals(savedState)) {
            throw new SecurityException("Invalid state");
        }

        // Authenticate
        Callback callback = Callback.builder()
            .code(code)
            .state(state)
            .build();

        Message result = githubProvider.authorize(callback);
        Claims user = result.getData(Claims.class);

        // Create user session
        session.setAttribute("user", user);

        return "redirect:/dashboard";
    }
}
```

### Q4: How do I debug authentication issues?

```java
// Enable debug logging
logging.level.org.miaixz.bus.auth=DEBUG

// Check configuration
Checker.check(context, Registry.GITHUB);

// Validate callback
Checker.check(Registry.GITHUB, callback);

// Log token response
Message tokenMsg = provider.token(callback);
log.debug("Token response: {}", tokenMsg);
```

### Q5: How do I support multiple redirect URIs?

```java
// Dynamic context based on request
Context getContextForRequest(HttpServletRequest request) {
    String redirectUri = determineRedirectUri(request);

    return Context.builder()
        .clientId(clientId)
        .clientSecret(clientSecret)
        .redirectUri(redirectUri)
        .build();
}
```

### Q6: How do I implement single sign-out?

```java
@PostMapping("/logout")
public Message logout(HttpSession session) {
    Claims user = (Claims) session.getAttribute("user");

    // Revoke token
    provider.revoke(user.getToken());

    // Clear session
    session.invalidate();

    return Message.success("Logged out successfully");
}
```

### Q7: How do I handle token expiration?

```java
// Check token expiry before API calls
public void callApi(Claims user) {
    Authorization token = user.getToken();

    if (isTokenExpired(token)) {
        // Refresh token
        Message refreshMsg = provider.refresh(token);
        Authorization newToken = refreshMsg.getData(Authorization.class);
        user.setToken(newToken);
    }

    // Use fresh token
    callApiWithToken(newToken.getToken());
}
```

### Q8: Which providers are supported?

Bus Auth supports **40+** providers including:
- Social: GitHub, Google, Facebook, Twitter, LinkedIn, Microsoft, Apple
- Chinese: WeChat, QQ, Weibo, Douyin, DingTalk, Feishu
- Enterprise: Okta, GitLab, Gitee, WeChat Work
- And many more! See `Registry` enum for complete list.

-----

## 🔄 Version Compatibility

| Bus Auth Version | JDK Version | Spring Boot | Notes |
| :--- | :--- | :--- | :--- |
| 8.x | 17+ | 3.x | Current stable version |
| 7.x | 11+ | 2.x | Legacy version |

-----

## 🚀 Roadmap

- [ ] Additional OAuth2 providers
- [ ] OpenID Connect Discovery support
- [ ] JWT token validation utilities
- [ ] Enhanced token caching strategies
- [ ] Multi-tenant authentication support
- [ ] Authentication event hooks
- [ ] Comprehensive test coverage

-----

## 📊 Supported Providers

### Social Login
- GitHub, GitLab, Gitee
- Google, Facebook, Twitter, LinkedIn, Microsoft
- Apple, Amazon, Slack, Line, VK
- Stack Overflow, Pinterest, Figma

### Chinese Platforms
- WeChat (MP, Open, Mini, Work)
- QQ, Weibo, Douyin, Toutiao
- DingTalk, Feishu, Baidu, Xiaomi
- Alipay, Taobao, JD, Meituan

### Enterprise
- Okta, GitLab, Coding
- WeChat Work (Enterprise WeChat)
- Aliyun, Huawei Cloud

### Domestic (China)
- Ximalaya, Renren, OSChina
- Kujiale, Proginn, RedNote
- Teambition, Eleme

For complete list, see the `Registry` enum.

-----

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

-----

## 📄 License

[The MIT License (MIT)](https://github.com/818000/bus/blob/main/LICENSE)

Copyright (c) 2015-2026 miaixz.org and other contributors.

-----

## 🔗 Links

- [GitHub Repository](https://github.com/818000/bus)
- [Issue Tracker](https://github.com/818000/bus/issues)
- [Maven Central](https://central.sonatype.com/artifact/org.miaixz/bus-auth)
