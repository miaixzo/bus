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
 * A constants class for service port numbers, covering a wide range of Java microservices, middleware, big data, ESB,
 * and APM ecosystems.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PORT {

    /**
     * <b>FTP Data</b>: 20
     * <p>
     * The data port for the File Transfer Protocol (Active Mode).
     */
    public static final int _20 = 20;

    /**
     * <b>FTP Command</b>: 21
     * <p>
     * The command control port for the File Transfer Protocol.
     */
    public static final int _21 = 21;

    /**
     * <b>SSH</b>: 22
     * <p>
     * Secure Shell protocol, used for secure remote login and command execution.
     */
    public static final int _22 = 22;

    /**
     * <b>SMTP</b>: 25
     * <p>
     * Simple Mail Transfer Protocol, used for sending emails.
     */
    public static final int _25 = 25;

    /**
     * <b>DNS</b>: 53
     * <p>
     * Domain Name System, used for resolving domain names to IP addresses.
     */
    public static final int _53 = 53;

    /**
     * <b>HTTP</b>: 80
     * <p>
     * Hypertext Transfer Protocol, the foundation of the World Wide Web.
     */
    public static final int _80 = 80;

    /**
     * <b>POP3</b>: 110
     * <p>
     * Post Office Protocol version 3, used for receiving emails.
     */
    public static final int _110 = 110;

    /**
     * <b>IMAP</b>: 143
     * <p>
     * Internet Message Access Protocol, used for receiving emails.
     */
    public static final int _143 = 143;

    /**
     * <b>HTTPS</b>: 443
     * <p>
     * Secure Hypertext Transfer Protocol, encrypted with TLS/SSL.
     */
    public static final int _443 = 443;

    /**
     * <b>Java RMI Registry</b>: 1099
     * <p>
     * The default port for the Java Remote Method Invocation (RMI) registry.
     */
    public static final int _1099 = 1099;

    /**
     * <b>Microsoft SQL Server</b>: 1433
     * <p>
     * The default port for Microsoft SQL Server databases.
     */
    public static final int _1433 = 1433;

    /**
     * <b>Oracle DB</b>: 1521
     * <p>
     * The default port for the Oracle database listener.
     */
    public static final int _1521 = 1521;

    /**
     * <b>Zookeeper</b>: 2181
     * <p>
     * The client port for the Apache Zookeeper coordination service.
     */
    public static final int _2181 = 2181;

    /**
     * <b>MySQL / MariaDB</b>: 3306
     * <p>
     * The default port for MySQL and MariaDB relational databases.
     */
    public static final int _3306 = 3306;

    /**
     * <b>Selenium Grid 4</b>: 4444
     * <p>
     * The Router port for Selenium Grid 4, the entry point for all WebDriver requests.
     */
    public static final int _4444 = 4444;

    /**
     * <b>GlassFish Admin</b>: 4848
     * <p>
     * The default port for the GlassFish application server admin console.
     */
    public static final int _4848 = 4848;

    /**
     * <b>JPDA Remote Debug</b>: 5005
     * <p>
     * A common convention port for Java Platform, Debugger Architecture (JPDA) remote debugging.
     */
    public static final int _5005 = 5005;

    /**
     * <b>Kibana</b>: 5601
     * <p>
     * The default port for Kibana, used for data visualization with Elasticsearch.
     * 
     */
    public static final int _5601 = 5601;

    /**
     * <b>RabbitMQ AMQP</b>: 5672
     * <p>
     * The AMQP protocol port for the RabbitMQ message queue.
     */
    public static final int _5672 = 5672;

    /**
     * <b>Hazelcast</b>: 5701
     * <p>
     * The cluster communication port for the Hazelcast distributed in-memory computing platform.
     */
    public static final int _5701 = 5701;

    /**
     * <b>Apache Flink JobManager</b>: 6123
     * <p>
     * The RPC port for the JobManager of the Apache Flink distributed computing framework.
     */
    public static final int _6123 = 6123;

    /**
     * <b>Apache Pulsar Broker</b>: 6650
     * <p>
     * The binary protocol port for the Apache Pulsar Broker.
     */
    public static final int _6650 = 6650;

    /**
     * <b>Cassandra Inter-node (Encrypted)</b>: 7001
     * <p>
     * The encrypted inter-node communication port for Cassandra clusters.
     */
    public static final int _7001 = 7001;

    /**
     * <b>Cassandra JMX</b>: 7199
     * <p>
     * A conventional port for JMX monitoring of Cassandra databases.
     */
    public static final int _7199 = 7199;

    /**
     * <b>Neo4j Bolt</b>: 7687
     * <p>
     * The Bolt protocol port for the Neo4j graph database.
     */
    public static final int _7687 = 7687;

    /**
     * <b>Mule ESB Agent</b>: 7777
     * <p>
     * The default port for the Mule ESB runtime Agent.
     */
    public static final int _7777 = 7777;

    /**
     * <b>Tomcat AJP</b>: 8009
     * <p>
     * The default port for the Tomcat AJP connector.
     */
    public static final int _8009 = 8009;

    /**
     * <b>General Web Port</b>: 8080
     * <p>
     * An extremely common port used for JBoss/WildFly, Tomcat, Spring Boot, Jenkins, Spark UI, Pulsar, Apollo,
     * Keycloak, Nginx reverse proxies, etc.
     */
    public static final int _8080 = 8080;

    /**
     * <b>General Web Alternate/Admin Port</b>: 8081
     * <p>
     * A highly reused port for Flink UI, Kafka Schema Registry, Nexus, Artifactory, Mule Agent, Druid UI, Spark Worker
     * UI, etc.
     */
    public static final int _8081 = 8081;

    /**
     * <b>Apache Druid Broker</b>: 8082
     * <p>
     * The Broker service port for the Apache Druid real-time analytics database.
     */
    public static final int _8082 = 8082;

    /**
     * <b>Apache Kafka Connect</b>: 8083
     * <p>
     * The REST API port for Kafka Connect in distributed mode.
     */
    public static final int _8083 = 8083;

    /**
     * <b>Ctrip Apollo Portal</b>: 8070
     * <p>
     * The port for the Portal service of the Ctrip Apollo distributed configuration center.
     */
    public static final int _8070 = 8070;

    /**
     * <b>Ctrip Apollo Admin Service</b>: 8090
     * <p>
     * The port for the Admin Service of the Ctrip Apollo distributed configuration center.
     */
    public static final int _8090 = 8090;

    /**
     * <b>Apache ActiveMQ Web Console</b>: 8161
     * <p>
     * The Web UI port for the Apache ActiveMQ admin console.
     */
    public static final int _8161 = 8161;

    /**
     * <b>Glowroot Central</b>: 8181
     * <p>
     * The UI and agent communication port for the Glowroot APM central collector (similar to _8081).
     */
    public static final int _8181 = 8181;

    /**
     * <b>WSO2 EI HTTPS</b>: 8243
     * <p>
     * The HTTPS PassThrough Transport port for WSO2 Enterprise Integrator.
     */
    public static final int _8243 = 8243;

    /**
     * <b>WSO2 EI HTTP</b>: 8280
     * <p>
     * The HTTP PassThrough Transport port for WSO2 Enterprise Integrator.
     */
    public static final int _8280 = 8280;

    /**
     * <b>HTTPS Alternative</b>: 8443
     * <p>
     * Often used as an alternative HTTPS port or for SSL ports on Java application servers.
     */
    public static final int _8443 = 8443;

    /**
     * <b>Netflix Eureka</b>: 8761
     * <p>
     * The default port for the Spring Cloud Netflix Eureka service discovery server.
     */
    public static final int _8761 = 8761;

    /**
     * <b>Bus Vortex</b>: 8765
     * <p>
     * The default startup port for Spring Webflux services.
     */
    public static final int _8765 = 8765;

    /**
     * <b>Alibaba Nacos</b>: 8848
     * <p>
     * The main server port for Nacos service discovery and configuration management.
     */
    public static final int _8848 = 8848;

    /**
     * <b>Apache Druid Router</b>: 8888
     * <p>
     * The Router process port for Apache Druid, serving as a unified query entry point.
     */
    public static final int _8888 = 8888;

    /**
     * <b>Apache Solr</b>: 8983
     * <p>
     * The default HTTP port for the Apache Solr search engine.
     */
    public static final int _8983 = 8983;

    /**
     * <b>SonarQube</b>: 9000
     * <p>
     * The default Web UI port for the SonarQube code quality analysis platform.
     */
    public static final int _9000 = 9000;

    /**
     * <b>Cassandra Client</b>: 9042
     * <p>
     * The client port for the Apache Cassandra distributed NoSQL database.
     */
    public static final int _9042 = 9042;

    /**
     * <b>Apache Thrift (Convention)</b>: 9090
     * <p>
     * A common convention port used in Apache Thrift RPC framework tutorials.
     */
    public static final int _9090 = 9090;

    /**
     * <b>Apache Kafka</b>: 9092
     * <p>
     * The default Broker port for the Apache Kafka distributed streaming platform.
     */
    public static final int _9092 = 9092;

    /**
     * <b>Elasticsearch HTTP</b>: 9200
     * <p>
     * The HTTP REST API port for the Elasticsearch search engine.
     */
    public static final int _9200 = 9200;

    /**
     * <b>Elasticsearch Transport</b>: 9300
     * <p>
     * The port for inter-node communication within an Elasticsearch cluster.
     */
    public static final int _9300 = 9300;

    /**
     * <b>WSO2 EI Management Console</b>: 9443
     * <p>
     * The secure port for the WSO2 Enterprise Integrator management console.
     */
    public static final int _9443 = 9443;

    /**
     * <b>Ehcache (Terracotta Server)</b>: 9510
     * <p>
     * The default port for the Terracotta Server in an Ehcache distributed cache setup.
     */
    public static final int _9510 = 9510;

    /**
     * <b>Nacos gRPC</b>: 9848
     * <p>
     * The port used by Nacos for client gRPC long connections.
     */
    public static final int _9848 = 9848;

    /**
     * <b>Apache RocketMQ NameServer</b>: 9876
     * <p>
     * The NameServer service port for Apache RocketMQ.
     */
    public static final int _9876 = 9876;

    /**
     * <b>Pinpoint Collector</b>: 9994
     * <p>
     * The TCP port where Pinpoint APM Agents send data to the Collector.
     */
    public static final int _9994 = 9994;

    /**
     * <b>XXL-JOB Executor</b>: 9999
     * <p>
     * The default communication port for the XXL-JOB distributed task scheduling platform's executor.
     */
    public static final int _9999 = 9999;

    /**
     * <b>Apache Ignite</b>: 10800
     * <p>
     * The SQL/thin client port for the Apache Ignite distributed database and caching platform.
     */
    public static final int _10800 = 10800;

    /**
     * <b>Apache RocketMQ Broker</b>: 10911
     * <p>
     * The Broker service port for Apache RocketMQ.
     */
    public static final int _10911 = 10911;

    /**
     * <b>SkyWalking OAP (gRPC)</b>: 11800
     * <p>
     * The gRPC port where the Apache SkyWalking OAP service receives data from agents.
     */
    public static final int _11800 = 11800;

    /**
     * <b>SkyWalking OAP (HTTP)</b>: 12800
     * <p>
     * The HTTP port where the Apache SkyWalking OAP service receives data from agents.
     */
    public static final int _12800 = 12800;

    /**
     * <b>RabbitMQ Management</b>: 15672
     * <p>
     * The Web UI port for the RabbitMQ Management plugin.
     */
    public static final int _15672 = 15672;

    /**
     * <b>Apache Spark History Server</b>: 18080
     * <p>
     * The Web UI port for the Apache Spark History Server.
     */
    public static final int _18080 = 18080;

    /**
     * <b>Apache Dubbo</b>: 20880
     * <p>
     * The default service port for the Apache Dubbo RPC framework.
     */
    public static final int _20880 = 20880;

    /**
     * <b>MongoDB</b>: 27017
     * <p>
     * The default port for the MongoDB NoSQL database.
     */
    public static final int _27017 = 27017;

    /**
     * <b>Gerrit SSH</b>: 29418
     * <p>
     * The SSH command port for the Gerrit code review tool.
     */
    public static final int _29418 = 29418;

    /**
     * <b>gRPC (Convention)</b>: 50051
     * <p>
     * A common convention port used in gRPC framework tutorials and examples.
     */
    public static final int _50051 = 50051;

    /**
     * <b>Apache ActiveMQ</b>: 61616
     * <p>
     * The OpenWire protocol port for the Apache ActiveMQ message queue.
     */
    public static final int _61616 = 61616;

}
