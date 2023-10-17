#### 项目说明

### 读取 properties

```java
    Properties properties = new Properties("test.properties");
    String user = properties.getProperty("user");
    Assert.assertEquals(user, "root");

    String driver = properties.getString("driver");
    Assert.assertEquals(driver, "com.mysql.jdbc.Driver");
    
    // 或者使用
    driver = Builder.get("test").get("driver");
    Assert.assertEquals("com.mysql.jdbc.Driver", driver);
```

### 读取 ini

```java
    // 得到输入流
    InputStream iniInput = this.class.getClassLoader().getResourceAsStream("test.ini");
    
    // 通过默认的bufferedIniReader类读取ini文件
    Readers ir = new BufferedIniReader();
    Ini ini = ir.read(iniInput);
    
    // 打印展示
    System.out.println(ini);
    
    // 转化为properties文件并展示
    ini.toProperties().forEach((k, v) -> {
        System.out.println(k + "=" + v);
    });
```

### 创建/输出 ini

```java
    Builder b=new Builder()
        .plusComment("this is a test ini")
        .skipLine(2)
        .plusSection("sec1","this is a section")
        .plusProperty("key1","value")
        .plusProperty("key2","value")
        .plusProperty("key3","value")
        .plusProperty("key4","value")
        .plusProperty("key5","value")
        .plusProperty("key6","value")
        .plusSection("sec2")
        .plusProperty("key1","value")
        .plusProperty("key2","value")
        .plusProperty("key3","value")
        .plusProperty("key4","value")
        .plusProperty("key5","value")
        .plusProperty("key6","value");

final Builder ini=b.build();

        System.out.println(ini);

        ini.write(Paths.get("F:\\test.ini"),true);
```

## 特性

* `IniElement`实现接口`java.io.Serializable`，可序列化。(1.1.0)
* `IniSetting`继承了`ArrayList<IniElement>`，可序列化，可作为list使用。
* `IniProperty`实现接口`Map.Entry<String, String>`。
* `IniSection`实现接口`List<IniProperty>`，可作为list使用。

## 自定义

可能我提供的默认解析类等无法满足你的需求，这时候你可以通过实现定义的接口来进行自定义。

我提供了一些（大概）便于实现的接口来支持使用者的自定义。如果你想，你也可以将你的额外实现开源出来。如果你开源了，可以告诉我，我会将地址展示在README中。

一些接口和抽象类的指路：

ini元素相关：父类接口/抽象类: `IniElement`、`BaseElement`, 具体元素的接口和默认实现：`IniComment`(
默认实现：`IniCommentImpl`)、`IniProperty`(
默认实现：`IniPropertyImpl`)、`IniSection`(默认实现：`IniSectionImpl`)。

ini读取相关：父类接口：`Readers` 或参考默认实现：`BufferedIniReader`。

Ini解析器相关：`Formatter`、`IniFormatterFactory`、`ElementFormatter`。

## 注意

- 默认情况下，在解析ini文件时，ini文件的内容分为以下几部分：

```ini
# 注释
[节1] # 标题(节)后的注释
# 注释, 下面是节1的键值对儿
key1=value
key2=value
key3=value
[节2] # 注释
key1=value
key2=value
key3=value
```

- 默认情况下，规则为：
    - 节(section)是由`[]`括起来的。结尾处可以有注释。
    - 注释以 `#` 开头，可以在一行的开头或节的结尾。
    - 属性是本节下的键/值对，并且不能在其后加上注释。