# 🚀 bus-storage: Unified Cloud Storage Service Provider

## 📖 Project Introduction

bus-storage is a comprehensive cloud storage component that provides unified integration with multiple cloud storage providers including Alibaba Cloud OSS, Qiniu Cloud, Tencent Cloud COS, Baidu Cloud BOS, Huawei Cloud OBS, JD Cloud, and UpYun. It offers both factory pattern and Spring dependency injection integration methods for maximum flexibility.

## ✨ Core Features

- **Multi-Cloud Support**: Unified API for 7+ cloud storage providers
- **Factory Pattern**: Create storage providers programmatically
- **Spring Integration**: Auto-configuration with Spring Boot
- **Simple API**: Upload, download, delete, and manage files
- **Flexible Configuration**: Support for multiple storage configurations
- **Provider Switching**: Easily switch between cloud providers

## 🚀 Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-storage</artifactId>
    <latestVersion>8.5.0</latestVersion>
</dependency>
```

### Provider Dependencies

Add the required cloud provider SDK(s):

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

<!-- Alibaba Cloud OSS -->
<dependency>
    <groupId>com.aliyun.oss</groupId>
    <artifactId>aliyun-sdk-oss</artifactId>
    <version>${aliyun.oss.version}</version>
    <optional>true</optional>
</dependency>

<!-- Baidu Cloud BOS -->
<dependency>
    <groupId>com.baidubce</groupId>
    <artifactId>bce-java-sdk</artifactId>
    <version>${baidu.bos.version}</version>
    <optional>true</optional>
</dependency>

<!-- Huawei Cloud OBS -->
<dependency>
    <groupId>com.huawei.storage</groupId>
    <artifactId>esdk-obs-java</artifactId>
    <version>${huawei.oss.version}</version>
    <optional>true</optional>
</dependency>

<!-- JD Cloud OSS -->
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-java-sdk</artifactId>
    <version>${jd.oss.version}</version>
    <optional>true</optional>
</dependency>

<!-- Qiniu Cloud -->
<dependency>
    <groupId>com.qiniu</groupId>
    <artifactId>qiniu-java-sdk</artifactId>
    <version>${qiniu.oss.version}</version>
    <optional>true</optional>
</dependency>

<!-- Tencent Cloud COS -->
<dependency>
    <groupId>com.qcloud</groupId>
    <artifactId>cos_api</artifactId>
    <version>${tencent.oss.version}</version>
    <optional>true</optional>
</dependency>

<!-- UpYun -->
<dependency>
    <groupId>com.upyun</groupId>
    <artifactId>java-sdk</artifactId>
    <version>${upyun.oss.version}</version>
    <optional>true</optional>
</dependency>
```

## 📝 Usage Examples

### Example 1: Factory Pattern - Aliyun OSS

```java
// Create context
Context context = new Context();
context.setAccessKey("your-access-key");
context.setSecretKey("your-secret-key");
context.setEndpoint("oss-cn-hangzhou.aliyuncs.com");
context.setBucketName("your-bucket");

// Create provider
StorageProvider provider = new AliyunOssProvider(context);

// Upload file
provider.upload("path/to/file.txt", new File("/local/file.txt"));

// Download file
provider.download("path/to/file.txt", "/local/downloaded.txt");

// Delete file
provider.delete("path/to/file.txt");
```

### Example 2: Spring Injection - Qiniu Cloud

```java
@Service
public class FileService {

    @Resource
    private StorageProviderService storageService;

    public void uploadFile(File file) {
        // Get Qiniu provider
        StorageProvider provider = storageService.get(Registry.QINIU);

        // Upload file
        provider.upload("test/logo.png", file);
    }

    public String getFileUrl(String key) {
        StorageProvider provider = storageService.get(Registry.QINIU);
        return provider.getUrl("test/logo.png");
    }
}
```

### Example 3: Upload with Progress Tracking

```java
Context context = new Context();
context.setAccessKey("your-access-key");
context.setSecretKey("your-secret-key");

StorageProvider provider = new AliyunOssProvider(context);

// Upload with progress callback
provider.upload("path/to/file.txt", new File("/local/file.txt"), progress -> {
    System.out.println("Upload progress: " + progress.getPercent() + "%");
});
```

### Example 4: Batch Upload

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

### Example 5: Generate Presigned URL

```java
@Service
public class FileShareService {

    @Resource
    private StorageProviderService storageService;

    public String getShareUrl(String key, int expireSeconds) {
        StorageProvider provider = storageService.get(Registry.TENCENT);

        // Generate URL that expires in 1 hour
        return provider.getPresignedUrl(key, expireSeconds);
    }
}
```

### Example 6: List Files

```java
@Service
public class FileManagementService {

    @Resource
    private StorageProviderService storageService;

    public List<String> listFiles(String prefix) {
        StorageProvider provider = storageService.get(Registry.BAIDU);

        // List all files under prefix
        return provider.list(prefix);
    }
}
```

### Example 7: Check File Existence

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

### Example 8: Get File Metadata

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

## 🔧 Configuration

### YAML Configuration

```yaml
extend:
  storage:
    # Cache configuration
    cache:
      type: DEFAULT
      timeout: 1L

    # Provider configurations
    type:
      # Qiniu Cloud
      qiniu:
        accessKey: your-qiniu-access-key
        secretKey: your-qiniu-secret-key
        bucket: your-bucket-name
        domain: http://your-domain.com
        prefix: uploads/

      # Alibaba Cloud OSS
      aliyun:
        accessKey: your-aliyun-access-key
        secretKey: your-aliyun-secret-key
        endpoint: oss-cn-hangzhou.aliyuncs.com
        bucket: your-bucket-name
        prefix: uploads/

      # Tencent Cloud COS
      tencent:
        accessKey: your-tencent-secret-id
        secretKey: your-tencent-secret-key
        region: ap-guangzhou
        bucket: your-bucket-name
        prefix: uploads/

      # Baidu Cloud BOS
      baidu:
        accessKey: your-baidu-access-key
        secretKey: your-baidu-secret-key
        endpoint: bj.bcebos.com
        bucket: your-bucket-name
        prefix: uploads/

      # Huawei Cloud OBS
      huawei:
        accessKey: your-huawei-access-key
        secretKey: your-huawei-secret-key
        endpoint: obs.cn-north-1.myhuaweicloud.com
        bucket: your-bucket-name
        prefix: uploads/

      # JD Cloud OSS
      jd:
        accessKey: your-jd-access-key
        secretKey: your-jd-secret-key
        endpoint: s3.cn-north-1.jdcloud-oss.com
        bucket: your-bucket-name
        prefix: uploads/

      # UpYun
      upyun:
        accessKey: your-upyun-operator
        secretKey: your-upyun-password
        bucket: your-bucket-name
        domain: http://your-domain.com
        prefix: uploads/
```

### Enable Storage in Spring Boot

```java
@SpringBootApplication
@EnableStorage
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### Configuration Parameters

| Parameter | Type | Required | Description |
|:---|:---|:---|:---|
| accessKey | String | Yes | Access key/ID for authentication |
| secretKey | String | Yes | Secret key for authentication |
| bucket | String | Yes | Bucket/container name |
| endpoint/domain | String | Provider | Endpoint URL or custom domain |
| region | String | Provider | Region for the service |
| prefix | String | No | Path prefix for all files |

### Provider Registry

| Registry Key | Provider | Description |
|:---|:---|:---|
| ALIYUN | AliyunOssProvider | Alibaba Cloud Object Storage Service |
| QINIU | QiniuOssProvider | Qiniu Cloud Storage |
| TENCENT | TencentOssProvider | Tencent Cloud Object Storage |
| BAIDU | BaiduOssProvider | Baidu Cloud Object Storage |
| HUAWEI | HuaweiOssProvider | Huawei Cloud Object Storage Service |
| JD | JdOssProvider | JD Cloud Object Storage Service |
| UPYUN | UpyunOssProvider | UpYun Cloud Storage |

## 💡 Best Practices

### 1. Use Environment Variables for Credentials

```yaml
extend:
  storage:
    type:
      aliyun:
        accessKey: ${ALIYUN_ACCESS_KEY}
        secretKey: ${ALIYUN_SECRET_KEY}
        bucket: ${ALIYUN_BUCKET}
```

### 2. Implement Fallback Strategy

```java
@Service
public class ReliableStorageService {

    @Resource
    private StorageProviderService storageService;

    public void uploadWithFallback(String key, File file) {
        try {
            // Try primary storage
            StorageProvider primary = storageService.get(Registry.ALIYUN);
            primary.upload(key, file);
        } catch (Exception e) {
            // Fallback to secondary storage
            StorageProvider backup = storageService.get(Registry.QINIU);
            backup.upload(key, file);
        }
    }
}
```

### 3. Organize Files with Prefixes

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

### 4. Use Multiple Buckets

```yaml
extend:
  storage:
    type:
      aliyun:
        bucket: ${ALIYUN_PUBLIC_BUCKET}  # Public files
      aliyun-private:
        accessKey: ${ALIYUN_ACCESS_KEY}
        secretKey: ${ALIYUN_SECRET_KEY}
        bucket: ${ALIYUN_PRIVATE_BUCKET}  # Private files
```

### 5. Implement Retry Logic

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

## ❓ FAQ

### Q: Which cloud provider should I choose?

A: Consider factors like:
- **Geographic location**: Choose provider with nearest region
- **Cost**: Compare pricing for storage and bandwidth
- **Features**: CDN, processing, analytics capabilities
- **Compliance**: Data residency and certification requirements

### Q: Can I use multiple cloud providers simultaneously?

A: Yes! You can configure multiple providers and switch between them:

```java
StorageProvider aliyun = storageService.get(Registry.ALIYUN);
StorageProvider qiniu = storageService.get(Registry.QINIU);

// Use different providers for different purposes
aliyun.upload("backup/file.dat", file);
qiniu.upload("cdn/file.dat", file);
```

### Q: How do I handle large file uploads?

A: Use multipart upload for files larger than 100MB:

```java
provider.uploadLargeFile("path/to/large.dat", largeFile, chunkSize);
```

### Q: Is there built-in caching?

A: Yes, configure cache in application.yml:

```yaml
extend:
  storage:
    cache:
      type: REDIS  # or DEFAULT, GUAVA
      timeout: 3600  # Cache duration in seconds
```

### Q: How do I set custom metadata for files?

A: Use the metadata parameter when uploading:

```java
Map<String, String> metadata = new HashMap<>();
metadata.put("Content-Type", "image/png");
metadata.put("author", "user123");

provider.upload("path/file.png", file, metadata);
```

### Q: Can I use custom domain names?

A: Yes, configure the domain parameter:

```yaml
extend:
  storage:
    type:
      qiniu:
        domain: https://cdn.yourdomain.com
```

## 🔍 Advanced Features

### Custom Storage Provider

```java
public class CustomStorageProvider implements StorageProvider {

    @Override
    public void upload(String key, File file) {
        // Custom implementation
    }

    @Override
    public void download(String key, String localPath) {
        // Custom implementation
    }

    // Implement other methods...
}
```

### File Processing Pipeline

```java
@Service
public class FileProcessingService {

    @Resource
    private StorageProviderService storageService;

    public void processAndUpload(File file) {
        // Compress image
        File compressed = ImageCompressor.compress(file);

        // Upload compressed version
        StorageProvider provider = storageService.get(Registry.ALIYUN);
        provider.upload("processed/" + file.getName(), compressed);

        // Delete temporary file
        compressed.delete();
    }
}
```

### Storage Health Check

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

## 🔄 Version Compatibility

- **JDK**: 8, 11, 17, 21+
- **Spring Boot**: 2.7.x, 3.x
- **Supported Providers**:
  - Alibaba Cloud OSS: 3.4.2+
  - Qiniu Cloud: 7.2.x
  - Tencent Cloud COS: 5.5.9+
  - Baidu Cloud BOS: 0.10.48+
  - Huawei Cloud OBS: 3.0.5+
  - JD Cloud OSS: 1.11.136+
  - UpYun: 4.0.1+

## 📚 Related Modules

- [bus-core](../bus-core): Core utilities
- [bus-extra](../bus-extra): Extended functionality including image processing
- [bus-starter](../bus-starter): Spring Boot integration
