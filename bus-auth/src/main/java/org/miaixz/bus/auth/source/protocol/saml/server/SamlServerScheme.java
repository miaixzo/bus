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
package org.miaixz.bus.auth.source.protocol.saml.server;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Scheme.Conformance;
import org.miaixz.bus.auth.source.protocol.ProtocolScheme;
import org.miaixz.bus.auth.source.protocol.saml.*;
import org.miaixz.bus.core.Version;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.net.Protocol;

/**
 * Declares the generic SAML 2.0 identity-server scheme.
 * <p>
 * This server scheme accepts service-provider requests through HTTP-Redirect, returns protocol responses through
 * HTTP-POST, and publishes SAML Metadata. It does not advertise HTTP-Artifact, SOAP, Enhanced Client or Proxy, or
 * identity-provider discovery profiles.
 * </p>
 *
 * @author Kimi Liu
 */
public class SamlServerScheme implements ProtocolScheme<SamlServerOptions> {

    /**
     * Stable Source type identifier for generic SAML identity-providers.
     */
    public static final String ID = "saml-server";
    /**
     * Processes an Authentication Request for an already authenticated subject.
     */
    public static final Capability<AuthnRequest, Response> SINGLE_SIGN_ON = capability(
            Saml.SINGLE_SIGN_ON,
            AuthnRequest.class,
            Response.class,
            Capability.Interaction.REDIRECT,
            Capability.Security.SUBJECT_AUTHENTICATED);
    /**
     * Processes a SAML Single Logout Request.
     */
    public static final Capability<LogoutRequest, LogoutResponse> SINGLE_LOGOUT = capability(
            Saml.SINGLE_LOGOUT,
            LogoutRequest.class,
            LogoutResponse.class,
            Capability.Interaction.REDIRECT,
            Capability.Security.PUBLIC);
    /**
     * Publishes the identity provider's standard SAML Metadata document.
     */
    public static final Capability<Void, EntityDescriptor> METADATA = capability(
            Saml.METADATA,
            Void.class,
            EntityDescriptor.class,
            Capability.Interaction.DIRECT,
            Capability.Security.PUBLIC);
    /**
     * Exact potential identity-provider operations before endpoint-specific runtime narrowing.
     */
    private static final Capability.Manifest MANIFEST = new Capability.Manifest(
            List.of(SINGLE_SIGN_ON, SINGLE_LOGOUT, METADATA));
    /**
     * Formal specifications implemented by this identity-server scheme.
     */
    private static final Conformance CONFORMANCE = new Conformance(Protocol.SAML, new Version("2.0"),
            Set.of(
                    citation("https://docs.oasis-open.org/security/saml/v2.0/saml-core-2.0-os.pdf", "Core"),
                    citation(
                            "https://docs.oasis-open.org/security/saml/v2.0/saml-bindings-2.0-os.pdf",
                            "HTTP-Redirect and HTTP-POST Bindings"),
                    citation(
                            "https://docs.oasis-open.org/security/saml/v2.0/saml-profiles-2.0-os.pdf",
                            "Web Browser SSO and Single Logout Profiles"),
                    citation("https://docs.oasis-open.org/security/saml/v2.0/saml-metadata-2.0-os.pdf", "Metadata")),
            "SAML 2.0 Identity Provider");
    /**
     * External management form containing only deployment options owned by a SAML identity-provider.
     */
    private static final Form FORM = new Form(List.of(
            new Form.Section("saml-identity-provider", "SAML 2.0 Identity Provider", List.of(
                    field("entity_id", "Identity provider entity ID", Form.Type.URL, true),
                    field("single_sign_on_service_url", "Single Sign-On Service URL", Form.Type.URL, true),
                    field(Saml.Parameters.SINGLE_LOGOUT_SERVICE_URL, "Single Logout Service URL", Form.Type.URL, false),
                    field("metadata_url", "Metadata URL", Form.Type.URL, true),
                    field("signing_key_id", "Signing key identifier", Form.Type.SECRET, true),
                    field("signature_algorithm", "XML Signature algorithm", Form.Type.SELECT, true),
                    field("assertions_signed", "Sign assertions", Form.Type.BOOLEAN, true),
                    field("responses_signed", "Sign responses", Form.Type.BOOLEAN, true)))));

    /**
     * Creates the stateless SAML identity-server scheme used to compile server Source configurations.
     */
    public SamlServerScheme() {
        // No initialization required.
    }

    /**
     * Creates one SAML Identity Provider capability.
     *
     * @param key          direction-neutral SAML operation key
     * @param requestType  exact request class
     * @param responseType exact success class
     * @param interaction  required interaction pattern
     * @param security     required caller or subject authentication boundary
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
        return new Capability<>(key, requestType, responseType, Capability.Direction.SERVER, Set.of(interaction),
                security);
    }

    /**
     * Creates one formal standards citation.
     *
     * @param url     official specification URL
     * @param section implemented subject description
     * @return immutable conformance citation
     */
    private static Conformance.Citation citation(final String url, final String section) {
        return new Conformance.Citation(url, section);
    }

    /**
     * Creates one management field without a default or generic constraint.
     *
     * @param key      stable management key
     * @param label    human-readable field label
     * @param type     management input presentation type
     * @param required whether the deployment value is mandatory
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
     * Returns the stable SAML server scheme identifier.
     *
     * @return {@value #ID}
     */
    @Override
    public String id() {
        return ID;
    }

    /**
     * Returns management presentation metadata for a SAML Identity Provider.
     *
     * @return immutable SAML server metadata
     */
    @Override
    public Metadata metadata() {
        return new Metadata("SAML Identity Provider", "Exposes standards-based SAML identity-provider services.",
                "saml");
    }

    /**
     * Returns the SAML identity-provider category.
     *
     * @return SAML identity-provider type
     */
    @Override
    public Protocol protocol() {
        return Protocol.SAML;
    }

    /**
     * Returns the immutable potential capability manifest.
     *
     * @return SAML identity-provider capabilities
     */
    @Override
    public Capability.Manifest manifest() {
        return MANIFEST;
    }

    /**
     * Returns the formal SAML conformance declaration.
     *
     * @return present conformance declaration
     */
    @Override
    public Optional<Conformance> conformance() {
        return Optional.of(CONFORMANCE);
    }

    /**
     * Returns the external management form for SAML identity-provider options.
     *
     * @return immutable SAML identity-provider form
     */
    @Override
    public Form form() {
        return FORM;
    }

    /**
     * Returns no deployment defaults because entity identifiers, endpoints, and keys are external data.
     *
     * @return empty options defaults
     */
    @Override
    public Optional<SamlServerOptions> defaults() {
        return Optional.empty();
    }

}
