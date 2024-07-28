/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
 * Mongo配置信息 {@link com.mongodb.MongoClientSettings}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@ConfigurationProperties(prefix = GeniusBuilder.MONGO)
public class MongoProperties {

    /**
     * The socket settings
     */
    @NestedConfigurationProperty
    private Socket socket;

    /**
     * The heartbeat socket settings
     */
    @NestedConfigurationProperty
    private Socket heartbeatSocket;

    /**
     * The cluster settings
     */
    @NestedConfigurationProperty
    private Cluster cluster;

    /**
     * Settings relating to monitoring of each server.
     */
    @NestedConfigurationProperty
    private Server server;

    /**
     * All settings that relate to the pool of connections to a MongoDB server.
     */
    @NestedConfigurationProperty
    private Connection connectionPool;

    /**
     * Settings for connecting to MongoDB via SSL.
     */
    @NestedConfigurationProperty
    private Ssl ssl;

    /**
     * All settings that relate to the pool of connections to a MongoDB server.
     *
     * @see com.mongodb.connection.ConnectionPoolSettings
     */
    @Data
    public static class Connection {
        private int maxSize = 100;
        private int minSize;
        private long maxWaitTimeMilliSeconds = 1000 * 60 * 2;
        private long maxConnectionLifeTimeMilliSeconds;
        private long maxConnectionIdleTimeMilliSeconds;
        private long maintenanceInitialDelayMilliSeconds;
        private long maintenanceFrequencyMilliSeconds = MILLISECONDS.convert(1, MINUTES);
    }

    /**
     * Settings for connecting to MongoDB via SSL.
     *
     * @see com.mongodb.connection.SslSettings
     */
    @Data
    public static class Ssl {
        private boolean enabled;
        private boolean invalidHostNameAllowed;
    }

    /**
     * Settings for the cluster.
     *
     * @see com.mongodb.connection.ClusterSettings
     */
    @Data
    public static class Cluster {
        private ClusterConnectionMode mode;
        private ClusterType requiredClusterType = ClusterType.UNKNOWN;
        private String requiredReplicaSetName;
        private long localThresholdMilliSeconds = 15;
        private long serverSelectionTimeoutMilliSeconds = 30000;
    }

    /**
     * Settings relating to monitoring of each server.
     *
     * @see com.mongodb.connection.ServerSettings
     */
    @Data
    public static class Server {
        private long heartbeatFrequencyMilliSeconds = 10000;
        private long minHeartbeatFrequencyMilliSeconds = 500;
    }

    /**
     * The mongo socket settings
     *
     * @see com.mongodb.connection.SocketSettings
     */
    @Data
    public static class Socket {
        /**
         * The socket connect timeout MilliSeconds
         */
        private long connectTimeoutMilliSeconds = 10000;
        /**
         * The socket read timeout MilliSeconds
         */
        private long readTimeoutMilliSeconds = 10000;
        /**
         * The receive buffer size
         */
        private int receiveBufferSize;
        /**
         * The send buffer size
         */
        private int sendBufferSize;
    }

}
