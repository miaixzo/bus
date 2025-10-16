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
package org.miaixz.bus.auth.nimble.wechat.open;

import org.miaixz.bus.auth.magic.AuthToken;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Material;
import org.miaixz.bus.auth.nimble.wechat.AbstractWeChatProvider;

import java.util.Map;

/**
 * WeChat Open Platform login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WeChatOpenProvider extends AbstractWeChatProvider {

    /**
     * Constructs a {@code WeChatOpenProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public WeChatOpenProvider(Context context) {
        super(context, Registry.WECHAT_OPEN);
    }

    /**
     * Constructs a {@code WeChatOpenProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public WeChatOpenProvider(Context context, CacheX cache) {
        super(context, Registry.WECHAT_OPEN, cache);
    }

    /**
     * Due to the specificity of WeChat, the returned information at this time includes both openid and access_token.
     *
     * @param callback the callback parameters returned
     * @return all information
     */
    @Override
    public AuthToken getAccessToken(Callback callback) {
        return this.getToken(accessTokenUrl(callback.getCode()));
    }

    /**
     * Retrieves user information from WeChat Open Platform's user info endpoint.
     *
     * @param authToken the {@link AuthToken} obtained after successful authorization
     * @return {@link Material} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Material getUserInfo(AuthToken authToken) {
        String openId = authToken.getOpenId();
        String response = doGetUserInfo(authToken);
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
                authToken.setUnionId(unionId);
            }

            String nickname = (String) object.get("nickname");
            String headimgurl = (String) object.get("headimgurl");
            String sex = (String) object.get("sex");

            return Material.builder().rawJson(JsonKit.toJsonString(object)).username(nickname).nickname(nickname)
                    .avatar(headimgurl).location(location).uuid(openId).gender(getWechatRealGender(sex))
                    .token(authToken).source(complex.toString()).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    /**
     * Refreshes the access token (renews its validity).
     *
     * @param authToken the token information returned after successful login
     * @return a {@link Message} containing the refreshed token information
     */
    @Override
    public Message refresh(AuthToken authToken) {
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(this.getToken(refreshTokenUrl(authToken.getRefreshToken()))).build();
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
     * @param accessTokenUrl the actual URL to request the token from
     * @return the {@link AuthToken} object
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    private AuthToken getToken(String accessTokenUrl) {
        String response = Httpx.get(accessTokenUrl);
        try {
            Map<String, Object> accessTokenObject = JsonKit.toPojo(response, Map.class);
            if (accessTokenObject == null) {
                throw new AuthorizedException("Failed to parse token response: empty response");
            }

            this.checkResponse(accessTokenObject);

            String accessToken = (String) accessTokenObject.get("access_token");
            if (accessToken == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            String refreshToken = (String) accessTokenObject.get("refresh_token");
            Object expiresInObj = accessTokenObject.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;
            String openId = (String) accessTokenObject.get("openid");

            return AuthToken.builder().accessToken(accessToken).refreshToken(refreshToken).expireIn(expiresIn)
                    .openId(openId).build();
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
    public String authorize(String state) {
        return Builder.fromUrl(complex.authorize()).queryParam("response_type", "code")
                .queryParam("appid", context.getAppKey()).queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("scope", "snsapi_login").queryParam("state", getRealState(state)).build();
    }

    /**
     * Returns the URL to obtain the access token.
     *
     * @param code the authorization code
     * @return the URL to obtain the access token
     */
    @Override
    protected String accessTokenUrl(String code) {
        return Builder.fromUrl(this.complex.accessToken()).queryParam("code", code)
                .queryParam("appid", context.getAppKey()).queryParam("secret", context.getAppSecret())
                .queryParam("grant_type", "authorization_code").build();
    }

    /**
     * Returns the URL to obtain user information.
     *
     * @param authToken the user's authorization token
     * @return the URL to obtain user information
     */
    @Override
    protected String userInfoUrl(AuthToken authToken) {
        return Builder.fromUrl(this.complex.userinfo()).queryParam("access_token", authToken.getAccessToken())
                .queryParam("openid", authToken.getOpenId()).queryParam("lang", "zh_CN").build();
    }

    /**
     * Returns the URL to refresh the access token.
     *
     * @param refreshToken the refresh token returned by the getAccessToken method
     * @return the URL to refresh the access token
     */
    @Override
    protected String refreshTokenUrl(String refreshToken) {
        return Builder.fromUrl(this.complex.refresh()).queryParam("appid", context.getAppKey())
                .queryParam("refresh_token", refreshToken).queryParam("grant_type", "refresh_token").build();
    }

}
