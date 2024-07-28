<p align="center">
  <a href="https://www.miaixz.org"><img src="../LOGO.svg" width="45%"></a>
</p>
<p align="center">
  👉 <a href="https://www.miaixz.org">https://www.miaixz.org</a> 👈
</p>

## 📚bus-parent 模块介绍

`bus-parent`模块只由一个`pom.xml`组成，同时提供了`dependencyManagement`声明。

-------------------------------------------------------------------------------

## 🍒使用

如果同 Spring Boot 方式引入 bus ，再由子模块决定用到哪些bus模块，你可以在父模块中加入：

```xml
<parent>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-parent</artifactId>
    <version>xx.xx.xx</version>
</parent>
```

在子模块中就可以引入自己需要的模块了：

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-all</artifactId>
</dependency>
```

或者，单独引入某个模块

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-core</artifactId>
</dependency>
```
