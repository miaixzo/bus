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
package org.miaixz.bus.auth.protocol.oauth2.server;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.Options;
import org.miaixz.bus.auth.protocol.oauth2.ClientAuthenticationMethod;
import org.miaixz.bus.auth.protocol.oauth2.GrantType;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.protocol.oauth2.Scope;
import org.miaixz.bus.auth.protocol.oauth2.grant.GrantPolicy;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Protocol;

/**
 * Holds immutable deployment and security policy for one OAuth 2.x authorization server Provider.
 *
 * @param issuer                              authorization server issuer identifier
 * @param authorizationEndpoint               optional authorization endpoint
 * @param tokenEndpoint                       optional token endpoint
 * @param introspectionEndpoint               optional token introspection endpoint
 * @param revocationEndpoint                  optional token revocation endpoint
 * @param deviceAuthorizationEndpoint         optional device authorization endpoint
 * @param deviceVerificationUri               optional end-user verification URI required by the device authorization
 *                                            grant
 * @param authorizationServerMetadataEndpoint optional authorization server metadata endpoint
 * @param scopesSupported                     scope tokens available for client registration
 * @param grantTypesSupported                 exact enabled standard grant types
 * @param tokenEndpointAuthMethodsSupported   exact token endpoint authentication methods
 * @param authorizationCodeLifetime           maximum authorization code lifetime
 * @param accessTokenLifetime                 access token lifetime
 * @param refreshTokenLifetime                refresh token family lifetime
 * @param deviceCodeLifetime                  device and user code lifetime
 * @param devicePollingInterval               minimum token polling interval
 * @param pkceRequired                        whether every authorization-code request requires PKCE
 * @param refreshTokenRotationRequired        whether every refresh use rotates the token family
 * @param dpopSupported                       whether the Provider can issue DPoP-constrained access tokens
 * @param federatedJwtEnabled                 whether explicit federated machine JWT authentication is enabled
 * @author Kimi Liu
 */
public record OAuth2ServerOptions(String issuer, Optional<Endpoint> authorizationEndpoint,
        Optional<Endpoint> tokenEndpoint, Optional<Endpoint> introspectionEndpoint,
        Optional<Endpoint> revocationEndpoint, Optional<Endpoint> deviceAuthorizationEndpoint,
        Optional<String> deviceVerificationUri, Optional<Endpoint> authorizationServerMetadataEndpoint,
        Set<String> scopesSupported, Set<GrantType> grantTypesSupported,
        Set<ClientAuthenticationMethod> tokenEndpointAuthMethodsSupported, Duration authorizationCodeLifetime,
        Duration accessTokenLifetime, Duration refreshTokenLifetime, Duration deviceCodeLifetime,
        Duration devicePollingInterval, boolean pkceRequired, boolean refreshTokenRotationRequired,
        boolean dpopSupported, boolean federatedJwtEnabled) implements Options<OAuth2ServerOptions>, GrantPolicy {

    /**
     * Maximum authorization code lifetime permitted by the frozen server policy.
     */
    private static final Duration MAXIMUM_AUTHORIZATION_CODE_LIFETIME = Duration.ofMinutes(10);
    /**
     * Maximum device authorization lifetime permitted by the frozen server policy.
     */
    private static final Duration MAXIMUM_DEVICE_CODE_LIFETIME = Duration.ofMinutes(15);
    /**
     * Minimum RFC 8628 device polling interval permitted by the frozen server policy.
     */
    private static final Duration MINIMUM_DEVICE_POLLING_INTERVAL = Duration.ofSeconds(5);
    /**
     * Minimum whole-second lifetime representable by standard expires_in and positive Store TTL values.
     */
    private static final Duration MINIMUM_TOKEN_LIFETIME = Duration.ofSeconds(1);
    /**
     * Grant variants implemented by the Provider token service.
     */
    private static final Set<GrantType> IMPLEMENTED_GRANTS = Set.of(
            GrantType.AUTHORIZATION_CODE,
            GrantType.REFRESH_TOKEN,
            GrantType.CLIENT_CREDENTIALS,
            GrantType.TOKEN_EXCHANGE,
            GrantType.DEVICE_CODE);
    /**
     * Client authentication methods implemented by the Provider endpoint adapter.
     */
    private static final Set<ClientAuthenticationMethod> IMPLEMENTED_AUTHENTICATION = Set.of(
            ClientAuthenticationMethod.NONE,
            ClientAuthenticationMethod.CLIENT_SECRET_BASIC,
            ClientAuthenticationMethod.CLIENT_SECRET_POST,
            ClientAuthenticationMethod.PRIVATE_KEY_JWT);

    /**
     * Normalizes immutable collections and enforces endpoint, grant, lifetime, PKCE, and rotation invariants.
     */
    public OAuth2ServerOptions {
        issuer = requireHttpsIssuer(issuer);
        authorizationEndpoint = normalize(authorizationEndpoint, "authorization endpoint");
        tokenEndpoint = normalize(tokenEndpoint, "token endpoint");
        introspectionEndpoint = normalize(introspectionEndpoint, "introspection endpoint");
        revocationEndpoint = normalize(revocationEndpoint, "revocation endpoint");
        deviceAuthorizationEndpoint = normalize(deviceAuthorizationEndpoint, "device authorization endpoint");
        deviceVerificationUri = verificationUri(deviceVerificationUri);
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
            throw new ValidateException("OAuth 2.x Provider options require at least one endpoint");
        }

        scopesSupported = scopes(scopesSupported);
        grantTypesSupported = grants(grantTypesSupported);
        tokenEndpointAuthMethodsSupported = authentication(tokenEndpointAuthMethodsSupported);
        if (!grantTypesSupported.isEmpty() && tokenEndpoint.isEmpty()) {
            throw new ValidateException("OAuth 2.x enabled grants require a token endpoint");
        }
        if (grantTypesSupported.contains(GrantType.AUTHORIZATION_CODE)) {
            if (authorizationEndpoint.isEmpty() || !pkceRequired) {
                throw new ValidateException(
                        "OAuth 2.x authorization code grant requires authorization endpoint and PKCE");
            }
        }
        if (grantTypesSupported.contains(GrantType.DEVICE_CODE)
                && (deviceAuthorizationEndpoint.isEmpty() || deviceVerificationUri.isEmpty())) {
            throw new ValidateException(
                    "OAuth 2.x device code grant requires device authorization and verification endpoints");
        }
        if (grantTypesSupported.contains(GrantType.REFRESH_TOKEN) && !refreshTokenRotationRequired) {
            throw new ValidateException("OAuth 2.x refresh token grant requires rotation and reuse detection");
        }
        if (dpopSupported) {
            throw new ValidateException(
                    "OAuth 2.x Provider options cannot enable DPoP without a complete token transport contract");
        }
        if (federatedJwtEnabled
                && (tokenEndpoint.isEmpty() || !grantTypesSupported.contains(GrantType.CLIENT_CREDENTIALS))) {
            throw new ValidateException(
                    "Federated JWT authentication requires a token endpoint and client_credentials grant");
        }
        final Endpoint token = tokenEndpoint.getOrNull();
        if (token != null) {
            if (tokenEndpointAuthMethodsSupported.isEmpty()) {
                throw new ValidateException("OAuth 2.x token endpoint requires a published authentication method");
            }
            for (ClientAuthenticationMethod method : tokenEndpointAuthMethodsSupported) {
                if (!token.authentication().contains(new Endpoint.Authentication(method.value()))) {
                    throw new ValidateException(
                            "Token endpoint does not declare every published authentication method");
                }
            }
        } else if (!tokenEndpointAuthMethodsSupported.isEmpty()) {
            throw new ValidateException("OAuth 2.x token authentication methods require a token endpoint");
        }
        final Endpoint introspection = introspectionEndpoint.getOrNull();
        if (introspection != null
                && !introspection.authentication().contains(Endpoint.Authentication.CLIENT_SECRET_BASIC)
                && !introspection.authentication().contains(Endpoint.Authentication.CLIENT_SECRET_POST)) {
            throw new ValidateException("OAuth 2.x introspection endpoint requires authenticated client access");
        }

        authorizationCodeLifetime = lifetime(authorizationCodeLifetime, "Authorization code lifetime");
        accessTokenLifetime = lifetime(accessTokenLifetime, "Access token lifetime");
        refreshTokenLifetime = lifetime(refreshTokenLifetime, "Refresh token lifetime");
        deviceCodeLifetime = lifetime(deviceCodeLifetime, "Device code lifetime");
        devicePollingInterval = positive(devicePollingInterval, "Device polling interval");
        if (authorizationCodeLifetime.compareTo(MAXIMUM_AUTHORIZATION_CODE_LIFETIME) > 0) {
            throw new ValidateException("OAuth 2.x authorization code lifetime must not exceed ten minutes");
        }
        if (deviceCodeLifetime.compareTo(MAXIMUM_DEVICE_CODE_LIFETIME) > 0) {
            throw new ValidateException("OAuth 2.x device code lifetime must not exceed fifteen minutes");
        }
        if (devicePollingInterval.compareTo(MINIMUM_DEVICE_POLLING_INTERVAL) < 0
                || devicePollingInterval.compareTo(deviceCodeLifetime) >= 0) {
            throw new ValidateException(
                    "OAuth 2.x device polling interval must be at least five seconds and shorter than code lifetime");
        }
        if (refreshTokenLifetime.compareTo(accessTokenLifetime) <= 0) {
            throw new ValidateException("OAuth 2.x refresh token lifetime must exceed access token lifetime");
        }
    }

    /**
     * Creates a secure-default mutable builder for one immutable authorization-server configuration.
     *
     * @param issuer exact HTTPS authorization-server issuer
     * @return new secure-default server-options builder
     */
    public static Builder builder(final String issuer) {
        return new Builder(issuer);
    }

    /**
     * Validates the HTTPS issuer identifier.
     *
     * @param value issuer lexical value
     * @return unchanged issuer
     */
    private static String requireHttpsIssuer(final String value) {
        Assert.notBlank(value, "OAuth 2.x issuer must not be blank");
        final URI uri = uri(value, "OAuth 2.x issuer");
        if (!Protocol.HTTPS.name.equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null || uri.getRawQuery() != null
                || uri.getRawFragment() != null) {
            throw new ValidateException("OAuth 2.x issuer must be an HTTPS URL without query or fragment");
        }
        return value;
    }

    /**
     * Normalizes one Bus optional endpoint container.
     *
     * @param value optional endpoint container
     * @param label safe endpoint label
     * @return normalized optional endpoint
     */
    private static Optional<Endpoint> normalize(final Optional<Endpoint> value, final String label) {
        Assert.notNull(value, "OAuth 2.x " + label + " container must not be null");
        return Optional.ofNullable(value.getOrNull());
    }

    /**
     * Validates the optional RFC 8628 end-user verification URI independently from the device authorization endpoint.
     *
     * @param value optional verification URI container
     * @return normalized optional HTTPS verification URI
     */
    private static Optional<String> verificationUri(final Optional<String> value) {
        Assert.notNull(value, "OAuth 2.x device verification URI container must not be null");
        final String verification = value.getOrNull();
        if (verification == null) {
            return Optional.empty();
        }
        Assert.notBlank(verification, "OAuth 2.x device verification URI must not be blank");
        final URI uri = uri(verification, "OAuth 2.x device verification URI");
        if (!Protocol.HTTPS.name.equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
            throw new ValidateException(
                    "OAuth 2.x device verification URI must be an HTTPS URL without userinfo or fragment");
        }
        if (Url.parse(verification).queryParameterNames().contains(OAuth2.Parameters.USER_CODE)) {
            throw new ValidateException("OAuth 2.x device verification URI must not predefine the user_code parameter");
        }
        return Optional.of(verification);
    }

    /**
     * Requires a present endpoint to use fragment-free HTTPS and the exact method.
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
                || endpoint.method().getOrNull() != method || endpoint.url().toUri().getRawFragment() != null) {
            throw new ValidateException(label + " must be a fragment-free HTTPS " + method.name() + " endpoint");
        }
    }

    /**
     * Validates and freezes supported scope tokens.
     *
     * @param values supported scope strings
     * @return immutable ordered set
     */
    private static Set<String> scopes(final Set<String> values) {
        Assert.notNull(values, "OAuth 2.x supported scopes must not be null");
        if (!values.isEmpty()) {
            new Scope(List.copyOf(values));
        }
        return Set.copyOf(new LinkedHashSet<>(values));
    }

    /**
     * Validates and freezes actively implemented grant types.
     *
     * @param values configured grant types
     * @return immutable grant set
     */
    private static Set<GrantType> grants(final Set<GrantType> values) {
        Assert.notNull(values, "OAuth 2.x supported grant types must not be null");
        for (GrantType value : values) {
            if (!IMPLEMENTED_GRANTS.contains(Assert.notNull(value, "OAuth 2.x grant type must not be null"))) {
                throw new ValidateException("OAuth 2.x Provider enables a grant not implemented by this version");
            }
        }
        return Set.copyOf(values);
    }

    /**
     * Validates and freezes actively implemented token endpoint authentication methods.
     *
     * @param values configured authentication methods
     * @return immutable non-empty authentication set when a token endpoint exists
     */
    private static Set<ClientAuthenticationMethod> authentication(final Set<ClientAuthenticationMethod> values) {
        Assert.notNull(values, "OAuth 2.x token authentication methods must not be null");
        for (ClientAuthenticationMethod value : values) {
            if (!IMPLEMENTED_AUTHENTICATION
                    .contains(Assert.notNull(value, "OAuth 2.x token authentication method must not be null"))) {
                throw new ValidateException(
                        "OAuth 2.x Provider enables client authentication not implemented by this version");
            }
        }
        return Set.copyOf(values);
    }

    /**
     * Requires a positive duration.
     *
     * @param value configured duration
     * @param label safe duration label
     * @return unchanged positive duration
     */
    private static Duration positive(final Duration value, final String label) {
        Assert.notNull(value, label + " must not be null");
        if (value.isZero() || value.isNegative()) {
            throw new ValidateException(label + " must be positive");
        }
        return value;
    }

    /**
     * Requires a whole-second protocol lifetime suitable for expires_in and millisecond Store TTLs.
     *
     * @param value configured lifetime
     * @param label safe duration label
     * @return unchanged validated lifetime
     */
    private static Duration lifetime(final Duration value, final String label) {
        positive(value, label);
        if (value.compareTo(MINIMUM_TOKEN_LIFETIME) < 0) {
            throw new ValidateException(label + " must be at least one second");
        }
        return value;
    }

    /**
     * Parses a standards URI lexical value.
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
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<OAuth2ServerOptions> type() {
        return OAuth2ServerOptions.class;
    }

    @Override
    public OAuth2ServerOptions snapshot() {
        return this;
    }

    /**
     * Collects server configuration without moving validation out of the immutable record.
     *
     * @author Kimi Liu
     */
    public static class Builder {

        /**
         * Exact HTTPS authorization-server issuer.
         */
        private final String issuer;
        /**
         * Authorization endpoint configuration.
         */
        private Endpoint authorizationEndpoint;
        /**
         * Token endpoint configuration.
         */
        private Endpoint tokenEndpoint;
        /**
         * Token introspection endpoint configuration.
         */
        private Endpoint introspectionEndpoint;
        /**
         * Token revocation endpoint configuration.
         */
        private Endpoint revocationEndpoint;
        /**
         * Device authorization endpoint configuration.
         */
        private Endpoint deviceAuthorizationEndpoint;
        /**
         * User-facing device verification URI.
         */
        private String deviceVerificationUri;
        /**
         * Authorization-server metadata endpoint configuration.
         */
        private Endpoint metadataEndpoint;
        /**
         * Supported OAuth scopes.
         */
        private Set<String> scopes = Set.of();
        /**
         * Supported OAuth grant types.
         */
        private Set<GrantType> grants = Set.of();
        /**
         * Supported client authentication methods.
         */
        private Set<ClientAuthenticationMethod> authenticationMethods = Set.of();
        /**
         * Authorization-code lifetime.
         */
        private Duration authorizationCodeLifetime = Duration.ofMinutes(5);
        /**
         * Access-token lifetime.
         */
        private Duration accessTokenLifetime = Duration.ofMinutes(5);
        /**
         * Refresh-token lifetime.
         */
        private Duration refreshTokenLifetime = Duration.ofDays(30);
        /**
         * Device-code lifetime.
         */
        private Duration deviceCodeLifetime = Duration.ofMinutes(10);
        /**
         * Minimum device polling interval.
         */
        private Duration devicePollingInterval = Duration.ofSeconds(5);
        /**
         * Whether authorization-code requests require PKCE.
         */
        private boolean pkceRequired = true;
        /**
         * Whether refresh-token rotation is mandatory.
         */
        private boolean refreshTokenRotationRequired = true;
        /**
         * Whether federated JWT client authentication is enabled.
         */
        private boolean federatedJwtEnabled;

        /**
         * Creates a secure-default collector for one issuer.
         *
         * @param issuer exact HTTPS authorization-server issuer
         */
        public Builder(final String issuer) {
            this.issuer = Assert.notBlank(issuer, "OAuth 2.x issuer must not be blank");
        }

        /**
         * Selects the authorization endpoint.
         *
         * @param value immutable authorization endpoint
         * @return this builder
         */
        public Builder authorizationEndpoint(final Endpoint value) {
            this.authorizationEndpoint = Assert.notNull(value, "Authorization endpoint must not be null");
            return this;
        }

        /**
         * Selects the token endpoint.
         *
         * @param value immutable token endpoint
         * @return this builder
         */
        public Builder tokenEndpoint(final Endpoint value) {
            this.tokenEndpoint = Assert.notNull(value, "Token endpoint must not be null");
            return this;
        }

        /**
         * Selects the token introspection endpoint.
         *
         * @param value immutable introspection endpoint
         * @return this builder
         */
        public Builder introspectionEndpoint(final Endpoint value) {
            this.introspectionEndpoint = Assert.notNull(value, "Introspection endpoint must not be null");
            return this;
        }

        /**
         * Selects the token revocation endpoint.
         *
         * @param value immutable revocation endpoint
         * @return this builder
         */
        public Builder revocationEndpoint(final Endpoint value) {
            this.revocationEndpoint = Assert.notNull(value, "Revocation endpoint must not be null");
            return this;
        }

        /**
         * Selects the device authorization and verification endpoints.
         *
         * @param value           immutable device authorization endpoint
         * @param verificationUri exact user-facing verification URI
         * @return this builder
         */
        public Builder deviceAuthorizationEndpoint(final Endpoint value, final String verificationUri) {
            this.deviceAuthorizationEndpoint = Assert.notNull(value, "Device authorization endpoint must not be null");
            this.deviceVerificationUri = Assert.notBlank(verificationUri, "Device verification URI must not be blank");
            return this;
        }

        /**
         * Selects the authorization-server metadata endpoint.
         *
         * @param value immutable metadata endpoint
         * @return this builder
         */
        public Builder metadataEndpoint(final Endpoint value) {
            this.metadataEndpoint = Assert.notNull(value, "Metadata endpoint must not be null");
            return this;
        }

        /**
         * Replaces the supported scope set.
         *
         * @param values supported OAuth scopes
         * @return this builder
         */
        public Builder scopes(final Set<String> values) {
            this.scopes = Set.copyOf(Assert.notNull(values, "OAuth scopes must not be null"));
            return this;
        }

        /**
         * Replaces the supported grant-type set.
         *
         * @param values supported OAuth grants
         * @return this builder
         */
        public Builder grants(final Set<GrantType> values) {
            this.grants = Set.copyOf(Assert.notNull(values, "OAuth grants must not be null"));
            return this;
        }

        /**
         * Replaces the supported client-authentication methods.
         *
         * @param values supported client-authentication methods
         * @return this builder
         */
        public Builder authenticationMethods(final Set<ClientAuthenticationMethod> values) {
            this.authenticationMethods = Set.copyOf(Assert.notNull(values, "Authentication methods must not be null"));
            return this;
        }

        /**
         * Replaces authorization-code, access-token, and refresh-token lifetimes.
         *
         * @param code    authorization-code lifetime
         * @param access  access-token lifetime
         * @param refresh refresh-token lifetime
         * @return this builder
         */
        public Builder lifetimes(final Duration code, final Duration access, final Duration refresh) {
            this.authorizationCodeLifetime = Assert.notNull(code, "Authorization code lifetime must not be null");
            this.accessTokenLifetime = Assert.notNull(access, "Access token lifetime must not be null");
            this.refreshTokenLifetime = Assert.notNull(refresh, "Refresh token lifetime must not be null");
            return this;
        }

        /**
         * Replaces device-code lifetime and polling interval.
         *
         * @param lifetime        device-code lifetime
         * @param pollingInterval minimum polling interval
         * @return this builder
         */
        public Builder device(final Duration lifetime, final Duration pollingInterval) {
            this.deviceCodeLifetime = Assert.notNull(lifetime, "Device code lifetime must not be null");
            this.devicePollingInterval = Assert.notNull(pollingInterval, "Device polling interval must not be null");
            return this;
        }

        /**
         * Configures mandatory PKCE validation.
         *
         * @param value whether PKCE is mandatory
         * @return this builder
         */
        public Builder pkceRequired(final boolean value) {
            this.pkceRequired = value;
            return this;
        }

        /**
         * Configures mandatory refresh-token rotation.
         *
         * @param value whether rotation is mandatory
         * @return this builder
         */
        public Builder refreshTokenRotationRequired(final boolean value) {
            this.refreshTokenRotationRequired = value;
            return this;
        }

        /**
         * Configures federated JWT client authentication.
         *
         * @param value whether the federated profile is enabled
         * @return this builder
         */
        public Builder federatedJwtEnabled(final boolean value) {
            this.federatedJwtEnabled = value;
            return this;
        }

        /**
         * Builds and validates immutable server options.
         *
         * @return validated immutable OAuth server options
         */
        public OAuth2ServerOptions build() {
            return new OAuth2ServerOptions(issuer, Optional.ofNullable(authorizationEndpoint),
                    Optional.ofNullable(tokenEndpoint), Optional.ofNullable(introspectionEndpoint),
                    Optional.ofNullable(revocationEndpoint), Optional.ofNullable(deviceAuthorizationEndpoint),
                    Optional.ofNullable(deviceVerificationUri), Optional.ofNullable(metadataEndpoint), scopes, grants,
                    authenticationMethods, authorizationCodeLifetime, accessTokenLifetime, refreshTokenLifetime,
                    deviceCodeLifetime, devicePollingInterval, pkceRequired, refreshTokenRotationRequired, false,
                    federatedJwtEnabled);
        }

    }

}
