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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Carries an immutable snapshot of non-sensitive data for one authentication invocation.
 * <p>
 * This context is distinct from Fabric transport context and Bus entity query context. It may carry correlation,
 * locale, caller-defined immutable attributes, and observed network metadata, but it must never contain credentials,
 * authorization codes, tokens, or other secret material.
 * </p>
 *
 * @param requestId            stable correlation identifier for this invocation
 * @param attributes           immutable provider-neutral invocation attributes
 * @param locale               locale selected for framework management messages, never protocol parameter localization
 * @param network              immutable observed network metadata
 * @param clientId             optional protocol client identifier verified by the owning client authenticator
 * @param authenticatedSubject optional subject already authenticated by the external request boundary
 * @param authentication       optional facts describing the same external authentication event as the authenticated
 *                             subject
 * @author Kimi Liu
 */
public record Context(RequestId requestId, Attributes attributes, Locale locale, Network network,
        Optional<String> clientId, Optional<Subject> authenticatedSubject, Optional<Authentication> authentication) {

    /**
     * Creates one immutable invocation context.
     *
     * @param requestId            stable invocation correlation identifier
     * @param attributes           provider-neutral invocation attributes
     * @param locale               management-message locale
     * @param network              observed network metadata
     * @param clientId             optional verified protocol client identifier
     * @param authenticatedSubject optional externally authenticated stable subject
     * @param authentication       optional authentication event facts paired with {@code authenticatedSubject}
     * @throws IllegalArgumentException if any component is {@code null}
     */
    public Context {
        Assert.notNull(requestId, "Authentication request id must not be null");
        Assert.notNull(attributes, "Authentication context attributes must not be null");
        Assert.notNull(locale, "Authentication context locale must not be null");
        Assert.notNull(network, "Authentication context network must not be null");
        Assert.notNull(clientId, "Authentication context client identifier container must not be null");
        final String verifiedClient = clientId.getOrNull();
        if (verifiedClient != null) {
            Assert.notBlank(verifiedClient, "Authentication context client identifier must not be blank");
        }
        clientId = Optional.ofNullable(verifiedClient);
        Assert.notNull(authenticatedSubject, "Authentication context subject container must not be null");
        authenticatedSubject = Optional.ofNullable(authenticatedSubject.getOrNull());
        Assert.notNull(authentication, "Authentication event container must not be null");
        authentication = Optional.ofNullable(authentication.getOrNull());
        Assert.isTrue(
                authenticatedSubject.isPresent() == authentication.isPresent(),
                "Authenticated subject and authentication event must be present together");
    }

    /**
     * Creates a derived context carrying the client identifier verified by a protocol authenticator.
     *
     * @param verifiedClientId non-blank verified client identifier
     * @return immutable derived context preserving every other invocation component
     */
    public Context withClientId(final String verifiedClientId) {
        return new Context(requestId, attributes, locale, network,
                Optional.of(Assert.notBlank(verifiedClientId, "Verified client identifier must not be blank")),
                authenticatedSubject, authentication);
    }

    /**
     * Identifies one framework invocation across Registry, worker ports, parsers, and protocol layers.
     *
     * @param value non-blank request correlation value
     * @author Kimi Liu
     */
    public record RequestId(String value) {

        /**
         * Creates a request identifier.
         *
         * @param value non-blank correlation value
         * @throws IllegalArgumentException if the value is blank
         */
        public RequestId {
            Assert.notBlank(value, "Authentication request id must not be blank");
        }

    }

    /**
     * Wraps an immutable provider-neutral attribute object for framework extensions.
     *
     * @param value attributes that contain no credential, code, or token material
     * @author Kimi Liu
     */
    public record Attributes(JsonValue.ObjectValue value) {

        /**
         * Creates a detached immutable attributes snapshot.
         *
         * @param value provider-neutral attributes
         * @throws IllegalArgumentException if the value is {@code null}
         */
        public Attributes {
            Assert.notNull(value, "Authentication context attribute value must not be null");
            value = new JsonValue.ObjectValue(value.values());
        }

    }

    /**
     * Carries network information observed at the external request boundary.
     *
     * @param remoteAddress remote network address as observed by the trusted boundary
     * @param userAgent     optional original user-agent field value
     * @param connection    optional connection snapshot supplied by a trusted connection-oriented transport boundary
     * @author Kimi Liu
     */
    public record Network(String remoteAddress, Optional<String> userAgent, Optional<Connection> connection) {

        /**
         * Creates an immutable network metadata snapshot.
         *
         * @param remoteAddress non-blank remote network address
         * @param userAgent     optional original user-agent field value
         * @param connection    optional connection snapshot
         * @throws IllegalArgumentException if the remote address is blank or an optional container is {@code null}
         */
        public Network {
            Assert.notBlank(remoteAddress, "Authentication remote address must not be blank");
            Assert.notNull(userAgent, "Authentication user-agent container must not be null");
            userAgent = Optional.ofNullable(userAgent.getOrNull());
            Assert.notNull(connection, "Authentication connection container must not be null");
            connection = Optional.ofNullable(connection.getOrNull());
        }

        /**
         * Carries the stable non-secret identity and effective transport of one connection-oriented invocation.
         * <p>
         * LDAP server adapters reuse the identifier for every PDU on one connection and replace the transport with
         * {@link Endpoint.Transport#TLS} after a successful StartTLS handshake. The value is transport metadata, not an
         * authentication Session or a socket resource, and cannot be used to perform network I/O.
         * </p>
         *
         * @param id        stable connection identifier assigned by the trusted transport boundary
         * @param transport effective connection transport for the current invocation
         * @author Kimi Liu
         */
        public record Connection(String id, Endpoint.Transport transport) {

            /**
             * Creates an immutable connection snapshot.
             *
             * @param id        stable non-blank connection identifier
             * @param transport effective transport
             * @throws IllegalArgumentException if the identifier is blank or the transport is {@code null}
             */
            public Connection {
                Assert.notBlank(id, "Authentication connection identifier must not be blank");
                Assert.notNull(transport, "Authentication connection transport must not be null");
            }

        }

    }

    /**
     * Carries non-secret facts established by the external authentication boundary for the current subject.
     * <p>
     * The active root session supplies the authentication time and session identifier. The optional authentication
     * context class and ordered method references use the StringOrURI grammar shared by OpenID Connect and SAML, but
     * this value remains framework invocation state rather than a protocol message.
     * </p>
     *
     * @param session                    active framework session established by the authentication event
     * @param authenticationContextClass optional authentication context class reference
     * @param authenticationMethods      ordered, duplicate-free authentication method references
     * @author Kimi Liu
     */
    public record Authentication(Session session, Optional<String> authenticationContextClass,
            List<String> authenticationMethods) {

        /**
         * Validates and freezes one authentication event snapshot.
         *
         * @param session                    active framework session
         * @param authenticationContextClass optional StringOrURI authentication context class
         * @param authenticationMethods      ordered StringOrURI authentication method references
         * @throws IllegalArgumentException if a component is {@code null}, the session is not active, or a reference
         *                                  violates StringOrURI syntax or uniqueness
         */
        public Authentication {
            Assert.notNull(session, "Authentication event Session must not be null");
            Assert.isTrue(session.state() == Session.State.ACTIVE, "Authentication event Session must be active");
            Assert.notNull(authenticationContextClass, "Authentication context class container must not be null");
            final String contextClass = authenticationContextClass.getOrNull();
            if (contextClass != null) {
                validateStringOrUri(contextClass, "Authentication context class");
            }
            authenticationContextClass = Optional.ofNullable(contextClass);
            Assert.notNull(authenticationMethods, "Authentication method references must not be null");
            final LinkedHashSet<String> unique = new LinkedHashSet<>(authenticationMethods.size());
            for (String method : authenticationMethods) {
                validateStringOrUri(method, "Authentication method reference");
                Assert.isTrue(unique.add(method), "Authentication method references must not contain duplicates");
            }
            authenticationMethods = List.copyOf(new ArrayList<>(unique));
        }

        /**
         * Validates the JWT StringOrURI grammar without normalizing the original lexical value.
         *
         * @param value candidate non-blank value
         * @param label safe semantic label used in validation diagnostics
         * @throws IllegalArgumentException if the value is blank or contains a colon without forming a valid URI
         */
        private static void validateStringOrUri(final String value, final String label) {
            Assert.notBlank(value, label + " must not be blank");
            if (!value.contains(Symbol.COLON)) {
                return;
            }
            try {
                new URI(value);
            } catch (URISyntaxException exception) {
                throw new IllegalArgumentException(label + " must satisfy JWT StringOrURI syntax", exception);
            }
        }

    }

}
