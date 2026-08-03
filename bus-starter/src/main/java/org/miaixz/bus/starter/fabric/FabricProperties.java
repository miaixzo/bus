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
package org.miaixz.bus.starter.fabric;

import java.time.Duration;

import lombok.Getter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import org.miaixz.bus.fabric.network.dns.server.DnsServerOptions;
import org.miaixz.bus.fabric.network.dns.server.DnsTransport;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Unified Fabric communication service properties.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Validated
@ConfigurationProperties(prefix = GeniusBuilder.FABRIC)
public class FabricProperties {

    /**
     * Whether the fabric integration is enabled.
     */
    private final boolean enabled;
    /**
     * TCP socket server endpoint settings.
     */
    private final Socket socket;
    /**
     * WebSocket server endpoint settings.
     */
    private final WebSocket websocket;
    /**
     * DNS server endpoint and runtime settings.
     */
    private final Dns dns;

    /**
     * Composes the independently configurable TCP socket and WebSocket service settings.
     *
     * @param enabled   whether fabric integration is enabled
     * @param socket    socket server options
     * @param websocket WebSocket server options
     * @param dns       DNS server options
     */
    public FabricProperties(@DefaultValue("false") boolean enabled, @DefaultValue Socket socket,
            @DefaultValue WebSocket websocket, @DefaultValue Dns dns) {
        this.enabled = enabled;
        this.socket = socket == null ? new Socket() : socket;
        this.websocket = websocket == null ? new WebSocket() : websocket;
        this.dns = dns == null ? Dns.defaults() : dns;
    }

    /**
     * Extensible socket server options.
     */
    public static class Socket {

        /**
         * Whether the TCP socket service is enabled.
         */
        private boolean enabled;

        /**
         * TCP socket listening host.
         */
        private String host;

        /**
         * TCP socket listening port.
         */
        private int port;

        /**
         * Creates socket defaults.
         */
        public Socket() {
            this(true, "0.0.0.0", 7890);
        }

        /**
         * Creates and validates TCP socket options.
         *
         * @param enabled whether the service is enabled
         * @param host    listening host
         * @param port    listening port
         */
        public Socket(boolean enabled, String host, int port) {
            validateEndpoint(enabled, host, port, null, "socket");
            this.enabled = enabled;
            this.host = host;
            this.port = port;
        }

        /**
         * Returns whether the TCP socket service is enabled.
         *
         * @return {@code true} when enabled
         */
        public boolean isEnabled() {
            return this.enabled;
        }

        /**
         * Exposes the network interface on which the TCP socket server listens.
         *
         * @return listening host
         */
        public String getHost() {
            return this.host;
        }

        /**
         * Exposes the TCP socket server listening port.
         *
         * @return listening port
         */
        public int getPort() {
            return this.port;
        }

        /**
         * Enables or disables the TCP socket service.
         *
         * @param enabled whether the service is enabled
         */
        public void setEnabled(boolean enabled) {
            validateEndpoint(enabled, this.host, this.port, null, "socket");
            this.enabled = enabled;
        }

        /**
         * Changes the TCP socket listening host.
         *
         * @param host listening host
         */
        public void setHost(String host) {
            validateEndpoint(this.enabled, host, this.port, null, "socket");
            this.host = host;
        }

        /**
         * Changes the TCP socket listening port.
         *
         * @param port listening port
         */
        public void setPort(int port) {
            validateEndpoint(this.enabled, this.host, port, null, "socket");
            this.port = port;
        }

    }

    /**
     * Extensible WebSocket server options.
     */
    public static class WebSocket {

        /**
         * Whether the WebSocket service is enabled.
         */
        private boolean enabled;

        /**
         * WebSocket listening host.
         */
        private String host;

        /**
         * WebSocket listening port.
         */
        private int port;

        /**
         * HTTP upgrade path.
         */
        private String path;

        /**
         * Creates WebSocket defaults.
         */
        public WebSocket() {
            this(false, "0.0.0.0", 7891, "/ws");
        }

        /**
         * Creates and validates WebSocket options.
         *
         * @param enabled whether the service is enabled
         * @param host    listening host
         * @param port    listening port
         * @param path    HTTP upgrade path
         */
        public WebSocket(boolean enabled, String host, int port, String path) {
            validateEndpoint(enabled, host, port, path, "websocket");
            this.enabled = enabled;
            this.host = host;
            this.port = port;
            this.path = path;
        }

        /**
         * Returns whether the WebSocket service is enabled.
         *
         * @return {@code true} when enabled
         */
        public boolean isEnabled() {
            return this.enabled;
        }

        /**
         * Exposes the network interface on which the WebSocket server listens.
         *
         * @return listening host
         */
        public String getHost() {
            return this.host;
        }

        /**
         * Exposes the WebSocket server listening port.
         *
         * @return listening port
         */
        public int getPort() {
            return this.port;
        }

        /**
         * Exposes the HTTP upgrade path accepted by the WebSocket server.
         *
         * @return upgrade path
         */
        public String getPath() {
            return this.path;
        }

        /**
         * Enables or disables the WebSocket service.
         *
         * @param enabled whether the service is enabled
         */
        public void setEnabled(boolean enabled) {
            validateEndpoint(enabled, this.host, this.port, this.path, "websocket");
            this.enabled = enabled;
        }

        /**
         * Changes the WebSocket listening host.
         *
         * @param host listening host
         */
        public void setHost(String host) {
            validateEndpoint(this.enabled, host, this.port, this.path, "websocket");
            this.host = host;
        }

        /**
         * Changes the WebSocket listening port.
         *
         * @param port listening port
         */
        public void setPort(int port) {
            validateEndpoint(this.enabled, this.host, port, this.path, "websocket");
            this.port = port;
        }

        /**
         * Changes the HTTP upgrade path.
         *
         * @param path HTTP upgrade path
         */
        public void setPath(String path) {
            validateEndpoint(this.enabled, this.host, this.port, path, "websocket");
            this.path = path;
        }

    }

    /**
     * Fabric DNS server options.
     */
    @Getter
    public static class Dns {

        /**
         * Default network interface used by the DNS listener.
         */
        private static final String DEFAULT_HOST = "0.0.0.0";
        /**
         * Default DNS listener port.
         */
        private static final int DEFAULT_PORT = 53;

        /**
         * Whether the DNS server is enabled.
         */
        private final boolean enabled;
        /**
         * DNS listener transport.
         */
        private final DnsTransport transport;
        /**
         * DNS listener host.
         */
        private final String host;
        /**
         * DNS listener port.
         */
        private final int port;
        /**
         * Whether DNS response caching is enabled.
         */
        private final boolean cache;
        /**
         * Maximum number of cached DNS responses.
         */
        private final int cacheMaxEntries;
        /**
         * DNS response cache lifetime.
         */
        private final Duration cacheTtl;
        /**
         * Duration for which expired DNS responses may be served.
         */
        private final Duration cacheServeStaleTtl;
        /**
         * Interval before expiry at which a DNS response is prefetched.
         */
        private final Duration cachePrefetchBeforeExpiry;
        /**
         * Maximum DNS response payload accepted over UDP.
         */
        private final int maxUdpPayloadBytes;
        /**
         * Maximum DNS queries accepted from one client per second.
         */
        private final int rateLimitPerSecond;

        /**
         * Creates and validates DNS server options.
         *
         * @param enabled                   whether the DNS server is enabled
         * @param transport                 listener transport
         * @param host                      listener bind host
         * @param port                      listener bind port
         * @param cache                     whether response caching is enabled
         * @param cacheMaxEntries           maximum cached responses
         * @param cacheTtl                  response cache TTL
         * @param cacheServeStaleTtl        serve-stale window
         * @param cachePrefetchBeforeExpiry cache prefetch window
         * @param maxUdpPayloadBytes        maximum UDP response payload
         * @param rateLimitPerSecond        per-client query rate limit
         */
        public Dns(final Boolean enabled, final DnsTransport transport, final String host, final Integer port,
                final Boolean cache, final Integer cacheMaxEntries, final Duration cacheTtl,
                final Duration cacheServeStaleTtl, final Duration cachePrefetchBeforeExpiry,
                final Integer maxUdpPayloadBytes, final Integer rateLimitPerSecond) {
            this.enabled = enabled != null && enabled;
            this.transport = transport == null ? DnsTransport.UDP : transport;
            this.host = normalizeHost(host);
            this.port = port == null ? DEFAULT_PORT : port;
            this.cache = cache == null || cache;
            this.cacheMaxEntries = cacheMaxEntries == null ? DnsServerOptions.DEFAULT_CACHE_MAX_ENTRIES
                    : cacheMaxEntries;
            this.cacheTtl = cacheTtl == null ? DnsServerOptions.DEFAULT_CACHE_TTL : cacheTtl;
            this.cacheServeStaleTtl = cacheServeStaleTtl == null ? DnsServerOptions.DEFAULT_CACHE_SERVE_STALE_TTL
                    : cacheServeStaleTtl;
            this.cachePrefetchBeforeExpiry = cachePrefetchBeforeExpiry == null
                    ? DnsServerOptions.DEFAULT_CACHE_PREFETCH_BEFORE_EXPIRY
                    : cachePrefetchBeforeExpiry;
            this.maxUdpPayloadBytes = maxUdpPayloadBytes == null ? DnsServerOptions.DEFAULT_UDP_PAYLOAD_BYTES
                    : maxUdpPayloadBytes;
            this.rateLimitPerSecond = rateLimitPerSecond == null ? 0 : rateLimitPerSecond;
            validate();
        }

        /**
         * Creates the default disabled DNS server options.
         *
         * @return default DNS options
         */
        private static Dns defaults() {
            return new Dns(null, null, null, null, null, null, null, null, null, null, null);
        }

        /**
         * Normalizes an optional DNS listener host.
         *
         * @param value configured listener host
         * @return trimmed host or the default listener host
         */
        private static String normalizeHost(final String value) {
            return value == null || value.isBlank() ? DEFAULT_HOST : value.trim();
        }

        /**
         * Validates every normalized DNS server option.
         */
        private void validate() {
            if (enabled && host.isBlank()) {
                throw new IllegalArgumentException("bus.fabric.dns.host is required when DNS is enabled");
            }
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("bus.fabric.dns.port must be in 1..65535");
            }
            if (cacheMaxEntries < 0) {
                throw new IllegalArgumentException("bus.fabric.dns.cache-max-entries must be non-negative");
            }
            if (cacheTtl.isNegative() || cacheTtl.isZero()) {
                throw new IllegalArgumentException("bus.fabric.dns.cache-ttl must be positive");
            }
            if (cacheServeStaleTtl.isNegative()) {
                throw new IllegalArgumentException("bus.fabric.dns.cache-serve-stale-ttl must be non-negative");
            }
            if (cachePrefetchBeforeExpiry.isNegative()) {
                throw new IllegalArgumentException("bus.fabric.dns.cache-prefetch-before-expiry must be non-negative");
            }
            if (!cachePrefetchBeforeExpiry.isZero() && cachePrefetchBeforeExpiry.compareTo(cacheTtl) >= 0) {
                throw new IllegalArgumentException(
                        "bus.fabric.dns.cache-prefetch-before-expiry must be shorter than bus.fabric.dns.cache-ttl");
            }
            if (maxUdpPayloadBytes < 512 || maxUdpPayloadBytes > 65535) {
                throw new IllegalArgumentException("bus.fabric.dns.max-udp-payload-bytes must be in 512..65535");
            }
            if (rateLimitPerSecond < 0) {
                throw new IllegalArgumentException("bus.fabric.dns.rate-limit-per-second must be non-negative");
            }
        }

    }

    /**
     * Validates the endpoint.
     *
     * @param enabled whether the feature is enabled
     * @param host    network host
     * @param port    network port
     * @param path    configured path
     * @param name    logical name
     */
    private static void validateEndpoint(boolean enabled, String host, int port, String path, String name) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("bus.fabric." + name + ".port must be in 1..65535");
        }
        if (enabled && (host == null || host.isBlank())) {
            throw new IllegalArgumentException("bus.fabric." + name + ".host is required when enabled");
        }
        if (enabled && path != null && path.isBlank()) {
            throw new IllegalArgumentException("bus.fabric." + name + ".path is required when enabled");
        }
    }

}
