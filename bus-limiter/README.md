#### 项目说明

## Quick Start

添加`@EnableLimiter` 注解

```java
@SpringBootApplication
@EnableLimiter
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
```

## How  To Use

以`Lock`组件为例

**Step 0. 定义锁资源**

下面的代码向`BeanFactory`中注册了一个`BeanId`为 `jdkLock`的实例

```java
@Bean
Lock jdkLock(){
        JdkLock jdkLock=new JdkLock("mnyJdkLock");
        return jdkLock;
        }
```

**Step 1. 编写业务接口**

我们假设有这样一个业务场景，用户可以使用一个Vip兑换码来延长自己的VIP期限，理所当然的是，每个兑换码只能被使用一次，通常情况下，我们会在数据库中查询该兑换码是否存在并且是否已经使用。

```java
@RequestMapping(method = RequestMethod.POST, value = "/exchangeVip")
public ResponseMessage exchangeVip(@RequestBody ExchangeVipRequest request){
        return demoService.exchangeVip(request,SpringAware.getCurrentUser());
        }
```

**Step 2.添加`HLock`注解**

上面的接口并不安全，假如在极短的的时间内用户发起了多次相同兑换的请求，由于数据库的事务隔离特性，该兑换码便会被多次兑换，这个漏洞可能被用户恶意使用，造成损失。
这里涉及的重放攻击问题此处不再深入讨论,(
欢迎移步我的[博客](https://blog.higgs.site/2019/06/24/从接口幂等性到重放攻击/#))。现在我们添加HLock注解保护该接口。

```java
@RequestMapping(method = RequestMethod.POST, value = "/exchangeVip")
@HLock(limiter = "jdkLock", key = "#request.vipCode", fallback = "fallbackToBusy")
public ResponseMessage exchangeVip(@RequestBody ExchangeVipRequest request){
        return demoService.exchangeVip(request,SpringAware.getCurrentUser());
        }
```

该注解的含义是，在请求到达时，使用`jdkLock`这个锁锁住`#request.vipCode`
这个资源，如果锁成功了，后面的逻辑继续进行，在业务逻辑完成后便会释放该资源，如果`#request.vipCode`
这个资源已经被锁定，便会降级到`fallbackToBusy`方法进行。这样其他相同 `#request.vipCode`的请求便会被拦截，

在同一class下添加降级方法 `fallbackToBusy`

```java
ResponseMessage fallbackToBusy(ExchangeVipRequest request){
        return ResponseMessage.error("服务繁忙，请稍后再试！");
        }
```

**Step 3. 使用分布式锁**

随着业务发展，单实例应用不能再满足业务的需求，分布式改造开始了。上面的jdkLock是一个存储在内存的锁，这意味着切换到多实例环境后，仍然可能在多个实例上同时发起多个相同的请求。要解决这个问题，只需要将锁切换至分布式锁即可，这里我们以RedisLock为例。

注入一个RedisLock

```java
@Bean
    Lock redisLock(){
            Config config=new Config();
            config.useSingleServer().setAddress("redis://127.0.0.1:6379")
            .setDatabase(1);
            RedissonClient redissonClient=null;
            try{
            redissonClient=Redisson.create(config);
            }catch(Exception e){
            logger.info("redis连接失败");

            }
            logger.info("redis连接成功");
            RedisLock redisLock=new RedisLock(redissonClient,"myRedisLock");

            return redisLock;
            }
```

修改注解

```java
@HLock(limiter = "redisLock", key = "#request.vipCode", fallback = "fallback")
```

**Step 4. star this niubility project**

## Document

`limiter`是作为顶层抽象，所有类型的限制组件均实现自该接口。limiter的定义非常简单

```java
public interface Limiter<T extends Annotation> {

    String getLimiterName();

    boolean limit(Object key, Map<String, Object> args);

    void release(Object key, Map<String, Object> args);
}
```

- `limit`表示锁定key值指定的资源，返回值作为是否锁定成功，锁定失败会立即进入降级逻辑。
- `release`表示释放已锁定的资源，会在正常的业务逻辑执行后执行。

具体类型的组件在继承limiter后进行更细粒度的抽象。例如Lock在作为进一步抽象，而用RedisLock和ZookeeperLock进行进一步的实现。

## 自带组件介绍

### 1. Lock

资源锁组件，用于限制资源的并发数量为1，抽象接口为 `Lock`

对应的注解为`HLock`

```java
public abstract class Lock implements Limiter<HLock> {

    public abstract boolean lock(Object key);

    public abstract void unlock(Object key);

    @Override
    public boolean limit(Object key, Map<String, Object> args) {
        return lock(key);
    }

    @Override
    public void release(Object key, Map<String, Object> args) {
        unlock(key);
    }
}
```

**提供三种实现**：

- 以`ConcurrentHashMap`和`ReentrantLock` 为基础的`JdkLock`，适应于单实例环境。
- 以Redis 为基础的`RedisLock`，适应于对资源容量和速度要求较高的分布式环境。底层为`redisson`实现，有完善的锁延期和防死锁机制。
- 以Zookeeper为基础的`ZookeeperLock`，适应于对一致性要求极高的分布式环境，速度和容量不及Redis实现

### 2. RateLimiter

频率限制器，用于限制某一资源的访问频率，抽象接口为`RateLimiter`，对应的注解为`HRateLimiter`

```java
public abstract class RateLimiter implements Limiter<HRateLimiter> {

    public abstract boolean acquire(Object key, double rate, long capacity);

    @Override
    public boolean limit(Object key, Map<String, Object> args) {
        double pps = (double) args.get("rate");
        long capacity = (long) args.get("capacity");
        return acquire(key, pps, capacity);
    }

    @Override
    public void release(Object key, Map<String, Object> args) {
        // do noting
    }
}

```

**提供两种实现：**

- 以`GuavaCache`和令牌桶算法为基础的`JdkRateLimiter`
- 以`Redis`和令牌桶算法为基础的`RedisRatelimiter`

### 3. Peak

限制一个资源的并发数小于固定值，抽象接口为`PeakLimiter`,对应的注解为

`HPeak`

```java
public abstract class PeakLimiter implements Limiter<HPeak> {

    public abstract boolean acquire(Object key, int max);

    public abstract void release(Object key, int max);

    @Override
    public boolean limit(Object key, Map<String, Object> args) {
        return acquire(key, (int) args.get("max"));
    }

    @Override
    public void release(Object key, Map<String, Object> args) {
        release(key, (int) args.get("max"));
    }
}
```

**提供两种实现：**

- 以`GuavaCache`和`Semaphore`为基础的`JdkPeakLimiter`，适应于单实例环境。
- 以`Redis`为基础的`RedisPeakLimiter`，适应于多实例环境。

## 注解介绍

### 1. 注解通用基础属性

所有类型的注解都默认包含下面5个属性

- **limiter** : 使用的限流器的BeanId,将会从Spring的BeanFacotry中获取，不能为空。 例如：设置limiter="jdkLock" 便会使用该限流组件

```java
  @Bean
  Lock jdkLock(){
          JdkLock jdkLock=new JdkLock("mnyJdkLock");
          return jdkLock;

          }
```

```java
@HLock(limiter = "jdkLock")
```

- **key**  :
  资源限制键，为空时将使用类名+方法名作为key，可以实现某一类需求。key中可以使用的参数包括方法的入参，参数注入器注入的参数。具体语法参考`SPEL`
  的语法。

- **fallback** : 在锁定资源失败时，触发的降级策略，默认为`defaultFallbackResolver`。limiter工作时，

  会优先寻找同一Class下相同入参的且方法名为指定值得方法作为降级方法，如果未找到该方法，

  在从`BeanFactory`中查找，该对象应该实现 `LimitedFallbackResolver`接口，例如

```java
  public class MyFallback implements LimitedFallbackResolver {
      @Override
      public Object resolve(Method method, Class clazz, Object[] args, LimitedResource limitedResource, Object target) {
          return null;
      }
  }
```

- **errorHandler** :   从BeanFactory中获取。
  限流组件出现错误时的处理方法，比如使用RedisLock作为分布式锁时，Redis宕机了，如果不想影响业务进行可以选择跳过该限流器。更好的策略应该在具体的限流实现中处理，此处作为一个兜底方法，
  默认策略为`defaultErrorHandler`，即抛出异常。该对象应该实现 接口

```java
  public class MyErrorHandler implements ErrorHandler {
      @Override
      public boolean resolve(Throwable throwable, LimiterExecutionContext executionContext) {
          // 返回true为跳过该限制器
          return true;
      }
  }
  
```

- **argumentInjectors**
  ：从BeanFactory中获取，可配置多个。参数注入器。如果我们想要使用方法入参之外的参数作为key的变量，可以使用参数注入器注入，比如从用户上下文中注入用户id、从请求上下文中注入ip。该对象应该实现`ArgumentInjector`
  接口

```java
  public class UserInfoArgumentInjector implements ArgumentInjector {
      @Override
      public Map<String, Object> inject(Object... args) {
          Map<String, Object> ret = new HashMap<>();
          ret.put("userInfo", SpringAware.getCurrentUser());
          return ret;
      }
  }
```

```java
   @Bean
   ArgumentInjector userInfoArgumentInjector(){
           return new UserInfoArgumentInjector();
           }
```

```java
  // 注入userInfo 信息 并在key中使用 
@HLock(limiter = "jdkLock", key = "#request.vipCode +  #userInfo.userId", fallback = "fallback", argumentInjectors = "userInfoArgumentInjector")
public ResponseMessage exchangeVipOnJDKLock(@RequestBody ExchangeVipRequest request){
        // ...
        }
```

#### 2. @HLock 注解

业务中最常见的需要，限制某一个资源的并发数量。下面的例子即为限制相同`vipCode`的请求最大并发数为1

```java
@RequestMapping(method = RequestMethod.POST, value = "/exchangeVip")
@HLock(limiter = "jdkLock", key = "#request.vipCode", fallback = "fallbackToBusy")
public ResponseMessage exchangeVip(@RequestBody ExchangeVipRequest request){
        return demoService.exchangeVip(request,SpringAware.getCurrentUser());
        }
```

#### 3. @HPeak 注解

如果你对Java中的信号量(`Semaphore`)熟悉，则你会很容易理解这个注解。该注解的含义是限制一个资源的并发数量。

- **max** ： 最多的并发数量，默认值为10.

#### 4. @HRateLimiter注解

从名字便可以看出，这是用来限制调用频率的，额外的配置

- **rate** ： 限制该资源的调用频率，单位为 次/秒，默认值为10
- **capacity** ： 该资源最多可累计的数量，
  比如该资源限制调用的频率为10次/秒，但是该资源已经3秒没有被调用过了，如果最大可累计数量为20，那该资源可在短期内超出10次/秒的限制。更多细节可以参考令牌桶算法。

## 如何扩展

Limiter提供了标准的扩展方式，开发者可以添加自定义组件。

- 定义组件的注解
- 定义新的限流组件
- 定义该组件的resource和meta
- 定义注解解析器
- 添加一个实现
- 使该组件生效

假设我们需要一个黑名单限制组件，我们想要实现一个简单的效果：在请求到达时，检查请求发起者是否是该接口的黑名单用户，如果是黑名单用户，则降级该请求。我们开始实现一个简陋的黑名单校验。

**SETP 0. 添加黑名单的注解**

```java
public @interface HBlacklist {
    String limiter() default "";

    // 默认的key应该是用户id，用户信息可以用参数注入的方法注入
    String key() default "#userInfo.userId";

    // 我们可以指定默认的降级方法
    String fallback() default "defaultFallbackResolver";

    String errorHandler() default "defaultErrorHandler";

    String[] argumentInjectors() default {};

    // 我们用这个值区分不同的接口
    String serviceId() default "";
}

```

**STEP 1.定义黑名单抽象类**

```java
public abstract class BlacklistLimiter implements Limiter<HBlacklist> {


    // 检查userId是否是serviceId的黑名单用户
    public abstract boolean checkExist(Object userId, String serviceId);

    @Override
    public boolean limit(Object key, Map<String, Object> args) {
		// key值解析出来的应该是userId 这里需要在使用时注意
        // 注意下文关于@LimiterParameter解释，你便会理解 args.get("serviceId")从哪里而来
        return !checkExist(key, (String) args.get("serviceId"));
    }

    @Override
    public void release(Object key, Map<String, Object> args) {

        //do nothing
    }
}
```

**STEP 2. 定义该组件的resource和meta**

所谓resource即将该注解解析出来后的一个容器，meta即将该resource在Spring工厂的配合下进行组装后的产物

serviceId用`@LimiterParameter`标注，在装配成meta时，会作为限流器的参数放入map中

```java
public class BlacklistResource extends AbstractLimitedResource {


    @LimiterParameter
    String serviceId;

    public BlacklistResource(String key, Collection<String> argumentInjectors, String fallback, String errorHandler, String limiter, String serviceId) {
        super(key, argumentInjectors, fallback, errorHandler, limiter);
        this.serviceId = serviceId;
    }


    @Override
    public LimitedResourceMetadata createMetadata(BeanFactory beanFactory, Class targetClass, Method targetMethod) {
        return new BlacklistLimiterResourceMetadata(this, targetClass, targetMethod, beanFactory);
    }
}

```

```java
public class BlacklistLimiterResourceMetadata extends AbstractLimitedResourceMetadata<BlacklistResource> {


    public BlacklistLimiterResourceMetadata(BlacklistResource limitedResource, Class<?> targetClass, Method targetMethod, BeanFactory beanFactory) {
        super(limitedResource, targetClass, targetMethod, beanFactory);
    }

    @Override
    protected void parseInternal(BlacklistResource limitedResource) {

    }
}
```

**SETP 3. 定义注解解析器**

下面的逻辑即为解析`HBlacklist`注解的过程，解析的结果是一个`BlacklistResource`

```java
public class BlacklistAnnotationParser extends AbstractLimiterAnnotationParser<BlacklistLimiter, HBlacklist> {
    @Override
    public LimitedResource<BlacklistLimiter> parseLimiterAnnotation(AnnotationAttributes attributes) {
         return new BlacklistResource(getKey(attributes),
                getArgumentInjectors(attributes),
                getFallback(attributes),
                getErrorHandler(attributes),
                getLimiter(attributes),
                attributes.getString("serviceId")
        );
        
    }
}
```

**SETP 4. 添加一个实现**

我们添加一个简单的实现，从Redis中检查该用户是否是该serciceId的该黑名单用户。

```java
public class RedisBlacklistLimiter extends BlacklistLimiter {

    String name;

    RedisClient redisClient;

    public RedisBlacklistLimiter(String name, RedisClient redisClient) {
        this.name = name;
        this.redisClient = redisClient;
    }

    @Override
    public boolean checkExist(Object userId, String serviceId) {
        // 查询redis
        // 返回是否存在
        return false;
    }

    @Override
    public String getLimiterName() {
        return name;
    }
}
```

**STEP 5.使该组件生效**

***在注解中指定注解解析器的全限定类名，启用该组件***

```java
@EnableLimiter(annotationParser = "org.miaixz.bus.limiter.limitertest.extend.BlacklistAnnotationParser")
```

## 性能测试

### 测试环境

- Docker 1.13.1 8核16G
- Redis CGroup 2核4G
- Application CGroup 2核4G

#### 1. Lock组件测试

- 4线程1000连接压测120秒
- tomcat.max-threads=1000，
- 业务代码执行时间均为1000ms
- Java 17+
- Zoookeeper和Redis和应用处于同一主机
- Zookeeper为3节点

|               | AVG RT    | AVG-QPS |
|---------------|-----------|---------|
| None          | 1008.38ms | 986.26  |
| JdkLock       | 1012.50ms | 984.79  |
| RedisLock     | 1034.64ms | 960.39  |
| ZookeeperLock | 1096.42ms | 914.52  |

#### 2. RateLimiter性能测试

- 4线程1000连接压测120秒
- tomcat.max-threads=1000，
- 业务代码执行时间均为1000ms
- Java 17+
- Zoookeeper和Redis和应用处于同一主机

|                  | AVG RT    | AVG-QPS |
|------------------|-----------|---------|
| None             | 1008.38ms | 986.26  |
| jdkRateLimiter   | 1008.50ms | 979.16  |
| RedisRatelimiter | 1022.01ms | 976.51  |

### 未来可能增加的feature

- 增加配置中心接入，支持动态修改注解配置
- 增加一些组件类型



