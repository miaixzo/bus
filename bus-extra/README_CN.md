# 🎨 Bus Extra：多功能增强工具集

<p align="center">
<strong>企业级应用的功能丰富扩展库</strong>
</p>

-----

## 📖 项目简介

**Bus Extra** 是一个基于 Bus 框架构建的强大扩展库，为企业应用程序提供了丰富的实用工具集。它通过统一的 API 与多个第三方库无缝集成，使开发者能够快速实现复杂功能。

**核心亮点**：

- **统一 API**：跨不同提供程序的一致接口
- **灵活提供程序**：可插拔架构支持多种实现
- **零侵入**：可选依赖，仅使用所需功能
- **生产就绪**：在企业环境中经过实战验证

-----

## ✨ 核心功能

### 📦 压缩与归档

支持多种压缩格式的统一 API：

| 格式 | 归档 | 解压 | 流式 |
| :--- | :---: | :---: | :---: |
| **ZIP** | ✓ | ✓ | ✓ |
| **7Z** | ✓ | ✓ | ✓ |
| **GZIP** | ✓ | ✓ | ✓ |
| **TAR** | ✓ | ✓ | ✓ |
| **BZIP2** | ✓ | ✓ | ✓ |
| **XZ** | ✓ | ✓ | ✓ |

```java
// 压缩为 ZIP
CompressBuilder.create(ZipArchiver.class)
    .add(file1)
    .add(file2)
    .build(outputFile);

// 自动检测格式解压
Extractor extractor = CompressBuilder.createExtractor(archiveFile);
extractor.extract(destDir);
```

### 🎨 图像处理

全面的图像处理能力：

- **转换**：缩放、裁剪、旋转、翻转
- **滤镜**：灰度化、二值化、压缩
- **水印**：文字和图像水印
- **格式转换**：支持 JPG、PNG、GIF、BMP 等
- **高级功能**：背景移除、颜色转换

```java
// 缩放图像
ImageKit.scale(srcFile, destFile, 0.5f);

// 添加文字水印
ImageText text = new ImageText("© 版权所有", Color.RED, new Font("Arial", Font.BOLD, 24));
ImageKit.pressText(srcFile, destFile, text);

// 格式转换
ImageKit.convert(srcFile, destFile);
```

### 📝 JSON 序列化

灵活的 JSON 处理，支持可插拔提供程序：

**支持的提供程序**：
- FastJSON2（默认）
- Jackson
- Gson

```java
// 自动检测提供程序
String json = JsonKit.toJsonString(obj);
User user = JsonKit.toPojo(json, User.class);

// 自定义提供程序
JsonFactory.use(JsonProvider.JACKSON);
```

### 📧 邮件发送

简化的邮件 API，支持：

- **SMTP/SMTPS**：标准邮件协议
- **附件**：文件和流附件
- **HTML 邮件**：富文本格式
- **全局配置**：集中式账户管理

```java
// 使用全局账户
Mail account = GlobalMailAccount.INSTANCE.getAccount();
account.send("to@example.com", "主题", "内容");

// 自定义账户
MailAccount customAccount = new MailAccount();
customAccount.setHost("smtp.example.com");
customAccount.setUser("user@example.com");
customAccount.setPass("password");
Mail mail = customAccount.getMail();
mail.send("to@example.com", "测试", "你好，世界！");
```

### 📨 消息队列集成

多种消息队列的统一抽象：

**支持的 MQ**：
- **Apache Kafka**
- **RabbitMQ**
- **RocketMQ**
- **ActiveMQ**

```java
// 发送消息
Producer producer = ProducerFactory.create(KafkaProducer.class);
producer.send("topic-name", message);

// 消费消息
Consumer consumer = ConsumerFactory.create(KafkaConsumer.class);
consumer.subscribe("topic-name", message -> {
    // 处理消息
});
```

### 🔤 拼音转换

支持多种引擎的汉字转拼音：

**支持的引擎**：
- JPinyin
- Pinyin4J
- TinyPinyin
- Bopomofo4J
- Houbb Pinyin

```java
// 获取拼音
String pinyin = PinyinKit.getPinyin("中国你好"); // "zhong guo ni hao"

// 首字母
String first = PinyinKit.getFirstLetter("中国"); // "zg"

// 带声调
String withTone = PinyinKit.getPinyin("你好", true); // "ni3 hao3"
```

### 📱 二维码生成

全面的二维码和条形码支持：

**支持的格式**：
- QR Code
- Code 128
- Code 39
- EAN-13
- UPC-A
- 更多...

**输出格式**：
- 图像（PNG、JPG 等）
- SVG（矢量）
- ASCII 艺术（文本）

```java
// 生成二维码
QrCodeKit.generate("https://example.com", 300, 300, qrFile);

// 解码二维码
String content = QrCodeKit.decode(qrFile);

// SVG 格式
String svg = QrCodeKit.generateAsSvg(content, QrConfig.of(300, 300));

// 自定义样式
QrConfig config = QrConfig.of(300, 300)
    .setForeColor(Color.BLUE)
    .setBackColor(Color.WHITE)
    .setMargin(2);
```

### 🔐 SSH/SFTP 操作

支持多种 SSH 库的安全远程操作：

**支持的库**：
- JSch
- SSHJ

```java
// SSH 会话
Session session = SessionFactory.create(JSchSession.class);
session.connect(host, port, username, password);

// SFTP 操作
Sftp sftp = session.getSftp();
sftp.upload(localFile, remotePath);
sftp.download(remotePath, localFile);
sftp.listFiles(remoteDir);
```

### 🎭 模板引擎

统一的模板引擎抽象：

**支持的引擎**：
- **Beetl**：高性能 Java 模板引擎
- **FreeMarker**：成熟的模板引擎
- **Thymeleaf**：现代服务器端 Java 模板引擎

```java
// 渲染模板
Template template = TemplateFactory.create(BeetlTemplate.class);
template.bind("name", "世界");
String result = template.render("template.btl");
```

### 🌐 自然语言处理

中文分词支持：

**支持的分词器**：
- Ansj
- Jieba
- JCseg
- MMseg4j
- HanLP
- SmartCN
- Word

### 🎭 Emoji 支持

Emoji 别名转换和 Unicode 处理（通过 emoji-java）

### 📡 FTP 操作

FTP/SFTP 客户端和服务器实现

### 🔢 验证码

生成验证码以防止机器人

-----

## 🚀 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-extra</artifactId>
    <version>x.x.x</version>
</dependency>
```

### 可选依赖

Bus Extra 使用可选依赖来最小化臃肿。仅添加所需内容：

```xml
<!-- JSON 处理（选择一个或多个） -->
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2</artifactId>
    <version>2.0.60</version>
</dependency>

<!-- 二维码 -->
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.3</version>
</dependency>

<!-- 拼音（选择一个） -->
<dependency>
    <groupId>com.github.stuxuhai</groupId>
    <artifactId>jpinyin</artifactId>
    <version>1.1.8</version>
</dependency>

<!-- 邮件 -->
<dependency>
    <groupId>jakarta.mail</groupId>
    <artifactId>jakarta.mail-api</artifactId>
    <version>2.1.3</version>
</dependency>

<!-- SSH -->
<dependency>
    <groupId>com.github.mwiede</groupId>
    <artifactId>jsch</artifactId>
    <version>2.27.2</version>
</dependency>

<!-- 压缩 -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-compress</artifactId>
    <version>1.28.0</version>
</dependency>

<!-- 消息队列（根据需要选择） -->
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
    <version>4.1.1</version>
</dependency>
```

-----

## 📝 使用示例

### 图像处理

```java
// 读取图像
BufferedImage image = ImageKit.read(new File("input.jpg"));

// 缩放到 50%
Image scaled = ImageKit.scale(image, 0.5f);

// 添加水印
ImageText watermark = new ImageText("© 我的公司", Color.RED, new Font("Arial", Font.BOLD, 32));
Image watermarked = ImageKit.pressText(scaled, watermark);

// 保存输出
ImageKit.write(watermarked, new File("output.png"));
ImageKit.flush(watermarked);
```

### 二维码生成

```java
// 基础二维码
QrCodeKit.generate(
    "https://www.example.com",
    300, 300,
    new File("qrcode.png")
);

// 自定义样式二维码
QrConfig config = QrConfig.of(350, 350)
    .setForeColor(new Color(0, 120, 215))  // 蓝色
    .setBackColor(Color.WHITE)
    .setMargin(2);

QrCodeKit.generate("自定义内容", config, new File("styled.png"));

// 解码二维码
String content = QrCodeKit.decode(new File("qrcode.png"));
System.out.println("二维码内容：" + content);
```

### 邮件发送

```java
// 配置账户
MailAccount account = new MailAccount();
account.setHost("smtp.example.com");
account.setPort(465);
account.setUser("sender@example.com");
account.setPass("password");
account.setSsl(true);

// 发送简单邮件
Mail mail = account.getMail();
mail.send("recipient@example.com", "测试主题", "邮件正文");

// 发送带附件的 HTML 邮件
mail.send(
    Collections.singleton("recipient@example.com"),
    null,  // 无抄送
    null,  // 无密送
    "HTML 邮件主题",
    "<h1>你好</h1><p>这是一封 <strong>HTML</strong> 邮件。</p>",
    true,  // 是 HTML
    Collections.singletonList(new File("attachment.pdf"))
);
```

### 压缩与解压

```java
// 创建 ZIP 归档
Archiver archiver = CompressBuilder.create(ZipArchiver.class);
archiver.add(new File("file1.txt"))
        .add(new File("file2.jpg"))
        .add(new File("documents/"))
        .build(new File("archive.zip"));

// 解压归档（自动检测格式）
Extractor extractor = CompressBuilder.createExtractor(new File("archive.zip"));
extractor.extract(new File("output/"));

// 流式压缩
try (OutputStream out = new FileOutputStream("compressed.zip")) {
    Archiver streamArchiver = CompressBuilder.create(ZipArchiver.class, out);
    streamArchiver.add(file1).build();
}
```

### JSON 操作

```java
// 序列化
User user = new User("张三", 30);
String json = JsonKit.toJsonString(user);

// 反序列化
User parsed = JsonKit.toPojo(json, User.class);

// 列表操作
List<User> users = Arrays.asList(user1, user2, user3);
String jsonArray = JsonKit.toJsonString(users);
List<User> parsedList = JsonKit.toList(jsonArray, User.class);

// Map 操作
Map<String, Object> map = JsonKit.toMap(json);
String value = JsonKit.getValue(json, "name");
```

### 拼音转换

```java
// 获取拼音
String pinyin = PinyinKit.getPinyin("中国"); // "zhong guo"

// 首字母用于排序
String first = PinyinKit.getFirstLetter("北京"); // "bj"

// 检查是否为中文
boolean isChinese = PinyinKit.isChinese('中'); // true

// 自定义分隔符
String withDash = PinyinKit.getPinyin("你好世界", "-"); // "ni-hao-shi-jie"
```

### SSH/SFTP 操作

```java
// 创建 SSH 会话
SessionConfig config = new SessionConfig();
config.setHost("example.com");
config.setPort(22);
config.setUser("username");
config.setPassword("password");

Session session = SessionFactory.create(JSchSession.class, config);
session.connect();

// 执行命令
String result = session.executeCommand("ls -la");

// SFTP 操作
Sftp sftp = session.getSftp();
sftp.upload("/local/file.txt", "/remote/file.txt");
sftp.download("/remote/file.txt", "/local/file.txt");

// 列出文件
List<SftpFile> files = sftp.listFiles("/remote/dir");

session.disconnect();
```

### 模板引擎

```java
// 使用 Beetl
TemplateEngine engine = TemplateFactory.create(BeetlEngine.class);
Template template = engine.getTemplate("hello.btl");

// 绑定数据
template.bind("name", "世界");
template.bind("items", Arrays.asList("A", "B", "C"));

// 渲染
String result = template.render();
```

-----

## 💡 最佳实践

### 1. 使用提供程序模式提高灵活性

```java
// ✅ 推荐：使用工厂切换提供程序
JsonProvider provider = JsonFactory.create(JsonProvider.JACKSON);

// ❌ 不推荐：直接依赖实现
ObjectMapper mapper = new ObjectMapper();
```

### 2. 正确的资源管理

```java
// ✅ 推荐：使用 try-with-resources 或显式刷新
BufferedImage image = ImageKit.read(file);
try {
    ImageKit.write(image, outputFile);
} finally {
    ImageKit.flush(image);
}

// ❌ 不推荐：忘记释放资源
BufferedImage image = ImageKit.read(file);
ImageKit.write(image, outputFile);
// 图像资源未释放！
```

### 3. 配置全局设置

```java
// ✅ 推荐：设置全局邮件账户
GlobalMailAccount.INSTANCE.getAccount().send(...);

// ✅ 推荐：全局配置 JSON 提供程序
JsonFactory.use(JsonProvider.FASTJSON2);
```

### 4. 适当处理异常

```java
// ✅ 推荐：处理特定异常
try {
    QrCodeKit.decode(qrFile);
} catch (QrCodeException e) {
    log.error("解码二维码失败", e);
}

// ❌ 不推荐：捕获所有异常
try {
    QrCodeKit.decode(qrFile);
} catch (Exception e) {
    // 吞噬所有错误
}
```

### 5. 使用适当的图像质量

```java
// ✅ 推荐：为 JPEG 压缩设置质量
QrConfig config = QrConfig.of(width, height)
    .setQuality(0.9f);  // 高质量

// ✅ 推荐：缩略图使用较低质量
ImageKit.compress(image, thumbnail, 0.7f);
```

-----

## ❓ 常见问题

### Q1: 如何在 JSON 提供程序之间切换？

```java
// 方法 1：全局设置（推荐）
JsonFactory.use(JsonProvider.JACKSON);

// 方法 2：特定实例
JsonProvider provider = JsonFactory.create(JsonProvider.GSON);
String json = provider.toJsonString(obj);
```

### Q2: 应该使用哪个拼音引擎？

| 引擎 | 性能 | 准确度 | 字典大小 | 推荐场景 |
| :--- | :--- | :--- | :--- | :--- |
| **TinyPinyin** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | 小 | 移动应用 |
| **JPinyin** | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | 中 | 通用 |
| **Pinyin4J** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | 大 | 桌面应用 |

```java
// 切换引擎
PinyinFactory.use(PinyinProvider.TINY_PINYIN);
```

### Q3: 如何生成带 Logo 的二维码？

```java
QrConfig config = QrConfig.of(300, 300);
// 生成后使用 ImageKit 合成 Logo
BufferedImage qr = QrCodeKit.generate(content, config);
BufferedImage logo = ImageKit.read(logoFile);

// 计算中心位置并合成
int x = (qr.getWidth() - logo.getWidth()) / 2;
int y = (qr.getHeight() - logo.getHeight()) / 2;
Graphics2D g = qr.createGraphics();
g.drawImage(logo, x, y, null);
g.dispose();
```

### Q4: 如何处理大文件压缩？

```java
// ✅ 大文件使用流式压缩
try (OutputStream out = new FileOutputStream("large.zip")) {
    Archiver archiver = CompressBuilder.create(ZipArchiver.class, out);
    archiver.add(largeFile).build();
}

// ✅ 或使用缓冲解压
Extractor extractor = CompressBuilder.createExtractor(archiveFile);
extractor.setBufferSize(8192);  // 8KB 缓冲
extractor.extract(destDir);
```

### Q5: 邮件发送失败，出现 SSL/TLS 错误？

```java
MailAccount account = new MailAccount();
// SSL（端口 465/993）
account.setSsl(true);
account.setSslPort(465);

// TLS（端口 587）
account.setStarttlsEnable(true);
account.setStarttlsRequired(true);
```

### Q6: 如何自定义二维码纠错级别？

```java
QrConfig config = QrConfig.of(300, 300)
    .setErrorCorrection(ErrorCorrectionLevel.H);  // 高（30%）
// 可用级别：L (7%)、M (15%)、Q (25%)、H (30%)
```

### Q7: SSH 连接超时处理？

```java
SessionConfig config = new SessionConfig();
config.setHost("example.com");
config.setTimeout(30000);  // 30 秒
config.setConnectTimeout(10000);  // 10 秒

Session session = SessionFactory.create(JSchSession.class, config);
try {
    session.connect();
} catch (SshException e) {
    log.error("连接超时", e);
}
```

### Q8: 如何无损压缩图像？

```java
// 使用 PNG 进行无损压缩
ImageKit.write(image, "png", outputStream);

// 或为 JPEG 设置质量为 1.0
ImageWriter.of(image, "jpg")
    .setQuality(1.0f)
    .write(outputFile);
```

-----

## 🔄 版本兼容性

| Bus Extra 版本 | Bus Core 版本 | JDK 版本 |
| :--- | :--- | :--- |
| 8.x | 8.x | 17+ |
| 7.x | 7.x | 11+ |

-----

## 🔧 配置示例

### 邮件配置（mail.setting）

```properties
# SMTP 服务器
mail.host=smtp.example.com
mail.port=465
mail.user=sender@example.com
mail.pass=password
mail.from=sender@example.com
mail.ssl.enable=true

# 字符集
mail.charset=UTF-8

# 超时
mail.timeout=30000
mail.connectiontimeout=10000
```

### JSON 提供程序配置

```java
// 配置提供程序
JsonFactory.use(JsonProvider.FASTJSON2);

// 使用自定义日期格式
JsonProvider provider = JsonFactory.get();
String json = provider.toJsonString(obj, "yyyy-MM-dd HH:mm:ss");
```

-----

## 📊 功能对比

| 功能 | Bus Extra | Apache Commons | 其他库 |
| :--- | :---: | :---: | :---: |
| **统一 API** | ✓ | ✗ | ✗ |
| **提供程序模式** | ✓ | ✗ | 部分 |
| **可选依赖** | ✓ | ✗ | ✗ |
| **中文支持** | ✓ | ✗ | 部分 |
| **现代 Java** | ✓ | ✗ | 部分 |
| **零侵入** | ✓ | N/A | ✗ |

-----

## 🎯 使用场景

- **企业应用**：全面的实用工具集
- **Web 应用**：图像处理、二维码、邮件
- **数据处理**：压缩、JSON、拼音
- **集成项目**：SSH/SFTP、消息队列
- **移动后端**：模板引擎、邮件、图像处理
- **微服务**：轻量级、可选依赖的功能

-----

## 🤝 贡献

欢迎贡献！请随时提交问题或拉取请求。

-----

## 📄 许可证

MIT License (MIT)

-----

## 🔗 链接

- **GitHub**：[https://github.com/818000/bus](https://github.com/818000/bus)
- **问题追踪**：[https://github.com/818000/bus/issues](https://github.com/818000/bus/issues)
