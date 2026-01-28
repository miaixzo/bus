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
package org.miaixz.bus.vortex;

import java.util.Map;

import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.vortex.strategy.QualifierStrategy;
import org.miaixz.bus.vortex.strategy.RequestStrategy;
import org.miaixz.bus.vortex.strategy.VettingStrategy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
public class Args {

    /**
     * The mandatory parameter name for the logical API method to be invoked (e.g., "user.getProfile").
     */
    public static final String METHOD = "method";

    /**
     * The parameter name for specifying the desired response format (e.g., "json", "xml").
     */
    public static final String FORMAT = "format";

    /**
     * The parameter name for specifying the version of the requested API method (e.g., "v1", "1.0.0").
     */
    public static final String VERSION = "v";

    /**
     * The parameter name for the request signature, used for validation and integrity checks.
     */
    public static final String SIGN = "sign";

    /**
     * The parameter name for the request timestamp (milliseconds since epoch), used for replay attack prevention.
     */
    public static final String TIMESTAMP = "timestamp";

    /**
     * The parameter name for the client's API key, used for identification and signature validation.
     */
    public static final String API_KEY = "api_key";

    /**
     * The HTTP header name for the bearer access token (e.g., JWT).
     */
    public static final String X_ACCESS_TOKEN = "X-Access-Token";

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
     * Pre-built mapping table for mode to router key. Using static map for O(1) lookup instead of switch expression
     * evaluation on every request.
     */
    public static final Map<Integer, String> MODE_TO_ROUTER = Map.of(
            1,
            Protocol.HTTP.getName(),
            2,
            Protocol.MQ.getName(),
            3,
            Protocol.MCP.getName(),
            4,
            Protocol.GRPC.getName(),
            5,
            Protocol.WS.getName(),
            6,
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
         * Whether rate limiting is globally enabled.
         */
        private boolean enabled;
    }

}
