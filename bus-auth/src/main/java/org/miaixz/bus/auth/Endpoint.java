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
package org.miaixz.bus.auth;

import java.util.HashSet;
import java.util.Set;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.tls.TlsClientAuth;
import org.miaixz.bus.core.net.tls.TlsVersion;
import org.miaixz.bus.fabric.UnoUrl;

/**
 * Describes one immutable protocol endpoint without executing network operations or retaining credentials.
 * <p>
 * The endpoint combines a parsed Bus URL, the real transport, optional HTTP method, supported authentication method
 * identifiers, and TLS requirements. Credential material is resolved separately from the identifiers declared here.
 * </p>
 *
 * @param url               parsed endpoint URL
 * @param transport         actual network transport
 * @param method            required HTTP method for HTTP-family transports, otherwise empty
 * @param authentication    supported endpoint authentication method identifiers
 * @param minimumTlsVersion optional minimum TLS version
 * @param clientAuth        TLS client-certificate authentication requirement
 * @author Kimi Liu
 */
public record Endpoint(UnoUrl url, Transport transport, Optional<Http.Method> method,
        Set<Authentication> authentication, Optional<TlsVersion> minimumTlsVersion, TlsClientAuth clientAuth) {

    /**
     * Creates and validates an immutable endpoint declaration.
     *
     * @param url               parsed endpoint URL
     * @param transport         actual network transport
     * @param method            HTTP method container
     * @param authentication    supported authentication identifiers
     * @param minimumTlsVersion optional minimum TLS version
     * @param clientAuth        TLS client-certificate authentication requirement
     * @throws IllegalArgumentException if a component is {@code null}, authentication contains {@code null}, or the
     *                                  HTTP method does not match the transport family
     */
    public Endpoint {
        Assert.notNull(url, "Endpoint URL must not be null");
        Assert.notNull(transport, "Endpoint transport must not be null");
        Assert.notNull(method, "Endpoint HTTP method container must not be null");
        Assert.notNull(authentication, "Endpoint authentication methods must not be null");
        Assert.notNull(minimumTlsVersion, "Endpoint minimum TLS version container must not be null");
        Assert.notNull(clientAuth, "Endpoint TLS client authentication must not be null");
        final Set<Authentication> methods = new HashSet<>(authentication.size());
        for (Authentication item : authentication) {
            methods.add(Assert.notNull(item, "Endpoint authentication method must not be null"));
        }
        authentication = Set.copyOf(methods);
        method = Optional.ofNullable(method.getOrNull());
        minimumTlsVersion = Optional.ofNullable(minimumTlsVersion.getOrNull());
        final boolean http = transport == Transport.HTTP || transport == Transport.HTTPS;
        Assert.isTrue(
                http == method.isPresent(),
                "HTTP and HTTPS endpoints require a method and non-HTTP transports prohibit one");
    }

    /**
     * Enumerates the actual transport families used by authentication endpoints.
     *
     * @author Kimi Liu
     */
    public enum Transport {

        /**
         * Cleartext Hypertext Transfer Protocol.
         */
        HTTP,

        /**
         * Hypertext Transfer Protocol over TLS.
         */
        HTTPS,

        /**
         * Raw Transmission Control Protocol.
         */
        TCP,

        /**
         * Raw protocol transport protected by TLS.
         */
        TLS,

        /**
         * User Datagram Protocol.
         */
        UDP,

        /**
         * RADIUS protected by TLS according to the applicable RADIUS transport profile.
         */
        RADIUS_TLS

    }

    /**
     * Represents an extensible registered endpoint authentication method identifier.
     * <p>
     * Values preserve their registered spelling. The constants cover methods emitted by the supplied endpoint adapters;
     * decoders may retain other non-blank registered values for standards extension processing.
     * </p>
     *
     * @param value registered authentication method value
     * @author Kimi Liu
     */
    public record Authentication(String value) {

        /**
         * Declares that the endpoint requires no authentication.
         */
        public static final Authentication NONE = new Authentication(Normal.NONE);

        /**
         * HTTP Basic authentication scheme.
         */
        public static final Authentication BASIC = new Authentication("basic");

        /**
         * HTTP Bearer authentication scheme.
         */
        public static final Authentication BEARER = new Authentication("bearer");

        /**
         * OAuth client secret sent with HTTP Basic authentication.
         */
        public static final Authentication CLIENT_SECRET_BASIC = new Authentication("client_secret_basic");

        /**
         * OAuth client secret sent in the form-encoded request body.
         */
        public static final Authentication CLIENT_SECRET_POST = new Authentication("client_secret_post");

        /**
         * OAuth private-key JWT client assertion authentication.
         */
        public static final Authentication PRIVATE_KEY_JWT = new Authentication("private_key_jwt");

        /**
         * OAuth PKI mutual-TLS client authentication.
         */
        public static final Authentication TLS_CLIENT_AUTH = new Authentication("tls_client_auth");

        /**
         * OAuth self-signed certificate mutual-TLS client authentication.
         */
        public static final Authentication SELF_SIGNED_TLS_CLIENT_AUTH = new Authentication(
                "self_signed_tls_client_auth");

        /**
         * Creates a registered authentication method identifier without changing its spelling.
         *
         * @param value non-blank registered value
         * @throws IllegalArgumentException if the value is blank
         */
        public Authentication {
            Assert.notBlank(value, "Endpoint authentication value must not be blank");
        }

    }

}
