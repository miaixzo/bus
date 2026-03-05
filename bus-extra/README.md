# 🎨 Bus Extra: Versatile Enhancement Toolkit

<p align="center">
<strong>Feature-Rich Extension Library for Enterprise Applications</strong>
</p>

-----

## 📖 Project Introduction

**Bus Extra** is a powerful extension library built on the Bus framework, providing a rich set of utility functions for enterprise applications. It offers seamless integration with multiple third-party libraries through a unified API, enabling developers to quickly implement complex functionality.

**Key Highlights**:

- **Unified API**: Consistent interface across different providers
- **Flexible Providers**: Pluggable architecture supporting multiple implementations
- **Zero Intrusion**: Optional dependencies, use only what you need
- **Production Ready**: Battle-tested in enterprise environments

-----

## ✨ Core Features

### 📦 Compression & Archiving

Support for multiple compression formats with unified API:

| Format | Archive | Extract | Stream |
| :--- | :---: | :---: | :---: |
| **ZIP** | ✓ | ✓ | ✓ |
| **7Z** | ✓ | ✓ | ✓ |
| **GZIP** | ✓ | ✓ | ✓ |
| **TAR** | ✓ | ✓ | ✓ |
| **BZIP2** | ✓ | ✓ | ✓ |
| **XZ** | ✓ | ✓ | ✓ |

```java
// Compress to ZIP
CompressBuilder.create(ZipArchiver.class)
    .add(file1)
    .add(file2)
    .build(outputFile);

// Extract with auto-detection
Extractor extractor = CompressBuilder.createExtractor(archiveFile);
extractor.extract(destDir);
```

### 🎨 Image Processing

Comprehensive image manipulation capabilities:

- **Transformations**: Scale, crop, rotate, flip
- **Filters**: Grayscale, binary, compress
- **Watermarks**: Text and image watermarks
- **Format Conversion**: Support for JPG, PNG, GIF, BMP, etc.
- **Advanced**: Background removal, color conversion

```java
// Scale image
ImageKit.scale(srcFile, destFile, 0.5f);

// Add text watermark
ImageText text = new ImageText("© Copyright", Color.RED, new Font("Arial", Font.BOLD, 24));
ImageKit.pressText(srcFile, destFile, text);

// Convert format
ImageKit.convert(srcFile, destFile);
```

### 📝 JSON Serialization

Flexible JSON processing with pluggable providers:

**Supported Providers**:
- FastJSON2 (default)
- Jackson
- Gson

```java
// Auto-detect provider
String json = JsonKit.toJsonString(obj);
User user = JsonKit.toPojo(json, User.class);

// Custom provider
JsonFactory.use(JsonProvider.JACKSON);
```

### 📧 Email Sending

Simplified email API with support for:

- **SMTP/SMTPS**: Standard email protocols
- **Attachments**: File and stream attachments
- **HTML Emails**: Rich text formatting
- **Global Configuration**: Centralized account management

```java
// Use global account
Mail account = GlobalMailAccount.INSTANCE.getAccount();
account.send("to@example.com", "Subject", "Content");

// Custom account
MailAccount customAccount = new MailAccount();
customAccount.setHost("smtp.example.com");
customAccount.setUser("user@example.com");
customAccount.setPass("password");
Mail mail = customAccount.getMail();
mail.send("to@example.com", "Test", "Hello World!");
```

### 📨 Message Queue Integration

Unified abstraction for multiple message queues:

**Supported MQs**:
- **Apache Kafka**
- **RabbitMQ**
- **RocketMQ**
- **ActiveMQ**

```java
// Produce message
Producer producer = ProducerFactory.create(KafkaProducer.class);
producer.send("topic-name", message);

// Consume message
Consumer consumer = ConsumerFactory.create(KafkaConsumer.class);
consumer.subscribe("topic-name", message -> {
    // Handle message
});
```

### 🔤 Pinyin Conversion

Chinese character to Pinyin conversion with multiple engines:

**Supported Engines**:
- JPinyin
- Pinyin4J
- TinyPinyin
- Bopomofo4J
- Houbb Pinyin

```java
// Get pinyin
String pinyin = PinyinKit.getPinyin("中国你好"); // "zhong guo ni hao"

// First letter
String first = PinyinKit.getFirstLetter("中国"); // "zg"

// With tone marks
String withTone = PinyinKit.getPinyin("你好", true); // "ni3 hao3"
```

### 📱 QR Code Generation

Comprehensive QR code and barcode support:

**Supported Formats**:
- QR Code
- Code 128
- Code 39
- EAN-13
- UPC-A
- And more...

**Output Formats**:
- Image (PNG, JPG, etc.)
- SVG (vector)
- ASCII Art (text)

```java
// Generate QR code
QrCodeKit.generate("https://example.com", 300, 300, qrFile);

// Decode QR code
String content = QrCodeKit.decode(qrFile);

// SVG format
String svg = QrCodeKit.generateAsSvg(content, QrConfig.of(300, 300));

// Custom style
QrConfig config = QrConfig.of(300, 300)
    .setForeColor(Color.BLUE)
    .setBackColor(Color.WHITE)
    .setMargin(2);
```

### 🔐 SSH/SFTP Operations

Secure remote operations with multiple SSH libraries:

**Supported Libraries**:
- JSch
- SSHJ

```java
// SSH session
Session session = SessionFactory.create(JSchSession.class);
session.connect(host, port, username, password);

// SFTP operations
Sftp sftp = session.getSftp();
sftp.upload(localFile, remotePath);
sftp.download(remotePath, localFile);
sftp.listFiles(remoteDir);
```

### 🎭 Template Engines

Unified template engine abstraction:

**Supported Engines**:
- **Beetl**: High-performance Java template engine
- **FreeMarker**: Mature template engine
- **Thymeleaf**: Modern server-side Java template engine

```java
// Render template
Template template = TemplateFactory.create(BeetlTemplate.class);
template.bind("name", "World");
String result = template.render("template.btl");
```

### 🌐 Natural Language Processing

Chinese word segmentation support:

**Supported Segementers**:
- Ansj
- Jieba
- JCseg
- MMseg4j
- HanLP
- SmartCN
- Word

### 🎭 Emoji Support

Emoji alias conversion and unicode handling (via emoji-java)

### 📡 FTP Operations

FTP/SFTP client and server implementation

### 🔢 CAPTCHA

Captcha generation for bot prevention

-----

## 🚀 Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-extra</artifactId>
    <version>x.x.x</version>
</dependency>
```

### Optional Dependencies

Bus Extra uses optional dependencies to minimize bloat. Add only what you need:

```xml
<!-- For JSON processing (choose one or more) -->
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2</artifactId>
    <version>2.0.60</version>
</dependency>

<!-- For QR codes -->
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.3</version>
</dependency>

<!-- For Pinyin (choose one) -->
<dependency>
    <groupId>com.github.stuxuhai</groupId>
    <artifactId>jpinyin</artifactId>
    <version>1.1.8</version>
</dependency>

<!-- For email -->
<dependency>
    <groupId>jakarta.mail</groupId>
    <artifactId>jakarta.mail-api</artifactId>
    <version>2.1.3</version>
</dependency>

<!-- For SSH -->
<dependency>
    <groupId>com.github.mwiede</groupId>
    <artifactId>jsch</artifactId>
    <version>2.27.2</version>
</dependency>

<!-- For compression -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-compress</artifactId>
    <version>1.28.0</version>
</dependency>

<!-- For message queues (choose as needed) -->
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
    <version>4.1.1</version>
</dependency>
```

-----

## 📝 Usage Examples

### Image Processing

```java
// Read image
BufferedImage image = ImageKit.read(new File("input.jpg"));

// Scale to 50%
Image scaled = ImageKit.scale(image, 0.5f);

// Add watermark
ImageText watermark = new ImageText("© MyCompany", Color.RED, new Font("Arial", Font.BOLD, 32));
Image watermarked = ImageKit.pressText(scaled, watermark);

// Save output
ImageKit.write(watermarked, new File("output.png"));
ImageKit.flush(watermarked);
```

### QR Code Generation

```java
// Basic QR code
QrCodeKit.generate(
    "https://www.example.com",
    300, 300,
    new File("qrcode.png")
);

// Custom style QR code
QrConfig config = QrConfig.of(350, 350)
    .setForeColor(new Color(0, 120, 215))  // Blue
    .setBackColor(Color.WHITE)
    .setMargin(2);

QrCodeKit.generate("Custom Content", config, new File("styled.png"));

// Decode QR code
String content = QrCodeKit.decode(new File("qrcode.png"));
System.out.println("QR Code content: " + content);
```

### Email Sending

```java
// Configure account
MailAccount account = new MailAccount();
account.setHost("smtp.example.com");
account.setPort(465);
account.setUser("sender@example.com");
account.setPass("password");
account.setSsl(true);

// Send simple email
Mail mail = account.getMail();
mail.send("recipient@example.com", "Test Subject", "Email body");

// Send HTML email with attachments
mail.send(
    Collections.singleton("recipient@example.com"),
    null,  // No CC
    null,  // No BCC
    "HTML Email Subject",
    "<h1>Hello</h1><p>This is an <strong>HTML</strong> email.</p>",
    true,  // is HTML
    Collections.singletonList(new File("attachment.pdf"))
);
```

### Compression & Extraction

```java
// Create ZIP archive
Archiver archiver = CompressBuilder.create(ZipArchiver.class);
archiver.add(new File("file1.txt"))
        .add(new File("file2.jpg"))
        .add(new File("documents/"))
        .build(new File("archive.zip"));

// Extract archive (auto-detect format)
Extractor extractor = CompressBuilder.createExtractor(new File("archive.zip"));
extractor.extract(new File("output/"));

// Stream compression
try (OutputStream out = new FileOutputStream("compressed.zip")) {
    Archiver streamArchiver = CompressBuilder.create(ZipArchiver.class, out);
    streamArchiver.add(file1).build();
}
```

### JSON Operations

```java
// Serialize
User user = new User("John", 30);
String json = JsonKit.toJsonString(user);

// Deserialize
User parsed = JsonKit.toPojo(json, User.class);

// List operations
List<User> users = Arrays.asList(user1, user2, user3);
String jsonArray = JsonKit.toJsonString(users);
List<User> parsedList = JsonKit.toList(jsonArray, User.class);

// Map operations
Map<String, Object> map = JsonKit.toMap(json);
String value = JsonKit.getValue(json, "name");
```

### Pinyin Conversion

```java
// Get pinyin
String pinyin = PinyinKit.getPinyin("中国"); // "zhong guo"

// First letter for sorting
String first = PinyinKit.getFirstLetter("北京"); // "bj"

// Check if Chinese
boolean isChinese = PinyinKit.isChinese('中'); // true

// Custom separator
String withDash = PinyinKit.getPinyin("你好世界", "-"); // "ni-hao-shi-jie"
```

### SSH/SFTP Operations

```java
// Create SSH session
SessionConfig config = new SessionConfig();
config.setHost("example.com");
config.setPort(22);
config.setUser("username");
config.setPassword("password");

Session session = SessionFactory.create(JSchSession.class, config);
session.connect();

// Execute command
String result = session.executeCommand("ls -la");

// SFTP operations
Sftp sftp = session.getSftp();
sftp.upload("/local/file.txt", "/remote/file.txt");
sftp.download("/remote/file.txt", "/local/file.txt");

// List files
List<SftpFile> files = sftp.listFiles("/remote/dir");

session.disconnect();
```

### Template Engine

```java
// Using Beetl
TemplateEngine engine = TemplateFactory.create(BeetlEngine.class);
Template template = engine.getTemplate("hello.btl");

// Bind data
template.bind("name", "World");
template.bind("items", Arrays.asList("A", "B", "C"));

// Render
String result = template.render();
```

-----

## 💡 Best Practices

### 1. Use Provider Pattern for Flexibility

```java
// ✅ Good: Use factory for provider switching
JsonProvider provider = JsonFactory.create(JsonProvider.JACKSON);

// ❌ Bad: Direct dependency on implementation
ObjectMapper mapper = new ObjectMapper();
```

### 2. Proper Resource Management

```java
// ✅ Good: Use try-with-resources or explicit flush
BufferedImage image = ImageKit.read(file);
try {
    ImageKit.write(image, outputFile);
} finally {
    ImageKit.flush(image);
}

// ❌ Bad: Forget to release resources
BufferedImage image = ImageKit.read(file);
ImageKit.write(image, outputFile);
// Image resources not released!
```

### 3. Configure Global Settings

```java
// ✅ Good: Set global mail account
GlobalMailAccount.INSTANCE.getAccount().send(...);

// ✅ Good: Configure JSON provider globally
JsonFactory.use(JsonProvider.FASTJSON2);
```

### 4. Handle Exceptions Appropriately

```java
// ✅ Good: Handle specific exceptions
try {
    QrCodeKit.decode(qrFile);
} catch (QrCodeException e) {
    log.error("Failed to decode QR code", e);
}

// ❌ Bad: Catch all exceptions
try {
    QrCodeKit.decode(qrFile);
} catch (Exception e) {
    // Swallows all errors
}
```

### 5. Use Appropriate Image Quality

```java
// ✅ Good: Set quality for JPEG compression
QrConfig config = QrConfig.of(width, height)
    .setQuality(0.9f);  // High quality

// ✅ Good: Lower quality for thumbnails
ImageKit.compress(image, thumbnail, 0.7f);
```

-----

## ❓ Frequently Asked Questions

### Q1: How to switch between JSON providers?

```java
// Method 1: Global setting (recommended)
JsonFactory.use(JsonProvider.JACKSON);

// Method 2: Specific instance
JsonProvider provider = JsonFactory.create(JsonProvider.GSON);
String json = provider.toJsonString(obj);
```

### Q2: Which Pinyin engine should I use?

| Engine | Performance | Accuracy | Dictionary Size | Recommendation |
| :--- | :--- | :--- | :--- | :--- |
| **TinyPinyin** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | Small | Mobile apps |
| **JPinyin** | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | Medium | General use |
| **Pinyin4J** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | Large | Desktop apps |

```java
// Switch engine
PinyinFactory.use(PinyinProvider.TINY_PINYIN);
```

### Q3: How to generate QR codes with logos?

```java
QrConfig config = QrConfig.of(300, 300);
// After generating, use ImageKit to composite logo
BufferedImage qr = QrCodeKit.generate(content, config);
BufferedImage logo = ImageKit.read(logoFile);

// Calculate center position and composite
int x = (qr.getWidth() - logo.getWidth()) / 2;
int y = (qr.getHeight() - logo.getHeight()) / 2;
Graphics2D g = qr.createGraphics();
g.drawImage(logo, x, y, null);
g.dispose();
```

### Q4: How to handle large file compression?

```java
// ✅ Use stream compression for large files
try (OutputStream out = new FileOutputStream("large.zip")) {
    Archiver archiver = CompressBuilder.create(ZipArchiver.class, out);
    archiver.add(largeFile).build();
}

// ✅ Or use buffered extraction
Extractor extractor = CompressBuilder.createExtractor(archiveFile);
extractor.setBufferSize(8192);  // 8KB buffer
extractor.extract(destDir);
```

### Q5: Email sending fails with SSL/TLS error?

```java
MailAccount account = new MailAccount();
// For SSL (port 465/993)
account.setSsl(true);
account.setSslPort(465);

// For TLS (port 587)
account.setStarttlsEnable(true);
account.setStarttlsRequired(true);
```

### Q6: How to customize QR code error correction level?

```java
QrConfig config = QrConfig.of(300, 300)
    .setErrorCorrection(ErrorCorrectionLevel.H);  // High (30%)
// Available levels: L (7%), M (15%), Q (25%), H (30%)
```

### Q7: SSH connection timeout handling?

```java
SessionConfig config = new SessionConfig();
config.setHost("example.com");
config.setTimeout(30000);  // 30 seconds
config.setConnectTimeout(10000);  // 10 seconds

Session session = SessionFactory.create(JSchSession.class, config);
try {
    session.connect();
} catch (SshException e) {
    log.error("Connection timeout", e);
}
```

### Q8: How to compress images without quality loss?

```java
// Use PNG for lossless compression
ImageKit.write(image, "png", outputStream);

// Or set quality to 1.0 for JPEG
ImageWriter.of(image, "jpg")
    .setQuality(1.0f)
    .write(outputFile);
```

-----

## 🔄 Version Compatibility

| Bus Extra Version | Bus Core Version | JDK Version |
| :--- | :--- | :--- |
| 8.x | 8.x | 17+ |
| 7.x | 7.x | 11+ |

-----

## 🔧 Configuration Examples

### Mail Configuration (mail.setting)

```properties
# SMTP Server
mail.host=smtp.example.com
mail.port=465
mail.user=sender@example.com
mail.pass=password
mail.from=sender@example.com
mail.ssl.enable=true

# Charset
mail.charset=UTF-8

# Timeout
mail.timeout=30000
mail.connectiontimeout=10000
```

### JSON Provider Configuration

```java
// Configure provider
JsonFactory.use(JsonProvider.FASTJSON2);

// With custom date format
JsonProvider provider = JsonFactory.get();
String json = provider.toJsonString(obj, "yyyy-MM-dd HH:mm:ss");
```

-----

## 📊 Feature Comparison

| Feature | Bus Extra | Apache Commons | Other Libraries |
| :--- | :---: | :---: | :---: |
| **Unified API** | ✓ | ✗ | ✗ |
| **Provider Pattern** | ✓ | ✗ | Partial |
| **Optional Dependencies** | ✓ | ✗ | ✗ |
| **Chinese Support** | ✓ | ✗ | Partial |
| **Modern Java** | ✓ | ✗ | Partial |
| **Zero Intrusion** | ✓ | N/A | ✗ |

-----

## 🎯 Use Cases

- **Enterprise Applications**: Comprehensive utility toolkit
- **Web Applications**: Image processing, QR codes, email
- **Data Processing**: Compression, JSON, Pinyin
- **Integration Projects**: SSH/SFTP, message queues
- **Mobile Backend**: Template engines, email, image processing
- **Microservices**: Lightweight, dependency-optional features

-----

## 🤝 Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

-----

## 📄 License

[Apache License Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)

-----

## 🔗 Links

- **GitHub**: [https://github.com/818000/bus](https://github.com/818000/bus)
- **Issues**: [https://github.com/818000/bus/issues](https://github.com/818000/bus/issues)
