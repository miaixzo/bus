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
package org.miaixz.bus.starter.elastic;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Immutable Elasticsearch client properties.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Validated
@ConfigurationProperties(prefix = GeniusBuilder.ELASTIC)
public class ElasticProperties {

    /**
     * HTTP scheme used when no Elasticsearch scheme is configured.
     */
    private static final String DEFAULT_SCHEMA = "http";

    /**
     * Whether the elastic integration is enabled.
     */
    private final boolean enabled;
    /**
     * Elasticsearch node addresses used to build the low-level client.
     */
    private final String hosts;
    /**
     * Username used for HTTP basic authentication.
     */
    private final String username;
    /**
     * Password used for HTTP basic authentication.
     */
    private final String password;
    /**
     * HTTP scheme applied to host entries that omit one.
     */
    private final String schema;
    /**
     * Maximum time allowed to establish a connection.
     */
    private final Duration connectTimeout;
    /**
     * Maximum inactivity time while waiting for socket data.
     */
    private final Duration socketTimeout;
    /**
     * Maximum time allowed to obtain a connection from the pool.
     */
    private final Duration connectionRequestTimeout;
    /**
     * Maximum number of pooled connections across all routes.
     */
    private final int maxConnectTotal;
    /**
     * Maximum number of pooled connections for one route.
     */
    private final int maxConnectPerRoute;

    /**
     * Creates Elasticsearch properties after validating connection limits and every configured host address.
     *
     * @param enabled                  whether Elasticsearch integration is enabled
     * @param hosts                    comma-separated host and port pairs
     * @param username                 credential-provider username reference
     * @param password                 credential-provider password reference
     * @param schema                   URI scheme
     * @param connectTimeout           connection timeout
     * @param socketTimeout            response timeout
     * @param connectionRequestTimeout connection-pool acquisition timeout
     * @param maxConnectTotal          maximum total connections
     * @param maxConnectPerRoute       maximum connections per route
     */
    public ElasticProperties(@DefaultValue("false") boolean enabled, String hosts, String username, String password,
            @DefaultValue(DEFAULT_SCHEMA) String schema, @DefaultValue("6s") Duration connectTimeout,
            @DefaultValue("60s") Duration socketTimeout, @DefaultValue("6s") Duration connectionRequestTimeout,
            @DefaultValue("2000") int maxConnectTotal, @DefaultValue("500") int maxConnectPerRoute) {
        requirePositive(connectTimeout, "connect-timeout");
        requirePositive(socketTimeout, "socket-timeout");
        requirePositive(connectionRequestTimeout, "connection-request-timeout");
        if (maxConnectTotal <= 0 || maxConnectPerRoute <= 0 || maxConnectPerRoute > maxConnectTotal) {
            throw new IllegalArgumentException(
                    "Elasticsearch connection limits must be positive and per-route <= total");
        }
        List<String> hostList = splitHosts(hosts);
        for (String host : hostList) {
            int separator = host.lastIndexOf(Symbol.COLON);
            if (separator <= 0 || separator == host.length() - 1) {
                throw new IllegalArgumentException("Invalid Elasticsearch host: " + host);
            }
            int port;
            try {
                port = Integer.parseInt(host.substring(separator + 1));
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Invalid Elasticsearch port: " + host, exception);
            }
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("Elasticsearch port must be in 1..65535: " + host);
            }
        }
        this.enabled = enabled;
        this.hosts = hosts;
        this.username = username;
        this.password = password;
        this.schema = schema;
        this.connectTimeout = connectTimeout;
        this.socketTimeout = socketTimeout;
        this.connectionRequestTimeout = connectionRequestTimeout;
        this.maxConnectTotal = maxConnectTotal;
        this.maxConnectPerRoute = maxConnectPerRoute;
    }

    /**
     * Validates a required positive duration property.
     *
     * @param value configured duration
     * @param name  configuration property suffix
     */
    private static void requirePositive(Duration value, String name) {
        if (value == null || value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException("bus.elastic." + name + " must be greater than zero");
        }
    }

    /**
     * Splits and normalizes the configured Elasticsearch host list.
     *
     * @param hosts configured Elasticsearch hosts
     * @return normalized Elasticsearch host addresses
     */
    private static List<String> splitHosts(String hosts) {
        if (hosts == null || hosts.isBlank()) {
            return List.of();
        }
        return Arrays.stream(hosts.split(Symbol.COMMA)).map(String::trim).filter(value -> !value.isEmpty()).toList();
    }

    /**
     * Returns the normalized immutable Elasticsearch node address list.
     *
     * @return normalized configured hosts
     */
    public List<String> getHostList() {
        return splitHosts(hosts);
    }

    /**
     * Exposes the validated connection establishment timeout.
     *
     * @return connection timeout in milliseconds
     */
    public int getConnectTimeout() {
        return Math.toIntExact(connectTimeout.toMillis());
    }

    /**
     * Exposes the validated socket inactivity timeout.
     *
     * @return response timeout in milliseconds
     */
    public int getSocketTimeout() {
        return Math.toIntExact(socketTimeout.toMillis());
    }

    /**
     * Exposes the validated connection-pool acquisition timeout.
     *
     * @return pool acquisition timeout in milliseconds
     */
    public int getConnectionRequestTimeout() {
        return Math.toIntExact(connectionRequestTimeout.toMillis());
    }

    /**
     * @return safe diagnostic text
     */
    @Override
    public String toString() {
        return "ElasticProperties[enabled=" + enabled + ", hosts=" + getHostList() + ", username=***, password=***"
                + ", schema=" + schema + ", connectTimeout=" + connectTimeout + ", socketTimeout=" + socketTimeout
                + ", connectionRequestTimeout=" + connectionRequestTimeout + ", maxConnectTotal=" + maxConnectTotal
                + ", maxConnectPerRoute=" + maxConnectPerRoute + "]";
    }

}
