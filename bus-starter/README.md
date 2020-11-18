## 使用说明

启动类中增加注解或者POM文件中增加依赖即可。
```java
    @EnableCorsFilter
    @EnableOnceFilter
    @EnableMapper
    @EnableDubbo
    @EnableCrypto
    @EnableValidate
    ...
```
  
1. 启用Cors 注解形式 `@EnableCorsFilter` 

2. 启用Druid 注解形式 `@EnableDruid` 

3. 启用Druid监控 注解形式 `@EnableDruids`同时会自动启用`@EnableDruid` 

4. 启用Dubbo 注解形式 `@EnableDubbo` 

5. 启用国际化 注解形式 `@EnableI18n` 
     
6. 启用Mapper 注解形式 `@EnableMapper`(同时会启用Druid)

7. 启用Wrapper 注解形式 `@EnableOnceFilter` 
 
8. 启用数据安全脱敏/加解密 注解形式 `@EnableSensitive` 

8. 启用第三方授权登陆 注解形式 `@EnableThirdAuth`

9. 启用OSS存储 注解形式 `@EnableStorage` 
      
10. 启用Validate 注解形式 `@EnableValidate` 



### 版本路由映射


- **@ApiVersion**
>* 通过此注解，自动为RequestMappingInfo合并一个以版本号开头的路径；建议：大版本在类上配置，小版本可以通过配置在方法上，此时将替换类上面的大版本配置

- **@ClientVersion**
>* 通过此注解，可以通过接口header中的cv,terminal参数路由倒不同的处理方法（handler method，基于RequestMappingHandlerMapping中的getCustom**Condition方法扩展）；

- **@VersionMapping** 
>* 组合注解，实现了RequestMapping的功能，同时提供了上述两种注解的配置


业务场景：
- ApiVersion：替换之前的版本定义在路径中，导致的接口升级需要重新定义类或者在代码中做判断的问题
- ClientVersion：碰到客户端已经在使用的接口，区分对待的情况下，通过通过ClientVersion优雅的避免在代码中写大量版本判断逻辑的问题

```java

@RequestMapping("/t")
@RestController
@ApiVersion("5")
public class TestController {
    //请求路径为/5/t/get
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
    @ClientVersion(expression = {"1>6.1.2"})
    public String cvcheck1(){return "6.1.2以上版本的1类型";}

    @GetMapping("/c")
    @ClientVersion({@TerminalVersion(terminals = 2,op= VersionOperator.GT,version = "6.1.2")})
    public String cvcheck2(){return "6.1.2以上版本的2类型";}

    @GetMapping("/c")
    @ClientVersion({@TerminalVersion(terminals = 2,op= VersionOperator.LTE,version = "6.1.2")})
    public String cvcheck3(){return "6.1.2以下版本的2类型";}

    public String c(){
        return "aaaa";
    }
}

```

```java
@RestController
@VersionMapping(value="/t",apiVersion = "5")
public class TController {

    @VersionMapping(value="a",terminalVersion = @TerminalVersion(terminals = 1,op = VersionOperator.EQ,version = "3.0"))
    public String t(){
        return "123";
    }

}
```