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
package org.miaixz.bus.auth.protocol.saml.client;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Scheme;
import org.miaixz.bus.auth.Scheme.Form;
import org.miaixz.bus.auth.protocol.Conformance;
import org.miaixz.bus.auth.protocol.saml.AuthnRequest;
import org.miaixz.bus.auth.protocol.saml.EntityDescriptor;
import org.miaixz.bus.auth.protocol.saml.LogoutRequest;
import org.miaixz.bus.auth.protocol.saml.Saml;
import org.miaixz.bus.core.Version;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.UnoUrl;

/**
 * Declares the generic SAML 2.0 service-provider client scheme.
 * <p>
 * The profile implements service-provider initiated Web Browser SSO and Single Logout using HTTP-Redirect, receives
 * identity-provider responses using HTTP-POST, and retrieves SAML Metadata. Runtime manifests may omit Single Logout
 * when the compiled Source does not configure its endpoint.
 * </p>
 *
 * @author Kimi Liu
 */
public final class SamlClientScheme implements Scheme<SamlClientOptions> {

    /**
     * Stable registration type identifier for generic SAML Sources.
     */
    public static final String ID = "saml";
    /**
     * Builds an HTTP-Redirect URL carrying a standard SAML Authentication Request.
     */
    public static final Capability<AuthnRequest, UnoUrl> SINGLE_SIGN_ON = capability(
            Saml.SINGLE_SIGN_ON,
            AuthnRequest.class,
            UnoUrl.class,
            Capability.Interaction.REDIRECT);
    /**
     * Builds an HTTP-Redirect URL carrying a standard SAML Logout Request.
     */
    public static final Capability<LogoutRequest, UnoUrl> SINGLE_LOGOUT = capability(
            Saml.SINGLE_LOGOUT,
            LogoutRequest.class,
            UnoUrl.class,
            Capability.Interaction.REDIRECT);
    /**
     * Retrieves the trusted identity-provider SAML Metadata document.
     */
    public static final Capability<Void, EntityDescriptor> METADATA = capability(
            Saml.METADATA,
            Void.class,
            EntityDescriptor.class,
            Capability.Interaction.DIRECT);
    /**
     * Potential service-provider operations before endpoint-specific runtime narrowing.
     */
    private static final Capability.Manifest MANIFEST = new Capability.Manifest(
            List.of(SINGLE_SIGN_ON, SINGLE_LOGOUT, METADATA));
    /**
     * Formal specifications implemented by this service-server scheme.
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
            "SAML 2.0 Service Provider");
    /**
     * External management form containing only deployment options owned by a SAML Source.
     */
    private static final Form FORM = new Form(List.of(
            new Form.Section("saml-service-provider", "SAML 2.0 Service Provider", List.of(
                    field("entity_id", "Service provider entity ID", Form.Type.URL, true),
                    field("identity_provider_entity_id", "Identity provider entity ID", Form.Type.URL, true),
                    field(
                            "identity_provider_metadata_endpoint",
                            "Identity provider metadata endpoint",
                            Form.Type.URL,
                            true),
                    field(
                            "single_sign_on_service_endpoint",
                            "Identity provider Single Sign-On endpoint",
                            Form.Type.URL,
                            true),
                    field("assertion_consumer_service_url", "Assertion Consumer Service URL", Form.Type.URL, true),
                    field("single_logout_service_url", "Single Logout Service URL", Form.Type.URL, false),
                    field("signing_key_id", "Signing key identifier", Form.Type.SECRET, true),
                    field("want_assertions_signed", "Require signed assertions", Form.Type.BOOLEAN, true),
                    field("want_responses_signed", "Require signed responses", Form.Type.BOOLEAN, true)))));

    /**
     * Creates the stateless SAML service-server scheme used to compile source registrations.
     */
    public SamlClientScheme() {
        // No initialization required.
    }

    /**
     * Creates one Source-direction SAML capability.
     *
     * @param key          direction-neutral SAML operation key
     * @param requestType  exact request class
     * @param responseType exact success class
     * @param interaction  required interaction pattern
     * @param <Q>          request type
     * @param <S>          success type
     * @return immutable Source capability
     */
    private static <Q, S> Capability<Q, S> capability(
            final Capability.Key key,
            final Class<Q> requestType,
            final Class<S> responseType,
            final Capability.Interaction interaction) {
        return new Capability<>(key, requestType, responseType, Capability.Direction.SOURCE, Set.of(interaction),
                Capability.Security.PUBLIC);
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
     * Returns the stable generic SAML client scheme identifier.
     *
     * @return {@value #ID}
     */
    @Override
    public String id() {
        return ID;
    }

    /**
     * Returns the SAML service-provider Source category.
     *
     * @return SAML Source type
     */
    @Override
    public Protocol protocol() {
        return Protocol.SAML;
    }

    /**
     * Returns the immutable potential capability manifest.
     *
     * @return SAML Source capabilities
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
     * Returns the external management form for SAML service-provider options.
     *
     * @return immutable SAML Source form
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
    public Optional<SamlClientOptions> defaults() {
        return Optional.empty();
    }

}
