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
package org.miaixz.bus.auth.vendor.gitee;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2ClientScheme;
import org.miaixz.bus.auth.source.SourceAuthentication;
import org.miaixz.bus.auth.vendor.VariantManifest;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorDeviation;
import org.miaixz.bus.auth.vendor.VendorTargets;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.tls.TlsClientAuth;
import org.miaixz.bus.fabric.UnoUrl;

/**
 * Declares the frozen Gitee OAuth browser Vendor manifest.
 * <p>
 * Only the authorization endpoint is published as standard OAuth. Gitee's incompletely specified token envelope,
 * unauthenticated refresh request, and query-carried resource token remain private Source-authentication behavior.
 * </p>
 *
 * @author Kimi Liu
 */
public final class GiteeManifest implements VariantManifest<GiteeOptions> {

    /**
     * Stable Gitee platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("gitee");

    /**
     * Stable identifier of the sole Gitee.com OAuth variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant("default");

    /**
     * Gitee resource authentication that carries the token in a query parameter.
     */
    private static final Endpoint.Authentication QUERY_ACCESS_TOKEN = new Endpoint.Authentication("query_access_token");

    /**
     * Exact Source and standard OAuth capabilities of the default variant.
     */
    private static final Capability.Manifest CAPABILITIES = new Capability.Manifest(List.of(
            SourceAuthentication.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceAuthentication.complete(Set.of(Capability.Interaction.REDIRECT)),
            OAuth2ClientScheme.AUTHORIZATION));

    /**
     * Exact Gitee wire differences retained only inside Source authentication.
     */
    private static final List<VendorDeviation> DEVIATIONS = List.of(
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.RESPONSE,
                    "created_at",
                    null,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.QUERY,
                    OAuth2.Parameters.ACCESS_TOKEN,
                    OAuth2.Parameters.ACCESS_TOKEN,
                    Optional.empty(),
                    Http.Method.GET,
                    false));

    /**
     * Complete immutable endpoint, client, scope, capability, form, and deviation manifest.
     */
    private static final VariantManifest.Variant VARIANT = new VariantManifest.Variant(ID, DEFAULT, Protocol.OAUTH2,
            List.of("user_info"),
            new VendorTargets(
                    Optional.of(
                            fixed("https://gitee.com/oauth/authorize", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed(
                                    "https://gitee.com/oauth/token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_POST)),
                    Optional.of(fixed("https://gitee.com/api/v5/user", Http.Method.GET, QUERY_ACCESS_TOKEN)),
                    Optional.of(fixed("https://gitee.com/oauth/token", Http.Method.POST, Endpoint.Authentication.NONE)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty()),
            CAPABILITIES, DEVIATIONS);

    /**
     * Creates the stateless Gitee manifest used by Vendor directory assembly.
     */
    public GiteeManifest() {
        // No initialization required.
        // All manifest state is held by immutable class constants.
    }

    /**
     * Creates one fixed credential-free HTTPS endpoint.
     *
     * @param value          exact endpoint URL
     * @param method         exact HTTP method
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
     * Creates one immutable registered Gitee wire deviation.
     *
     * @param operation    affected Source operation
     * @param location     exact wire location
     * @param vendorName   exact Gitee field name
     * @param standardName corresponding standard field, or {@code null}
     * @param mediaType    exact representation when applicable
     * @param method       exact HTTP method
     * @param enveloped    whether Gitee wraps the response
     * @return immutable deviation declaration
     */
    private static VendorDeviation deviation(
            final String operation,
            final VendorDeviation.Location location,
            final String vendorName,
            final String standardName,
            final Optional<MediaType> mediaType,
            final Http.Method method,
            final boolean enveloped) {
        return new VendorDeviation(operation, location, vendorName, Optional.ofNullable(standardName), mediaType,
                method, enveloped);
    }

    /**
     * Returns the stable Gitee routing identifier.
     *
     * @return stable platform identifier
     */
    @Override
    public Vendor.Id vendor() {
        return ID;
    }

    /**
     * Returns non-sensitive Gitee presentation metadata.
     *
     * @return immutable management metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("Gitee", "Gitee third-party application login", "gitee");
    }

    /**
     * Returns the sole supported Gitee variant.
     *
     * @return immutable single-element manifest list
     */
    @Override
    public List<VariantManifest.Variant> variants() {
        return List.of(VARIANT);
    }

    /**
     * Returns the exact default Gitee manifest.
     *
     * @param variant requested variant
     * @return exact immutable manifest
     * @throws ValidateException if the requested variant is unsupported
     */
    @Override
    public VariantManifest.Variant variant(final Vendor.Variant variant) {
        if (!DEFAULT.equals(variant)) {
            throw new ValidateException("Gitee Vendor variant is not supported");
        }
        return VARIANT;
    }

}
