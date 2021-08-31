## 使用说明

启动类中增加注解或者POM文件中增加依赖即可。

```java
@EnableCors
@EnableWrapper
@EnableMapper
@EnableDubbo
@EnableCrypto
@EnableValidate
    ...
```

1. 启用Cors 注解形式 `@EnableCors`

2. 启用Druid 注解形式 `@EnableDruid`

3. 启用Druid监控 注解形式 `@EnableDruids`同时会自动启用`@EnableDruid`

4. 启用Dubbo 注解形式 `@EnableDubbo`

5. 启用国际化 注解形式 `@EnableI18n`

6. 启用Mapper 注解形式 `@EnableMapper`(同时会启用Druid)

7. 启用Wrapper 注解形式 `@EnableWrapper`

8. 启用数据安全脱敏/加解密 注解形式 `@EnableSensitive`

8. 启用第三方授权登陆 注解形式 `@EnableThirdAuth`

9. 启用OSS存储 注解形式 `@EnableStorage`

10. 启用Validate 注解形式 `@EnableValidate`