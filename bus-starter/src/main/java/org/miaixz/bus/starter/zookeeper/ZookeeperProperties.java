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
package org.miaixz.bus.starter.zookeeper;

import java.time.Duration;

import lombok.Getter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Immutable ZooKeeper connection properties.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Validated
@ConfigurationProperties(prefix = GeniusBuilder.ZOOKEEPER)
public class ZookeeperProperties {

    /**
     * Whether the zookeeper integration is enabled.
     */
    private final boolean enabled;
    /**
     * ZooKeeper ensemble connection string.
     */
    private final String connectString;
    /**
     * Curator namespace applied to all client paths.
     */
    private final String namespace;
    /**
     * Maximum time allowed to establish a ZooKeeper connection.
     */
    private final Duration connectionTimeout;
    /**
     * ZooKeeper session timeout negotiated by the client.
     */
    private final Duration sessionTimeout;
    /**
     * Initial delay used by the retry backoff policy.
     */
    private final Duration baseSleepTime;
    /**
     * Maximum number of connection retry attempts.
     */
    private final int maxRetries;
    /**
     * Optional authentication reference supplied to the Curator client.
     */
    private final String authReference;

    /**
     * Creates validated ZooKeeper properties.
     *
     * @param enabled           whether the feature is enabled
     * @param connectString     connect string
     * @param namespace         logical registry namespace
     * @param connectionTimeout connection timeout
     * @param sessionTimeout    session timeout
     * @param baseSleepTime     base sleep time
     * @param maxRetries        max retries
     * @param authReference     auth reference
     */
    public ZookeeperProperties(@DefaultValue("false") boolean enabled, String connectString, String namespace,
            @DefaultValue("15s") Duration connectionTimeout, @DefaultValue("60s") Duration sessionTimeout,
            @DefaultValue("1s") Duration baseSleepTime, @DefaultValue("3") int maxRetries, String authReference) {
        positive(connectionTimeout, "connection-timeout");
        positive(sessionTimeout, "session-timeout");
        positive(baseSleepTime, "base-sleep-time");
        if (maxRetries <= 0) {
            throw new IllegalArgumentException("bus.zookeeper.max-retries must be positive");
        }
        if (enabled) {
            validateConnectString(connectString);
        }
        this.enabled = enabled;
        this.connectString = connectString;
        this.namespace = namespace;
        this.connectionTimeout = connectionTimeout;
        this.sessionTimeout = sessionTimeout;
        this.baseSleepTime = baseSleepTime;
        this.maxRetries = maxRetries;
        this.authReference = authReference;
    }

    /**
     * Validates every host and port in a ZooKeeper connection string.
     *
     * @param value comma-separated ZooKeeper nodes
     */
    private static void validateConnectString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("bus.zookeeper.connect-string is required when enabled");
        }
        for (String node : value.split(Symbol.COMMA)) {
            int separator = node.trim().lastIndexOf(Symbol.C_COLON);
            if (separator <= 0) {
                throw new IllegalArgumentException("Invalid ZooKeeper node: " + node);
            }
            int port;
            try {
                port = Integer.parseInt(node.trim().substring(separator + 1));
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Invalid ZooKeeper port: " + node, exception);
            }
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("ZooKeeper port must be in 1..65535: " + node);
            }
        }
    }

    /**
     * Validates a positive duration property.
     *
     * @param value configured duration to validate
     * @param name  configuration property suffix
     */
    private static void positive(Duration value, String name) {
        if (value == null || value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException("bus.zookeeper." + name + " must be positive");
        }
    }

    /**
     * Exposes the retry policy's initial backoff duration in milliseconds.
     *
     * @return retry base sleep milliseconds
     */
    public int getBaseSleepTimeMs() {
        return Math.toIntExact(baseSleepTime.toMillis());
    }

    /**
     * Exposes the validated ZooKeeper connection timeout in milliseconds.
     *
     * @return connection timeout milliseconds
     */
    public int getConnectionTimeoutMs() {
        return Math.toIntExact(connectionTimeout.toMillis());
    }

    /**
     * Exposes the validated ZooKeeper session timeout in milliseconds.
     *
     * @return session timeout milliseconds
     */
    public int getSessionTimeoutMs() {
        return Math.toIntExact(sessionTimeout.toMillis());
    }

    /**
     * @return masked diagnostic representation
     */
    @Override
    public String toString() {
        return "ZookeeperProperties[enabled=" + enabled + ", connectString=" + connectString + ", namespace="
                + namespace + ", connectionTimeout=" + connectionTimeout + ", sessionTimeout=" + sessionTimeout
                + ", authReference=***]";
    }

}
