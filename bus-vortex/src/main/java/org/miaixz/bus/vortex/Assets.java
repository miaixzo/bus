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

import org.miaixz.bus.vortex.strategy.VettingStrategy;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Represents a configured API resource, acting as a blueprint for routing and processing requests in the Vortex
 * gateway.
 * <p>
 * This class encapsulates all necessary metadata for the gateway to handle an incoming request and forward it to the
 * appropriate downstream service. It includes details for service discovery, network addressing, security policies, and
 * protocol selection.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@RequiredArgsConstructor
public class Assets {

    /**
     * The unique identifier for this asset.
     * <p>
     * Used for internal caching, management, and equality checks.
     * </p>
     */
    private String id;

    /**
     * The human-readable name of this asset.
     * <p>
     * Primarily used for display in user interfaces, administrative panels, and system logging.
     * </p>
     */
    private String name;

    /**
     * An optional URL or identifier for an icon associated with this asset.
     * <p>
     * Intended for use in administrative dashboards or developer portals.
     * </p>
     */
    private String icon;

    /**
     * The hostname or IP address of the downstream service (upstream target).
     * <p>
     * Example: {@code "api.downstream.com"} or {@code "192.168.1.50"}.
     * </p>
     */
    private String host;

    /**
     * The network port of the downstream service.
     * <p>
     * Example: {@code 8080}.
     * </p>
     */
    private Integer port;

    /**
     * The context path on the downstream service.
     * <p>
     * This segment is prefixed to the URI when forwarding the request to the backend service. Example:
     * {@code "/v2/api"}.
     * </p>
     */
    private String path;

    /**
     * The gateway entry endpoint pattern.
     * <p>
     * Specifies the URL pattern on the gateway side that maps to this asset. Example: {@code "/users/{id}"}.
     * </p>
     */
    private String url;

    /**
     * The logical API method identifier.
     * <p>
     * A unique string key used for internal service lookup or logical binding, distinct from the HTTP method. Example:
     * {@code "user.getProfile"}.
     * </p>
     */
    private String method;

    /**
     * Upstream Interaction Mode.
     * <p>
     * All requests enter the gateway via HTTP, but this field defines how the gateway forwards or processes the request
     * upstream.
     * </p>
     * <ul>
     * <li>{@code 1}: <strong>HTTP/HTTPS Reverse Proxy</strong> - Traditional REST/RPC forwarding.</li>
     * <li>{@code 2}: <strong>Message Queue</strong> - Send payload to MQ broker (e.g., Kafka, RabbitMQ).</li>
     * <li>{@code 3}: <strong>Model Context Protocol (MCP)</strong> - AI tool calling protocol (JSON-RPC based).</li>
     * <li>{@code 4}: <strong>gRPC</strong> - High-performance RPC framework using Protocol Buffers.</li>
     * <li>{@code 5}: <strong>WebSocket</strong> - Bidirectional real-time communication over persistent
     * connections.</li>
     * </ul>
     */
    private Integer mode;

    /**
     * Streaming Output Flag.
     * <p>
     * Indicates whether the downstream response should be treated as a continuous stream (e.g., Server-Sent Events, AI
     * generation, Large File Downloads).
     * </p>
     * <ul>
     * <li>{@code 1}: <strong>Atomic (Default)</strong> <br>
     * Gateway buffers the full response before sending it to the client. Best for standard APIs.</li> *
     * <li>{@code 2}: <strong>Streaming</strong> <br>
     * Gateway disables buffering and flushes data chunks immediately. Read timeouts may need to be extended.</li>
     * </ul>
     */
    private Integer stream;

    /**
     * HTTP Request Method (Verb).
     * <p>
     * Specifies the allowed HTTP method for this asset.
     * </p>
     * <ul>
     * <li>{@code 1}: GET</li>
     * <li>{@code 2}: POST</li>
     * <li>{@code 3}: HEAD</li>
     * <li>{@code 4}: PUT</li>
     * <li>{@code 5}: PATCH</li>
     * <li>{@code 6}: DELETE</li>
     * <li>{@code 7}: OPTIONS</li>
     * <li>{@code 8}: TRACE</li>
     * <li>{@code 9}: CONNECT</li>
     * </ul>
     */
    private Integer type;

    /**
     * Access Control Policy / Security Level.
     * <p>
     * Specifies the authentication and authorization rigor required to access this asset.
     * </p>
     * <ul>
     * <li>{@code 0}: <strong>Anonymous</strong> <br>
     * Public access. No authentication required.</li>
     *
     * <li>{@code 1}: <strong>Authenticated</strong> <br>
     * Requires a valid identity token (e.g., JWT Login). Verifies <i>who</i> the user is.</li>
     *
     * <li>{@code 2}: <strong>Authorized</strong> <br>
     * Requires a valid token AND specific RBAC permissions/roles. Verifies <i>what</i> the user can do.</li>
     *
     * <li>{@code 3}: <strong>Strict/Licensed</strong> <br>
     * Requires Authentication, Authorization, AND a valid system license/entitlement check. Usually for enterprise
     * features.</li>
     * </ul>
     */
    private Integer policy;

    /**
     * Signature Verification Flag.
     * <p>
     * Indicates whether the request/response requires cryptographic signature validation. Used to enable strategies
     * like {@link VettingStrategy}.
     * </p>
     * <ul>
     * <li>{@code 0}: Disabled</li>
     * <li>{@code 1}: Enabled</li>
     * </ul>
     */
    private Integer sign;

    /**
     * OAuth2 Scope ID / Permission Level.
     * <p>
     * Defines the specific scope identifier (mapped from an integer) that the credential must possess.
     * </p>
     */
    private Integer scope;

    /**
     * Retry Policy.
     * <p>
     * The maximum number of automatic retry attempts for transient failures (e.g., network timeouts).
     * </p>
     */
    @Builder.Default
    private Integer retries = 3;

    /**
     * Request Timeout.
     * <p>
     * The maximum duration (in milliseconds) the gateway waits for a downstream response.
     * </p>
     */
    @Builder.Default
    private Integer timeout = 60;

    /**
     * Load Balancing Strategy.
     * <p>
     * Specifies the algorithm for distributing traffic among upstream instances. (e.g., Round-Robin, Least-Connections,
     * Hash).
     * </p>
     */
    private Integer balance;

    /**
     * Routing Weight.
     * <p>
     * Determines the traffic proportion for this asset relative to others in a weighted balancing scenario. Higher
     * values receive more traffic.
     * </p>
     */
    private Integer weight;

    /**
     * Execution Command (STDIO Mode).
     * <p>
     * The command line string (executable and arguments) used when {@code mode} is set to {@code 4}. <br>
     * Example: {@code "python script.py --verbose"}
     * </p>
     */
    private String command;

    /**
     * Environment Variables (STDIO Mode).
     * <p>
     * A JSON-serialized map of environment variables injected into the process when {@code mode} is {@code 4}. <br>
     * Example: {@code "{\"ENV\":\"production\"}"}
     * </p>
     */
    private String env;

    /**
     * Sort Order / Priority.
     * <p>
     * Controls the display order or matching priority. Lower numbers typically indicate higher priority.
     * </p>
     */
    private Integer sort;

    /**
     * API Version.
     * <p>
     * The version identifier for this asset (e.g., {@code "v1.0.0"}). Used for version negotiation and lifecycle
     * management.
     * </p>
     */
    private String version;

    /**
     * Custom Metadata.
     * <p>
     * A JSON string for storing extensible configuration attributes not covered by standard fields.
     * </p>
     */
    private String metadata;

    /**
     * Asset Description.
     * <p>
     * Detailed documentation of the asset's functionality, intended for developer portals (Swagger/OpenAPI).
     * </p>
     */
    private String description;

}
