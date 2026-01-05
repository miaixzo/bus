# ⚙️ Bus Setting: Configuration Management Framework

<p align="center">
<strong>Unified Configuration Management for Properties and INI Files</strong>
</p>

-----

## 📖 Project Introduction

**Bus Setting** provides a unified and powerful framework for managing application configurations. It supports both Java Properties and INI file formats, offering flexible APIs for reading, writing, and manipulating configuration files.

The module provides:
* **Properties Enhancement**: Enhanced Properties class with additional convenience methods
* **INI File Support**: Full support for INI format with sections, comments, and properties
* **Builder Pattern**: Fluent API for creating and modifying INI files
* **Type Conversion**: Automatic type conversion for property values
* **Multiple Sources**: Load configurations from files, streams, and resources
* **Serialization**: All configuration elements are serializable

-----

## ✨ Core Features

### Properties Enhancement

* **Enhanced Properties**: Extended Properties class with convenient getter methods
* **Type-Safe Access**: Get values as specific types (String, Integer, Boolean, etc.)
* **Default Values**: Support for default values when properties are missing
* **Global Access**: Singleton-style access with `Builder.get(name)`

### INI File Support

* **Complete INI Format**: Supports sections, properties, comments, and blank lines
* **Serializable Elements**: All INI elements implement `java.io.Serializable`
* **List-Based API**: IniSection and IniSetting extend List for easy manipulation
* **Map.Entry Support**: IniProperty implements `Map.Entry<String, String>`
* **Flexible Parsing**: Customizable parsing rules and formatters

### Advanced Features

* **Comments Support**: Preserve and manage comments in configuration files
* **Multiple Sections**: Organize properties into logical sections
* **Builder API**: Fluent interface for creating complex INI structures
* **Custom Readers/Writer**: Extensible parser architecture for custom formats

-----

## 🚀 Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-setting</artifactId>
    <version>8.5.1</version>
</dependency>
```

### Read Properties Files

#### Method 1: Using Enhanced Properties

```java
// Load properties file
Properties properties = new Properties("test.properties");

// Get string value
String user = properties.getProperty("user");
System.out.println(user);  // Output: root

// Get with type conversion
String driver = properties.getString("driver");
System.out.println(driver);  // Output: com.mysql.jdbc.Driver

// Get with default value
int timeout = properties.getInt("timeout", 30);
```

#### Method 2: Using Global Builder

```java
// Access properties globally
String driver = Builder.get("test").get("driver");
System.out.println(driver);  // Output: com.mysql.jdbc.Driver
```

### Read INI Files

```java
// Get input stream
InputStream iniInput = getClass().getClassLoader().getResourceAsStream("test.ini");

// Read INI file using default reader
Readers reader = new BufferedIniReader();
Ini ini = reader.read(iniInput);

// Print INI content
System.out.println(ini);

// Convert to Properties
ini.toProperties().forEach((k, v) -> {
    System.out.println(k + "=" + v);
});
```

### Create/Write INI Files

```java
// Build INI file using fluent API
Builder builder = new Builder()
    .plusComment("this is a test ini")
    .skipLine(2)
    .plusSection("sec1", "this is a section")
    .plusProperty("key1", "value1")
    .plusProperty("key2", "value2")
    .plusProperty("key3", "value3")
    .plusSection("sec2")
    .plusProperty("key1", "value1")
    .plusProperty("key2", "value2");

// Build and write
final Ini ini = builder.build();
System.out.println(ini);

// Write to file
ini.write(Paths.get("/path/to/config.ini"), true);
```

-----

## 📝 Usage Examples

### Example 1: Read Database Configuration

**config.properties:**

```properties
# Database Configuration
db.driver=com.mysql.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/mydb
db.username=root
db.password=secret
db.pool.size=10
db.pool.timeout=30000
```

**Java Code:**

```java
Properties config = new Properties("config.properties");

// Read configuration
String driver = config.getString("db.driver");
String url = config.getString("db.url");
String username = config.getString("db.username");
String password = config.getString("db.password");
int poolSize = config.getInt("db.pool.size", 5);
long timeout = config.getLong("db.pool.timeout");

// Use configuration
DataSource dataSource = createDataSource(driver, url, username, password);
```

### Example 2: Multi-Environment Configuration

```java
// Load environment-specific configuration
String env = System.getProperty("env", "dev");
String configName = String.format("config-%s.properties", env);

Properties config = new Properties(configName);

// Access properties
String apiUrl = config.getString("api.url");
boolean debug = config.getBoolean("debug.enabled", false);
```

### Example 3: INI File with Multiple Sections

**config.ini:**

```ini
# Application Configuration
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

**Java Code:**

```java
InputStream input = getClass().getResourceAsStream("config.ini");
Readers reader = new BufferedIniReader();
Ini ini = reader.read(input);

// Get section
IniSection database = ini.getSection("database");

// Get property from section
String driver = database.getProperty("driver");
String url = database.getProperty("url");

// Iterate properties
database.forEach(property -> {
    System.out.println(property.getKey() + "=" + property.getValue());
});

// Access all sections
ini.getSections().forEach(section -> {
    System.out.println("[" + section.getName() + "]");
    section.forEach(prop -> {
        System.out.println(prop.getKey() + "=" + prop.getValue());
    });
});
```

### Example 4: Dynamic Configuration Builder

```java
// Create INI dynamically
Builder builder = new Builder()
    .plusComment("Generated Configuration")
    .skipLine(1)
    .plusSection("database", "Database Connection")
    .plusProperty("url", "jdbc:mysql://localhost:3306/db")
    .plusProperty("username", "root")
    .plusProperty("password", "secret")
    .plusSection("cache", "Cache Configuration")
    .plusProperty("enabled", "true")
    .plusProperty("size", "1000");

// Add more programmatically
IniSection app = new IniSection("application");
app.addProperty("name", "MyApp");
app.addProperty("version", "1.0.0");
builder.plusSection(app);

// Build
Ini ini = builder.build();

// Save to file
ini.write(Paths.get("application.ini"), false);
```

### Example 5: Configuration with Comments

```java
// INI with comments
Builder builder = new Builder()
    .plusComment("Database Configuration")
    .plusComment("Author: John Doe")
    .plusComment("Date: 2024-01-01")
    .skipLine(1)
    .plusSection("database")
    .plusProperty("host", "localhost")
    .plusProperty("port", "3306");

// Inline comment
IniSection section = new IniSection("app");
IniProperty prop = new IniProperty("name", "MyApp");
prop.setComment("Application name");
section.addProperty(prop);

builder.plusSection(section);
```

### Example 6: Modify Existing INI

```java
// Load existing INI
InputStream input = new FileInputStream("config.ini");
Readers reader = new BufferedIniReader();
Ini ini = reader.read(input);

// Modify values
IniSection db = ini.getSection("database");
db.setProperty("url", "jdbc:mysql://newhost:3306/db");

// Add new section
IniSection newSection = new IniSection("new_feature");
newSection.addProperty("enabled", "true");
ini.addSection(newSection);

// Save changes
ini.write(Paths.get("config.ini"), true);
```

### Example 7: Custom INI Parser

```java
// Implement custom reader
public class CustomIniReader implements Readers {
    @Override
    public Ini read(InputStream input) throws IOException {
        Ini ini = new Ini();

        // Custom parsing logic
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line;
        while ((line = reader.readLine()) != null) {
            // Parse line and add to ini
            if (line.startsWith("[")) {
                // Parse section
            } else if (line.contains("=")) {
                // Parse property
            }
        }

        return ini;
    }
}

// Use custom reader
Readers customReader = new CustomIniReader();
Ini ini = customReader.read(inputStream);
```

-----

## 🔧 INI File Format

### Default Format Rules

By default, INI files are parsed with the following rules:

```ini
# Comment (lines starting with #)
[section] # Comment after section header
# Comment below section header (properties belong to this section)
key1=value
key2=value
key3=value

[section2] # Comment
key1=value
key2=value
key3=value
```

### Parsing Rules

1. **Sections**: Enclosed in square brackets `[]`, can have comments at the end
2. **Comments**: Start with `#`, can appear at line start or after section header
3. **Properties**: Key-value pairs within a section, format: `key=value`
4. **Blank Lines**: Preserved for readability

### Format Variations

```ini
; Semicolon comments are also supported
[section]
key = value        # Spaces around = are trimmed
key=value          # No spaces
key = value        # Spaces only around =
```

-----

## 💡 Advanced Features

### Custom Formatters

Implement custom formatters for special formatting needs:

```java
public class CustomFormatter implements ElementFormatter {
    @Override
    public String format(IniElement element) {
        if (element instanceof IniProperty) {
            IniProperty prop = (IniProperty) element;
            // Custom formatting
            return String.format("%s ::: %s", prop.getKey(), prop.getValue());
        }
        return element.toString();
    }
}

// Use custom formatter
Ini ini = new Ini();
ini.setFormatter(new CustomFormatter());
```

### Type Conversion

```java
Properties props = new Properties("config.properties");

// Automatic type conversion
String str = props.getString("key");
Integer num = props.getInt("key");
Long lng = props.getLong("key");
Boolean bool = props.getBoolean("key");
Double dbl = props.getDouble("key");

// With default values
int timeout = props.getInt("timeout", 30);
boolean enabled = props.getBoolean("enabled", true);
```

### Configuration Inheritance

```java
// Load base configuration
Properties base = new Properties("base.properties");

// Load environment-specific overrides
Properties env = new Properties("env.properties");

// Merge configurations
Properties config = new Properties();
config.putAll(base);
config.putAll(env);

// Use merged configuration
String value = config.getString("some.key");
```

### Watch Configuration Changes

```java
// Watch for file changes
Path configPath = Paths.get("config.properties");

WatchService watcher = FileSystems.getDefault().newWatchService();
configPath.getParent().register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);

while (true) {
    WatchKey key = watcher.take();
    for (WatchEvent<?> event : key.pollEvents()) {
        if (event.context().toString().equals("config.properties")) {
            // Reload configuration
            Properties config = new Properties("config.properties");
            // Update application
        }
    }
    key.reset();
}
```

-----

## 💡 Best Practices

### 1. Use Type-Safe Methods

```java
// ✅ Recommended: Use type-safe getters
int port = config.getInt("server.port", 8080);
boolean debug = config.getBoolean("debug.enabled", false);

// ❌ Not Recommended: Manual parsing
int port = Integer.parseInt(config.getProperty("server.port"));
```

### 2. Provide Default Values

```java
// ✅ Recommended: Always provide defaults
int timeout = config.getInt("timeout", 30);
String encoding = config.getString("encoding", "UTF-8");

// ❌ Not Recommended: No defaults (can throw NPE)
int timeout = config.getInt("timeout");
```

### 3. Organize INI Files Logically

```ini
# ✅ Recommended: Logical grouping
[database.primary]
host=localhost
port=3306

[database.replica]
host=replica.local
port=3306

# ❌ Not Recommended: Flat structure
db.primary.host=localhost
db.primary.port=3306
db.replica.host=replica.local
db.replica.port=3306
```

### 4. Use Comments for Documentation

```ini
# Database connection timeout in seconds
# Default: 30, Min: 5, Max: 300
db.timeout=30

# Enable query logging (true/false)
# WARNING: Performance impact in production
db.log_queries=false
```

### 5. Validate Configuration

```java
public class ConfigValidator {
    public static void validate(Properties config) {
        // Required properties
        require(config, "db.url");
        require(config, "db.username");

        // Validate ranges
        int port = config.getInt("server.port", 8080);
        if (port < 1024 || port > 65535) {
            throw new IllegalArgumentException("Invalid port: " + port);
        }

        // Validate boolean
        boolean ssl = config.getBoolean("server.ssl", false);
        if (ssl && !config.contains("server.ssl.cert")) {
            throw new IllegalArgumentException("SSL cert required when SSL enabled");
        }
    }

    private static void require(Properties config, String key) {
        if (!config.contains(key)) {
            throw new IllegalArgumentException("Missing required config: " + key);
        }
    }
}
```

-----

## ❓ Frequently Asked Questions

### Q1: What's the difference between Properties and Ini?

**Properties**:
* Simple key-value pairs
* No hierarchy or sections
* Standard Java format

**INI**:
* Supports sections and hierarchy
* Supports comments
* More human-readable

### Q2: How do I handle configuration for multiple environments?

```java
// Method 1: Multiple files
String env = System.getProperty("env", "dev");
Properties config = new Properties("config-" + env + ".properties");

// Method 2: Profile-based
Properties base = new Properties("config.properties");
Properties profile = new Properties("config-" + env + ".properties");
base.putAll(profile);
```

### Q3: Can I encrypt sensitive data in configuration files?

Yes, use encrypted values:

```properties
# Encrypted password (use Bus Crypto for encryption)
db.password=ENC(AQIDBAUGBwgJCgsMDQ4PEBESExQVFhcYGRobHB0eHyA=)
```

Then decrypt at runtime:

```java
String encrypted = config.getString("db.password");
String password = Crypto.decrypt(encrypted);
```

### Q4: How do I handle special characters in property values?

```properties
# Use Unicode escape for special characters
special.chars=Hello\u0020World\nNewLine

# Or use properties file native escaping
path=C\:\\Program Files\\App
message=Line1\nLine2\tTabbed
```

### Q5: What encoding does Bus Setting use?

By default, UTF-8 encoding is used. For legacy ISO-8859-1 files:

```java
Properties props = new Properties();
props.load(new InputStreamReader(
    new FileInputStream("config.properties"),
    StandardCharsets.ISO_8859_1
));
```

-----

## 🔄 Version Compatibility

| Bus Setting Version | JDK Version | Status |
| :--- | :--- | :--- |
| **8.x** | 17+ | Current |
| 7.x | 11+ | Maintenance |

-----

## 🔗 Related Modules

* **[bus-core](../bus-core)**: Core utilities and type conversion
* **[bus-crypto](../bus-crypto)**: Configuration encryption support
* **[bus-logger](../bus-logger)**: Configuration-based logging

-----

## 📚 Additional Resources

* [GitHub Repository](https://github.com/818000/bus)
* [Java Properties Documentation](https://docs.oracle.com/javase/8/docs/api/java/util/Properties.html)
