# 介绍

一个简单易用的接口开放平台，平台封装了常用的参数校验、结果返回等功能，开发者只需实现业务代码即可。

功能类似于[淘宝开放平台](http://open.taobao.com/docs/api.htm?spm=a219a.7629065.0.0.6cQDnQ&apiId=4)，它的所有接口只提供一个url，通过参数来区分不同业务。这样做的好处是接口url管理方便了，平台管理者只需维护好接口参数即可。由于参数的数量是可知的，这样可以在很大程度上进行封装。封装完后平台开发者只需要写业务代码，其它功能可以通过配置来完成。

得益于Java的注解功能以及Spring容器对bean的管理，我们的开放接口平台就这样产生了。

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

#### 功能①按参数路由：

#### 功能②按版本路由：

- **@ApiVersion**

> * 通过此注解，自动为requestMappinginfo合并一个以版本号开头的路径；建议：大版本在类上配置，小版本可以通过配置在方法上，此时将替换类上面的大版本配置

- **@ClientVersion**

> * 通过此注解，可以通过接口header中的cv,terminal参数路由倒不同的处理方法（handler method，基于RequestMappingHandlerMapping中的getCustom**Condition方法扩展）；

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
    @RequestMapping(value="/get")
    public String get1 (){
        return "旧接口";
    }

    //请求路径为/5.1/t/get
    @RequestMapping(value= "/get",params = "data=tree")
    @ApiVersion("5.1")
    //method的apiversion会优先于class上的,方便升级小版本
    public String get2(){
        return "新数据";
    }

    //以下三个请求路径都是/c，
    //通过header里的客户端类型（如果是从url参数取，修改TerminalVersionExpression即可）以及版本号路由到不同方法
    @GetMapping("/c")
    @ClientVersion(expression = {"1>6.0.0"})
    public String cvcheck1(){return "6.0.0以上版本的1类型";}

    @GetMapping("/c")
    @ClientVersion({@TerminalVersion(terminals = 2,op= VersionOperator.GT,version = "6.0.0")})
    public String cvcheck2(){return "6.0.0以上版本的2类型";}

    @GetMapping("/c")
    @ClientVersion({@TerminalVersion(terminals = 2,op= VersionOperator.LTE,version = "6.0.0")})
    public String cvcheck3(){return "6.0.0以下版本的2类型";}
 
}

```

```java
@RestController
@VersionMapping(value="/t",apiVersion = "5")
public class TController {

    @VersionMapping(value="a",terminalVersion = @TerminalVersion(terminals = 1,op = VersionOperator.EQ,version = "3.0"))
    public String t(){
        return "5";
    }

}
```
        