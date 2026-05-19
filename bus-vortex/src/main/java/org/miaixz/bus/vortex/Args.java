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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Specifics;
import org.miaixz.bus.vortex.strategy.RequestStrategy;
import org.miaixz.bus.vortex.strategy.qualifier.McpQualifierStrategy;
import org.miaixz.bus.vortex.strategy.qualifier.RestQualifierStrategy;
import org.miaixz.bus.vortex.strategy.vetting.McpVettingStrategy;
import org.miaixz.bus.vortex.strategy.vetting.RestVettingStrategy;

/**
 * A central repository for constants defining the gateway's public API contract and for binding configuration
 * properties.
 * <p>
 * This class holds constants for request parameter names, HTTP header names, and URI path prefixes that form the
 * "language" of the gateway. It also contains nested static classes that serve as models for type-safe configuration
 * binding from application properties (e.g., YAML or .properties files) using {@code @ConfigurationProperties}.
 *
 * @see RestQualifierStrategy
 * @see McpQualifierStrategy
 * @see RestVettingStrategy
 * @see McpVettingStrategy
 * @see RequestStrategy
 * @author Kimi Liu
 * @since Java 21+
 */
public class Args extends Specifics {

    /**
     * Creates an empty gateway argument contract holder.
     */
    public Args() {
        // No initialization required.
    }

    /**
     * The parameter name for selecting the logical namespace used to resolve registry assets.
     */
    public static final String NAMESPACE = "x_namespace_id";

    /**
     * The parameter name for selecting the application-specific route scope.
     */
    public static final String APP_ID = "x_app_id";

    /**
     * The parameter name for selecting the numeric or legacy textual registry type scope.
     */
    public static final String TYPE = "x_type";

    /**
     * The HTTP header name for identifying the client channel (e.g., "web", "app", "mobile").
     */
    public static final String X_REMOTE_CHANNEL = "x_remote_channel";

    /**
     * Signature timestamp header name.
     */
    public static final String X_TIMESTAMP = "X-Timestamp";

    /**
     * Signature nonce header name.
     */
    public static final String X_NONCE = "X-Nonce";

    /**
     * Signature value header name.
     */
    public static final String X_SIGN = "X-Sign";

    /**
     * MCP protocol-version header name.
     */
    public static final String MCP_PROTOCOL_VERSION = "MCP-Protocol-Version";

    /**
     * MCP session-id header name.
     */
    public static final String MCP_SESSION_ID = "Mcp-Session-Id";

    /**
     * MCP last-event-id header name.
     */
    public static final String LAST_EVENT_ID = "Last-Event-ID";

    /**
     * The base URI path for standard RESTful API requests.
     */
    public static final String REST_PATH_PREFIX = Symbol.SLASH + "router" + Symbol.SLASH + "rest";

    /**
     * The base URI path for requests to be forwarded to a Message Queue.
     */
    public static final String MQ_PATH_PREFIX = Symbol.SLASH + "router" + Symbol.SLASH + "mq";

    /**
     * The base URI path for requests to the MCP.
     */
    public static final String MCP_PATH_PREFIX = Symbol.SLASH + "router" + Symbol.SLASH + "mcp";

    /**
     * The base URI path for standard gRPC requests.
     */
    public static final String GRPC_PATH_PREFIX = Symbol.SLASH + "router" + Symbol.SLASH + "grpc";

    /**
     * The base URI path for WebSocket connections.
     */
    public static final String WS_PATH_PREFIX = Symbol.SLASH + "router" + Symbol.SLASH + "ws";

    /**
     * The base URI path for Large Language Model (LLM) proxy requests.
     */
    public static final String LLM_PATH_PREFIX = Symbol.SLASH + "router" + Symbol.SLASH + "llm";

    /**
     * The base URI path for custom (CST) requests.
     */
    public static final String CST_PATH_PREFIX = Symbol.SLASH + "router" + Symbol.SLASH + "cst";

    /**
     * A constant for a default API version, e.g., "1.0".
     */
    public static final String DEFAULT_VERSION = "1.0";

    /**
     * Route protocol for HTTP assets.
     */
    public static final int PROTOCOL_HTTP = 1;

    /**
     * Route protocol for MQ assets.
     */
    public static final int PROTOCOL_MQ = 2;

    /**
     * Route protocol for MCP assets.
     */
    public static final int PROTOCOL_MCP = 3;

    /**
     * Route protocol for gRPC assets.
     */
    public static final int PROTOCOL_GRPC = 4;

    /**
     * Route protocol for WebSocket assets.
     */
    public static final int PROTOCOL_WS = 5;

    /**
     * Route protocol for LLM assets.
     */
    public static final int PROTOCOL_LLM = 6;

    /**
     * Checks if the given path is a RESTful API proxy request path.
     *
     * @param path The URL path string to check.
     * @return {@code true} if the path starts with the REST prefix, {@code false} otherwise.
     */
    public static boolean isRestRequest(String path) {
        return isPathPrefix(path, REST_PATH_PREFIX);
    }

    /**
     * Checks if the given path is a Model Context Protocol (MCP) proxy request path.
     *
     * @param path The URL path string to check.
     * @return {@code true} if the path starts with the MCP prefix, {@code false} otherwise.
     */
    public static boolean isMcpRequest(String path) {
        return isPathPrefix(path, MCP_PATH_PREFIX);
    }

    /**
     * Checks if the given path is a Message Queue (MQ) proxy request path.
     *
     * @param path The URL path string to check.
     * @return {@code true} if the path starts with the MQ prefix, {@code false} otherwise.
     */
    public static boolean isMqRequest(String path) {
        return isPathPrefix(path, MQ_PATH_PREFIX);
    }

    /**
     * Checks if the given path is a WebSocket connection request path.
     *
     * @param path The URL path string to check.
     * @return {@code true} if the path starts with the WebSocket prefix, {@code false} otherwise.
     */
    public static boolean isWsRequest(String path) {
        return isPathPrefix(path, WS_PATH_PREFIX);
    }

    /**
     * Checks if the given path is a custom (CST) request path.
     *
     * @param path The URL path string to check.
     * @return {@code true} if the path starts with the CST prefix, {@code false} otherwise.
     */
    public static boolean isCstRequest(String path) {
        return isPathPrefix(path, CST_PATH_PREFIX);
    }

    /**
     * Checks if the given path is a gRPC request path.
     *
     * @param path The URL path string to check.
     * @return {@code true} if the path starts with the gRPC prefix, {@code false} otherwise.
     */
    public static boolean isGrpcRequest(String path) {
        return isPathPrefix(path, GRPC_PATH_PREFIX);
    }

    /**
     * Checks if the given path is an LLM (Large Language Model) proxy request path.
     *
     * @param path The URL path string to check.
     * @return {@code true} if the path starts with the LLM prefix, {@code false} otherwise.
     */
    public static boolean isLlmRequest(String path) {
        return isPathPrefix(path, LLM_PATH_PREFIX);
    }

    /**
     * Checks if the given path matches any of the known gateway prefixes (REST, MCP, MQ, WS, CST, gRPC, or LLM). This
     * method combines the individual check methods for convenience.
     *
     * @param path The URL path string to check.
     * @return {@code true} if the path matches any known gateway prefix, {@code false} otherwise.
     */
    public static boolean isKnownRequest(String path) {
        return isRestRequest(path) || isMcpRequest(path) || isMqRequest(path) || isWsRequest(path) || isCstRequest(path)
                || isGrpcRequest(path) || isLlmRequest(path);
    }

    /**
     * Checks whether a request parameter is a gateway control parameter that must not be forwarded downstream.
     * <p>
     * Matching is case-insensitive because clients may send these gateway fields with different key casing.
     *
     * @param name request parameter name
     * @return {@code true} when the parameter should be removed before forwarding
     */
    public static boolean isForwardingControlParameter(String name) {
        return name != null && (METHOD.equalsIgnoreCase(name) || VERSION.equalsIgnoreCase(name)
                || SIGN.equalsIgnoreCase(name) || FORMAT.equalsIgnoreCase(name));
    }

    /**
     * Checks whether the path equals one public route prefix or starts with that prefix on a path-segment boundary.
     *
     * @param path   request path
     * @param prefix public route prefix
     * @return {@code true} when the request belongs to the prefix
     */
    private static boolean isPathPrefix(String path, String prefix) {
        return path != null && (path.equals(prefix) || path.startsWith(prefix + Symbol.SLASH));
    }

    /**
     * A configuration properties model for rate limiting settings, typically bound from application properties under a
     * prefix like {@code vortex.limit}.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @Getter
    @Setter
    @SuperBuilder
    @AllArgsConstructor
    public static class Limit {

        /**
         * Creates an empty rate-limit configuration.
         */
        public Limit() {
            // No initialization required.
        }

        /**
         * Whether rate limiting is globally enabled.
         */
        private boolean enabled;

    }

}
