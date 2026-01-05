# 📢 Bus Notify: Multi-Channel Notification Service

<p align="center">
<strong>Unified Notification Service with Load Balancing Support</strong>
</p>

-----

## 📖 Project Introduction

**Bus Notify** is a multi-channel notification service based on Spring Boot, supporting load balancing across different channels. It provides a unified API for sending notifications through various service providers including SMS, email, voice, push notifications, and more.

![](https://img.shields.io/maven-central/v/net.guerlab.sms/guerlab-sms-server-starter.svg)
[![Build Status](https://travis-ci.org/guerlab-net/guerlab-sms.svg?branch=master)](https://travis-ci.org/guerlab-net/guerlab-sms)
![](https://img.shields.io/badge/LICENSE-LGPL--3.0-brightgreen.svg)

-----

## ✨ Core Features

### 🎯 Unified API

- **Simple Integration**: Single API for multiple notification channels
- **Load Balancing**: Automatic distribution across multiple service providers
- **Failover Support**: Automatic fallback to alternative channels
- **Zero Intrusion**: Annotation-based configuration

### 🌍 Supported Channels

#### SMS Providers

| Provider | Type | Status |
|:---|:---|:---:|
| [Aliyun](https://www.aliyun.com/product/sms) | SMS/Voice/Email | [ √ ] |
| [Baidu Cloud](https://cloud.baidu.com/product/sms.html) | SMS | [ √ ] |
| [RongCloud](https://www.yuntongxun.com/sms/note-inform) | SMS | [ √ ] |
| [Tianyi Cloud](https://www.ctyun.cn/products/10020341) | SMS | [ √ ] |
| [Tencent Cloud](https://cloud.tencent.com/product/sms) | SMS | [ √ ] |
| [Huawei Cloud](https://www.huaweicloud.com/product/msgsms.html) | SMS | [ √ ] |
| [JD Cloud](https://www.jdcloud.com/cn/products/text-message) | SMS | [ √ ] |
| [Qiniu Cloud](https://www.qiniu.com/products/sms) | SMS | [ √ ] |
| [Netease IM](https://netease.im/sms) | SMS | [ √ ] |
| [Upyun](https://www.upyun.com/products/sms) | SMS | [ √ ] |
| [Emay](https://www.emay.cn/article949.html) | SMS | [ √ ] |
| [Zhutong](https://www.ztinfo.cn/products/sms) | SMS | [ √ ] |
| [UniSMS](https://unisms.apistd.com/) | SMS | [ √ ] |
| [Yunpian](https://www.yunpian.com/product/domestic-sms) | SMS | [ √ ] |

#### Push Notification Providers

| Provider | Type | Status |
|:---|:---|:---:|
| [WeChat](https://mp.weixin.qq.com/) | Mini Program/Enterprise WeChat/Template Message/WeChat Customer Service | [ √ ] |
| [DingTalk](https://open.dingtalk.com/document/orgapp/api-overview) | Push | [ √ ] |
| [JPush](https://docs.jiguang.cn/jpush) | Push | [ √ ] |

-----

## 🚀 Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-notify</artifactId>
    <version>x.x.x</version>
</dependency>
```

### Configuration

Add the following to your `application.yml`:

```yaml
extend:
  notify:
    # SMS configuration
    sms:
      # Provider list (comma-separated for load balancing)
      providers: aliyun,tencent

      # Aliyun SMS configuration
      aliyun:
        access-key-id: YOUR_ACCESS_KEY_ID
        access-key-secret: YOUR_ACCESS_KEY_SECRET
        sign-name: YOUR_SIGN_NAME
        region-id: cn-hangzhou

      # Tencent Cloud SMS configuration
      tencent:
        secret-id: YOUR_SECRET_ID
        secret-key: YOUR_SECRET_KEY
        region: ap-guangzhou
        app-id: YOUR_APP_ID
```

### Basic Usage

#### Send SMS

```java
@Service
public class NotificationService {

    @Autowired
    private SmsService smsService;

    public void sendNotification(String phoneNumber, String code) {
        SmsRequest request = SmsRequest.builder()
            .phone(phoneNumber)
            .templateId("YOUR_TEMPLATE_ID")
            .params(Arrays.asList(code))
            .build();

        smsService.send(request);
    }
}
```

#### Send Email

```java
@Autowired
private EmailService emailService;

public void sendEmail(String to, String subject, String content) {
    EmailRequest request = EmailRequest.builder()
        .to(to)
        .subject(subject)
        .content(content)
        .build();

    emailService.send(request);
}
```

#### Send Push Notification

```java
@Autowired
private PushService pushService;

public void sendPush(String userId, String message) {
    PushRequest request = PushRequest.builder()
        .userId(userId)
        .title("Notification")
        .content(message)
        .build();

    pushService.send(request);
}
```

-----

## 🔧 Configuration Reference

### Aliyun SMS Configuration

| Property | Required | Description |
|:---|:---:|:---|
| access-key-id | ✓ | Aliyun Access Key ID |
| access-key-secret | ✓ | Aliyun Access Key Secret |
| sign-name | ✓ | SMS signature name |
| region-id | ✗ | Region ID (default: cn-hangzhou) |
| endpoint | ✗ | API endpoint |

### Tencent Cloud SMS Configuration

| Property | Required | Description |
|:---|:---:|:---|
| secret-id | ✓ | Tencent Cloud Secret ID |
| secret-key | ✓ | Tencent Cloud Secret Key |
| region | ✗ | Region (default: ap-guangzhou) |
| app-id | ✓ | SMS application ID |
| sign-name | ✓ | SMS signature name |

### WeChat Configuration

| Property | Required | Description |
|:---|:---:|:---|
| app-id | ✓ | WeChat App ID |
| app-secret | ✓ | WeChat App Secret |
| agent-id | ✓ | Enterprise WeChat Agent ID |

### DingTalk Configuration

| Property | Required | Description |
|:---|:---:|:---|
| app-key | ✓ | DingTalk App Key |
| app-secret | ✓ | DingTalk App Secret |
| agent-id | ✓ | Agent ID |

-----

## 💡 Advanced Features

### Load Balancing

Configure multiple providers for automatic load balancing:

```yaml
extend:
  notify:
    sms:
      providers: aliyun,tencent,huawei
      strategy: round-robin  # round-robin, random, weighted
```

### Fallback Strategy

Configure automatic fallback on failure:

```yaml
extend:
  notify:
    sms:
      providers: aliyun,tencent
      fallback-enabled: true
      max-retries: 3
```

### Template Management

#### SMS Templates

```java
@Service
public class SmsService {

    public void sendVerificationCode(String phone, String code) {
        Map<String, String> params = new HashMap<>();
        params.put("code", code);
        params.put("time", "5");

        smsService.sendTemplate(
            phone,
            "VERIFICATION_CODE_TEMPLATE",
            params
        );
    }
}
```

#### Email Templates

```java
@Autowired
private EmailTemplateService emailTemplateService;

public void sendTemplateEmail(String to, String templateName, Map<String, Object> model) {
    emailTemplateService.sendTemplate(
        to,
        templateName,
        model,
        "text/html"
    );
}
```

### Batch Sending

```java
public void sendBatchNotification(List<String> phones, String message) {
    BatchSmsRequest request = BatchSmsRequest.builder()
        .phones(phones)
        .message(message)
        .build();

    smsService.sendBatch(request);
}
```

-----

## 📊 Monitoring & Logging

### Enable Request Logging

```yaml
logging:
  level:
    org.miaixz.bus.notify: DEBUG
```

### Monitoring Metrics

```java
@Component
public class NotificationMonitor {

    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        // Monitor notification success/failure
        // Record metrics
        // Send alerts
    }
}
```

-----

## 🎯 Use Cases

### Verification Code

```java
@Service
public class VerificationService {

    @Autowired
    private SmsService smsService;

    public void sendVerificationCode(String phone) {
        String code = generateCode();

        smsService.sendTemplate(
            phone,
            "VERIFICATION_CODE",
            Map.of("code", code)
        );

        // Store code with expiration
        redisTemplate.opsForValue().set(
            "verify:" + phone,
            code,
            5,
            TimeUnit.MINUTES
        );
    }
}
```

### Marketing Notifications

```java
@Service
public class MarketingService {

    @Autowired
    private SmsService smsService;

    public void sendPromotion(List<String> phones, String promotion) {
        BatchSmsRequest request = BatchSmsRequest.builder()
            .phones(phones)
            .templateId("PROMOTION_TEMPLATE")
            .params(Map.of("promotion", promotion))
            .build();

        smsService.sendBatch(request);
    }
}
```

### System Alerts

```java
@Service
public class AlertService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private DingTalkService dingTalkService;

    public void sendAlert(String message, AlertLevel level) {
        // Send email
        emailService.send(
            "admin@example.com",
            "System Alert: " + level,
            message
        );

        // Send DingTalk notification
        dingTalkService.send(
            AlertMessage.builder()
                .title("System Alert")
                .text(message)
                .build()
        );
    }
}
```

-----

## 🔒 Security

### API Key Management

Store sensitive credentials securely:

```yaml
extend:
  notify:
    sms:
      aliyun:
        access-key-id: ${ALIYUN_ACCESS_KEY_ID}
        access-key-secret: ${ALIYUN_ACCESS_KEY_SECRET}
```

Use environment variables or secure vaults for production.

### Rate Limiting

Configure rate limits to prevent abuse:

```yaml
extend:
  notify:
    rate-limit:
      enabled: true
      max-requests-per-minute: 100
      max-requests-per-hour: 1000
```

-----

## 🔄 Version Compatibility

| Bus Notify Version | Spring Boot Version | JDK Version |
|:---|:---|:---|
| 8.x | 3.x+ | 17+ |
| 7.x | 2.x+ | 11+ |

-----

## ❓ FAQ

### Q: How to add a new provider?

A: Implement the `SmsProvider` interface and register it as a Spring bean.

### Q: How to handle provider failures?

A: Configure fallback strategies and retry mechanisms in the configuration file.

### Q: Can I use multiple providers simultaneously?

A: Yes, configure multiple providers and enable load balancing.

### Q: How to monitor notification status?

A: Use event listeners or enable logging to track notification delivery.

-----

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

-----

## 📄 License

[LGPL-3.0](LICENSE)

-----

## 🔗 Related Documentation

- [Aliyun SMS Documentation](https://help.aliyun.com/product/44282.html)
- [Tencent Cloud SMS Documentation](https://cloud.tencent.com/document/product/382)
