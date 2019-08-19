## 使用说明

启动类中增加注解或者POM文件中增加依赖即可。
```
    @EnableXssFilter
    @EnableCorsFilter
    @EnableMapper
    @EnableDubbo
    @EnableSwagger
    @EnableCrypto
    @EnableValidate
    @SpringBootApplication(scanBasePackages = "org.aoju.**")
```
    
1. 启用Xss 注解形式 `@EnableXssFilter` 或者POM形式：
    ``` 
        <dependency>
           <groupId>org.aoju</groupId>
           <artifactId>bus-boot-xss-starter</artifactId>
           <version>3.1.0</version> 
        </dependency> 
    ```
    
2. 启用Cors 注解形式 `@EnableCorsFilter` 或者POM如下：
    ``` 
        <dependency>
            <groupId>org.aoju</groupId>
            <artifactId>bus-boot-cors-starter</artifactId>
            <version>3.1.0</version>
        </dependency> 
    ```
3. 启用Validate 注解形式 `@EnableValidate` 或者POM如下：
    ``` 
        <dependency>
            <groupId>org.aoju</groupId>
            <artifactId>bus-boot-validate-starter</artifactId>
            <version>3.1.0</version>
        </dependency>
    ```
4. 启用Mapper 注解形式 `@EnableMapper`(同时会启用Druid) 或者POM如下：
    ``` 
        <dependency>
            <groupId>org.aoju</groupId>
            <artifactId>bus-boot-mapper-starter</artifactId>
            <version>3.1.0</version>
        </dependency>
    ```
5. 启用Swagger 注解形式 `@EnableSwagger` 或者POM如下：
    ``` 
        <dependency>
            <groupId>org.aoju</groupId>
            <artifactId>bus-boot-swagger-starter</artifactId>
            <version>3.1.0</version>
        </dependency>
    ```
6. 启用Druid 注解形式 `@EnableDruid` 或者POM如下：
    ``` 
        <dependency>
            <groupId>org.aoju</groupId>
            <artifactId>bus-boot-druid-starter</artifactId>
            <version>3.1.0</version>
        </dependency>
    ```
7. 启用Dubbo 注解形式 `@EnableDubbo` 或者POM如下：
    ``` 
        <dependency>
            <groupId>org.aoju</groupId>
            <artifactId>bus-boot-dubbo-starter</artifactId>
            <version>3.1.0</version>
        </dependency>
    ```
