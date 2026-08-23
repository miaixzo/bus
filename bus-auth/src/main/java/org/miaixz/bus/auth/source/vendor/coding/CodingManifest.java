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
package org.miaixz.bus.auth.source.vendor.coding;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.source.SourceWorkflow;
import org.miaixz.bus.auth.source.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.source.vendor.Vendor;
import org.miaixz.bus.auth.source.vendor.VendorDeviation;
import org.miaixz.bus.auth.source.vendor.VendorManifest;
import org.miaixz.bus.auth.source.vendor.VendorTargets;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.tls.TlsClientAuth;

/**
 * Declares the frozen tenant-scoped CODING OAuth 2.0 Vendor manifest.
 * <p>
 * The manifest keeps all three official endpoints under a constrained single-label tenant template. Its comma-delimited
 * authorization scope, string token lifetime, and OpenAPI response envelope remain private Source adaptations, so no
 * standard OAuth client capability is advertised.
 * </p>
 *
 * @author Kimi Liu
 */
public class CodingManifest implements VendorManifest<CodingOptions> {

    /**
     * Stable platform routing identifier shared by Source lookup and runtime compilation.
     */
    public static final Vendor.Id ID = new Vendor.Id("coding");

    /**
     * Internal identifier of the sole CODING tenant authorization variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant(Normal.DEFAULT);

    /**
     * Exact Source-authentication-only capability manifest.
     */
    private static final Capability.Manifest CAPABILITIES = new Capability.Manifest(List.of(
            SourceWorkflow.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceWorkflow.complete(Set.of(Capability.Interaction.REDIRECT))));

    /**
     * Exact platform differences that prevent publication as standard OAuth or UserInfo operations.
     */
    private static final List<VendorDeviation> DEVIATIONS = List.of(
            deviation(
                    Builder.SOURCE_AUTHENTICATION_INITIATE,
                    VendorDeviation.Location.QUERY,
                    OAuth2.Parameters.SCOPE,
                    OAuth2.Parameters.SCOPE,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.QUERY,
                    "team",
                    null,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.JSON,
                    OAuth2.Parameters.EXPIRES_IN,
                    OAuth2.Parameters.EXPIRES_IN,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    false),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.RESPONSE,
                    "Response",
                    null,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    true));

    /**
     * Complete immutable endpoint, client policy, scope, capability, form, and deviation manifest.
     */
    private static final VendorManifest.Variant VARIANT = new VendorManifest.Variant(ID, DEFAULT, Protocol.OAUTH2,
            VendorManifest.Pkce.DISABLED, Credential.Type.CLIENT_SECRET, List.of("user:profile:ro"),
            new VendorTargets(
                    Optional.of(
                            template(
                                    "https://{instance}.coding.net/oauth_authorize.html",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.of(
                            template(
                                    "https://{instance}.coding.net/api/oauth/access_token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_POST)),
                    Optional.of(
                            template(
                                    "https://{instance}.coding.net/open-api",
                                    Http.Method.POST,
                                    Endpoint.Authentication.BEARER)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty()),
            CAPABILITIES, DEVIATIONS);

    /**
     * Creates the stateless CODING manifest used by Vendor module assembly.
     */
    public CodingManifest() {
        // No initialization required.
        // All manifest state is held by immutable class constants.
    }

    /**
     * Creates one constrained team-host endpoint template.
     *
     * @param value          official HTTPS endpoint template
     * @param method         HTTP method used by the operation
     * @param authentication endpoint authentication method
     * @return immutable constrained endpoint target
     */
    private static VendorTargets.Template template(
            final String value,
            final Http.Method method,
            final Endpoint.Authentication authentication) {
        return new VendorTargets.Template(value, method, Set.of(authentication), Optional.empty(), TlsClientAuth.NONE);
    }

    /**
     * Creates one immutable CODING wire deviation.
     *
     * @param operation    exact affected operation
     * @param location     exact wire location
     * @param vendorName   CODING field or envelope name
     * @param standardName corresponding standard field, or {@code null}
     * @param mediaType    exact representation when applicable
     * @param method       exact HTTP method
     * @param enveloped    whether the response uses a platform envelope
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
     * Returns the one-time CODING client form including its constrained team label.
     *
     * @param variant exact CODING variant whose form is requested
     * @return immutable CODING client configuration form
     */
    @Override
    public Scheme.Form form(final Vendor.Variant variant) {
        final VendorManifest.Variant selected = variant(variant);
        return VendorManifest.Forms.extended(
                selected,
                List.of(VendorManifest.Forms.field("team", "CODING team", Scheme.Form.Type.TEXT, true)));
    }

    /**
     * Returns the stable platform identifier used to select this manifest.
     *
     * @return stable CODING routing identifier
     */
    @Override
    public Vendor.Id vendor() {
        return ID;
    }

    /**
     * Returns non-sensitive CODING presentation metadata.
     *
     * @return immutable CODING management metadata
     */
    @Override
    public Scheme.Metadata metadata() {
        return new Scheme.Metadata("CODING", "CODING team account authorization", "coding");
    }

    /**
     * Returns the sole supported CODING variant.
     *
     * @return immutable single-element manifest list
     */
    @Override
    public List<VendorManifest.Variant> variants() {
        return List.of(VARIANT);
    }

    /**
     * Returns the exact default CODING manifest.
     *
     * @param variant requested variant
     * @return exact CODING manifest
     * @throws ValidateException if the requested variant is unsupported
     */
    @Override
    public VendorManifest.Variant variant(final Vendor.Variant variant) {
        if (!DEFAULT.equals(variant)) {
            throw new ValidateException("CODING Vendor variant is not supported");
        }
        return VARIANT;
    }

}
