# Bus All

`bus-all` is the aggregate distribution module for the Bus framework. It provides one Maven coordinate for the curated Bus runtime modules, so applications can depend on a single artifact when they need the complete framework surface.

This module is a convenience artifact. For a minimal production classpath, prefer importing `bus-bom` and adding only the modules that the application actually uses.

-----

## Purpose

`bus-all` is designed for:

- Quick evaluation of the Bus framework.
- Integration tests that need several Bus modules at once.
- Internal tools where a broad framework classpath is acceptable.
- Applications that deliberately standardize on the full Bus runtime.

`bus-all` is not:

- A BOM. Use `bus-bom` for dependency version management.
- A parent POM. Use `bus-parent` for shared build configuration.
- A Spring Boot starter only. Use `bus-starter` when the application only needs Spring Boot auto-configuration.

-----

## Maven Usage

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-all</artifactId>
    <version>x.x.x</version>
</dependency>
```

When the project already imports `bus-bom`, the version can be omitted:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.miaixz</groupId>
            <artifactId>bus-bom</artifactId>
            <version>x.x.x</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>org.miaixz</groupId>
        <artifactId>bus-all</artifactId>
    </dependency>
</dependencies>
```

-----

## Gradle Usage

```gradle
implementation "org.miaixz:bus-all:x.x.x"
```

With the BOM:

```gradle
dependencyManagement {
    imports {
        mavenBom "org.miaixz:bus-bom:x.x.x"
    }
}

dependencies {
    implementation "org.miaixz:bus-all"
}
```

-----

## Included Modules

`bus-all` aggregates the following runtime modules:

- `bus-auth`: authentication and authorization helpers
- `bus-base`: shared base abstractions
- `bus-cache`: cache abstraction and cache integrations
- `bus-core`: core utilities
- `bus-cortex`: service registry, discovery and health probing
- `bus-crypto`: cryptography, certificate and security utilities
- `bus-extra`: extended utilities and integrations
- `bus-fabric`: HTTP, socket, WebSocket, SSE and STOMP protocol runtime
- `bus-gitlab`: GitLab API integration
- `bus-health`: health checks and diagnostics
- `bus-image`: image processing utilities
- `bus-limiter`: rate limiting support
- `bus-logger`: logging facade and logging integrations
- `bus-mapper`: mapper and persistence helpers
- `bus-metrics`: metrics support
- `bus-notify`: notification integrations
- `bus-office`: office document utilities
- `bus-pay`: payment integrations
- `bus-proxy`: proxy support
- `bus-sensitive`: sensitive data masking
- `bus-setting`: configuration utilities
- `bus-shade`: shaded dependency support
- `bus-starter`: Spring Boot auto-configuration
- `bus-storage`: object storage integrations
- `bus-tempus`: time and scheduling utilities
- `bus-tracer`: tracing support
- `bus-validate`: validation utilities
- `bus-vortex`: gateway and routing runtime

Build-only modules such as `bus-parent` and `bus-bom` are not runtime modules and are not used as application dependencies.

-----

## Packaging Notes

During package creation, `bus-all` shades the Bus runtime modules into one aggregate artifact and consolidates native-image metadata for GraalVM usage. The published POM is normalized so consumers use `bus-all` as a single entry artifact instead of receiving every internal module as a regular transitive dependency.

This makes `bus-all` convenient, but it also means the resulting classpath is intentionally broad. Choose direct module dependencies when startup time, dependency surface, native-image size or deployment footprint is important.

-----

## Choosing the Right Entry Point

| Requirement | Recommended entry point |
| --- | --- |
| Full Bus runtime with one dependency | `bus-all` |
| Version alignment for selected modules | `bus-bom` |
| Shared build configuration | `bus-parent` |
| Spring Boot auto-configuration | `bus-starter` |
| Minimal runtime dependency set | Direct module dependencies |

-----

## License

Bus is released under the Apache License 2.0.
