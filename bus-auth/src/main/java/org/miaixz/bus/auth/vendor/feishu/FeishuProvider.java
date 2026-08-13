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
package org.miaixz.bus.auth.vendor.feishu;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party client for Feishu OAuth authorization, token, profile, and refresh operations.
 *
 * <p>
 * All HTTP traffic uses the injected Fabric context. The explicitly injected bus-cache instance owns the Feishu
 * application token for its vendor lifetime, and client secrets are resolved only when that token is absent.
 * </p>
 *
 * @author Kimi Liu
 */
public class FeishuProvider extends AbstractProvider {

    /**
     * Feishu path for enterprise application-access-token acquisition.
     */
    private static final String APP_TOKEN_PATH = "/open-apis/auth/v3/app_access_token/internal/";

    /**
     * Explicit caller-owned cache used only for Feishu application access tokens.
     */
    private final CacheX<String, String> cache;

    /**
     * Creates a Feishu OAuth client from explicit runtime dependencies.
     *
     * @param configuration non-null registration, Fabric, clock, state-store, cache, and secret dependencies
     * @throws NullPointerException if the configuration or a required dependency is null
     * @throws AuthorizedException  if the Feishu registration is invalid
     */
    public FeishuProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.FEISHU);
        this.cache = stringCache(configuration.cache());
    }

    /**
     * Rejects absent or non-zero Feishu response codes.
     *
     * @param code      numeric Feishu response code
     * @param message   optional Feishu response message
     * @param operation response operation name
     * @throws AuthorizedException if {@code code} is not zero
     */
    private static void check(final int code, final String message, final String operation) {
        if (code != 0) {
            throw new AuthorizedException(message == null ? "Invalid Feishu " + operation + " response" : message);
        }
    }

    /**
     * Narrows the explicitly injected cache to Feishu-owned string entries.
     *
     * @param cache caller-owned cache instance
     * @return the same cache viewed through its Feishu key and value types
     */
    @SuppressWarnings("unchecked")
    private static CacheX<String, String> stringCache(final CacheX<?, ?> cache) {
        return (CacheX<String, String>) Objects.requireNonNull(cache, "Cache must not be null");
    }

    /**
     * Builds the Feishu authorization URL and atomically registers state.
     *
     * @param context immutable root operation context used for state ownership
     * @param state   optional caller-supplied state; a generated state is used when absent
     * @return successful client message containing the complete authorization URL
     * @throws NullPointerException if {@code context} is null
     * @throws AuthorizedException  if the endpoint is absent or state registration fails
     */
    @Override
    public Message<String> build(final Context context, final String state) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        return Message.success(
                VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.AUTHORIZE))
                        .queryParam("app_id", registration.clientId())
                        .queryParam("redirect_uri", UrlEncoder.encodeAll(registration.redirectUri()))
                        .queryParam("state", state(current, state)).build());
    }

    /**
     * Exchanges an authorization code using a cached Feishu application token.
     *
     * @param context  immutable root operation context used to resolve the client secret on a cache miss
     * @param callback immutable inbound callback containing the standard {@code code} parameter
     * @return successful client message containing mapped Feishu token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if an endpoint, secret, or response is invalid
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        return Message.success(
                exchange(
                        endpoint(VendorEndpoint.TOKEN),
                        Map.of(
                                "app_access_token",
                                applicationToken(current),
                                "grant_type",
                                "authorization_code",
                                "code",
                                inbound.value("code").orElse(""))));
    }

    /**
     * Retrieves and maps the Feishu profile associated with an access token.
     *
     * @param context immutable root operation context for this profile request
     * @param token   non-null Feishu token set
     * @return successful client message containing the mapped Feishu identity
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint or response is invalid
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final String response = get(
                endpoint(VendorEndpoint.USERINFO),
                null,
                Map.of(
                        Http.Header.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON,
                        Http.Header.AUTHORIZATION,
                        Http.Auth.BEARER_PREFIX + authorization.getToken()));
        final ProfileResponse profile = JsonKit.toPojo(response, ProfileResponse.class);
        check(profile == null ? -1 : profile.code(), profile == null ? null : profile.message(), "profile");
        if (profile.data() == null || profile.data().union_id() == null) {
            throw new AuthorizedException("Missing data or union_id in Feishu profile response");
        }
        final ProfileData data = profile.data();
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(profile)).uuid(data.union_id())
                        .username(data.name()).nickname(data.name()).avatar(data.avatar_url()).email(data.email())
                        .gender(Gender.UNKNOWN).token(authorization).source(descriptor().id()).build());
    }

    /**
     * Refreshes a Feishu access token using the cached application token.
     *
     * @param context immutable root operation context used to resolve the client secret on a cache miss
     * @param token   non-null token set containing the refresh token
     * @return successful client message containing mapped refreshed token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if an endpoint, secret, or response is invalid
     */
    @Override
    public Message<VendorTokenSet> refresh(final Context context, final VendorTokenSet token) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        return Message.success(
                exchange(
                        endpoint(VendorEndpoint.REFRESH),
                        Map.of(
                                "app_access_token",
                                applicationToken(current),
                                "grant_type",
                                "refresh_token",
                                "refresh_token",
                                Objects.requireNonNull(authorization.getRefresh(), "Refresh token must not be null"))));
    }

    /**
     * Returns the cached application token or obtains and caches a new token.
     *
     * @param context root operation context used to resolve the client secret
     * @return non-null application access token
     * @throws AuthorizedException if the Feishu response is invalid
     */
    private String applicationToken(final Context context) {
        final String key = descriptor().id() + ":app_access_token:" + registration.clientId();
        final String cached = cache.read(key);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }
        final AppTokenResponse response = JsonKit
                .toPojo(
                        post(
                                appTokenUrl(),
                                JsonKit.toJsonString(
                                        Map.of("app_id", registration.clientId(), "app_secret", secret(context))),
                                Map.of(Http.Header.CONTENT_TYPE, MediaType.APPLICATION_JSON),
                                MediaType.APPLICATION_JSON),
                        AppTokenResponse.class);
        check(
                response == null ? -1 : response.code(),
                response == null ? null : response.message(),
                "application token");
        if (response.app_access_token() == null) {
            throw new AuthorizedException("Missing app_access_token in Feishu response");
        }
        final long lifetimeMillis = Duration.ofSeconds(Math.max(0L, response.expire())).toMillis();
        cache.write(key, response.app_access_token(), lifetimeMillis);
        return response.app_access_token();
    }

    /**
     * Exchanges a Feishu authorization or refresh request and maps its token fields.
     *
     * @param url  effective token endpoint
     * @param body immutable JSON request fields
     * @return mapped Feishu token set
     * @throws AuthorizedException if the response reports an error or omits token data
     */
    private VendorTokenSet exchange(final String url, final Map<String, String> body) {
        final TokenResponse response = JsonKit.toPojo(
                post(
                        url,
                        JsonKit.toJsonString(body),
                        Map.of(Http.Header.CONTENT_TYPE, MediaType.APPLICATION_JSON),
                        MediaType.APPLICATION_JSON),
                TokenResponse.class);
        check(response == null ? -1 : response.code(), response == null ? null : response.message(), "token");
        if (response.data() == null || response.data().access_token() == null) {
            throw new AuthorizedException("Missing data or access_token in Feishu token response");
        }
        final TokenData data = response.data();
        return VendorTokenSet.builder().token(data.access_token()).refresh(data.refresh_token())
                .expireIn(data.expires_in()).tokenType(data.token_type()).openId(data.open_id()).build();
    }

    /**
     * Derives the fixed application-token path from the effective Feishu token endpoint origin.
     *
     * @return absolute application-token endpoint
     */
    private String appTokenUrl() {
        final URI token = URI.create(endpoint(VendorEndpoint.TOKEN));
        return token.resolve(APP_TOKEN_PATH).toString();
    }

    /**
     * Typed Feishu application-token response.
     *
     * @param code             numeric vendor response code
     * @param message          optional vendor response message
     * @param app_access_token application-scoped bearer token
     * @param expire           application-token lifetime in seconds
     * @author Kimi Liu
     */
    private record AppTokenResponse(int code, String message, String app_access_token, long expire) {
    }

    /**
     * Typed Feishu authorization and refresh response.
     *
     * @param code    numeric vendor response code
     * @param message optional vendor response message
     * @param data    token fields
     * @author Kimi Liu
     */
    private record TokenResponse(int code, String message, TokenData data) {
    }

    /**
     * Typed Feishu token data.
     *
     * @param access_token  sensitive user access token
     * @param refresh_token sensitive refresh token
     * @param expires_in    access-token lifetime in seconds
     * @param token_type    access-token scheme
     * @param open_id       application-scoped user identifier
     * @author Kimi Liu
     */
    private record TokenData(String access_token, String refresh_token, int expires_in, String token_type,
            String open_id) {
    }

    /**
     * Typed Feishu profile response.
     *
     * @param code    numeric vendor response code
     * @param message optional vendor response message
     * @param data    profile fields
     * @author Kimi Liu
     */
    private record ProfileResponse(int code, String message, ProfileData data) {
    }

    /**
     * Typed Feishu profile data.
     *
     * @param union_id   cross-application user identifier
     * @param name       display name
     * @param avatar_url avatar URL
     * @param email      email address granted by the vendor
     * @author Kimi Liu
     */
    private record ProfileData(String union_id, String name, String avatar_url, String email) {
    }

}
