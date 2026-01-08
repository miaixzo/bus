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
package org.miaixz.bus.starter.mongo;

import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Auto-configuration for MongoDB, providing fine-grained control over the {@link com.mongodb.MongoClientSettings}.
 * <p>
 * This class defines a series of {@link MongoClientSettingsBuilderCustomizer} beans. Each bean is responsible for
 * applying a specific part of the configuration from {@link MongoProperties} to the
 * {@code MongoClientSettings.Builder}. The {@code @Order} annotation ensures they are applied in a predictable
 * sequence.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@EnableConfigurationProperties(value = { MongoProperties.class })
public class MongoConfiguration {

    /**
     * Injected MongoDB configuration properties.
     */
    @Resource
    MongoProperties properties;

    /**
     * Customizer for general socket settings.
     *
     * @return A {@link MongoClientSettingsBuilderCustomizer} for socket settings.
     */
    @Bean
    @Order(0)
    public MongoClientSettingsBuilderCustomizer socketSettings() {
        return builder -> Optional.ofNullable(properties.getSocket()).ifPresent(
                socketSettings -> builder.applyToSocketSettings(
                        socket -> socket
                                .readTimeout((int) socketSettings.getReadTimeoutMilliSeconds(), TimeUnit.MILLISECONDS)
                                .connectTimeout(
                                        (int) socketSettings.getConnectTimeoutMilliSeconds(),
                                        TimeUnit.MILLISECONDS)
                                .receiveBufferSize(socketSettings.getReceiveBufferSize())
                                .sendBufferSize(socketSettings.getSendBufferSize())));
    }

    /**
     * Customizer for heartbeat socket settings.
     *
     * @return A {@link MongoClientSettingsBuilderCustomizer} for heartbeat socket settings.
     */
    @Bean
    @Order(1)
    public MongoClientSettingsBuilderCustomizer heartbeatSocketSettings() {
        return builder -> Optional
                .ofNullable(
                        properties.getHeartbeatSocket())
                .ifPresent(
                        heartbeatSocketSettings -> builder.applyToSocketSettings(
                                heartBeatSocket -> heartBeatSocket
                                        .readTimeout(
                                                (int) heartbeatSocketSettings.getReadTimeoutMilliSeconds(),
                                                TimeUnit.MILLISECONDS)
                                        .connectTimeout(
                                                (int) heartbeatSocketSettings.getConnectTimeoutMilliSeconds(),
                                                TimeUnit.MILLISECONDS)
                                        .receiveBufferSize(heartbeatSocketSettings.getReceiveBufferSize())
                                        .sendBufferSize(heartbeatSocketSettings.getSendBufferSize())));
    }

    /**
     * Customizer for cluster settings.
     *
     * @return A {@link MongoClientSettingsBuilderCustomizer} for cluster settings.
     */
    @Bean
    @Order(2)
    public MongoClientSettingsBuilderCustomizer clusterSettings() {
        return builder -> Optional.ofNullable(properties.getCluster())
                .ifPresent(clusterSettings -> builder.applyToClusterSettings(cluster -> {
                    Optional.ofNullable(clusterSettings.getMode()).ifPresent(cluster::mode);
                    Optional.ofNullable(clusterSettings.getRequiredClusterType())
                            .ifPresent(cluster::requiredClusterType);
                    Optional.ofNullable(clusterSettings.getRequiredReplicaSetName())
                            .ifPresent(cluster::requiredReplicaSetName);

                    cluster.localThreshold(clusterSettings.getLocalThresholdMilliSeconds(), TimeUnit.MILLISECONDS)
                            .serverSelectionTimeout(
                                    clusterSettings.getServerSelectionTimeoutMilliSeconds(),
                                    TimeUnit.MILLISECONDS);
                }));
    }

    /**
     * Customizer for server settings.
     *
     * @return A {@link MongoClientSettingsBuilderCustomizer} for server settings.
     */
    @Bean
    @Order(3)
    public MongoClientSettingsBuilderCustomizer serverSettings() {
        return builder -> Optional
                .ofNullable(
                        properties.getServer())
                .ifPresent(
                        serverSettings -> builder.applyToServerSettings(
                                server -> server.heartbeatFrequency(
                                        serverSettings.getHeartbeatFrequencyMilliSeconds(),
                                        TimeUnit.MILLISECONDS).minHeartbeatFrequency(
                                                serverSettings.getMinHeartbeatFrequencyMilliSeconds(),
                                                TimeUnit.MILLISECONDS)));
    }

    /**
     * Customizer for connection pool settings.
     *
     * @return A {@link MongoClientSettingsBuilderCustomizer} for connection pool settings.
     */
    @Bean
    @Order(4)
    public MongoClientSettingsBuilderCustomizer connectionSettings() {
        return builder -> Optional.ofNullable(properties.getConnectionPool()).ifPresent(
                connectionSettings -> builder.applyToConnectionPoolSettings(
                        connection -> connection.maxSize(connectionSettings.getMaxSize())
                                .minSize(connectionSettings.getMinSize())
                                .maxWaitTime(connectionSettings.getMaxWaitTimeMilliSeconds(), TimeUnit.MILLISECONDS)
                                .maxConnectionLifeTime(
                                        connectionSettings.getMaxConnectionLifeTimeMilliSeconds(),
                                        TimeUnit.MILLISECONDS)
                                .maxConnectionIdleTime(
                                        connectionSettings.getMaxConnectionIdleTimeMilliSeconds(),
                                        TimeUnit.MILLISECONDS)
                                .maintenanceFrequency(
                                        connectionSettings.getMaintenanceFrequencyMilliSeconds(),
                                        TimeUnit.MILLISECONDS)
                                .maintenanceInitialDelay(
                                        connectionSettings.getMaintenanceInitialDelayMilliSeconds(),
                                        TimeUnit.MILLISECONDS)));
    }

    /**
     * Customizer for SSL/TLS settings.
     *
     * @return A {@link MongoClientSettingsBuilderCustomizer} for SSL settings.
     */
    @Bean
    @Order(5)
    public MongoClientSettingsBuilderCustomizer sslSettings() {
        return builder -> Optional.ofNullable(properties.getSsl()).ifPresent(
                sslSettings -> builder.applyToSslSettings(
                        ssl -> ssl.enabled(sslSettings.isEnabled())
                                .invalidHostNameAllowed(sslSettings.isInvalidHostNameAllowed())));
    }

}
