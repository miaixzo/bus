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
package org.miaixz.bus.vortex;

import java.util.Map;

import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.Specifics;
import org.miaixz.bus.vortex.strategy.QualifierStrategy;
import org.miaixz.bus.vortex.strategy.RequestStrategy;
import org.miaixz.bus.vortex.strategy.VettingStrategy;

import lombok.Getter;
import lombok.Setter;

/**
 * A central repository for constants defining the gateway's public API contract and for binding configuration
 * properties.
 * <p>
 * This class holds constants for request parameter names, HTTP header names, and URI path prefixes that form the
 * "language" of the gateway. It also contains nested static classes that serve as models for type-safe configuration
 * binding from application properties (e.g., YAML or .properties files) using {@code @ConfigurationProperties}.
 *
 * @author Kimi Liu
 * @see QualifierStrategy
 * @see VettingStrategy
 * @see RequestStrategy
 * @since Java 21+
 */
public class Args extends Specifics {

    /**
     * Creates an empty gateway argument contract holder.
     */
    public Args() {
    }

    /**
     * The parameter name for selecting the logical namespace used to resolve registry assets.
     */
    public static final String NAMESPACE = "namespace";

    /**
     * The HTTP header name for identifying the client channel (e.g., "web", "app", "mobile").
     */
    public static final String X_REMOTE_CHANNEL = "x_remote_channel";

    /**
     * The base URI path for standard RESTful API requests.
     */
    public static final String REST_PATH_PREFIX = "/router/rest";

    /**
     * The base URI path for requests to be forwarded to a Message Queue.
     */
    public static final String MQ_PATH_PREFIX = "/router/mq";

    /**
     * The base URI path for requests to the MCP (Miaixz Communication Protocol) hub.
     */
    public static final String MCP_PATH_PREFIX = "/router/mcp";

    /**
     * The base URI path for standard gRPC requests.
     */
    public static final String GRPC_PATH_PREFIX = "/router/grpc";

    /**
     * The base URI path for WebSocket connections.
     */
    public static final String WS_PATH_PREFIX = "/router/ws";

    /**
     * The base URI path for Large Language Model (LLM) proxy requests.
     */
    public static final String LLM_PATH_PREFIX = "/router/llm";

    /**
     * The base URI path for custom (CST) requests.
     */
    public static final String CST_PATH_PREFIX = "/router/cst";

    /**
     * The base URI path for CAS (Central Authentication Service) requests.
     */
    public static final String CAS_PATH_PREFIX = "/router/cas";

    /**
     * A constant for a default API version, e.g., "1.0".
     */
    public static final String DEFAULT_VERSION = "1.0";

    /**
     * Route mode for HTTP assets.
     */
    public static final int MODE_HTTP = 1;

    /**
     * Route mode for MQ assets.
     */
    public static final int MODE_MQ = 2;

    /**
     * Route mode for MCP assets.
     */
    public static final int MODE_MCP = 3;

    /**
     * Route mode for gRPC assets.
     */
    public static final int MODE_GRPC = 4;

    /**
     * Route mode for WebSocket assets.
     */
    public static final int MODE_WS = 5;

    /**
     * Route mode for LLM assets.
     */
    public static final int MODE_LLM = 6;

    /**
     * Pre-built mapping table for mode to router key. Using static map for O(1) lookup instead of switch expression
     * evaluation on every request.
     */
    public static final Map<Integer, String> MODE_TO_ROUTER = Map.of(
            MODE_HTTP,
            Protocol.HTTP.getName(),
            MODE_MQ,
            Protocol.MQ.getName(),
            MODE_MCP,
            Protocol.MCP.getName(),
            MODE_GRPC,
            Protocol.GRPC.getName(),
            MODE_WS,
            Protocol.WS.getName(),
            MODE_LLM,
            "llm");

    /**
     * Checks if the given path is a RESTful API proxy request path.
     *
     * @param path The URL path string to check.
     * @return {@code true} if the path starts with the REST prefix, {@code false} otherwise.
     */
    public static boolean isRestRequest(String path) {
        return path.startsWith(REST_PATH_PREFIX);
    }

    /**
     * Checks if the given path is an MCP (Miaixz Communication Protocol) proxy request path.
     *
     * @param path The URL path string to check.
     * @return {@code true} if the path starts with the MCP prefix, {@code false} otherwise.
     */
    public static boolean isMcpRequest(String path) {
        return path.startsWith(MCP_PATH_PREFIX);
    }

    /**
     * Checks if the given path is a Message Queue (MQ) proxy request path.
     *
     * @param path The URL path string to check.
     * @return {@code true} if the path starts with the MQ prefix, {@code false} otherwise.
     */
    public static boolean isMqRequest(String path) {
        return path.startsWith(MQ_PATH_PREFIX);
    }

    /**
     * Checks if the given path is a WebSocket connection request path.
     *
     * @param path The URL path string to check.
     * @return {@code true} if the path starts with the WebSocket prefix, {@code false} otherwise.
     */
    public static boolean isWsRequest(String path) {
        return path.startsWith(WS_PATH_PREFIX);
    }

    /**
     * Checks if the given path is a custom (CST) request path.
     *
     * @param path The URL path string to check.
     * @return {@code true} if the path starts with the CST prefix, {@code false} otherwise.
     */
    public static boolean isCstRequest(String path) {
        return path.startsWith(CST_PATH_PREFIX);
    }

    /**
     * Checks if the given path is a CAS (Central Authentication Service) request path.
     *
     * @param path The URL path string to check.
     * @return {@code true} if the path starts with the CAS prefix, {@code false} otherwise.
     */
    public static boolean isCasRequest(String path) {
        return path.startsWith(CAS_PATH_PREFIX);
    }

    /**
     * Checks if the given path is a gRPC request path.
     *
     * @param path The URL path string to check.
     * @return {@code true} if the path starts with the gRPC prefix, {@code false} otherwise.
     */
    public static boolean isGrpcRequest(String path) {
        return path.startsWith(GRPC_PATH_PREFIX);
    }

    /**
     * Checks if the given path is an LLM (Large Language Model) proxy request path.
     *
     * @param path The URL path string to check.
     * @return {@code true} if the path starts with the LLM prefix, {@code false} otherwise.
     */
    public static boolean isLlmRequest(String path) {
        return path.startsWith(LLM_PATH_PREFIX);
    }

    /**
     * Checks if the given path matches any of the known gateway prefixes (REST, MCP, MQ, WS, CAS, CST, gRPC, or LLM).
     * This method combines the individual check methods for convenience.
     *
     * @param path The URL path string to check.
     * @return {@code true} if the path matches any known gateway prefix, {@code false} otherwise.
     */
    public static boolean isKnownRequest(String path) {
        return isRestRequest(path) || isMcpRequest(path) || isMqRequest(path) || isWsRequest(path) || isCasRequest(path)
                || isCstRequest(path) || isGrpcRequest(path) || isLlmRequest(path);
    }

    /**
     * A configuration properties model for rate limiting settings, typically bound from application properties under a
     * prefix like {@code vortex.limit}.
     */
    @Getter
    @Setter
    public static class Limit {

        /**
         * Creates an empty rate-limit configuration.
         */
        public Limit() {
        }

        /**
         * Whether rate limiting is globally enabled.
         */
        private boolean enabled;
    }

}
