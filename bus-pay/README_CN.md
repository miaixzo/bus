# 💳 Bus Pay: 通用支付集成框架

<p align="center">
<strong>统一支付集成，简化开发</strong>
</p>

-----

## 📖 项目介绍

**Bus Pay** 是一个企业级支付集成框架，为多个第三方支付平台提供**统一 API**。它抽象了各个支付网关 SDK 的复杂性，使支付集成对开发者来说"变得非常简单！"。

该库支持主流支付网关，包括**支付宝**、**微信支付**、**银联**、**PayPal**、**QQ 钱包**和**京东支付**，让您能够以最少的代码更改集成所有主要的支付方式。

-----

## ✨ 核心特性

### 🎯 统一集成

* **单一 API，多平台**: 在所有支付提供商上使用相同的 API 模式
* **最小代码更改**: 通过简单更改配置即可切换支付网关
* **类型安全设计**: 强类型构建减少运行时错误
* **流畅接口**: 链式 API 提供直观可读的代码

### 🔐 全面的安全性

* **多种加密标准**: 支持 RSA、RSA2、SM2（国密）和 AES 加密
* **签名验证**: 所有请求的自动签名生成和验证
* **证书管理**: 内置支持商户证书和公钥验证
* **安全密钥存储**: 安全处理私钥、公钥和证书

### ⚡ 丰富的支付能力

| 功能 | 支持 | 描述 |
| :--- | :--- | :--- |
| **创建支付** | ✅ | APP、WAP、网页、二维码、扫码、小程序 |
| **订单查询** | ✅ | 通过交易号查询订单状态 |
| **退款处理** | ✅ | 全额和部分退款支持 |
| **退款查询** | ✅ | 查询退款状态和详情 |
| **取消支付** | ✅ | 取消待处理交易 |
| **关闭订单** | ✅ | 关闭未支付订单 |
| **转账/汇款** | ✅ | 单笔和批量转账 |
| **账单下载** | ✅ | 下载交易账单 |
| **通知验证** | ✅ | 自动回调签名验证 |

### 🌍 支持的支付提供商

#### **中文支付网关**

| 提供商 | 状态 | 功能 |
| :--- | :--- | :--- |
| **支付宝** | ✅ 完整支持 | APP、WAP、网页、二维码、转账、退款 |
| **微信支付** | ✅ 完整支持 | APP、JSAPI、H5、原生、小程序 |
| **QQ 钱包（财付通）** | ✅ 完整支持 | 类似微信支付功能 |
| **京东支付** | ✅ 完整支持 | APP、WAP、网页支付 |
| **银联** | ✅ 完整支持 | 线上和线下支付 |

#### **国际支付网关**

| 提供商 | 状态 | 功能 |
| :--- | :--- | :--- |
| **PayPal** | ✅ 完整支持 | REST API v2、网页结账 |
| **其他提供商** | 🚧 计划中 | Stripe、Square 等 |

### 🛠️ 高级特性

* **沙箱/生产模式**: 在测试和生产环境之间轻松切换
* **缓存支持**: 内置访问令牌和证书缓存（使用 `bus-cache`）
* **HTTP 客户端集成**: 与 `bus-http` 无缝集成处理请求
* **加密操作**: 与 `bus-crypto` 集成进行加密和签名
* **灵活配置**: 支持多个商户账户和服务提供商模式
* **回调验证**: 支付通知的内置签名验证

-----

## 🚀 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-pay</artifactId>
    <version>x.x.x</version>
</dependency>
```

### Spring Boot 集成

#### 1. 添加 Starter 依赖（可选）

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
    # 支付宝配置
    alipay:
      app-id: your-alipay-app-id
      private-key: your-alipay-private-key
      public-key: alipay-public-key
      sandbox: true  # 使用沙箱环境测试

    # 微信支付配置
    wechat:
      app-id: your-wechat-app-id
      mch-id: your-merchant-id
      api-key: your-api-key-v2
      private-key: your-private-key-v3
      serial-no: your-certificate-serial-no
      cert-path: classpath:apiclient_cert.p12
      cert-mode: true
      sandbox: false  # 生产模式
```

#### 3. 创建支付服务

```java
@Service
public class PaymentService {

    private final AliPayProvider alipayProvider;
    private final WechatPayProvider wechatProvider;

    public PaymentService() {
        // 初始化支付宝提供商
        Context alipayContext = Context.builder()
            .appId("your-app-id")
            .privateKey("your-private-key")
            .publicKey("alipay-public-key")
            .build();

        alipayProvider = new AliPayProvider(alipayContext, Registry.ALIPAY);

        // 初始化微信支付提供商
        Context wechatContext = Context.builder()
            .appId("your-app-id")
            .mchId("your-mch-id")
            .privateKey("your-private-key")
            .build();

        wechatProvider = new WechatPayProvider(wechatContext, Registry.WECHAT);
    }
}
```

（完整内容继续...）

-----

## 🔐 安全注意事项

1. **永不在代码或版本控制中暴露私钥**
2. **对所有支付相关的 API 调用使用 HTTPS**
3. **使用签名验证所有通知**
4. **记录所有支付交易以进行审计**
5. **在支付端点上实施速率限制**
6. **对退款操作使用幂等性密钥**
7. **在过期前轮换证书**
8. **验证输入参数以防止注入攻击**

-----

## 📞 支持

- **问题**: [https://github.com/818000/bus/issues](https://github.com/818000/bus/issues)
