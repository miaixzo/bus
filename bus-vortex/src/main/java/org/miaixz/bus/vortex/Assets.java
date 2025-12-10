/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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

import java.util.Objects;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Represents a configured API resource, acting as a blueprint for routing and processing requests in the Vortex
 * gateway.
 * <p>
 * This class is a data object that encapsulates all necessary metadata for the gateway to handle an incoming request
 * and forward it to the appropriate downstream service. It includes details for service discovery, network addressing,
 * security policies, and protocol selection.
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
     * The unique identifier for this asset. Used for internal caching, management, and equality checks.
     */
    private String id;

    /**
     * The human-readable name of this asset, primarily used for display in user interfaces and for logging.
     */
    private String name;

    /**
     * An optional URL or identifier for an icon associated with this asset, intended for use in administrative UIs.
     */
    private String icon;

    /**
     * The hostname or IP address of the downstream service to which requests for this asset will be routed. Example:
     * {@code "api.downstream.com"} or {@code "10.0.0.5"}.
     */
    private String host;

    /**
     * The base path on the downstream service. This path segment is prefixed to the final request URI. Example:
     * {@code "/v2/api"}.
     */
    private String path;

    /**
     * The network port of the downstream service. Example: {@code 8080}.
     */
    private Integer port;

    /**
     * The specific endpoint path on the downstream service. This is combined with other fields to form the final target
     * URL. Example: {@code "/users/{id}"}.
     */
    private String url;

    /**
     * The logical API method name defined by the gateway's contract (e.g., {@code "user.getProfile"}). This is used for
     * service lookup and is distinct from the HTTP method.
     */
    private String method;

    /**
     * Defines the routing mechanism or protocol to be used for this asset.
     * <ul>
     * <li>{@code 1}: HTTP/HTTPS - Standard reverse proxying.</li>
     * <li>{@code 2}: Message Queue - Forwards the request as a message to a broker.</li>
     * <li>{@code 3}: Server-Sent Events (SSE) - Forwards to a streaming endpoint.</li>
     * <li>{@code 4}: Standard I/O (STDIO) - Invokes a local command-line tool.</li>
     * </ul>
     */
    private Integer mode;

    /**
     * Specifies the HTTP method (verb) required to access this asset (e.g., GET, POST).
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
     * A flag indicating whether an authentication token is required to access this asset.
     * <ul>
     * <li>{@code 0}: No token required (public access).</li>
     * <li>{@code 1}: A valid token is required.</li>
     * </ul>
     */
    private Integer token;

    /**
     * A flag indicating whether the request and response for this asset are expected to be cryptographically signed.
     * This is used to enable or disable strategies like {@link org.miaixz.bus.vortex.strategy.VettingStrategy}.
     */
    private Integer sign;

    /**
     * Defines the required authorization scope (e.g., an OAuth2 scope like {@code "read:profile"}) that the provided
     * credential must possess to access this asset.
     */
    private Integer scope;

    /**
     * The number of automatic retry attempts to be made if the initial request to the downstream service fails with a
     * transient error.
     */
    private Integer retries;

    /**
     * The maximum time in milliseconds to wait for a response from the downstream service before the request is
     * considered timed out.
     */
    @Builder.Default
    private Integer timeout = 60;

    /**
     * The load balancing strategy to use when multiple downstream service instances are available for this asset (e.g.,
     * round-robin, least-connections).
     */
    private Integer balance;

    /**
     * In a weighted load balancing scenario, this value determines the proportion of traffic this service instance
     * should receive relative to others.
     */
    private Integer weight;

    /**
     * A space-separated string representing the command and its arguments to be executed when this asset is started in
     * STDIO mode (mode 4). Example: {@code "python /path/to/script.py --port 8000"}
     */
    private String command;

    /**
     * A JSON string representing a map of environment variables to be set for the process when this asset is started in
     * STDIO mode (mode 4). This is the recommended way to pass sensitive information. Example:
     * {@code "{\"API_KEY\":\"your-secret-key\",\"DB_HOST\":\"localhost\"}"}
     */
    private String env;

    /**
     * A flexible JSON string for storing any additional, custom configuration or metadata related to this asset that is
     * not covered by other fields.
     */
    private String metadata;

    /**
     * Defines the security policy or firewall level to be applied to this asset.
     * <ul>
     * <li>{@code 0}: Public - No authentication or special checks required.</li>
     * <li>{@code 1}: Secure - Requires authentication (e.g., token or API key).</li>
     * <li>{@code 2}: Licensed - Requires both authentication and a valid license check.</li>
     * </ul>
     */
    private Integer firewall;

    /**
     * The specific version of the API endpoint (e.g., {@code "v1"}, {@code "2.5.0"}), used for version-based routing
     * and asset lookup.
     */
    private String version;

    /**
     * A detailed, human-readable description of what this API asset does, intended for display in UIs or documentation.
     */
    private String description;

    /**
     * Compares this Assets object with the specified object for equality. The comparison is based solely on the
     * {@code id} field.
     *
     * @param o The object to compare with.
     * @return {@code true} if the objects are equal (have the same id), {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Assets assets = (Assets) o;
        return Objects.equals(id, assets.id);
    }

    /**
     * Returns a hash code value for the object. This hash code is based solely on the {@code id} field.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
