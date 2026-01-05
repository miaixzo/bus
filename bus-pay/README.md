# 💳 Bus Pay: Universal Payment Integration Framework

<p align="center">
<strong>Unified Payment Integration, Simplified Development</strong>
</p>

-----

## 📖 Project Introduction

**Bus Pay** is an enterprise-level payment integration framework that provides a **unified API** for multiple third-party payment platforms. It abstracts away the complexities of individual payment gateway SDKs, making payment integration "So easy!" for developers.

This library supports mainstream payment gateways including **Alipay**, **WeChat Pay**, **UnionPay**, **PayPal**, **QQ Pay**, and **JD Pay**, enabling you to integrate all major payment methods with minimal code changes.

-----

## ✨ Core Features

### 🎯 Unified Integration

* **Single API, Multiple Platforms**: Use the same API pattern across all payment providers
* **Minimal Code Changes**: Switch between payment gateways by simply changing configuration
* **Type-Safe Design**: Built with strong typing to reduce runtime errors
* **Fluent Interface**: Chain-style API for intuitive and readable code

### 🔐 Comprehensive Security

* **Multiple Encryption Standards**: Supports RSA, RSA2, SM2 (Chinese national standard), and AES encryption
* **Signature Verification**: Automatic signature generation and verification for all requests
* **Certificate Management**: Built-in support for merchant certificates and public key verification
* **Secure Key Storage**: Secure handling of private keys, public keys, and certificates

### ⚡ Rich Payment Capabilities

| Feature | Support | Description |
| :--- | :--- | :--- |
| **Payment Creation** | ✅ | APP, WAP, Web, QR Code, Scan, Mini Program |
| **Order Query** | ✅ | Query order status by transaction ID |
| **Refund Processing** | ✅ | Full and partial refund support |
| **Refund Query** | ✅ | Query refund status and details |
| **Cancel Payment** | ✅ | Cancel pending transactions |
| **Close Order** | ✅ | Close unpaid orders |
| **Transfer/Remit** | ✅ | Single and batch transfers |
| **Bill Download** | ✅ | Download transaction statements |
| **Notify Verification** | ✅ | Automatic callback signature verification |

### 🌍 Supported Payment Providers

#### **Chinese Payment Gateways**

| Provider | Status | Features |
| :--- | :--- | :--- |
| **Alipay** | ✅ Full Support | APP, WAP, Web, QR, Transfer, Refund |
| **WeChat Pay** | ✅ Full Support | APP, JSAPI, H5, Native, Mini Program |
| **QQ Pay (Tenpay)** | ✅ Full Support | Similar to WeChat Pay features |
| **JD Pay** | ✅ Full Support | APP, WAP, Web payment |
| **UnionPay** | ✅ Full Support | Online and offline payment |

#### **International Payment Gateways**

| Provider | Status | Features |
| :--- | :--- | :--- |
| **PayPal** | ✅ Full Support | REST API v2, Web checkout |
| **Other Providers** | 🚧 Roadmap | Stripe, Square, etc. |

### 🛠️ Advanced Features

* **Sandbox/Production Mode**: Easy switching between test and production environments
* **Cache Support**: Built-in caching for access tokens and certificates (using `bus-cache`)
* **HTTP Client Integration**: Seamless integration with `bus-http` for request handling
* **Crypto Operations**: Integration with `bus-crypto` for encryption and signing
* **Flexible Configuration**: Support for multiple merchant accounts and service provider mode
* **Callback Verification**: Built-in signature verification for payment notifications

-----

## 🚀 Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-pay</artifactId>
    <version>x.x.x</version>
</dependency>
```

### Spring Boot Integration

#### 1. Add Starter Dependency (Optional)

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-starter</artifactId>
    <version>x.x.x</version>
</dependency>
```

#### 2. Configure Payment Provider

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

#### 3. Create Payment Service

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

## 📝 Usage Examples

### 1. Alipay Payment

#### Create Payment Order (Web/WAP)

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

#### Query Order Status

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

#### Refund Payment

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

#### Verify Payment Notification

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

### 2. WeChat Pay

#### Create Native Payment (QR Code)

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

#### Create JSAPI Payment (WeChat Browser)

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

#### Query Order Status

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

#### Refund Payment

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

#### Verify Payment Notification

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

### 3. UnionPay Payment

#### Create Payment

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

### 4. PayPal Payment

#### Create Order

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

## 🔧 Configuration Reference

### Context Configuration

| Parameter | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `appId` | String | ✅ | Application ID (appid on each platform) |
| `appKey` | String | ❌ | API key or apiKey (platform-specific) |
| `appSecret` | String | ❌ | Application secret (appSecret) |
| `privateKey` | String | ❌ | Merchant private key (for signing) |
| `publicKey` | String | ❌ | Platform public key (for verification) |
| `mchId` | String | ❌ | Merchant ID (WeChat Pay, UnionPay) |
| `partnerKey` | String | ❌ | Partner key (service provider mode) |
| `p12` | String | ❌ | P12 certificate content |
| `certPath` | String | ❌ | Certificate file path |
| `certMode` | String | ❌ | Whether to use certificate mode |
| `domain` | String | ❌ | Application domain for callbacks |
| `exParams` | Object | ❌ | Extra parameters |

### Registry Enum (Payment Platforms)

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

## 💡 Best Practices

### 1. Store Sensitive Information Securely

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

### 2. Use Sandbox Environment for Testing

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

### 3. Implement Idempotency for Payment Notifications

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

### 4. Use Database Transactions for Payment Processing

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

### 5. Handle Network Failures Gracefully

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

### 6. Validate Input Parameters

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

### 7. Use Asynchronous Notification

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

## ❓ Frequently Asked Questions

### Q1: How do I switch between sandbox and production environments?

```java
// Method 1: Using Registry enum
Complex complex = Registry.ALIPAY;
complex.setSandbox(true);  // Sandbox
complex.setSandbox(false); // Production

// Method 2: Directly override URLs
AliPayProvider provider = new AliPayProvider(context, complex);
provider.setCustomGatewayUrl("https://your-custom-gateway.com");
```

### Q2: How to handle multiple payment methods in one application?

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

### Q3: What should I do if signature verification fails?

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

### Q4: How to query refund status?

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

### Q5: How to handle different currency types?

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

### Q6: How to configure service provider mode (ISV)?

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

### Q7: How to download and reconcile bills?

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

### Q8: Common error codes and solutions

| Error Code | Description | Solution |
| :--- | :--- | :--- |
| `INVALID_APP_ID` | Invalid application ID | Check appId configuration |
| `INVALID_SIGNATURE` | Signature verification failed | Verify privateKey/publicKey |
| `OUT_TRADE_NO_USED` | Order number already used | Use unique order numbers |
| `NOT_ENOUGH_BALANCE` | Insufficient balance | Check account balance |
| `TRADE_CLOSED` | Order closed | Recreate payment |
| `SYSTEM_ERROR` | System error | Retry the request |

-----

## 🔄 Version Compatibility

| Bus Pay Version | JDK Version | Spring Boot Version |
| :--- | :--- | :--- |
| 8.x | 17+ | 3.x+ |
| 7.x | 11+ | 2.x+ |

-----

## 📚 API Method Reference

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

## 🔐 Security Notes

1. **Never expose private keys** in code or version control
2. **Use HTTPS** for all payment-related API calls
3. **Verify all notifications** using signature verification
4. **Log all payment transactions** for auditing
5. **Implement rate limiting** on payment endpoints
6. **Use idempotency keys** for refund operations
7. **Rotate certificates** before expiration
8. **Validate input parameters** to prevent injection attacks

-----

## 📞 Support

- **Issues**: [https://github.com/818000/bus/issues](https://github.com/818000/bus/issues)
