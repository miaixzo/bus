# ⚙️ Bus Setting: 配置管理框架

<p align="center">
<strong>属性和 INI 文件的统一配置管理</strong>
</p>

-----

## 📖 项目介绍

**Bus Setting** 提供了一个统一而强大的框架来管理应用程序配置。它支持 Java Properties 和 INI 文件格式，为读取、写入和操作配置文件提供了灵活的 API。

该模块提供:
* **Properties 增强**: 带有额外便捷方法的增强 Properties 类
* **INI 文件支持**: 完全支持 INI 格式，包括节、注释和属性
* **构建器模式**: 用于创建和修改 INI 文件的流式 API
* **类型转换**: 属性值的自动类型转换
* **多源支持**: 从文件、流和资源加载配置
* **序列化**: 所有配置元素都可序列化

-----

## ✨ 核心特性

### Properties 增强

* **增强的 Properties**: 带有便捷 getter 方法的扩展 Properties 类
* **类型安全访问**: 获取特定类型的值(String、Integer、Boolean 等)
* **默认值**: 属性缺失时支持默认值
* **全局访问**: 使用 `Builder.get(name)` 的单例样式访问

### INI 文件支持

* **完整的 INI 格式**: 支持节、属性、注释和空行
* **可序列化元素**: 所有 INI 元素实现 `java.io.Serializable`
* **基于列表的 API**: IniSection 和 IniSetting 扩展 List 以便于操作
* **Map.Entry 支持**: IniProperty 实现 `Map.Entry<String, String>`
* **灵活解析**: 可自定义的解析规则和格式化程序

### 高级特性

* **注释支持**: 保留和管理配置文件中的注释
* **多个节**: 将属性组织到逻辑节中
* **构建器 API**: 用于创建复杂 INI 结构的流式接口
* **自定义读取器/写入器**: 可扩展的解析器架构以支持自定义格式

-----

## 🚀 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-setting</artifactId>
    <version>8.x.x</version>
</dependency>
```

### 读取 Properties 文件

#### 方法 1: 使用增强的 Properties

```java
// 加载 properties 文件
Properties properties = new Properties("test.properties");

// 获取字符串值
String user = properties.getProperty("user");
System.out.println(user);  // 输出: root

// 带类型转换获取
String driver = properties.getString("driver");
System.out.println(driver);  // 输出: com.mysql.jdbc.Driver

// 带默认值获取
int timeout = properties.getInt("timeout", 30);
```

#### 方法 2: 使用全局构建器

```java
// 全局访问 properties
String driver = Builder.get("test").get("driver");
System.out.println(driver);  // 输出: com.mysql.jdbc.Driver
```

### 读取 INI 文件

```java
// 获取输入流
InputStream iniInput = getClass().getClassLoader().getResourceAsStream("test.ini");

// 使用默认读取器读取 INI 文件
Readers reader = new BufferedIniReader();
Ini ini = reader.read(iniInput);

// 打印 INI 内容
System.out.println(ini);

// 转换为 Properties
ini.toProperties().forEach((k, v) -> {
    System.out.println(k + "=" + v);
});
```

### 创建/写入 INI 文件

```java
// 使用流式 API 构建 INI 文件
Builder builder = new Builder()
    .plusComment("这是一个测试 ini")
    .skipLine(2)
    .plusSection("sec1", "这是一个节")
    .plusProperty("key1", "value1")
    .plusProperty("key2", "value2")
    .plusProperty("key3", "value3")
    .plusSection("sec2")
    .plusProperty("key1", "value1")
    .plusProperty("key2", "value2");

// 构建并写入
final Ini ini = builder.build();
System.out.println(ini);

// 写入文件
ini.write(Paths.get("/path/to/config.ini"), true);
```

-----

## 📝 使用示例

### 示例 1: 读取数据库配置

**config.properties:**

```properties
# 数据库配置
db.driver=com.mysql.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/mydb
db.username=root
db.password=secret
db.pool.size=10
db.pool.timeout=30000
```

**Java 代码:**

```java
Properties config = new Properties("config.properties");

// 读取配置
String driver = config.getString("db.driver");
String url = config.getString("db.url");
String username = config.getString("db.username");
String password = config.getString("db.password");
int poolSize = config.getInt("db.pool.size", 5);
long timeout = config.getLong("db.pool.timeout");

// 使用配置
DataSource dataSource = createDataSource(driver, url, username, password);
```

### 示例 2: 多环境配置

```java
// 加载特定环境的配置
String env = System.getProperty("env", "dev");
String configName = String.format("config-%s.properties", env);

Properties config = new Properties(configName);

// 访问属性
String apiUrl = config.getString("api.url");
boolean debug = config.getBoolean("debug.enabled", false);
```

### 示例 3: 多个节的 INI 文件

**config.ini:**

```ini
# 应用程序配置
[database]
driver=com.mysql.jdbc.Driver
url=jdbc:mysql://localhost:3306/mydb
username=root
password=secret

[server]
host=0.0.0.0
port=8080
threads=200

[logging]
level=INFO
file=application.log
```

**Java 代码:**

```java
InputStream input = getClass().getResourceAsStream("config.ini");
Readers reader = new BufferedIniReader();
Ini ini = reader.read(input);

// 获取节
IniSection database = ini.getSection("database");

// 从节获取属性
String driver = database.getProperty("driver");
String url = database.getProperty("url");

// 遍历属性
database.forEach(property -> {
    System.out.println(property.getKey() + "=" + property.getValue());
});

// 访问所有节
ini.getSections().forEach(section -> {
    System.out.println("[" + section.getName() + "]");
    section.forEach(prop -> {
        System.out.println(prop.getKey() + "=" + prop.getValue());
    });
});
```

### 示例 4: 动态配置构建器

```java
// 动态创建 INI
Builder builder = new Builder()
    .plusComment("生成的配置")
    .skipLine(1)
    .plusSection("database", "数据库连接")
    .plusProperty("url", "jdbc:mysql://localhost:3306/db")
    .plusProperty("username", "root")
    .plusProperty("password", "secret")
    .plusSection("cache", "缓存配置")
    .plusProperty("enabled", "true")
    .plusProperty("size", "1000");

// 以编程方式添加更多内容
IniSection app = new IniSection("application");
app.addProperty("name", "MyApp");
app.addProperty("version", "1.0.0");
builder.plusSection(app);

// 构建
Ini ini = builder.build();

// 保存到文件
ini.write(Paths.get("application.ini"), false);
```

### 示例 5: 带注释的配置

```java
// 带注释的 INI
Builder builder = new Builder()
    .plusComment("数据库配置")
    .plusComment("作者: 张三")
    .plusComment("日期: 2024-01-01")
    .skipLine(1)
    .plusSection("database")
    .plusProperty("host", "localhost")
    .plusProperty("port", "3306");

// 内联注释
IniSection section = new IniSection("app");
IniProperty prop = new IniProperty("name", "MyApp");
prop.setComment("应用程序名称");
section.addProperty(prop);

builder.plusSection(section);
```

### 示例 6: 修改现有 INI

```java
// 加载现有 INI
InputStream input = new FileInputStream("config.ini");
Readers reader = new BufferedIniReader();
Ini ini = reader.read(input);

// 修改值
IniSection db = ini.getSection("database");
db.setProperty("url", "jdbc:mysql://newhost:3306/db");

// 添加新节
IniSection newSection = new IniSection("new_feature");
newSection.addProperty("enabled", "true");
ini.addSection(newSection);

// 保存更改
ini.write(Paths.get("config.ini"), true);
```

### 示例 7: 自定义 INI 解析器

```java
// 实现自定义读取器
public class CustomIniReader implements Readers {
    @Override
    public Ini read(InputStream input) throws IOException {
        Ini ini = new Ini();

        // 自定义解析逻辑
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line;
        while ((line = reader.readLine()) != null) {
            // 解析行并添加到 ini
            if (line.startsWith("[")) {
                // 解析节
            } else if (line.contains("=")) {
                // 解析属性
            }
        }

        return ini;
    }
}

// 使用自定义读取器
Readers customReader = new CustomIniReader();
Ini ini = customReader.read(inputStream);
```

-----

## 🔧 INI 文件格式

### 默认格式规则

默认情况下，INI 文件使用以下规则解析:

```ini
# 注释(以 # 开头的行)
[section] # 节头后的注释
# 节头下的注释(属性属于此节)
key1=value
key2=value
key3=value

[section2] # 注释
key1=value
key2=value
key3=value
```

### 解析规则

1. **节**: 用方括号 `[]` 括起来，末尾可以有注释
2. **注释**: 以 `#` 开头，可以出现在行首或节头之后
3. **属性**: 节内的键值对，格式: `key=value`
4. **空行**: 保留以提高可读性

### 格式变体

```ini
; 也支持分号注释
[section]
key = value        # = 周围的空格将被修剪
key=value          # 无空格
key = value        # 仅 = 周围有空格
```

-----

## 💡 高级特性

### 自定义格式化程序

为特殊格式需求实现自定义格式化程序:

```java
public class CustomFormatter implements ElementFormatter {
    @Override
    public String format(IniElement element) {
        if (element instanceof IniProperty) {
            IniProperty prop = (IniProperty) element;
            // 自定义格式化
            return String.format("%s ::: %s", prop.getKey(), prop.getValue());
        }
        return element.toString();
    }
}

// 使用自定义格式化程序
Ini ini = new Ini();
ini.setFormatter(new CustomFormatter());
```

### 类型转换

```java
Properties props = new Properties("config.properties");

// 自动类型转换
String str = props.getString("key");
Integer num = props.getInt("key");
Long lng = props.getLong("key");
Boolean bool = props.getBoolean("key");
Double dbl = props.getDouble("key");

// 带默认值
int timeout = props.getInt("timeout", 30);
boolean enabled = props.getBoolean("enabled", true);
```

### 配置继承

```java
// 加载基础配置
Properties base = new Properties("base.properties");

// 加载环境特定覆盖
Properties env = new Properties("env.properties");

// 合并配置
Properties config = new Properties();
config.putAll(base);
config.putAll(env);

// 使用合并的配置
String value = config.getString("some.key");
```

### 监视配置更改

```java
// 监视文件更改
Path configPath = Paths.get("config.properties");

WatchService watcher = FileSystems.getDefault().newWatchService();
configPath.getParent().register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);

while (true) {
    WatchKey key = watcher.take();
    for (WatchEvent<?> event : key.pollEvents()) {
        if (event.context().toString().equals("config.properties")) {
            // 重新加载配置
            Properties config = new Properties("config.properties");
            // 更新应用程序
        }
    }
    key.reset();
}
```

-----

## 💡 最佳实践

### 1. 使用类型安全方法

```java
// ✅ 推荐: 使用类型安全 getter
int port = config.getInt("server.port", 8080);
boolean debug = config.getBoolean("debug.enabled", false);

// ❌ 不推荐: 手动解析
int port = Integer.parseInt(config.getProperty("server.port"));
```

### 2. 提供默认值

```java
// ✅ 推荐: 始终提供默认值
int timeout = config.getInt("timeout", 30);
String encoding = config.getString("encoding", "UTF-8");

// ❌ 不推荐: 无默认值(可能抛出 NPE)
int timeout = config.getInt("timeout");
```

### 3. 逻辑组织 INI 文件

```ini
# ✅ 推荐: 逻辑分组
[database.primary]
host=localhost
port=3306

[database.replica]
host=replica.local
port=3306

# ❌ 不推荐: 扁平结构
db.primary.host=localhost
db.primary.port=3306
db.replica.host=replica.local
db.replica.port=3306
```

### 4. 使用注释进行文档记录

```ini
# 数据库连接超时(秒)
# 默认: 30，最小: 5，最大: 300
db.timeout=30

# 启用查询日志 (true/false)
# 警告: 生产环境中的性能影响
db.log_queries=false
```

### 5. 验证配置

```java
public class ConfigValidator {
    public static void validate(Properties config) {
        // 必需属性
        require(config, "db.url");
        require(config, "db.username");

        // 验证范围
        int port = config.getInt("server.port", 8080);
        if (port < 1024 || port > 65535) {
            throw new IllegalArgumentException("无效端口: " + port);
        }

        // 验证布尔值
        boolean ssl = config.getBoolean("server.ssl", false);
        if (ssl && !config.contains("server.ssl.cert")) {
            throw new IllegalArgumentException("启用 SSL 时需要 SSL 证书");
        }
    }

    private static void require(Properties config, String key) {
        if (!config.contains(key)) {
            throw new IllegalArgumentException("缺少必需配置: " + key);
        }
    }
}
```

-----

## ❓ 常见问题

### Q1: Properties 和 Ini 有什么区别？

**Properties**:
* 简单的键值对
* 无层次结构或节
* 标准 Java 格式

**INI**:
* 支持节和层次结构
* 支持注释
* 更人性化

### Q2: 如何处理多个环境的配置？

```java
// 方法 1: 多个文件
String env = System.getProperty("env", "dev");
Properties config = new Properties("config-" + env + ".properties");

// 方法 2: 基于配置文件
Properties base = new Properties("config.properties");
Properties profile = new Properties("config-" + env + ".properties");
base.putAll(profile);
```

### Q3: 可以加密配置文件中的敏感数据吗？

可以，使用加密值:

```properties
# 加密密码(使用 Bus Crypto 进行加密)
db.password=ENC(AQIDBAUGBwgJCgsMDQ4PEBESExQVFhcYGRobHB0eHyA=)
```

然后在运行时解密:

```java
String encrypted = config.getString("db.password");
String password = Crypto.decrypt(encrypted);
```

### Q4: 如何处理属性值中的特殊字符？

```properties
# 对特殊字符使用 Unicode 转义
special.chars=Hello\u0020World\nNewLine

# 或使用 properties 文件本机转义
path=C\:\\Program Files\\App
message=Line1\nLine2\tTabbed
```

### Q5: Bus Setting 使用什么编码？

默认情况下，使用 UTF-8 编码。对于传统 ISO-8859-1 文件:

```java
Properties props = new Properties();
props.load(new InputStreamReader(
    new FileInputStream("config.properties"),
    Charset.ISO_8859_1
));
```

-----

## 🔄 版本兼容性

| Bus Setting 版本 | JDK 版本 | 状态 |
| :--- | :--- | :--- |
| **8.x** | 17+ | 当前 |
| 7.x | 11+ | 维护中 |

-----

## 🔗 相关模块

* **[bus-core](../bus-core)**: 核心工具和类型转换
* **[bus-crypto](../bus-crypto)**: 配置加密支持
* **[bus-logger](../bus-logger)**: 基于配置的日志记录

-----

## 📚 其他资源

* [GitHub 仓库](https://github.com/818000/bus)
* [Java Properties 文档](https://docs.oracle.com/javase/8/docs/api/java/util/Properties.html)
