#### 项目说明
文件存储组件,支持阿里云、七牛集成，提供了工厂模式和spring注入两种集成方式。
#### 添加依赖
```
<dependency>
	<groupId>com.qiniu</groupId>
	<artifactId>qiniu-java-sdk</artifactId>
	<version>7.2.2</version>
</dependency>

<dependency>
    <groupId>com.aliyun.oss</groupId>
    <artifactId>aliyun-sdk-oss</artifactId>
    <version>2.8.3</version>
</dependency>
```
#### 工厂模式
##### 配置文件,xx.yml

# 服务提供者可选值（qiniu，aliyun）
```
storage.oss:
  - provider: qiniu
    bucketName: oss_qiniu
    accessKey: com.mysql.cj.jdbc.Driver
    secretKey: 0193672ebc14dd8be9719e88f2c0ad36a000b84243897ca6f60ec70f7410977f3221af3961e96a4def51d91d91b0831e56903317d79da792061af529a64fa4b1c4d2f84be6357cf546fdd8055ad82ae3c3e6f0eb25e577e56932c8689505cd20c783dd50c8d50cf0ba1d0ac2d7e1f68e
    prefix: 8b2d76ea487bfba5ad30bd56467ac8fc 
    privated: false
  - provider: aliyun
    bucketName: oss_aliyun
    accessKey: com.mysql.cj.jdbc.Driver
    secretKey: 0193672ebc14dd8be9719e88f2c0ad36a000b84243897ca6f60ec70f7410977f3221af3961e96a4def51d91d91b0831e56903317d79da792061af529a64fa4b1c4d2f84be6357cf546fdd8055ad82ae3c3e6f0eb25e577e56932c8689505cd20c783dd50c8d50cf0ba1d0ac2d7e1f68e
    prefix: 8b2d76ea487bfba5ad30bd56467ac8fc 
    privated: false
 
......
```

参数说明：
- bucket：多个组用“,”隔开，对应的`bucketName`

##### 用法

```
StorageProvider provider = new AliyunossProvider(xxxx);
String url = provider.upload("test", null, new File("/Users/vakinge/logo.gif"));
```
##### 用法

```
@Autowired
StorageProvider provider;

String url = provider.upload("test", null, new File("/Users/vakinge/logo.gif"));
```
