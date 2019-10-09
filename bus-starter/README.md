## 使用说明

启动类中增加注解或者POM文件中增加依赖即可。
```
    @EnableCorsFilter
    @EnableOnceFilter
    @EnableMapper
    @EnableDubbo
    @EnableSwagger
    @EnableCrypto
    @EnableValidate
    @SpringBootApplication
```
  
1. 启用Cors 注解形式 `@EnableCorsFilter` 或者POM如下：
  ``` 
      <dependency>
          <groupId>org.aoju</groupId>
          <artifactId>bus-boot-cors-starter</artifactId>
          <version>3.6.8</version>
      </dependency> 
  ```  

2. 启用Druid 注解形式 `@EnableDruid` 或者POM如下：
    ``` 
        <dependency>
            <groupId>org.aoju</groupId>
            <artifactId>bus-boot-druid-starter</artifactId>
            <version>3.6.8</version>
        </dependency>
    ```

3. 启用Druid监控 注解形式 `@EnableDruids`同时会自动启用`@EnableDruid` 或者POM如下：
    ``` 
        <dependency>
            <groupId>org.aoju</groupId>
            <artifactId>bus-boot-druid-starter</artifactId>
            <version>3.6.8</version>
        </dependency>
    ```
4. 启用Dubbo 注解形式 `@EnableDubbo` 或者POM如下：
    ``` 
        <dependency>
            <groupId>org.aoju</groupId>
            <artifactId>bus-boot-dubbo-starter</artifactId>
            <version>3.6.8</version>
        </dependency>
    ```      
   
5. 启用国际化 注解形式 `@EnableI18n` 或者POM如下：
    ``` 
        <dependency>
            <groupId>org.aoju</groupId>
            <artifactId>bus-boot-i18n-starter</artifactId>
            <version>3.6.8</version>
        </dependency>
    ```
     
6. 启用Mapper 注解形式 `@EnableMapper`(同时会启用Druid) 或者POM如下：
    ``` 
        <dependency>
            <groupId>org.aoju</groupId>
            <artifactId>bus-boot-mapper-starter</artifactId>
            <version>3.6.8</version>
        </dependency>
    ```
      
7. 启用Wrapper 注解形式 `@EnableOnceFilter` 或者POM形式：
    ``` 
        <dependency>
           <groupId>org.aoju</groupId>
           <artifactId>bus-boot-wrapper-starter</artifactId>
           <version>3.6.8</version> 
        </dependency> 
    ```
 
8. 启用数据安全脱敏/加解密 注解形式 `@EnableSensitive` 或者POM如下：
    ``` 
        <dependency>
            <groupId>org.aoju</groupId>
            <artifactId>bus-boot-sensitive-starter</artifactId>
            <version>3.6.8</version>
        </dependency>
    ```

8. 启用第三方授权登陆 注解形式 `@EnableThirdAuth` 或者POM如下：
   ``` 
       <dependency>
           <groupId>org.aoju</groupId>
           <artifactId>bus-boot-oauth-starter</artifactId>
           <version>3.6.8</version>
       </dependency>
   ```

9. 启用OSS存储 注解形式 `@EnableStorage` 或者POM如下：
    ``` 
        <dependency>
            <groupId>org.aoju</groupId>
            <artifactId>bus-boot-storage-starter</artifactId>
            <version>3.6.8</version>
        </dependency>
    ```
   
9. 启用Swagger 注解形式 `@EnableSwagger` 或者POM如下：
    ``` 
        <dependency>
            <groupId>org.aoju</groupId>
            <artifactId>bus-boot-swagger-starter</artifactId>
            <version>3.6.8</version>
        </dependency>
    ```
      
10. 启用Validate 注解形式 `@EnableValidate` 或者POM如下：
    ``` 
        <dependency>
            <groupId>org.aoju</groupId>
            <artifactId>bus-boot-validate-starter</artifactId>
            <version>3.6.8</version>
        </dependency>
    ```

