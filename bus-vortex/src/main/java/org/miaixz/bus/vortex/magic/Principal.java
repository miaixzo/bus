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
package org.miaixz.bus.vortex.magic;

import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.provider.AuthorizeProvider;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Represents a unified security principal, encapsulating the credential to be validated by an
 * {@link AuthorizeProvider}.
 * <p>
 * This class acts as a standard data transfer object for passing different types of credentials (e.g., bearer tokens,
 * API keys) along with their context to the authorization service. It allows the core authorization logic to be
 * agnostic of the specific credential type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
public class Principal {

    /**
     * Creates an empty principal.
     */
    public Principal() {
    }

    /**
     * The type of the credential being presented.
     * <ul>
     * <li>{@link Consts#ONE} (1): Represents a bearer token (e.g., JWT, Opaque Token).</li>
     * <li>{@link Consts#TWO} (2): Represents a static API Key.</li>
     * </ul>
     * This type is used by the {@link AuthorizeProvider} to dispatch to the correct validation logic.
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
    protected Integer channel;

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
    protected Context context;

}
