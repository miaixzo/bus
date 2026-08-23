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
package org.miaixz.bus.auth.source.protocol.oauth2.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.Scheme.Options;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Protocol;

/**
 * Holds immutable deployment options for one standards-based OAuth 2.x client Source.
 *
 * @param authorizationEndpoint               optional browser authorization endpoint
 * @param tokenEndpoint                       optional token endpoint shared by all supported grants
 * @param introspectionEndpoint               optional RFC 7662 introspection endpoint
 * @param revocationEndpoint                  optional RFC 7009 revocation endpoint
 * @param deviceAuthorizationEndpoint         optional RFC 8628 device authorization endpoint
 * @param authorizationServerMetadataEndpoint optional RFC 8414 metadata endpoint
 * @param expectedIssuer                      optional trusted authorization server issuer
 * @param clientId                            authorization server-issued client identifier
 * @param redirectUris                        exact registered redirect URI values
 * @param clientAuthenticationMethod          selected endpoint client authentication method
 * @param clientCredential                    optional external client-secret reference
 * @param pkceRequired                        whether every browser authorization request must use explicit PKCE
 * @param dpopRequired                        whether issued access tokens must be sender-constrained with DPoP
 * @author Kimi Liu
 */
public record OAuth2ClientOptions(Optional<Endpoint> authorizationEndpoint, Optional<Endpoint> tokenEndpoint,
        Optional<Endpoint> introspectionEndpoint, Optional<Endpoint> revocationEndpoint,
        Optional<Endpoint> deviceAuthorizationEndpoint, Optional<Endpoint> authorizationServerMetadataEndpoint,
        Optional<String> expectedIssuer, String clientId, Set<String> redirectUris,
        Endpoint.Authentication clientAuthenticationMethod, Optional<Credential.Reference> clientCredential,
        boolean pkceRequired, boolean dpopRequired) implements Options<OAuth2ClientOptions> {

    /**
     * Normalizes optional containers and enforces transport, registration, credential, and security invariants.
     */
    public OAuth2ClientOptions {
        authorizationEndpoint = normalize(authorizationEndpoint, "authorization endpoint");
        tokenEndpoint = normalize(tokenEndpoint, "token endpoint");
        introspectionEndpoint = normalize(introspectionEndpoint, "introspection endpoint");
        revocationEndpoint = normalize(revocationEndpoint, "revocation endpoint");
        deviceAuthorizationEndpoint = normalize(deviceAuthorizationEndpoint, "device authorization endpoint");
        authorizationServerMetadataEndpoint = normalize(
                authorizationServerMetadataEndpoint,
                "authorization server metadata endpoint");
        requireEndpoint(authorizationEndpoint.getOrNull(), Http.Method.GET, "Authorization endpoint");
        requireEndpoint(tokenEndpoint.getOrNull(), Http.Method.POST, "Token endpoint");
        requireEndpoint(introspectionEndpoint.getOrNull(), Http.Method.POST, "Introspection endpoint");
        requireEndpoint(revocationEndpoint.getOrNull(), Http.Method.POST, "Revocation endpoint");
        requireEndpoint(deviceAuthorizationEndpoint.getOrNull(), Http.Method.POST, "Device authorization endpoint");
        requireEndpoint(
                authorizationServerMetadataEndpoint.getOrNull(),
                Http.Method.GET,
                "Authorization server metadata endpoint");
        if (List.of(
                authorizationEndpoint,
                tokenEndpoint,
                introspectionEndpoint,
                revocationEndpoint,
                deviceAuthorizationEndpoint,
                authorizationServerMetadataEndpoint).stream().noneMatch(Optional::isPresent)) {
            throw new ValidateException("OAuth 2.x client options require at least one endpoint");
        }

        expectedIssuer = normalizeIssuer(expectedIssuer);
        if (authorizationServerMetadataEndpoint.isPresent() && expectedIssuer.isEmpty()) {
            throw new ValidateException("OAuth 2.x authorization server metadata requires a trusted expected issuer");
        }
        Assert.notNull(clientId, "OAuth 2.x client identifier must not be null");
        requireVisibleAscii(clientId, "OAuth 2.x client identifier");
        redirectUris = redirectUris(redirectUris);
        if (authorizationEndpoint.isPresent() && redirectUris.isEmpty()) {
            throw new ValidateException("OAuth 2.x authorization Sources require a registered redirect URI");
        }

        Assert.notNull(clientAuthenticationMethod, "OAuth 2.x client authentication method must not be null");
        Assert.notNull(clientCredential, "OAuth 2.x client credential container must not be null");
        final Credential.Reference credential = clientCredential.getOrNull();
        if (Endpoint.Authentication.NONE.equals(clientAuthenticationMethod)) {
            if (credential != null) {
                throw new ValidateException("OAuth 2.x public client authentication prohibits a client credential");
            }
            if (introspectionEndpoint.isPresent()) {
                throw new ValidateException("OAuth 2.x token introspection requires an authenticated client");
            }
        } else if (Endpoint.Authentication.CLIENT_SECRET_BASIC.equals(clientAuthenticationMethod)
                || Endpoint.Authentication.CLIENT_SECRET_POST.equals(clientAuthenticationMethod)) {
            if (credential == null || credential.type() != Credential.Type.CLIENT_SECRET) {
                throw new ValidateException(
                        "OAuth 2.x client-secret authentication requires a client-secret reference");
            }
            if (tokenEndpoint.isEmpty() && introspectionEndpoint.isEmpty() && revocationEndpoint.isEmpty()
                    && deviceAuthorizationEndpoint.isEmpty()) {
                throw new ValidateException(
                        "OAuth 2.x client-secret authentication requires an authenticated endpoint");
            }
        } else {
            throw new ValidateException(
                    "OAuth 2.x generic client does not implement the selected authentication method");
        }
        clientCredential = Optional.ofNullable(credential);
        requireAuthentication(tokenEndpoint.getOrNull(), clientAuthenticationMethod, "Token endpoint");
        requireAuthentication(introspectionEndpoint.getOrNull(), clientAuthenticationMethod, "Introspection endpoint");
        requireAuthentication(revocationEndpoint.getOrNull(), clientAuthenticationMethod, "Revocation endpoint");
        requireAuthentication(
                deviceAuthorizationEndpoint.getOrNull(),
                clientAuthenticationMethod,
                "Device authorization endpoint");
        if (pkceRequired && authorizationEndpoint.isEmpty()) {
            throw new ValidateException("OAuth 2.x PKCE policy requires an authorization endpoint");
        }
        if (dpopRequired) {
            throw new ValidateException(
                    "OAuth 2.x client options cannot enable DPoP without a complete token transport contract");
        }
    }

    /**
     * Creates a one-shot Builder for a standards-based OAuth 2.x client.
     *
     * @param clientId public authorization-server-issued client identifier
     * @return mutable build-scoped client Options builder
     */
    public static Builder builder(final String clientId) {
        return new Builder(clientId);
    }

    /**
     * Normalizes one Bus optional endpoint container.
     *
     * @param value optional endpoint container
     * @param label safe endpoint label
     * @return normalized container
     */
    private static Optional<Endpoint> normalize(final Optional<Endpoint> value, final String label) {
        Assert.notNull(value, "OAuth 2.x " + label + " container must not be null");
        return Optional.ofNullable(value.getOrNull());
    }

    /**
     * Requires a present protocol endpoint to use fragment-free HTTPS and the exact method.
     *
     * @param endpoint optional endpoint value
     * @param method   required HTTP method
     * @param label    safe endpoint label
     */
    private static void requireEndpoint(final Endpoint endpoint, final Http.Method method, final String label) {
        if (endpoint == null) {
            return;
        }
        if (endpoint.transport() != Endpoint.Transport.HTTPS || endpoint.method().isEmpty()
                || endpoint.method().getOrNull() != method) {
            throw new ValidateException(label + " must use HTTPS " + method.name());
        }
        if (endpoint.url().toUri().getRawFragment() != null) {
            throw new ValidateException(label + " must not contain a fragment");
        }
    }

    /**
     * Ensures the selected client authentication method is declared by a present authenticated endpoint.
     *
     * @param endpoint optional endpoint value
     * @param method   selected client authentication method
     * @param label    safe endpoint label
     */
    private static void requireAuthentication(
            final Endpoint endpoint,
            final Endpoint.Authentication method,
            final String label) {
        if (endpoint != null && !endpoint.authentication().contains(method)) {
            throw new ValidateException(label + " does not declare the selected client authentication method");
        }
    }

    /**
     * Validates and copies exact registered redirect URI strings.
     *
     * @param values redirect URI set
     * @return immutable insertion-ordered URI set
     */
    private static Set<String> redirectUris(final Set<String> values) {
        Assert.notNull(values, "OAuth 2.x redirect URI set must not be null");
        final Set<String> result = new LinkedHashSet<>();
        for (String value : values) {
            Assert.notBlank(value, "OAuth 2.x redirect URI must not be blank");
            final URI uri = uri(value, "OAuth 2.x redirect URI");
            if (!uri.isAbsolute() || uri.getRawFragment() != null) {
                throw new ValidateException("OAuth 2.x redirect URI must be absolute and fragment-free");
            }
            result.add(value);
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * Normalizes an optional trusted issuer and validates each present RFC 8414 issuer URL.
     *
     * @param value optional issuer container
     * @return normalized empty container or the unchanged validated issuer
     * @throws IllegalArgumentException if the container or its present value is invalid
     * @throws ValidateException        if a present issuer is not a secure issuer URL
     */
    private static Optional<String> normalizeIssuer(final Optional<String> value) {
        Assert.notNull(value, "OAuth 2.x expected issuer container must not be null");
        final String issuerValue = value.getOrNull();
        if (issuerValue == null) {
            return Optional.empty();
        }
        Assert.notBlank(issuerValue, "OAuth 2.x expected issuer must not be blank");
        final URI issuer = uri(issuerValue, "OAuth 2.x expected issuer");
        if (!Protocol.HTTPS.name.equalsIgnoreCase(issuer.getScheme()) || issuer.getRawQuery() != null
                || issuer.getRawFragment() != null || issuer.getHost() == null) {
            throw new ValidateException("OAuth 2.x expected issuer must be an HTTPS URL without query or fragment");
        }
        return Optional.of(issuerValue);
    }

    /**
     * Parses a URI without introducing a framework-specific URI type.
     *
     * @param value URI lexical value
     * @param label safe value label
     * @return parsed URI
     */
    private static URI uri(final String value, final String label) {
        try {
            return new URI(value);
        } catch (URISyntaxException exception) {
            throw new ValidateException(label + " is not a valid URI", exception);
        }
    }

    /**
     * Requires a non-empty RFC 6749 visible-ASCII string.
     *
     * @param value candidate wire value
     * @param label safe value label
     */
    private static void requireVisibleAscii(final String value, final String label) {
        if (value.isEmpty()) {
            throw new ValidateException(label + " must not be empty");
        }
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            if (character < 0x20 || character > 0x7e) {
                throw new ValidateException(label + " contains a character outside VSCHAR");
            }
        }
    }

    /**
     * Returns a redacted endpoint URL or an empty marker.
     *
     * @param value optional endpoint
     * @return safe endpoint text
     */
    private static String endpoint(final Optional<Endpoint> value) {
        final Endpoint endpoint = value.getOrNull();
        return endpoint == null ? "empty" : endpoint.url().redact();
    }

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<OAuth2ClientOptions> type() {
        return OAuth2ClientOptions.class;
    }

    /**
     * Returns this already immutable OAuth 2.x client configuration snapshot.
     *
     * @return this immutable options value
     */
    @Override
    public OAuth2ClientOptions snapshot() {
        return this;
    }

    /**
     * Returns a diagnostic summary without client, credential, or redirect URI values.
     *
     * @return redacted options text
     */
    @Override
    public String toString() {
        return "OAuth2ClientOptions[authorizationEndpoint=" + endpoint(authorizationEndpoint) + ", tokenEndpoint="
                + endpoint(tokenEndpoint) + ", introspectionEndpoint=" + endpoint(introspectionEndpoint)
                + ", revocationEndpoint=" + endpoint(revocationEndpoint) + ", deviceAuthorizationEndpoint="
                + endpoint(deviceAuthorizationEndpoint) + ", authorizationServerMetadataEndpoint="
                + endpoint(authorizationServerMetadataEndpoint) + ", expectedIssuer=" + expectedIssuer
                + ", clientId=[REDACTED], redirectUris=[REDACTED]" + ", clientAuthenticationMethod="
                + clientAuthenticationMethod.value() + ", clientCredential=[REDACTED], pkceRequired=" + pkceRequired
                + ", dpopRequired=" + dpopRequired + Symbol.BRACKET_RIGHT;
    }

    /**
     * Collects OAuth 2.x client deployment values before immutable canonical validation.
     *
     * @author Kimi Liu
     */
    public static class Builder {

        /**
         * Public OAuth client identifier.
         */
        private final String clientId;
        /**
         * Registered redirect URI values in caller order.
         */
        private final Set<String> redirectUris = new LinkedHashSet<>();
        /**
         * Optional authorization endpoint.
         */
        private Optional<Endpoint> authorizationEndpoint = Optional.empty();
        /**
         * Optional token endpoint.
         */
        private Optional<Endpoint> tokenEndpoint = Optional.empty();
        /**
         * Optional token introspection endpoint.
         */
        private Optional<Endpoint> introspectionEndpoint = Optional.empty();
        /**
         * Optional token revocation endpoint.
         */
        private Optional<Endpoint> revocationEndpoint = Optional.empty();
        /**
         * Optional device authorization endpoint.
         */
        private Optional<Endpoint> deviceAuthorizationEndpoint = Optional.empty();
        /**
         * Optional authorization-server metadata endpoint.
         */
        private Optional<Endpoint> metadataEndpoint = Optional.empty();
        /**
         * Optional exact authorization-server issuer.
         */
        private Optional<String> expectedIssuer = Optional.empty();
        /**
         * Selected client authentication method.
         */
        private Endpoint.Authentication authentication = Endpoint.Authentication.NONE;
        /**
         * Optional external client credential reference.
         */
        private Optional<Credential.Reference> credential = Optional.empty();
        /**
         * Whether authorization requests require PKCE.
         */
        private boolean pkceRequired;
        /**
         * Whether protected-resource calls require DPoP.
         */
        private boolean dpopRequired;

        /**
         * Creates a build-scoped collector for one public client identifier.
         *
         * @param clientId public OAuth client identifier
         */
        public Builder(final String clientId) {
            this.clientId = Assert.notBlank(clientId, "OAuth 2.x client identifier must not be blank");
        }

        /**
         * Selects the authorization endpoint.
         *
         * @param value immutable authorization endpoint
         * @return this builder
         */
        public Builder authorizationEndpoint(final Endpoint value) {
            authorizationEndpoint = Optional.of(Assert.notNull(value, "Authorization endpoint must not be null"));
            return this;
        }

        /**
         * Selects the token endpoint.
         *
         * @param value immutable token endpoint
         * @return this builder
         */
        public Builder tokenEndpoint(final Endpoint value) {
            tokenEndpoint = Optional.of(Assert.notNull(value, "Token endpoint must not be null"));
            return this;
        }

        /**
         * Selects the token introspection endpoint.
         *
         * @param value immutable introspection endpoint
         * @return this builder
         */
        public Builder introspectionEndpoint(final Endpoint value) {
            introspectionEndpoint = Optional.of(Assert.notNull(value, "Introspection endpoint must not be null"));
            return this;
        }

        /**
         * Selects the token revocation endpoint.
         *
         * @param value immutable revocation endpoint
         * @return this builder
         */
        public Builder revocationEndpoint(final Endpoint value) {
            revocationEndpoint = Optional.of(Assert.notNull(value, "Revocation endpoint must not be null"));
            return this;
        }

        /**
         * Selects the device authorization endpoint.
         *
         * @param value immutable device authorization endpoint
         * @return this builder
         */
        public Builder deviceAuthorizationEndpoint(final Endpoint value) {
            deviceAuthorizationEndpoint = Optional
                    .of(Assert.notNull(value, "Device authorization endpoint must not be null"));
            return this;
        }

        /**
         * Selects the authorization-server metadata endpoint.
         *
         * @param value immutable metadata endpoint
         * @return this builder
         */
        public Builder metadataEndpoint(final Endpoint value) {
            metadataEndpoint = Optional.of(Assert.notNull(value, "Metadata endpoint must not be null"));
            return this;
        }

        /**
         * Selects the exact issuer expected in authorization-server metadata.
         *
         * @param value exact expected issuer
         * @return this builder
         */
        public Builder expectedIssuer(final String value) {
            expectedIssuer = Optional.of(Assert.notBlank(value, "Expected issuer must not be blank"));
            return this;
        }

        /**
         * Adds one exact registered redirect URI.
         *
         * @param value exact redirect URI lexical value
         * @return this builder
         */
        public Builder redirectUri(final String value) {
            redirectUris.add(Assert.notBlank(value, "Redirect URI must not be blank"));
            return this;
        }

        /**
         * Adds exact registered redirect URIs in caller order.
         *
         * @param values registered redirect URI values
         * @return this builder
         */
        public Builder redirectUris(final Set<String> values) {
            Assert.notNull(values, "Redirect URI values must not be null");
            values.forEach(this::redirectUri);
            return this;
        }

        /**
         * Selects a public client without a recoverable client credential.
         *
         * @return this builder
         */
        public Builder publicClient() {
            authentication = Endpoint.Authentication.NONE;
            credential = Optional.empty();
            return this;
        }

        /**
         * Selects one confidential-client authentication method and external credential reference.
         *
         * @param method    registered endpoint authentication method
         * @param reference external recoverable credential reference
         * @return this builder
         */
        public Builder clientSecret(final Endpoint.Authentication method, final Credential.Reference reference) {
            authentication = Assert.notNull(method, "Client authentication method must not be null");
            credential = Optional.of(Assert.notNull(reference, "Client credential reference must not be null"));
            return this;
        }

        /**
         * Selects whether authorization requests must use PKCE.
         *
         * @param value required-state selection
         * @return this builder
         */
        public Builder pkceRequired(final boolean value) {
            pkceRequired = value;
            return this;
        }

        /**
         * Selects whether protected requests must use DPoP.
         *
         * @param value required-state selection
         * @return this builder
         */
        public Builder dpopRequired(final boolean value) {
            dpopRequired = value;
            return this;
        }

        /**
         * Builds and canonically validates one immutable OAuth 2.x client Options value.
         *
         * @return immutable OAuth 2.x client Options
         */
        public OAuth2ClientOptions build() {
            return new OAuth2ClientOptions(authorizationEndpoint, tokenEndpoint, introspectionEndpoint,
                    revocationEndpoint, deviceAuthorizationEndpoint, metadataEndpoint, expectedIssuer, clientId,
                    redirectUris, authentication, credential, pkceRequired, dpopRequired);
        }

    }

}
