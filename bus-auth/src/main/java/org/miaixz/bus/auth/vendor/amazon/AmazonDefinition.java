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
package org.miaixz.bus.auth.vendor.amazon;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2SourceProfile;
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
 * Declares the frozen Login with Amazon OAuth 2.0 Vendor definition.
 *
 * @author Kimi Liu
 */
public final class AmazonDefinition implements VendorDefinition<AmazonSourceSettings> {

    /**
     * Stable platform routing identifier shared by registration, catalog, and runtime compilation.
     */
    public static final Vendor.Id ID = new Vendor.Id("amazon");

    /**
     * Internal identifier of the single Login with Amazon variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant("default");

    /**
     * Exact public operations supported by the compiled Login with Amazon Source.
     */
    private static final Capability.Manifest MANIFEST = new Capability.Manifest(List.of(
            SourceAuthentication.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceAuthentication.complete(Set.of(Capability.Interaction.REDIRECT)),
            OAuth2SourceProfile.AUTHORIZATION,
            OAuth2SourceProfile.TOKEN));

    /**
     * Complete immutable endpoint, policy, scope, capability, and form definition.
     */
    private static final VendorDefinition.Definition DEFINITION = new VendorDefinition.Definition(ID, DEFAULT,
            Protocol.OAUTH2, List.of("profile", "profile:user_id", "postal_code"),
            new VendorTargets(
                    Optional.of(fixed("https://www.amazon.com/ap/oa", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.empty(),
                    Optional.of(
                            fixed(
                                    "https://api.amazon.com/auth/o2/token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_POST)),
                    Optional.of(
                            fixed(
                                    "https://api.amazon.com/user/profile",
                                    Http.Method.GET,
                                    Endpoint.Authentication.BEARER)),
                    Optional.of(
                            fixed(
                                    "https://api.amazon.com/auth/o2/token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_POST)),
                    Optional.of(
                            fixed(
                                    "https://api.amazon.com/auth/o2/tokeninfo",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()),
            MANIFEST, List.of());

    /**
     * Creates the stateless Login with Amazon definition used by Vendor directory assembly.
     */
    public AmazonDefinition() {
        // No initialization required.
        // All definition state is held by immutable class constants.
    }

    /**
     * Creates one fixed HTTPS endpoint.
     *
     * @param value          exact credential-free endpoint URL
     * @param method         HTTP method used by the operation
     * @param authentication endpoint authentication method
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
     * @return stable Amazon routing identifier
     */
    @Override
    public Vendor.Id type() {
        return ID;
    }

    /**
     * Returns non-sensitive presentation metadata for external management catalogs.
     *
     * @return immutable Login with Amazon presentation metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("Amazon", "Login with Amazon OAuth 2.0 sign-in", "amazon");
    }

    /**
     * Returns the exact immutable settings record accepted by this definition.
     *
     * @return Login with Amazon Source settings class
     */
    @Override
    public Class<AmazonSourceSettings> settingsType() {
        return AmazonSourceSettings.class;
    }

    /**
     * Returns the sole supported Login with Amazon variant.
     *
     * @return immutable single-element definition list
     */
    @Override
    public List<VendorDefinition.Definition> variants() {
        return List.of(DEFINITION);
    }

    /**
     * Returns the exact default definition.
     *
     * @param variant requested variant
     * @return exact Login with Amazon definition
     * @throws ValidateException if the requested variant is not supported
     */
    @Override
    public VendorDefinition.Definition variant(final Vendor.Variant variant) {
        if (!DEFAULT.equals(variant)) {
            throw new ValidateException("Login with Amazon Vendor variant is not supported");
        }
        return DEFINITION;
    }

}
