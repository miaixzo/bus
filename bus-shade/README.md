
## 介绍
完美集成lombok，swagger的代码生成工具，让你不再为繁琐的注释和简单的接口实现而烦恼：
1. entity集成，格式校验，
2. swagger; 
3. mapper自动加@Mapper，
4. service自动注释和依赖; 
5. 控制器实现单表的增副改查，并集成swagger实现api文档
 
## 快速开始

- 引入依赖
```xml
<dependency>
    <groupId>org.aoju</groupId>
    <artifactId>bus-shade</artifactId>
    <version>6.1.1</version>
</dependency>
```

## 数据表结构样式
```sql
CREATE TABLE `hi_user` (
  `id` varchar(24) NOT NULL COMMENT 'ID',
  `name` varchar(40) DEFAULT NULL COMMENT '登录名',
  `password` varchar(100) DEFAULT NULL COMMENT '秘密',
  `nick` varchar(50) DEFAULT NULL COMMENT '昵称',
  `type` int(10) DEFAULT NULL COMMENT '类型',
  `status` int(10) DEFAULT NULL COMMENT '状态：-1失败，0等待,1成功',
  `created` varchar(24) DEFAULT NULL COMMENT '创建时间',
  `creator` timestamp DEFAULT NULL COMMENT '创建人',
  `modifier` varchar(24) DEFAULT NULL COMMENT '修改人',
  `modified` bigint(50) DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB;
```
要求必须有表注释，要求必须有主键为id,所有字段必须有注释(便于生成java注释swagger等)

#### 功能①生成对象类及方法：
```java
	 // 基础信息：项目名、作者、版本
        public static final String PROJECT = "bus-shade";
        public static final String AUTHOR = "Kimi Liu";
        public static final String VERSION = "1.0.0";
        // 数据库连接信息：连接URL、用户名、秘密、数据库名
        public static final String URL = "jdbc:mysql://localhost:3000/database?useUnicode=true&characterEncoding=utf-8&autoReconnect=true&failOverReadOnly=false&useSSL=true&serverTimezone=UTC";
        public static final String NAME = "root";
        public static final String PASS = "root";
        public static final String DATABASE = "hi_test";
        // 类信息：类名、对象名(一般是【类名】的首字母小些)、类说明、时间
    
        public static final String TABLE = "hi_user";
        public static final String CLASSCOMMENT = "用户信息";
        public static final String AGILE = new Date().getTime() + "";
        // 路径信息，分开路径方便聚合工程项目，微服务项目
        public static final String ENTITY_URL = "org.aoju.test.entity";
        public static final String MAPPER_URL = "org.aoju.test.mapper";
        public static final String MAPPER_XML_URL = "mapper";
        public static final String SERVICE_URL = "org.aoju.test.service";
        public static final String SERVICE_IMPL_URL = "org.aoju.test.service.impl";
        public static final String CONTROLLER_URL = "org.aoju.test.spring";
        //是否是Swagger配置
        public static final String IS_SWAGGER = "false";
    
        public static void main(String[] args) {
            TableEntity tb = new TableEntity(PROJECT, AUTHOR, VERSION, URL, NAME, PASS, DATABASE, TABLE, AGILE, ENTITY_URL,
                    MAPPER_URL, MAPPER_XML_URL, SERVICE_URL, SERVICE_IMPL_URL, CONTROLLER_URL, IS_SWAGGER);
            tb.setTable(TABLE);
            tb.setEntityName(NamingRules.getClassName(TABLE.replace("hi_", "")));
            tb.setObjectName(NamingRules.changeToJavaFiled(TABLE.replace("hi_", ""),true));
            tb.setEntityComment(CLASSCOMMENT);
    
            tb = TableEntity.get(tb);
            String path = "/Volumes/Angela.Fang/Phoebe/Works/Aoju.org/bus/bus-xtest/src/main/";
            String javaUrl = path + "java/";
            String resourceUrl = path + "resources/";
            //开始生成文件
            String aa1 = Builder.createEntity(javaUrl, tb).toString();
            String aa2 = Builder.createMapper(javaUrl, tb).toString();
            String aa4 = Builder.createService(javaUrl, tb).toString();
            String aa5 = Builder.createServiceImpl(javaUrl, tb).toString();
            String aa6 = Builder.createController(javaUrl, tb).toString();
            String aa3 = Builder.createMapperXml(resourceUrl, tb).toString();
    
            System.out.println(aa1);
            System.out.println(aa2);
            System.out.println(aa4);
            System.out.println(aa5);
            System.out.println(aa6);
            System.out.println(aa3);
        }
``` 
#### 功能②生成数据库文档：
```java
  //数据源
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        druidDataSource.setUrl("jdbc:mysql://127.0.0.1:3306/database?useInformationSchema=true");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("password");

        //生成配置
        EngineConfig engineConfig = EngineConfig.builder()
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
        List<String> ignoreTableName = new ArrayList<>();
        ignoreTableName.add("test_user");
        ignoreTableName.add("test_group");
        //忽略表前缀
        List<String> ignorePrefix = new ArrayList<>();
        ignorePrefix.add("test_");
        //忽略表后缀
        List<String> ignoreSuffix = new ArrayList<>();
        ignoreSuffix.add("_test");
        ProcessConfig processConfig = ProcessConfig.builder()
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
        Config config = Config.builder()
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