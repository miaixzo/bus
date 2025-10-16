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
package org.miaixz.bus.auth.nimble.douyin;

import java.util.Map;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.AuthToken;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.Material;
import org.miaixz.bus.auth.nimble.AbstractProvider;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;

/**
 * Douyin Mini Program login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DouyinMiniProvider extends AbstractProvider {

    /**
     * Constructs a {@code DouyinMiniProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public DouyinMiniProvider(Context context) {
        super(context, Registry.DOUYIN_MINI);
    }

    /**
     * Constructs a {@code DouyinMiniProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public DouyinMiniProvider(Context context, CacheX cache) {
        super(context, Registry.DOUYIN_MINI, cache);
    }

    /**
     * Retrieves the access token for the Douyin Mini Program. See
     * https://developer.open-douyin.com/docs/resource/zh-CN/mini-game/develop/api/open-capacity/log-in/auth.code2Session.html
     * documentation. Uses the authorization code to obtain the openId, unionId, and session_key.
     *
     * @param callback the callback object containing the authorization code and anonymous code
     * @return the {@link AuthToken} containing access token details
     * @throws AuthorizedException if the response indicates an error or is missing required token information
     */
    @Override
    public AuthToken getAccessToken(Callback callback) {
        // See https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/login/auth.code2Session.html
        // documentation
        // Use the code to get the corresponding openId, unionId, etc.
        String response = Httpx.get(accessTokenUrl(callback.getCode(), callback.getAnonymous_code()));

        Map<String, Object> accessTokenObject = JsonKit.toPojo(response, Map.class);
        checkResponse(accessTokenObject);

        // Assemble the result
        return AuthToken.builder().openId((String) accessTokenObject.get("openid"))
                .unionId((String) accessTokenObject.get("unionid"))
                .accessToken((String) accessTokenObject.get("session_key")).build();
    }

    /**
     * Retrieves user information for the Douyin Mini Program. See
     * https://developers.weixin.qq.com/miniprogram/dev/api/open-api/user-info/wx.getUserProfile.html documentation.
     * Note: If user information is required, it needs to be passed to the backend after the Mini Program calls a
     * function.
     *
     * @param authToken the {@link AuthToken} obtained after successful authorization
     * @return {@link Material} containing the user's information
     */
    @Override
    public Material getUserInfo(AuthToken authToken) {
        // See https://developers.weixin.qq.com/miniprogram/dev/api/open-api/user-info/wx.getUserProfile.html
        // documentation
        // If user information is required, it needs to be passed to the backend after the Mini Program calls a function
        return Material.builder().username(Normal.EMPTY).nickname(Normal.EMPTY).avatar(Normal.EMPTY)
                .uuid(authToken.getOpenId()).token(authToken).source(complex.toString()).build();
    }

    /**
     * Constructs the access token URL for the Douyin Mini Program.
     *
     * @param code          the authorization code
     * @param anonymousCode the anonymous code for anonymous login
     * @return the access token URL
     */
    private String accessTokenUrl(String code, String anonymousCode) {
        return Builder.fromUrl(this.complex.accessToken()).queryParam("appid", this.context.getAppKey())
                .queryParam("secret", this.context.getAppSecret()).queryParam("code", code)
                .queryParam("anonymous_code", anonymousCode).build();
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
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse token response: empty response");
            }
            this.checkResponse(object);

            Map<String, Object> dataObj = (Map<String, Object>) object.get(Consts.DATA);
            if (dataObj == null) {
                throw new AuthorizedException("Missing data field in token response");
            }

            String accessToken = (String) dataObj.get("access_token");
            if (accessToken == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            String openId = (String) dataObj.get("anonymousOpenid");
            Object expiresInObj = dataObj.get("expires_in");
            String unionId = dataObj.get("unionId").toString();
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;
            String refreshToken = (String) dataObj.get("refresh_token");
            Object refreshExpiresInObj = dataObj.get("refresh_expires_in");
            int refreshExpiresIn = refreshExpiresInObj instanceof Number ? ((Number) refreshExpiresInObj).intValue()
                    : 0;
            String scope = (String) dataObj.get("scope");

            return AuthToken.builder().accessToken(accessToken).openId(openId).expireIn(expiresIn).unionId(unionId)
                    .refreshToken(refreshToken).refreshTokenExpireIn(refreshExpiresIn).scope(scope).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse token response: " + e.getMessage());
        }
    }

    /**
     * Checks if the response content is correct.
     *
     * @param object the response map to check
     * @throws AuthorizedException if the response indicates an error
     */
    private void checkResponse(Map<String, Object> object) {
        String message = (String) object.get("message");
        Map<String, Object> data = (Map<String, Object>) object.get(Consts.DATA);
        if (data == null) {
            throw new AuthorizedException("Missing data field in response");
        }
        Object errorCodeObj = data.get("error_code");
        String errorCode = errorCodeObj != null ? String.valueOf(errorCodeObj) : null;
        if ("error".equals(message) || !"0".equals(errorCode)) {
            String description = (String) data.get("description");
            throw new AuthorizedException(errorCode, description != null ? description : "Unknown error");
        }
    }

}
