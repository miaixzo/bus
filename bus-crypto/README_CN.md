# 🔒 Bus Crypto: 企业级加密框架

<p align="center">
<strong>全面的Java加密库,包含安全算法</strong>
</p>

-----

## 📖 项目介绍

**Bus Crypto** 是一个企业级Java加密框架,提供全面的加密、解密、哈希和数字签名功能。基于Java加密架构(JCA)构建并增强Bouncy Castle支持,为所有安全需求提供统一、开发者友好的API。

-----

## ✨ 核心特性

### 🎯 全面的算法支持

* **对称加密**: AES、DES、TDEA (3DES)、SM4、ChaCha20、ZUC、RC4、TEA、维吉尼亚密码
* **非对称加密**: RSA、SM2、ECIES
* **哈希算法**: MD5、SHA-1、SHA-256、SHA-512、SM3
* **消息认证**: HMac-MD5、HMac-SHA1、HMac-SHA256、HMac-SM3
* **密码哈希**: BCrypt、PBKDF2、Argon2
* **数字签名**: RSA、SM2,支持各种摘要算法
* **保留格式加密**: FF1、FF3-1模式
* **一次性密码**: HOTP、TOTP (RFC 4226、RFC 6238)

### ⚡ 高级特性

| 特性 | 描述 |
| :--- | :--- |
| **中国国家标准** | 完整支持SM2/SM3/SM4(国密)算法 |
| **多种模式** | ECB、CBC、CTR、OFB、CFB、GCM等 |
| **灵活填充** | PKCS5Padding、PKCS7Padding、NoPadding、ISO10126Padding |
| **流和块密码** | 同时支持两种加密范式 |
| **Bouncy Castle集成** | 可选BC提供程序支持扩展算法 |
| **线程安全操作** | 在多线程环境中安全并发使用 |

### 🛡️ 安全最佳实践

* **安全随机数生成**: 使用`SecureRandom`生成密钥和IV
* **密钥管理**: 统一的密钥生成和转换工具
* **常量时间比较**: 防止时序攻击
* **内存安全**: 正确处理敏感数据
* **算法灵活性**: 轻松在算法间切换

-----

## 🚀 快速开始

### Maven依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-crypto</artifactId>
    <version>x.x.x</version>
</dependency>
```

### 基本使用示例

#### 对称加密 (AES)

```java
import org.miaixz.bus.crypto.Builder;

// 创建自动生成密钥的AES实例
AES aes = Builder.aes();

// 加密数据
byte[] encrypted = aes.encrypt("Hello World".getBytes());

// 解密数据
byte[] decrypted = aes.decrypt(encrypted);

// 或使用自己的密钥(AES-128/192/256需要16、24或32字节)
byte[] key = "my-secret-key-16-bytes".getBytes();
AES aesCustom = Builder.aes(key);
String encryptedHex = aesCustom.encryptHex("Sensitive Data");
```

#### 非对称加密 (RSA)

```java
import org.miaixz.bus.crypto.Builder;

// 创建自动生成密钥对的RSA实例
RSA rsa = Builder.rsa();

// 获取密钥用于存储/使用
String privateKey = rsa.getPrivateKeyBase64();
String publicKey = rsa.getPublicKeyBase64();

// 使用公钥加密
byte[] encrypted = rsa.encrypt("Secret Message".getBytes());

// 使用私钥解密
byte[] decrypted = rsa.decrypt(encrypted);

// 或使用现有密钥
RSA rsaCustom = Builder.rsa(privateKey, publicKey);
```

#### 哈希

```java
import org.miaixz.bus.crypto.Builder;

// MD5哈希
String md5 = Builder.md5("password");

// SHA-256哈希
String sha256 = Builder.sha256("password");

// SHA-512哈希
String sha512 = Builder.sha512("password");

// SM3哈希(中国标准)
String sm3 = Builder.sm3("password");
```

#### HMAC (消息认证)

```java
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.core.lang.Algorithm;

// HMac-SHA256
byte[] key = "secret-key".getBytes();
HMac hmac = Builder.hmacSha256(key);
String mac = hmac.digestHex("message to authenticate");

// HMac-SM3
HMac hmacSm3 = Builder.hmacSm3(key);
String macSm3 = hmacSm3.digestHex("message");
```

#### 密码哈希 (BCrypt)

```java
import org.miaixz.bus.crypto.Builder;

// 哈希密码
String password = "mySecurePassword123";
String hashed = Builder.hashpw(password);

// 验证密码
boolean isValid = Builder.checkpw(password, hashed);
```

#### SM2/SM3/SM4 (中国国家标准)

```java
import org.miaixz.bus.crypto.Builder;

// SM2非对称加密
SM2 sm2 = Builder.sm2();
byte[] encrypted = sm2.encrypt("data".getBytes());
byte[] decrypted = sm2.decrypt(encrypted);
byte[] signature = sm2.sign("data".getBytes());
boolean verified = sm2.verify("data".getBytes(), signature);

// SM3哈希
String sm3Hash = Builder.sm3("data");

// SM4对称加密
SM4 sm4 = Builder.sm4();
byte[] sm4Encrypted = sm4.encrypt("data".getBytes());
byte[] sm4Decrypted = sm4.decrypt(sm4Encrypted);
```

-----

## 📝 详细使用示例

### 1. 高级对称加密

#### 带CBC模式和IV的AES

```java
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Algorithm.Mode;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.Padding;

// 创建带CBC模式和PKCS5填充的AES
byte[] key = "0123456789abcdef".getBytes();  // 16字节
byte[] iv = "fedcba9876543210".getBytes();    // 16字节

AES aes = new AES(Mode.CBC, Padding.PKCS5Padding, key, iv);

String encrypted = aes.encryptHex("Hello World");
String decrypted = aes.decryptStr(encrypted);
```

#### SM4加密

```java
import org.miaixz.bus.crypto.center.SM4;

// SM4-128-ECB-PKCS5Padding
SM4 sm4 = new SM4();
byte[] encrypted = sm4.encrypt("data".getBytes());

// SM4-CBC,自定义模式和填充
SM4 sm4Cbc = new SM4(
    Algorithm.Mode.CBC,
    Padding.PKCS5Padding,
    key,
    iv
);
```

### 2. 非对称加密和签名

#### RSA加密和解密

```java
import org.miaixz.bus.crypto.center.RSA;

// 生成新密钥对
RSA rsa = new RSA();

// 使用公钥加密,私钥解密
byte[] data = "Secret Message".getBytes();
byte[] encrypted = rsa.encrypt(data, KeyType.PublicKey);
byte[] decrypted = rsa.decrypt(encrypted, KeyType.PrivateKey);

// 使用私钥签名,公钥验证
byte[] signature = rsa.sign(data);
boolean valid = rsa.verify(data, signature);
```

#### SM2 (中国椭圆曲线)

```java
import org.miaixz.bus.crypto.center.SM2;

SM2 sm2 = new SM2();

// 加密(公钥)
byte[] encrypted = sm2.encrypt("data".getBytes());

// 解密(私钥)
byte[] decrypted = sm2.decrypt(encrypted);

// 带自定义ID的签名
byte[] id = "1234567812345678".getBytes();
byte[] signature = sm2.sign("data".getBytes(), id);

// 验证
boolean valid = sm2.verify("data".getBytes(), signature, id);
```

### 3. 哈希和摘要操作

#### 多种哈希算法

```java
import org.miaixz.bus.crypto.Builder;

// MD5 (128位)
String md5 = Builder.md5Hex("data");

// SHA-1 (160位)
String sha1 = Builder.sha1Hex("data");

// SHA-256 (256位)
String sha256 = Builder.sha256Hex("data");

// SHA-512 (512位)
String sha512 = Builder.sha512Hex("data");

// SM3 (中国标准,256位)
String sm3 = Builder.sm3("data");

// 16字符MD5(用于旧系统)
String md5_16 = Builder.md5Hex16("data");
```

#### 哈希文件和流

```java
import java.io.File;
import java.io.FileInputStream;

// 哈希文件
File file = new File("large_file.zip");
String fileMd5 = Builder.md5Hex(file);
String fileSha256 = Builder.sha256Hex(file);

// 哈希输入流
try (FileInputStream fis = new FileInputStream(file)) {
    String streamSha512 = Builder.sha512Hex(fis);
}
```

### 4. HMAC (消息认证码)

#### 用于API认证的HMac

```java
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.center.HMac;

// HMac-SHA256用于API签名
HMac hmac = Builder.hmacSha256("your-secret-key".getBytes());
String apiSignature = hmac.digestHex("timestamp=2024-01-01&user=alice");

// HMac-SM3
HMac hmacSm3 = Builder.hmacSm3("your-secret-key".getBytes());
String signature = hmacSm3.digestHex("data");
```

### 5. 密码哈希

#### 用于安全密码存储的BCrypt

```java
import org.miaixz.bus.crypto.center.BCrypt;

// 使用自动生成盐哈希密码
String hashed = BCrypt.hashpw("password123", BCrypt.gensalt());

// 带特定工作因子哈希(2^12次迭代)
String strongHash = BCrypt.hashpw("password123", BCrypt.gensalt(12));

// 验证密码
boolean matches = BCrypt.checkpw("password123", hashed);
```

#### PBKDF2

```java
import org.miaixz.bus.crypto.center.PBKDF2;

PBKDF2 pbkdf2 = new PBKDF2();

// 从密码派生密钥
char[] password = "myPassword".toCharArray();
byte[] salt = RandomKit.randomBytes(16);
String derivedKey = pbkdf2.encryptHex(password, salt);
```

#### Argon2 (现代密码哈希)

```java
import org.miaixz.bus.crypto.center.Argon2;

Argon2 argon2 = new Argon2();

// 使用Argon2哈希密码
String hash = argon2.hash("password");

// 验证密码
boolean valid = argon2.verify("password", hash);
```

### 6. 数字签名

#### RSA签名

```java
import org.miaixz.bus.crypto.center.Sign;
import org.miaixz.bus.core.lang.Algorithm;

// 创建签名实例
Sign sign = new Sign(Algorithm.SHA256withRSA);

// 签名数据
byte[] signature = sign.sign("important document".getBytes());

// 验证签名
boolean valid = sign.verify("important document".getBytes(), signature);
```

#### SM2签名

```java
import org.miaixz.bus.crypto.center.SM2;

SM2 sm2 = new SM2();

// 使用SM2签名
byte[] data = "important message".getBytes();
byte[] signature = sm2.sign(data);

// 使用SM2验证
boolean valid = sm2.verify(data, signature);
```

### 7. 保留格式加密 (FPE)

#### 加密数字而不改变格式

```java
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.center.FPE;
import org.bouncycastle.crypto.AlphabetMapper;

// 加密信用卡号(保留格式)
AlphabetMapper digitMapper = AlphabetMapper.digitsOnly();
FPE fpe = Builder.fpe(
    FPE.FPEMode.FF1,
    "16-byte-key-12345".getBytes(),
    digitMapper,
    "tweak".getBytes()
);

// "1234567890123456" 变成另一个16位数字
String encrypted = fpe.encrypt("1234567890123456");
String decrypted = fpe.decrypt(encrypted);
```

### 8. 一次性密码 (OTP)

#### HOTP (基于HMAC的一次性密码)

```java
import org.miaixz.bus.crypto.center.HOTP;

HOTP hotp = new HOTP();

// 生成HOTP
String code = hotp.generate("secret-key", 0);  // 计数器 = 0

// 验证HOTP
boolean valid = hotp.verify("secret-key", 0, code);
```

#### TOTP (基于时间的一次性密码)

```java
import org.miaixz.bus.crypto.center.TOTP;

TOTP totp = new TOTP();

// 生成TOTP(30秒有效)
String code = totp.generate("secret-key");

// 验证TOTP
boolean valid = totp.verify("secret-key", code);
```

-----

## 📋 算法参考

### 对称算法

| 算法 | 密钥大小 | 块大小 | 模式 | 说明 |
|:---|:---|:---|:---|:---|
| **AES** | 128/192/256 | 128 | ECB/CBC/CTR/GCM/OFB/CFB | 最常用 |
| **DES** | 56 | 64 | ECB/CBC/CTR | 已弃用,仅用于遗留系统 |
| **TDEA** | 112/168 | 64 | ECB/CBC | 三重DES,遗留系统 |
| **SM4** | 128 | 128 | ECB/CBC/CTR | 中国标准 |
| **ChaCha20** | 256 | N/A (流) | 流密码 | 高性能 |
| **ZUC** | 128/256 | N/A (流) | 流密码 | 中国标准 |
| **RC4** | 40-2048 | N/A (流) | 流密码 | 遗留,不推荐 |
| **TEA** | 128 | 64 | ECB | 简单,快速 |

### 非对称算法

| 算法 | 密钥大小 | 操作 | 说明 |
|:---|:---|:---|:---|
| **RSA** | 512-4096 | 加密/解密/签名/验证 | 最常用 |
| **SM2** | 256 | 加密/解密/签名/验证 | 中国ECC标准 |
| **ECIES** | 可变 | 加密/解密 | 椭圆曲线集成 |

### 摘要算法

| 算法 | 输出大小 | 说明 |
|:---|:---|:---|
| **MD5** | 128位 | 遗留,不安全 |
| **SHA-1** | 160位 | 遗留,不安全 |
| **SHA-256** | 256位 | 推荐 |
| **SHA-512** | 512位 | 高安全性 |
| **SM3** | 256位 | 中国标准 |

### HMAC算法

| 算法 | 输出大小 | 说明 |
|:---|:---|:---|
| **HMac-MD5** | 128位 | 仅用于遗留使用 |
| **HMac-SHA1** | 160位 | 仅用于遗留使用 |
| **HMac-SHA256** | 256位 | 推荐 |
| **HMac-SHA512** | 512位 | 高安全性 |
| **HMac-SM3** | 256位 | 中国标准 |

### 密码哈希

| 算法 | 安全性 | 速度 | 说明 |
|:---|:---|:---|:---|
| **BCrypt** | 高 | 慢 | 推荐 |
| **PBKDF2** | 高 | 慢 | PKCS#5标准 |
| **Argon2** | 很高 | 可调 | PHC 2015优胜者 |

-----

## 💡 最佳实践

### 1. 使用安全的密钥大小

```java
// ✅ 推荐: AES-256
byte[] key256 = RandomKit.randomBytes(32);
AES aes256 = Builder.aes(key256);

// ✅ 良好: AES-128
byte[] key128 = RandomKit.randomBytes(16);
AES aes128 = Builder.aes(key128);

// ❌ 不推荐: 小密钥大小
byte[] key64 = RandomKit.randomBytes(8);  // 太小!
```

### 2. 使用适当的模式

```java
// ✅ 推荐: AES-GCM或带IV的AES-CBC
new AES(Algorithm.Mode.GCM, Padding.NoPadding, key);
new AES(Algorithm.Mode.CBC, Padding.PKCS5Padding, key, iv);

// ⚠️ 谨慎使用: ECB模式(不是语义安全的)
new AES(Algorithm.Mode.ECB, Padding.PKCS5Padding, key);
```

### 3. 安全存储密钥

```java
// ✅ 推荐: 使用KeyStore或环境变量
byte[] key = readKeyFromSecureStore();

// ❌ 不推荐: 在源代码中硬编码密钥
byte[] key = "my-hardcoded-key".getBytes();  // 不要这样做!
```

### 4. 使用适当的算法

```java
// ✅ 推荐: SHA-256或更好
String hash = Builder.sha256("data");

// ⚠️ 仅遗留使用: MD5或SHA-1
String hash = Builder.md5("data");  // 用于签名不安全
```

### 5. 密码哈希

```java
// ✅ 推荐: 使用BCrypt或Argon2
String hashed = BCrypt.hashpw(password, BCrypt.gensalt(12));

// ❌ 不推荐: 纯哈希或快速哈希
String hash = Builder.md5(password);  // 易受彩虹表攻击
```

-----

## ❓ 常见问题

### Q1: 如何在AES和SM4之间选择?

**答**: 根据合规性要求选择:

```java
// 国际标准(最常用)
AES aes = Builder.aes();

// 中国国家标准(在中国必需)
SM4 sm4 = Builder.sm4();
```

**使用AES**用于:
- 全球应用
- 最大兼容性
- 大多数平台上更好的性能

**使用SM4**用于:
- 中国国内应用
- 符合中国密码法规
- 与中国系统集成

### Q2: 为什么加密失败时出现"无效密钥长度"?

**答**: 不同算法有特定的密钥长度要求:

```java
// AES: 16、24或32字节(128/192/256位)
byte[] aesKey = RandomKit.randomBytes(16);  // ✅ 有效

// SM4: 仅16字节(128位)
byte[] sm4Key = RandomKit.randomBytes(16);  // ✅ 有效

// DES: 8字节(56位+奇偶校验)
byte[] desKey = RandomKit.randomBytes(8);   // ✅ 有效
```

### Q3: 如何安全存储加密密钥?

**答**: 永远不要以明文存储密钥。使用以下方法之一:

```java
// 选项1: Java KeyStore
KeyStore keyStore = KeyStore.getInstance("JCEKS");
keyStore.load(new FileInputStream("keystore.jks"), password);

// 选项2: 环境变量
String key = System.getenv("ENCRYPTION_KEY");

// 选项3: 密钥管理服务
String key = SecretsManager.getSecret("encryption-key");
```

### Q4: 何时使用ECB vs CBC vs GCM模式?

**答**: 根据安全要求选择:

```java
// ❌ 避免: ECB(不是语义安全的)
new AES(Algorithm.Mode.ECB, ...);

// ✅ 良好: CBC(需要唯一IV)
new AES(Algorithm.Mode.CBC, Padding.PKCS5Padding, key, iv);

// ✅ 最佳: GCM(提供认证)
new AES(Algorithm.Mode.GCM, Padding.NoPadding, key);
```

### Q5: 如何验证数据完整性?

**答**: 使用HMAC或认证加密:

```java
// 选项1: HMAC
HMac hmac = Builder.hmacSha256(key);
String mac = hmac.digestHex("data");
// 同时发送数据和mac

// 选项2: 认证加密(AES-GCM)
AES aes = new AES(Algorithm.Mode.GCM, Padding.NoPadding, key);
byte[] encrypted = aes.encrypt("data");  // 包含认证标签
```

### Q6: 为什么BCrypt比MD5更适合密码?

**答**: BCrypt专为密码设计:

```java
// ✅ BCrypt: 慢、加盐、自适应
String hash = BCrypt.hashpw(password, BCrypt.gensalt(12));

// ❌ MD5: 快、易受彩虹表攻击
String hash = Builder.md5(password);
```

**BCrypt优势**:
- 内置盐生成
- 计算慢(防止暴力破解)
- 可调整工作因子
- 抗彩虹表攻击

### Q7: 如何从MD5迁移到BCrypt?

**答**: 使用渐进式迁移策略:

```java
// 首先用BCrypt检查密码
if (BCrypt.checkpw(password, bcryptHash)) {
    return true;
}

// 遗留用户回退到MD5
if (Builder.md5(password).equals(legacyMd5Hash)) {
    // 成功登录后用BCrypt重新哈希
    String newHash = BCrypt.hashpw(password, BCrypt.gensalt(12));
    updatePasswordHashInDatabase(userId, newHash);
    return true;
}
```

### Q8: 我可以将Bus Crypto与Spring Security一起使用吗?

**答**: 可以,与Spring Security的密码编码器集成:

```java
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return BCrypt.hashpw(rawPassword.toString(), BCrypt.gensalt(12));
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return BCrypt.checkpw(rawPassword.toString(), encodedPassword);
            }
        };
    }
}
```

-----

## 🔄 版本兼容性

| Bus Crypto版本 | JDK版本 | Bouncy Castle版本 |
|:---|:---|:---|
| 8.x | 17+ | 1.70+ (可选) |
| 7.x | 11+ | 1.60+ (可选) |

-----

## 🔐 安全考虑

### 算法选择

1. **对于新应用**:
   - 使用AES-256进行对称加密
   - 使用RSA-4096或ECC进行非对称加密
   - 使用SHA-256或SHA-512进行哈希
   - 使用BCrypt或Argon2进行密码哈希

2. **对于中国国内应用**:
   - 使用SM2进行非对称加密
   - 使用SM3进行哈希
   - 使用SM4进行对称加密

3. **避免这些算法**:
   - MD5(仅用于非加密哈希)
   - SHA-1(仅用于兼容性)
   - DES(使用AES代替)
   - RC4(已破解的流密码)

### 密钥管理

- **永远不要在源代码中硬编码密钥**
- **使用环境变量**或密钥管理系统
- **定期轮换密钥**
- **为不同用途使用不同密钥**
- **不再需要时销毁密钥**

### 随机数生成

```java
// ✅ 推荐: SecureRandom
SecureRandom secureRandom = new SecureRandom();
byte[] key = new byte[32];
secureRandom.nextBytes(key);

// ❌ 不推荐: java.util.Random
Random random = new Random();
byte[] key = new byte[32];
random.nextBytes(key);  // 可预测!
```

-----

## 📚 其他资源

- [Java加密架构(JCA)](https://docs.oracle.com/javase/8/docs/technotes/guides/security/crypto/CryptoSpec.html)
- [Bouncy Castle文档](https://www.bouncycastle.org/documentation.html)
- [NIST加密标准](https://csrc.nist.gov/projects/cryptographic-standards-and-guidelines)
- [中国国家密码标准(GM/T)](https://www.oscca.gov.cn/)

-----

## 🤝 贡献

欢迎贡献!请确保:

1. 所有测试通过
2. 代码遵循项目风格指南
3. 仔细考虑安全影响
4. 文档已更新
