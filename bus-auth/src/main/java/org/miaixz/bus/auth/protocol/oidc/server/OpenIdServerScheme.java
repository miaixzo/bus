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
package org.miaixz.bus.auth.protocol.oidc.server;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Scheme;
import org.miaixz.bus.auth.protocol.Conformance;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.protocol.oauth2.server.OAuth2ServerScheme;
import org.miaixz.bus.auth.protocol.oidc.*;
import org.miaixz.bus.auth.shared.jose.JwkSet;
import org.miaixz.bus.core.Version;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Describes the standards-based OpenID Connect server scheme.
 *
 * @author Kimi Liu
 */
public final class OpenIdServerScheme implements Scheme<OpenIdServerOptions> {

    /**
     * Stable registration type identifier.
     */
    public static final String ID = "oidc-server";
    /**
     * Processes an OIDC Authentication Request for an authenticated end user.
     */
    public static final Capability<HttpRequest, HttpResponse> AUTHENTICATION = capability(
            OpenIdConnect.AUTHENTICATION,
            HttpRequest.class,
            HttpResponse.class,
            Capability.Interaction.REDIRECT,
            Capability.Security.SUBJECT_AUTHENTICATED);
    /**
     * Executes the standard OAuth token operation used by Authorization Code Flow.
     */
    public static final Capability<HttpRequest, HttpResponse> TOKEN = OAuth2ServerScheme.TOKEN;
    /**
     * Introspects OAuth access-token state when the composed endpoint is configured.
     */
    public static final Capability<HttpRequest, HttpResponse> INTROSPECTION = OAuth2ServerScheme.INTROSPECTION;
    /**
     * Revokes OAuth access or refresh token state when the composed endpoint is configured.
     */
    public static final Capability<HttpRequest, HttpResponse> REVOCATION = OAuth2ServerScheme.REVOCATION;
    /**
     * Issues OAuth device and user codes when the composed endpoint is configured.
     */
    public static final Capability<HttpRequest, HttpResponse> DEVICE_AUTHORIZATION = OAuth2ServerScheme.DEVICE_AUTHORIZATION;
    /**
     * Publishes composed OAuth Authorization Server Metadata when configured.
     */
    public static final Capability<HttpRequest, HttpResponse> AUTHORIZATION_SERVER_METADATA = OAuth2ServerScheme.AUTHORIZATION_SERVER_METADATA;
    /**
     * Publishes OpenID Provider Metadata.
     */
    public static final Capability<HttpRequest, HttpResponse> DISCOVERY = capability(
            OpenIdConnect.DISCOVERY,
            HttpRequest.class,
            HttpResponse.class,
            Capability.Interaction.DIRECT,
            Capability.Security.PUBLIC);
    /**
     * Publishes the Provider public JWK Set.
     */
    public static final Capability<HttpRequest, HttpResponse> JWK_SET = capability(
            OpenIdConnect.JWK_SET,
            HttpRequest.class,
            HttpResponse.class,
            Capability.Interaction.DIRECT,
            Capability.Security.PUBLIC);
    /**
     * Returns claims authorized by a bearer access token.
     */
    public static final Capability<HttpRequest, HttpResponse> USERINFO = capability(
            OpenIdConnect.USERINFO,
            HttpRequest.class,
            HttpResponse.class,
            Capability.Interaction.DIRECT,
            Capability.Security.PUBLIC);
    /**
     * Ends an OpenID Provider session without creating a response entity.
     */
    public static final Capability<HttpRequest, HttpResponse> END_SESSION = capability(
            OpenIdConnect.END_SESSION,
            HttpRequest.class,
            HttpResponse.class,
            Capability.Interaction.REDIRECT,
            Capability.Security.PUBLIC);
    /**
     * Exact potential Provider operations, narrowed by the compiled runtime manifest when endpoints are absent.
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
     * Formal standards implemented by the OpenID Provider.
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
            "OpenID Provider");
    /**
     * Management form using standard endpoint, OAuth, OIDC, and JOSE member names.
     */
    private static final Form FORM = new Form(List.of(
            new Form.Section("oidc-provider", "OpenID Provider", List.of(
                    field(OAuth2.Metadata.ISSUER, "Issuer", Form.Type.URL, true),
                    field(OAuth2.Metadata.AUTHORIZATION_ENDPOINT, "Authorization endpoint", Form.Type.URL, true),
                    field(OAuth2.Metadata.TOKEN_ENDPOINT, "Token endpoint", Form.Type.URL, true),
                    field("discovery_endpoint", "Discovery endpoint", Form.Type.URL, true),
                    field(OpenIdConnect.Metadata.USERINFO_ENDPOINT, "UserInfo endpoint", Form.Type.URL, false),
                    field(OAuth2.Metadata.JWKS_URI, "JWK Set URI", Form.Type.URL, true),
                    field(OpenIdConnect.Metadata.END_SESSION_ENDPOINT, "End-session endpoint", Form.Type.URL, false),
                    field(OAuth2.Metadata.SCOPES_SUPPORTED, "Supported scopes", Form.Type.MULTI_SELECT, true),
                    field(OAuth2.Metadata.GRANT_TYPES_SUPPORTED, "Supported grant types", Form.Type.MULTI_SELECT, true),
                    field(
                            OAuth2.Metadata.TOKEN_ENDPOINT_AUTH_METHODS_SUPPORTED,
                            "Token endpoint authentication methods",
                            Form.Type.MULTI_SELECT,
                            true),
                    field(
                            OpenIdConnect.Metadata.SUBJECT_TYPES_SUPPORTED,
                            "Subject identifier types",
                            Form.Type.MULTI_SELECT,
                            true),
                    field(
                            OpenIdConnect.Metadata.ID_TOKEN_SIGNING_ALGORITHMS_SUPPORTED,
                            "ID Token signing algorithms",
                            Form.Type.MULTI_SELECT,
                            true),
                    field("id_token_lifetime", "ID Token lifetime", Form.Type.NUMBER, true),
                    field("pkce_required", "Require PKCE", Form.Type.BOOLEAN, true),
                    field(
                            "refresh_token_rotation_required",
                            "Require refresh token rotation",
                            Form.Type.BOOLEAN,
                            true)))));

    /**
     * Creates the stateless OpenID server scheme used to compile server registrations.
     */
    public OpenIdServerScheme() {
        // No initialization required.
    }

    /**
     * Creates one Provider-direction OpenID Connect capability.
     *
     * @param key          direction-neutral operation key
     * @param requestType  exact request class
     * @param responseType exact success class
     * @param interaction  required interaction pattern
     * @param security     minimum invocation security boundary
     * @param <Q>          request type
     * @param <S>          success type
     * @return immutable server-role Source capability
     */
    private static <Q, S> Capability<Q, S> capability(
            final Capability.Key key,
            final Class<Q> requestType,
            final Class<S> responseType,
            final Capability.Interaction interaction,
            final Capability.Security security) {
        return new Capability<>(key, requestType, responseType, Capability.Direction.PROVIDER, Set.of(interaction),
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
     * Returns the stable OpenID server scheme identifier.
     *
     * @return {@value #ID}
     */
    @Override
    public String id() {
        return ID;
    }

    /**
     * Returns the OpenID Connect Provider category.
     *
     * @return OIDC Provider type
     */
    @Override
    public Protocol protocol() {
        return Protocol.OIDC;
    }

    /**
     * Returns the immutable potential server-role Source capability manifest.
     *
     * @return OpenID Provider capabilities
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
     * Returns the external Provider management form.
     *
     * @return immutable OpenID Provider form
     */
    @Override
    public Form form() {
        return FORM;
    }

    /**
     * Returns no deployment defaults because endpoints and policies are external data.
     *
     * @return empty options defaults
     */
    @Override
    public Optional<OpenIdServerOptions> defaults() {
        return Optional.empty();
    }

}
