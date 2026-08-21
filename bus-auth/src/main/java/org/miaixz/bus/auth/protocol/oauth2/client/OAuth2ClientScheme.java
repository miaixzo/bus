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
package org.miaixz.bus.auth.protocol.oauth2.client;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Scheme;
import org.miaixz.bus.auth.protocol.Conformance;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.core.Version;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.UnoUrl;

/**
 * Describes the generic standards-based OAuth 2.x client client scheme.
 *
 * @author Kimi Liu
 */
public final class OAuth2ClientScheme implements Scheme<OAuth2ClientOptions> {

    /**
     * Stable registration type identifier.
     */
    public static final String ID = "oauth2";
    /**
     * Builds a standard authorization URL for user-agent redirection.
     */
    public static final Capability<AuthorizationRequest, UnoUrl> AUTHORIZATION = capability(
            OAuth2.AUTHORIZATION,
            AuthorizationRequest.class,
            UnoUrl.class,
            Capability.Interaction.REDIRECT,
            Capability.Security.PUBLIC);
    /**
     * Executes every supported grant at the single token endpoint.
     */
    public static final Capability<TokenRequest, TokenEndpointResponse> TOKEN = capability(
            OAuth2.TOKEN,
            TokenRequest.class,
            TokenEndpointResponse.class,
            Capability.Interaction.DIRECT,
            Capability.Security.PUBLIC);
    /**
     * Introspects an opaque token using an authenticated client.
     */
    public static final Capability<IntrospectionRequest, IntrospectionResponse> INTROSPECTION = capability(
            OAuth2.INTROSPECTION,
            IntrospectionRequest.class,
            IntrospectionResponse.class,
            Capability.Interaction.DIRECT,
            Capability.Security.CLIENT_AUTHENTICATED);
    /**
     * Revokes an opaque token using an authenticated client.
     */
    public static final Capability<RevocationRequest, Void> REVOCATION = capability(
            OAuth2.REVOCATION,
            RevocationRequest.class,
            Void.class,
            Capability.Interaction.DIRECT,
            Capability.Security.CLIENT_AUTHENTICATED);
    /**
     * Initiates RFC 8628 device authorization.
     */
    public static final Capability<DeviceAuthorizationRequest, DeviceAuthorizationResponse> DEVICE_AUTHORIZATION = capability(
            OAuth2.DEVICE_AUTHORIZATION,
            DeviceAuthorizationRequest.class,
            DeviceAuthorizationResponse.class,
            Capability.Interaction.DEVICE,
            Capability.Security.PUBLIC);
    /**
     * Retrieves RFC 8414 authorization server metadata.
     */
    public static final Capability<Void, AuthorizationServerMetadata> AUTHORIZATION_SERVER_METADATA = capability(
            OAuth2.AUTHORIZATION_SERVER_METADATA,
            Void.class,
            AuthorizationServerMetadata.class,
            Capability.Interaction.DIRECT,
            Capability.Security.PUBLIC);
    /**
     * Exact OAuth 2.x Source operations implemented by this scheme.
     */
    private static final Capability.Manifest MANIFEST = new Capability.Manifest(List
            .of(AUTHORIZATION, TOKEN, INTROSPECTION, REVOCATION, DEVICE_AUTHORIZATION, AUTHORIZATION_SERVER_METADATA));
    /**
     * Formal standards implemented by the generic OAuth 2.x Source.
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
            "OAuth 2.x Client");
    /**
     * External management form using registered OAuth member names where standards define them.
     */
    private static final Form FORM = new Form(List.of(
            new Form.Section("oauth2-client", "OAuth 2.x Client", List.of(
                    field(OAuth2.Metadata.AUTHORIZATION_ENDPOINT, "Authorization endpoint", Form.Type.URL, false),
                    field(OAuth2.Metadata.TOKEN_ENDPOINT, "Token endpoint", Form.Type.URL, false),
                    field(OAuth2.Metadata.INTROSPECTION_ENDPOINT, "Introspection endpoint", Form.Type.URL, false),
                    field(OAuth2.Metadata.REVOCATION_ENDPOINT, "Revocation endpoint", Form.Type.URL, false),
                    field(
                            OAuth2.Metadata.DEVICE_AUTHORIZATION_ENDPOINT,
                            "Device authorization endpoint",
                            Form.Type.URL,
                            false),
                    field(
                            "authorization_server_metadata_endpoint",
                            "Authorization server metadata endpoint",
                            Form.Type.URL,
                            false),
                    field(OAuth2.Metadata.ISSUER, "Expected issuer", Form.Type.URL, false),
                    field(OAuth2.Parameters.CLIENT_ID, "Client identifier", Form.Type.TEXT, true),
                    field("redirect_uris", "Registered redirect URIs", Form.Type.MULTI_SELECT, false),
                    field("token_endpoint_auth_method", "Client authentication method", Form.Type.SELECT, true),
                    field(OAuth2.Parameters.CLIENT_SECRET, "Client secret reference", Form.Type.SECRET, false),
                    flag("pkce_required", "Require PKCE")))));

    /**
     * Creates the stateless OAuth 2.0 client scheme used to compile client registrations.
     */
    public OAuth2ClientScheme() {
        // No initialization required.
    }

    /**
     * Creates one Source-direction OAuth 2.x capability.
     *
     * @param key          shared direction-neutral operation key
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
     * Creates one RFC Editor conformance citation.
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
     * Creates an optional platform security flag with a non-sensitive false default.
     *
     * @param key   stable platform options key
     * @param label human-readable field label
     * @return immutable Boolean field declaration
     */
    private static Form.Field flag(final String key, final String label) {
        return new Form.Field(key, label, Form.Type.BOOLEAN, false, Optional.of(new JsonValue.BooleanValue(false)),
                List.of());
    }

    /**
     * Returns the stable generic OAuth 2.x scheme identifier.
     *
     * @return {@value #ID}
     */
    @Override
    public String id() {
        return ID;
    }

    /**
     * Returns the OAuth 2.x client Source category.
     *
     * @return OAuth 2.x Source type
     */
    @Override
    public Protocol protocol() {
        return Protocol.OAUTH2;
    }

    /**
     * Returns the exact implemented OAuth 2.x Source capabilities.
     *
     * @return immutable capability manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return MANIFEST;
    }

    /**
     * Returns the formal standards basis for the generic client.
     *
     * @return present OAuth 2.x conformance declaration
     */
    @Override
    public Optional<Conformance> conformance() {
        return Optional.of(CONFORMANCE);
    }

    /**
     * Returns the immutable external management form.
     *
     * @return OAuth 2.x client options form
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
    public Optional<OAuth2ClientOptions> defaults() {
        return Optional.empty();
    }

}
