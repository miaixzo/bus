/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.auth.nimble.wechat.mp;

import org.miaixz.bus.auth.magic.Authorization;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Claims;
import org.miaixz.bus.auth.nimble.wechat.AbstractWeChatProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * WeChat Official Account (MP) login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WeChatMpProvider extends AbstractWeChatProvider {

    /**
     * Constructs a {@code WeChatMpProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public WeChatMpProvider(Context context) {
        super(context, Registry.WECHAT_MP);
    }

    /**
     * Constructs a {@code WeChatMpProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public WeChatMpProvider(Context context, CacheX cache) {
        super(context, Registry.WECHAT_MP, cache);
    }

    /**
     * Due to the specificity of WeChat, the returned information at this time includes both openid and access_token.
     *
     * @param callback the callback parameters returned
     * @return all information
     */
    @Override
    public Message token(Callback callback) {
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(this.getToken(tokenUrl(callback.getCode())))
                .build();
    }

    /**
     * Retrieves user information from WeChat Official Account's user info endpoint.
     *
     * @param authorization the {@link Authorization} obtained after successful authorization
     * @return {@link Claims} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Message userInfo(Authorization authorization) {
        String openId = authorization.getOpenId();
        String scope = authorization.getScope();
        if (!StringKit.isEmpty(scope) && !scope.contains("snsapi_userinfo")) {
            Map<String, Object> tokenMap = new HashMap<>();
            tokenMap.put("access_token", authorization.getToken());
            tokenMap.put("refresh_token", authorization.getRefresh());
            tokenMap.put("expires_in", authorization.getExpireIn());
            tokenMap.put("openid", authorization.getOpenId());
            tokenMap.put("scope", authorization.getScope());
            tokenMap.put("is_snapshotuser", authorization.isSnapshotUser() ? 1 : 0);

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                    .data(
                            Claims.builder().rawJson(JsonKit.toJsonString(tokenMap)).uuid(openId)
                                    .snapshotUser(authorization.isSnapshotUser()).token(authorization)
                                    .source(complex.toString()).build())
                    .build();
        }

        String response = doGetUserInfo(authorization);
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }

            this.checkResponse(object);

            String country = (String) object.get("country");
            String province = (String) object.get("province");
            String city = (String) object.get("city");
            String location = String.format("%s-%s-%s", country, province, city);

            String unionId = (String) object.get("unionid");
            if (unionId != null) {
                authorization.setUnionId(unionId);
            }

            String nickname = (String) object.get("nickname");
            String headimgurl = (String) object.get("headimgurl");
            String sex = (String) object.get("sex");

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                    .data(
                            Claims.builder().rawJson(JsonKit.toJsonString(object)).username(nickname).nickname(nickname)
                                    .avatar(headimgurl).location(location).uuid(openId)
                                    .snapshotUser(authorization.isSnapshotUser()).gender(getWechatRealGender(sex))
                                    .token(authorization).source(complex.toString()).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    /**
     * Refreshes the access token (renews its validity).
     *
     * @param authorization the token information returned after successful login
     * @return a {@link Message} containing the refreshed token information
     */
    @Override
    public Message refresh(Authorization authorization) {
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(this.getToken(refreshUrl(authorization.getRefresh()))).build();
    }

    /**
     * Checks if the response content is correct.
     *
     * @param object the response map to check
     * @throws AuthorizedException if the response indicates an error or message indicating failure
     */
    private void checkResponse(Map<String, Object> object) {
        if (object.containsKey(Consts.ERRCODE)) {
            String errcode = String.valueOf(object.get(Consts.ERRCODE));
            String errmsg = (String) object.get(Consts.ERRMSG);
            throw new AuthorizedException(errcode, errmsg != null ? errmsg : "Unknown error");
        }
    }

    /**
     * Retrieves the token, applicable for both obtaining access tokens and refreshing tokens.
     *
     * @param tokenUrl the actual URL to request the token from
     * @return the {@link Authorization} object
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    private Authorization getToken(String tokenUrl) {
        String response = Httpx.get(tokenUrl);
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse token response: empty response");
            }

            this.checkResponse(object);

            String token = (String) object.get("access_token");
            if (token == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            String refresh = (String) object.get("refresh_token");
            Object expiresInObj = object.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;
            String openId = (String) object.get("openid");
            String scope = (String) object.get("scope");
            Object snapshotUserObj = object.get("is_snapshotuser");
            boolean snapshotUser = snapshotUserObj instanceof Number && ((Number) snapshotUserObj).intValue() == 1;

            return Authorization.builder().token(token).refresh(refresh).expireIn(expiresIn).openId(openId).scope(scope)
                    .snapshotUser(snapshotUser).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse token response: " + e.getMessage());
        }
    }

    /**
     * Returns the authorization URL with a {@code state} parameter. The {@code state} will be included in the
     * authorization callback.
     *
     * @param state the parameter to verify the authorization process, which can prevent CSRF attacks
     * @return the authorization URL
     */
    @Override
    public Message build(String state) {
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(
                        Builder.fromUrl(complex.authorize()).queryParam("appid", context.getClientId())
                                .queryParam("redirect_uri", UrlEncoder.encodeAll(context.getRedirectUri()))
                                .queryParam("response_type", "code")
                                .queryParam(
                                        "scope",
                                        this.getScopes(Symbol.COMMA, false, this.getScopes(WechatMpScope.values())))
                                .queryParam("state", getRealState(state).concat("#wechat_redirect")).build())
                .build();
    }

    /**
     * Returns the URL to obtain the access token.
     *
     * @param code the authorization code
     * @return the URL to obtain the access token
     */
    @Override
    protected String tokenUrl(String code) {
        return Builder.fromUrl(this.complex.token()).queryParam("code", code).queryParam("appid", context.getClientId())
                .queryParam("secret", context.getClientSecret()).queryParam("grant_type", "authorization_code").build();
    }

    /**
     * Returns the URL to obtain user information.
     *
     * @param authorization the user's authorization token
     * @return the URL to obtain user information
     */
    @Override
    protected String userInfoUrl(Authorization authorization) {
        return Builder.fromUrl(this.complex.userinfo()).queryParam("access_token", authorization.getToken())
                .queryParam("openid", authorization.getOpenId()).queryParam("lang", "zh_CN").build();
    }

    /**
     * Returns the URL to refresh the access token.
     *
     * @param refresh the refresh token returned by the getToken method
     * @return the URL to refresh the access token
     */
    @Override
    protected String refreshUrl(String refresh) {
        return Builder.fromUrl(this.complex.refresh()).queryParam("appid", context.getClientId())
                .queryParam("refresh_token", refresh).queryParam("grant_type", "refresh_token").build();
    }

}
