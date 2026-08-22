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
package org.miaixz.bus.auth.protocol.oidc.client;

import static org.miaixz.bus.auth.Builder.ABSENT_VALUE;
import static org.miaixz.bus.auth.Builder.CONFIGURED_VALUE;

import java.util.LinkedHashSet;
import java.util.Set;

import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.Options;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2ClientOptions;
import org.miaixz.bus.auth.shared.jose.JwaAlgorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;

/**
 * Holds immutable deployment options for one OpenID Connect relying-party Source.
 * <p>
 * OAuth client identity, credentials, redirect URIs, and authorization/token endpoints remain owned by the composed
 * OAuth options. Discovery documents and JWK Sets are runtime data and are never stored in this registration model.
 * </p>
 *
 * @param oauth2Options            validated OAuth 2.x client options used by the relying party
 * @param discoveryEndpoint        required OpenID Provider Metadata endpoint
 * @param userInfoEndpoint         optional UserInfo endpoint
 * @param jwkSetEndpoint           required JWK Set endpoint
 * @param endSessionEndpoint       optional RP-Initiated Logout endpoint
 * @param idTokenSigningAlgorithms exact locally enabled ID Token JWS algorithms
 * @author Kimi Liu
 */
public record OpenIdClientOptions(OAuth2ClientOptions oauth2Options, Optional<Endpoint> discoveryEndpoint,
        Optional<Endpoint> userInfoEndpoint, Optional<Endpoint> jwkSetEndpoint, Optional<Endpoint> endSessionEndpoint,
        Set<JwaAlgorithm> idTokenSigningAlgorithms) implements Options<OpenIdClientOptions> {

    /**
     * Normalizes endpoint containers and enforces OIDC code-flow transport and algorithm invariants.
     *
     * @throws IllegalArgumentException if a required component, container, or set item is {@code null}
     * @throws ValidateException        if an endpoint, OAuth profile, or signing algorithm is unsupported or insecure
     */
    public OpenIdClientOptions {
        Assert.notNull(oauth2Options, "OpenID Connect OAuth 2.x client options must not be null");
        if (oauth2Options.authorizationEndpoint().isEmpty() || oauth2Options.tokenEndpoint().isEmpty()) {
            throw new ValidateException(
                    "OpenID Connect Authorization Code Flow requires OAuth authorization and token endpoints");
        }
        if (!oauth2Options.pkceRequired()) {
            throw new ValidateException("OpenID Connect client policy requires PKCE for Authorization Code Flow");
        }
        if (oauth2Options.expectedIssuer().isEmpty()) {
            throw new ValidateException("OpenID Connect Sources require a trusted expected issuer");
        }

        discoveryEndpoint = normalize(discoveryEndpoint, "discovery endpoint");
        userInfoEndpoint = normalize(userInfoEndpoint, "UserInfo endpoint");
        jwkSetEndpoint = normalize(jwkSetEndpoint, "JWK Set endpoint");
        endSessionEndpoint = normalize(endSessionEndpoint, "end-session endpoint");
        if (discoveryEndpoint.isEmpty() || jwkSetEndpoint.isEmpty()) {
            throw new ValidateException("OpenID Connect Sources require discovery and JWK Set endpoints");
        }
        requireEndpoint(discoveryEndpoint.getOrNull(), Endpoint.Authentication.NONE, "Discovery endpoint");
        requireEndpoint(userInfoEndpoint.getOrNull(), Endpoint.Authentication.BEARER, "UserInfo endpoint");
        requireEndpoint(jwkSetEndpoint.getOrNull(), Endpoint.Authentication.NONE, "JWK Set endpoint");
        requireEndpoint(endSessionEndpoint.getOrNull(), Endpoint.Authentication.NONE, "End-session endpoint");

        Assert.notNull(idTokenSigningAlgorithms, "OpenID Connect ID Token signing algorithm set must not be null");
        if (idTokenSigningAlgorithms.isEmpty()) {
            throw new ValidateException("OpenID Connect ID Token signing algorithm set must not be empty");
        }
        for (JwaAlgorithm algorithm : idTokenSigningAlgorithms) {
            Assert.notNull(algorithm, "OpenID Connect ID Token signing algorithm must not be null")
                    .require(JwaAlgorithm.Kind.SIGNATURE);
        }
        idTokenSigningAlgorithms = Set.copyOf(idTokenSigningAlgorithms);
    }

    /**
     * Creates a one-shot Builder around validated OAuth 2.x client Options.
     *
     * @param oauth2Options immutable OAuth 2.x client foundation
     * @return mutable build-scoped OpenID client Options builder
     */
    public static Builder builder(final OAuth2ClientOptions oauth2Options) {
        return new Builder(oauth2Options);
    }

    /**
     * Normalizes one Bus optional endpoint container.
     *
     * @param value optional endpoint container
     * @param label safe endpoint label
     * @return normalized container
     */
    private static Optional<Endpoint> normalize(final Optional<Endpoint> value, final String label) {
        Assert.notNull(value, "OpenID Connect " + label + " container must not be null");
        return Optional.ofNullable(value.getOrNull());
    }

    /**
     * Requires one present OIDC endpoint to use fragment-free HTTPS GET and its exact authentication scheme.
     *
     * @param endpoint       optional endpoint value
     * @param authentication required endpoint authentication declaration
     * @param label          safe endpoint label
     * @throws ValidateException if transport, method, fragment, or authentication is invalid
     */
    private static void requireEndpoint(
            final Endpoint endpoint,
            final Endpoint.Authentication authentication,
            final String label) {
        if (endpoint == null) {
            return;
        }
        if (endpoint.transport() != Endpoint.Transport.HTTPS || endpoint.method().isEmpty()
                || endpoint.method().getOrNull() != Http.Method.GET
                || endpoint.url().toUri().getRawFragment() != null) {
            throw new ValidateException(label + " must be a fragment-free HTTPS GET endpoint");
        }
        if (!endpoint.authentication().contains(authentication)) {
            throw new ValidateException(label + " must declare " + authentication.value() + " authentication");
        }
    }

    /**
     * Returns only whether an endpoint is configured.
     *
     * @param endpoint optional sensitive endpoint
     * @return stable presence label
     */
    private static String present(final Optional<Endpoint> endpoint) {
        return endpoint.isPresent() ? CONFIGURED_VALUE : ABSENT_VALUE;
    }

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<OpenIdClientOptions> type() {
        return OpenIdClientOptions.class;
    }

    @Override
    public OpenIdClientOptions snapshot() {
        return this;
    }

    /**
     * Returns the trusted issuer from the composed OAuth options.
     *
     * @return exact expected OpenID Provider issuer
     */
    public String expectedIssuer() {
        return oauth2Options.expectedIssuer().getOrNull();
    }

    /**
     * Returns the client identifier from the composed OAuth options.
     *
     * @return registered relying-party client identifier
     */
    public String clientId() {
        return oauth2Options.clientId();
    }

    /**
     * Returns a diagnostic summary without client, credential, redirect, or endpoint query values.
     *
     * @return redacted OpenID Connect options summary
     */
    @Override
    public String toString() {
        return "OpenIdClientOptions[oauth2Options=[REDACTED], discoveryEndpoint=" + present(discoveryEndpoint)
                + ", userInfoEndpoint=" + present(userInfoEndpoint) + ", jwkSetEndpoint=" + present(jwkSetEndpoint)
                + ", endSessionEndpoint=" + present(endSessionEndpoint) + ", idTokenSigningAlgorithms="
                + idTokenSigningAlgorithms + Symbol.BRACKET_RIGHT;
    }

    /**
     * Collects OpenID relying-party endpoints and algorithms before immutable canonical validation.
     *
     * @author Kimi Liu
     */
    public static class Builder {

        private final OAuth2ClientOptions oauth2Options;
        private Optional<Endpoint> discoveryEndpoint = Optional.empty();
        private Optional<Endpoint> userInfoEndpoint = Optional.empty();
        private Optional<Endpoint> jwkSetEndpoint = Optional.empty();
        private Optional<Endpoint> endSessionEndpoint = Optional.empty();
        private final Set<JwaAlgorithm> signingAlgorithms = new LinkedHashSet<>();

        /**
         * Creates a build-scoped collector around immutable OAuth 2.x client Options.
         */
        public Builder(final OAuth2ClientOptions oauth2Options) {
            this.oauth2Options = Assert.notNull(oauth2Options, "OpenID OAuth 2.x options must not be null");
        }

        /**
         * Selects the OpenID Provider discovery endpoint.
         *
         * @param value immutable discovery endpoint
         * @return this builder
         */
        public Builder discoveryEndpoint(final Endpoint value) {
            discoveryEndpoint = Optional.of(Assert.notNull(value, "Discovery endpoint must not be null"));
            return this;
        }

        /**
         * Selects the UserInfo endpoint.
         *
         * @param value immutable UserInfo endpoint
         * @return this builder
         */
        public Builder userInfoEndpoint(final Endpoint value) {
            userInfoEndpoint = Optional.of(Assert.notNull(value, "UserInfo endpoint must not be null"));
            return this;
        }

        /**
         * Selects the provider JWK Set endpoint.
         *
         * @param value immutable JWK Set endpoint
         * @return this builder
         */
        public Builder jwkSetEndpoint(final Endpoint value) {
            jwkSetEndpoint = Optional.of(Assert.notNull(value, "JWK Set endpoint must not be null"));
            return this;
        }

        /**
         * Selects the relying-party initiated end-session endpoint.
         *
         * @param value immutable end-session endpoint
         * @return this builder
         */
        public Builder endSessionEndpoint(final Endpoint value) {
            endSessionEndpoint = Optional.of(Assert.notNull(value, "End-session endpoint must not be null"));
            return this;
        }

        /**
         * Adds one accepted ID Token signing algorithm.
         *
         * @param value accepted signing algorithm
         * @return this builder
         */
        public Builder signingAlgorithm(final JwaAlgorithm value) {
            signingAlgorithms.add(Assert.notNull(value, "ID Token signing algorithm must not be null"));
            return this;
        }

        /**
         * Adds accepted ID Token signing algorithms in caller order.
         *
         * @param values accepted signing algorithms
         * @return this builder
         */
        public Builder signingAlgorithms(final Set<JwaAlgorithm> values) {
            Assert.notNull(values, "ID Token signing algorithms must not be null");
            values.forEach(this::signingAlgorithm);
            return this;
        }

        /**
         * Builds and canonically validates one immutable OpenID client Options value.
         *
         * @return immutable OpenID client Options
         */
        public OpenIdClientOptions build() {
            return new OpenIdClientOptions(oauth2Options, discoveryEndpoint, userInfoEndpoint, jwkSetEndpoint,
                    endSessionEndpoint, signingAlgorithms);
        }

    }

}
