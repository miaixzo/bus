# 工业级缓存解决方案

- 多级缓存设计&实现;
- 消除限制5: `@CachedPut`注解;
- `@Invalid`开启前向清除缓存;
- 缓存预热;

----

# Cache使用限制

---

### 1. 多个@CacheKey属性为批量模式

```java
@Cached
Object func(@CacheKey("#arg0[#i]") List<Long> fIds,@CacheKey("#arg1[#i]") List<Long> aIds);
```

> 该模式会导致CacheX对方法参数做**笛卡尔积**, 结果将会计算产生大量的缓存`key`, 性能损耗较大, 因此不支持.

### 2. 以参数Map作为批量模式参数

```java
@Cached
Object func(@CacheKey("#arg0[#i]") Map<Long, Object> map);
```

> 同上: 如果将Map内所有的`Key`/`Value`进行交叉拼装为缓存`key`的话, 也会产生类似**笛卡尔积**的效果, 因此也不支持.

### 3. 以Map.keySet()作为批量模式参数

```java
@Cached
Object func(@CacheKey("#arg0.keySet()[#i]") Map<Long, Object> map);
```

> 这种模式不常用且实现复杂、性能损耗较大, 因此不支持.

### 4. 非标准容器作为批量模式参数

```java
@Cached
Object func(@CacheKey("#arg0[#i]") List<Long> ids){
        return Collections.emptyList();
        }
```

> 由于在批量模式下, Cache会在构造容器返回值时反射调用容器类的默认构造方法, 以及向容器内添加元素, 但这些容器并未暴露这些方法,
> 因此不能支持.

- 这类容器有:
    - Arrays.ArrayList
    - Collections.SingleList
    - ...

## I. 简单使用

### 配置

- pom

```xml
<dependency>
    <groupId>org.miaixz.bus</groupId>
    <artifactId>bus-cache</artifactId>
    <version>x.x.x</version>
</dependency>
```

- XML注册

```xml
<!-- 启用自动代理: 如果已经开启则不必重复开启 -->
<aop:aspectj-autoproxy proxy-target-class="true"/>

<!-- 配置CacheX切面(Cache代理需要手动生效) -->
<bean id="cache" class="CacheAspect">
    <constructor-arg name="caches">
        <map>
            <entry key="default" value-ref="guava"/>
            <entry key="redis" value-ref="redis"/>
        </map>
    </constructor-arg>
</bean>

        org.miaixz.bus.cache.metrics.cache.Cache实现 -->
<bean id="guava" class="GuavaCache">
    <constructor-arg name="expire" value="600000"/>
    <constructor-arg name="size" value="100000"/>
</bean>

<bean id="redis" class="RedisCache">
    <constructor-arg name="jedisPool" ref="jedisPool"/>
</bean>
```

---

### 使用

#### 1. 添加缓存(`@Cached` & `@CacheKey`)

- 在要添加缓存的方法上标`@Cached`
- 在要组装为key的方法参数上标`@CacheKey`

---

#### 2. 缓存失效(`@Invalid` & `@CacheKey`)

---

## II. 注解详解

> CacheX提供如下注解`@Cached`、`@Invalid`、`@CacheKey`.
(ext: `@CachedGet`、`@CachedWrite`)

---

### @Cached

- 在需要走缓存的方法前添加`@Cached`注解.

```java
@Documented
@Target(value = ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cached {


    /**
     * 指定使用的缓存实现
     * <p>
     * 默认使用在CacheAspect中配置的第一个caches
     * </p>
     *
     * @return 缓存实现名称
     */
    String value() default Normal.EMPTY;

    /**
     * 指定每个键的前缀
     * <p>
     * 如果方法没有参数，prefix将作为该方法使用的常量键
     * </p>
     *
     * @return 键前缀
     */
    String prefix() default Normal.EMPTY;

    /**
     * 使用SpEL表达式
     * <p>
     * 当这个SpEL表达式为true时，该方法会使用缓存
     * </p>
     *
     * @return SpEL条件表达式
     */
    String condition() default Normal.EMPTY;

    /**
     * 过期时间
     * <p>
     * 时间单位：毫秒
     * </p>
     *
     * @return 过期时间（毫秒）
     */
    int expire() default CacheExpire.FOREVER;
}
```

|     属性      | 描述                                                  | Ext                                                                        |
:-----------:|-----------------------------------------------------|----------------------------------------------------------------------------
|   `value`   | 指定缓存实现: `CacheXAspect`/`CacheXProxy`的`caches`参数的key | 选填: 默认为注入caches的第一个实现(即`caches`的第一个Entry实例)                                |
|  `prefix`   | 缓存**key**的统一前缀                                      | 选填: 默认为`""`, 若方法无参或没有`@CacheKey`注解, 则必须在此配置一个`prefix`, 令其成为***缓存静态常量key*** |
| `condition` | SpEL表达式                                             | 选填: 默认为`""`(`true`), 在CacheX执行前会先eval该表达式, 当表达式值为`true`才会执行缓存逻辑            |
|  `expire`   | 缓存过期时间(秒)                                           | 选填: 默认为`Expire.FOREVER`                                                    |

---

### @Invalid

- 在需要失效缓存的方法前添加`@Invalid`注解.

```java
@Documented
@Target(value = ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Invalid {

    /**
     * 指定使用的缓存实现
     * <p>
     * 与@Cached注解的value属性功能相同
     * </p>
     *
     * @return 缓存实现名称
     */
    String value() default Normal.EMPTY;

    /**
     * 指定每个键的前缀
     * <p>
     * 与@Cached注解的prefix属性功能相同
     * </p>
     *
     * @return 键前缀
     */
    String prefix() default Normal.EMPTY;

    /**
     * 使用SpEL表达式
     * <p>
     * 与@Cached注解的condition属性功能相同 当这个SpEL表达式为true时，该方法会使缓存失效
     * </p>
     *
     * @return SpEL条件表达式
     */
    String condition() default Normal.EMPTY;

}
```

> 注解内属性含义与`@Cached`相同.

---

### @CacheKey

- 在需要作为缓存key的方法参数前添加`@CacheKey`注解.

```java
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheKey {


    /**
     * 使用参数的一部分作为缓存的关键部分
     * <p>
     * 可以使用SpEL表达式指定参数的特定部分作为键的组成部分
     * </p>
     *
     * @return SpEL表达式，用于指定参数的哪部分作为键
     */
    String value() default Normal.EMPTY;

    /**
     * 使用多模型(value has `# i ` index)方法返回{@code Collection} {@code field} 指示与此参数相关的 {@code Collection} 实体字段中的哪个
     * <p>
     * 当方法返回Collection类型时，指定Collection中对象的哪个字段作为键的组成部分
     * </p>
     *
     * @return 字段名称，用于指定Collection中对象的哪个字段作为键
     */
    String field() default Normal.EMPTY;

}
```

|   属性    | 描述                                                                        | Ext         |
:-------:|---------------------------------------------------------------------------|-------------
| `value` | SpEL表达式: 缓存key的拼装逻辑                                                       | 选填: 默认为`""` |
| `field` | **批量模式**(`value`参数包含`#i`索引)且方法返回值为`Collection`时生效: 指明该返回值的某个属性是与该参数是关联起来的 | 详见Ext.批量模式  |

---

### Ext. @CachedGet

- 在需要走缓存的方法前添加`@CachedGet`注解.

> 与`@Cached`的不同在于`@CachedGet`只会从缓存内查询, 不会写入缓存(当缓存不存在时, 只是会取执行方法,
> 但不讲方法返回内容写入缓存).

```java
@Documented
@Target(value = ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CachedGet {

    /**
     * 指定使用的缓存实现
     * <p>
     * 默认使用在CacheAspect中配置的第一个caches
     * </p>
     *
     * @return 缓存实现名称
     */
    String value() default Normal.EMPTY;

    /**
     * 指定每个键的前缀
     * <p>
     * 如果方法没有参数，prefix将作为该方法使用的常量键
     * </p>
     *
     * @return 键前缀
     */
    String prefix() default Normal.EMPTY;

    /**
     * 使用SpEL表达式
     * <p>
     * 当这个SpEL表达式为true时，该方法会使用缓存
     * </p>
     *
     * @return SpEL条件表达式
     */
    String condition() default Normal.EMPTY;

}
```

> 注解内属性含义与`@Cached`相同.

---

### Ext. 批量模式

在该模式下: `#i`指定了ids作为批量参数: 假设ids={1,2,3},
CacheX会结合前面的prefix组装出 {`[USER]:1`、`[USER]:2`、`[USER]:3`} 这3个key去批量的查询缓存,
假设只有{1,2}能够命中, 则CacheX会只保留{3}去调用`getUsers()`方法, 将返回值写入缓存后, 将两部分内容进行merge返回.

1. 注意1: 如果方法的返回值为`Collection`实例: 则`@CacheKey`必须指定`field`参数, 该参数会指定`Collection`元素(如`User`)
   内的某个属性(如`id`)与批量参数的元素(如`ids`
   内的元素项)是一一对应的, 这样CacheX就可以根据该属性提取出参数值, 拼装key然后写入缓存.
2. 注意2. 如果方法的返回值为`Map`实例: 则`field`属性不填, 默认使用Map的Key作为`field`.
3. 注意3. `#i`作为批量模式指示器, 批量模式需要使用`#i`来开启, `#i`指明某个参数作为批量参数,
   CacheX会不断的迭代该参数生成批量缓存key进行缓存的读写.

---

### Ext. SpEL执行环境

对于`@CacheKey`内的`value`属性(SpEL), CacheX在将方法的参数组装为key时, 会将整个方法的参数导入到SpEL的执行环境内,
所以在任一参数的`@CacheKey`的`value`
属性内都可以自由的引用这些变量, 尽管在`arg0`我们可以引用整个方法的任意参数, 但为了可读性,
我们仍然建议对某个参数的引用放在该参数自己的`@CacheKey`

> 注意: 在Java8环境中, 如果编译时没有指定`-parameters`参数, 则参数名默认为`arg0`、`arg1`、...、`argN`, 如果指定了该参数,
> 则在`spel`中使用实际的参数名即可,
> 如:`#source.name()`; 为了兼容这两种方式, CacheX提供了自己的命名方式`args0`、`args1`、...、`argsN`, 使用户可以不用区分是否开启编译参数.
