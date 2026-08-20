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
package org.miaixz.bus.auth.vendor.afdian;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Endpoint;
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
 * Declares the frozen Afdian creator authorization Vendor definition.
 *
 * @author Kimi Liu
 */
public final class AfdianDefinition implements VendorDefinition<AfdianSourceSettings> {

    /**
     * Stable Afdian platform identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("afdian");
    /**
     * Stable default Afdian authorization variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant("default");
    /**
     * Afdian browser-only Source authentication manifest.
     */
    private static final Capability.Manifest MANIFEST = new Capability.Manifest(List.of(
            SourceAuthentication.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceAuthentication.complete(Set.of(Capability.Interaction.REDIRECT))));
    /**
     * Complete immutable Afdian variant definition.
     */
    private static final VendorDefinition.Definition DEFINITION = new VendorDefinition.Definition(ID, DEFAULT,
            Protocol.VENDOR_AUTH, List.of("basic"),
            new VendorTargets(Optional
                    .of(fixed("https://afdian.net/oauth2/authorize", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.empty(),
                    Optional.of(
                            fixed(
                                    "https://afdian.net/api/oauth2/access_token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_POST)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty()),
            MANIFEST, List.of());

    /**
     * Creates the stateless Afdian definition that exposes its immutable compiled definition.
     */
    public AfdianDefinition() {
        // No initialization required.
    }

    /**
     * Creates one fixed public HTTPS endpoint.
     *
     * @param value          endpoint URL
     * @param method         HTTP method
     * @param authentication endpoint authentication method
     * @return immutable fixed Vendor endpoint
     */
    private static VendorTargets.Fixed fixed(
            final String value,
            final Http.Method method,
            final Endpoint.Authentication authentication) {
        return new VendorTargets.Fixed(new Endpoint(UnoUrl.parse(value), Endpoint.Transport.HTTPS, Optional.of(method),
                Set.of(authentication), Optional.empty(), TlsClientAuth.NONE));
    }

    /**
     * Returns the stable Afdian platform identifier.
     *
     * @return Afdian identifier
     */
    @Override
    public Vendor.Id type() {
        return ID;
    }

    /**
     * Returns Afdian management presentation metadata.
     *
     * @return immutable Afdian metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("Afdian", "Afdian creator account authorization", "afdian");
    }

    /**
     * Returns the exact settings record decoded for Afdian.
     *
     * @return Afdian settings class
     */
    @Override
    public Class<AfdianSourceSettings> settingsType() {
        return AfdianSourceSettings.class;
    }

    /**
     * Returns the sole frozen Afdian authorization variant.
     *
     * @return immutable variant list
     */
    @Override
    public List<VendorDefinition.Definition> variants() {
        return List.of(DEFINITION);
    }

    /**
     * Returns the exact frozen Afdian variant definition.
     *
     * @param variant requested variant
     * @return default Afdian definition
     * @throws ValidateException if the variant is not default
     */
    @Override
    public VendorDefinition.Definition variant(final Vendor.Variant variant) {
        if (!DEFAULT.equals(variant)) {
            throw new ValidateException("Afdian Vendor variant is not supported");
        }
        return DEFINITION;
    }

}
