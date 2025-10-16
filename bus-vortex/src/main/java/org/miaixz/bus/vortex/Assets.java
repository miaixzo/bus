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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Represents a configured API endpoint, acting as a blueprint for routing and processing requests in the Vortex
 * gateway.
 * <p>
 * This class encapsulates all the necessary information for the gateway to handle an incoming request and forward it to
 * the appropriate downstream service. It includes details for service discovery, network addressing, request
 * transformation, security policies, and load balancing.
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
     * A unique identifier for this API asset, used for caching and equality checks.
     */
    private String id;

    /**
     * A human-readable name for the API asset, used for display and logging purposes.
     */
    private String name;

    /**
     * An optional URL or identifier for an icon representing this asset, typically for use in administrative UIs.
     */
    private String icon;

    /**
     * The hostname or IP address of the downstream service. Example: {@code api.downstream.com} or {@code 10.0.0.5}.
     */
    private String host;

    /**
     * The base path on the downstream service, which is prefixed to the final request URI. Example: {@code /v2/api}.
     */
    private String path;

    /**
     * The network port of the downstream service. Example: {@code 8080}.
     */
    private Integer port;

    /**
     * The specific endpoint path on the downstream service. This is combined with the host, port, and base path to form
     * the final target URL. Example: {@code /users/{id}}.
     */
    private String url;

    /**
     * The logical API method name defined by the gateway's contract (e.g., {@code user.getProfile}). This is distinct
     * from the HTTP method ({@code type}).
     */
    private String method;

    /**
     * Defines the routing mechanism or protocol used to forward the request.
     * <ul>
     * <li>{@code 1}: HTTP/HTTPS</li>
     * <li>{@code 2}: Message Queue (e.g., RabbitMQ, Kafka)</li>
     * <li>{@code 3}: Server-Sent Events (SSE)</li>
     * <li>{@code 4}: Standard I/O (STDIO) - for invoking local command-line tools</li>
     * <li>{@code 5}: OpenAPI discovery</li>
     * <li>{@code 6}: Streamable HTTP (for large data streaming)</li>
     * </ul>
     */
    private Integer mode;

    /**
     * Specifies the HTTP method to be used when forwarding the request (if applicable).
     * <ul>
     * <li>{@code 1}: GET</li>
     * <li>{@code 2}: POST</li>
     * <li>{@code 3}: HEAD</li>
     * <li>{@code 4}: PUT</li>
     * <li>{@code 5}: PATCH</li>
     * <li>{@code 6}: DELETE</li>
     * <li>{@code 7}: OPTIONS</li>
     * <li>{@code 8}: TRACE</li>
     * </ul>
     */
    private Integer type;

    /**
     * Specifies whether an authentication token is required for this API call.
     * <ul>
     * <li>{@code 0}: No validation required.</li>
     * <li>{@code 1}: Authentication token is required.</li>
     * </ul>
     */
    private Integer token;

    /**
     * Specifies whether the request must be cryptographically signed and which signature validation scheme to apply.
     */
    private Integer sign;

    /**
     * Defines the required authorization scope (e.g., OAuth2 scope like {@code read:profile}) that the provided token
     * must possess to access this API.
     */
    private Integer scope;

    /**
     * The number of automatic retry attempts if the initial request to the downstream service fails with a transient
     * error.
     */
    private Integer retries;

    /**
     * The maximum time in milliseconds to wait for a response from the downstream service before timing out.
     */
    private Integer timeout;

    /**
     * The load balancing strategy to use when multiple downstream service instances are available (e.g., round-robin,
     * least-connections).
     */
    private Integer balance;

    /**
     * In a weighted load balancing scenario, this value determines the proportion of traffic this service instance
     * should receive relative to others.
     */
    private Integer weight;

    /**
     * For non-HTTP routing modes (e.g., {@code STDIO}), this field can hold command-line arguments for the target
     * process.
     */
    private String args;

    /**
     * For {@code STDIO} routing mode, this specifies the executable command or script to be run.
     */
    private String command;

    /**
     * A flexible JSON string for storing any additional, custom configuration or metadata related to this asset.
     */
    private String metadata;

    /**
     * Defines a set of firewall rules or policies to be applied to this request, such as rate limiting or IP
     * blacklisting.
     */
    private Integer firewall;

    /**
     * The specific version of the API endpoint (e.g., {@code v1}, {@code 2.5.0}), used for version-based routing.
     */
    private String version;

    /**
     * A detailed, human-readable description of what this API asset does.
     */
    private String description;

    /**
     * Compares this Assets object with the specified object for equality. The comparison is based on the {@code id}
     * field.
     *
     * @param o The object to compare with.
     * @return {@code true} if the objects are equal (have the same id), {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (null == o || getClass() != o.getClass())
            return false;
        Assets assets = (Assets) o;
        return id.equals(assets.id);
    }

    /**
     * Returns a hash code value for the object. This hash code is based on the {@code id} field.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
