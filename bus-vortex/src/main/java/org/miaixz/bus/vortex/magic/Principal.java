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
package org.miaixz.bus.vortex.magic;

import org.miaixz.bus.vortex.Assets;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.miaixz.bus.vortex.Context;

/**
 * Represents an authenticated principal with authentication credentials and request context.
 * <p>
 * This class encapsulates authentication information for multi-tenant API gateway scenarios, including credential type,
 * tenant identifier, source channel, and associated request context. Used in OAuth 2.0, API Key, and license key
 * authentication flows.
 * <p>
 * <b>Thread Safety:</b> Immutable after construction (channel and context are final).
 * <p>
 * <b>Serialization:</b> Key field is transient to exclude runtime tenant context from persistent storage.
 *
 * @author Justubborn
 * @since Java 17+
 * @see Context Request context information
 * @see Assets Resource configuration and permissions
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@RequiredArgsConstructor
public class Principal {

    /**
     * Authentication credential type identifier.
     * <p>
     * Defines the type of authentication credential for validation and processing:
     * <ul>
     * <li>{@code 1} = Access Token (OAuth 2.0 Bearer Token, JWT, Opaque Token)</li>
     * <li>{@code 2} = API Key (Static service credentials)</li>
     * </ul>
     * <p>
     * Determines:
     * <ul>
     * <li>Token validation strategy and issuer verification</li>
     * <li>Header extraction method (Authorization vs custom headers)</li>
     * <li>Expiration, refresh, and revocation policies</li>
     * <li>JWT signature verification or API key lookup</li>
     * </ul>
     * <p>
     * <b>Standards:</b>
     * <ul>
     * <li>RFC 6749 (OAuth 2.0 Framework) - Access Tokens</li>
     * <li>RFC 6750 (Bearer Token Usage)</li>
     * <li>RFC 7519 (JSON Web Token - JWT)</li>
     * </ul>
     */
    protected Integer type;

    /**
     * Raw authentication credential value.
     * <p>
     * Contains the actual credential string based on {@code type}:
     * <ul>
     * <li><b>Type 1 (Token):</b> Complete bearer token including prefix
     * 
     * <pre>
     * Authorization: Bearer eyJhbGciOiJSUzI1NiJ9...
     * </pre>
     * 
     * </li>
     * <li><b>Type 2 (API Key):</b> Static API secret
     * 
     * <pre>
     * X-API-Key: sk_live_51ABC123xyz...
     * </pre>
     * 
     * </li>
     * <li><b>Type 3 (License):</b> Product activation key
     * 
     * <pre>
     * License: LIC-2025-PRO-001-ABCDEF
     * </pre>
     * 
     * </li>
     * </ul>
     * <p>
     * <b>Security Requirements:</b>
     * <ul>
     * <li><b>Storage:</b> Encrypt at rest using strong encryption (AES-256)</li>
     * <li><b>Logging:</b> Mask or truncate in logs (show first/last 4 chars only)</li>
     * <li><b>Transmission:</b> Always use TLS 1.2+ with strong ciphers</li>
     * <li><b>Memory:</b> Clear from memory after use (overwrite with zeros)</li>
     * </ul>
     * <p>
     * <b>Validation:</b> Must not be null or empty. Length and format validated by type-specific validators.
     */
    protected String value;

    /**
     * Source channel identifier.
     * <p>
     * Identifies the originating client platform or integration channel:
     * <ul>
     * <li>Mobile applications (iOS, Android)</li>
     * <li>Web applications (SPA, traditional web)</li>
     * <li>Server-to-server integrations</li>
     * <li>Third-party platforms (WeChat, DingTalk, Slack)</li>
     * <li>IoT devices and embedded systems</li>
     * </ul>
     * <p>
     * Enables channel-specific policies:
     * <ul>
     * <li>Differentiated rate limiting and quotas</li>
     * <li>Platform-specific feature flags</li>
     * <li>Client version compatibility checks</li>
     * <li>Analytics and usage tracking</li>
     * <li>A/B testing and canary deployments</li>
     * </ul>
     * <p>
     * <b>Immutable:</b> Final field set during authentication, cannot be modified during request lifecycle.
     * <p>
     * <b>Typical Values:</b> 1=Web, 2=iOS, 3=Android, 4=Server, 5=ThirdParty, etc.
     */
    protected final Integer channel;

    /**
     * Request context information.
     * <p>
     * Contains runtime request context data populated during the authentication and request processing pipeline:
     * <ul>
     * <li><b>Request Metadata:</b> Request ID, timestamps, IP address, user agent</li>
     * <li><b>Session Data:</b> Session attributes, temporary state</li>
     * <li><b>Routing Information:</b> Target service, route parameters, query params</li>
     * <li><b>Processing Context:</b> Middleware execution state, correlation IDs</li>
     * <li><b>Business Context:</b> Tenant-specific configuration, feature flags</li>
     * </ul>
     * <p>
     * <b>Population Timing:</b>
     * <ul>
     * <li>Pre-authentication: Initial request metadata capture</li>
     * <li>Authentication: Token validation results and user context</li>
     * <li>Authorization: Permission checks and policy decisions</li>
     * <li>Routing: Service discovery and load balancing context</li>
     * </ul>
     * <p>
     * <b>Usage Scenarios:</b>
     * <ul>
     * <li>Request tracing and distributed logging</li>
     * <li>Cross-service context propagation</li>
     * <li>Dynamic routing and service mesh integration</li>
     * <li>Middleware chain execution context</li>
     * <li>Request/response transformation</li>
     * </ul>
     * <p>
     * <b>Immutable:</b> Final field established during request lifecycle initialization.
     * <p>
     * <b>Thread Safety:</b> Designed for single request processing; should not be shared across threads.
     * <p>
     * <b>Performance Note:</b> Context should be lightweight; avoid storing large payloads or complex objects.
     *
     * @see Context Request context structure and lifecycle
     */
    protected final Context context;

}
