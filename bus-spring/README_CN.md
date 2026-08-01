# bus-spring

Bus 应用共享的 Spring 与 Spring Boot 集成基础设施。

本模块包含 `bus-starter` 使用的上下文工具、启动生命周期、环境处理器、Banner、启动指标以及 Servlet MVC
适配器。只需要这些通用集成能力的应用可以直接依赖 `bus-spring`；使用 Bus 自动配置集合的应用可继续依赖
`bus-starter`。

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-spring</artifactId>
    <version>${revision}</version>
</dependency>
```
