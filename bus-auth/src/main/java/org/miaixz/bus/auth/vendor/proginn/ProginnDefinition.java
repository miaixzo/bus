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
package org.miaixz.bus.auth.vendor.proginn;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2SourceProfile;
import org.miaixz.bus.auth.source.SourceAuthentication;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorDefinition;
import org.miaixz.bus.auth.vendor.VendorDeviation;
import org.miaixz.bus.auth.vendor.VendorTargets;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.tls.TlsClientAuth;
import org.miaixz.bus.fabric.UnoUrl;

/**
 * Declares the Proginn OAuth 2.0 browser Vendor definition.
 * <p>
 * Authorization and token operations use standard OAuth models and the shared client implementation. Only Proginn's
 * access-token query profile transport remains a registered private Source-authentication deviation.
 * </p>
 *
 * @author Kimi Liu
 */
public final class ProginnDefinition implements VendorDefinition<ProginnSourceSettings> {

    /**
     * Stable Proginn platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("proginn");

    /**
     * Stable identifier of the sole Proginn variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant("default");

    /**
     * Exact Source authentication and public OAuth operations exposed by Proginn.
     */
    private static final Capability.Manifest MANIFEST = new Capability.Manifest(List.of(
            SourceAuthentication.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceAuthentication.complete(Set.of(Capability.Interaction.REDIRECT)),
            OAuth2SourceProfile.AUTHORIZATION,
            OAuth2SourceProfile.TOKEN));

    /**
     * Complete immutable Proginn endpoint, client, scope, capability, form, and deviation definition.
     */
    private static final VendorDefinition.Definition DEFINITION = new VendorDefinition.Definition(ID, DEFAULT,
            Protocol.OAUTH2, List.of("basic"),
            new VendorTargets(Optional.of(
                    fixed("https://www.proginn.com/oauth2/authorize", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.empty(),
                    Optional.of(
                            fixed(
                                    "https://www.proginn.com/oauth2/access_token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_POST)),
                    Optional.of(
                            fixed(
                                    "https://www.proginn.com/openapi/user/basic_info",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty()),
            MANIFEST,
            List.of(
                    new VendorDeviation("source_authentication.complete", VendorDeviation.Location.QUERY,
                            OAuth2.Parameters.ACCESS_TOKEN, Optional.of(Http.Header.AUTHORIZATION), Optional.empty(),
                            Http.Method.GET, false)));

    /**
     * Creates the stateless Proginn definition used by Vendor directory assembly.
     */
    public ProginnDefinition() {
        // No initialization required.
        // Immutable definition state is retained by class constants.
    }

    /**
     * Creates one fixed Proginn HTTPS endpoint.
     *
     * @param value          exact credential-free endpoint URL
     * @param method         operation HTTP method
     * @param authentication endpoint authentication declaration
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
     * Returns the stable Proginn routing identifier.
     *
     * @return Proginn platform identifier
     */
    @Override
    public Vendor.Id type() {
        return ID;
    }

    /**
     * Returns non-sensitive Proginn presentation metadata.
     *
     * @return immutable Proginn management metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("Proginn", "Proginn account authorization", "proginn");
    }

    /**
     * Returns the exact externally decoded settings record.
     *
     * @return Proginn Source settings class
     */
    @Override
    public Class<ProginnSourceSettings> settingsType() {
        return ProginnSourceSettings.class;
    }

    /**
     * Returns the sole supported Proginn variant.
     *
     * @return immutable single-element definition list
     */
    @Override
    public List<VendorDefinition.Definition> variants() {
        return List.of(DEFINITION);
    }

    /**
     * Resolves the exact default Proginn definition.
     *
     * @param variant requested Proginn variant
     * @return immutable default definition
     * @throws ValidateException if the requested variant is unsupported
     */
    @Override
    public VendorDefinition.Definition variant(final Vendor.Variant variant) {
        if (!DEFAULT.equals(variant)) {
            throw new ValidateException("Proginn Vendor variant is not supported");
        }
        return DEFINITION;
    }

}
