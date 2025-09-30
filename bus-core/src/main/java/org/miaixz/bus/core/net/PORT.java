/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.net;

/**
 * 服务端口号常量类，深度覆盖Java微服务、中间件、大数据、ESB及APM等生态。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PORT {

    /**
     * <b>FTP Data</b>: 20
     * <p>
     * 文件传输协议的数据端口 (主动模式)。
     * </p>
     */
    public static final int _20 = 20;

    /**
     * <b>FTP Command</b>: 21
     * <p>
     * 文件传输协议的命令控制端口。
     * </p>
     */
    public static final int _21 = 21;

    /**
     * <b>SSH</b>: 22
     * <p>
     * 安全壳协议，用于安全的远程登录和命令执行。
     * </p>
     */
    public static final int _22 = 22;

    /**
     * <b>SMTP</b>: 25
     * <p>
     * 简单邮件传输协议，用于发送电子邮件。
     * </p>
     */
    public static final int _25 = 25;

    /**
     * <b>DNS</b>: 53
     * <p>
     * 域名系统，用于域名与IP地址的相互解析。
     * </p>
     */
    public static final int _53 = 53;

    /**
     * <b>HTTP</b>: 80
     * <p>
     * 超文本传输协议，万维网的基础。
     * </p>
     */
    public static final int _80 = 80;

    /**
     * <b>POP3</b>: 110
     * <p>
     * 邮局协议版本3，用于接收电子邮件。
     * </p>
     */
    public static final int _110 = 110;

    /**
     * <b>IMAP</b>: 143
     * <p>
     * 互联网消息访问协议，用于接收电子邮件。
     * </p>
     */
    public static final int _143 = 143;

    /**
     * <b>HTTPS</b>: 443
     * <p>
     * 安全的超文本传输协议，经过TLS/SSL加密。
     * </p>
     */
    public static final int _443 = 443;

    /**
     * <b>Java RMI Registry</b>: 1099
     * <p>
     * Java远程方法调用(RMI)注册表的默认端口。
     * </p>
     */
    public static final int _1099 = 1099;

    /**
     * <b>Microsoft SQL Server</b>: 1433
     * <p>
     * 微软SQL Server数据库的默认端口。
     * </p>
     */
    public static final int _1433 = 1433;

    /**
     * <b>Oracle DB</b>: 1521
     * <p>
     * Oracle数据库监听器的默认端口。
     * </p>
     */
    public static final int _1521 = 1521;

    /**
     * <b>Zookeeper</b>: 2181
     * <p>
     * Apache Zookeeper协调服务的客户端端口。
     * </p>
     */
    public static final int _2181 = 2181;

    /**
     * <b>MySQL / MariaDB</b>: 3306
     * <p>
     * MySQL 和 MariaDB 关系型数据库的默认端口。
     * </p>
     */
    public static final int _3306 = 3306;

    /**
     * <b>Selenium Grid 4</b>: 4444
     * <p>
     * Selenium Grid 4 的Router端口，所有WebDriver请求的入口。
     * </p>
     */
    public static final int _4444 = 4444;

    /**
     * <b>GlassFish Admin</b>: 4848
     * <p>
     * GlassFish应用服务器管理控制台的默认端口。
     * </p>
     */
    public static final int _4848 = 4848;

    /**
     * <b>JPDA Remote Debug</b>: 5005
     * <p>
     * Java平台远程调试(Remote Debug)的通用约定端口。
     * </p>
     */
    public static final int _5005 = 5005;

    /**
     * <b>Kibana</b>: 5601
     * <p>
     * Kibana的默认端口，用于Elasticsearch的数据可视化。
     * </p>
     */
    public static final int _5601 = 5601;

    /**
     * <b>RabbitMQ AMQP</b>: 5672
     * <p>
     * RabbitMQ消息队列的AMQP协议端口。
     * </p>
     */
    public static final int _5672 = 5672;

    /**
     * <b>Hazelcast</b>: 5701
     * <p>
     * Hazelcast 分布式内存计算平台的集群通信端口。
     * </p>
     */
    public static final int _5701 = 5701;

    /**
     * <b>Apache Flink JobManager</b>: 6123
     * <p>
     * Apache Flink 分布式计算框架 JobManager 的RPC端口。
     * </p>
     */
    public static final int _6123 = 6123;

    /**
     * <b>Apache Pulsar Broker</b>: 6650
     * <p>
     * Apache Pulsar Broker的二进制协议端口。
     * </p>
     */
    public static final int _6650 = 6650;

    /**
     * <b>Cassandra Inter-node (Encrypted)</b>: 7001
     * <p>
     * Cassandra 集群节点间加密通信端口。
     * </p>
     */
    public static final int _7001 = 7001;

    /**
     * <b>Cassandra JMX</b>: 7199
     * <p>
     * Cassandra 数据库JMX监控的约定俗成端口。
     * </p>
     */
    public static final int _7199 = 7199;

    /**
     * <b>Neo4j Bolt</b>: 7687
     * <p>
     * Neo4j 图数据库的Bolt协议端口。
     * </p>
     */
    public static final int _7687 = 7687;

    /**
     * <b>Mule ESB Agent</b>: 7777
     * <p>
     * Mule ESB 运行时Agent的默认端口。
     * </p>
     */
    public static final int _7777 = 7777;

    /**
     * <b>Tomcat AJP</b>: 8009
     * <p>
     * Tomcat AJP连接器的默认端口。
     * </p>
     */
    public static final int _8009 = 8009;

    /**
     * <b>通用Web端口</b>: 8080
     * <p>
     * 极其通用的端口，用于JBoss/WildFly, Tomcat, Spring Boot, Jenkins, Spark UI, Pulsar, Apollo, Keycloak, Nginx反代等。
     * </p>
     */
    public static final int _8080 = 8080;

    /**
     * <b>通用Web备用/管理端口</b>: 8081
     * <p>
     * 高度复用的端口，用于Flink UI, Kafka Schema Registry, Nexus, Artifactory, Mule Agent, Druid UI, Spark Worker UI等。
     * </p>
     */
    public static final int _8081 = 8081;

    /**
     * <b>Apache Druid Broker</b>: 8082
     * <p>
     * Apache Druid 实时分析数据库的Broker服务端口。
     * </p>
     */
    public static final int _8082 = 8082;

    /**
     * <b>Apache Kafka Connect</b>: 8083
     * <p>
     * Kafka Connect分布式模式的REST API端口。
     * </p>
     */
    public static final int _8083 = 8083;

    /**
     * <b>Ctrip Apollo Portal</b>: 8070
     * <p>
     * 携程Apollo分布式配置中心Portal服务的端口。
     * </p>
     */
    public static final int _8070 = 8070;

    /**
     * <b>Ctrip Apollo Admin Service</b>: 8090
     * <p>
     * 携程Apollo分布式配置中心Admin Service的端口。
     * </p>
     */
    public static final int _8090 = 8090;

    /**
     * <b>Apache ActiveMQ Web Console</b>: 8161
     * <p>
     * Apache ActiveMQ的管理后台Web UI端口。
     * </p>
     */
    public static final int _8161 = 8161;

    /**
     * <b>Glowroot Central</b>: 8181
     * <p>
     * Glowroot APM 中心收集器的UI和agent通信端口 (与 _8081 相似)。
     * </p>
     */
    public static final int _8181 = 8181;

    /**
     * <b>WSO2 EI HTTPS</b>: 8243
     * <p>
     * WSO2 Enterprise Integrator 的HTTPS PassThrough Transport端口。
     * </p>
     */
    public static final int _8243 = 8243;

    /**
     * <b>WSO2 EI HTTP</b>: 8280
     * <p>
     * WSO2 Enterprise Integrator 的HTTP PassThrough Transport端口。
     * </p>
     */
    public static final int _8280 = 8280;

    /**
     * <b>HTTPS Alternative</b>: 8443
     * <p>
     * 常用于备用HTTPS或Java应用服务器的SSL端口。
     * </p>
     */
    public static final int _8443 = 8443;

    /**
     * <b>Netflix Eureka</b>: 8761
     * <p>
     * Spring Cloud Netflix Eureka 服务发现服务器的默认端口。
     * </p>
     */
    public static final int _8761 = 8761;

    /**
     * <b>Bus Vortex</b>: 8765
     * <p>
     * Spring Webflux 服务启动默认端口。
     * </p>
     */
    public static final int _8765 = 8765;

    /**
     * <b>Alibaba Nacos</b>: 8848
     * <p>
     * Nacos 服务发现和配置管理的主服务器端口。
     * </p>
     */
    public static final int _8848 = 8848;

    /**
     * <b>Apache Druid Router</b>: 8888
     * <p>
     * Apache Druid 的Router进程端口，作为统一查询入口。
     * </p>
     */
    public static final int _8888 = 8888;

    /**
     * <b>Apache Solr</b>: 8983
     * <p>
     * Apache Solr 搜索引擎的默认HTTP端口。
     * </p>
     */
    public static final int _8983 = 8983;

    /**
     * <b>SonarQube</b>: 9000
     * <p>
     * SonarQube代码质量分析平台的Web UI默认端口。
     * </p>
     */
    public static final int _9000 = 9000;

    /**
     * <b>Cassandra Client</b>: 9042
     * <p>
     * Apache Cassandra 分布式NoSQL数据库的客户端端口。
     * </p>
     */
    public static final int _9042 = 9042;

    /**
     * <b>Apache Thrift (Convention)</b>: 9090
     * <p>
     * Apache Thrift RPC框架教程中常用的约定端口。
     * </p>
     */
    public static final int _9090 = 9090;

    /**
     * <b>Apache Kafka</b>: 9092
     * <p>
     * Apache Kafka分布式流处理平台的Broker默认端口。
     * </p>
     */
    public static final int _9092 = 9092;

    /**
     * <b>Elasticsearch HTTP</b>: 9200
     * <p>
     * Elasticsearch搜索引擎的HTTP REST API端口。
     * </p>
     */
    public static final int _9200 = 9200;

    /**
     * <b>Elasticsearch Transport</b>: 9300
     * <p>
     * Elasticsearch集群内部节点间通信的端口。
     * </p>
     */
    public static final int _9300 = 9300;

    /**
     * <b>WSO2 EI Management Console</b>: 9443
     * <p>
     * WSO2 Enterprise Integrator 的管理控制台安全端口。
     * </p>
     */
    public static final int _9443 = 9443;

    /**
     * <b>Ehcache (Terracotta Server)</b>: 9510
     * <p>
     * Ehcache 分布式缓存 Terracotta 服务器的默认端口。
     * </p>
     */
    public static final int _9510 = 9510;

    /**
     * <b>Nacos gRPC</b>: 9848
     * <p>
     * Nacos 用于客户端gRPC长连接的端口。
     * </p>
     */
    public static final int _9848 = 9848;

    /**
     * <b>Apache RocketMQ NameServer</b>: 9876
     * <p>
     * Apache RocketMQ的NameServer服务端口。
     * </p>
     */
    public static final int _9876 = 9876;

    /**
     * <b>Pinpoint Collector</b>: 9994
     * <p>
     * Pinpoint APM Agent向Collector发送数据的TCP端口。
     * </p>
     */
    public static final int _9994 = 9994;

    /**
     * <b>XXL-JOB Executor</b>: 9999
     * <p>
     * 分布式任务调度平台 XXL-JOB 执行器默认通信端口。
     * </p>
     */
    public static final int _9999 = 9999;

    /**
     * <b>Apache Ignite</b>: 10800
     * <p>
     * Apache Ignite 分布式数据库和缓存平台的SQL/瘦客户端端口。
     * </p>
     */
    public static final int _10800 = 10800;

    /**
     * <b>Apache RocketMQ Broker</b>: 10911
     * <p>
     * Apache RocketMQ的Broker服务端口。
     * </p>
     */
    public static final int _10911 = 10911;

    /**
     * <b>SkyWalking OAP (gRPC)</b>: 11800
     * <p>
     * Apache SkyWalking OAP服务接收agent数据的gRPC端口。
     * </p>
     */
    public static final int _11800 = 11800;

    /**
     * <b>SkyWalking OAP (HTTP)</b>: 12800
     * <p>
     * Apache SkyWalking OAP服务接收agent数据的HTTP端口。
     * </p>
     */
    public static final int _12800 = 12800;

    /**
     * <b>RabbitMQ Management</b>: 15672
     * <p>
     * RabbitMQ管理插件的Web UI端口。
     * </p>
     */
    public static final int _15672 = 15672;

    /**
     * <b>Apache Spark History Server</b>: 18080
     * <p>
     * Apache Spark History Server的Web UI端口。
     * </p>
     */
    public static final int _18080 = 18080;

    /**
     * <b>Apache Dubbo</b>: 20880
     * <p>
     * Apache Dubbo RPC框架的默认服务端口。
     * </p>
     */
    public static final int _20880 = 20880;

    /**
     * <b>MongoDB</b>: 27017
     * <p>
     * MongoDB NoSQL数据库的默认端口。
     * </p>
     */
    public static final int _27017 = 27017;

    /**
     * <b>Gerrit SSH</b>: 29418
     * <p>
     * Gerrit代码审查工具的SSH命令端口。
     * </p>
     */
    public static final int _29418 = 29418;

    /**
     * <b>gRPC (Convention)</b>: 50051
     * <p>
     * gRPC 框架教程和示例中常用的约定端口。
     * </p>
     */
    public static final int _50051 = 50051;

    /**
     * <b>Apache ActiveMQ</b>: 61616
     * <p>
     * Apache ActiveMQ消息队列的OpenWire协议端口。
     * </p>
     */
    public static final int _61616 = 61616;

}
