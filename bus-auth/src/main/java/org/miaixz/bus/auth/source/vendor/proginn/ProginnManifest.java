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
package org.miaixz.bus.auth.source.vendor.proginn;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.Scheme;
import org.miaixz.bus.auth.source.SourceWorkflow;
import org.miaixz.bus.auth.source.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.source.protocol.oauth2.client.OAuth2ClientScheme;
import org.miaixz.bus.auth.source.vendor.Vendor;
import org.miaixz.bus.auth.source.vendor.VendorDeviation;
import org.miaixz.bus.auth.source.vendor.VendorManifest;
import org.miaixz.bus.auth.source.vendor.VendorTargets;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.tls.TlsClientAuth;

/**
 * Declares the Proginn OAuth 2.0 browser Vendor manifest.
 * <p>
 * Authorization and token operations use standard OAuth models and the shared client implementation. Only Proginn's
 * access-token query profile transport remains a registered private Source-authentication deviation.
 * </p>
 *
 * @author Kimi Liu
 */
public class ProginnManifest implements VendorManifest<ProginnOptions> {

    /**
     * Stable Proginn platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("proginn");

    /**
     * Stable identifier of the sole Proginn variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant(Normal.DEFAULT);

    /**
     * Exact Source authentication and public OAuth operations exposed by Proginn.
     */
    private static final Capability.Manifest CAPABILITIES = new Capability.Manifest(List.of(
            SourceWorkflow.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceWorkflow.complete(Set.of(Capability.Interaction.REDIRECT)),
            OAuth2ClientScheme.AUTHORIZATION,
            OAuth2ClientScheme.TOKEN));

    /**
     * Complete immutable Proginn endpoint, client, scope, capability, form, and deviation manifest.
     */
    private static final VendorManifest.Variant VARIANT = new VendorManifest.Variant(ID, DEFAULT, Protocol.OAUTH2,
            VendorManifest.Pkce.DISABLED, Credential.Type.CLIENT_SECRET, List.of("basic"),
            new VendorTargets(Optional.of(
                    fixed("https://www.proginn.com/oauth2/authorize", Http.Method.GET, Endpoint.Authentication.NONE)),
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
            CAPABILITIES,
            List.of(
                    new VendorDeviation(Builder.SOURCE_AUTHENTICATION_COMPLETE, VendorDeviation.Location.QUERY,
                            OAuth2.Parameters.ACCESS_TOKEN, Optional.of(Http.Header.AUTHORIZATION), Optional.empty(),
                            Http.Method.GET, false)));

    /**
     * Creates the stateless Proginn manifest used by Vendor directory assembly.
     */
    public ProginnManifest() {
        // No initialization required.
        // Immutable manifest state is retained by class constants.
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
        return new VendorTargets.Fixed(new Endpoint(Url.parse(value), Endpoint.Transport.HTTPS, Optional.of(method),
                Set.of(authentication), Optional.empty(), TlsClientAuth.NONE));
    }

    /**
     * Returns the stable Proginn routing identifier.
     *
     * @return Proginn platform identifier
     */
    @Override
    public Vendor.Id vendor() {
        return ID;
    }

    /**
     * Returns non-sensitive Proginn presentation metadata.
     *
     * @return immutable Proginn management metadata
     */
    @Override
    public Scheme.Metadata metadata() {
        return new Scheme.Metadata("Proginn", "Proginn account authorization", "proginn");
    }

    /**
     * Returns the sole supported Proginn variant.
     *
     * @return immutable single-element manifest list
     */
    @Override
    public List<VendorManifest.Variant> variants() {
        return List.of(VARIANT);
    }

    /**
     * Resolves the exact default Proginn manifest.
     *
     * @param variant requested Proginn variant
     * @return immutable default manifest
     * @throws ValidateException if the requested variant is unsupported
     */
    @Override
    public VendorManifest.Variant variant(final Vendor.Variant variant) {
        if (!DEFAULT.equals(variant)) {
            throw new ValidateException("Proginn Vendor variant is not supported");
        }
        return VARIANT;
    }

}
