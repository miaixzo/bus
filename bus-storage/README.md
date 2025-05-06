#### 项目说明

文件存储组件,支持阿里云、七牛集成，提供了工厂模式和spring注入两种集成方式。

#### 添加依赖(华为SDK需要添加至私服)

```xml
    <aliyun.oss.version>3.4.2</aliyun.oss.version>
    <baidu.bos.version>0.10.48</baidu.bos.version>
    <huawei.oss.version>3.0.5</huawei.oss.version>
    <jd.oss.version>1.11.136</jd.oss.version>
    <qiniu.oss.version>[7.2.0, 7.2.99]</qiniu.oss.version>
    <tencent.oss.version>5.5.9</tencent.oss.version>
    <upyun.oss.version>4.0.1</upyun.oss.version>
```

```xml
    <!-- 阿里云 -->
    <dependency>
        <groupId>com.aliyun.oss</groupId>
        <artifactId>aliyun-sdk-oss</artifactId>
        <version>${aliyun.oss.version}</version>
        <optional>true</optional>
        <exclusions>
            <exclusion>
                <groupId>org.apache.httpcomponents</groupId>
                <artifactId>httpclient</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    <!-- 百度云 -->
    <dependency>
        <groupId>com.baidubce</groupId>
        <artifactId>bce-java-sdk</artifactId>
        <version>${baidu.bos.version}</version>
        <optional>true</optional>
        <exclusions>
            <exclusion>
                <groupId>ch.qos.logback</groupId>
                <artifactId>logback-core</artifactId>
            </exclusion>
            <exclusion>
                <groupId>ch.qos.logback</groupId>
                <artifactId>logback-classic</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    <!-- 华为云 -->
    <dependency>
        <groupId>com.huawei.storage</groupId>
        <artifactId>esdk-obs-java</artifactId>
        <version>${huawei.oss.version}</version>
        <optional>true</optional>
        <exclusions>
            <exclusion>
                <groupId>org.apache.logging.log4j</groupId>
                <artifactId>log4j-core</artifactId>
            </exclusion>
            <exclusion>
                <groupId>org.apache.logging.log4j</groupId>
                <artifactId>log4j-api</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    <!-- 京东云 -->
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
    <!-- 腾讯云 -->
    <dependency>
        <groupId>com.qcloud</groupId>
        <artifactId>cos_api</artifactId>
        <version>${tencent.oss.version}</version>
        <optional>true</optional>
        <exclusions>
            <exclusion>
                <groupId>org.slf4j</groupId>
                <artifactId>slf4j-log4j12</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    <!-- 又拍云 -->
    <dependency>
        <groupId>com.upyun</groupId>
        <artifactId>java-sdk</artifactId>
        <version>${upyun.oss.version}</version>
        <optional>true</optional>
    </dependency>
```

#### 工厂模式

##### 配置文件,xx.yml

# 服务提供者可选值(qiniu，aliyun)

```yaml
extend:
  storage:
    cache:
      type: DEFAULT
      timeout: 1L
    type:
      qliyun:
        accessKey: 10******85
        secretKey: 1f7d************************d629e
        prefix: http://oauth.miaixz.org/qq/callback
      baidu:
        accessKey: 10******85
        secretKey: 1f7d************************d629e
        prefix: http://oauth.miaixz.org/qq/callback
......
```

参数说明：

- bucket：多个组用“,”隔开，对应的`bucket`

##### 用法①

```java
Context context = new Context();
context.setAccessKey(xxx);
context.setSecretKey(xxx);
StorageProvider provider = new AliyunossProvider(context);
provider.upload("test", null, new File("/Users/leaves/logo.gif"));
```

##### 用法②

```java
@Resource
StorageProviderService service;
        service.get(Registry.ALIYUN);
        provider.upload("test",null,new File("/Users/leaves/logo.gif"));
```
