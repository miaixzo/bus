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
package org.miaixz.bus.auth.vendor.huawei;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party client for Huawei authorization, token, refresh, and account-profile operations.
 *
 * <p>
 * Remote requests use the injected Fabric context. Authorization state uses the injected atomic store, optional PKCE
 * verifiers use the caller-owned bus-cache instance for ten minutes, and client secrets are resolved only for token
 * operations. An available OpenID Connect ID token is decoded locally instead of issuing a profile request.
 * </p>
 *
 * @author Kimi Liu
 */
public class HuaweiProvider extends AbstractProvider {

    /**
     * PKCE verifier lifetime in milliseconds.
     */
    private static final long PKCE_TTL_MILLIS = Duration.ofMinutes(10).toMillis();

    /**
     * Huawei PKCE transformation method.
     */
    private static final String PKCE_METHOD = "S256";

    /**
     * Explicit caller-owned cache used only for Huawei PKCE verifier entries.
     */
    private final CacheX<String, String> cache;

    /**
     * Creates a Huawei client from the complete immutable dependency aggregate.
     *
     * @param configuration non-null registration, Fabric, clock, state-store, cache, and secret dependencies
     * @throws NullPointerException if the configuration or a required dependency is null
     * @throws AuthorizedException  if the Huawei registration is invalid
     */
    public HuaweiProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.HUAWEI);
        this.cache = stringCache(configuration.cache());
    }

    /**
     * Parses and validates one Huawei token response.
     *
     * @param json token response document
     * @return mapped token set
     * @throws AuthorizedException if the response is empty, reports an error, or omits the access token
     */
    private static VendorTokenSet readToken(final String json) {
        final TokenResponse response = JsonKit.toPojo(json, TokenResponse.class);
        validate(response);
        if (response.access_token() == null) {
            throw new AuthorizedException("Missing access_token in Huawei token response");
        }
        return VendorTokenSet.builder().token(response.access_token()).expireIn(response.expires_in())
                .refresh(response.refresh_token()).idToken(response.id_token()).build();
    }

    /**
     * Decodes the payload segment of a Huawei OpenID Connect ID token.
     *
     * @param idToken signed ID token received from Huawei
     * @return validated typed payload claims
     * @throws AuthorizedException if the compact token or payload is invalid
     */
    private static IdClaims readClaims(final String idToken) {
        try {
            final String[] segments = idToken.split("\\.");
            if (segments.length < 2) {
                throw new AuthorizedException("Malformed Huawei id_token");
            }
            final String payload = new String(Base64.getUrlDecoder().decode(segments[1]), StandardCharsets.UTF_8);
            final IdClaims claims = JsonKit.toPojo(payload, IdClaims.class);
            if (claims == null || claims.sub() == null) {
                throw new AuthorizedException("Missing sub in Huawei id_token payload");
            }
            return claims;
        } catch (AuthorizedException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new AuthorizedException("Failed to parse Huawei id_token payload: " + exception.getMessage());
        }
    }

    /**
     * Rejects an empty Huawei response or a response carrying either supported error shape.
     *
     * @param response typed Huawei response
     * @throws AuthorizedException if the response is null or reports an error
     */
    private static void validate(final HuaweiResponse response) {
        if (response == null) {
            throw new AuthorizedException("Failed to parse Huawei response: empty response");
        }
        if (response.nspStatus() != null) {
            throw new AuthorizedException(response.error() == null ? "Unknown error" : response.error());
        }
        if (response.error() != null) {
            throw new AuthorizedException((response.subError() == null ? "Unknown sub_error" : response.subError())
                    + Symbol.COLON
                    + (response.errorDescription() == null ? "Unknown description" : response.errorDescription()));
        }
    }

    /**
     * Narrows the explicitly injected compatibility cache to Huawei string PKCE entries.
     *
     * @param cache caller-owned cache instance
     * @return the same cache viewed through Huawei-owned key and value types
     */
    @SuppressWarnings("unchecked")
    private static CacheX<String, String> stringCache(final CacheX<?, ?> cache) {
        return (CacheX<String, String>) Objects.requireNonNull(cache, "Cache must not be null");
    }

    /**
     * Builds the offline Huawei authorization URL and stores an optional PKCE verifier.
     *
     * @param context immutable root operation context used for state ownership
     * @param state   optional caller-supplied state; a generated state is used when absent
     * @return successful client message containing the complete authorization URL
     * @throws NullPointerException if {@code context} is null
     * @throws AuthorizedException  if state registration or PKCE generation fails
     */
    @Override
    public Message<String> build(final Context context, final String state) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final String actualState = state(current, state);
        final VendorRequestBuilder request = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.AUTHORIZE))
                .queryParam("response_type", "code").queryParam("client_id", registration.clientId())
                .queryParam("redirect_uri", registration.redirectUri()).queryParam("state", actualState)
                .queryParam("access_type", "offline")
                .queryParam("scope", scopes(Symbol.SPACE, true, getScopes(HuaweiScope.values())));
        if (registration.pkce()) {
            final String verifier = VendorRequestBuilder.codeVerifier();
            request.queryParam("code_challenge", VendorRequestBuilder.codeChallenge(PKCE_METHOD, verifier))
                    .queryParam("code_challenge_method", PKCE_METHOD);
            cache.write(pkceKey(actualState), verifier, PKCE_TTL_MILLIS);
        }
        return Message.success(request.build());
    }

    /**
     * Exchanges a Huawei authorization code and supplies the cached PKCE verifier when enabled.
     *
     * @param context  immutable root operation context used to resolve the client secret
     * @param callback immutable inbound callback containing standard {@code code} and {@code state} parameters
     * @return successful client message containing the mapped Huawei token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint, secret, or response is invalid
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final Map<String, String> form = new LinkedHashMap<>();
        form.put("grant_type", "authorization_code");
        form.put("code", inbound.value("code").orElse(null));
        form.put("client_id", registration.clientId());
        form.put("client_secret", secret(current));
        form.put("redirect_uri", registration.redirectUri());
        if (registration.pkce()) {
            form.put("code_verifier", cache.read(pkceKey(inbound.value("state").orElse(null))));
        }
        return Message.success(readToken(post(endpoint(VendorEndpoint.TOKEN), form)));
    }

    /**
     * Retrieves the Huawei account profile or maps claims from an available ID token.
     *
     * @param context immutable root operation context for this profile operation
     * @param token   non-null Huawei token set
     * @return successful client message containing the mapped Huawei identity
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint, ID token, or response is invalid
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        if (authorization.getIdToken() != null && !authorization.getIdToken().isEmpty()) {
            return Message.success(identity(readClaims(authorization.getIdToken()), authorization));
        }
        final Map<String, String> form = new LinkedHashMap<>();
        form.put("access_token", authorization.getToken());
        form.put("getNickName", Symbol.ONE);
        form.put("nsp_svc", "GOpen.User.getInfo");
        final ProfileResponse response = JsonKit
                .toPojo(post(endpoint(VendorEndpoint.USERINFO), form), ProfileResponse.class);
        validate(response);
        if (response.unionID() == null) {
            throw new AuthorizedException("Missing unionID in Huawei profile response");
        }
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(response)).uuid(response.unionID())
                        .username(response.displayName()).nickname(response.displayName()).gender(Gender.UNKNOWN)
                        .avatar(response.headPictureURL()).token(authorization).source(descriptor().id()).build());
    }

    /**
     * Refreshes a Huawei access token through the configured refresh endpoint.
     *
     * @param context immutable root operation context used to resolve the client secret
     * @param token   non-null token set containing the refresh token
     * @return successful client message containing the refreshed Huawei token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint, secret, or response is invalid
     */
    @Override
    public Message<VendorTokenSet> refresh(final Context context, final VendorTokenSet token) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final Map<String, String> form = new LinkedHashMap<>();
        form.put("client_id", registration.clientId());
        form.put("client_secret", secret(current));
        form.put("refresh_token", authorization.getRefresh());
        form.put("grant_type", "refresh_token");
        return Message.success(readToken(post(endpoint(VendorEndpoint.REFRESH), form)));
    }

    /**
     * Maps locally decoded ID-token claims to a vendor identity.
     *
     * @param claims validated Huawei ID-token claims
     * @param token  originating token set retained by the identity
     * @return mapped Huawei identity
     */
    private VendorIdentity identity(final IdClaims claims, final VendorTokenSet token) {
        return VendorIdentity.builder().rawJson(JsonKit.toJsonString(claims)).uuid(claims.sub()).username(claims.name())
                .nickname(claims.nickname()).gender(Gender.UNKNOWN).avatar(claims.picture()).token(token)
                .source(descriptor().id()).build();
    }

    /**
     * Builds the deterministic cache key for one Huawei PKCE verifier.
     *
     * @param state opaque authorization state
     * @return vendor-prefixed cache key
     */
    private String pkceKey(final String state) {
        return descriptor().id() + ":code_verifier:" + state;
    }

    /**
     * Common error fields exposed by Huawei token and profile responses.
     *
     * @author Kimi Liu
     */
    private interface HuaweiResponse {

        /**
         * Returns the legacy Huawei service status marker.
         *
         * @return status marker, or null when absent
         */
        Object nspStatus();

        /**
         * Returns the vendor error code.
         *
         * @return error code, or null for success
         */
        String error();

        /**
         * Returns the vendor error subtype.
         *
         * @return error subtype, or null when absent
         */
        String subError();

        /**
         * Returns the vendor diagnostic description.
         *
         * @return error description, or null when absent
         */
        String errorDescription();
    }

    /**
     * Typed Huawei token response.
     *
     * @param access_token      sensitive access token
     * @param expires_in        access-token lifetime in seconds
     * @param refresh_token     sensitive refresh token
     * @param id_token          signed OpenID Connect token
     * @param NSP_STATUS        legacy Huawei service status marker
     * @param error             vendor error code
     * @param sub_error         vendor error subtype
     * @param error_description vendor diagnostic description
     * @author Kimi Liu
     */
    private record TokenResponse(String access_token, int expires_in, String refresh_token, String id_token,
            Object NSP_STATUS, String error, String sub_error, String error_description) implements HuaweiResponse {

        /**
         * {@inheritDoc}
         */
        @Override
        public Object nspStatus() {
            return NSP_STATUS;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String subError() {
            return sub_error;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String errorDescription() {
            return error_description;
        }
    }

    /**
     * Typed Huawei account-profile response.
     *
     * @param unionID           stable Huawei union identifier
     * @param displayName       account display name
     * @param headPictureURL    profile image URL
     * @param NSP_STATUS        legacy Huawei service status marker
     * @param error             vendor error code
     * @param sub_error         vendor error subtype
     * @param error_description vendor diagnostic description
     * @author Kimi Liu
     */
    private record ProfileResponse(String unionID, String displayName, String headPictureURL, Object NSP_STATUS,
            String error, String sub_error, String error_description) implements HuaweiResponse {

        /**
         * {@inheritDoc}
         */
        @Override
        public Object nspStatus() {
            return NSP_STATUS;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String subError() {
            return sub_error;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String errorDescription() {
            return error_description;
        }
    }

    /**
     * Typed subset of locally decoded Huawei ID-token claims.
     *
     * @param sub      stable OpenID Connect subject identifier
     * @param name     account display name
     * @param nickname account nickname
     * @param picture  profile image URL
     * @author Kimi Liu
     */
    private record IdClaims(String sub, String name, String nickname, String picture) {
    }

}
