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
package org.miaixz.bus.auth.source.vendor.afdian;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.Scheme;
import org.miaixz.bus.auth.source.SourceWorkflow;
import org.miaixz.bus.auth.source.vendor.Vendor;
import org.miaixz.bus.auth.source.vendor.VendorManifest;
import org.miaixz.bus.auth.source.vendor.VendorTargets;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.tls.TlsClientAuth;

/**
 * Declares the frozen Afdian creator authorization Vendor manifest.
 *
 * @author Kimi Liu
 */
public class AfdianManifest implements VendorManifest<AfdianOptions> {

    /**
     * Stable Afdian platform identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("afdian");
    /**
     * Stable default Afdian authorization variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant(Normal.DEFAULT);
    /**
     * Afdian browser-only Source authentication manifest.
     */
    private static final Capability.Manifest CAPABILITIES = new Capability.Manifest(List.of(
            SourceWorkflow.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceWorkflow.complete(Set.of(Capability.Interaction.REDIRECT))));
    /**
     * Complete immutable Afdian variant manifest.
     */
    private static final VendorManifest.Variant VARIANT = new VendorManifest.Variant(ID, DEFAULT, Protocol.HTTPS,
            VendorManifest.Pkce.DISABLED, Credential.Type.CLIENT_SECRET, List.of("basic"),
            new VendorTargets(Optional
                    .of(fixed("https://afdian.net/oauth2/authorize", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed(
                                    "https://afdian.net/api/oauth2/access_token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_POST)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty()),
            CAPABILITIES, List.of());

    /**
     * Creates the stateless Afdian manifest that exposes its immutable compiled manifest.
     */
    public AfdianManifest() {
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
        return new VendorTargets.Fixed(new Endpoint(Url.parse(value), Endpoint.Transport.HTTPS, Optional.of(method),
                Set.of(authentication), Optional.empty(), TlsClientAuth.NONE));
    }

    /**
     * Returns the stable Afdian platform identifier.
     *
     * @return Afdian identifier
     */
    @Override
    public Vendor.Id vendor() {
        return ID;
    }

    /**
     * Returns Afdian management presentation metadata.
     *
     * @return immutable Afdian metadata
     */
    @Override
    public Scheme.Metadata metadata() {
        return new Scheme.Metadata("Afdian", "Afdian creator account authorization", "afdian");
    }

    /**
     * Returns the sole frozen Afdian authorization variant.
     *
     * @return immutable variant list
     */
    @Override
    public List<VendorManifest.Variant> variants() {
        return List.of(VARIANT);
    }

    /**
     * Returns the exact frozen Afdian variant manifest.
     *
     * @param variant requested variant
     * @return default Afdian manifest
     * @throws ValidateException if the variant is not default
     */
    @Override
    public VendorManifest.Variant variant(final Vendor.Variant variant) {
        if (!DEFAULT.equals(variant)) {
            throw new ValidateException("Afdian Vendor variant is not supported");
        }
        return VARIANT;
    }

}
