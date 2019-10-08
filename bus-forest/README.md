### Spring Boot JAR 安全加密运行工具，同时支持的原生JAR。
### 基于对JAR包内资源的加密以及拓展ClassLoader来构建的一套程序加密启动，无需修改任何项目代码,动态解密运行的方案，避免源码泄露或反编译。
 
## 功能特性
* 无需侵入代码，只需要把编译好的JAR包通过工具加密即可。
* 完全内存解密，杜绝源码以及字节码泄露或反编译。
* 支持所有JDK内置加解密算法。
* 可选择需要加解密的字节码或其他资源文件，避免计算资源浪费。
* 运行加密项目时，无需求修改tomcat，spring等源代码
* 支持maven插件，添加插件后在打包过程中自动加密
* 支持加密WEB-INF/lib或BOOT-INF/lib下的依赖jar包

## 环境依赖
JDK 1.8+ +

## 使用步骤

```java
// Spring-Boot Jar包加密
String password = "forest";
Key key = Kit.key(password);
Boot.encrypt("/path/to/read/forest.jar", "/path/to/save/enforest.jar", key);
```

```java
// 危险加密模式，即不需要输入密码即可启动的加密方式，这种方式META-INF/MANIFEST.MF中会保留密钥，请谨慎使用！
String password = "forest";
Key key = Kit.key(password);
Boot.encrypt("/path/to/read/forest.jar", "/path/to/save/enforest.jar", key, Builder.MODE_DANGER);
```

```java
// Spring-Boot Jar包解密
String password = "forest";
Key key = Kit.key(password);
Boot.decrypt("/path/to/read/forest.jar", "/path/to/save/deforest.jar", key);
```

```java
// Jar包加密
String password = "forest";
Key key = Kit.key(password);
Jar.encrypt("/path/to/read/forest.jar", "/path/to/save/enforest.jar", key);
```

```java
// 危险加密模式，即不需要输入密码即可启动的加密方式，这种方式META-INF/MANIFEST.MF中会保留密钥，请谨慎使用！
String password = "forest";
Key key = Kit.key(password);
Jar.encrypt("/path/to/read/forest.jar", "/path/to/save/enforest.jar", key, XConstants.MODE_DANGER);
```

```java
// Jar包解密
String password = "forest";
Key key = Kit.key(password);
Jar.decrypt("/path/to/read/forest.jar", "/path/to/save/deforest.jar", key);
```

## 启动命令
```text
// 命令行运行JAR 然后在提示输入密码的时候输入密码后按回车即可正常启动
java -jar /path/to/enforest.jar
```
```text
// 也可以通过传参的方式直接启动，不太推荐这种方式，因为泄露的可能性更大！
java -jar /path/to/enforest.jar --xjar.password=password
```
```text
// 对于 nohup 或 javaw 这种后台启动方式，无法使用控制台来输入密码，推荐使用指定密钥文件的方式启动
nohup java -jar /path/to/enforest.jar --xjar.keyfile=/path/to/forest.key
```

## 参数说明
| 参数名称 | 参数含义 | 缺省值 | 说明 |
| :------- | :------- | :----- | :--- |
| --xjar.password |  密码 |
| --xjar.algorithm | 密钥算法 | AES | 支持JDK所有内置算法，如AES / DES ... |
| --xjar.keysize |   密钥长度 | 128 | 根据不同的算法选取不同的密钥长度。|
| --xjar.ivsize |    向量长度 | 128 | 根据不同的算法选取不同的向量长度。|
| --xjar.keyfile |   密钥文件 | ./forest.key | 密钥文件相对或绝对路径。|

## 密钥文件
密钥文件采用properties的书写格式：
```properties
password: PASSWORD
algorithm: ALGORITHM
keysize: KEYSIZE
ivsize: IVSIZE
hold: HOLD
```

其中 algorithm/keysize/ivsize/hold 均有缺省值，当 hold 值不为 true | 1 | yes | y 时，密钥文件在读取后将自动删除。

| 参数名称 | 参数含义 | 缺省值 | 说明 |
| :------- | :------- | :----- | :--- |
| password |  密码 | 无 | 密码字符串 |
| algorithm | 密钥算法 | AES | 支持JDK所有内置算法，如AES / DES ... |
| keysize |   密钥长度 | 128 | 根据不同的算法选取不同的密钥长度。|
| ivsize |    向量长度 | 128 | 根据不同的算法选取不同的向量长度。|
| hold | 是否保留 | false | 读取后是否保留密钥文件。|

## 进阶用法
默认情况下，即没有提供过滤器的时候，将会加密所有资源其中也包括项目其他依赖模块以及第三方依赖的 JAR 包资源，
框架提供使用过滤器的方式来灵活指定需要加密的资源或排除不需要加密的资源。

* #### 硬编码方式
```java
// 假如项目所有类的包名都以 com.company.project 开头，那只加密自身项目的字节码即可采用以下方式。
XBoot.encrypt(
        "/path/to/read/plaintext.jar", 
        "/path/to/save/encrypted.jar", 
        "forest", 
        (entry) -> {
            String name = entry.getName();
            String pkg = "com/company/project/";
            return name.startsWith(pkg);
        }
    );
```
* #### 表达式方式
```java
// 1. 采用Ant表达式过滤器更简洁地来指定需要加密的资源。
XBoot.encrypt(plaintext, encrypted, password, new XJarAntEntryFilter("com/company/project/**"));

XBoot.encrypt(plaintext, encrypted, password, new XJarAntEntryFilter("mapper/*Mapper.xml"));

XBoot.encrypt(plaintext, encrypted, password, new XJarAntEntryFilter("com/company/project/**/*API.class"));

// 2. 采用更精确的正则表达式过滤器。
XBoot.encrypt(plaintext, encrypted, password, new XJarRegexEntryFilter("com/company/project/(.+)"));

XBoot.encrypt(plaintext, encrypted, password, new XJarRegexEntryFilter("mapper/(.+)Mapper.xml"));

XBoot.encrypt(plaintext, encrypted, password, new XJarRegexEntryFilter("com/company/project/(.+)/(.+)API.class"));
```
* #### 混合方式
当过滤器的逻辑复杂或条件较多时可以将过滤器分成多个，并且使用 XKit 工具类提供的多个过滤器混合方法混合成一个，XKit 提供 “与” “或” “非” 三种逻辑运算的混合。
```java
// 1. 与运算，即所有过滤器都满足的情况下才满足，mix() 方法返回的是this，可以继续拼接。
XEntryFilter and = XKit.and()
    .mix(new XJarAntEntryFilter("com/company/project/**"))
    .mix(new XJarAntEntryFilter("*/**.class"));

XEntryFilter all = XKit.all()
    .mix(new XJarAntEntryFilter("com/company/project/**"))
    .mix(new XJarAntEntryFilter("*/**.class"));

// 2. 或运算，即任意一个过滤器满足的情况下就满足，mix() 方法返回的是this，可以继续拼接。
XEntryFilter or = XKit.or()
    .mix(new XJarAntEntryFilter("com/company/project/**"))
    .mix(new XJarAntEntryFilter("mapper/*Mapper.xml"));

XEntryFilter any = XKit.any()
    .mix(new XJarAntEntryFilter("com/company/project/**"))
    .mix(new XJarAntEntryFilter("mapper/*Mapper.xml"));

// 3. 非运算，即除此之外都满足，该例子中即排除项目或其他模块和第三方依赖jar中的静态文件。
XEntryFilter not  = XKit.not(
        XKit.or()
            .mix(new XJarAntEntryFilter("static/**"))
            .mix(new XJarAntEntryFilter("META-INF/resources/**"))
);
```

## 静态资源无法加载问题
由于静态文件被加密后文件体积变大，Spring Boot 会采用文件的大小作为
Content-Length 头返回给浏览器， 但实际上通过 XJar
加载解密后文件大小恢复了原本的大小，所以浏览器认为还没接收完导致一直等待服务端。
由此我们需要在加密时忽略静态文件的加密，实际上静态文件也没加密的必要，因为即便加密了用户在浏览器
查看源代码也是能看到完整的源码的。通常情况下静态文件都会放在 static/ 和
META-INF/resources/ 目录下，
我们只需要在加密时通过过滤器排除这些资源即可，可以采用以下的过滤器：
```java
XKit.not(
        XKit.or()
            .mix(new XJarAntEntryFilter("static/**"))
            .mix(new XJarAntEntryFilter("META-INF/resources/**"))
);
```
或通过插件配置排除
```xml
<plugin>
    <groupId>com.github.core-lib</groupId>
    <artifactId>forest-maven-plugin</artifactId>
    <version>1.0.5</version>
    <executions>
        <execution>
            <goals>
                <goal>build</goal>
            </goals>
            <phase>package</phase>
            <!-- 或使用
            <phase>install</phase>
            -->
            <configuration>
                <password>forest</password>
                <excludes>
                    <exclude>static/**</exclude>
                    <exclude>META-INF/resources/**</exclude>
                </excludes>
            </configuration>
        </execution>
    </executions>
</plugin>
```

#### 对于Spring Boot 项目或模块，该插件要后于 spring-boot-maven-plugin 插件执行，有两种方式：
* 将插件放置于 spring-boot-maven-plugin 的后面，因为其插件的默认 phase 也是 package
* 将插件的 phase 设置为 install（默认值为：package），打包命令采用 mvn clean install
```xml
<project>
    <build>
        <plugins>
            <plugin>
                <groupId>org.aoju</groupId>
                <artifactId>forest-maven-plugin</artifactId>
                <version>1.0.5</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>build</goal>
                        </goals>
                        <phase>package</phase>
                        <!-- 或使用
                        <phase>install</phase>
                        -->
                        <configuration>
                            <password>forest</password>
                            <includes>
                                <include>com/company/project/**</include>
                                <include>mapper/*Mapper.xml</include>
                            </includes>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>

```
#### 也可以通过Maven命令执行
```text
mvn xjar:build -Dxjar.password=forest
mvn xjar:build -Dxjar.password=forest -Dxjar.targetDir=/directory/to/save/target.xjar
```

#### 但通常情况下是让XJar插件绑定到指定的phase中自动执行，这样就能在项目构建的时候自动构建出加密的包。
```text
mvn clean package -Dxjar.password=forest
mvn clean install -Dxjar.password=forest -Dxjar.targetDir=/directory/to/save/target.xjar
```

## 参数说明
| 参数名称 | 命令参数名称 | 参数说明 | 参数类型 | 缺省值 | 示例值 |
| :------ | :----------- | :------ | :------ | :----- | :----- |
| password | -Dxjar.password | 密码字符串 | String | 必须 | 任意字符串，forest |
| algorithm | -Dxjar.algorithm | 加密算法名称 | String | AES | JDK内置加密算法，如：AES / DES |
| keySize | -Dxjar.keySize | 密钥长度 | int | 128 | 根据加密算法而定，56，128，256 |
| ivSize | -Dxjar.ivSize | 密钥向量长度 | int | 128 | 根据加密算法而定，128 |
| mode | -Dxjar.mode | 加密模式 | int | 0 | 0：普通模式 1：危险模式（免密码启动）|
| sourceDir | -Dxjar.sourceDir | 源jar所在目录 | File | ${project.build.directory} | 文件目录 |
| sourceJar | -Dxjar.sourceJar | 源jar名称 | String | ${project.build.finalName}.jar | 文件名称 |
| targetDir | -Dxjar.targetDir | 目标jar存放目录 | File | ${project.build.directory} | 文件目录 |
| targetJar | -Dxjar.targetJar | 目标jar名称 | String | ${project.build.finalName}.xjar | 文件名称 |
| includes | -Dxjar.includes | 需要加密的资源路径表达式 | String[] | 无 | com/company/project/** , mapper/*Mapper.xml , 支持Ant表达式 |
| excludes | -Dxjar.excludes | 无需加密的资源路径表达式 | String[] | 无 | static/** , META-INF/resources/** , 支持Ant表达式 |

#### 注意：
当includes和excludes同时使用时即加密在includes的范围内且排除了excludes的资源
