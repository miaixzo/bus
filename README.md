<p align="center">
  <a href="https://www.miaixz.org"><img src="LOGO.svg" width="40%"></a>
</p>
<p align="center">
  <a target="_blank" href="https://search.maven.org/search?q=org.miaixz">
    <img src="https://img.shields.io/badge/maven--central-v8.5.x-blue.svg?label=Maven%20Central" />
  </a>
  <a target="_blank" href="https://jdk.java.net/">
    <img src="https://img.shields.io/badge/Java-21+-green.svg">
  </a>
  <a target="_blank" href="https://spring.io/projects/spring-boot">
    <img src="https://img.shields.io/badge/Spring Boot-3.5.5-brightgreen.svg">
  </a>
  <a target="_blank" href="https://www.postgresql.org">
    <img src="https://img.shields.io/badge/postgresql-17.x-blue.svg">
  </a>
  <a target="_blank" href="http://dubbo.apache.org">
    <img src="https://img.shields.io/badge/dubbo-3.3.x-yellow.svg">
  </a>
  <a target="_blank" href="http://poi.apache.org">
    <img src="https://img.shields.io/badge/poi-5.4.x-blue.svg">
  </a>
  <a target="_blank" href="https://github.com/818000/bus/blob/main/LICENSE">
    <img src="https://img.shields.io/badge/license-MIT-green.svg">
  </a>
</p>

<p align="center">
    <a target="_blank" href="README.md">EN</a> ｜  <a target="_blank" href="README_CN.md">CN</a>
</p>


---

### ✨ Project Description

Bus (Enterprise Application/Service Bus) is a foundational framework and service suite; a lightweight enterprise-level application/service framework built on Java 17+, providing standardized service suites and distributed middleware solutions.

Everyone is welcome to visit and explore. Life is short! Write less repetitive code! Please give us a star as a reward~

The goal is to create a comprehensive full-stack technical solution that rapidly implements business requirements, covering foundational frameworks - distributed microservices architecture - continuous integration - automated deployment - system monitoring, and more.


### ✨ Version Selection

Bus primarily has versions 3.x, 5.x, 6.x, and 8.x, as detailed below:

| Java Version | Maven Repository                                                           | Main Features                                                           |
|--------------|----------------------------------------------------------------------------|-------------------------------------------------------------------------|
| Java 8       | [bus-all:5.x](https://central.sonatype.com/artifact/org.aoju/bus-all/5.9.9) | Compiled with java 8                                                    |
| Java 17      | [bus-all:6.x](https://central.sonatype.com/artifact/org.aoju/bus-all/6.6.1) | Compiled with java 17, uses Jakarta EE, compatible with java 11, 17     |
| Java 21      | [bus-all:8.x](https://central.sonatype.com/artifact/org.miaixz/bus-all)    | Compiled with java 21, uses Jakarta EE, compatible with java 11, 17, 21 |


### ✨ Component Information

| Status | Module                        | Description                                                                 |
|--------|-------------------------------|-----------------------------------------------------------------------------|
| [√]    | [bus-all](bus-all)            | Microservice full aggregation module, containing all business components and common functionalities |
| [√]    | [bus-auth](bus-auth)          | Unified authentication center, supporting OIDC/OAuth2/SAML protocols, integrated with 15+ mainstream third-party login platforms |
| [√]    | [bus-base](bus-base)          | Basic architecture layer, providing entity/service/controller base classes and common business interfaces |
| [√]    | [bus-bom](bus-bom)            | Dependency management module, unified version control, supporting on-demand component loading |
| [√]    | [bus-cache](bus-cache)        | Distributed caching service, supporting Redis/Memcached/Hessian multi-level caching solutions |
| [√]    | [bus-core](bus-core)          | Core utility library, containing 20+ common utility classes for concurrency/reflection/date/collections |
| [√]    | [bus-cron](bus-cron)          | Distributed task scheduling, supporting CRON expressions and cluster task coordination |
| [√]    | [bus-crypto](bus-crypto)      | Encryption/decryption component, supporting AES/DES/SM4/MD5 algorithms and national cipher suites |
| [√]    | [bus-extra](bus-extra)        | Extended functionality package, integrating peripheral services like FTP/QR code/MQ/file processing |
| [√]    | [bus-gitlab](bus-gitlab)      | Deep GitLab integration, providing full lifecycle management for CI/CD/repositories/issues |
| [√]    | [bus-health](bus-health)      | Health monitoring center, real-time collection of JVM/OS/container/middleware monitoring metrics |
| [√]    | [bus-http](bus-http)          | HTTP client wrapper, providing synchronous/asynchronous/reactive three calling modes |
| [√]    | [bus-image](bus-image)        | Image processing engine, supporting format conversion/thumbnail generation/OCR recognition |
| [√]    | [bus-limiter](bus-limiter)    | High-performance rate limiting component, supporting token bucket/sliding window/distributed rate limiting strategies |
| [√]    | [bus-logger](bus-logger)      | Log enhancement module, supporting dynamic log levels/trace ID/sensitive data filtering |
| [√]    | [bus-mapper](bus-mapper)      | MyBatis enhancement tool, providing code generation/multi-tenancy/logical deletion extensions |
| [√]    | [bus-notify](bus-notify)      | Multi-channel notification center, supporting push methods like email/SMS/DingTalk/WeChat Work |
| [√]    | [bus-office](bus-office)      | Office document processing engine, implementing Excel/Word/PPT operations based on POI |
| [√]    | [bus-opencv](bus-opencv)      | Computer vision library, wrapping OpenCV to provide image recognition/face detection/video analysis capabilities |
| [√]    | [bus-parent](bus-parent)      | Parent POM, unified management of dependency versions/build configurations/coding standards |
| [√]    | [bus-pay](bus-pay)            | Payment aggregation service, integrating 20+ payment channels including WeChat Pay/Alipay/UnionPay |
| [√]    | [bus-proxy](bus-proxy)        | Dynamic proxy tool, simplifying JDK/CGLIB proxy implementation |
| [√]    | [bus-sensitive](bus-sensitive)| Sensitive data masking, providing annotation-based/rule-based data obfuscation solutions |
| [√]    | [bus-setting](bus-setting)    | Configuration management tool, supporting multi-environment configuration/dynamic refresh/encrypted storage |
| [√]    | [bus-shade](bus-shade)        | Code generator, one-click generation of basic code like Entity/Service/Mapper |
| [√]    | [bus-socket](bus-socket)      | Network communication framework, wrapping NIO/AIO to implement TCP/UDP/WebSocket communication |
| [√]    | [bus-starter](bus-starter)    | SpringBoot starter, auto-assembly of core components and configurations |
| [√]    | [bus-storage](bus-storage)    | Object storage service, supporting storage solutions like Alibaba Cloud OSS/Tencent Cloud COS/MinIO |
| [×]    | [bus-tracer](bus-tracer)      | Distributed tracing, integrating Zipkin/Pinpoint for full-link monitoring (in development) |
| [√]    | [bus-validate](bus-validate)  | Parameter validation framework, extending JSR-303 annotations to support custom validation rules |
| [√]    | [bus-vortex](bus-vortex)      | Reactive gateway, high-performance API gateway built on WebFlux |


### ✨ Function Overview

1. Java basic utility classes that encapsulate JDK methods for files, streams, encryption/decryption, transcoding, regular expressions, threading, XML, etc., forming various utility classes;
   Combined with SpringBoot, it encapsulates commonly used tools for on-demand loading, such as mybatis, xss, i18n, sensitive, validate, and other frameworks.

2. For detailed instructions and usage, please refer to the README in each module.


### ✨ Installation & Usage

#### Maven

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-all</artifactId>
    <version>x.x.x</version>
</dependency>
```

Or use a single component

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-xxx</artifactId>
    <version>x.x.x</version>
</dependency>
```

#### Gradle

```groovy
implementation 'org.miaixz:bus-all:x.x.x'
```

#### Download

Click any of the following links to download `bus-*-x.x.x.jar`:

- [Maven Central Repository](https://repo.maven.apache.org/maven2/org/miaixz)


### ✨ Testing

To ensure project compilation efficiency and related rules, please refer to the `abarth` project for all unit tests and usage:

- Address: [https://github.com/818000/abarth](https://github.com/818000/abarth)

> Note
> The Bus project supports Java 17+. Some modules have not been tested on the Android platform, and we cannot guarantee that all utility classes or methods will work.


### ✨ Feedback & Suggestions

All kinds of contributions (enhancements, new features, documentation & code improvements, issues & bugs reporting) are welcome.

We welcome various forms of contributions, including but not limited to optimizations, feature additions, documentation & code improvements, and reporting of issues and BUGs.


### ✨ Design Philosophy

Source code is always the best tutorial. For those who are good at reading source code and debugging, mastering it is effortless. Source code is the most intuitive display of the author's design philosophy, which is also the charm of open source.
"Talk is cheap, Show me the code."
Open source makes discussions about technical problems more practical. After reading the source code, you will have your own conclusion about it. In the author's view, Bus genuinely reduces the development learning threshold while ensuring high performance and high availability of services. If readers have doubts about the design of certain parts in the source code, you are welcome to communicate with the author.


### ✨ Project Status

![Alt](https://repobeats.axiom.co/api/embed/52a2707cd51eecee830f6d596187122ba3ca8810.svg "Repobeats analytics image")