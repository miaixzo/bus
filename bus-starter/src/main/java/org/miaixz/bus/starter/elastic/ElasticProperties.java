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

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Immutable Elasticsearch client properties.
 *
 * @author Kimi Liu
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
     * Preferred HTTP protocol version: {@code auto}, {@code 1.1} or {@code 2}. Version {@code 2} offers h2 through TLS
     * ALPN; plain HTTP and TLS peers without ALPN fall back to HTTP/1.1.
     */
    private final String httpVersion;

    /**
     * HTTP/2 keepalive PING interval; zero disables keepalive.
     */
    private final Duration http2PingInterval;

    /**
     * HTTP/2 keepalive PING acknowledgement timeout.
     */
    private final Duration http2PingTimeout;

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
     * @param httpVersion              preferred HTTP protocol version
     * @param http2PingInterval        HTTP/2 keepalive PING interval
     * @param http2PingTimeout         HTTP/2 keepalive PING acknowledgement timeout
     * @param connectTimeout           connection timeout
     * @param socketTimeout            response timeout
     * @param connectionRequestTimeout connection-pool acquisition timeout
     * @param maxConnectTotal          maximum total connections
     * @param maxConnectPerRoute       maximum connections per route
     */
    @ConstructorBinding
    public ElasticProperties(@DefaultValue("false") boolean enabled, String hosts, String username, String password,
            @DefaultValue(DEFAULT_SCHEMA) String schema, @DefaultValue("auto") String httpVersion,
            @DefaultValue("30s") Duration http2PingInterval, @DefaultValue("10s") Duration http2PingTimeout,
            @DefaultValue("6s") Duration connectTimeout, @DefaultValue("60s") Duration socketTimeout,
            @DefaultValue("6s") Duration connectionRequestTimeout, @DefaultValue("2000") int maxConnectTotal,
            @DefaultValue("500") int maxConnectPerRoute) {
        if (http2PingInterval == null || http2PingInterval.isNegative()) {
            throw new IllegalArgumentException("bus.elastic.http2-ping-interval must not be negative");
        }
        if (http2PingTimeout == null || http2PingTimeout.isZero() || http2PingTimeout.isNegative()) {
            throw new IllegalArgumentException("bus.elastic.http2-ping-timeout must be greater than zero");
        }
        requirePositive(connectTimeout, "connect-timeout");
        requirePositive(socketTimeout, "socket-timeout");
        requirePositive(connectionRequestTimeout, "connection-request-timeout");
        if (!"auto".equalsIgnoreCase(httpVersion) && !"1.1".equals(httpVersion) && !"2".equals(httpVersion)) {
            throw new IllegalArgumentException("bus.elastic.http-version must be one of: auto, 1.1, 2");
        }
        if (maxConnectTotal <= 0 || maxConnectPerRoute <= 0 || maxConnectPerRoute > maxConnectTotal) {
            throw new IllegalArgumentException(
                    "Elasticsearch connection limits must be positive and per-route <= total");
        }
        List<String> hostList = splitHosts(hosts);
        for (String host : hostList) {
            nodeUri(schema, host);
        }
        this.enabled = enabled;
        this.hosts = hosts;
        this.username = username;
        this.password = password;
        this.schema = schema;
        this.httpVersion = httpVersion == null ? "auto" : httpVersion;
        this.http2PingInterval = http2PingInterval;
        this.http2PingTimeout = http2PingTimeout;
        this.connectTimeout = connectTimeout;
        this.socketTimeout = socketTimeout;
        this.connectionRequestTimeout = connectionRequestTimeout;
        this.maxConnectTotal = maxConnectTotal;
        this.maxConnectPerRoute = maxConnectPerRoute;
    }

    /**
     * Creates properties with the historical constructor signature.
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
    public ElasticProperties(boolean enabled, String hosts, String username, String password, String schema,
            Duration connectTimeout, Duration socketTimeout, Duration connectionRequestTimeout, int maxConnectTotal,
            int maxConnectPerRoute) {
        this(enabled, hosts, username, password, schema, "auto", Duration.ofSeconds(30), Duration.ofSeconds(10),
                connectTimeout, socketTimeout, connectionRequestTimeout, maxConnectTotal, maxConnectPerRoute);
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
     * Validates and builds a node using the same parser during binding and transport initialization.
     *
     * @param schema HTTP scheme
     * @param host   host:port or [IPv6]:port address
     * @return validated node URI
     */
    static URI nodeUri(String schema, String host) {
        if (!"http".equalsIgnoreCase(schema) && !"https".equalsIgnoreCase(schema)) {
            throw new IllegalArgumentException("Elasticsearch schema must be http or https");
        }
        try {
            URI uri = new URI(schema + "://" + host).parseServerAuthority();
            if (uri.getHost() == null || uri.getPort() < 1 || uri.getPort() > 65535 || uri.getUserInfo() != null
                    || !uri.getRawPath().isEmpty() || uri.getRawQuery() != null || uri.getRawFragment() != null) {
                throw new IllegalArgumentException("Invalid Elasticsearch host: " + host);
            }
            return uri;
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("Invalid Elasticsearch host: " + host, exception);
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
     * Returns a safe diagnostic representation of this configuration.
     *
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
