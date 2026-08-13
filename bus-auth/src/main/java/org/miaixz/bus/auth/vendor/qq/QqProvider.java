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
package org.miaixz.bus.auth.vendor.qq;

import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party client for QQ authorization, token, profile, and refresh operations.
 *
 * @author Kimi Liu
 */
public class QqProvider extends AbstractProvider {

    /**
     * QQ endpoint that resolves OpenID and optional UnionID.
     */
    private static final String OPEN_ID_ENDPOINT = "https://graph.qq.com/oauth2.0/me";

    /**
     * Creates a QQ client from explicit runtime dependencies.
     *
     * @param configuration non-null runtime dependencies
     */
    public QqProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.QQ);
    }

    /**
     * Parses a form-encoded token response.
     *
     * @param document response document
     * @return immutable token set
     * @throws AuthorizedException if the response reports an error or omits the access token
     */
    private static VendorTokenSet readToken(final String document) {
        final Map<String, String> values = VendorRequestBuilder.parseStringToMap(document);
        if (!values.containsKey("access_token") || values.containsKey("code"))
            throw new AuthorizedException(values.get("msg"));
        return VendorTokenSet.builder().token(values.get("access_token"))
                .expireIn(Integer.parseInt(values.getOrDefault("expires_in", Symbol.ZERO)))
                .refresh(values.get("refresh_token")).build();
    }

    /**
     * Parses a JSONP-wrapped OpenID response.
     *
     * @param document response document
     * @return typed OpenID response
     * @throws AuthorizedException if parsing fails or QQ reports an error
     */
    private static OpenIdResponse readOpenId(final String document) {
        final String json = document.replace("callback(", Normal.EMPTY).replace(");", Normal.EMPTY).trim();
        final OpenIdResponse response = JsonKit.toPojo(json, OpenIdResponse.class);
        if (response == null || response.error() != null)
            throw new AuthorizedException(response == null ? "Empty QQ OpenID response"
                    : response.error() + Symbol.COLON + response.error_description());
        return response;
    }

    /**
     * Builds the QQ consent URL with comma-separated scopes and atomically registered state.
     *
     * @param context immutable root operation context
     * @param state   optional state
     * @return successful authorization URL
     */
    @Override
    public Message<String> build(final Context context, final String state) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        return Message.success(
                VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.AUTHORIZE)).queryParam("response_type", "code")
                        .queryParam("client_id", registration.clientId())
                        .queryParam("redirect_uri", registration.redirectUri())
                        .queryParam("state", state(current, state))
                        .queryParam("scope", scopes(Symbol.COMMA, false, getScopes(QqScope.values()))).build());
    }

    /**
     * Exchanges an authorization code through QQ's empty form POST.
     *
     * @param context  root context used for secret resolution
     * @param callback inbound callback
     * @return successful token result
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final String url = tokenUrl(current, inbound.value("code").orElse(null), "authorization_code", null);
        return Message.success(readToken(post(url)));
    }

    /**
     * Refreshes a QQ token through a query-authenticated GET operation.
     *
     * @param context root context used for secret resolution
     * @param token   non-null token set
     * @return successful refreshed token result
     */
    @Override
    public Message<VendorTokenSet> refresh(final Context context, final VendorTokenSet token) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        return Message.success(readToken(get(tokenUrl(current, null, "refresh_token", authorization.getRefresh()))));
    }

    /**
     * Resolves QQ OpenID and retrieves the user profile through two GET operations.
     *
     * @param context immutable root operation context
     * @param token   non-null token set
     * @return successful identity result
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final OpenIdResponse open = readOpenId(
                get(
                        OPEN_ID_ENDPOINT,
                        Map.of("access_token", authorization.getToken(), "unionid", registration.flag() ? 1 : 0)));
        authorization.setOpenId(open.openid());
        if (open.unionid() != null)
            authorization.setUnionId(open.unionid());
        final String identifier = StringKit.isEmpty(authorization.getUnionId()) ? authorization.getOpenId()
                : authorization.getUnionId();
        final ProfileResponse profile = JsonKit.toPojo(
                get(
                        endpoint(VendorEndpoint.USERINFO),
                        Map.of(
                                "access_token",
                                authorization.getToken(),
                                "oauth_consumer_key",
                                registration.clientId(),
                                "openid",
                                authorization.getOpenId())),
                ProfileResponse.class);
        if (profile == null || profile.ret() != 0) {
            throw new AuthorizedException(profile == null ? "Empty QQ profile response" : profile.msg());
        }
        final String avatar = StringKit.isEmpty(profile.figureurl_qq_2()) ? profile.figureurl_qq_1()
                : profile.figureurl_qq_2();
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(profile)).username(profile.nickname())
                        .nickname(profile.nickname()).avatar(avatar).location(profile.province() + "-" + profile.city())
                        .uuid(identifier).gender(Gender.of(profile.gender())).token(authorization)
                        .source(descriptor().id()).build());
    }

    /**
     * Builds a QQ token or refresh URL while resolving and clearing the operation-scoped secret.
     *
     * @param context root operation context
     * @param code    authorization code, or {@code null} for refresh
     * @param grant   OAuth grant type
     * @param refresh refresh token, or {@code null} for code exchange
     * @return encoded request URL
     */
    private String tokenUrl(final Context context, final String code, final String grant, final String refresh) {
        return VendorRequestBuilder.fromUrl(
                grant.equals("refresh_token") ? endpoint(VendorEndpoint.REFRESH) : endpoint(VendorEndpoint.TOKEN))
                .queryParam("code", code).queryParam("client_id", registration.clientId())
                .queryParam("client_secret", secret(context)).queryParam("refresh_token", refresh)
                .queryParam("grant_type", grant).queryParam("redirect_uri", registration.redirectUri()).build();
    }

    /**
     * QQ OpenID response.
     *
     * @param openid            OpenID
     * @param unionid           optional UnionID
     * @param error             error code
     * @param error_description error diagnostic
     * @author Kimi Liu
     */
    private record OpenIdResponse(String openid, String unionid, String error, String error_description) {
    }

    /**
     * QQ profile response.
     *
     * @param ret            numeric status, where zero denotes success
     * @param msg            diagnostic
     * @param nickname       nickname
     * @param figureurl_qq_1 small avatar URL
     * @param figureurl_qq_2 large avatar URL
     * @param province       province
     * @param city           city
     * @param gender         gender text
     * @author Kimi Liu
     */
    private record ProfileResponse(int ret, String msg, String nickname, String figureurl_qq_1, String figureurl_qq_2,
            String province, String city, String gender) {
    }

}
