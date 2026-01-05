# 🚀 bus-storage: 统一云存储服务提供商

## 📖 项目介绍

bus-storage 是一个全面的云存储组件，提供与多个云存储提供商的统一集成，包括阿里云 OSS、七牛云、腾讯云 COS、百度云 BOS、华为云 OBS、京东云和又拍云。它提供工厂模式和 Spring 依赖注入集成方法，以实现最大的灵活性。

## ✨ 核心特性

- **多云支持**: 7+ 云存储提供商的统一 API
- **工厂模式**: 以编程方式创建存储提供商
- **Spring 集成**: 与 Spring Boot 自动配置
- **简单 API**: 上传、下载、删除和管理文件
- **灵活配置**: 支持多种存储配置
- **提供商切换**: 在云提供商之间轻松切换

## 🚀 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-storage</artifactId>
    <latestVersion>8.5.0</latestVersion>
</dependency>
```

### 提供商依赖

添加所需的云提供商 SDK:

```xml
<properties>
    <aliyun.oss.version>3.4.2</aliyun.oss.version>
    <baidu.bos.version>0.10.48</baidu.bos.version>
    <huawei.oss.version>3.0.5</huawei.oss.version>
    <jd.oss.version>1.11.136</jd.oss.version>
    <qiniu.oss.version>[7.2.0, 7.2.99]</qiniu.oss.version>
    <tencent.oss.version>5.5.9</tencent.oss.version>
    <upyun.oss.version>4.0.1</upyun.oss.version>
</properties>

<!-- 阿里云 OSS -->
<dependency>
    <groupId>com.aliyun.oss</groupId>
    <artifactId>aliyun-sdk-oss</artifactId>
    <version>${aliyun.oss.version}</version>
    <optional>true</optional>
</dependency>

<!-- 百度云 BOS -->
<dependency>
    <groupId>com.baidubce</groupId>
    <artifactId>bce-java-sdk</artifactId>
    <version>${baidu.bos.version}</version>
    <optional>true</optional>
</dependency>

<!-- 华为云 OBS -->
<dependency>
    <groupId>com.huawei.storage</groupId>
    <artifactId>esdk-obs-java</artifactId>
    <version>${huawei.oss.version}</version>
    <optional>true</optional>
</dependency>

<!-- 京东云 OSS -->
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-java-sdk</artifactId>
    <version>${jd.oss.version}</version>
    <optional>true</optional>
</dependency>

<!-- 七牛云 -->
<dependency>
    <groupId>com.qiniu</groupId>
    <artifactId>qiniu-java-sdk</artifactId>
    <version>${qiniu.oss.version}</version>
    <optional>true</optional>
</dependency>

<!-- 腾讯云 COS -->
<dependency>
    <groupId>com.qcloud</groupId>
    <artifactId>cos_api</artifactId>
    <version>${tencent.oss.version}</version>
    <optional>true</optional>
</dependency>

<!-- 又拍云 -->
<dependency>
    <groupId>com.upyun</groupId>
    <artifactId>java-sdk</artifactId>
    <version>${upyun.oss.version}</version>
    <optional>true</optional>
</dependency>
```

## 📝 使用示例

### 示例 1: 工厂模式 - 阿里云 OSS

```java
// 创建上下文
Context context = new Context();
context.setAccessKey("your-access-key");
context.setSecretKey("your-secret-key");
context.setEndpoint("oss-cn-hangzhou.aliyuncs.com");
context.setBucketName("your-bucket");

// 创建提供商
StorageProvider provider = new AliyunOssProvider(context);

// 上传文件
provider.upload("path/to/file.txt", new File("/local/file.txt"));

// 下载文件
provider.download("path/to/file.txt", "/local/downloaded.txt");

// 删除文件
provider.delete("path/to/file.txt");
```

### 示例 2: Spring 注入 - 七牛云

```java
@Service
public class FileService {

    @Resource
    private StorageProviderService storageService;

    public void uploadFile(File file) {
        // 获取七牛提供商
        StorageProvider provider = storageService.get(Registry.QINIU);

        // 上传文件
        provider.upload("test/logo.png", file);
    }

    public String getFileUrl(String key) {
        StorageProvider provider = storageService.get(Registry.QINIU);
        return provider.getUrl("test/logo.png");
    }
}
```

### 示例 3: 带进度跟踪的上传

```java
Context context = new Context();
context.setAccessKey("your-access-key");
context.setSecretKey("your-secret-key");

StorageProvider provider = new AliyunOssProvider(context);

// 带进度回调上传
provider.upload("path/to/file.txt", new File("/local/file.txt"), progress -> {
    System.out.println("上传进度: " + progress.getPercent() + "%");
});
```

### 示例 4: 批量上传

```java
@Service
public class BatchUploadService {

    @Resource
    private StorageProviderService storageService;

    public void uploadMultipleFiles(List<File> files) {
        StorageProvider provider = storageService.get(Registry.ALIYUN);

        for (int i = 0; i < files.size(); i++) {
            String key = "batch/file_" + i + ".txt";
            provider.upload(key, files.get(i));
        }
    }
}
```

### 示例 5: 生成预签名 URL

```java
@Service
public class FileShareService {

    @Resource
    private StorageProviderService storageService;

    public String getShareUrl(String key, int expireSeconds) {
        StorageProvider provider = storageService.get(Registry.TENCENT);

        // 生成 1 小时后过期的 URL
        return provider.getPresignedUrl(key, expireSeconds);
    }
}
```

### 示例 6: 列出文件

```java
@Service
public class FileManagementService {

    @Resource
    private StorageProviderService storageService;

    public List<String> listFiles(String prefix) {
        StorageProvider provider = storageService.get(Registry.BAIDU);

        // 列出前缀下的所有文件
        return provider.list(prefix);
    }
}
```

### 示例 7: 检查文件存在

```java
@Service
public class FileCheckService {

    @Resource
    private StorageProviderService storageService;

    public boolean fileExists(String key) {
        StorageProvider provider = storageService.get(Registry.HUAWEI);
        return provider.exists(key);
    }
}
```

### 示例 8: 获取文件元数据

```java
@Service
public class FileMetadataService {

    @Resource
    private StorageProviderService storageService;

    public FileInfo getFileInfo(String key) {
        StorageProvider provider = storageService.get(Registry.UPYUN);
        return provider.getFileInfo(key);
    }
}
```

## 🔧 配置

### YAML 配置

```yaml
extend:
  storage:
    # 缓存配置
    cache:
      type: DEFAULT
      timeout: 1L

    # 提供商配置
    type:
      # 七牛云
      qiniu:
        accessKey: your-qiniu-access-key
        secretKey: your-qiniu-secret-key
        bucket: your-bucket-name
        domain: http://your-domain.com
        prefix: uploads/

      # 阿里云 OSS
      aliyun:
        accessKey: your-aliyun-access-key
        secretKey: your-aliyun-secret-key
        endpoint: oss-cn-hangzhou.aliyuncs.com
        bucket: your-bucket-name
        prefix: uploads/

      # 腾讯云 COS
      tencent:
        accessKey: your-tencent-secret-id
        secretKey: your-tencent-secret-key
        region: ap-guangzhou
        bucket: your-bucket-name
        prefix: uploads/

      # 百度云 BOS
      baidu:
        accessKey: your-baidu-access-key
        secretKey: your-baidu-secret-key
        endpoint: bj.bcebos.com
        bucket: your-bucket-name
        prefix: uploads/

      # 华为云 OBS
      huawei:
        accessKey: your-huawei-access-key
        secretKey: your-huawei-secret-key
        endpoint: obs.cn-north-1.myhuaweicloud.com
        bucket: your-bucket-name
        prefix: uploads/

      # 京东云 OSS
      jd:
        accessKey: your-jd-access-key
        secretKey: your-jd-secret-key
        endpoint: s3.cn-north-1.jdcloud-oss.com
        bucket: your-bucket-name
        prefix: uploads/

      # 又拍云
      upyun:
        accessKey: your-upyun-operator
        secretKey: your-upyun-password
        bucket: your-bucket-name
        domain: http://your-domain.com
        prefix: uploads/
```

### 在 Spring Boot 中启用存储

```java
@SpringBootApplication
@EnableStorage
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 配置参数

| 参数 | 类型 | 必需 | 描述 |
|:---|:---|:---|:---|
| accessKey | String | 是 | 用于身份验证的访问密钥/ID |
| secretKey | String | 是 | 用于身份验证的密钥 |
| bucket | String | 是 | 存储桶/容器名称 |
| endpoint/domain | String | 提供商 | 端点 URL 或自定义域 |
| region | String | 提供商 | 服务区域 |
| prefix | String | 否 | 所有文件的路径前缀 |

### 提供商注册表

| 注册表键 | 提供商 | 描述 |
|:---|:---|:---|
| ALIYUN | AliyunOssProvider | 阿里云对象存储服务 |
| QINIU | QiniuOssProvider | 七牛云存储 |
| TENCENT | TencentOssProvider | 腾讯云对象存储 |
| BAIDU | BaiduOssProvider | 百度云对象存储 |
| HUAWEI | HuaweiOssProvider | 华为云对象存储服务 |
| JD | JdOssProvider | 京东云对象存储服务 |
| UPYUN | UpyunOssProvider | 又拍云存储 |

## 💡 最佳实践

### 1. 使用环境变量存储凭据

```yaml
extend:
  storage:
    type:
      aliyun:
        accessKey: ${ALIYUN_ACCESS_KEY}
        secretKey: ${ALIYUN_SECRET_KEY}
        bucket: ${ALIYUN_BUCKET}
```

### 2. 实现回退策略

```java
@Service
public class ReliableStorageService {

    @Resource
    private StorageProviderService storageService;

    public void uploadWithFallback(String key, File file) {
        try {
            // 尝试主存储
            StorageProvider primary = storageService.get(Registry.ALIYUN);
            primary.upload(key, file);
        } catch (Exception e) {
            // 回退到辅助存储
            StorageProvider backup = storageService.get(Registry.QINIU);
            backup.upload(key, file);
        }
    }
}
```

### 3. 使用前缀组织文件

```java
public class FileOrganizer {
    private static final String UPLOAD_PATH = "uploads/";
    private static final String AVATAR_PATH = "avatars/";
    private static final String DOCUMENT_PATH = "documents/";

    public void uploadAvatar(File file) {
        String key = AVATAR_PATH + file.getName();
        provider.upload(key, file);
    }
}
```

### 4. 使用多个存储桶

```yaml
extend:
  storage:
    type:
      aliyun:
        bucket: ${ALIYUN_PUBLIC_BUCKET}  # 公共文件
      aliyun-private:
        accessKey: ${ALIYUN_ACCESS_KEY}
        secretKey: ${ALIYUN_SECRET_KEY}
        bucket: ${ALIYUN_PRIVATE_BUCKET}  # 私有文件
```

### 5. 实现重试逻辑

```java
@Service
public class ResilientStorageService {

    @Resource
    private StorageProviderService storageService;

    @Retryable(value = {StorageException.class}, maxAttempts = 3)
    public void uploadWithRetry(String key, File file) {
        StorageProvider provider = storageService.get(Registry.ALIYUN);
        provider.upload(key, file);
    }
}
```

## ❓ 常见问题

### 问: 应该选择哪个云提供商？

答: 考虑以下因素:
- **地理位置**: 选择距离最近的提供商
- **成本**: 比较存储和带宽的定价
- **功能**: CDN、处理、分析能力
- **合规性**: 数据驻留和认证要求

### 问: 可以同时使用多个云提供商吗？

答: 可以！您可以配置多个提供商并在它们之间切换:

```java
StorageProvider aliyun = storageService.get(Registry.ALIYUN);
StorageProvider qiniu = storageService.get(Registry.QINIU);

// 为不同目的使用不同的提供商
aliyun.upload("backup/file.dat", file);
qiniu.upload("cdn/file.dat", file);
```

### 问: 如何处理大文件上传？

答: 对于大于 100MB 的文件使用分片上传:

```java
provider.uploadLargeFile("path/to/large.dat", largeFile, chunkSize);
```

### 问: 有内置缓存吗？

答: 有，在 application.yml 中配置缓存:

```yaml
extend:
  storage:
    cache:
      type: REDIS  # 或 DEFAULT、GUAVA
      timeout: 3600  # 缓存持续时间(秒)
```

### 问: 如何为文件设置自定义元数据？

答: 上传时使用元数据参数:

```java
Map<String, String> metadata = new HashMap<>();
metadata.put("Content-Type", "image/png");
metadata.put("author", "user123");

provider.upload("path/file.png", file, metadata);
```

### 问: 可以使用自定义域名吗？

答: 可以，配置 domain 参数:

```yaml
extend:
  storage:
    type:
      qiniu:
        domain: https://cdn.yourdomain.com
```

## 🔍 高级特性

### 自定义存储提供商

```java
public class CustomStorageProvider implements StorageProvider {

    @Override
    public void upload(String key, File file) {
        // 自定义实现
    }

    @Override
    public void download(String key, String localPath) {
        // 自定义实现
    }

    // 实现其他方法...
}
```

### 文件处理管道

```java
@Service
public class FileProcessingService {

    @Resource
    private StorageProviderService storageService;

    public void processAndUpload(File file) {
        // 压缩图像
        File compressed = ImageCompressor.compress(file);

        // 上传压缩版本
        StorageProvider provider = storageService.get(Registry.ALIYUN);
        provider.upload("processed/" + file.getName(), compressed);

        // 删除临时文件
        compressed.delete();
    }
}
```

### 存储健康检查

```java
@Component
public class StorageHealthCheck implements HealthIndicator {

    @Resource
    private StorageProviderService storageService;

    @Override
    public Health health() {
        try {
            StorageProvider provider = storageService.get(Registry.ALIYUN);
            provider.exists("health-check.txt");
            return Health.up().build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
```

## 🔄 版本兼容性

- **JDK**: 8, 11, 17, 21+
- **Spring Boot**: 2.7.x, 3.x
- **支持的提供商**:
  - 阿里云 OSS: 3.4.2+
  - 七牛云: 7.2.x
  - 腾讯云 COS: 5.5.9+
  - 百度云 BOS: 0.10.48+
  - 华为云 OBS: 3.0.5+
  - 京东云 OSS: 1.11.136+
  - 又拍云: 4.0.1+

## 📚 相关模块

- [bus-core](../bus-core): 核心工具
- [bus-extra](../bus-extra): 包括图像处理的扩展功能
- [bus-starter](../bus-starter): Spring Boot 集成
