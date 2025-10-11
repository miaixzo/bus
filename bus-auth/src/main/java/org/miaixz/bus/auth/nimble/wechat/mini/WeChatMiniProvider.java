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
package org.miaixz.bus.auth.nimble.wechat.mini;

import lombok.Data;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.AuthToken;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.Material;
import org.miaixz.bus.auth.nimble.AbstractProvider;

/**
 * WeChat Mini Program authorization login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WeChatMiniProvider extends AbstractProvider {

    /**
     * Constructs a {@code WeChatMiniProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public WeChatMiniProvider(Context context) {
        super(context, Registry.WECHAT_MINI);
    }

    /**
     * Constructs a {@code WeChatMiniProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public WeChatMiniProvider(Context context, CacheX cache) {
        super(context, Registry.WECHAT_MINI, cache);
    }

    /**
     * Retrieves the access token for the WeChat Mini Program. See
     * https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/login/auth.code2Session.html documentation.
     * Uses the authorization code to obtain the openId, unionId, and session_key.
     *
     * @param authCallback the callback object containing the authorization code
     * @return the {@link AuthToken} containing access token details
     * @throws AuthorizedException if the response indicates an error or is missing required token information
     */
    @Override
    public AuthToken getAccessToken(Callback authCallback) {
        // See https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/login/auth.code2Session.html
        // documentation
        // Use the code to get the corresponding openId, unionId, etc.
        String response = Httpx.get(accessTokenUrl(authCallback.getCode()));
        JSCode2SessionResponse accessTokenObject = JsonKit.toPojo(response, JSCode2SessionResponse.class);
        assert accessTokenObject != null;
        checkResponse(accessTokenObject);
        // Assemble the result
        return AuthToken.builder().openId(accessTokenObject.getOpenid()).unionId(accessTokenObject.getUnionid())
                .accessToken(accessTokenObject.getSession_key()).build();
    }

    /**
     * Retrieves user information for the WeChat Mini Program. See
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
        return Material.builder().username("").nickname("").avatar("").uuid(authToken.getOpenId()).token(authToken)
                .source(complex.toString()).build();
    }

    /**
     * Checks if the response content is correct.
     *
     * @param response the response content from the request
     * @throws AuthorizedException if the response indicates an error
     */
    private void checkResponse(JSCode2SessionResponse response) {
        if (!Symbol.ZERO.equals(response.getErrcode())) {
            throw new AuthorizedException(response.getErrcode(), response.getErrmsg());
        }
    }

    /**
     * Constructs the access token URL for the WeChat Mini Program.
     *
     * @param code the authorization code
     * @return the access token URL
     */
    @Override
    protected String accessTokenUrl(String code) {
        return Builder.fromUrl(this.complex.accessToken()).queryParam("appid", context.getAppKey())
                .queryParam("secret", context.getAppSecret()).queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code").build();
    }

    /**
     * Data class representing the response from WeChat Mini Program's jscode2session API.
     */
    @Data
    private static class JSCode2SessionResponse {

        /**
         * Error code returned by the API.
         */
        private String errcode;
        /**
         * Error message returned by the API.
         */
        private String errmsg;
        /**
         * Session key for the user.
         */
        private String session_key;
        /**
         * User's OpenID.
         */
        private String openid;
        /**
         * User's UnionID.
         */
        private String unionid;

    }

}
