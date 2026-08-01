# bus-spring

Shared Spring and Spring Boot integration infrastructure for Bus applications.

This module contains the reusable context helpers, lifecycle hooks, environment processors, banner support, metrics,
and Servlet MVC adapters used by `bus-starter`. Applications that only need these integration primitives can depend on
`bus-spring` directly; applications using the Bus auto-configuration bundle can continue to depend on `bus-starter`.

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-spring</artifactId>
    <version>${revision}</version>
</dependency>
```
