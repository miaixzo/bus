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
package org.miaixz.bus.starter.mongo;

import com.mongodb.connection.ClusterConnectionMode;
import com.mongodb.connection.ClusterType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.spring.GeniusBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Configuration properties for MongoDB, mirroring a subset of {@link com.mongodb.MongoClientSettings}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@ConfigurationProperties(prefix = GeniusBuilder.MONGO)
public class MongoProperties {

    /**
     * The settings for the network socket.
     */
    @NestedConfigurationProperty
    private Socket socket;

    /**
     * The settings for the heartbeat network socket.
     */
    @NestedConfigurationProperty
    private Socket heartbeatSocket;

    /**
     * The settings for the cluster.
     */
    @NestedConfigurationProperty
    private Cluster cluster;

    /**
     * The settings for monitoring each server in the cluster.
     */
    @NestedConfigurationProperty
    private Server server;

    /**
     * The settings for the connection pool to a MongoDB server.
     */
    @NestedConfigurationProperty
    private Connection connectionPool;

    /**
     * The settings for connecting to MongoDB via SSL/TLS.
     */
    @NestedConfigurationProperty
    private Ssl ssl;

    /**
     * Represents settings for the connection pool to a MongoDB server.
     *
     * @see com.mongodb.connection.ConnectionPoolSettings
     */
    @Data
    public static class Connection {

        /**
         * The maximum number of connections in the pool. Default is 100.
         */
        private int maxSize = 100;
        /**
         * The minimum number of connections in the pool.
         */
        private int minSize;
        /**
         * The maximum time a thread will wait for a connection to become available. Default is 2 minutes.
         */
        private long maxWaitTimeMilliSeconds = 1000 * 60 * 2;
        /**
         * The maximum lifetime of a connection in the pool. An idle or in-use connection will be closed when it exceeds
         * this time.
         */
        private long maxConnectionLifeTimeMilliSeconds;
        /**
         * The maximum time a connection can remain idle in the pool before being removed.
         */
        private long maxConnectionIdleTimeMilliSeconds;
        /**
         * The initial delay before the first run of the maintenance task.
         */
        private long maintenanceInitialDelayMilliSeconds;
        /**
         * The frequency at which the maintenance task runs. Default is 1 minute.
         */
        private long maintenanceFrequencyMilliSeconds = MILLISECONDS.convert(1, MINUTES);
    }

    /**
     * Represents settings for connecting to MongoDB via SSL/TLS.
     *
     * @see com.mongodb.connection.SslSettings
     */
    @Data
    public static class Ssl {

        /**
         * Whether SSL/TLS is enabled.
         */
        private boolean enabled;
        /**
         * Whether to allow invalid hostnames. If true, the driver will not perform hostname verification.
         */
        private boolean invalidHostNameAllowed;
    }

    /**
     * Represents settings for the cluster.
     *
     * @see com.mongodb.connection.ClusterSettings
     */
    @Data
    public static class Cluster {

        /**
         * The mode for connecting to the cluster.
         */
        private ClusterConnectionMode mode;
        /**
         * The required type of the cluster. Default is UNKNOWN.
         */
        private ClusterType requiredClusterType = ClusterType.UNKNOWN;
        /**
         * The required name of the replica set.
         */
        private String requiredReplicaSetName;
        /**
         * The acceptable latency window for selecting a server from multiple suitable servers. Default is 15ms.
         */
        private long localThresholdMilliSeconds = 15;
        /**
         * The timeout for server selection. Default is 30 seconds.
         */
        private long serverSelectionTimeoutMilliSeconds = 30000;
    }

    /**
     * Represents settings for monitoring each server in the cluster.
     *
     * @see com.mongodb.connection.ServerSettings
     */
    @Data
    public static class Server {

        /**
         * The frequency at which the driver sends a heartbeat to the server. Default is 10 seconds.
         */
        private long heartbeatFrequencyMilliSeconds = 10000;
        /**
         * The minimum frequency for heartbeats. Default is 500ms.
         */
        private long minHeartbeatFrequencyMilliSeconds = 500;
    }

    /**
     * Represents settings for the network socket.
     *
     * @see com.mongodb.connection.SocketSettings
     */
    @Data
    public static class Socket {

        /**
         * The timeout for establishing a new socket connection. Default is 10 seconds.
         */
        private long connectTimeoutMilliSeconds = 10000;
        /**
         * The timeout for reading from a socket. Default is 10 seconds.
         */
        private long readTimeoutMilliSeconds = 10000;
        /**
         * The size of the socket receive buffer.
         */
        private int receiveBufferSize;
        /**
         * The size of the socket send buffer.
         */
        private int sendBufferSize;
    }

}
