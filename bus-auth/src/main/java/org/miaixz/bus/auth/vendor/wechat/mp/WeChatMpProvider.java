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
package org.miaixz.bus.auth.vendor.wechat.mp;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.auth.vendor.wechat.AbstractWeChatProvider;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party client for WeChat Official Account web authorization.
 *
 * @author Kimi Liu
 */
public class WeChatMpProvider extends AbstractWeChatProvider {

    /**
     * Default scope selected when the registration supplies no explicit scopes.
     */
    private static final List<String> DEFAULT_SCOPES = List.of("snsapi_userinfo");

    /**
     * Creates a WeChat Official Account client from explicit runtime dependencies.
     *
     * @param configuration complete non-null vendor dependencies
     * @throws NullPointerException if the configuration or a required dependency is null
     */
    public WeChatMpProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.WECHAT_MP);
    }

    /**
     * Parses and validates token response fields.
     *
     * @param document token response JSON
     * @return mapped vendor token set
     * @throws AuthorizedException if the response is absent, reports an error, or omits the access token
     */
    private static VendorTokenSet readToken(final String document) {
        final TokenResponse response = JsonKit.toPojo(document, TokenResponse.class);
        validate(response);
        if (response.access_token() == null) {
            throw new AuthorizedException("Missing access_token in WeChat response");
        }
        return VendorTokenSet.builder().token(response.access_token()).refresh(response.refresh_token())
                .expireIn(response.expires_in()).openId(response.openid()).scope(response.scope())
                .snapshotUser(response.is_snapshotuser() == 1).build();
    }

    /**
     * Validates a typed WeChat response envelope.
     *
     * @param response typed response envelope
     * @throws AuthorizedException if the envelope is null or contains an error code
     */
    private static void validate(final Response response) {
        if (response == null) {
            throw new AuthorizedException("Failed to parse WeChat response: empty response");
        }
        if (response.errcode() != null && !"0".equals(response.errcode())) {
            throw new AuthorizedException(response.errcode(),
                    response.errmsg() == null ? "Unknown error" : response.errmsg());
        }
    }

    /**
     * Builds the account authorization URL and atomically registers its state.
     *
     * @param context immutable root operation context used to isolate state
     * @param state   optional caller-supplied authorization state
     * @return successful message containing the authorization URL
     * @throws NullPointerException if the context is null
     */
    @Override
    public Message<String> build(final Context context, final String state) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final String url = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.AUTHORIZE))
                .queryParam("appid", registration.clientId())
                .queryParam("redirect_uri", UrlEncoder.encodeAll(registration.redirectUri()))
                .queryParam("response_type", "code").queryParam("scope", scopes(Symbol.COMMA, false, DEFAULT_SCOPES))
                .queryParam("state", state(current, state)).build();
        return Message.success(url.concat("#wechat_redirect"));
    }

    /**
     * Exchanges an authorization code through WeChat's query-authenticated token GET operation.
     *
     * @param context  root operation context used to resolve and clear the client secret
     * @param callback immutable inbound callback containing the authorization code
     * @return successful message containing the WeChat token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the response is absent, reports an error, or omits the access token
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final String url = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.TOKEN))
                .queryParam("code", inbound.value("code").orElse(null)).queryParam("appid", registration.clientId())
                .queryParam("secret", secret(current)).queryParam("grant_type", "authorization_code").build();
        return Message.success(readToken(get(url)));
    }

    /**
     * Retrieves an authorized profile or returns the token-only snapshot identity required by a reduced scope.
     *
     * @param context immutable root operation context
     * @param token   non-null WeChat token set
     * @return successful message containing the profile or snapshot identity
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if a remote profile response is absent or reports an error
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final String scope = authorization.getScope();
        if (scope != null && !scope.isEmpty() && !scope.contains("snsapi_userinfo")) {
            final Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("access_token", authorization.getToken());
            snapshot.put("refresh_token", authorization.getRefresh());
            snapshot.put("expires_in", authorization.getExpireIn());
            snapshot.put("openid", authorization.getOpenId());
            snapshot.put("scope", scope);
            snapshot.put("is_snapshotuser", authorization.isSnapshotUser() ? 1 : 0);
            return Message.success(
                    VendorIdentity.builder().rawJson(JsonKit.toJsonString(snapshot)).uuid(authorization.getOpenId())
                            .snapshotUser(authorization.isSnapshotUser()).token(authorization).source(descriptor().id())
                            .build());
        }
        final String url = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.USERINFO))
                .queryParam("access_token", authorization.getToken()).queryParam("openid", authorization.getOpenId())
                .queryParam("lang", "zh_CN").build();
        final ProfileResponse profile = JsonKit.toPojo(get(url), ProfileResponse.class);
        validate(profile);
        if (profile.unionid() != null) {
            authorization.setUnionId(profile.unionid());
        }
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(profile)).username(profile.nickname())
                        .nickname(profile.nickname()).avatar(profile.headimgurl())
                        .location(String.format("%s-%s-%s", profile.country(), profile.province(), profile.city()))
                        .uuid(authorization.getOpenId()).snapshotUser(authorization.isSnapshotUser())
                        .gender(getWechatRealGender(profile.sex())).token(authorization).source(descriptor().id())
                        .build());
    }

    /**
     * Refreshes the token through WeChat's query-authenticated refresh GET operation.
     *
     * @param context immutable root operation context
     * @param token   non-null token set containing the refresh token
     * @return successful message containing the refreshed token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the response is absent, reports an error, or omits the access token
     */
    @Override
    public Message<VendorTokenSet> refresh(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final String url = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.REFRESH))
                .queryParam("appid", registration.clientId()).queryParam("refresh_token", authorization.getRefresh())
                .queryParam("grant_type", "refresh_token").build();
        return Message.success(readToken(get(url)));
    }

    /**
     * Shared typed WeChat error envelope.
     *
     * @author Kimi Liu
     */
    private interface Response {

        /**
         * Returns the vendor error code.
         *
         * @return vendor error code, absent or zero for success
         */
        String errcode();

        /**
         * Returns the vendor error diagnostic.
         *
         * @return vendor error diagnostic
         */
        String errmsg();
    }

    /**
     * Typed WeChat token response.
     *
     * @param errcode         vendor error code
     * @param errmsg          vendor error diagnostic
     * @param access_token    access token
     * @param refresh_token   refresh token
     * @param expires_in      access-token lifetime in seconds
     * @param openid          account-scoped user identifier
     * @param scope           granted scope text
     * @param is_snapshotuser snapshot-user numeric marker
     * @author Kimi Liu
     */
    private record TokenResponse(String errcode, String errmsg, String access_token, String refresh_token,
            int expires_in, String openid, String scope, int is_snapshotuser) implements Response {
    }

    /**
     * Typed WeChat profile response.
     *
     * @param errcode    vendor error code
     * @param errmsg     vendor error diagnostic
     * @param nickname   display name
     * @param headimgurl avatar URL
     * @param country    country text
     * @param province   province text
     * @param city       city text
     * @param unionid    cross-application user identifier
     * @param sex        vendor gender code
     * @author Kimi Liu
     */
    private record ProfileResponse(String errcode, String errmsg, String nickname, String headimgurl, String country,
            String province, String city, String unionid, String sex) implements Response {
    }

}
