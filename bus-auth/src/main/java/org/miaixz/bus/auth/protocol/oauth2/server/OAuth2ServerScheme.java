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

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.FabricX.Request;
import org.miaixz.bus.auth.FabricX.Response;
import org.miaixz.bus.auth.Scheme;
import org.miaixz.bus.auth.protocol.Conformance;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.core.Version;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.net.Protocol;

/**
 * Describes the standards-based OAuth 2.x authorization server server scheme.
 *
 * @author Kimi Liu
 */
public class OAuth2ServerScheme implements Scheme<OAuth2ServerOptions> {

    /**
     * Stable registration type identifier.
     */
    public static final String ID = "oauth2-server";
    /**
     * Processes an authorization request for an authenticated resource owner.
     */
    public static final Capability<Request, Response> AUTHORIZATION = capability(
            OAuth2.AUTHORIZATION,
            Request.class,
            Response.class,
            Capability.Interaction.REDIRECT,
            Capability.Security.SUBJECT_AUTHENTICATED);
    /**
     * Executes every enabled grant at the single token endpoint.
     */
    public static final Capability<Request, Response> TOKEN = capability(
            OAuth2.TOKEN,
            Request.class,
            Response.class,
            Capability.Interaction.DIRECT,
            Capability.Security.PUBLIC);
    /**
     * Returns RFC 7662 token state to an authenticated protected resource.
     */
    public static final Capability<Request, Response> INTROSPECTION = capability(
            OAuth2.INTROSPECTION,
            Request.class,
            Response.class,
            Capability.Interaction.DIRECT,
            Capability.Security.PUBLIC);
    /**
     * Revokes a token according to RFC 7009 client rules.
     */
    public static final Capability<Request, Response> REVOCATION = capability(
            OAuth2.REVOCATION,
            Request.class,
            Response.class,
            Capability.Interaction.DIRECT,
            Capability.Security.PUBLIC);
    /**
     * Issues device and user codes for RFC 8628 authorization.
     */
    public static final Capability<Request, Response> DEVICE_AUTHORIZATION = capability(
            OAuth2.DEVICE_AUTHORIZATION,
            Request.class,
            Response.class,
            Capability.Interaction.DEVICE,
            Capability.Security.PUBLIC);
    /**
     * Publishes RFC 8414 authorization server metadata.
     */
    public static final Capability<Request, Response> AUTHORIZATION_SERVER_METADATA = capability(
            OAuth2.AUTHORIZATION_SERVER_METADATA,
            Request.class,
            Response.class,
            Capability.Interaction.DIRECT,
            Capability.Security.PUBLIC);
    /**
     * Exact OAuth 2.x server operations implemented by this scheme.
     */
    private static final Capability.Manifest MANIFEST = new Capability.Manifest(List
            .of(AUTHORIZATION, TOKEN, INTROSPECTION, REVOCATION, DEVICE_AUTHORIZATION, AUTHORIZATION_SERVER_METADATA));
    /**
     * Formal standards implemented by the OAuth 2.x Provider.
     */
    private static final Conformance CONFORMANCE = new Conformance(Protocol.OAUTH2, new Version("2.0"),
            Set.of(
                    citation("rfc6749", "OAuth 2.0 authorization framework"),
                    citation("rfc6750", "Bearer token usage"),
                    citation("rfc7009", "Token revocation"),
                    citation("rfc7636", "Proof Key for Code Exchange"),
                    citation("rfc7662", "Token introspection"),
                    citation("rfc8414", "Authorization server metadata"),
                    citation("rfc8628", "Device authorization grant"),
                    citation("rfc8693", "Token exchange"),
                    citation("rfc9207", "Authorization response issuer"),
                    citation("rfc9700", "OAuth 2.0 security best current practice")),
            "OAuth 2.x Authorization Server");
    /**
     * Management form containing protocol options but no Store or runtime dependencies.
     */
    private static final Form FORM = new Form(List.of(
            new Form.Section("oauth2-provider", "OAuth 2.x Authorization Server", List.of(
                    field(OAuth2.Metadata.ISSUER, "Issuer", Form.Type.URL, true),
                    field(OAuth2.Metadata.AUTHORIZATION_ENDPOINT, "Authorization endpoint", Form.Type.URL, false),
                    field(OAuth2.Metadata.TOKEN_ENDPOINT, "Token endpoint", Form.Type.URL, false),
                    field(OAuth2.Metadata.INTROSPECTION_ENDPOINT, "Introspection endpoint", Form.Type.URL, false),
                    field(OAuth2.Metadata.REVOCATION_ENDPOINT, "Revocation endpoint", Form.Type.URL, false),
                    field(
                            OAuth2.Metadata.DEVICE_AUTHORIZATION_ENDPOINT,
                            "Device authorization endpoint",
                            Form.Type.URL,
                            false),
                    field("device_verification_uri", "Device verification URI", Form.Type.URL, false),
                    field(
                            "authorization_server_metadata_endpoint",
                            "Authorization server metadata endpoint",
                            Form.Type.URL,
                            false),
                    field(OAuth2.Metadata.SCOPES_SUPPORTED, "Supported scopes", Form.Type.MULTI_SELECT, false),
                    field(OAuth2.Metadata.GRANT_TYPES_SUPPORTED, "Supported grant types", Form.Type.MULTI_SELECT, true),
                    field(
                            OAuth2.Metadata.TOKEN_ENDPOINT_AUTH_METHODS_SUPPORTED,
                            "Client authentication methods",
                            Form.Type.MULTI_SELECT,
                            true),
                    field("authorization_code_lifetime", "Authorization code lifetime", Form.Type.NUMBER, true),
                    field("access_token_lifetime", "Access token lifetime", Form.Type.NUMBER, true),
                    field("refresh_token_lifetime", "Refresh token lifetime", Form.Type.NUMBER, true),
                    field("device_code_lifetime", "Device code lifetime", Form.Type.NUMBER, true),
                    field("device_polling_interval", "Device polling interval", Form.Type.NUMBER, true),
                    field("pkce_required", "Require PKCE", Form.Type.BOOLEAN, true),
                    field(
                            "refresh_token_rotation_required",
                            "Require refresh token rotation",
                            Form.Type.BOOLEAN,
                            true)))));

    /**
     * Creates the stateless OAuth 2.0 server scheme used to compile authorization-server registrations.
     */
    public OAuth2ServerScheme() {
        // No initialization required.
    }

    /**
     * Creates one Provider-direction OAuth 2.x capability.
     *
     * @param key          shared direction-neutral operation key
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
     * Creates one stable RFC Editor conformance citation.
     *
     * @param document lowercase RFC document identifier
     * @param section  implemented subject description
     * @return immutable citation
     */
    private static Conformance.Citation citation(final String document, final String section) {
        return new Conformance.Citation("https://www.rfc-editor.org/rfc/" + document, section);
    }

    /**
     * Creates a management field without a default or generic constraint.
     *
     * @param key      registered or platform options key
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
     * Returns the stable OAuth 2.x server scheme identifier.
     *
     * @return {@value #ID}
     */
    @Override
    public String id() {
        return ID;
    }

    /**
     * Returns the OAuth 2.x authorization server category.
     *
     * @return OAuth 2.x Provider type
     */
    @Override
    public Protocol protocol() {
        return Protocol.OAUTH2;
    }

    /**
     * Returns the exact implemented server capability manifest.
     *
     * @return immutable capability manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return MANIFEST;
    }

    /**
     * Returns the formal authorization server conformance declaration.
     *
     * @return present OAuth 2.x conformance
     */
    @Override
    public Optional<Conformance> conformance() {
        return Optional.of(CONFORMANCE);
    }

    /**
     * Returns the immutable Provider management form.
     *
     * @return OAuth 2.x Provider options form
     */
    @Override
    public Form form() {
        return FORM;
    }

    /**
     * Returns no deployment defaults because issuer and policy are external decisions.
     *
     * @return empty options defaults
     */
    @Override
    public Optional<OAuth2ServerOptions> defaults() {
        return Optional.empty();
    }

}
