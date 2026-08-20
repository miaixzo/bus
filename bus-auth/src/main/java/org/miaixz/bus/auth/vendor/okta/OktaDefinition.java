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
package org.miaixz.bus.auth.vendor.okta;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.protocol.oidc.client.OpenIdSourceProfile;
import org.miaixz.bus.auth.source.SourceAuthentication;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorDefinition;
import org.miaixz.bus.auth.vendor.VendorTargets;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.tls.TlsClientAuth;

/**
 * Declares the Okta custom authorization-server OpenID Connect Vendor definition.
 * <p>
 * The organization label and authorization-server identifier are deployment selectors, while every host suffix,
 * endpoint path, HTTP method, client authentication method, scope default, and signing algorithm remains frozen by this
 * immutable profile. The resolved issuer is used unchanged by Discovery, authorization-response, and ID Token
 * validation.
 * </p>
 *
 * @author Kimi Liu
 */
public final class OktaDefinition implements VendorDefinition<OktaSourceSettings> {

    /**
     * Stable Okta platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("okta");

    /**
     * Stable identifier of the custom authorization-server variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant("default");

    /**
     * Definition-owned issuer template resolved from the two external Okta selectors.
     */
    private static final String ISSUER = "https://{instance}.okta.com/oauth2/{authorizationServerId}";

    /**
     * Exact Source authentication and standard OIDC operations exposed by the Okta adapter.
     */
    private static final Capability.Manifest MANIFEST = new Capability.Manifest(List.of(
            SourceAuthentication.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceAuthentication.complete(Set.of(Capability.Interaction.REDIRECT)),
            OpenIdSourceProfile.AUTHENTICATION,
            OpenIdSourceProfile.TOKEN,
            OpenIdSourceProfile.REVOCATION,
            OpenIdSourceProfile.DISCOVERY,
            OpenIdSourceProfile.JWK_SET,
            OpenIdSourceProfile.USERINFO));

    /**
     * Historical default Okta login scopes preserved in deterministic request order.
     */
    private static final List<String> DEFAULT_SCOPES = List
            .of("openid", "profile", "email", "address", "phone", "offline_access");

    /**
     * Complete immutable Okta endpoint, client, scope, capability, and form definition.
     */
    private static final VendorDefinition.Definition DEFINITION = new VendorDefinition.Definition(ID, DEFAULT,
            Protocol.OIDC, DEFAULT_SCOPES,
            new VendorTargets(
                    Optional.of(template(ISSUER + "/v1/authorize", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.empty(),
                    Optional.of(
                            template(
                                    ISSUER + "/v1/token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_BASIC)),
                    Optional.of(template(ISSUER + "/v1/userinfo", Http.Method.GET, Endpoint.Authentication.BEARER)),
                    Optional.of(
                            template(
                                    ISSUER + "/v1/token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_BASIC)),
                    Optional.empty(),
                    Optional.of(
                            template(
                                    ISSUER + "/v1/revoke",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_BASIC)),
                    Optional.empty(),
                    Optional.of(
                            template(
                                    ISSUER + "/.well-known/openid-configuration",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.of(template(ISSUER + "/v1/keys", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.empty()),
            MANIFEST, List.of());

    /**
     * Creates the stateless Okta definition used by Vendor directory assembly.
     */
    public OktaDefinition() {
        // No initialization required.
        // Immutable definition state is retained by class constants.
    }

    /**
     * Creates one constrained Okta HTTPS endpoint template.
     *
     * @param value          definition-owned endpoint template
     * @param method         standard operation HTTP method
     * @param authentication endpoint authentication declaration
     * @return immutable constrained endpoint target
     */
    private static VendorTargets.Template template(
            final String value,
            final Http.Method method,
            final Endpoint.Authentication authentication) {
        return new VendorTargets.Template(value, method, Set.of(authentication), Optional.empty(), TlsClientAuth.NONE);
    }

    /**
     * Returns the stable Okta routing identifier.
     *
     * @return Okta platform identifier
     */
    @Override
    public Vendor.Id type() {
        return ID;
    }

    /**
     * Returns non-sensitive Okta management metadata.
     *
     * @return immutable Okta presentation metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("Okta", "Okta custom authorization-server sign-in", "okta");
    }

    /**
     * Returns the exact externally decoded Okta settings record.
     *
     * @return Okta Source settings class
     */
    @Override
    public Class<OktaSourceSettings> settingsType() {
        return OktaSourceSettings.class;
    }

    /**
     * Returns the sole supported Okta custom authorization-server variant.
     *
     * @return immutable single-element definition list
     */
    @Override
    public List<VendorDefinition.Definition> variants() {
        return List.of(DEFINITION);
    }

    /**
     * Resolves the exact default Okta definition.
     *
     * @param variant requested Okta variant
     * @return immutable default definition
     * @throws ValidateException if the requested variant is unsupported
     */
    @Override
    public VendorDefinition.Definition variant(final Vendor.Variant variant) {
        if (!DEFAULT.equals(variant)) {
            throw new ValidateException("Okta Vendor variant is not supported");
        }
        return DEFINITION;
    }

}
