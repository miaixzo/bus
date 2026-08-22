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
package org.miaixz.bus.auth.vendor.rednote;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.vendor.VariantManifest;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorDeviation;
import org.miaixz.bus.auth.vendor.VendorTargets;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.tls.TlsClientAuth;

/**
 * Declares the Xiaohongshu marketing authorization-only Vendor API.
 * <p>
 * This platform flow is not represented as OAuth 2.0 because its camel-case authorization request and token/error
 * documents do not satisfy the standard wire contract. The two public capabilities and nested request/response records
 * are scoped to this manifest and never enter protocol metadata or Source sign-in flows.
 * </p>
 *
 * @author Kimi Liu
 */
public class RedNoteManifest implements VariantManifest<RedNoteOptions> {

    /**
     * Stable RedNote platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("rednote");
    /**
     * Starts an exact RedNote marketing authorization interaction.
     */
    public static final Capability<MarketingAuthorizationRequest, Url> REDNOTE_MARKETING_AUTHORIZE = new Capability<>(
            Capability.Key.application("vendor.rednote.marketing_authorize"), MarketingAuthorizationRequest.class,
            Url.class, Capability.Direction.SOURCE, Set.of(Capability.Interaction.REDIRECT),
            Capability.Security.PUBLIC);
    /**
     * Exchanges or refreshes an exact RedNote marketing platform token.
     */
    public static final Capability<MarketingTokenRequest, MarketingTokenResponse> REDNOTE_MARKETING_TOKEN = new Capability<>(
            Capability.Key.application("vendor.rednote.marketing_token"), MarketingTokenRequest.class,
            MarketingTokenResponse.class, Capability.Direction.SOURCE, Set.of(Capability.Interaction.DIRECT),
            Capability.Security.CLIENT_AUTHENTICATED);
    /**
     * Stable marketing authorization variant identifier.
     */
    public static final Vendor.Variant MARKETING = new Vendor.Variant("marketing");
    /**
     * RedNote form authentication using application identifier and secret fields.
     */
    private static final Endpoint.Authentication APP_SECRET_FORM = new Endpoint.Authentication("app_secret_form");
    /**
     * Exact authorization-only RedNote capability set.
     */
    private static final Capability.Manifest CAPABILITIES = new Capability.Manifest(
            List.of(REDNOTE_MARKETING_AUTHORIZE, REDNOTE_MARKETING_TOKEN));

    /**
     * Complete immutable RedNote marketing manifest.
     */
    private static final VariantManifest.Variant VARIANT = new VariantManifest.Variant(ID, MARKETING,
            Protocol.VENDOR_AUTH, VariantManifest.Pkce.DISABLED, Credential.Type.CLIENT_SECRET,
            List.of("report_service"),
            new VendorTargets(Optional
                    .of(fixed("https://ad-market.xiaohongshu.com/auth", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed(
                                    "https://adapi.xiaohongshu.com/api/open/oauth2/access_token",
                                    Http.Method.POST,
                                    APP_SECRET_FORM)),
                    Optional.empty(),
                    Optional.of(
                            fixed(
                                    "https://adapi.xiaohongshu.com/api/open/oauth2/refresh_token",
                                    Http.Method.POST,
                                    APP_SECRET_FORM)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty()),
            CAPABILITIES,
            List.of(
                    deviation(
                            "marketing_authorize",
                            VendorDeviation.Location.QUERY,
                            "appId",
                            OAuth2.Parameters.CLIENT_ID,
                            Optional.empty(),
                            Http.Method.GET,
                            false),
                    deviation(
                            "marketing_authorize",
                            VendorDeviation.Location.QUERY,
                            "redirectUri",
                            OAuth2.Parameters.REDIRECT_URI,
                            Optional.empty(),
                            Http.Method.GET,
                            false),
                    deviation(
                            "marketing_token",
                            VendorDeviation.Location.FORM,
                            "app_id",
                            OAuth2.Parameters.CLIENT_ID,
                            Optional.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                            Http.Method.POST,
                            false),
                    deviation(
                            "marketing_token",
                            VendorDeviation.Location.FORM,
                            "secret",
                            OAuth2.Parameters.CLIENT_SECRET,
                            Optional.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                            Http.Method.POST,
                            false),
                    deviation(
                            "marketing_token",
                            VendorDeviation.Location.RESPONSE,
                            "code/error/sub_error/error_description",
                            null,
                            Optional.of(MediaType.APPLICATION_JSON_TYPE),
                            Http.Method.POST,
                            true),
                    deviation(
                            "marketing_token",
                            VendorDeviation.Location.RESPONSE,
                            "access_token_expires_in without token_type",
                            "expires_in/token_type",
                            Optional.of(MediaType.APPLICATION_JSON_TYPE),
                            Http.Method.POST,
                            true),
                    deviation(
                            "marketing_token",
                            VendorDeviation.Location.FORM,
                            OAuth2.Parameters.REFRESH_TOKEN,
                            null,
                            Optional.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                            Http.Method.POST,
                            false),
                    deviation(
                            "marketing_token",
                            VendorDeviation.Location.RESPONSE,
                            OAuth2.Parameters.EXPIRES_IN,
                            null,
                            Optional.of(MediaType.APPLICATION_JSON_TYPE),
                            Http.Method.POST,
                            true)));

    /**
     * Creates the stateless RedNote manifest used by Vendor directory assembly.
     */
    public RedNoteManifest() {
        // No initialization required.
        // Immutable manifest state is retained by class constants.
    }

    /**
     * Creates one fixed credential-free RedNote HTTPS endpoint.
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
        return new VendorTargets.Fixed(new Endpoint(Url.parse(value), Endpoint.Transport.HTTPS, Optional.of(method),
                Set.of(authentication), Optional.empty(), TlsClientAuth.NONE));
    }

    /**
     * Creates one immutable registered RedNote wire deviation.
     *
     * @param operation    affected profile-scoped operation
     * @param location     exact wire location
     * @param vendorName   exact RedNote field or representation name
     * @param standardName related standard field, or {@code null}
     * @param mediaType    exact representation when applicable
     * @param method       exact HTTP method
     * @param enveloped    whether the result uses a platform envelope
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
     * Returns the stable RedNote routing identifier.
     *
     * @return RedNote platform identifier
     */
    @Override
    public Vendor.Id vendor() {
        return ID;
    }

    /**
     * Returns non-sensitive RedNote presentation metadata.
     *
     * @return immutable RedNote management metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("Xiaohongshu Marketing", "Xiaohongshu marketing account authorization",
                "rednote-marketing");
    }

    /**
     * Returns the sole marketing variant.
     *
     * @return immutable single-element manifest list
     */
    @Override
    public List<VariantManifest.Variant> variants() {
        return List.of(VARIANT);
    }

    /**
     * Resolves the exact marketing manifest.
     *
     * @param variant requested RedNote variant
     * @return immutable marketing manifest
     * @throws ValidateException if the requested variant is unsupported
     */
    @Override
    public VariantManifest.Variant variant(final Vendor.Variant variant) {
        if (!MARKETING.equals(variant)) {
            throw new ValidateException("RedNote Vendor variant is not supported");
        }
        return VARIANT;
    }

    /**
     * Represents one exact RedNote marketing authorization request.
     *
     * @param redirectUri registered callback URI that must equal the selected Source setting
     * @param scopes      ordered official marketing scopes, or empty to use manifest defaults
     * @param state       non-blank application correlation value
     * @author Kimi Liu
     */
    public record MarketingAuthorizationRequest(String redirectUri, List<String> scopes, String state) {

        /**
         * Validates and freezes one authorization request without adding OAuth fields.
         *
         * @throws IllegalArgumentException if a component, collection member, or state is {@code null} or blank
         */
        public MarketingAuthorizationRequest {
            redirectUri = Assert.notBlank(redirectUri, "RedNote marketing redirect URI must not be blank");
            Assert.notNull(scopes, "RedNote marketing authorization scopes must not be null");
            final List<String> copy = new ArrayList<>(scopes.size());
            for (String scope : scopes) {
                final String checked = Assert
                        .notBlank(scope, "RedNote marketing authorization scope must not be blank");
                Assert.isFalse(copy.contains(checked), "RedNote marketing authorization scopes must be unique");
                copy.add(checked);
            }
            scopes = List.copyOf(copy);
            state = Assert.notBlank(state, "RedNote marketing authorization state must not be blank");
        }

    }

    /**
     * Represents one exact RedNote initial-token or refresh-token request.
     *
     * @param code         initial authorization code, empty for refresh
     * @param refreshToken refresh token, empty for initial exchange
     * @author Kimi Liu
     */
    public record MarketingTokenRequest(Optional<String> code, Optional<String> refreshToken) {

        /**
         * Requires exactly one non-blank platform credential branch.
         *
         * @throws IllegalArgumentException if an optional container is {@code null} or a present value is blank
         * @throws ValidateException        if both or neither request branches are selected
         */
        public MarketingTokenRequest {
            Assert.notNull(code, "RedNote marketing code container must not be null");
            Assert.notNull(refreshToken, "RedNote marketing refresh-token container must not be null");
            code = Optional.ofNullable(code.getOrNull());
            refreshToken = Optional.ofNullable(refreshToken.getOrNull());
            if (code.isPresent()) {
                Assert.notBlank(code.getOrNull(), "RedNote marketing authorization code must not be blank");
            }
            if (refreshToken.isPresent()) {
                Assert.notBlank(refreshToken.getOrNull(), "RedNote marketing refresh token must not be blank");
            }
            if (code.isPresent() == refreshToken.isPresent()) {
                throw new ValidateException("RedNote marketing token request must select exactly one branch");
            }
        }

        /**
         * Returns a diagnostic representation without the code or refresh token.
         *
         * @return redacted request representation
         */
        @Override
        public String toString() {
            return "MarketingTokenRequest[code=[REDACTED], refreshToken=[REDACTED]]";
        }

    }

    /**
     * Represents one successful RedNote marketing platform token document.
     *
     * @param accessToken  sensitive marketing access token
     * @param refreshToken optional sensitive marketing refresh token
     * @param scope        optional platform-returned scope text
     * @param expiresIn    optional positive lifetime selected from the active response branch
     * @author Kimi Liu
     */
    public record MarketingTokenResponse(String accessToken, Optional<String> refreshToken, Optional<String> scope,
            Optional<Long> expiresIn) {

        /**
         * Validates one successful platform token result without inventing a token type.
         *
         * @throws IllegalArgumentException if an optional container is null or a present text value is blank
         * @throws ValidateException        if a present lifetime is not positive
         */
        public MarketingTokenResponse {
            accessToken = Assert.notBlank(accessToken, "RedNote marketing access token must not be blank");
            Assert.notNull(refreshToken, "RedNote marketing refresh-token container must not be null");
            Assert.notNull(scope, "RedNote marketing scope container must not be null");
            Assert.notNull(expiresIn, "RedNote marketing lifetime container must not be null");
            refreshToken = Optional.ofNullable(refreshToken.getOrNull());
            scope = Optional.ofNullable(scope.getOrNull());
            expiresIn = Optional.ofNullable(expiresIn.getOrNull());
            if (refreshToken.isPresent()) {
                Assert.notBlank(refreshToken.getOrNull(), "RedNote marketing refresh token must not be blank");
            }
            if (scope.isPresent()) {
                Assert.notBlank(scope.getOrNull(), "RedNote marketing scope must not be blank");
            }
            if (expiresIn.isPresent() && expiresIn.getOrNull() <= 0L) {
                throw new ValidateException("RedNote marketing token lifetime must be positive");
            }
        }

        /**
         * Returns a diagnostic representation without token material.
         *
         * @return redacted response representation
         */
        @Override
        public String toString() {
            return "MarketingTokenResponse[accessToken=[REDACTED], refreshToken=[REDACTED], scope=" + scope
                    + ", expiresIn=" + expiresIn + Symbol.C_BRACKET_RIGHT;
        }

    }

}
