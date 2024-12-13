### 介绍

轻量级本地化热点检测/降级框架，适用于大流量场景，可轻松解决业务中超高流量的并发查询等场景。并且接入和使用极其简单，10秒钟即可接入使用！

### 使用

引入依赖：

```xml
<dependency>
  <groupId>org.miaixz</groupId>
  <artifactId>bus-limiter</artifactId>
  <version>x.x.x</version>
</dependency>
```

#### 对任意方法进行热点处理

只需要加上`@Hotspot`这个标注，任意方法均可以获得热点检测，并在热点期间用热点数据进行返回，在热点过后，又会自动调用原本业务逻辑。

举例：比如有一个商品查询的业务，传入tid，返回商品信息。当某个商品进行促销时，访问的量就会增加，但是对于相同的tid而言，其短时间窗口内返回的信息是一致的，框架能够在短时间内把这个tid提为热点数据，并通过对其进行缓存返回来降低对下游业务的压力。而当热点值过后，框架又能够自动摘除这个热点值，使其按照原有方式进行查询。

其本质相当于实时的监测了热点，并对其热点数据做了一个短时间内的缓存。

以下示例代表了：当相同的tid在5秒内超过50次调用时，会自动把这个tid的值提为热点，并用最后一次的返回值直接返回。当调用低于5秒50次调用时，框架会自动的摘除掉这个热点。使其正常的调用你原有代码进行逻辑计算并返回。这一切都是自动的。

```java
@Hotspot(grade = FlowGrade.FLOW_GRADE_QPS, count = 50, duration = 5)
public Object get(String tid){
    return tid;
}
```

其中`grade`参数除了有以`QPS`作为维度统计，还有以`Thread`个数作为维度统计。比如：

```java
@Hotspot(grade = FlowGrade.FLOW_GRADE_THREAD, count = 50, duration = 5)
public Object get(String tid){
    return tid;
}
```

这就代表了，如果某个tid在5秒之内有超过50个线程正在运行，那么就提为热点，并用热点数据直接返回。

#### 对任意方法进行降级

只需要加上`@Downgrade`注解。任意方法均可获得降级功能。

举例：某一个方法需要调用外部的接口，但是外部的接口性能不佳，耗时高。当并发一高时，线程池就会吃满，线程池队列也会逐渐堆积从而导致超时，或者丢弃，严重时会拖垮整个系统。

这时，我们只要对这个方法加上`@Downgrade`标注，即可解决。

```java
@Downgrade(grade = FlowGrade.FLOW_GRADE_THREAD, count = 100)
public String get(String name){
  return name;
}

public String getFallback(String name){
  return "fallback";
}
```

以上注解表示了，当这个方法的同时运行的线程超过100个时，触发降级，降级会自动调用`原方法名+Fallback`方法名(并且参数要一致)
，当降级触发后会直接返回`fallback`，当线程数小于100时，框架也会自动摘除降级，还是输出`name`。

如果你的类中没有定义fallback方法，那么触发降级时会报错，当然你可以在降级方法中去抛错，来让上游系统知道你这个方法已经达到了瓶颈。

### 注意事项

只针对于Springboot，Spring环境，并且所有标注了`@Hotspot`或者`@Downgrade`的类一定得注册到spring上下文中
