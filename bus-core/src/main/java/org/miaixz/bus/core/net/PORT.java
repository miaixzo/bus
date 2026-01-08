/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.lang.Optional;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A constants enum for service port numbers, covering a wide range of Java microservices, middleware, big data, ESB,
 * and APM ecosystems.
 * <p>
 * This enum provides a type-safe alternative to using static final integers, bundling the port number, service name,
 * and description into one object.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum PORT {

    /**
     * <b>FTP Data</b>: 20
     * <p>
     * The data port for the File Transfer Protocol (Active Mode).
     */
    _20(20, "FTP Data", "The data port for the File Transfer Protocol (Active Mode)."),

    /**
     * <b>FTP Command</b>: 21
     * <p>
     * The command control port for the File Transfer Protocol.
     */
    _21(21, "FTP Command", "The command control port for the File Transfer Protocol."),

    /**
     * <b>SSH</b>: 22
     * <p>
     * Secure Shell protocol, used for secure remote login and command execution.
     */
    _22(22, "SSH", "Secure Shell protocol, used for secure remote login and command execution."),

    /**
     * <b>SMTP</b>: 25
     * <p>
     * Simple Mail Transfer Protocol, used for sending emails.
     */
    _25(25, "SMTP", "Simple Mail Transfer Protocol, used for sending emails."),

    /**
     * <b>DNS</b>: 53
     * <p>
     * Domain Name System, used for resolving domain names to IP addresses.
     */
    _53(53, "DNS", "Domain Name System, used for resolving domain names to IP addresses."),

    /**
     * <b>HTTP</b>: 80
     * <p>
     * Hypertext Transfer Protocol, the foundation of the World Wide Web.
     */
    _80(80, "HTTP", "Hypertext Transfer Protocol, the foundation of the World Wide Web."),

    /**
     * <b>POP3</b>: 110
     * <p>
     * Post Office Protocol version 3, used for receiving emails.
     */
    _110(110, "POP3", "Post Office Protocol version 3, used for receiving emails."),

    /**
     * <b>IMAP</b>: 143
     * <p>
     * Internet Message Access Protocol, used for receiving emails.
     */
    _143(143, "IMAP", "Internet Message Access Protocol, used for receiving emails."),

    /**
     * <b>HTTPS</b>: 443
     * <p>
     * Secure Hypertext Transfer Protocol, encrypted with TLS/SSL.
     */
    _443(443, "HTTPS", "Secure Hypertext Transfer Protocol, encrypted with TLS/SSL."),

    /**
     * <b>Java RMI Registry</b>: 1099
     * <p>
     * The default port for the Java Remote Method Invocation (RMI) registry.
     */
    _1099(1099, "Java RMI Registry", "The default port for the Java Remote Method Invocation (RMI) registry."),

    /**
     * <b>Microsoft SQL Server</b>: 1433
     * <p>
     * The default port for Microsoft SQL Server databases.
     */
    _1433(1433, "Microsoft SQL Server", "The default port for Microsoft SQL Server databases."),

    /**
     * <b>Oracle DB</b>: 1521
     * <p>
     * The default port for the Oracle database listener.
     */
    _1521(1521, "Oracle DB", "The default port for the Oracle database listener."),

    /**
     * <b>Zookeeper</b>: 2181
     * <p>
     * The client port for the Apache Zookeeper coordination service.
     */
    _2181(2181, "Zookeeper", "The client port for the Apache Zookeeper coordination service."),

    /**
     * <b>MySQL / MariaDB</b>: 3306
     * <p>
     * The default port for MySQL and MariaDB relational databases.
     */
    _3306(3306, "MySQL / MariaDB", "The default port for MySQL and MariaDB relational databases."),

    /**
     * <b>Selenium Grid 4</b>: 4444
     * <p>
     * The Router port for Selenium Grid 4, the entry point for all WebDriver requests.
     */
    _4444(4444, "Selenium Grid 4", "The Router port for Selenium Grid 4, the entry point for all WebDriver requests."),

    /**
     * <b>GlassFish Admin</b>: 4848
     * <p>
     * The default port for the GlassFish application server admin console.
     */
    _4848(4848, "GlassFish Admin", "The default port for the GlassFish application server admin console."),

    /**
     * <b>JPDA Remote Debug</b>: 5005
     * <p>
     * A common convention port for Java Platform, Debugger Architecture (JPDA) remote debugging.
     */
    _5005(5005, "JPDA Remote Debug",
            "A common convention port for Java Platform, Debugger Architecture (JPDA) remote debugging."),

    /**
     * <b>Kibana</b>: 5601
     * <p>
     * The default port for Kibana, used for data visualization with Elasticsearch.
     */
    _5601(5601, "Kibana", "The default port for Kibana, used for data visualization with Elasticsearch."),

    /**
     * <b>RabbitMQ AMQP</b>: 5672
     * <p>
     * The AMQP protocol port for the RabbitMQ message queue.
     */
    _5672(5672, "RabbitMQ AMQP", "The AMQP protocol port for the RabbitMQ message queue."),

    /**
     * <b>Hazelcast</b>: 5701
     * <p>
     * The cluster communication port for the Hazelcast distributed in-memory computing platform.
     */
    _5701(5701, "Hazelcast",
            "The cluster communication port for the Hazelcast distributed in-memory computing platform."),

    /**
     * <b>Apache Flink JobManager</b>: 6123
     * <p>
     * The RPC port for the JobManager of the Apache Flink distributed computing framework.
     */
    _6123(6123, "Apache Flink JobManager",
            "The RPC port for the JobManager of the Apache Flink distributed computing framework."),

    /**
     * <b>Apache Pulsar Broker</b>: 6650
     * <p>
     * The binary protocol port for the Apache Pulsar Broker.
     */
    _6650(6650, "Apache Pulsar Broker", "The binary protocol port for the Apache Pulsar Broker."),

    /**
     * <b>Cassandra Inter-node (Encrypted)</b>: 7001
     * <p>
     * The encrypted inter-node communication port for Cassandra clusters.
     */
    _7001(7001, "Cassandra Inter-node (Encrypted)",
            "The encrypted inter-node communication port for Cassandra clusters."),

    /**
     * <b>Cassandra JMX</b>: 7199
     * <p>
     * A conventional port for JMX monitoring of Cassandra databases.
     */
    _7199(7199, "Cassandra JMX", "A conventional port for JMX monitoring of Cassandra databases."),

    /**
     * <b>Neo4j Bolt</b>: 7687
     * <p>
     * The Bolt protocol port for the Neo4j graph database.
     */
    _7687(7687, "Neo4j Bolt", "The Bolt protocol port for the Neo4j graph database."),

    /**
     * <b>Mule ESB Agent</b>: 7777
     * <p>
     * The default port for the Mule ESB runtime Agent.
     */
    _7777(7777, "Mule ESB Agent", "The default port for the Mule ESB runtime Agent."),

    /**
     * <b>Tomcat AJP</b>: 8009
     * <p>
     * The default port for the Tomcat AJP connector.
     */
    _8009(8009, "Tomcat AJP", "The default port for the Tomcat AJP connector."),

    /**
     * <b>General Web Port</b>: 8080
     * <p>
     * An extremely common port used for JBoss/WildFly, Tomcat, Spring Boot, Jenkins, Spark UI, Pulsar, Apollo,
     * Keycloak, Nginx reverse proxies, etc.
     */
    _8080(8080, "General Web Port",
            "An extremely common port used for JBoss/WildFly, Tomcat, Spring Boot, Jenkins, Spark UI, Pulsar, Apollo, Keycloak, Nginx reverse proxies, etc."),

    /**
     * <b>General Web Alternate/Admin Port</b>: 8081
     * <p>
     * A highly reused port for Flink UI, Kafka Schema Registry, Nexus, Artifactory, Mule Agent, Druid UI, Spark Worker
     * UI, etc.
     */
    _8081(8081, "General Web Alternate/Admin Port",
            "A highly reused port for Flink UI, Kafka Schema Registry, Nexus, Artifactory, Mule Agent, Druid UI, Spark Worker UI, etc."),

    /**
     * <b>Apache Druid Broker</b>: 8082
     * <p>
     * The Broker service port for the Apache Druid real-time analytics database.
     */
    _8082(8082, "Apache Druid Broker", "The Broker service port for the Apache Druid real-time analytics database."),

    /**
     * <b>Apache Kafka Connect</b>: 8083
     * <p>
     * The REST API port for Kafka Connect in distributed mode.
     */
    _8083(8083, "Apache Kafka Connect", "The REST API port for Kafka Connect in distributed mode."),

    /**
     * <b>Ctrip Apollo Portal</b>: 8070
     * <p>
     * The port for the Portal service of the Ctrip Apollo distributed configuration center.
     */
    _8070(8070, "Ctrip Apollo Portal",
            "The port for the Portal service of the Ctrip Apollo distributed configuration center."),

    /**
     * <b>Ctrip Apollo Admin Service</b>: 8090
     * <p>
     * The port for the Admin Service of the Ctrip Apollo distributed configuration center.
     */
    _8090(8090, "Ctrip Apollo Admin Service",
            "The port for the Admin Service of the Ctrip Apollo distributed configuration center."),

    /**
     * <b>Apache ActiveMQ Web Console</b>: 8161
     * <p>
     * The Web UI port for the Apache ActiveMQ admin console.
     */
    _8161(8161, "Apache ActiveMQ Web Console", "The Web UI port for the Apache ActiveMQ admin console."),

    /**
     * <b>Glowroot Central</b>: 8181
     * <p>
     * The UI and agent communication port for the Glowroot APM central collector (similar to _8081).
     */
    _8181(8181, "Glowroot Central",
            "The UI and agent communication port for the Glowroot APM central collector (similar to _8081)."),

    /**
     * <b>WSO2 EI HTTPS</b>: 8243
     * <p>
     * The HTTPS PassThrough Transport port for WSO2 Enterprise Integrator.
     */
    _8243(8243, "WSO2 EI HTTPS", "The HTTPS PassThrough Transport port for WSO2 Enterprise Integrator."),

    /**
     * <b>WSO2 EI HTTP</b>: 8280
     * <p>
     * The HTTP PassThrough Transport port for WSO2 Enterprise Integrator.
     */
    _8280(8280, "WSO2 EI HTTP", "The HTTP PassThrough Transport port for WSO2 Enterprise Integrator."),

    /**
     * <b>HTTPS Alternative</b>: 8443
     * <p>
     * Often used as an alternative HTTPS port or for SSL ports on Java application servers.
     */
    _8443(8443, "HTTPS Alternative",
            "Often used as an alternative HTTPS port or for SSL ports on Java application servers."),

    /**
     * <b>Netflix Eureka</b>: 8761
     * <p>
     * The default port for the Spring Cloud Netflix Eureka service discovery server.
     */
    _8761(8761, "Netflix Eureka", "The default port for the Spring Cloud Netflix Eureka service discovery server."),

    /**
     * <b>Bus Vortex</b>: 8765
     * <p>
     * The default startup port for Spring Webflux services.
     */
    _8765(8765, "Bus Vortex", "The default startup port for Spring Webflux services."),

    /**
     * <b>Alibaba Nacos</b>: 8848
     * <p>
     * The main server port for Nacos service discovery and configuration management.
     */
    _8848(8848, "Alibaba Nacos", "The main server port for Nacos service discovery and configuration management."),

    /**
     * <b>Apache Druid Router</b>: 8888
     * <p>
     * The Router process port for Apache Druid, serving as a unified query entry point.
     */
    _8888(8888, "Apache Druid Router",
            "The Router process port for Apache Druid, serving as a unified query entry point."),

    /**
     * <b>Apache Solr</b>: 8983
     * <p>
     * The default HTTP port for the Apache Solr search engine.
     */
    _8983(8983, "Apache Solr", "The default HTTP port for the Apache Solr search engine."),

    /**
     * <b>SonarQube</b>: 9000
     * <p>
     * The default Web UI port for the SonarQube code quality analysis platform.
     */
    _9000(9000, "SonarQube", "The default Web UI port for the SonarQube code quality analysis platform."),

    /**
     * <b>Cassandra Client</b>: 9042
     * <p>
     * The client port for the Apache Cassandra distributed NoSQL database.
     */
    _9042(9042, "Cassandra Client", "The client port for the Apache Cassandra distributed NoSQL database."),

    /**
     * <b>Apache Thrift (Convention)</b>: 9090
     * <p>
     * A common convention port used in Apache Thrift RPC framework tutorials.
     */
    _9090(9090, "Apache Thrift (Convention)",
            "A common convention port used in Apache Thrift RPC framework tutorials."),

    /**
     * <b>Apache Kafka</b>: 9092
     * <p>
     * The default Broker port for the Apache Kafka distributed streaming platform.
     */
    _9092(9092, "Apache Kafka", "The default Broker port for the Apache Kafka distributed streaming platform."),

    /**
     * <b>Elasticsearch HTTP</b>: 9200
     * <p>
     * The HTTP REST API port for the Elasticsearch search engine.
     */
    _9200(9200, "Elasticsearch HTTP", "The HTTP REST API port for the Elasticsearch search engine."),

    /**
     * <b>Elasticsearch Transport</b>: 9300
     * <p>
     * The port for inter-node communication within an Elasticsearch cluster.
     */
    _9300(9300, "Elasticsearch Transport", "The port for inter-node communication within an Elasticsearch cluster."),

    /**
     * <b>WSO2 EI Management Console</b>: 9443
     * <p>
     * The secure port for the WSO2 Enterprise Integrator management console.
     */
    _9443(9443, "WSO2 EI Management Console", "The secure port for the WSO2 Enterprise Integrator management console."),

    /**
     * <b>Ehcache (Terracotta Server)</b>: 9510
     * <p>
     * The default port for the Terracotta Server in an Ehcache distributed cache setup.
     */
    _9510(9510, "Ehcache (Terracotta Server)",
            "The default port for the Terracotta Server in an Ehcache distributed cache setup."),

    /**
     * <b>Nacos gRPC</b>: 9848
     * <p>
     * The port used by Nacos for client gRPC long connections.
     */
    _9848(9848, "Nacos gRPC", "The port used by Nacos for client gRPC long connections."),

    /**
     * <b>Apache RocketMQ NameServer</b>: 9876
     * <p>
     * The NameServer service port for Apache RocketMQ.
     */
    _9876(9876, "Apache RocketMQ NameServer", "The NameServer service port for Apache RocketMQ."),

    /**
     * <b>Pinpoint Collector</b>: 9994
     * <p>
     * The TCP port where Pinpoint APM Agents send data to the Collector.
     */
    _9994(9994, "Pinpoint Collector", "The TCP port where Pinpoint APM Agents send data to the Collector."),

    /**
     * <b>XXL-JOB Executor</b>: 9999
     * <p>
     * The default communication port for the XXL-JOB distributed task scheduling platform's executor.
     */
    _9999(9999, "XXL-JOB Executor",
            "The default communication port for the XXL-JOB distributed task scheduling platform's executor."),

    /**
     * <b>Apache Ignite</b>: 10800
     * <p>
     * The SQL/thin client port for the Apache Ignite distributed database and caching platform.
     */
    _10800(10800, "Apache Ignite",
            "The SQL/thin client port for the Apache Ignite distributed database and caching platform."),

    /**
     * <b>Apache RocketMQ Broker</b>: 10911
     * <p>
     * The Broker service port for Apache RocketMQ.
     */
    _10911(10911, "Apache RocketMQ Broker", "The Broker service port for Apache RocketMQ."),

    /**
     * <b>SkyWalking OAP (gRPC)</b>: 11800
     * <p>
     * The gRPC port where the Apache SkyWalking OAP service receives data from agents.
     */
    _11800(11800, "SkyWalking OAP (gRPC)",
            "The gRPC port where the Apache SkyWalking OAP service receives data from agents."),

    /**
     * <b>SkyWalking OAP (HTTP)</b>: 12800
     * <p>
     * The HTTP port where the Apache SkyWalking OAP service receives data from agents.
     */
    _12800(12800, "SkyWalking OAP (HTTP)",
            "The HTTP port where the Apache SkyWalking OAP service receives data from agents."),

    /**
     * <b>RabbitMQ Management</b>: 15672
     * <p>
     * The Web UI port for the RabbitMQ Management plugin.
     */
    _15672(15672, "RabbitMQ Management", "The Web UI port for the RabbitMQ Management plugin."),

    /**
     * <b>Apache Spark History Server</b>: 18080
     * <p>
     * The Web UI port for the Apache Spark History Server.
     */
    _18080(18080, "Apache Spark History Server", "The Web UI port for the Apache Spark History Server."),

    /**
     * <b>Apache Dubbo</b>: 20880
     * <p>
     * The default service port for the Apache Dubbo RPC framework.
     */
    _20880(20880, "Apache Dubbo", "The default service port for the Apache Dubbo RPC framework."),

    /**
     * <b>MongoDB</b>: 27017
     * <p>
     * The default port for the MongoDB NoSQL database.
     */
    _27017(27017, "MongoDB", "The default port for the MongoDB NoSQL database."),

    /**
     * <b>Gerrit SSH</b>: 29418
     * <p>
     * The SSH command port for the Gerrit code review tool.
     */
    _29418(29418, "Gerrit SSH", "The SSH command port for the Gerrit code review tool."),

    /**
     * <b>gRPC (Convention)</b>: 50051
     * <p>
     * A common convention port used in gRPC framework tutorials and examples.
     */
    _50051(50051, "gRPC (Convention)", "A common convention port used in gRPC framework tutorials and examples."),

    /**
     * <b>Apache ActiveMQ</b>: 61616
     * <p>
     * The OpenWire protocol port for the Apache ActiveMQ message queue.
     */
    _61616(61616, "Apache ActiveMQ", "The OpenWire protocol port for the Apache ActiveMQ message queue.");

    /**
     * Stores the integer port number (e.g., 22, 8080).
     */
    private final int port;
    /**
     * Stores the common name of the service (e.g., "SSH", "MongoDB").
     */
    private final String name;
    /**
     * Stores the detailed description of the port's purpose.
     */
    private final String desc;

    /**
     * A static map for efficient lookup of PORT by port number. This is lazily initialized on the first call to get().
     */
    private static volatile Map<Integer, PORT> PORT_MAP;
    /**
     * A private static lock object used for thread-safe lazy initialization of the {@code portMap}.
     */
    private static final Object LOCK = new Object();

    /**
     * Private constructor for the PORT enum.
     *
     * @param port The integer port number.
     * @param name The common name of the service (e.g., "SSH").
     * @param desc A description of the service's purpose.
     */
    PORT(int port, String name, String desc) {
        this.port = port;
        this.name = name;
        this.desc = desc;
    }

    /**
     * Gets the integer port number.
     *
     * @return The port number.
     */
    public int getPort() {
        return port;
    }

    /**
     * Gets the common name of the service.
     *
     * @return The service name (e.g., "SSH", "MongoDB").
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the detailed description of the port's purpose.
     *
     * @return The service description.
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Returns the port number as a string. This allows the enum to be easily used in string concatenations expecting
     * the port number, while maintaining type safety.
     *
     * @return The port number as a String (e.g., "22", "8080").
     */
    @Override
    public String toString() {
        return String.valueOf(this.port);
    }

    /**
     * Finds a PORT enum constant by its integer port number.
     * <p>
     * This method uses a lazily initialized, thread-safe cache for efficient lookups.
     *
     * @param portNumber The port number to search for (e.g., 22).
     * @return An {@link Optional} containing the matching {@code PORT}, or {@link Optional#empty()} if no match is
     *         found.
     */
    public static Optional<PORT> get(int portNumber) {
        if (PORT_MAP == null) {
            synchronized (LOCK) {
                if (PORT_MAP == null) {
                    PORT_MAP = Stream.of(values())
                            .collect(Collectors.toUnmodifiableMap(PORT::getPort, Function.identity()));
                }
            }
        }
        return Optional.ofNullable(PORT_MAP.get(portNumber));
    }

}
