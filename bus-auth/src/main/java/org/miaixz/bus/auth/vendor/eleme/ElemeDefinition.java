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
package org.miaixz.bus.auth.vendor.eleme;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2SourceProfile;
import org.miaixz.bus.auth.source.SourceAuthentication;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorDefinition;
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
 * Declares the frozen Eleme service-provider OAuth 2.0 Vendor definition.
 * <p>
 * Authorization, token, and refresh operations retain their standard OAuth models. Merchant identity is resolved only
 * inside Source authentication through the platform's MD5-signed JSON RPC and is never exposed as UserInfo.
 * </p>
 *
 * @author Kimi Liu
 */
public final class ElemeDefinition implements VendorDefinition<ElemeSourceSettings> {

    /**
     * Stable Eleme platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("eleme");

    /**
     * Stable identifier of the sole service-provider variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant("default");

    /**
     * Exact Source and standard OAuth capabilities of the variant.
     */
    private static final Capability.Manifest MANIFEST = new Capability.Manifest(List.of(
            SourceAuthentication.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceAuthentication.complete(Set.of(Capability.Interaction.REDIRECT)),
            OAuth2SourceProfile.AUTHORIZATION,
            OAuth2SourceProfile.TOKEN));

    /**
     * Exact merchant RPC deviations retained only inside Source authentication.
     */
    private static final List<VendorDeviation> DEVIATIONS = List.of(
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.JSON,
                    "nop",
                    null,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    true),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.JSON,
                    "metas",
                    null,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    true),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.JSON,
                    "signature",
                    null,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    true),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.RESPONSE,
                    "result",
                    null,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    true));

    /**
     * Complete immutable endpoint, client policy, scope, capability, form, and deviation definition.
     */
    private static final VendorDefinition.Definition DEFINITION = new VendorDefinition.Definition(ID, DEFAULT,
            Protocol.OAUTH2, List.of("all"),
            new VendorTargets(Optional
                    .of(fixed("https://open-api.shop.ele.me/authorize", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.empty(),
                    Optional.of(
                            fixed(
                                    "https://open-api.shop.ele.me/token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_BASIC)),
                    Optional.of(
                            fixed(
                                    "https://open-api.shop.ele.me/api/v1/",
                                    Http.Method.POST,
                                    Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed(
                                    "https://open-api.shop.ele.me/token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_BASIC)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty()),
            MANIFEST, DEVIATIONS);

    /**
     * Creates the stateless Eleme definition used by Vendor directory assembly.
     */
    public ElemeDefinition() {
        // No initialization required.
        // All definition state is held by immutable class constants.
    }

    /**
     * Creates one fixed credential-free HTTPS endpoint.
     *
     * @param value          exact endpoint URL
     * @param method         exact HTTP method
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
     * Creates one immutable Eleme merchant RPC deviation.
     *
     * @param operation    affected Source operation
     * @param location     exact wire location
     * @param vendorName   exact platform field or envelope name
     * @param standardName corresponding standard field, or {@code null}
     * @param mediaType    exact representation
     * @param method       exact HTTP method
     * @param enveloped    whether the platform wraps the result
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
     * Returns the stable Eleme routing identifier.
     *
     * @return stable platform identifier
     */
    @Override
    public Vendor.Id type() {
        return ID;
    }

    /**
     * Returns non-sensitive Eleme presentation metadata.
     *
     * @return immutable management metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("Eleme", "Eleme service-provider OAuth authorization", "eleme");
    }

    /**
     * Returns the exact immutable settings record accepted by this definition.
     *
     * @return Eleme Source settings class
     */
    @Override
    public Class<ElemeSourceSettings> settingsType() {
        return ElemeSourceSettings.class;
    }

    /**
     * Returns the sole supported Eleme variant.
     *
     * @return immutable single-element definition list
     */
    @Override
    public List<VendorDefinition.Definition> variants() {
        return List.of(DEFINITION);
    }

    /**
     * Returns the exact default Eleme definition.
     *
     * @param variant requested variant
     * @return exact immutable definition
     * @throws ValidateException if the requested variant is unsupported
     */
    @Override
    public VendorDefinition.Definition variant(final Vendor.Variant variant) {
        if (!DEFAULT.equals(variant)) {
            throw new ValidateException("Eleme Vendor variant is not supported");
        }
        return DEFINITION;
    }

}
