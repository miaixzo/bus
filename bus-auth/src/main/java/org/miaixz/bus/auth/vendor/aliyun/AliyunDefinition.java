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
package org.miaixz.bus.auth.vendor.aliyun;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.protocol.oidc.OpenIdConnect;
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
import org.miaixz.bus.fabric.UnoUrl;

/**
 * Declares the frozen Alibaba Cloud OpenID Connect Vendor definition.
 *
 * @author Kimi Liu
 */
public final class AliyunDefinition implements VendorDefinition<AliyunSourceSettings> {

    /**
     * Stable platform routing identifier shared by registration, catalog, and runtime compilation.
     */
    public static final Vendor.Id ID = new Vendor.Id("aliyun");

    /**
     * Internal identifier of the single Alibaba Cloud OpenID Connect variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant("default");

    /**
     * Exact public operations supported by the compiled Alibaba Cloud Source.
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
     * Complete immutable endpoint, client-policy, scope, capability, and form definition for the default variant.
     */
    private static final VendorDefinition.Definition DEFINITION = new VendorDefinition.Definition(ID, DEFAULT,
            Protocol.OIDC, List.of(OpenIdConnect.Scopes.OPENID, OpenIdConnect.Scopes.PROFILE),
            new VendorTargets(Optional.of(
                    fixed("https://signin.aliyun.com/oauth2/v1/auth", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.empty(),
                    Optional.of(
                            fixed(
                                    "https://oauth.aliyun.com/v1/token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_POST)),
                    Optional.of(
                            fixed(
                                    "https://oauth.aliyun.com/v1/userinfo",
                                    Http.Method.GET,
                                    Endpoint.Authentication.BEARER)),
                    Optional.of(
                            fixed(
                                    "https://oauth.aliyun.com/v1/token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_POST)),
                    Optional.empty(),
                    Optional.of(
                            fixed(
                                    "https://oauth.aliyun.com/v1/revoke",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_POST)),
                    Optional.empty(),
                    Optional.of(
                            fixed(
                                    "https://oauth.aliyun.com/.well-known/openid-configuration",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed("https://oauth.aliyun.com/v1/keys", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.empty()),
            MANIFEST, List.of());

    /**
     * Creates the stateless Alibaba Cloud definition used by Vendor directory assembly.
     */
    public AliyunDefinition() {
        // No initialization required.
        // All definition state is held by immutable class constants.
    }

    /**
     * Creates one fixed HTTPS endpoint.
     *
     * @param value          exact credential-free endpoint URL
     * @param method         HTTP method used by the standard operation
     * @param authentication endpoint authentication
     * @return immutable fixed endpoint target
     */
    private static VendorTargets.Fixed fixed(
            final String value,
            final Http.Method method,
            final Endpoint.Authentication authentication) {
        return new VendorTargets.Fixed(new Endpoint(UnoUrl.parse(value), Endpoint.Transport.HTTPS, Optional.of(method),
                Set.of(authentication), Optional.empty(), TlsClientAuth.NONE));
    }

    /**
     * Returns the stable platform identifier used to select this definition.
     *
     * @return stable Alibaba Cloud routing identifier
     */
    @Override
    public Vendor.Id type() {
        return ID;
    }

    /**
     * Returns non-sensitive Alibaba Cloud presentation metadata for external management catalogs.
     *
     * @return immutable Alibaba Cloud presentation metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("Alibaba Cloud", "Alibaba Cloud OpenID Connect sign-in", "aliyun");
    }

    /**
     * Returns the exact immutable settings record accepted by this definition.
     *
     * @return Alibaba Cloud Source settings class
     */
    @Override
    public Class<AliyunSourceSettings> settingsType() {
        return AliyunSourceSettings.class;
    }

    /**
     * Returns the sole supported Alibaba Cloud OpenID Connect variant.
     *
     * @return immutable single-element variant definition list
     */
    @Override
    public List<VendorDefinition.Definition> variants() {
        return List.of(DEFINITION);
    }

    /**
     * Returns the exact default definition.
     *
     * @param variant requested variant
     * @return exact default Alibaba Cloud definition
     * @throws ValidateException if the requested variant is not supported
     */
    @Override
    public VendorDefinition.Definition variant(final Vendor.Variant variant) {
        if (!DEFAULT.equals(variant)) {
            throw new ValidateException("Alibaba Cloud Vendor variant is not supported");
        }
        return DEFINITION;
    }

}
