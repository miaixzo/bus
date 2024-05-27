#### 项目说明

本工具是基于mybatis的插件机制编写的一套敏感数据加解密以及数据脱敏工具。

在使用时通过注解指定一个字段是需要加密的字段，该插件会在存储时自动加密存储。 而查询时会自动解密出明文在程序内部使用。
在使用时也可以通过注解指定一个字段是需要脱敏的字段，该插件会在入库时将字段脱敏存储。 内置了一些常用数据的脱敏处理方式。

## 设计目标

#### 对应用和使用者透明，业务逻辑无感知，通过配置集成，改动代码量小。

#### 加密算法可扩展。

## 实现原理

1，拦截mybatis的StatementHandler 对读写请求进行脱敏和字段的加密。 2，拦截mybatis的ResultSetHandler，对读请求的响应进行加密字段的解密赋值。

## 使用方式

1,编写加解密实现类以及配置mybatis的插件，下面在springboot场景下的一个配置案例。

```java
    /**
     * 插件配置
     */
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        List<String> imports = new ArrayList<>();
        imports.add(AspectjProxyPoint.class.getName());
        return imports.toArray(new String[0]);
    }
```

2，在vo类上添加功能注解使得插件生效：

```java

@Getter
@Setter
public class Entity {

    private String id;
    /**
     * 用户名
     */
    private String name;
    /**
     * 脱敏的用户名
     */
    @Field(Builder.Type.NAME)
    private String userNameSensitive;
    /**
     * 值的赋值不从数据库取，而是从name字段获得。
     */
    @Field(field = "name", type = Builder.Type.NAME)
    private String userNameOnlyDTO;
    /**
     * 身份证号-加密
     * 脱敏的身份证号
     */
    @Field(encrypt = true)
    private String idCard;
    /**
     * 脱敏的身份证号
     */
    @Field(field = "idCard", value = Builder.Type.CITIZENID)
    private String idcardSensitive;
    /**
     * 一个json串，需要脱敏
     * SensitiveJSONField标记json中需要脱敏的字段
     */
    @JSON({
            @Field(key = "idCard", type = Builder.Type.CITIZENID),
            @Field(key = "name", type = Builder.Type.NAME),
    })
    private String jsonStr;

    private int age;

    @Field(Builder.Type.EMAIL)
    private String email;
}
```

## 注解详解

#### @Sensitive

    标记在类上，声明此数据库映射的model对象开启数据加密和脱敏功能。

#### @Field(Builder.Type.NAME)

    标记在字段上，必须是字符串。
    声明此字段在入库或者修改时，会脱敏存储。
    Builder.Type是脱敏类型，详见脱敏类型章节。
    
    一般考虑如下场景。
    用户的手机号需要在数据库存储为加密的密文，为了查询方便，可能数据库也会有一个脱敏的手机号字段。
    那就可以这样定义两个字段：
    
    //在数据库加密存储的
    @Field(encrypt = true)
    private String phone;
    //在数据库脱敏存储的
    @Field(Builder.Type.MOBILE)
    private String phoneSensitive;
    
    而业务代码赋值时，可以赋值两次：
    
    ......
    user.setPhone("18233586969");
    user.setPhoneSensitive("18233586969");
    ......
    此时，数据库两个字段，一个会加密，一个会脱敏。
    在查询请求的响应结果集里，phone是明文，phoneSensitive是脱敏的。

#### @JSON

    标记在json字符串上，声明此json串在入库前会将json中指定的字段脱敏。
    
    例如：
    @JSON({
        @Field(key = "idCard",type = Builder.Type.CITIZENID),
        @Field(key = "name",type = Builder.Type.NAME)
       })
    private String jsonStr;
    
    如果jsonStr原文为
    {
      "age":18,
      "idCard":"130722188284646474",
      "name":"吴彦祖",
      "city":"北京"
    }
    则脱敏后为：
    {
      "age":18,
      "idCard":"130***********6474",
      "name":"吴**",
      "city":"北京"
    
    }
    使用场景：
    有时候数据库会存储一些第三方返回的json串，可能会包含敏感信息。
    业务里不需要用到敏感信息的明文，此时可以脱敏存储整个json串。

#### @Field(field = "userName",value = Builder.Type.NAME)

     此注解适用于如下场景：
     例如，数据库只存了username字段的加密信息，没有冗余脱敏展示的字段。
     我的响应类里希望将数据库的加密的某个字段映射到响应的两个属性上(一个解密的属性，一个脱敏的属性)就可以使用该注解。
     例如，dto里有如下字段：
     @EncryptField
     private String name
     
     @SensitiveBinded(bindField = "name",value = SensitiveType.NAME)
     private String userNameOnlyDTO;
     
     则当查询出结果时，userNameOnlyDTO会赋值为username解密后再脱敏的值。
     相当于数据库的一个字段的值以不同的形式映射到了对象的两个字段上。

## 脱敏类型

```java
    public enum Type {
    /**
     * 不脱敏
     */
    NONE,
    /**
     * 默认脱敏方式
     */
    DEFAUL,
    /**
     * 中文名
     */
    NAME,
    /**
     * 身份证号
     */
    CITIZENID,
    /**
     * 座机号
     */
    PHONE,
    /**
     * 手机号
     */
    MOBILE,
    /**
     * 地址
     */
    ADDRESS,
    /**
     * 电子邮件
     */
    EMAIL,
    /**
     * 银行卡
     */
    BANK_CARD,
    /**
     * 企业银行联号
     */
    CNAPS_CODE,
    /**
     * 支付签约协议号
     */
    PAY_SIGN_NO,
    /**
     * 密码
     */
    PASSWORD,
    /**
     * 普通号码
     */
    GENERIC
}
```

## 注意事项

#### 使用领域对象化的参数和响应

必须使用javabean类入参声明方式才能使得本插件生效。例如：

```java
 int insert(Entity entity);
```

使用如下的方式操作mybatis，则本插件无效。

```java
 int insert(Map map);
 int insert(String name,String idCard);
```

#### sql应该是预编译类型的

sql语句应该使用如#{userName}这种预编译的方式组织变量，不能使用'${userName}'这种方式