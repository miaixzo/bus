# 🚀 bus-shade: Java JAR 加密和代码生成工具包

## 📖 项目介绍

bus-shade 是一个功能强大的 Java 工具包，提供全面的 JAR 加密和代码生成功能。它为 Spring Boot JAR
文件提供安全加密，以保护您的源代码和字节码免受反编译，并包含自动化代码生成工具以加速开发。

## ✨ 核心特性

### JAR 加密

- **零代码入侵**: 加载编译的 JAR 包而无需修改源代码
- **内存解密**: 完全内存解密，防止源代码和字节码泄漏
- **算法支持**: 支持所有 JDK 内置加密算法 (AES、DES 等)
- **选择性加密**: 选择要加密的特定字节码或资源文件，避免计算浪费
- **透明运行时**: 无需修改 Tomcat、Spring 或其他源代码
- **Maven 插件**: Maven 插件集成，打包时自动加密
- **依赖加密**: 支持加密 WEB-INF/lib 或 BOOT-INF/lib 中的依赖

### 代码生成

- **Lombok 集成**: 与 Lombok 注解无缝集成
- **Swagger 支持**: 自动 API 文档生成
- **MyBatis 集成**: 自动生成 Entity、Mapper、Service 和 Controller 类
- **验证**: 内置格式验证注解
- **RESTful API**: 自动生成 CRUD 操作并与 Swagger 集成

### 数据库文档

- **自动生成**: 生成全面的数据库设计文档
- **多种格式**: 支持 HTML 和其他文档格式
- **灵活配置**: 按名称、前缀或后缀过滤表

## 🚀 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-shade</artifactId>
    <latestVersion>8.x.x</latestVersion>
</dependency>
```

## 📝 使用示例

### 示例 1: 加密 Spring Boot JAR (标准模式)

```java
String password = "forest";
Key key = Builder.key(password);
Boot.encrypt("/path/to/read/forest.jar", "/path/to/save/enforest.jar", key);
```

### 示例 2: 加密 Spring Boot JAR (危险模式 - 无需密码)

**⚠️ 警告**: 在此模式下，密钥存储在 META-INF/MANIFEST.MF 中。请谨慎使用！

```java
String password = "forest";
Key key = Builder.key(password);
Boot.encrypt("/path/to/read/forest.jar", "/path/to/save/enforest.jar", key, Builder.MODE_DANGER);
```

### 示例 3: 解密 Spring Boot JAR

```java
String password = "forest";
Key key = Builder.key(password);
Boot.decrypt("/path/to/read/enforest.jar", "/path/to/save/deforest.jar", key);
```

### 示例 4: 加密标准 JAR

```java
String password = "forest";
Key key = Builder.key(password);
Jar.encrypt("/path/to/read/forest.jar", "/path/to/save/enforest.jar", key);
```

### 示例 5: 使用密码提示运行加密的 JAR

```bash
java -jar /path/to/enforest.jar
# 提示时输入密码
```

### 示例 6: 使用命令行密码运行加密的 JAR

```bash
java -jar /path/to/enforest.jar --xjar.password=forest
```

### 示例 7: 使用密钥文件运行加密的 JAR (后台模式)

```bash
nohup java -jar /path/to/enforest.jar --xjar.keyfile=/path/to/forest.key
```

### 示例 8: 使用过滤器加密 (选择性加密)

```java
// 仅加密 com.company.project 包下的类
Boot.encrypt(
    "/path/to/read/plaintext.jar",
    "/path/to/save/encrypted.jar",
    "forest",
    (entry) -> {
        String name = entry.getName();
        String pkg = "com/company/project/";
        return name.startsWith(pkg);
    }
);
```

## 🔧 配置

### 启动参数

| 参数             | 描述         | 默认值       | 示例             |
|:-----------------|:-------------|:-------------|:-----------------|
| --xjar.password  | 密码         | 必需         | forest           |
| --xjar.algorithm | 密钥算法     | AES          | AES, DES         |
| --xjar.keysize   | 密钥大小     | 128          | 128, 256         |
| --xjar.ivsize    | 向量大小     | 128          | 128              |
| --xjar.keyfile   | 密钥文件路径 | ./forest.key | /path/to/key.key |

### 密钥文件格式

密钥文件使用 properties 格式:

```properties
password: PASSWORD
algorithm: AES
keysize: 128
ivsize: 128
hold: false
```

**参数说明**:

| 参数      | 描述         | 默认值 | 说明                                  |
|:----------|:-------------|:-------|:--------------------------------------|
| password  | 密码字符串   | 必需   | 任何字符串                            |
| algorithm | 密钥算法     | AES    | 支持所有 JDK 内置算法                 |
| keysize   | 密钥大小     | 128    | 取决于算法                            |
| ivsize    | 向量大小     | 128    | 取决于算法                            |
| hold      | 保留密钥文件 | false  | 如果不是 true/1/yes/y，读取后自动删除 |

### Maven 插件配置

```xml
<plugin>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-shade-maven-plugin</artifactId>
    <version>8.x.x</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>build</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <password>forest</password>
        <algorithm>AES</algorithm>
        <keySize>128</keySize>
        <ivSize>128</ivSize>
        <mode>0</mode>
        <includes>
            <include>com/company/project/**</include>
            <include>mapper/*Mapper.xml</include>
        </includes>
        <excludes>
            <exclude>static/**</exclude>
            <exclude>META-INF/resources/**</exclude>
        </excludes>
    </configuration>
</plugin>
```

### Maven 命令行

```bash
# 直接加密
mvn xjar:build -Dxjar.password=forest

# 使用自定义输出目录
mvn xjar:build -Dxjar.password=forest -Dxjar.targetDir=/directory/to/save

# 与打包阶段集成
mvn clean package -Dxjar.password=forest
```

## 💡 最佳实践

### 1. 使用过滤器进行选择性加密

**Ant 表达式过滤器**:

```java
Boot.encrypt(plaintext, encrypted, password, new AntEntryFilter("com/company/project/**"));
Boot.encrypt(plaintext, encrypted, password, new AntEntryFilter("mapper/*Mapper.xml"));
```

**正则表达式过滤器**:

```java
Boot.encrypt(plaintext, encrypted, password, new RegexEntryFilter("com/company/project/(.+)"));
```

### 2. 组合过滤器

```java
// AND 操作 - 所有过滤器必须匹配
XEntryFilter and = Builder.and()
    .mix(new AntEntryFilter("com/company/project/**"))
    .mix(new AntEntryFilter("*/**.class"));

// OR 操作 - 任何过滤器都可以匹配
XEntryFilter or = Builder.or()
    .mix(new AntEntryFilter("com/company/project/**"))
    .mix(new AntEntryFilter("mapper/*Mapper.xml"));

// NOT 操作 - 排除匹配的资源
XEntryFilter not = Builder.not(
    Builder.or()
        .mix(new AntEntryFilter("static/**"))
        .mix(new AntEntryFilter("META-INF/resources/**"))
);
```

### 3. 静态资源处理

⚠️ **重要**: 静态文件应从加密中排除，因为它们在加密时会膨胀，导致浏览器 Content-Length 不匹配。

```java
// 排除静态资源
XEntryFilter filter = Builder.not(
    Builder.or()
        .mix(new AntEntryFilter("static/**"))
        .mix(new AntEntryFilter("META-INF/resources/**"))
);
```

### 4. 代码生成配置

```java
// 基本项目信息
public static final String PROJECT = "bus-shade";
public static final String AUTHOR = "Kimi Liu";
public static final String VERSION = "1.0.0";

// 数据库连接
public static final String URL = "jdbc:mysql://localhost:3306/database";
public static final String NAME = "root";
public static final String PASS = "password";
public static final String DATABASE = "hi_test";

// 表信息
public static final String TABLE = "hi_user";
public static final String CLASSCOMMENT = "用户信息";
public static final String AGILE = new Date().getTime() + "";

// 包路径
public static final String ENTITY_URL = "org.miaixz.test.entity";
public static final String MAPPER_URL = "org.miaixz.test.mapper";
public static final String SERVICE_URL = "org.miaixz.test.service";
public static final String CONTROLLER_URL = "org.miaixz.test.spring";

// 功能开关
public static final String IS_SWAGGER = "true";
public static final String IS_DUBBO = "false";
public static final boolean IS_HUMP = false;
```

## ❓ 常见问题

### 问: 为什么我的加密 JAR 无法启动？

答: 检查以下几点:

1. 密码是否正确
2. 算法和密钥大小是否与加密时使用的匹配
3. 是否使用了正确的启动参数

### 问: 可以只加密特定的包吗？

答: 可以，使用过滤器有选择地加密资源:

```java
Boot.encrypt(source, target, password,
    new AntEntryFilter("com/company/project/**"));
```

### 问: 如何在后台模式下运行加密的 JAR？

答: 使用密钥文件进行后台操作:

```bash
nohup java -jar app.jar --xjar.keyfile=/path/to/key.key
```

### 问: 危险模式安全吗？

答: 不安全！危险模式将加密密钥存储在 MANIFEST.MF 中，使其可恢复。仅用于内部/测试环境。

### 问: 为什么加密后静态资源无法加载？

答: 静态文件在加密时会膨胀，导致 Content-Length 不匹配。使用过滤器将它们从加密中排除。

## 🔍 高级特性

### 数据库文档生成

```java
// 配置数据源
DruidDataSource dataSource = new DruidDataSource();
dataSource.setUrl("jdbc:mysql://127.0.0.1:3306/database");
dataSource.setUsername("root");
dataSource.setPassword("password");

// 配置输出
EngineConfig engineConfig = EngineConfig.builder()
    .fileOutputDir("/data/")
    .openOutputDir(true)
    .fileType(EngineFileType.HTML)
    .produceType(TemplateType.FREEMARKER)
    .fileName("Database_Documentation")
    .build();

// 配置表过滤器
ProcessConfig processConfig = ProcessConfig.builder()
    .designatedTableName(new ArrayList<>())
    .ignoreTableName(Arrays.asList("test_user", "test_group"))
    .ignoreTablePrefix(Arrays.asList("test_"))
    .ignoreTableSuffix(Arrays.asList("_test"))
    .build();

// 生成文档
Config config = Config.builder()
    .version("1.0.0")
    .description("数据库设计文档")
    .dataSource(dataSource)
    .engineConfig(engineConfig)
    .produceConfig(processConfig)
    .build();

Builder.createFile(config);
```

## 🔄 版本兼容性

- **JDK**: 8, 11, 17, 21+
- **Spring Boot**: 2.x, 3.x
- **Maven**: 3.6+
- **构建工具**: Maven, Gradle
