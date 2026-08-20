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
package org.miaixz.bus.auth.protocol.oauth1.client;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Form;
import org.miaixz.bus.auth.protocol.Conformance;
import org.miaixz.bus.auth.protocol.oauth1.*;
import org.miaixz.bus.auth.source.SourceProfile;
import org.miaixz.bus.core.Version;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Describes the generic RFC 5849 OAuth 1.0 client Source profile.
 *
 * @author Kimi Liu
 */
public final class OAuth1SourceProfile implements SourceProfile<OAuth1ClientSettings> {

    /**
     * Stable registration type identifier.
     */
    public static final String ID = "oauth1";
    /**
     * Obtains temporary credentials from the RFC 5849 temporary credentials endpoint.
     */
    public static final Capability<TemporaryCredentialsRequest, TemporaryCredentialsResponse> TEMPORARY_CREDENTIALS = capability(
            "temporary_credentials",
            TemporaryCredentialsRequest.class,
            TemporaryCredentialsResponse.class,
            Capability.Interaction.DIRECT);
    /**
     * Creates the RFC 5849 resource owner authorization redirect URI.
     */
    public static final Capability<ResourceOwnerAuthorizationRequest, UnoUrl> RESOURCE_OWNER_AUTHORIZATION = capability(
            "resource_owner_authorization",
            ResourceOwnerAuthorizationRequest.class,
            UnoUrl.class,
            Capability.Interaction.REDIRECT);
    /**
     * Exchanges an authorized temporary credential for RFC 5849 token credentials.
     */
    public static final Capability<TokenCredentialsRequest, TokenCredentialsResponse> TOKEN_CREDENTIALS = capability(
            "token_credentials",
            TokenCredentialsRequest.class,
            TokenCredentialsResponse.class,
            Capability.Interaction.DIRECT);
    /**
     * Signs and executes an RFC 5849 protected-resource request.
     */
    public static final Capability<ProtectedResourceRequest, HttpResponse> PROTECTED_RESOURCE = capability(
            "protected_resource",
            ProtectedResourceRequest.class,
            HttpResponse.class,
            Capability.Interaction.DIRECT);
    /**
     * Exact RFC 5849 client capability manifest implemented by this profile.
     */
    private static final Capability.Manifest MANIFEST = new Capability.Manifest(
            List.of(TEMPORARY_CREDENTIALS, RESOURCE_OWNER_AUTHORIZATION, TOKEN_CREDENTIALS, PROTECTED_RESOURCE));
    /**
     * Formal RFC 5849 conformance declaration.
     */
    private static final Conformance CONFORMANCE = new Conformance(Protocol.OAUTH1, new Version("1.0"),
            Set.of(new Conformance.Citation("https://www.rfc-editor.org/rfc/rfc5849", "Sections 2-3")),
            "OAuth 1.0 Protocol");
    /**
     * Management form containing only deployable OAuth 1.0 Source settings.
     */
    private static final Form FORM = new Form(List.of(
            new Form.Section("oauth1-client", "OAuth 1.0 Client", List.of(
                    field("temporary_credentials_endpoint", "Temporary credentials endpoint", Form.Type.URL, true),
                    field(
                            "resource_owner_authorization_endpoint",
                            "Resource owner authorization endpoint",
                            Form.Type.URL,
                            true),
                    field("token_credentials_endpoint", "Token credentials endpoint", Form.Type.URL, true),
                    field("consumer_key", "Consumer key", Form.Type.TEXT, true),
                    field("signing_credential", "Signing credential", Form.Type.SECRET, true),
                    field("signature_method", "Signature method", Form.Type.SELECT, true),
                    field("temporary_credential_lifetime", "Temporary credential lifetime", Form.Type.NUMBER, true),
                    field("realm", "Authorization realm", Form.Type.TEXT, false)))));

    /**
     * Creates the stateless OAuth 1.0 source profile used to compile client registrations.
     */
    public OAuth1SourceProfile() {
        // No initialization required.
    }

    /**
     * Creates one Source-direction RFC 5849 client capability.
     *
     * @param operation    stable RFC operation routing name
     * @param requestType  exact standard request class
     * @param responseType exact response class
     * @param interaction  required interaction pattern
     * @param <Q>          request type
     * @param <S>          response type
     * @return immutable client capability
     */
    private static <Q, S> Capability<Q, S> capability(
            final String operation,
            final Class<Q> requestType,
            final Class<S> responseType,
            final Capability.Interaction interaction) {
        return new Capability<>(Capability.Key.standard(Protocol.OAUTH1, operation), requestType, responseType,
                Capability.Direction.SOURCE, Set.of(interaction), Capability.Security.CLIENT_AUTHENTICATED);
    }

    /**
     * Creates a management field without a non-sensitive default or extra generic constraints.
     *
     * @param key      stable settings field key
     * @param label    human-readable field label
     * @param type     management input presentation type
     * @param required whether the field is required
     * @return immutable form field
     */
    private static Form.Field field(
            final String key,
            final String label,
            final Form.Type type,
            final boolean required) {
        return new Form.Field(key, label, type, required, Optional.empty(), List.of());
    }

    /**
     * Returns the stable generic OAuth 1.0 profile identifier.
     *
     * @return {@value #ID}
     */
    @Override
    public String id() {
        return ID;
    }

    /**
     * Returns the OAuth 1.0 client Source category.
     *
     * @return OAuth 1.0 Source type
     */
    @Override
    public Protocol type() {
        return Protocol.OAUTH1;
    }

    /**
     * Returns the exact immutable settings class.
     *
     * @return OAuth 1.0 client settings class
     */
    @Override
    public Class<OAuth1ClientSettings> settingsType() {
        return OAuth1ClientSettings.class;
    }

    /**
     * Returns the exact RFC 5849 client capability manifest.
     *
     * @return immutable capability manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return MANIFEST;
    }

    /**
     * Returns the formal RFC 5849 conformance declaration.
     *
     * @return present OAuth 1.0 conformance
     */
    @Override
    public Optional<Conformance> conformance() {
        return Optional.of(CONFORMANCE);
    }

    /**
     * Returns the immutable management form.
     *
     * @return OAuth 1.0 client settings form
     */
    @Override
    public Form form() {
        return FORM;
    }

    /**
     * Returns no deployment defaults because endpoints and credentials are explicit external data.
     *
     * @return empty settings defaults
     */
    @Override
    public Optional<OAuth1ClientSettings> defaults() {
        return Optional.empty();
    }

}
