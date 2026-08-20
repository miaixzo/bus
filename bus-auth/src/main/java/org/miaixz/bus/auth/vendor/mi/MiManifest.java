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
package org.miaixz.bus.auth.vendor.mi;

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
 * Declares the frozen Xiaomi OAuth 2.0 browser Vendor manifest.
 * <p>
 * Authorization retains the platform {@code skip_confirm} extension. Token and refresh requests use Xiaomi's
 * query-authenticated GET wire and prefixed JSON response, while profile retrieval uses the historical
 * {@code clientId}/{@code token} query pair. These deviations remain isolated in the platform adapter.
 * </p>
 *
 * @author Kimi Liu
 */
public final class MiManifest implements VariantManifest<MiOptions> {

    /**
     * Stable Xiaomi platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("mi");

    /**
     * Stable identifier of the sole Xiaomi account variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant("default");

    /**
     * Xiaomi query client authentication declaration.
     */
    private static final Endpoint.Authentication CLIENT_SECRET_QUERY = new Endpoint.Authentication(
            "client_secret_query");

    /**
     * Xiaomi profile query authentication declaration.
     */
    private static final Endpoint.Authentication CLIENT_TOKEN_QUERY = new Endpoint.Authentication("client_token_query");

    /**
     * Exact public Source and OAuth operations of the default variant.
     */
    private static final Capability.Manifest CAPABILITIES = new Capability.Manifest(List.of(
            SourceAuthentication.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceAuthentication.complete(Set.of(Capability.Interaction.REDIRECT)),
            OAuth2ClientScheme.AUTHORIZATION,
            OAuth2ClientScheme.TOKEN));

    /**
     * Historical Xiaomi scopes required for profile, OpenID, and optional contact retrieval.
     */
    private static final List<String> DEFAULT_SCOPES = List.of("user/profile", "user/openIdV2", "user/phoneAndEmail");

    /**
     * Exact Xiaomi authorization, token, and profile wire differences.
     */
    private static final List<VendorDeviation> DEVIATIONS = List.of(
            deviation(
                    "oauth2.authorization",
                    VendorDeviation.Location.QUERY,
                    "skip_confirm",
                    null,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "oauth2.token",
                    VendorDeviation.Location.QUERY,
                    OAuth2.Parameters.CLIENT_SECRET,
                    OAuth2.Parameters.CLIENT_SECRET,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "oauth2.token",
                    VendorDeviation.Location.RESPONSE,
                    "&&&START&&&",
                    null,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.GET,
                    false),
            deviation(
                    "oauth2.token",
                    VendorDeviation.Location.JSON,
                    "openId",
                    null,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.QUERY,
                    "clientId",
                    OAuth2.Parameters.CLIENT_ID,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.QUERY,
                    OAuth2.Parameters.TOKEN,
                    OAuth2.Parameters.ACCESS_TOKEN,
                    Optional.empty(),
                    Http.Method.GET,
                    false));

    /**
     * Complete immutable Xiaomi manifest.
     */
    private static final VariantManifest.Variant VARIANT = new VariantManifest.Variant(ID, DEFAULT, Protocol.OAUTH2,
            DEFAULT_SCOPES,
            new VendorTargets(
                    Optional.of(
                            fixed(
                                    "https://account.xiaomi.com/oauth2/authorize",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.of(fixed("https://account.xiaomi.com/OIDC/token", Http.Method.GET, CLIENT_SECRET_QUERY)),
                    Optional.of(
                            fixed("https://open.account.xiaomi.com/user/profile", Http.Method.GET, CLIENT_TOKEN_QUERY)),
                    Optional.of(fixed("https://account.xiaomi.com/OIDC/token", Http.Method.GET, CLIENT_SECRET_QUERY)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty()),
            CAPABILITIES, DEVIATIONS);

    /**
     * Creates the stateless Xiaomi manifest used by Vendor directory assembly.
     */
    public MiManifest() {
        // No initialization required.
        // All manifest state is held by immutable class constants.
    }

    /**
     * Creates one fixed credential-free HTTPS endpoint target.
     *
     * @param value          exact endpoint URL
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
     * Creates one immutable Xiaomi wire deviation.
     *
     * @param operation    affected public or Source operation
     * @param location     exact wire location
     * @param vendorName   exact Xiaomi field or representation marker
     * @param standardName corresponding registered OAuth field when present
     * @param mediaType    exact representation when applicable
     * @param method       exact HTTP method
     * @param enveloped    whether Xiaomi wraps the operation response
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
     * Returns the stable Xiaomi routing identifier.
     *
     * @return stable platform identifier
     */
    @Override
    public Vendor.Id vendor() {
        return ID;
    }

    /**
     * Returns non-sensitive Xiaomi presentation metadata.
     *
     * @return immutable management metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("Xiaomi", "Xiaomi account OAuth sign-in", "xiaomi");
    }

    /**
     * Returns the sole supported Xiaomi variant.
     *
     * @return immutable single-element manifest list
     */
    @Override
    public List<VariantManifest.Variant> variants() {
        return List.of(VARIANT);
    }

    /**
     * Returns the exact default Xiaomi manifest.
     *
     * @param variant requested variant
     * @return immutable default manifest
     * @throws ValidateException if the variant is unsupported
     */
    @Override
    public VariantManifest.Variant variant(final Vendor.Variant variant) {
        if (!DEFAULT.equals(variant)) {
            throw new ValidateException("Xiaomi Vendor variant is not supported");
        }
        return VARIANT;
    }

}
