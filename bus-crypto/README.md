# 🔒 Bus Crypto: Enterprise-Grade Cryptographic Framework

<p align="center">
<strong>Comprehensive Java Cryptography Library with Secure Algorithms</strong>
</p>

-----

## 📖 Project Introduction

**Bus Crypto** is an enterprise-grade cryptographic framework for Java, providing comprehensive encryption, decryption, hashing, and digital signature capabilities. Built on top of the Java Cryptography Architecture (JCA) and enhanced with Bouncy Castle, it offers a unified, developer-friendly API for all your security needs.

-----

## ✨ Core Features

### 🎯 Comprehensive Algorithm Support

* **Symmetric Encryption**: AES, DES, TDEA (3DES), SM4, ChaCha20, ZUC, RC4, TEA, Vigenère
* **Asymmetric Encryption**: RSA, SM2, ECIES
* **Hash Algorithms**: MD5, SHA-1, SHA-256, SHA-512, SM3
* **Message Authentication**: HMac-MD5, HMac-SHA1, HMac-SHA256, HMac-SM3
* **Password Hashing**: BCrypt, PBKDF2, Argon2
* **Digital Signatures**: RSA, SM2 with various digest algorithms
* **Format-Preserving Encryption**: FF1, FF3-1 modes
* **One-Time Password**: HOTP, TOTP (RFC 4226, RFC 6238)

### ⚡ Advanced Features

| Feature | Description |
| :--- | :--- |
| **Chinese National Standards** | Full support for SM2/SM3/SM4 (国密) algorithms |
| **Multiple Modes** | ECB, CBC, CTR, OFB, CFB, GCM, etc. |
| **Flexible Padding** | PKCS5Padding, PKCS7Padding, NoPadding, ISO10126Padding |
| **Stream & Block Ciphers** | Support for both encryption paradigms |
| **Bouncy Castle Integration** | Optional BC provider for extended algorithms |
| **Thread-Safe Operations** | Safe for concurrent use in multi-threaded environments |

### 🛡️ Security Best Practices

* **Secure Random Generation**: Uses `SecureRandom` for key and IV generation
* **Key Management**: Unified key generation and conversion utilities
* **Constant-Time Comparison**: Protection against timing attacks
* **Memory Safety**: Proper handling of sensitive data
* **Algorithm Agility**: Easy switching between algorithms

-----

## 🚀 Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-crypto</artifactId>
    <version>x.x.x</version>
</dependency>
```

### Basic Usage Examples

#### Symmetric Encryption (AES)

```java
import org.miaixz.bus.crypto.Builder;

// Create AES instance with auto-generated key
AES aes = Builder.aes();

// Encrypt data
byte[] encrypted = aes.encrypt("Hello World".getBytes());

// Decrypt data
byte[] decrypted = aes.decrypt(encrypted);

// Or use with your own key (16, 24, or 32 bytes for AES-128/192/256)
byte[] key = "my-secret-key-16-bytes".getBytes();
AES aesCustom = Builder.aes(key);
String encryptedHex = aesCustom.encryptHex("Sensitive Data");
```

#### Asymmetric Encryption (RSA)

```java
import org.miaixz.bus.crypto.Builder;

// Create RSA instance with auto-generated key pair
RSA rsa = Builder.rsa();

// Get keys for storage/use
String privateKey = rsa.getPrivateKeyBase64();
String publicKey = rsa.getPublicKeyBase64();

// Encrypt with public key
byte[] encrypted = rsa.encrypt("Secret Message".getBytes());

// Decrypt with private key
byte[] decrypted = rsa.decrypt(encrypted);

// Or use existing keys
RSA rsaCustom = Builder.rsa(privateKey, publicKey);
```

#### Hashing

```java
import org.miaixz.bus.crypto.Builder;

// MD5 hash
String md5 = Builder.md5("password");

// SHA-256 hash
String sha256 = Builder.sha256("password");

// SHA-512 hash
String sha512 = Builder.sha512("password");

// SM3 hash (Chinese standard)
String sm3 = Builder.sm3("password");
```

#### HMAC (Message Authentication)

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

#### Password Hashing (BCrypt)

```java
import org.miaixz.bus.crypto.Builder;

// Hash a password
String password = "mySecurePassword123";
String hashed = Builder.hashpw(password);

// Verify a password
boolean isValid = Builder.checkpw(password, hashed);
```

#### SM2/SM3/SM4 (Chinese National Standards)

```java
import org.miaixz.bus.crypto.Builder;

// SM2 asymmetric encryption
SM2 sm2 = Builder.sm2();
byte[] encrypted = sm2.encrypt("data".getBytes());
byte[] decrypted = sm2.decrypt(encrypted);
byte[] signature = sm2.sign("data".getBytes());
boolean verified = sm2.verify("data".getBytes(), signature);

// SM3 hash
String sm3Hash = Builder.sm3("data");

// SM4 symmetric encryption
SM4 sm4 = Builder.sm4();
byte[] sm4Encrypted = sm4.encrypt("data".getBytes());
byte[] sm4Decrypted = sm4.decrypt(sm4Encrypted);
```

-----

## 📝 Detailed Usage Examples

### 1. Advanced Symmetric Encryption

#### AES with CBC Mode and IV

```java
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Algorithm.Mode;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.Padding;

// Create AES with CBC mode and PKCS5 padding
byte[] key = "0123456789abcdef".getBytes();  // 16 bytes
byte[] iv = "fedcba9876543210".getBytes();    // 16 bytes

AES aes = new AES(Mode.CBC, Padding.PKCS5Padding, key, iv);

String encrypted = aes.encryptHex("Hello World");
String decrypted = aes.decryptStr(encrypted);
```

#### SM4 Encryption

```java
import org.miaixz.bus.crypto.center.SM4;

// SM4-128-ECB-PKCS5Padding
SM4 sm4 = new SM4();
byte[] encrypted = sm4.encrypt("data".getBytes());

// SM4-CBC with custom mode and padding
SM4 sm4Cbc = new SM4(
    Algorithm.Mode.CBC,
    Padding.PKCS5Padding,
    key,
    iv
);
```

### 2. Asymmetric Encryption & Signatures

#### RSA Encryption and Decryption

```java
import org.miaixz.bus.crypto.center.RSA;

// Generate new key pair
RSA rsa = new RSA();

// Encrypt with public key, decrypt with private key
byte[] data = "Secret Message".getBytes();
byte[] encrypted = rsa.encrypt(data, KeyType.PublicKey);
byte[] decrypted = rsa.decrypt(encrypted, KeyType.PrivateKey);

// Sign with private key, verify with public key
byte[] signature = rsa.sign(data);
boolean valid = rsa.verify(data, signature);
```

#### SM2 (Chinese Elliptic Curve)

```java
import org.miaixz.bus.crypto.center.SM2;

SM2 sm2 = new SM2();

// Encryption (public key)
byte[] encrypted = sm2.encrypt("data".getBytes());

// Decryption (private key)
byte[] decrypted = sm2.decrypt(encrypted);

// Signature with custom ID
byte[] id = "1234567812345678".getBytes();
byte[] signature = sm2.sign("data".getBytes(), id);

// Verification
boolean valid = sm2.verify("data".getBytes(), signature, id);
```

### 3. Hash and Digest Operations

#### Multiple Hash Algorithms

```java
import org.miaixz.bus.crypto.Builder;

// MD5 (128-bit)
String md5 = Builder.md5Hex("data");

// SHA-1 (160-bit)
String sha1 = Builder.sha1Hex("data");

// SHA-256 (256-bit)
String sha256 = Builder.sha256Hex("data");

// SHA-512 (512-bit)
String sha512 = Builder.sha512Hex("data");

// SM3 (Chinese standard, 256-bit)
String sm3 = Builder.sm3("data");

// 16-char MD5 (for legacy systems)
String md5_16 = Builder.md5Hex16("data");
```

#### Hashing Files and Streams

```java
import java.io.File;
import java.io.FileInputStream;

// Hash a file
File file = new File("large_file.zip");
String fileMd5 = Builder.md5Hex(file);
String fileSha256 = Builder.sha256Hex(file);

// Hash an input stream
try (FileInputStream fis = new FileInputStream(file)) {
    String streamSha512 = Builder.sha512Hex(fis);
}
```

### 4. HMAC (Message Authentication Codes)

#### HMac for API Authentication

```java
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.center.HMac;

// HMac-SHA256 for API signatures
HMac hmac = Builder.hmacSha256("your-secret-key".getBytes());
String apiSignature = hmac.digestHex("timestamp=2024-01-01&user=alice");

// HMac-SM3
HMac hmacSm3 = Builder.hmacSm3("your-secret-key".getBytes());
String signature = hmacSm3.digestHex("data");
```

### 5. Password Hashing

#### BCrypt for Secure Password Storage

```java
import org.miaixz.bus.crypto.center.BCrypt;

// Hash a password with auto-generated salt
String hashed = BCrypt.hashpw("password123", BCrypt.gensalt());

// Hash with specific work factor (2^12 iterations)
String strongHash = BCrypt.hashpw("password123", BCrypt.gensalt(12));

// Verify password
boolean matches = BCrypt.checkpw("password123", hashed);
```

#### PBKDF2

```java
import org.miaixz.bus.crypto.center.PBKDF2;

PBKDF2 pbkdf2 = new PBKDF2();

// Derive key from password
char[] password = "myPassword".toCharArray();
byte[] salt = RandomKit.randomBytes(16);
String derivedKey = pbkdf2.encryptHex(password, salt);
```

#### Argon2 (Modern Password Hashing)

```java
import org.miaixz.bus.crypto.center.Argon2;

Argon2 argon2 = new Argon2();

// Hash password with Argon2
String hash = argon2.hash("password");

// Verify password
boolean valid = argon2.verify("password", hash);
```

### 6. Digital Signatures

#### RSA Signatures

```java
import org.miaixz.bus.crypto.center.Sign;
import org.miaixz.bus.core.lang.Algorithm;

// Create signature instance
Sign sign = new Sign(Algorithm.SHA256withRSA);

// Sign data
byte[] signature = sign.sign("important document".getBytes());

// Verify signature
boolean valid = sign.verify("important document".getBytes(), signature);
```

#### SM2 Signatures

```java
import org.miaixz.bus.crypto.center.SM2;

SM2 sm2 = new SM2();

// Sign with SM2
byte[] data = "important message".getBytes();
byte[] signature = sm2.sign(data);

// Verify with SM2
boolean valid = sm2.verify(data, signature);
```

### 7. Format-Preserving Encryption (FPE)

#### Encrypt Numbers Without Changing Format

```java
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.center.FPE;
import org.bouncycastle.crypto.AlphabetMapper;

// Encrypt credit card numbers (preserves format)
AlphabetMapper digitMapper = AlphabetMapper.digitsOnly();
FPE fpe = Builder.fpe(
    FPE.FPEMode.FF1,
    "16-byte-key-12345".getBytes(),
    digitMapper,
    "tweak".getBytes()
);

// "1234567890123456" becomes another 16-digit number
String encrypted = fpe.encrypt("1234567890123456");
String decrypted = fpe.decrypt(encrypted);
```

### 8. One-Time Passwords (OTP)

#### HOTP (HMAC-based One-Time Password)

```java
import org.miaixz.bus.crypto.center.HOTP;

HOTP hotp = new HOTP();

// Generate HOTP
String code = hotp.generate("secret-key", 0);  // counter = 0

// Verify HOTP
boolean valid = hotp.verify("secret-key", 0, code);
```

#### TOTP (Time-based One-Time Password)

```java
import org.miaixz.bus.crypto.center.TOTP;

TOTP totp = new TOTP();

// Generate TOTP (valid for 30 seconds)
String code = totp.generate("secret-key");

// Verify TOTP
boolean valid = totp.verify("secret-key", code);
```

### 9. Advanced Configuration

#### Using Custom Providers

```java
import org.bouncycastle.jce.provider.BouncyCastleProvider;

// Add BouncyCastle provider
Builder.addProvider(new BouncyCastleProvider());

// Now all crypto operations can use BC algorithms
AES aes = Builder.aes();
```

#### Disable Custom Provider

```java
// Use only JDK default provider
Builder.disableCustomProvider();
```

-----

## 📋 Algorithm Reference

### Symmetric Algorithms

| Algorithm | Key Size | Block Size | Modes | Notes |
|:---|:---|:---|:---|:---|
| **AES** | 128/192/256 | 128 | ECB/CBC/CTR/GCM/OFB/CFB | Most widely used |
| **DES** | 56 | 64 | ECB/CBC/CTR | Deprecated, legacy only |
| **TDEA** | 112/168 | 64 | ECB/CBC | Triple DES, legacy |
| **SM4** | 128 | 128 | ECB/CBC/CTR | Chinese standard |
| **ChaCha20** | 256 | N/A (stream) | Stream cipher | High performance |
| **ZUC** | 128/256 | N/A (stream) | Stream cipher | Chinese standard |
| **RC4** | 40-2048 | N/A (stream) | Stream cipher | Legacy, not recommended |
| **TEA** | 128 | 64 | ECB | Simple, fast |

### Asymmetric Algorithms

| Algorithm | Key Size | Operations | Notes |
|:---|:---|:---|:---|
| **RSA** | 512-4096 | Encrypt/Decrypt/Sign/Verify | Most common |
| **SM2** | 256 | Encrypt/Decrypt/Sign/Verify | Chinese ECC standard |
| **ECIES** | Variable | Encrypt/Decrypt | Elliptic Curve Integrated |

### Digest Algorithms

| Algorithm | Output Size | Notes |
|:---|:---|:---|
| **MD5** | 128 bits | Legacy, not secure |
| **SHA-1** | 160 bits | Legacy, not secure |
| **SHA-256** | 256 bits | Recommended |
| **SHA-512** | 512 bits | High security |
| **SM3** | 256 bits | Chinese standard |

### HMAC Algorithms

| Algorithm | Output Size | Notes |
|:---|:---|:---|
| **HMac-MD5** | 128 bits | Legacy use only |
| **HMac-SHA1** | 160 bits | Legacy use only |
| **HMac-SHA256** | 256 bits | Recommended |
| **HMac-SHA512** | 512 bits | High security |
| **HMac-SM3** | 256 bits | Chinese standard |

### Password Hashing

| Algorithm | Security | Speed | Notes |
|:---|:---|:---|:---|
| **BCrypt** | High | Slow | Recommended |
| **PBKDF2** | High | Slow | PKCS#5 standard |
| **Argon2** | Very High | Adjustable | Winner of PHC 2015 |

-----

## 💡 Best Practices

### 1. Use Secure Key Sizes

```java
// ✅ Recommended: AES-256
byte[] key256 = RandomKit.randomBytes(32);
AES aes256 = Builder.aes(key256);

// ✅ Good: AES-128
byte[] key128 = RandomKit.randomBytes(16);
AES aes128 = Builder.aes(key128);

// ❌ Not Recommended: Small key sizes
byte[] key64 = RandomKit.randomBytes(8);  // Too small!
```

### 2. Use Appropriate Modes

```java
// ✅ Recommended: AES-GCM or AES-CBC with IV
new AES(Algorithm.Mode.GCM, Padding.NoPadding, key);
new AES(Algorithm.Mode.CBC, Padding.PKCS5Padding, key, iv);

// ⚠️ Use with caution: ECB mode (not semantically secure)
new AES(Algorithm.Mode.ECB, Padding.PKCS5Padding, key);
```

### 3. Store Keys Securely

```java
// ✅ Recommended: Use KeyStore or environment variables
byte[] key = readKeyFromSecureStore();

// ❌ Not Recommended: Hardcode keys in source
byte[] key = "my-hardcoded-key".getBytes();  // Don't do this!
```

### 4. Use Appropriate Algorithms

```java
// ✅ Recommended: SHA-256 or better
String hash = Builder.sha256("data");

// ⚠️ Legacy only: MD5 or SHA-1
String hash = Builder.md5("data");  // Not secure for signatures
```

### 5. Password Hashing

```java
// ✅ Recommended: Use BCrypt or Argon2
String hashed = BCrypt.hashpw(password, BCrypt.gensalt(12));

// ❌ Not Recommended: Plain hash or fast hash
String hash = Builder.md5(password);  // Vulnerable to rainbow tables
```

-----

## ❓ Frequently Asked Questions

### Q1: How do I choose between AES and SM4?

**A**: Choose based on your compliance requirements:

```java
// International standard (most common)
AES aes = Builder.aes();

// Chinese national standard (required in China)
SM4 sm4 = Builder.sm4();
```

**Use AES** for:
- Global applications
- Maximum compatibility
- Better performance on most platforms

**Use SM4** for:
- Chinese domestic applications
- Compliance with Chinese cryptography regulations
- Integration with Chinese systems

### Q2: Why does encryption fail with "Invalid key length"?

**A**: Different algorithms have specific key length requirements:

```java
// AES: 16, 24, or 32 bytes (128/192/256 bits)
byte[] aesKey = RandomKit.randomBytes(16);  // ✅ Valid

// SM4: 16 bytes only (128 bits)
byte[] sm4Key = RandomKit.randomBytes(16);  // ✅ Valid

// DES: 8 bytes (56 bits + parity)
byte[] desKey = RandomKit.randomBytes(8);   // ✅ Valid
```

### Q3: How do I securely store encryption keys?

**A**: Never store keys in plaintext. Use one of these approaches:

```java
// Option 1: Java KeyStore
KeyStore keyStore = KeyStore.getInstance("JCEKS");
keyStore.load(new FileInputStream("keystore.jks"), password);

// Option 2: Environment variables
String key = System.getenv("ENCRYPTION_KEY");

// Option 3: Secrets management service
String key = SecretsManager.getSecret("encryption-key");
```

### Q4: When should I use ECB vs CBC vs GCM mode?

**A**: Choose based on security requirements:

```java
// ❌ Avoid: ECB (not semantically secure)
new AES(Algorithm.Mode.ECB, ...);

// ✅ Good: CBC (requires unique IV)
new AES(Algorithm.Mode.CBC, Padding.PKCS5Padding, key, iv);

// ✅ Best: GCM (provides authentication)
new AES(Algorithm.Mode.GCM, Padding.NoPadding, key);
```

### Q5: How do I verify data integrity?

**A**: Use HMAC or authenticated encryption:

```java
// Option 1: HMAC
HMac hmac = Builder.hmacSha256(key);
String mac = hmac.digestHex("data");
// Send both data and mac

// Option 2: Authenticated encryption (AES-GCM)
AES aes = new AES(Algorithm.Mode.GCM, Padding.NoPadding, key);
byte[] encrypted = aes.encrypt("data");  // Includes authentication tag
```

### Q6: Why is BCrypt preferred over MD5 for passwords?

**A**: BCrypt is designed for passwords:

```java
// ✅ BCrypt: Slow, salted, adaptive
String hash = BCrypt.hashpw(password, BCrypt.gensalt(12));

// ❌ MD5: Fast, vulnerable to rainbow tables
String hash = Builder.md5(password);
```

**BCrypt advantages**:
- Built-in salt generation
- Computationally slow (prevents brute force)
- Adjustable work factor
- Resistant to rainbow table attacks

### Q7: How do I migrate from MD5 to BCrypt?

**A**: Use a gradual migration strategy:

```java
// Check password with BCrypt first
if (BCrypt.checkpw(password, bcryptHash)) {
    return true;
}

// Fallback to MD5 for legacy users
if (Builder.md5(password).equals(legacyMd5Hash)) {
    // Re-hash with BCrypt on successful login
    String newHash = BCrypt.hashpw(password, BCrypt.gensalt(12));
    updatePasswordHashInDatabase(userId, newHash);
    return true;
}
```

### Q8: Can I use Bus Crypto with Spring Security?

**A**: Yes, integrate with Spring Security's password encoder:

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

## 🔄 Version Compatibility

| Bus Crypto Version | JDK Version | Bouncy Castle Version |
|:---|:---|:---|
| 8.x | 17+ | 1.70+ (optional) |
| 7.x | 11+ | 1.60+ (optional) |

-----

## 🔐 Security Considerations

### Algorithm Selection

1. **For new applications**:
   - Use AES-256 for symmetric encryption
   - Use RSA-4096 or ECC for asymmetric encryption
   - Use SHA-256 or SHA-512 for hashing
   - Use BCrypt or Argon2 for passwords

2. **For Chinese domestic applications**:
   - Use SM2 for asymmetric encryption
   - Use SM3 for hashing
   - Use SM4 for symmetric encryption

3. **Avoid these algorithms**:
   - MD5 (except for non-cryptographic hashes)
   - SHA-1 (except for compatibility)
   - DES (use AES instead)
   - RC4 (broken stream cipher)

### Key Management

- **Never hardcode keys** in source code
- **Use environment variables** or secret management systems
- **Rotate keys regularly**
- **Use different keys** for different purposes
- **Destroy keys** when no longer needed

### Random Number Generation

```java
// ✅ Recommended: SecureRandom
SecureRandom secureRandom = new SecureRandom();
byte[] key = new byte[32];
secureRandom.nextBytes(key);

// ❌ Not Recommended: java.util.Random
Random random = new Random();
byte[] key = new byte[32];
random.nextBytes(key);  // Predictable!
```

-----

## 📚 Additional Resources

- [Java Cryptography Architecture (JCA)](https://docs.oracle.com/javase/8/docs/technotes/guides/security/crypto/CryptoSpec.html)
- [Bouncy Castle Documentation](https://www.bouncycastle.org/documentation.html)
- [NIST Cryptographic Standards](https://csrc.nist.gov/projects/cryptographic-standards-and-guidelines)
- [Chinese National Cryptography Standards (GM/T)](https://www.oscca.gov.cn/)

-----

## 🤝 Contributing

Contributions are welcome! Please ensure:

1. All tests pass
2. Code follows the project's style guidelines
3. Security implications are carefully considered
4. Documentation is updated
