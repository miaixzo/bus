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
 * @since Java 21+
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
