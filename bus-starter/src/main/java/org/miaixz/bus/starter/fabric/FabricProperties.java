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
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;

import org.miaixz.bus.spring.GeniusBuilder;

/**
 * Configuration properties for fabric communication services.
 * <p>
 * This class binds properties from the configuration file (e.g., {@code application.yml}) under the prefix
 * {@code bus.fabric}. Protocol-specific settings are grouped under nested sections such as {@code socket},
 * {@code websocket}, and {@code dns}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@ConfigurationProperties(prefix = GeniusBuilder.FABRIC)
public class FabricProperties {

    /**
     * Constructs a new FabricProperties instance.
     */
    public FabricProperties() {
        // No initialization required.
    }

    /**
     * Socket server settings.
     */
    private Socket socket = new Socket();

    /**
     * WebSocket server settings.
     */
    private WebSocket websocket = new WebSocket();

    /**
     * DNS settings reserved for fabric resolver integration.
     */
    private Dns dns = new Dns();

    /**
     * Socket server properties.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @Getter
    @Setter
    public static class Socket {

        /**
         * Constructs a new Socket instance.
         */
        public Socket() {
            // No initialization required.
        }

        /**
         * Whether the socket server is enabled.
         */
        private boolean enabled = true;

        /**
         * Host on which the socket server will listen.
         */
        private String host = "0.0.0.0";

        /**
         * Port on which the socket server will listen.
         */
        private int port = 7890;

    }

    /**
     * WebSocket server properties.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @Getter
    @Setter
    public static class WebSocket {

        /**
         * Constructs a new WebSocket instance.
         */
        public WebSocket() {
            // No initialization required.
        }

        /**
         * Whether the WebSocket server is enabled.
         */
        private boolean enabled;

        /**
         * Host on which the WebSocket server will listen.
         */
        private String host = "0.0.0.0";

        /**
         * Port on which the WebSocket server will listen.
         */
        private int port = 7891;

        /**
         * WebSocket upgrade path.
         */
        private String path = "/ws";

    }

    /**
     * DNS properties reserved for fabric resolver integration.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @Getter
    @Setter
    public static class Dns {

        /**
         * Constructs a new Dns instance.
         */
        public Dns() {
            // No initialization required.
        }

        /**
         * Whether fabric DNS integration is enabled.
         */
        private boolean enabled;

        /**
         * Whether DNS cache support is enabled.
         */
        private boolean cache = true;

    }

}
