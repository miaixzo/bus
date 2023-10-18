#### 项目说明

## 功能①: Spring Boot JAR 加密

### 功能特性

* 无需侵入代码，只需要把编译好的JAR包通过工具加密即可。
* 完全内存解密，杜绝源码以及字节码泄露或反编译。
* 支持所有JDK内置加解密算法。
* 可选择需要加解密的字节码或其他资源文件，避免计算资源浪费。
* 运行加密项目时，无需求修改tomcat，spring等源代码
* 支持maven插件，添加插件后在打包过程中自动加密
* 支持加密WEB-INF/lib或BOOT-INF/lib下的依赖jar包

### 使用步骤

```java
// Spring-Boot Jar包加密
String password="forest";
        Key key=Builder.key(password);
        Boot.encrypt("/path/to/read/forest.jar","/path/to/save/enforest.jar",key);
```

```java
// 危险加密模式，即不需要输入密码即可启动的加密方式，这种方式META-INF/MANIFEST.MF中会保留密钥，请谨慎使用！
String password="forest";
        Key key=Builder.key(password);
        Boot.encrypt("/path/to/read/forest.jar","/path/to/save/enforest.jar",key,Builder.MODE_DANGER);
```

```java
// Spring-Boot Jar包解密
String password="forest";
        Key key=Builder.key(password);
        Boot.decrypt("/path/to/read/forest.jar","/path/to/save/deforest.jar",key);
```

```java
// Jar包加密
String password="forest";
        Key key=Builder.key(password);
        Jar.encrypt("/path/to/read/forest.jar","/path/to/save/enforest.jar",key);
```

```java
// 危险加密模式，即不需要输入密码即可启动的加密方式，这种方式META-INF/MANIFEST.MF中会保留密钥，请谨慎使用！
String password="forest";
        Key key=Kit.key(password);
        Jar.encrypt("/path/to/read/forest.jar","/path/to/save/enforest.jar",key,Builder.MODE_DANGER);
```

```java
// Jar包解密
String password="forest";
        Key key=Builder.key(password);
        Jar.decrypt("/path/to/read/forest.jar","/path/to/save/deforest.jar",key);
```

### 启动命令

```java
// 命令行运行JAR 然后在提示输入密码的时候输入密码后按回车即可正常启动
java-jar/path/to/enforest.jar
```

```java
// 也可以通过传参的方式直接启动，不太推荐这种方式，因为泄露的可能性更大！
java-jar/path/to/enforest.jar--xjar.password=password
```

```java
// 对于 nohup 或 javaw 这种后台启动方式，无法使用控制台来输入密码，推荐使用指定密钥文件的方式启动
nohup java-jar/path/to/enforest.jar--xjar.keyfile=/path/to/forest.key
```

### 参数说明

| 参数名称             | 参数含义 | 缺省值          | 说明                         |
|:-----------------|:-----|:-------------|:---------------------------|
| --xjar.password  | 密码   |
| --xjar.algorithm | 密钥算法 | AES          | 支持JDK所有内置算法，如AES / DES ... |
| --xjar.keysize   | 密钥长度 | 128          | 根据不同的算法选取不同的密钥长度。          |
| --xjar.ivsize    | 向量长度 | 128          | 根据不同的算法选取不同的向量长度。          |
| --xjar.keyfile   | 密钥文件 | ./forest.key | 密钥文件相对或绝对路径。               |

### 密钥文件

密钥文件采用properties的书写格式：

```
password: PASSWORD
algorithm: ALGORITHM
keysize: KEYSIZE
ivsize: IVSIZE
hold: HOLD
```

其中 algorithm/keysize/ivsize/hold 均有缺省值，当 hold 值不为 true | 1 | yes | y 时，密钥文件在读取后将自动删除。

| 参数名称      | 参数含义 | 缺省值   | 说明                         |
|:----------|:-----|:------|:---------------------------|
| password  | 密码   | 无     | 密码字符串                      |
| algorithm | 密钥算法 | AES   | 支持JDK所有内置算法，如AES / DES ... |
| keysize   | 密钥长度 | 128   | 根据不同的算法选取不同的密钥长度。          |
| ivsize    | 向量长度 | 128   | 根据不同的算法选取不同的向量长度。          |
| hold      | 是否保留 | false | 读取后是否保留密钥文件。               |

### 进阶用法

默认情况下，即没有提供过滤器的时候，将会加密所有资源其中也包括项目其他依赖模块以及第三方依赖的 JAR 包资源，
框架提供使用过滤器的方式来灵活指定需要加密的资源或排除不需要加密的资源。

* #### 硬编码方式

```java
// 假如项目所有类的包名都以 com.company.project 开头，那只加密自身项目的字节码即可采用以下方式。
Boot.encrypt(
        "/path/to/read/plaintext.jar",
        "/path/to/save/encrypted.jar",
        "forest",
        (entry)->{
        String name=entry.getName();
        String pkg="com/company/project/";
        return name.startsWith(pkg);
        }
        );
```

* #### 表达式方式

```java
// 1. 采用Ant表达式过滤器更简洁地来指定需要加密的资源。
Boot.encrypt(plaintext,encrypted,password,new XJarAntEntryFilter("com/company/project/**"));

        Boot.encrypt(plaintext,encrypted,password,new XJarAntEntryFilter("mapper/*Mapper.xml"));

        Boot.encrypt(plaintext,encrypted,password,new XJarAntEntryFilter("com/company/project/**/*API.class"));

// 2. 采用更精确的正则表达式过滤器。
        Boot.encrypt(plaintext,encrypted,password,new XJarRegexEntryFilter("com/company/project/(.+)"));

        Boot.encrypt(plaintext,encrypted,password,new XJarRegexEntryFilter("mapper/(.+)Mapper.xml"));

        Boot.encrypt(plaintext,encrypted,password,new XJarRegexEntryFilter("com/company/project/(.+)/(.+)API.class"));
```

* #### 混合方式

当过滤器的逻辑复杂或条件较多时可以将过滤器分成多个，并且使用 Builder 工具类提供的多个过滤器混合方法混合成一个，Builder 提供
“与” “或” “非” 三种逻辑运算的混合。

```java
// 1. 与运算，即所有过滤器都满足的情况下才满足，mix() 方法返回的是this，可以继续拼接。
XEntryFilter and=Builder.and()
        .mix(new AntEntryFilter("com/company/project/**"))
        .mix(new AntEntryFilter("*/**.class"));

        XEntryFilter all=Builder.all()
        .mix(new AntEntryFilter("com/company/project/**"))
        .mix(new AntEntryFilter("*/**.class"));

// 2. 或运算，即任意一个过滤器满足的情况下就满足，mix() 方法返回的是this，可以继续拼接。
        XEntryFilter or=Builder.or()
        .mix(new AntEntryFilter("com/company/project/**"))
        .mix(new AntEntryFilter("mapper/*Mapper.xml"));

        XEntryFilter any=Builder.any()
        .mix(new AntEntryFilter("com/company/project/**"))
        .mix(new AntEntryFilter("mapper/*Mapper.xml"));

// 3. 非运算，即除此之外都满足，该例子中即排除项目或其他模块和第三方依赖jar中的静态文件。
        XEntryFilter not=Builder.not(
        XKit.or()
        .mix(new AntEntryFilter("static/**"))
        .mix(new AntEntryFilter("META-INF/resources/**"))
        );
```

### 静态资源无法加载问题

由于静态文件被加密后文件体积变大，Spring Boot 会采用文件的大小作为 Content-Length 头返回给浏览器， 但实际上通过
加载解密后文件大小恢复了原本的大小，所以浏览器认为还没接收完导致一直等待服务端。
由此我们需要在加密时忽略静态文件的加密，实际上静态文件也没加密的必要，因为即便加密了用户在浏览器
查看源代码也是能看到完整的源码的。通常情况下静态文件都会放在 static/ 和 META-INF/resources/ 目录下，
我们只需要在加密时通过过滤器排除这些资源即可，可以采用以下的过滤器：

```java
XKit.not(
        XKit.or()
        .mix(new AntEntryFilter("static/**"))
        .mix(new AntEntryFilter("META-INF/resources/**"))
        );
```

#### 也可以通过Maven命令执行

```
mvn xjar:build -Dxjar.password=forest
mvn xjar:build -Dxjar.password=forest -Dxjar.targetDir=/directory/to/save/target.xjar
```

#### 但通常情况下是让XJar插件绑定到指定的phase中自动执行，这样就能在项目构建的时候自动构建出加密的包。

```
mvn clean package -Dxjar.password=forest
mvn clean install -Dxjar.password=forest -Dxjar.targetDir=/directory/to/save/target.xjar
```

### 参数说明

| 参数名称      | 命令参数名称           | 参数说明         | 参数类型     | 缺省值                             | 示例值                                                    |
|:----------|:-----------------|:-------------|:---------|:--------------------------------|:-------------------------------------------------------|
| password  | -Dxjar.password  | 密码字符串        | String   | 必须                              | 任意字符串，forest                                           |
| algorithm | -Dxjar.algorithm | 加密算法名称       | String   | AES                             | JDK内置加密算法，如：AES / DES                                  |
| keySize   | -Dxjar.keySize   | 密钥长度         | int      | 128                             | 根据加密算法而定，56，128，256                                    |
| ivSize    | -Dxjar.ivSize    | 密钥向量长度       | int      | 128                             | 根据加密算法而定，128                                           |
| mode      | -Dxjar.mode      | 加密模式         | int      | 0                               | 0：普通模式 1：危险模式(免密码启动)                                   |
| sourceDir | -Dxjar.sourceDir | 源jar所在目录     | File     | ${project.build.directory}      | 文件目录                                                   |
| sourceJar | -Dxjar.sourceJar | 源jar名称       | String   | ${project.build.finalName}.jar  | 文件名称                                                   |
| targetDir | -Dxjar.targetDir | 目标jar存放目录    | File     | ${project.build.directory}      | 文件目录                                                   |
| targetJar | -Dxjar.targetJar | 目标jar名称      | String   | ${project.build.finalName}.xjar | 文件名称                                                   |
| includes  | -Dxjar.includes  | 需要加密的资源路径表达式 | String[] | 无                               | com/company/project/** , mapper/*Mapper.xml , 支持Ant表达式 |
| excludes  | -Dxjar.excludes  | 无需加密的资源路径表达式 | String[] | 无                               | static/** , META-INF/resources/** , 支持Ant表达式           |

#### 注意：

当includes和excludes同时使用时即加密在includes的范围内且排除了excludes的资源

## 功能②: 生成对象类及方法

完美集成lombok，swagger的代码生成工具，让你不再为繁琐的注释和简单的接口实现而烦恼：

1. entity集成，格式校验，
2. swagger;
3. mapper自动加@Mapper，
4. service自动注释和依赖;
5. 控制器实现单表的增副改查，并集成swagger实现api文档

### 数据表结构样式

```sql
CREATE TABLE `hi_user`
(
    `id`       varchar(24) NOT NULL COMMENT 'ID',
    `name`     varchar(40)  DEFAULT NULL COMMENT '登录名',
    `password` varchar(100) DEFAULT NULL COMMENT '秘密',
    `nick`     varchar(50)  DEFAULT NULL COMMENT '昵称',
    `type`     int(10) DEFAULT NULL COMMENT '类型',
    `status`   int(10) DEFAULT NULL COMMENT '状态：-1失败，0等待,1成功',
    `created`  varchar(24)  DEFAULT NULL COMMENT '创建时间',
    `creator`  timestamp    DEFAULT NULL COMMENT '创建人',
    `modifier` varchar(24)  DEFAULT NULL COMMENT '修改人',
    `modified` bigint(50) DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB;
```

要求必须有表注释，要求必须有主键为id,所有字段必须有注释(便于生成java注释swagger等)

```java
     // 基础信息：项目名、作者、版本
public static final String PROJECT="bus-shade";
public static final String AUTHOR="Kimi Liu";
public static final String VERSION="1.0.0";
// 数据库连接信息：连接URL、用户名、秘密、数据库名
public static final String URL="jdbc:mysql://localhost:3000/database?useUnicode=true&characterEncoding=utf-8&autoReconnect=true&failOverReadOnly=false&useSSL=true&serverTimezone=UTC";
public static final String NAME="root";
public static final String PASS="root";
public static final String DATABASE="hi_test";
// 类信息：类名、对象名(一般是【类名】的首字母小些)、类说明、时间

public static final String TABLE="hi_user";
public static final String CLASSCOMMENT="用户信息";
public static final String AGILE=new Date().getTime()+"";
// 路径信息，分开路径方便聚合工程项目，微服务项目
public static final String ENTITY_URL="org.aoju.test.entity";
public static final String MAPPER_URL="org.aoju.test.mapper";
public static final String MAPPER_XML_URL="mapper";
public static final String SERVICE_URL="org.aoju.test.service";
public static final String SERVICE_IMPL_URL="org.aoju.test.service.impl";
public static final String CONTROLLER_URL="org.aoju.test.spring";
//是否启用Swagger
public static final String IS_SWAGGER="false";
//是否启用dbuuo
public static final String IS_DUBBO="true";
//是否驼峰命名
public static final boolean IS_HUMP=false;

public static void main(String[]args){
        TableEntity tb=new TableEntity(PROJECT,AUTHOR,VERSION,URL,NAME,PASS,DATABASE,TABLE,AGILE,ENTITY_URL,
        MAPPER_URL,MAPPER_XML_URL,SERVICE_URL,SERVICE_IMPL_URL,CONTROLLER_URL,IS_SWAGGER,IS_DUBBO,IS_HUMP);

        tb.setTable(TABLE);
        tb.setEntityName(NamingRules.getClassName(TABLE.replace("hi_","")));
        tb.setObjectName(NamingRules.changeToJavaFiled(TABLE.replace("hi_",""),true));
        tb.setEntityComment(CLASSCOMMENT);

        tb=TableEntity.get(tb);
        String path="/Volumes/Angela.Fang/Phoebe/Works/Aoju.org/bus/bus-xtest/src/main/";
        String javaUrl=path+"java/";
        String resourceUrl=path+"resources/";
        //开始生成文件
        String aa1=Builder.createEntity(javaUrl,tb).toString();
        String aa2=Builder.createMapper(javaUrl,tb).toString();
        String aa4=Builder.createService(javaUrl,tb).toString();
        String aa5=Builder.createServiceImpl(javaUrl,tb).toString();
        String aa6=Builder.createController(javaUrl,tb).toString();
        String aa3=Builder.createMapperXml(resourceUrl,tb).toString();

        System.out.println(aa1);
        System.out.println(aa2);
        System.out.println(aa4);
        System.out.println(aa5);
        System.out.println(aa6);
        System.out.println(aa3);
        }
``` 

## 功能③: 生成数据库文档

```java
  //数据源
        DruidDataSource druidDataSource=new DruidDataSource();
                druidDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
                druidDataSource.setUrl("jdbc:mysql://127.0.0.1:3306/database?useInformationSchema=true");
                druidDataSource.setUsername("root");
                druidDataSource.setPassword("password");

                //生成配置
                EngineConfig engineConfig=EngineConfig.builder()
                //生成文件路径
                .fileOutputDir("/data/")
                //打开目录
                .openOutputDir(true)
                //文件类型
                .fileType(EngineFileType.HTML)
                //生成模板实现
                .produceType(TemplateType.FREEMARKER)
                //自定义文件名称
                .fileName("自定义文件名称").build();

                //忽略表
                List<String> ignoreTableName=new ArrayList<>();
        ignoreTableName.add("test_user");
        ignoreTableName.add("test_group");
        //忽略表前缀
        List<String> ignorePrefix=new ArrayList<>();
        ignorePrefix.add("test_");
        //忽略表后缀
        List<String> ignoreSuffix=new ArrayList<>();
        ignoreSuffix.add("_test");
        ProcessConfig processConfig=ProcessConfig.builder()
        //指定生成逻辑、当存在指定表、指定表前缀、指定表后缀时，将生成指定表，其余表不生成、并跳过忽略表配置
        //根据名称指定表生成
        .designatedTableName(new ArrayList<>())
        //根据表前缀生成
        .designatedTablePrefix(new ArrayList<>())
        //根据表后缀生成
        .designatedTableSuffix(new ArrayList<>())
        //忽略表名
        .ignoreTableName(ignoreTableName)
        //忽略表前缀
        .ignoreTablePrefix(ignorePrefix)
        //忽略表后缀
        .ignoreTableSuffix(ignoreSuffix).build();
        //配置
        Config config=Config.builder()
        //版本
        .version("1.0.0")
        //描述
        .description("数据库设计文档生成")
        //数据源
        .dataSource(druidDataSource)
        //生成配置
        .engineConfig(engineConfig)
        //生成配置
        .produceConfig(processConfig)
        .build();
        //执行生成
        Builder.createFile(config);
```