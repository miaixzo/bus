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
package org.miaixz.bus.auth.vendor.oschina;

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
 * Declares the OSChina OAuth 2.0 browser Vendor manifest.
 * <p>
 * Authorization is standard. The token capability preserves OSChina's historical GET query transport while exposing
 * only the standard OAuth request and response types now supported by the documented {@code token_type} member. The
 * access-token query profile remains private to Source authentication and maps only the stable profile {@code id} to an
 * external subject.
 * </p>
 *
 * @author Kimi Liu
 */
public final class OsChinaManifest implements VariantManifest<OsChinaOptions> {

    /**
     * Stable OSChina platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("oschina");

    /**
     * Stable identifier of the only OSChina OAuth variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant("default");

    /**
     * Platform authentication identifier for a client secret carried in the token query.
     */
    private static final Endpoint.Authentication CLIENT_SECRET_QUERY = new Endpoint.Authentication(
            "client_secret_query");

    /**
     * Exact Source authentication and public OAuth operations exposed by the adapter.
     */
    private static final Capability.Manifest CAPABILITIES = new Capability.Manifest(List.of(
            SourceAuthentication.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceAuthentication.complete(Set.of(Capability.Interaction.REDIRECT)),
            OAuth2ClientScheme.AUTHORIZATION,
            OAuth2ClientScheme.TOKEN));

    /**
     * Exact registered OSChina wire deviations retained outside the protocol package.
     */
    private static final List<VendorDeviation> DEVIATIONS = List.of(
            deviation(
                    OAuth2.Parameters.TOKEN,
                    VendorDeviation.Location.QUERY,
                    OAuth2.Parameters.CLIENT_SECRET,
                    OAuth2.Parameters.CLIENT_SECRET,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    OAuth2.Parameters.TOKEN,
                    VendorDeviation.Location.QUERY,
                    "dataType",
                    null,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    OAuth2.Parameters.TOKEN,
                    VendorDeviation.Location.RESPONSE,
                    "uid",
                    null,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.QUERY,
                    OAuth2.Parameters.ACCESS_TOKEN,
                    Http.Header.AUTHORIZATION,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.QUERY,
                    "dataType",
                    null,
                    Optional.empty(),
                    Http.Method.GET,
                    false));

    /**
     * Complete immutable OSChina endpoint, client, capability, form, and deviation manifest.
     */
    private static final VariantManifest.Variant VARIANT = new VariantManifest.Variant(ID, DEFAULT, Protocol.OAUTH2,
            List.of(),
            new VendorTargets(
                    Optional.of(
                            fixed(
                                    "https://www.oschina.net/action/oauth2/authorize",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed(
                                    "https://www.oschina.net/action/openapi/token",
                                    Http.Method.GET,
                                    CLIENT_SECRET_QUERY)),
                    Optional.of(
                            fixed(
                                    "https://www.oschina.net/action/openapi/user",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty()),
            CAPABILITIES, DEVIATIONS);

    /**
     * Creates the stateless OSChina manifest used by Vendor directory assembly.
     */
    public OsChinaManifest() {
        // No initialization required.
        // Immutable manifest state is retained by class constants.
    }

    /**
     * Creates one fixed OSChina HTTPS endpoint.
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
     * Creates one immutable OSChina wire deviation.
     *
     * @param operation    exact affected operation
     * @param location     exact wire location
     * @param vendorName   OSChina field name
     * @param standardName corresponding standard field or {@code null}
     * @param mediaType    exact representation when applicable
     * @param method       exact HTTP method
     * @param enveloped    whether the response uses a wrapper object
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
     * Returns the stable OSChina routing identifier.
     *
     * @return OSChina platform identifier
     */
    @Override
    public Vendor.Id vendor() {
        return ID;
    }

    /**
     * Returns non-sensitive OSChina presentation metadata.
     *
     * @return immutable OSChina management metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("OSChina", "OSChina account authorization", "oschina");
    }

    /**
     * Returns the sole supported OSChina variant.
     *
     * @return immutable single-element manifest list
     */
    @Override
    public List<VariantManifest.Variant> variants() {
        return List.of(VARIANT);
    }

    /**
     * Resolves the exact default OSChina manifest.
     *
     * @param variant requested OSChina variant
     * @return immutable default manifest
     * @throws ValidateException if the requested variant is unsupported
     */
    @Override
    public VariantManifest.Variant variant(final Vendor.Variant variant) {
        if (!DEFAULT.equals(variant)) {
            throw new ValidateException("OSChina Vendor variant is not supported");
        }
        return VARIANT;
    }

}
