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

import lombok.Getter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Immutable fabric communication service properties.
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
     * DNS service settings and cache integration options.
     */
    private final Dns dns;

    /**
     * Composes the independently configurable TCP socket, WebSocket, and DNS service settings.
     *
     * @param enabled   whether fabric integration is enabled
     * @param socket    socket server options
     * @param websocket WebSocket server options
     * @param dns       DNS integration options
     */
    public FabricProperties(@DefaultValue("false") boolean enabled, @DefaultValue Socket socket,
            @DefaultValue WebSocket websocket, @DefaultValue Dns dns) {
        this.enabled = enabled;
        this.socket = socket == null ? new Socket() : socket;
        this.websocket = websocket == null ? new WebSocket() : websocket;
        this.dns = dns == null ? new Dns() : dns;
    }

    /**
     * Socket server options.
     *
     * @param enabled whether the feature is enabled
     * @param host    network host
     * @param port    network port
     */
    public record Socket(boolean enabled, String host, int port) {

        /**
         * Creates socket defaults.
         */
        public Socket() {
            this(true, "0.0.0.0", 7890);
        }

        /**
         * Validates socket options.
         */
        public Socket {
            validateEndpoint(enabled, host, port, null, "socket");
        }

        /**
         * Exposes the network interface on which the TCP socket server listens.
         *
         * @return listening host
         */
        public String getHost() {
            return host;
        }

        /**
         * Exposes the TCP socket server listening port.
         *
         * @return listening port
         */
        public int getPort() {
            return port;
        }
    }

    /**
     * WebSocket server options.
     *
     * @param enabled whether the feature is enabled
     * @param host    network host
     * @param port    network port
     * @param path    configured path
     */
    public record WebSocket(boolean enabled, String host, int port, String path) {

        /**
         * Creates WebSocket defaults.
         */
        public WebSocket() {
            this(false, "0.0.0.0", 7891, "/ws");
        }

        /**
         * Validates WebSocket options.
         */
        public WebSocket {
            validateEndpoint(enabled, host, port, path, "websocket");
        }

        /**
         * Exposes the network interface on which the WebSocket server listens.
         *
         * @return listening host
         */
        public String getHost() {
            return host;
        }

        /**
         * Exposes the WebSocket server listening port.
         *
         * @return listening port
         */
        public int getPort() {
            return port;
        }

        /**
         * Exposes the HTTP upgrade path accepted by the WebSocket server.
         *
         * @return upgrade path
         */
        public String getPath() {
            return path;
        }
    }

    /**
     * DNS integration options.
     *
     * @param enabled whether the feature is enabled
     * @param cache   cache settings
     */
    public record Dns(boolean enabled, boolean cache) {

        /**
         * Creates DNS defaults.
         */
        public Dns() {
            this(false, true);
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
