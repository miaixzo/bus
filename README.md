<p align="center">
  <a href="https://www.miaixz.org"><img src="LOGO.svg" width="45%"></a>
</p>
<p align="center">
  <a target="_blank" href="https://search.maven.org/search?q=org.miaixz">
    <img src="https://img.shields.io/badge/maven--central-v8.2.6-blue.svg?label=Maven%20Central" />
  </a>
  <a target="_blank" href="https://travis-ci.org/818000/bus">
    <img src="https://app.travis-ci.com/818000/bus.svg?token=TZPNK5FQiKMp9cao3SnY&branch=main">
  </a>
  <a target="_blank" href="https://jdk.java.net/">
    <img src="https://img.shields.io/badge/Java-17+-green.svg">
  </a>
  <a target="_blank" href="https://spring.io/projects/spring-boot">
    <img src="https://img.shields.io/badge/Spring Boot-4.0.0.M1-brightgreen.svg">
  </a>
  <a target="_blank" href="https://www.mysql.com">
    <img src="https://img.shields.io/badge/Mysql-8.x-blue.svg">
  </a>
  <a target="_blank" href="http://dubbo.apache.org">
    <img src="https://img.shields.io/badge/dubbo-3.2.11-yellow.svg">
  </a>
  <a target="_blank" href="http://poi.apache.org">
    <img src="https://img.shields.io/badge/poi-5.2.5-blue.svg">
  </a>
  <a target="_blank" href="https://github.com/818000/bus/blob/main/LICENSE">
    <img src="https://img.shields.io/badge/license-MIT-green.svg">
  </a>
</p>

---

### ✨ 项目说明

Bus (企业级应用/服务总线) 是一个基础框架、服务套件；基于Java 17+构建的轻量级企业级应用/服务框架，提供标准化服务套件与分布式中间件解决方案。通过模块化设计整合微服务架构核心能力，可作为后端系统开发的统一技术底座。

欢迎大家来 这里 踩踩,生命有限！少写重复代码！给颗星奖励下呗~

目标期望能努力打造一套从 基础框架 - 分布式微服务架构 - 持续集成 - 自动化部署 -系统监测等，快速实现业务需求的全栈式技术解决方案。


### ✨ 版本选择

Bus主要版本 3.x、5.x、6.x、8.x，具体如下：

| 版本  | jdk    | Maven仓库                                                                            | 主要特点                                |
|-----|--------|------------------------------------------------------------------------------------|-------------------------------------|
| 3.x | jdk1.8 | [org.aoju/bus-all/3.x](https://mvnrepository.com/artifact/org.aoju/bus-all/3.6.9)  | jdk1.8编译                            |
| 5.x | jdk1.8 | [org.aoju/bus-all/5.x ](https://mvnrepository.com/artifact/org.aoju/bus-all/5.9.9) | jdk1.8编译                            |
| 6.x | jdk 17 | [org.aoju/bus-all/6.x ](https://mvnrepository.com/artifact/org.aoju/bus-all/6.6.0) | jdk 17编译,使用Jakarta EE,适配JDK11、17    |
| 8.x | jdk 21 | [org.miaixz/bus-all/8.x ](https://mvnrepository.com/artifact/org.miaixz/bus-all)   | jdk 21编译,使用Jakarta EE,适配JDK11、17、21 |


### ✨ 组件信息

| 完成 | 模块                             | 描述信息                                           |
|------|--------------------------------|------------------------------------------------|
| [√]  | [bus-all](bus-all)             | 微服务全量聚合模块，包含所有业务组件及通用功能                        |
| [√]  | [bus-auth](bus-auth)           | 统一认证中心，支持OIDC/OAuth2/SAML等协议，集成国内外15+主流第三方登录平台 |
| [√]  | [bus-base](bus-base)           | 基础架构层，提供实体/服务/控制器基类及通用业务接口                     |
| [√]  | [bus-bom](bus-bom)             | 依赖管理模块，统一版本控制，支持按需加载组件                         |
| [√]  | [bus-cache](bus-cache)         | 分布式缓存服务，支持Redis/Memcached/Hessian等多级缓存方案       |
| [√]  | [bus-core](bus-core)           | 核心工具库，包含并发/反射/日期/集合等20+常用工具类                   |
| [√]  | [bus-cron](bus-cron)           | 分布式任务调度，支持CRON表达式及集群任务协调                       |
| [√]  | [bus-crypto](bus-crypto)       | 加密解密组件，支持AES/DES/SM4/MD5等算法及国密套件               |
| [√]  | [bus-extra](bus-extra)         | 扩展功能包，集成FTP/二维码/MQ/文件处理等外围服务                   |
| [√]  | [bus-gitlab](bus-gitlab)       | GitLab深度集成，提供CI/CD/仓库/问题等全生命周期管理               |
| [√]  | [bus-health](bus-health)       | 健康监控中心，实时采集JVM/OS/容器/中间件监控指标                   |
| [√]  | [bus-http](bus-http)           | HTTP客户端封装，提供同步/异步/响应式三种调用模式                    |
| [√]  | [bus-image](bus-image)         | 图像处理引擎，支持格式转换/缩略图生成/OCR识别等功能                   |
| [√]  | [bus-limiter](bus-limiter)     | 高性能限流组件，支持令牌桶/滑动窗口/分布式限流策略                     |
| [√]  | [bus-logger](bus-logger)       | 日志增强模块，支持动态日志级别/链路追踪ID/敏感数据过滤                  |
| [√]  | [bus-mapper](bus-mapper)       | MyBatis增强工具，提供代码生成/多租户/逻辑删除等扩展功能               |
| [√]  | [bus-notify](bus-notify)       | 多通道通知中心，支持邮件/短信/钉钉/企业微信等推送方式                   |
| [√]  | [bus-office](bus-office)       | Office文档处理引擎，基于POI实现Excel/Word/PPT操作           |
| [√]  | [bus-opencv](bus-opencv)       | 计算机视觉库，封装OpenCV提供图像识别/人脸检测/视频分析能力              |
| [√]  | [bus-pager](bus-pager)         | 分页查询增强，支持物理分页/多数据源/动态排序                        |
| [√]  | [bus-parent](bus-parent)       | 父级POM，统一管理依赖版本/构建配置/编码规范                       |
| [√]  | [bus-pay](bus-pay)             | 支付聚合服务，集成微信/支付宝/银联等20+支付渠道                     |
| [√]  | [bus-proxy](bus-proxy)         | 动态代理工具，简化JDK/CGLIB代理实现                         |
| [√]  | [bus-sensitive](bus-sensitive) | 敏感数据脱敏，提供注解式/规则式数据遮蔽方案                         |
| [√]  | [bus-setting](bus-setting)     | 配置管理工具，支持多环境配置/动态刷新/加密存储                       |
| [√]  | [bus-shade](bus-shade)         | 代码生成器，一键生成Entity/Service/Mapper等基础代码           |
| [√]  | [bus-socket](bus-socket)       | 网络通信框架，封装NIO/AIO实现TCP/UDP/WebSocket通信          |
| [√]  | [bus-starter](bus-starter)     | SpringBoot启动器，自动装配核心组件及配置                      |
| [√]  | [bus-storage](bus-storage)     | 对象存储服务，支持阿里云OSS/腾讯云COS/MinIO等存储方案              |
| [×]  | [bus-tracer](bus-tracer)       | 分布式链路追踪，集成Zipkin/Pinpoint实现全链路监控（开发中）          |
| [√]  | [bus-validate](bus-validate)   | 参数校验框架，扩展JSR-303注解支持自定义校验规则                    |
| [√]  | [bus-vortex](bus-vortex)       | 响应式网关，基于WebFlux构建的高性能API网关                     |


### ✨ 功能概述

1. Java基础工具类，对文件、流、加密解密、转码、正则、线程、XML等JDK方法进行封装，组成各种工具类；
   以及结合springboot封装常用工具按需加载例如mybatis、xss、i18n、sensitive、validate等框架

2. 详细说明以及使用姿势请参考每个模块下README介绍


### ✨ 安装使用

#### Maven

```
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-all</artifactId>
    <version>x.x.x</version>
</dependency>
```

或者单独使用某个组件

```
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-xxx</artifactId>
    <version>x.x.x</version>
</dependency>
```

#### Gradle

```
implementation 'org.miaixz:bus-all:x.x.x'
```

#### Downlad

点击以下任一链接，下载`bus-*-x.x.x.jar`即可：

- [Maven中央库](https://repo1.maven.org/maven2/org/miaixz)


### ✨ 测试使用

为确保项目编译效率及相关规则，本项目所有单元测试及使用请参考`abarth`项目：

- 地址: [https://github.com/818000/abarth](https://github.com/818000/abarth)

> 注意
> Bus项目支持Java 17+，对Android平台部分模块没有测试，不能保证所有工具类或工具方法可用。


### ✨ 意见建议

All kinds of contributions (enhancements, new features, documentation & code improvements, issues & bugs reporting) are
welcome.

欢迎各种形式的贡献，包括但不限于优化，添加功能，文档 & 代码的改进，问题和 BUG 的报告。


### ✨ 设计理念

源码永远是最好的教程，善于读源码和DEBUG朋友掌握完全是轻而易举的事。源码是作者设计理念最直观的展现，这也是开源的魅力所在。
"Talk is cheap, Show me the code."
，开源让技术难题的探讨变得更加务实，在您看完源码后心中对它都会有一个定论。在作者看来，Bus切切实实降低了开发学习门槛，也保障了服务的高性能、高可用。如果读者朋友对源码中某些部分的设计存在疑虑，也欢迎与作者保持沟通。


### ✨ 项目状态

![alt](https://repobeats.axiom.co/api/embed/e6aec865b2638153d492db3db4ab8f09b39725cd.svg "repobeats analytics image")