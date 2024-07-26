#### 项目说明

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

9. 启用第三方授权登陆 注解形式 `@EnableThirdAuth`

10. 启用OSS存储 注解形式 `@EnableStorage`

11. 启用Validate 注解形式 `@EnableValidate`

12. 启用 Elasticsearch 注解形式 `@EnableElastic`

+ 使用步骤
    + 第一步: 在 application.yml 中配置 elasticsearch 集群信息，基本如下:

      ```yaml
      #    ElasticSearch 配置
      extend:
        elastic:
          hosts: 192.168.100.126:29200
          schema: http
          connect-timeout: 60000
          socket-timeout: 60000
          connection-request-timeout: 60000
          max-connect-total: 2000
          max-connect-per-route: 500
      ```
    + 第二步: 在业务 Service 实现类中注入 RestHighLevelClient
      ```java
          @Resource
          private RestHighLevelClient restHighLevelClient;
      ```
    + 第三步: 在程序主入口 Application 加入注解:
      ```java
          @EnableElastic
      ```

13. ...
