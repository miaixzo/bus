# 📢 Bus Notify: 多渠道通知服务

<p align="center">
<strong>支持负载均衡的统一通知服务</strong>
</p>

-----

## 📖 项目介绍

**Bus Notify** 是基于 Spring Boot 的多渠道通知服务,支持不同渠道之间的负载均衡。它为通过短信、邮件、语音、推送通知等各种服务提供商发送通知提供统一的
API。

-----

## ✨ 核心特性

### 🎯 统一 API

- **简单集成**: 多渠道通知的单一 API
- **负载均衡**: 在多个服务提供商之间自动分配
- **故障转移支持**: 自动回退到备用渠道
- **零侵入**: 基于注解的配置

### 🌍 支持的渠道

#### 短信提供商

| 提供商                                                     | 类型           | 状态  |
|:-----------------------------------------------------------|:---------------|:-----:|
| [阿里云](https://www.aliyun.com/product/sms)               | 短信/语音/邮件 | [ √ ] |
| [百度云](https://cloud.baidu.com/product/sms.html)         | 短信           | [ √ ] |
| [容联云](https://www.yuntongxun.com/sms/note-inform)       | 短信           | [ √ ] |
| [天翼云](https://www.ctyun.cn/products/10020341)           | 短信           | [ √ ] |
| [腾讯云](https://cloud.tencent.com/product/sms)            | 短信           | [ √ ] |
| [华为云](https://www.huaweicloud.com/product/msgsms.html)  | 短信           | [ √ ] |
| [京东云](https://www.jdcloud.com/cn/products/text-message) | 短信           | [ √ ] |
| [七牛云](https://www.qiniu.com/products/sms)               | 短信           | [ √ ] |
| [网易云信](https://netease.im/sms)                         | 短信           | [ √ ] |
| [又拍云](https://www.upyun.com/products/sms)               | 短信           | [ √ ] |
| [亿美软通](https://www.emay.cn/article949.html)            | 短信           | [ √ ] |
| [助通](https://www.ztinfo.cn/products/sms)                 | 短信           | [ √ ] |
| [UniSMS](https://unisms.apistd.com/)                       | 短信           | [ √ ] |
| [云片](https://www.yunpian.com/product/domestic-sms)       | 短信           | [ √ ] |

#### 推送通知提供商

| 提供商                                                         | 类型                              | 状态  |
|:---------------------------------------------------------------|:----------------------------------|:-----:|
| [微信](https://mp.weixin.qq.com/)                              | 小程序/企业微信/模板消息/微信客服 | [ √ ] |
| [钉钉](https://open.dingtalk.com/document/orgapp/api-overview) | 推送                              | [ √ ] |
| [极光推送](https://docs.jiguang.cn/jpush)                      | 推送                              | [ √ ] |

-----

## 🚀 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-notify</artifactId>
    <version>x.x.x</version>
</dependency>
```

### 配置

在您的 `application.yml` 中添加以下内容:

```yaml
extend:
  notify:
    # 短信配置
    sms:
      # 提供商列表(逗号分隔,用于负载均衡)
      providers: aliyun,tencent

      # 阿里云短信配置
      aliyun:
        access-key-id: YOUR_ACCESS_KEY_ID
        access-key-secret: YOUR_ACCESS_KEY_SECRET
        sign-name: YOUR_SIGN_NAME
        region-id: cn-hangzhou

      # 腾讯云短信配置
      tencent:
        secret-id: YOUR_SECRET_ID
        secret-key: YOUR_SECRET_KEY
        region: ap-guangzhou
        app-id: YOUR_APP_ID
```

### 基本用法

#### 发送短信

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

#### 发送邮件

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

#### 发送推送通知

```java
@Autowired
private PushService pushService;

public void sendPush(String userId, String message) {
    PushRequest request = PushRequest.builder()
        .userId(userId)
        .title("通知")
        .content(message)
        .build();

    pushService.send(request);
}
```

-----

## 🔧 配置参考

### 阿里云短信配置

| 属性              | 必需 | 描述                       |
|:------------------|:----:|:---------------------------|
| access-key-id     |  ✓  | 阿里云访问密钥 ID          |
| access-key-secret |  ✓  | 阿里云访问密钥密码         |
| sign-name         |  ✓  | 短信签名名称               |
| region-id         |  ✗  | 区域 ID(默认: cn-hangzhou) |
| endpoint          |  ✗  | API 端点                   |

### 腾讯云短信配置

| 属性       | 必需 | 描述                     |
|:-----------|:----:|:-------------------------|
| secret-id  |  ✓  | 腾讯云密钥 ID            |
| secret-key |  ✓  | 腾讯云密钥密码           |
| region     |  ✗  | 区域(默认: ap-guangzhou) |
| app-id     |  ✓  | 短信应用 ID              |
| sign-name  |  ✓  | 短信签名名称             |

### 微信配置

| 属性       | 必需 | 描述            |
|:-----------|:----:|:----------------|
| app-id     |  ✓  | 微信应用 ID     |
| app-secret |  ✓  | 微信应用密码    |
| agent-id   |  ✓  | 企业微信代理 ID |

### 钉钉配置

| 属性       | 必需 | 描述         |
|:-----------|:----:|:-------------|
| app-key    |  ✓  | 钉钉应用密钥 |
| app-secret |  ✓  | 钉钉应用密码 |
| agent-id   |  ✓  | 代理 ID      |

-----

## 💡 高级功能

### 负载均衡

配置多个提供商以实现自动负载均衡:

```yaml
extend:
  notify:
    sms:
      providers: aliyun,tencent,huawei
      strategy: round-robin  # round-robin, random, weighted
```

### 故障转移策略

配置失败时的自动故障转移:

```yaml
extend:
  notify:
    sms:
      providers: aliyun,tencent
      fallback-enabled: true
      max-retries: 3
```

### 模板管理

#### 短信模板

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

#### 邮件模板

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

### 批量发送

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

## 📊 监控与日志

### 启用请求日志

```yaml
logging:
  level:
    org.miaixz.bus.notify: DEBUG
```

### 监控指标

```java
@Component
public class NotificationMonitor {

    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        // 监控通知成功/失败
        // 记录指标
        // 发送警报
    }
}
```

-----

## 🎯 使用场景

### 验证码

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

        // 存储验证码及过期时间
        redisTemplate.opsForValue().set(
            "verify:" + phone,
            code,
            5,
            TimeUnit.MINUTES
        );
    }
}
```

### 营销通知

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

### 系统警报

```java
@Service
public class AlertService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private DingTalkService dingTalkService;

    public void sendAlert(String message, AlertLevel level) {
        // 发送邮件
        emailService.send(
            "admin@example.com",
            "系统警报: " + level,
            message
        );

        // 发送钉钉通知
        dingTalkService.send(
            AlertMessage.builder()
                .title("系统警报")
                .text(message)
                .build()
        );
    }
}
```

-----

## 🔒 安全

### API 密钥管理

安全存储敏感凭据:

```yaml
extend:
  notify:
    sms:
      aliyun:
        access-key-id: ${ALIYUN_ACCESS_KEY_ID}
        access-key-secret: ${ALIYUN_ACCESS_KEY_SECRET}
```

在生产环境中使用环境变量或安全保管库。

### 限流

配置限流以防止滥用:

```yaml
extend:
  notify:
    rate-limit:
      enabled: true
      max-requests-per-minute: 100
      max-requests-per-hour: 1000
```

-----

## 🔄 版本兼容性

| Bus Notify 版本 | Spring Boot 版本 | JDK 版本 |
|:----------------|:-----------------|:---------|
| 8.x             | 3.x+             | 17+      |
| 7.x             | 2.x+             | 11+      |

-----

## ❓ FAQ

### Q: 如何添加新提供商?

A: 实现 `SmsProvider` 接口并将其注册为 Spring bean。

### Q: 如何处理提供商故障?

A: 在配置文件中配置故障转移策略和重试机制。

### Q: 可以同时使用多个提供商吗?

A: 可以,配置多个提供商并启用负载均衡。

### Q: 如何监控通知状态?

A: 使用事件监听器或启用日志来跟踪通知传递。

-----

## 🤝 贡献

欢迎贡献!请随时提交拉取请求。

-----

## 📄 许可证

[LGPL-3.0](LICENSE)

-----

## 🔗 相关文档

- [阿里云短信文档](https://help.aliyun.com/product/44282.html)
- [腾讯云短信文档](https://cloud.tencent.com/document/product/382)

-----

**由 Miaixz 团队用 ❤️ 构建**
