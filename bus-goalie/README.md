#### 项目说明

基于spring webflux 开发的API网关,是一个分布式,全异步,高性能,可扩展 ,轻量级的API网关。 立足于spring巨人的肩膀上,灵感来自阿里云的API网关;

## 功能特点

- 开箱即用，写完业务代码直接启动服务即可使用，无需其它配置
- 参数自动校验，支持国际化参数校验（JSR-303）
- 校验功能和结果返回功能实现各自独立，方便自定义实现或扩展
- 采用注解来定义接口，维护简单方便
- 支持i18n国际化消息返回
- 采用数字签名进行参数验证
- 采用appKey、secret形式接入平台，即需要给接入方提供一个appKey和secret

## 技术点

- 加密算法（MD5、AES、RSA）
- Netty（编解码、长连接、断开重连）
- 限流（漏桶策略、令牌桶策略）
- 权限（RBAC、校验）
- session（单机、分布式）
- 注解（文档生成）
- token（jwt、accessToken）
- SDK（Java、C#、JavaScript）
- 格式化(xml,json)

#### 功能①按参数路由：

Api接口类说明:

```java
public class Assets {

    private String id;   //接口id 唯一
    private String name; //接口名称
    private String host; //目标主机名
    private int port;    //目标端口
    private String url;  //目标url
    private String method; //对应请求参数method
    private HttpMethod httpMethod;
    private boolean token; //是否需要token (0 不需要,1需要)
    private boolean sign;  //返回内容是否加密(0 不需要,1需要) 需配置开启加密
    private boolean firewall; // 防火墙,预留
    private String version; //对应请求参数v
    private String description; //接口描述
}
```

请求参数说明:

| 参数     | 说明                                 |
|--------|------------------------------------|
| method | api的方法名   (xxx.xxx.xxx)            |
| v      | api的版本号,和method 一起使用 (1.1 ,1.2)    |
| format | 接口返回的格式,目前支持（json,xml） 两种          |
| sign   | 在配置文件中开启解密配置,若请求中包含sign字段,则对请求字段解密 |

配置文件说明:

```yaml
extend:
  goalie:
    server:
      port: 8765 #网关端口
      path: /router/rest #网关path
      encrypt:
        enabled: true  #是否开启加密
        key: xxxxxx #加密key
        type: AES #加密算法
        offset: xxxxxx #偏移量
      decrypt:
        enabled: true #是否开启解密
        key: xxxxxx #解密key
        type: AES #解密算法
        offset: xxxx #偏移量
      limit:
        enabled: true #是否开启限流

```

集成方式说明:

1.在springboot启动类加上注解`@EnableGoalie`

```java
@EnableGoalie
@SpringBootApplication
public class TunnelApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(TunnelApplication.class);
        app.run(args);
    }

}

```

2.实现至少包含一个`Registry`的 spring bean 保证缓中有接口

```java

@Component
public class DbAssetRegistriesImpl implements Registry {
    //TODO
}

```

3.实现一个`Authorize` 的 sping bean 保证身份认证功能正常

```java
public class AuthProviderImpl implements Authorize {
    //TODO
}
```

4.sping配置文件`application.yml`相应配置

扩展方式:

可实现`webfilter` 对网关功能扩展,例如限流,日志,黑名单，熔断(目前暂未实现☺☺)等

```java
@Component
@Order("123")
public class CustomFilter implements WebFilter {

}
```

#### 功能②按版本路由：

- **@ApiVersion**

> * 通过此注解，自动为requestMappinginfo合并一个以版本号开头的路径；建议：大版本在类上配置，小版本可以通过配置在方法上，此时将替换类上面的大版本配置

- **@ClientVersion**

> * 通过此注解，可以通过接口header中的cv,terminal参数路由倒不同的处理方法（handler
    method，基于RequestMappingHandlerMapping中的getCustom**
    Condition方法扩展）；

- **@VersionMapping**

> * 组合注解，实现了RequestMapping的功能，同时提供了上述两种注解的配置

业务场景：

- ApiVersion：替换之前的版本定义在路径中，导致的接口升级需要重新定义类或者在代码中做判断的问题
- ClientVersion：碰到客户端已经在使用的接口，区分对待的情况下，通过通过ClientVersion优雅的避免在代码中写大量版本判断逻辑的问题

```java

@RequestMapping("/t")
@RestController
@ApiVersion("5")
public class TController {
    //请求路径为/4/t/get
    @RequestMapping(value = "/get")
    public String get1() {
        return "旧接口";
    }

    //请求路径为/5.1/t/get
    @RequestMapping(value = "/get", params = "data=tree")
    @ApiVersion("5.1")
    //method的apiversion会优先于class上的,方便升级小版本
    public String get2() {
        return "新数据";
    }

    //以下三个请求路径都是/c，
    //通过header里的客户端类型（如果是从url参数取，修改TerminalVersionExpression即可）以及版本号路由到不同方法
    @GetMapping("/c")
    @ClientVersion(expression = {"1>6.0.0"})
    public String cvcheck1() {
        return "6.0.0以上版本的1类型";
    }

    @GetMapping("/c")
    @ClientVersion({@TerminalVersion(terminals = 2, op = VersionOperator.GT, version = "6.0.0")})
    public String cvcheck2() {
        return "6.0.0以上版本的2类型";
    }

    @GetMapping("/c")
    @ClientVersion({@TerminalVersion(terminals = 2, op = VersionOperator.LTE, version = "6.0.0")})
    public String cvcheck3() {
        return "6.0.0以下版本的2类型";
    }

}

```

```java

@RestController
@VersionMapping(value = "/t", apiVersion = "5")
public class TController {

    @VersionMapping(value = "a", terminalVersion = @TerminalVersion(terminals = 1, op = VersionOperator.EQ, version = "3.0"))
    public String t() {
        return "5";
    }

}
```
