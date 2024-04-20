#### 项目说明

一个校验器框架，提供注解校验方法参数和对象属性的功能，在方法运行前，拦截方法并执行参数校验，如果校验失败可以抛出自定义异常和信息；便于拓展自定义校验器。    
开发时，参考了Hibernate-Validator 5.x，但是没有做到兼容，因为JSR-303提供的注解的方法太少，不方便拓展，所以写了这个框架。

## 运行环境

要求Java 17+

使用说明：

1. 在spring容器中定义`AspectjProxyPoint`， 这是校验器的切面拦截功能，会默认拦截所有的标记有`@Valid`的方法或类上面有`@Valid`注解的内部所有方法。

1) `@Valid` value 启用部分属性校验
2) `@Valid` skip 忽略部分属性校验
3) `@Valid` inside 开启内部校验，即对象属性校验，同`@Inside`注解

2. 现在可以使用框架中自带的校验器注解。
    * 在任意方法上标记`@Valid`注解，表明校验该方法运行时的入参。
    * 在任意类上标记`@Valid`注解，表明校验该类中所有方法运行时的入参。
    * 在任意类上标记`@Valid`注解，表明校验该类中所有方法运行时的入参。
    * `@Valid`注解，默认全部校验，当标记value时按照标记校验，标记inside时取消或者开启内部校验。
    * `@Inside`注解，当标记在类中时,优先级高于@Valid` 中的inside

```java
        @Controller
@RequestMapping("user")
public UserController{


@ResponseBody
public User create(@Valid(value = {"name"}) @NotNull User user){
        ...
        }
        }
```

3. 在非spring环境使用校验器功能

``` java
        //标记校验对象内部字段的注解，否则默认不去校验对象内部字段
        @Inside
        class User {
            @IntRange(max = 1880, min = 5)
            private int age;
        
            @NotBlank
            private String name;
        
            @Length(min = 10)
            private List<String> list;
        }
        
        //校验对象 
        public void test() {
            User user = new User();
            user.setAge(0);
            user.setList(Lists.newArrayList("12"));
            user.setName("asdf");
    
            Context context = Context.newInstance();
            //fast=false表示即使校验过程中，存在校验失败，将校验结果仍然收集到校验收集器中，而不是立即抛出异常.
            context.setFast(false);   
    
            Validated validated = new Validated(user, context);
            Collector check = validated.access();
            System.out.println(check.getResult());
        }
```

5. 框架内部可用校验器注解
    * 校验器注解的具体使用方法，可以阅读源代码的api注释文档。
   * 注意该框架的校验器的包名为`org.miaixz.bus.validate.annotation`
    * 下列校验器注解，默认都**不会**产生校验对象内部字段的功能。如需校验对象内部字段，请使用后文提到的`@Inside`注解。

```text
        @Blank: 字符串必须为空
        @Each: 遍历数组或列表、Map执行校验器
        @Equals: toString方法的返回值必须和指定的字符串相同
        @False: boolean, Boolean类型的值必须为false
        @In: 对象必须包含在指定的数组中
        @InEnum: 对象必须在指定的枚举类型中
        @IntRange: 数字必须在指定的范围
        @Length: 字符串长度或数组、列表长度在指定范围
        @MultiValidate: 注入指定的一个或多个校验器校验对象
        @NotBlank: 字符串不能为null或为空字符串，多个空白字符也不允许
        @NotIn: 对象不能在指定的数组内
        @NotNull: 对象不能为null
        @Null: 对象必须为null
        @Reflect: 通过反射执行一个指定的方法，其中被校验对象作为入参，最后指定校验器校验方法的运行结果
        @Regex: 正则表达式校验字符串
        @True: boolean, Boolean类型的值必须为true
```

* 特殊的元数据注解

```text
        @Valid: 标记在类上或方法上，表示校验切面拦截位置
        @Group: 标记在被校验方法上或入参上，表示当前的校验组
        @Inside: 标记在被校验方法的入参或POJO字段上，表示校验对象内部字段
        @Array: 在自定义注解时，标记在自定义校验注解上，表示被校验对象是数组或列表时，仅遍历校验内部元素。
        @Filler: 在自定义注解时，标记在自定义注解的内部方法上，表示该方法的值可以在emsg插值中使用。
        @Complex： 在自定义注解时，标记在自定义注解上，表示该注解使用的具体的校验执行器。
        @ValidEx: 校验异常注解，校验失败时将ValidateException替换为指定的异常并抛出
```

6. 自定义校验器及注解：
    1. 自定义一个注解，必须有errcode、errmsg、group、field四个方法。注解上要有`@Complex`标明注解在运行时使用的校验器，示例如下：

```java

@Documented
@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Complex(value = Builder._BLANK, clazz = BlankStrategy.class)
public @interface Blank {

    /**
     * 默认使用的异常码
     */
    int errcode() default Builder.DEFAULT_ERRCODE;

    /**
     * 默认使用的异常信息
     */
    String errmsg() default "${field}字符串必须为空";

    /**
     * 校验器组
     */
    String[] group() default {};

    /**
     * 被校验字段名称
     */
    String field() default Builder.DEFAULT_FIELD;
}
```

    2. 实现一个校验器，实现`Complex`接口， 并且将其注入Spring容器中。

```java
        public class BlankStrategy implements Complex<String, Blank> {
    @Override
    public boolean on(String object, Blank annotation, ValidateContext context) {
        return validate(object, context);
    }
}
```

    3. 在需要进行参数校验的地方使用该注解

```java
        @Valid //标记注解，启用验证器AOP拦截
public append(@Blank String text){
        ...
        }
```