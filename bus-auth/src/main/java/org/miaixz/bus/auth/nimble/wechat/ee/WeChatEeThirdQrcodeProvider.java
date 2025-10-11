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
package org.miaixz.bus.auth.nimble.wechat.ee;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.magic.AuthToken;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Material;

import java.util.HashMap;
import java.util.Map;

/**
 * WeChat Enterprise third-party QR code login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WeChatEeThirdQrcodeProvider extends AbstractWeChatEeProvider {

    /**
     * Constructs a {@code WeChatEeThirdQrcodeProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public WeChatEeThirdQrcodeProvider(Context context) {
        super(context, Registry.WECHAT_EE_QRCODE);
    }

    /**
     * Constructs a {@code WeChatEeThirdQrcodeProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public WeChatEeThirdQrcodeProvider(Context context, CacheX cache) {
        super(context, Registry.WECHAT_EE_QRCODE, cache);
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
        return Builder.fromUrl(complex.authorize()).queryParam("appid", context.getAppKey())
                .queryParam("redirect_uri", context.getRedirectUri()).queryParam("state", getRealState(state))
                .queryParam("usertype", context.getType()).build();
    }

    /**
     * Handles the login process, including state validation, access token retrieval, and user information fetching.
     *
     * @param callback the callback object containing authorization data
     * @return a {@link Message} object containing user information or an error message
     */
    @Override
    public Message login(Callback callback) {
        try {
            if (!context.isIgnoreState()) {
                Checker.check(callback.getState(), complex, cache);
            }
            AuthToken authToken = this.getAccessToken(callback);
            Material user = this.getUserInfo(authToken);
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(user).build();
        } catch (Exception e) {
            Logger.error("Failed to login with auth authorization.", e);
            return this.responseError(e);
        }
    }

    /**
     * Retrieves the access token from WeChat Enterprise's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link AuthToken} containing access token details
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    @Override
    public AuthToken getAccessToken(Callback callback) {
        try {
            String response = doGetAuthorizationCode(accessTokenUrl());
            Map<String, Object> object = this.checkResponse(response);
            String accessToken = (String) object.get("provider_access_token");
            if (accessToken == null) {
                throw new AuthorizedException("Missing provider_access_token in response");
            }
            Object expiresInObj = object.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;

            return AuthToken.builder().accessToken(accessToken).expireIn(expiresIn).build();
        } catch (Exception e) {
            throw new AuthorizedException("企业微信获取token失败", e);
        }
    }

    /**
     * Performs a GET request to obtain the authorization code.
     *
     * @param code the authorization code
     * @return the response content
     */
    @Override
    public String doGetAuthorizationCode(String code) {
        Map<String, Object> data = new HashMap<>();
        data.put("corpid", context.getAppKey());
        data.put("provider_secret", context.getAppSecret());
        return Httpx.post(accessTokenUrl(code), JsonKit.toJsonString(data), MediaType.APPLICATION_JSON);
    }

    /**
     * Returns the URL to obtain the access token.
     *
     * @return the access token URL
     */
    public String accessTokenUrl() {
        return Builder.fromUrl(this.complex.accessToken()).build();
    }

    /**
     * Retrieves user information from WeChat Enterprise's user info endpoint.
     *
     * @param authToken the {@link AuthToken} obtained after successful authorization
     * @return {@link Material} containing the user's information
     */
    @Override
    public Material getUserInfo(AuthToken authToken) {
        Map<String, Object> response = this.checkResponse(doGetUserInfo(authToken));
        return Material.builder().rawJson(JsonKit.toJsonString(response)).build();
    }

    /**
     * Performs a GET request to obtain user information.
     *
     * @param authToken the access token
     * @return the response content
     */
    @Override
    public String doGetUserInfo(AuthToken authToken) {
        Map<String, Object> data = new HashMap<>();
        data.put("auth_code", authToken.getCode());
        return Httpx.post(userInfoUrl(authToken), JsonKit.toJsonString(data), MediaType.APPLICATION_JSON);
    }

    /**
     * Returns the URL to obtain user information.
     *
     * @param authToken the user's authorization token
     * @return the URL to obtain user information
     */
    @Override
    public String userInfoUrl(AuthToken authToken) {
        return Builder.fromUrl(this.complex.userinfo()).queryParam("access_token", authToken.getAccessToken()).build();
    }

    /**
     * Checks the response content for errors.
     *
     * @param response the response string to check
     * @return the parsed response map if the request result is normal
     * @throws AuthorizedException if the response indicates an error or is malformed
     */
    private Map<String, Object> checkResponse(String response) {
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse response: empty response");
            }
            Object errcodeObj = object.get(Consts.ERRCODE);
            if (errcodeObj != null && !errcodeObj.equals(0)) {
                String errmsg = (String) object.get(Consts.ERRMSG);
                throw new AuthorizedException(errmsg != null ? errmsg : "Unknown error", complex.getName());
            }
            return object;
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse response: " + e.getMessage());
        }
    }

}
