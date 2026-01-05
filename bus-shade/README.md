# 🚀 bus-shade: Java JAR Encryption and Code Generation Toolkit

## 📖 Project Introduction

bus-shade is a powerful Java toolkit that provides comprehensive JAR encryption and code generation capabilities. It offers secure encryption for Spring Boot JAR files to protect your source code and bytecode from decompilation, along with automated code generation utilities to accelerate development.

## ✨ Core Features

### JAR Encryption
- **Zero Code Intrusion**: Encrypt compiled JAR packages without modifying source code
- **In-Memory Decryption**: Complete in-memory decryption prevents source code and bytecode leakage
- **Algorithm Support**: Supports all JDK built-in encryption algorithms (AES, DES, etc.)
- **Selective Encryption**: Choose specific bytecode or resource files to encrypt, avoiding computational waste
- **Transparent Runtime**: No need to modify Tomcat, Spring, or other source code
- **Maven Plugin**: Automatic encryption during packaging with Maven plugin integration
- **Dependency Encryption**: Support for encrypting dependencies in WEB-INF/lib or BOOT-INF/lib

### Code Generation
- **Lombok Integration**: Seamless integration with Lombok annotations
- **Swagger Support**: Automatic API documentation generation
- **MyBatis Integration**: Auto-generate Entity, Mapper, Service, and Controller classes
- **Validation**: Built-in format validation annotations
- **RESTful APIs**: Auto-generated CRUD operations with Swagger integration

### Database Documentation
- **Auto-Generation**: Generate comprehensive database design documentation
- **Multiple Formats**: Support for HTML and other document formats
- **Flexible Configuration**: Filter tables by name, prefix, or suffix

## 🚀 Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-shade</artifactId>
    <latestVersion>8.x.x</latestVersion>
</dependency>
```

## 📝 Usage Examples

### Example 1: Encrypt Spring Boot JAR (Standard Mode)

```java
String password = "forest";
Key key = Builder.key(password);
Boot.encrypt("/path/to/read/forest.jar", "/path/to/save/enforest.jar", key);
```

### Example 2: Encrypt Spring Boot JAR (Danger Mode - No Password Required)

**⚠️ Warning**: In this mode, the key is stored in META-INF/MANIFEST.MF. Use with caution!

```java
String password = "forest";
Key key = Builder.key(password);
Boot.encrypt("/path/to/read/forest.jar", "/path/to/save/enforest.jar", key, Builder.MODE_DANGER);
```

### Example 3: Decrypt Spring Boot JAR

```java
String password = "forest";
Key key = Builder.key(password);
Boot.decrypt("/path/to/read/enforest.jar", "/path/to/save/deforest.jar", key);
```

### Example 4: Encrypt Standard JAR

```java
String password = "forest";
Key key = Builder.key(password);
Jar.encrypt("/path/to/read/forest.jar", "/path/to/save/enforest.jar", key);
```

### Example 5: Run Encrypted JAR with Password Prompt

```bash
java -jar /path/to/enforest.jar
# Enter password when prompted
```

### Example 6: Run Encrypted JAR with Command Line Password

```bash
java -jar /path/to/enforest.jar --xjar.password=forest
```

### Example 7: Run Encrypted JAR with Key File (Background Mode)

```bash
nohup java -jar /path/to/enforest.jar --xjar.keyfile=/path/to/forest.key
```

### Example 8: Encrypt with Filter (Selective Encryption)

```java
// Encrypt only classes under com.company.project package
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

## 🔧 Configuration

### Startup Parameters

| Parameter | Description | Default | Example |
|:---|:---|:---|:---|
| --xjar.password | Password | Required | forest |
| --xjar.algorithm | Key Algorithm | AES | AES, DES |
| --xjar.keysize | Key Size | 128 | 128, 256 |
| --xjar.ivsize | Vector Size | 128 | 128 |
| --xjar.keyfile | Key File Path | ./forest.key | /path/to/key.key |

### Key File Format

Key files use properties format:

```properties
password: PASSWORD
algorithm: AES
keysize: 128
ivsize: 128
hold: false
```

**Parameter Description**:

| Parameter | Description | Default | Notes |
|:---|:---|:---|:---|
| password | Password String | Required | Any string |
| algorithm | Key Algorithm | AES | All JDK built-in algorithms supported |
| keysize | Key Size | 128 | Depends on algorithm |
| ivsize | Vector Size | 128 | Depends on algorithm |
| hold | Retain Key File | false | Auto-delete after reading if not true/1/yes/y |

### Maven Plugin Configuration

```xml
<plugin>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-shade-maven-plugin</artifactId>
    <version>8.5.1</version>
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

### Maven Command Line

```bash
# Direct encryption
mvn xjar:build -Dxjar.password=forest

# With custom output directory
mvn xjar:build -Dxjar.password=forest -Dxjar.targetDir=/directory/to/save

# Integrated with package phase
mvn clean package -Dxjar.password=forest
```

## 💡 Best Practices

### 1. Selective Encryption with Filters

**Ant Expression Filter**:

```java
Boot.encrypt(plaintext, encrypted, password, new AntEntryFilter("com/company/project/**"));
Boot.encrypt(plaintext, encrypted, password, new AntEntryFilter("mapper/*Mapper.xml"));
```

**Regular Expression Filter**:

```java
Boot.encrypt(plaintext, encrypted, password, new RegexEntryFilter("com/company/project/(.+)"));
```

### 2. Combined Filters

```java
// AND operation - all filters must match
XEntryFilter and = Builder.and()
    .mix(new AntEntryFilter("com/company/project/**"))
    .mix(new AntEntryFilter("*/**.class"));

// OR operation - any filter can match
XEntryFilter or = Builder.or()
    .mix(new AntEntryFilter("com/company/project/**"))
    .mix(new AntEntryFilter("mapper/*Mapper.xml"));

// NOT operation - exclude matching resources
XEntryFilter not = Builder.not(
    Builder.or()
        .mix(new AntEntryFilter("static/**"))
        .mix(new AntEntryFilter("META-INF/resources/**"))
);
```

### 3. Static Resource Handling

⚠️ **Important**: Static files should be excluded from encryption as they expand in size when encrypted, causing browser Content-Length mismatches.

```java
// Exclude static resources
XEntryFilter filter = Builder.not(
    Builder.or()
        .mix(new AntEntryFilter("static/**"))
        .mix(new AntEntryFilter("META-INF/resources/**"))
);
```

### 4. Code Generation Configuration

```java
// Basic project information
public static final String PROJECT = "bus-shade";
public static final String AUTHOR = "Kimi Liu";
public static final String VERSION = "1.0.0";

// Database connection
public static final String URL = "jdbc:mysql://localhost:3306/database";
public static final String NAME = "root";
public static final String PASS = "password";
public static final String DATABASE = "hi_test";

// Table information
public static final String TABLE = "hi_user";
public static final String CLASSCOMMENT = "User Information";
public static final String AGILE = new Date().getTime() + "";

// Package paths
public static final String ENTITY_URL = "org.miaixz.test.entity";
public static final String MAPPER_URL = "org.miaixz.test.mapper";
public static final String SERVICE_URL = "org.miaixz.test.service";
public static final String CONTROLLER_URL = "org.miaixz.test.spring";

// Feature flags
public static final String IS_SWAGGER = "true";
public static final String IS_DUBBO = "false";
public static final boolean IS_HUMP = false;
```

## ❓ FAQ

### Q: Why does my encrypted JAR fail to start?

A: Check that:
1. The password is correct
2. The algorithm and key sizes match those used during encryption
3. You're using the correct startup parameters

### Q: Can I encrypt only specific packages?

A: Yes, use filters to selectively encrypt resources:

```java
Boot.encrypt(source, target, password,
    new AntEntryFilter("com/company/project/**"));
```

### Q: How do I run encrypted JARs in background mode?

A: Use a key file for background operations:

```bash
nohup java -jar app.jar --xjar.keyfile=/path/to/key.key
```

### Q: Is Danger Mode safe?

A: No! Danger Mode stores the encryption key in MANIFEST.MF, making it recoverable. Only use it for internal/testing environments.

### Q: Why are static resources not loading after encryption?

A: Static files expand when encrypted, causing Content-Length mismatches. Exclude them from encryption using filters.

## 🔍 Advanced Features

### Database Documentation Generation

```java
// Configure data source
DruidDataSource dataSource = new DruidDataSource();
dataSource.setUrl("jdbc:mysql://127.0.0.1:3306/database");
dataSource.setUsername("root");
dataSource.setPassword("password");

// Configure output
EngineConfig engineConfig = EngineConfig.builder()
    .fileOutputDir("/data/")
    .openOutputDir(true)
    .fileType(EngineFileType.HTML)
    .produceType(TemplateType.FREEMARKER)
    .fileName("Database_Documentation")
    .build();

// Configure table filters
ProcessConfig processConfig = ProcessConfig.builder()
    .designatedTableName(new ArrayList<>())
    .ignoreTableName(Arrays.asList("test_user", "test_group"))
    .ignoreTablePrefix(Arrays.asList("test_"))
    .ignoreTableSuffix(Arrays.asList("_test"))
    .build();

// Generate documentation
Config config = Config.builder()
    .version("1.0.0")
    .description("Database Design Documentation")
    .dataSource(dataSource)
    .engineConfig(engineConfig)
    .produceConfig(processConfig)
    .build();

Builder.createFile(config);
```

## 🔄 Version Compatibility

- **JDK**: 8, 11, 17, 21+
- **Spring Boot**: 2.x, 3.x
- **Maven**: 3.6+
- **Build Tools**: Maven, Gradle
