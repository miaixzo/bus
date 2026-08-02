/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.starter.mongo;

import java.time.Duration;

import lombok.Getter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import com.mongodb.connection.ClusterConnectionMode;
import com.mongodb.connection.ClusterType;

import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Immutable properties used only by Bus Mongo client-settings customizers.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Validated
@ConfigurationProperties(prefix = GeniusBuilder.MONGO)
public class MongoProperties {

    /**
     * Whether the mongo integration is enabled.
     */
    private final boolean enabled;
    /**
     * MongoDB client socket timeout and buffer settings.
     */
    private final Socket socket;
    /**
     * Socket settings used by the server heartbeat monitor.
     */
    private final Socket heartbeatSocket;
    /**
     * Cluster discovery and server-selection settings.
     */
    private final Cluster cluster;
    /**
     * Server heartbeat frequency settings.
     */
    private final Server server;
    /**
     * Connection pool capacity and lifecycle settings.
     */
    private final Connection connectionPool;
    /**
     * TLS activation and hostname validation settings.
     */
    private final Ssl ssl;

    /**
     * Creates Mongo customizer properties.
     *
     * @param enabled         whether the feature is enabled
     * @param socket          MongoDB socket settings
     * @param heartbeatSocket heartbeat socket
     * @param cluster         MongoDB cluster settings
     * @param server          MongoDB server settings
     * @param connectionPool  connection pool
     * @param ssl             MongoDB TLS settings
     */
    public MongoProperties(@DefaultValue("false") boolean enabled, Socket socket, Socket heartbeatSocket,
            Cluster cluster, Server server, Connection connectionPool, Ssl ssl) {
        this.enabled = enabled;
        this.socket = socket;
        this.heartbeatSocket = heartbeatSocket;
        this.cluster = cluster;
        this.server = server;
        this.connectionPool = connectionPool;
        this.ssl = ssl;
    }

    /**
     * Socket customizer settings.
     *
     * @param connectTimeout    connect timeout
     * @param readTimeout       read timeout
     * @param receiveBufferSize receive buffer size
     * @param sendBufferSize    send buffer size
     */
    public record Socket(@DefaultValue("10s") Duration connectTimeout, @DefaultValue("10s") Duration readTimeout,
            @DefaultValue("0") int receiveBufferSize, @DefaultValue("0") int sendBufferSize) {

        /**
         * Validates socket settings.
         */
        public Socket {
            positive(connectTimeout, "socket.connect-timeout", false);
            positive(readTimeout, "socket.read-timeout", false);
            if (receiveBufferSize < 0 || sendBufferSize < 0) {
                throw new IllegalArgumentException("Mongo socket buffer sizes must not be negative");
            }
        }

        /**
         * Exposes the socket read timeout in milliseconds.
         *
         * @return read timeout milliseconds
         */
        public long getReadTimeoutMilliSeconds() {
            return readTimeout.toMillis();
        }

        /**
         * Exposes the socket connection timeout in milliseconds.
         *
         * @return connect timeout milliseconds
         */
        public long getConnectTimeoutMilliSeconds() {
            return connectTimeout.toMillis();
        }

        /**
         * Exposes the requested socket receive-buffer capacity in bytes.
         *
         * @return receive buffer size
         */
        public int getReceiveBufferSize() {
            return receiveBufferSize;
        }

        /**
         * Exposes the requested socket send-buffer capacity in bytes.
         *
         * @return send buffer size
         */
        public int getSendBufferSize() {
            return sendBufferSize;
        }
    }

    /**
     * Cluster customizer settings.
     *
     * @param mode                   configured operating mode
     * @param requiredClusterType    required cluster type
     * @param requiredReplicaSetName required replica set name
     * @param localThreshold         local threshold
     * @param serverSelectionTimeout server selection timeout
     */
    public record Cluster(ClusterConnectionMode mode, @DefaultValue("UNKNOWN") ClusterType requiredClusterType,
            String requiredReplicaSetName, @DefaultValue("15ms") Duration localThreshold,
            @DefaultValue("30s") Duration serverSelectionTimeout) {

        /**
         * Validates cluster timing settings.
         */
        public Cluster {
            positive(localThreshold, "cluster.local-threshold", false);
            positive(serverSelectionTimeout, "cluster.server-selection-timeout", false);
        }

        /**
         * Exposes the MongoDB cluster connection mode.
         *
         * @return connection mode
         */
        public ClusterConnectionMode getMode() {
            return mode;
        }

        /**
         * Exposes the server type required during cluster discovery.
         *
         * @return required cluster type
         */
        public ClusterType getRequiredClusterType() {
            return requiredClusterType;
        }

        /**
         * Exposes the replica-set name required during cluster discovery.
         *
         * @return required replica set
         */
        public String getRequiredReplicaSetName() {
            return requiredReplicaSetName;
        }

        /**
         * Exposes the cluster latency threshold in milliseconds.
         *
         * @return local threshold milliseconds
         */
        public long getLocalThresholdMilliSeconds() {
            return localThreshold.toMillis();
        }

        /**
         * Exposes the server-selection timeout in milliseconds.
         *
         * @return selection timeout milliseconds
         */
        public long getServerSelectionTimeoutMilliSeconds() {
            return serverSelectionTimeout.toMillis();
        }
    }

    /**
     * Server-monitor customizer settings.
     *
     * @param heartbeatFrequency    heartbeat frequency
     * @param minHeartbeatFrequency min heartbeat frequency
     */
    public record Server(@DefaultValue("10s") Duration heartbeatFrequency,
            @DefaultValue("500ms") Duration minHeartbeatFrequency) {

        /**
         * Validates heartbeat intervals.
         */
        public Server {
            positive(heartbeatFrequency, "server.heartbeat-frequency", false);
            positive(minHeartbeatFrequency, "server.min-heartbeat-frequency", false);
        }

        /**
         * Exposes the regular server heartbeat interval in milliseconds.
         *
         * @return heartbeat interval milliseconds
         */
        public long getHeartbeatFrequencyMilliSeconds() {
            return heartbeatFrequency.toMillis();
        }

        /**
         * Exposes the minimum server heartbeat interval in milliseconds.
         *
         * @return minimum heartbeat interval milliseconds
         */
        public long getMinHeartbeatFrequencyMilliSeconds() {
            return minHeartbeatFrequency.toMillis();
        }
    }

    /**
     * Connection-pool customizer settings.
     *
     * @param maxSize                 max size
     * @param minSize                 min size
     * @param maxWaitTime             max wait time
     * @param maxConnectionLifeTime   max connection life time
     * @param maxConnectionIdleTime   max connection idle time
     * @param maintenanceInitialDelay maintenance initial delay
     * @param maintenanceFrequency    maintenance frequency
     */
    public record Connection(@DefaultValue("100") int maxSize, @DefaultValue("0") int minSize,
            @DefaultValue("2m") Duration maxWaitTime, @DefaultValue("0") Duration maxConnectionLifeTime,
            @DefaultValue("0") Duration maxConnectionIdleTime, @DefaultValue("0") Duration maintenanceInitialDelay,
            @DefaultValue("1m") Duration maintenanceFrequency) {

        /**
         * Validates pool bounds and durations.
         */
        public Connection {
            if (maxSize <= 0 || minSize < 0 || minSize > maxSize) {
                throw new IllegalArgumentException("Mongo pool sizes require 0 <= min-size <= max-size and max > 0");
            }
            positive(maxWaitTime, "connection-pool.max-wait-time", false);
            positive(maxConnectionLifeTime, "connection-pool.max-connection-life-time", true);
            positive(maxConnectionIdleTime, "connection-pool.max-connection-idle-time", true);
            positive(maintenanceInitialDelay, "connection-pool.maintenance-initial-delay", true);
            positive(maintenanceFrequency, "connection-pool.maintenance-frequency", false);
        }

        /**
         * Exposes the maximum number of connections retained by the pool.
         *
         * @return maximum pool size
         */
        public int getMaxSize() {
            return maxSize;
        }

        /**
         * Exposes the minimum number of connections retained by the pool.
         *
         * @return minimum pool size
         */
        public int getMinSize() {
            return minSize;
        }

        /**
         * Exposes the maximum connection-pool wait time in milliseconds.
         *
         * @return wait milliseconds
         */
        public long getMaxWaitTimeMilliSeconds() {
            return maxWaitTime.toMillis();
        }

        /**
         * Exposes the maximum pooled-connection lifetime in milliseconds.
         *
         * @return lifetime milliseconds
         */
        public long getMaxConnectionLifeTimeMilliSeconds() {
            return maxConnectionLifeTime.toMillis();
        }

        /**
         * Exposes the maximum pooled-connection idle time in milliseconds.
         *
         * @return idle milliseconds
         */
        public long getMaxConnectionIdleTimeMilliSeconds() {
            return maxConnectionIdleTime.toMillis();
        }

        /**
         * Exposes the initial connection-pool maintenance delay in milliseconds.
         *
         * @return maintenance delay milliseconds
         */
        public long getMaintenanceInitialDelayMilliSeconds() {
            return maintenanceInitialDelay.toMillis();
        }

        /**
         * Exposes the connection-pool maintenance interval in milliseconds.
         *
         * @return maintenance frequency milliseconds
         */
        public long getMaintenanceFrequencyMilliSeconds() {
            return maintenanceFrequency.toMillis();
        }
    }

    /**
     * Immutable TLS settings for MongoDB client connections.
     *
     * @param enabled                whether the feature is enabled
     * @param invalidHostNameAllowed invalid host name allowed
     */
    public record Ssl(boolean enabled, boolean invalidHostNameAllowed) {

        /**
         * Indicates whether TLS is enabled for MongoDB connections.
         *
         * @return whether TLS is enabled
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * Indicates whether TLS certificate hostname mismatches are accepted.
         *
         * @return whether invalid hostnames are accepted
         */
        public boolean isInvalidHostNameAllowed() {
            return invalidHostNameAllowed;
        }
    }

    /**
     * Validates a non-negative or positive duration property.
     *
     * @param value       configured duration
     * @param name        configuration property suffix
     * @param zeroAllowed whether zero is accepted
     */
    private static void positive(Duration value, String name, boolean zeroAllowed) {
        if (value == null || value.isNegative() || (!zeroAllowed && value.isZero())) {
            throw new IllegalArgumentException("bus.mongo." + name + " has an invalid duration");
        }
    }

}
