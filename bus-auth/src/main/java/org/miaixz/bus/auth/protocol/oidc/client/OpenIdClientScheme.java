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

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.Scheme;
import org.miaixz.bus.auth.protocol.Conformance;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2ClientScheme;
import org.miaixz.bus.auth.protocol.oidc.*;
import org.miaixz.bus.auth.shared.jose.JwkSet;
import org.miaixz.bus.core.Version;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.net.Protocol;

/**
 * Describes the generic standards-based OpenID Connect relying-party client scheme.
 *
 * @author Kimi Liu
 */
public class OpenIdClientScheme implements Scheme<OpenIdClientOptions> {

    /**
     * Stable registration type identifier.
     */
    public static final String ID = "oidc";
    /**
     * Builds an OIDC Authentication Request URL for user-agent navigation.
     */
    public static final Capability<AuthenticationRequest, Url> AUTHENTICATION = capability(
            OpenIdConnect.AUTHENTICATION,
            AuthenticationRequest.class,
            Url.class,
            Capability.Interaction.REDIRECT,
            Capability.Security.PUBLIC);
    /**
     * Executes the standard OAuth token operation used by Authorization Code Flow.
     */
    public static final Capability<TokenRequest, TokenEndpointResponse> TOKEN = OAuth2ClientScheme.TOKEN;
    /**
     * Executes standard OAuth token introspection when the Source declares an introspection endpoint.
     */
    public static final Capability<IntrospectionRequest, IntrospectionResponse> INTROSPECTION = OAuth2ClientScheme.INTROSPECTION;
    /**
     * Executes standard OAuth token revocation when the Source declares a revocation endpoint.
     */
    public static final Capability<RevocationRequest, Void> REVOCATION = OAuth2ClientScheme.REVOCATION;
    /**
     * Executes standard OAuth device authorization when the Source declares a device endpoint.
     */
    public static final Capability<DeviceAuthorizationRequest, DeviceAuthorizationResponse> DEVICE_AUTHORIZATION = OAuth2ClientScheme.DEVICE_AUTHORIZATION;
    /**
     * Retrieves RFC 8414 Authorization Server Metadata when the Source declares its endpoint.
     */
    public static final Capability<Void, AuthorizationServerMetadata> AUTHORIZATION_SERVER_METADATA = OAuth2ClientScheme.AUTHORIZATION_SERVER_METADATA;
    /**
     * Retrieves OpenID Provider Metadata.
     */
    public static final Capability<Void, OpenIdProviderMetadata> DISCOVERY = capability(
            OpenIdConnect.DISCOVERY,
            Void.class,
            OpenIdProviderMetadata.class,
            Capability.Interaction.DIRECT,
            Capability.Security.PUBLIC);
    /**
     * Retrieves the issuer-bound public JWK Set.
     */
    public static final Capability<Void, JwkSet> JWK_SET = capability(
            OpenIdConnect.JWK_SET,
            Void.class,
            JwkSet.class,
            Capability.Interaction.DIRECT,
            Capability.Security.PUBLIC);
    /**
     * Retrieves claims from the UserInfo endpoint.
     */
    public static final Capability<UserInfoRequest, UserInfoResponse> USERINFO = capability(
            OpenIdConnect.USERINFO,
            UserInfoRequest.class,
            UserInfoResponse.class,
            Capability.Interaction.DIRECT,
            Capability.Security.PUBLIC);
    /**
     * Builds an RP-Initiated Logout URL for user-agent navigation.
     */
    public static final Capability<EndSessionRequest, Url> END_SESSION = capability(
            OpenIdConnect.END_SESSION,
            EndSessionRequest.class,
            Url.class,
            Capability.Interaction.REDIRECT,
            Capability.Security.PUBLIC);
    /**
     * Exact potential Source operations, narrowed by the compiled runtime manifest when endpoints are absent.
     */
    private static final Capability.Manifest MANIFEST = new Capability.Manifest(List.of(
            AUTHENTICATION,
            TOKEN,
            INTROSPECTION,
            REVOCATION,
            DEVICE_AUTHORIZATION,
            AUTHORIZATION_SERVER_METADATA,
            DISCOVERY,
            JWK_SET,
            USERINFO,
            END_SESSION));
    /**
     * Formal standards implemented by the generic relying party.
     */
    private static final Conformance CONFORMANCE = new Conformance(Protocol.OIDC, new Version("1.0"),
            Set.of(
                    citation("https://openid.net/specs/openid-connect-core-1_0-final.html", "Core 1.0 code flow"),
                    citation("https://openid.net/specs/openid-connect-discovery-1_0.html", "Discovery 1.0"),
                    citation("https://openid.net/specs/openid-connect-rpinitiated-1_0.html", "RP-Initiated Logout 1.0"),
                    citation("https://www.rfc-editor.org/rfc/rfc7517", "JSON Web Key"),
                    citation("https://www.rfc-editor.org/rfc/rfc7519", "JSON Web Token"),
                    citation("https://www.rfc-editor.org/rfc/rfc9207", "Authorization response issuer"),
                    citation("https://www.rfc-editor.org/rfc/rfc9700", "OAuth security best current practice")),
            "OpenID Connect Relying Party");
    /**
     * Management form using standard discovery, endpoint, client, and JOSE member names.
     */
    private static final Form FORM = new Form(List.of(
            new Form.Section("oidc-client", "OpenID Connect Relying Party", List.of(
                    field(OAuth2.Metadata.ISSUER, "Expected issuer", Form.Type.URL, true),
                    field(OAuth2.Metadata.AUTHORIZATION_ENDPOINT, "Authorization endpoint", Form.Type.URL, true),
                    field(OAuth2.Metadata.TOKEN_ENDPOINT, "Token endpoint", Form.Type.URL, true),
                    field("discovery_endpoint", "Discovery endpoint", Form.Type.URL, true),
                    field(OpenIdConnect.Metadata.USERINFO_ENDPOINT, "UserInfo endpoint", Form.Type.URL, false),
                    field(OAuth2.Metadata.JWKS_URI, "JWK Set URI", Form.Type.URL, true),
                    field(OpenIdConnect.Metadata.END_SESSION_ENDPOINT, "End-session endpoint", Form.Type.URL, false),
                    field(OAuth2.Parameters.CLIENT_ID, "Client identifier", Form.Type.TEXT, true),
                    field("redirect_uris", "Registered redirect URIs", Form.Type.MULTI_SELECT, true),
                    field("token_endpoint_auth_method", "Token endpoint authentication method", Form.Type.SELECT, true),
                    field(OAuth2.Parameters.CLIENT_SECRET, "Client secret reference", Form.Type.SECRET, false),
                    field(
                            OpenIdConnect.Metadata.ID_TOKEN_SIGNING_ALGORITHMS_SUPPORTED,
                            "Allowed ID Token signing algorithms",
                            Form.Type.MULTI_SELECT,
                            true),
                    field("pkce_required", "Require PKCE", Form.Type.BOOLEAN, true)))));

    /**
     * Creates the stateless OpenID Relying Party profile used to compile source registrations.
     */
    public OpenIdClientScheme() {
        // No initialization required.
    }

    /**
     * Creates one Source-direction OpenID Connect capability.
     *
     * @param key          direction-neutral operation key
     * @param requestType  exact request class
     * @param responseType exact success class
     * @param interaction  required interaction pattern
     * @param security     minimum invocation security boundary
     * @param <Q>          request type
     * @param <S>          success type
     * @return immutable Source capability
     */
    private static <Q, S> Capability<Q, S> capability(
            final Capability.Key key,
            final Class<Q> requestType,
            final Class<S> responseType,
            final Capability.Interaction interaction,
            final Capability.Security security) {
        return new Capability<>(key, requestType, responseType, Capability.Direction.SOURCE, Set.of(interaction),
                security);
    }

    /**
     * Creates one formal conformance citation.
     *
     * @param url     official standards URL
     * @param section implemented subject description
     * @return immutable citation
     */
    private static Conformance.Citation citation(final String url, final String section) {
        return new Conformance.Citation(url, section);
    }

    /**
     * Creates one management field without a default or generic constraint.
     *
     * @param key      registered or explicit management key
     * @param label    human-readable field label
     * @param type     input presentation type
     * @param required whether management input is mandatory
     * @return immutable field declaration
     */
    private static Form.Field field(
            final String key,
            final String label,
            final Form.Type type,
            final boolean required) {
        return new Form.Field(key, label, type, required, Optional.empty(), List.of());
    }

    /**
     * Returns the stable generic OpenID Connect scheme identifier.
     *
     * @return {@value #ID}
     */
    @Override
    public String id() {
        return ID;
    }

    /**
     * Returns the OpenID Connect Source category.
     *
     * @return OIDC Source type
     */
    @Override
    public Protocol protocol() {
        return Protocol.OIDC;
    }

    /**
     * Returns the immutable potential Source capability manifest.
     *
     * @return OpenID Connect Source capabilities
     */
    @Override
    public Capability.Manifest manifest() {
        return MANIFEST;
    }

    /**
     * Returns the formal OpenID Connect conformance declaration.
     *
     * @return present conformance declaration
     */
    @Override
    public Optional<Conformance> conformance() {
        return Optional.of(CONFORMANCE);
    }

    /**
     * Returns the external management form.
     *
     * @return immutable OpenID Connect client form
     */
    @Override
    public Form form() {
        return FORM;
    }

    /**
     * Returns no deployment defaults because issuer, endpoints, and client registration are external data.
     *
     * @return empty options defaults
     */
    @Override
    public Optional<OpenIdClientOptions> defaults() {
        return Optional.empty();
    }

}
