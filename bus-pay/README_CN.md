# 💳 Bus Pay：通用支付集成框架

<p align="center">
<strong>统一支付集成，简化开发</strong>
</p>

-----

## 📖 项目介绍

**Bus Pay**是企业级支付集成框架，提供**统一API**
第三方支付平台。它抽象化了单个支付网关 SDK 的复杂性，使支付变得简单
集成“如此简单！”对于开发人员来说。

该库支持主流支付网关包括**Alipay**、**微信支付**、**银联**、**PayPal**、 **QQ
Pay** 和 **JD Pay**，使您能够以最少的代码更改集成所有主要支付方式。

-----

## ✨ 核心功能

### 🎯 统一集成

* **单一 API，多平台**：在所有支付提供商中使用相同的 API 模式
* **最少的代码更改**：只需更改配置即可在支付网关之间切换
* **类型安全设计**：采用强类型构建，以减少运行时错误
* **Fluent Interface**：链式API，代码直观易读

### 🔐 全面的安全性

* **多种加密标准**：支持RSA、RSA2、SM2（国标）、AES加密
* **签名验证**：所有请求自动签名生成和验证
* **证书管理**：内置支持商户证书和公钥验证
* **安全密钥存储**：私钥、公钥和证书的安全处理

### ⚡ 丰富的支付能力

| 功能 | 支持 | 描述 |
|:------------------------|:--------|:-------------------------------------------|
| **支付创建** | ✅ | APP、WAP、Web、二维码、扫描、小程序 |
| **订单查询** | ✅ | 通过交易ID查询订单状态 |
| **退款处理** | ✅ | 支持全额和部分退款 |
| **退款查询** | ✅ | 查询退款状态及详情 |
| **取消付款** | ✅ | 取消待处理交易 |
| **关闭订单** | ✅ | 关闭未付款订单 |
| **转账/汇款** | ✅ | 单笔及批量转账 |
| **账单下载** | ✅ | 下载交易报表 |
| **通知验证** | ✅ | 自动回调签名验证 |

### 🌍 支持的支付提供商

#### **中国支付网关**

| 提供商 | 状态 | 功能 |
|:--------------------|:----------------|:-------------------------------------|
| **支付宝** | ✅ 全面支持| APP、WAP、Web、二维码、转账、退款 |
| **微信支付** | ✅ 全面支持| APP、JSAPI、H5、原生、小程序|
| **QQ支付（财付通）** | ✅ 全面支持 | 类似微信支付功能 |
| **JD Pay** | ✅ 全面支持| APP、WAP、网络支付 |
| **银联** | ✅全面支持|线上线下支付 |

#### **国际支付网关**

| 提供商 | 状态 | 功能 |
|:--------------------|:----------------|:--------------------------|
| **PayPal** | ✅ 完全支持 | REST API v2，Web 结账 |
| **其他供应商** | 🚧 路线图 | 条纹、方形等 |

### 🛠️ 高级功能

* **沙箱/生产模式**：测试和生产环境之间轻松切换
* **Cache 支持**：访问令牌和证书的内置缓存（使用 `bus-cache`）
* **HTTP 客户端集成**：与 `bus-fabric` 无缝集成以进行请求处理
* **加密操作**：与`bus-crypto`集成以进行加密和签名
* **灵活配置**：支持多商户账户和服务商模式
* **回调验证**：支付通知内置签名验证

-----

## 🚀 快速入门

### Maven 依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-pay</artifactId>
    <version>x.x.x</version>
</dependency>
```

### Spring Boot 集成

#### 1. 添加 Starter 依赖项（可选）

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-starter</artifactId>
    <version>x.x.x</version>
</dependency>
```

#### 2. 配置支付提供商

```yaml
# application.yml
bus:
  pay:
    # Alipay Configuration
    alipay:
      app-id: your-alipay-app-id
      private-key: your-alipay-private-key
      public-key: alipay-public-key
      sandbox: true  # Use sandbox environment for testing

    # WeChat Pay Configuration
    wechat:
      app-id: your-wechat-app-id
      mch-id: your-merchant-id
      api-key: your-api-key-v2
      private-key: your-private-key-v3
      serial-no: your-certificate-serial-no
      cert-path: classpath:apiclient_cert.p12
      cert-mode: true
      sandbox: false  # Production mode
```

#### 3. 创建支付服务

```java
@Service
public class PaymentService {

    private final AliPayProvider alipayProvider;
    private final WechatPayProvider wechatProvider;

    public PaymentService() {
        // Initialize Alipay provider
        Context alipayContext = Context.builder()
            .appId("your-app-id")
            .privateKey("your-private-key")
            .publicKey("alipay-public-key")
            .build();

        alipayProvider = new AliPayProvider(alipayContext, Registry.ALIPAY);

        // Initialize WeChat Pay provider
        Context wechatContext = Context.builder()
            .appId("your-app-id")
            .mchId("your-mch-id")
            .privateKey("your-private-key")
            .build();

        wechatProvider = new WechatPayProvider(wechatContext, Registry.WECHAT);
    }
}
```

-----

## 📝 使用示例

### 1. 支付宝支付

#### 创建付款订单（Web/WAP）

```java
// Build payment parameters
Map<String, String> model = new HashMap<>();
model.put("out_trade_no", "ORDER_" + System.currentTimeMillis());
model.put("total_amount", "99.99");
model.put("subject", "Premium Subscription");
model.put("body", "Monthly premium subscription");

// Create WAP payment
String payForm = alipayProvider.wapPay(
    model,
    "https://your-site.com/payment/return",  // Return URL
    "https://your-site.com/payment/notify"   // Notify URL
);

// Return HTML form to browser for auto-submission
return payForm;
```

#### 查询订单状态

```java
Map<String, String> model = new HashMap<>();
model.put("out_trade_no", "ORDER_1234567890");

Map<String, Object> result = alipayProvider.tradeQuery(model);

if ("TRADE_SUCCESS".equals(result.get("trade_status"))) {
    // Payment successful
    String tradeNo = (String) result.get("trade_no");
    // Handle success logic
}
```

#### 退款

```java
Map<String, String> model = new HashMap<>();
model.put("out_trade_no", "ORDER_1234567890");
model.put("refund_amount", "50.00");
model.put("refund_reason", "Customer request");
model.put("out_request_no", "REFUND_" + System.currentTimeMillis());

Map<String, Object> result = alipayProvider.tradeRefund(model);

if ("10000".equals(result.get("code"))) {
    // Refund successful
}
```

#### 验证付款通知

```java
@PostMapping("/payment/notify")
public String handleAlipayNotify(HttpServletRequest request) {
    Map<String, String> params = getRequestParamMap(request);

    // Verify signature
    boolean verified = alipayProvider.verifyNotify(params);

    if (verified && "TRADE_SUCCESS".equals(params.get("trade_status"))) {
        String outTradeNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");
        String totalAmount = params.get("total_amount");

        // Update order status in database
        orderService.paymentSuccess(outTradeNo, tradeNo, totalAmount);

        return "success";
    }

    return "fail";
}
```

### 2. 微信支付

#### 创建原生支付（二维码）

```java
// Build payment parameters
Map<String, String> model = new HashMap<>();
model.put("out_trade_no", "ORDER_" + System.currentTimeMillis());
model.put("description", "Premium Subscription");
model.put("total_fee", "9999");  // Amount in cents (99.99 yuan)
model.put("spbill_create_ip", getClientIp());

// Create Native payment
Map<String, Object> result = wechatPay.nativePay(model);

String codeUrl = (String) result.get("code_url");

// Generate QR code for customer to scan
String qrCode = QrCodeKit.generate(codeUrl);
return qrCode;
```

#### 创建JSAPI支付（微信浏览器）

```java
Map<String, String> model = new HashMap<>();
model.put("out_trade_no", "ORDER_" + System.currentTimeMillis());
model.put("description", "Premium Subscription");
model.put("total_fee", "9999");
model.put("spbill_create_ip", getClientIp());
model.put("openid", getUserOpenId());  // User's OpenID

// Create JSAPI payment
Map<String, Object> result = wechatPay.jsapiPay(model);

String prepayId = (String) result.get("prepay_id");

// Generate frontend payment parameters
Map<String, String> payParams = wechatPay.getPayParams(prepayId);
return payParams;  // Return to frontend for WeChat Pay SDK
```

#### 查询订单状态

```java
Map<String, String> model = new HashMap<>();
model.put("out_trade_no", "ORDER_1234567890");

Map<String, Object> result = wechatPay.orderQuery(model);

if ("SUCCESS".equals(result.get("trade_state"))) {
    // Payment successful
    String transactionId = (String) result.get("transaction_id");
    // Handle success logic
}
```

#### 退款

```java
Map<String, String> model = new HashMap<>();
model.put("out_trade_no", "ORDER_1234567890");
model.put("out_refund_no", "REFUND_" + System.currentTimeMillis());
model.put("total_fee", "9999");
model.put("refund_fee", "5000");  // Refund 50 yuan

Map<String, Object> result = wechatPay.refund(model);

if ("SUCCESS".equals(result.get("return_code")) &&
    "SUCCESS".equals(result.get("result_code"))) {
    // Refund successful
}
```

#### 验证付款通知

```java
@PostMapping("/payment/notify")
public String handleWechatNotify(HttpServletRequest request) {
    String xml = IoKit.read(request.getInputStream(), Charset.UTF_8);
    Map<String, String> params = XmlKit.toMap(xml);

    // Verify signature
    boolean verified = wechatProvider.verifyNotify(params);

    if (verified && "SUCCESS".equals(params.get("return_code")) &&
        "SUCCESS".equals(params.get("result_code"))) {
        String outTradeNo = params.get("out_trade_no");
        String transactionId = params.get("transaction_id");
        String totalFee = params.get("total_fee");

        // Update order status in database
        orderService.paymentSuccess(outTradeNo, transactionId, totalFee);

        // Return XML response
        return "<xml><return_code><![CDATA[SUCCESS]]></return_code></xml>";
    }

    return "<xml><return_code><![CDATA[FAIL]]></return_code></xml>";
}
```

### 3. 银联支付

#### 创建付款

```java
Context unionpayContext = Context.builder()
    .appId("your-mer-id")
    .privateKey("your-cert-password")
    .certPath("classpath:acp_test_sign.pfx")
    .publicKey("acp_test_verify_sign.cer")
    .build();

UnionPayProvider unionpayProvider = new UnionPayProvider(unionpayContext, Registry.UNIONPAY);

Map<String, String> model = new HashMap<>();
model.put("orderId", "ORDER_" + System.currentTimeMillis());
model.put("txnAmt", "9999");  // Amount in cents
model.put("txnTime", DateKit.format(new Date(), "yyyyMMddHHmmss"));

String formHtml = unionpayProvider.createPayForm(model);
return formHtml;  // Return auto-submit form
```

### 4. PayPal 支付

#### 创建订单

```java
Context paypalContext = Context.builder()
    .appId("your-client-id")
    .appSecret("your-client-secret")
    .sandbox(true)
    .build();

PaypalProvider paypalProvider = new PaypalProvider(paypalContext, Registry.PAYPAL);

Map<String, String> model = new HashMap<>();
model.put("intent", "CAPTURE");
model.put("purchase_units[0].amount.currency_code", "USD");
model.put("purchase_units[0].amount.value", "99.99");

Map<String, Object> result = paypalProvider.createOrder(model);

String approveUrl = (String) result.get("approve_link");
// Redirect user to approveUrl for payment approval
```

-----

## 🔧 配置参考

### 上下文配置

| 参数 | 类型 | 必需 | 描述 |
|:-------------|:-------|:---------|:----------------------------------------|
| `appId` | String | ✅ | 应用程序 ID（各平台上的 appid） |
| `appKey` | 字符串 | ❌ | API 密钥或 apiKey（特定于平台） |
| `appSecret` | 字符串 | ❌ | 应用程序机密 (appSecret) |
| `privateKey` | String | ❌ | 商户私钥（签名用） |
| `publicKey` | String | ❌ | 平台公钥（验证用） |
| `mchId` | String | ❌ | 商户 ID（微信支付、银联） |
| `partnerKey` | 字符串 | ❌ | 合作伙伴密钥（服务提供商模式） |
| `p12` | 字符串 | ❌ | P12 证书内容 |
| `certPath` | 字符串 | ❌ | 证书文件路径 |
| `certMode` | String | ❌ | 是否使用证书模式 |
| `domain` | String | ❌ | 回调应用域 |
| `exParams` | 对象 | ❌ | 额外参数 |

### 注册表枚举（支付平台）

```java
public enum Registry implements Complex {
    ALIPAY,     // Alipay
    WECHAT,     // WeChat Pay
    TENPAY,     // QQ Pay
    JDPAY,      // JD Pay
    UNIONPAY,   // UnionPay
    PAYPAL      // PayPal
}
```

-----

## 💡 最佳实践

### 1. 安全存储敏感信息

```java
// ✅ Recommended: Use environment variables or secret management
Context context = Context.builder()
    .appId(env.get("ALIPAY_APP_ID"))
    .privateKey(env.get("ALIPAY_PRIVATE_KEY"))
    .publicKey(env.get("ALIPAY_PUBLIC_KEY"))
    .build();

// ❌ Not Recommended: Hardcode secrets
Context context = Context.builder()
    .appId("2021001234567890")
    .privateKey("MIIEvQIBADANBgkqhkiG9w0BAQE...")
    .build();
```

### 2. 使用沙箱环境进行测试

```java
// ✅ Test in sandbox first
Complex complex = Registry.ALIPAY;
complex.setSandbox(true);

AliPayProvider provider = new AliPayProvider(context, complex);

// Test with small amounts
model.put("total_amount", "0.01");

// After testing, switch to production
complex.setSandbox(false);
```

### 3. 实现付款通知的幂等性

```java
@PostMapping("/payment/notify")
public String handleNotify(HttpServletRequest request) {
    String outTradeNo = params.get("out_trade_no");

    // Check if order already processed
    if (orderService.isPaymentProcessed(outTradeNo)) {
        return "success";  // Already processed
    }

    // Process payment
    orderService.processPayment(outTradeNo, params);
    return "success";
}
```

### 4. 使用数据库事务进行支付处理

```java
@Transactional
public void handlePaymentSuccess(String outTradeNo, Map<String, String> params) {
    // 1. Update order status
    Order order = orderDao.findByOutTradeNo(outTradeNo);
    order.setStatus(OrderStatus.PAID);
    order.setTransactionId(params.get("trade_no"));
    orderDao.update(order);

    // 2. Add user credits/subscriptions
    userService.addPremium(order.getUserId());

    // 3. Record payment log
    paymentLogDao.insert(params);
}
```

### 5. 优雅地处理网络故障

```java
try {
    Map<String, Object> result = alipayProvider.tradeQuery(model);
    // Process result
} catch (PaymentException e) {
    // Log error
    logger.error("Payment query failed: {}", e.getMessage());

    // Retry logic
    if (retryCount < MAX_RETRY) {
        return retryPaymentQuery(model, retryCount + 1);
    }

    // Fallback to manual processing
    return manualVerificationRequired(outTradeNo);
}
```

### 6. 验证输入参数

```java
private void validatePaymentRequest(Map<String, String> model) {
    Assert.notNull(model.get("out_trade_no"), "Order number is required");
    Assert.notNull(model.get("total_amount"), "Amount is required");

    BigDecimal amount = new BigDecimal(model.get("total_amount"));
    Assert.isTrue(amount.compareTo(BigDecimal.ZERO) > 0, "Amount must be greater than zero");
    Assert.isTrue(amount.compareTo(new BigDecimal("100000")) < 0, "Amount exceeds limit");

    // Check if order already exists
    String outTradeNo = model.get("out_trade_no");
    Assert.isFalse(orderDao.exists(outTradeNo), "Order already exists");
}
```

### 7. 使用异步通知

```java
// For time-sensitive operations, use message queue
@Async
public void processPaymentNotification(Map<String, String> params) {
    // Send to message queue for background processing
    mqClient.send("payment-notify", params);
}

// Consumer
@RabbitListener(queues = "payment-notify")
public void handlePaymentNotify(Map<String, String> params) {
    // Process payment asynchronously
    orderService.processPayment(params);
}
```

-----

## ❓ 常见问题

### Q1：如何在沙箱和生产环境之间切换？

```java
// Method 1: Using Registry enum
Complex complex = Registry.ALIPAY;
complex.setSandbox(true);  // Sandbox
complex.setSandbox(false); // Production

// Method 2: Directly override URLs
AliPayProvider provider = new AliPayProvider(context, complex);
provider.setCustomGatewayUrl("https://your-custom-gateway.com");
```

### Q2：如何在一个应用程序中处理多种支付方式？

```java
@Service
public class PaymentService {

    private final Map<Registry, Provider> providers = new HashMap<>();

    @PostConstruct
    public void init() {
        providers.put(Registry.ALIPAY, alipayProvider);
        providers.put(Registry.WECHAT, wechatProvider);
        providers.put(Registry.PAYPAL, paypalProvider);
    }

    public String createPayment(Registry provider, Map<String, String> params) {
        Provider provider = providers.get(provider);
        return provider.createPayment(params);
    }
}
```

### Q3：签名验证失败怎么办？

```java
try {
    boolean verified = provider.verifyNotify(params);
    if (!verified) {
        // Log suspicious activity
        logger.warn("Signature verification failed: {}", params);

        // Do not process payment
        return "fail";
    }
} catch (Exception e) {
    // Check if keys/certs are correctly configured
    logger.error("Signature verification error: {}", e.getMessage());
    return "fail";
}
```

### Q4：如何查询退款状态？

```java
// Alipay
Map<String, String> model = new HashMap<>();
model.put("out_request_no", "REFUND_1234567890");
model.put("out_trade_no", "ORDER_1234567890");

Map<String, Object> result = alipayProvider.tradeRefundQuery(model);

// WeChat Pay
Map<String, String> model = new HashMap<>();
model.put("out_refund_no", "REFUND_1234567890");

Map<String, Object> result = wechatPay.refundQuery(model);
```

### Q5：如何处理不同的币种？

```java
// Alipay supports multiple currencies
Map<String, String> model = new HashMap<>();
model.put("total_amount", "99.99");
model.put("currency", "USD");  // USD, EUR, GBP, etc.

// PayPal explicitly sets currency
Map<String, String> model = new HashMap<>();
model.put("purchase_units[0].amount.currency_code", "EUR");
model.put("purchase_units[0].amount.value", "99.99");
```

### Q6：如何配置服务提供商模式（ISV）？

```java
Context context = Context.builder()
    .appId("service-provider-appid")
    .mchId("service-provider-mch-id")
    .partnerKey("service-provider-key")
    // Sub-merchant info
    .slAppId("sub-merchant-appid")
    .slMchId("sub-merchant-mch-id")
    .build();

WechatPayProvider provider = new WechatPayProvider(context, Registry.WECHAT);
```

### Q7：如何下载和核对账单？

```java
// Alipay - Download bill
Map<String, String> model = new HashMap<>();
model.put("bill_type", "trade");
model.put("bill_date", "2023-10-01");

String billUrl = alipayProvider.dataBillDownloadUrl(model);

// WeChat Pay - Download bill
Map<String, String> model = new HashMap<>();
model.put("bill_date", "20231001");
model.put("bill_type", "ALL");

String billContent = wechatPay.downloadBill(model);
```

### Q8：常见错误代码及解决方法

| 错误代码 | 描述 | 解决方案 |
|:---------------------|:------------------------------|:----------------------------|
| `INVALID_APP_ID` | 无效的应用 ID | 检查 appId 配置 |
| `INVALID_SIGNATURE` | 签名验证失败 | 验证私钥/公钥 |
| `OUT_TRADE_NO_USED` | 订单号已使用 | 使用唯一订单号 |
| `NOT_ENOUGH_BALANCE` | 余额不足 | 查看账户余额 |
| `TRADE_CLOSED` | 订单已关闭 | 重新创建付款 |
| `SYSTEM_ERROR` | 系统错误 | 重试请求 |

-----

## 🔄 版本兼容性

| 公交付费版本 | JDK 版本 | Spring Boot 版本 |
|:----------------|:------------|:--------------------|
| 8.x | 17+ | 3.x+ |
| 7.x | 11+ | 2.x+ |

-----

## 📚 API 方法参考

### AlipayProvider

```java
// Payment methods
String appPay(Map<String, String> model, String notifyUrl)
String wapPay(Map<String, String> model, String returnUrl, String notifyUrl)
Map<String, Object> tradePay(Map<String, String> model, String notifyUrl)
String pagePay(Map<String, String> model, String returnUrl, String notifyUrl)
String qrPay(Map<String, String> model, String notifyUrl)

// Query methods
Map<String, Object> tradeQuery(Map<String, String> model)
Map<String, Object> transferQuery(Map<String, String> model)

// Refund methods
Map<String, Object> tradeRefund(Map<String, String> model)
Map<String, Object> tradeRefundQuery(Map<String, String> model)

// Order management
Map<String, Object> tradeCancel(Map<String, String> model)
Map<String, Object> tradeClose(Map<String, String> model)

// Transfer methods
Map<String, Object> transfer(Map<String, String> model)
Map<String, Object> transferQuery(Map<String, String> model)

// Signature verification
boolean verifyNotify(Map<String, String> params)
```

### WechatPayProvider

```java
// Payment methods (v2)
Map<String, Object> jsapiPay(Map<String, String> model)
Map<String, Object> nativePay(Map<String, String> model)
Map<String, Object> h5Pay(Map<String, String> model)
Map<String, Object> appPay(Map<String, String> model)

// Payment methods (v3)
Map<String, Object> jsapiPayV3(Map<String, String> model)
Map<String, Object> nativePayV3(Map<String, String> model)
Map<String, Object> h5PayV3(Map<String, String> model)

// Query methods
Map<String, Object> orderQuery(Map<String, String> model)

// Refund methods
Map<String, Object> refund(Map<String, String> model)
Map<String, Object> refundQuery(Map<String, String> model)

// Order management
Map<String, Object> closeOrder(Map<String, String> model)

// Signature verification
boolean verifyNotify(Map<String, String> params)
```

-----

## 🔐 安全说明

1. **切勿在代码或版本控制中暴露私钥**
2. ** 所有支付相关的API调用均使用HTTPS**
3. **使用签名验证来验证所有通知**
4. **记录所有支付交易**以供审计
5、**对支付端点实施限速**
6、**使用幂等密钥**进行退款操作
7. ** 到期前轮换证书**
8、**验证输入参数**，防止注入攻击

-----

## 📞 支持

- **问题**：[https://github.com/818000/bus/issues](https://github.com/818000/bus/issues)
