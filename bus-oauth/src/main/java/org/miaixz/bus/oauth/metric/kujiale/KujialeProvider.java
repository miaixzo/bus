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
package org.miaixz.bus.oauth.metric.kujiale;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.ErrorCode;
import org.miaixz.bus.oauth.magic.Material;
import org.miaixz.bus.oauth.metric.AbstractProvider;

import java.util.Map;

/**
 * 酷家乐 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class KujialeProvider extends AbstractProvider {

    public KujialeProvider(Context context) {
        super(context, Registry.KUJIALE);
    }

    public KujialeProvider(Context context, ExtendCache cache) {
        super(context, Registry.KUJIALE, cache);
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state} 默认只向用户请求用户信息授权
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     */
    @Override
    public String authorize(String state) {
        return Builder.fromUrl(super.authorize(state))
                .queryParam("scope", this.getScopes(Symbol.COMMA, false, this.getDefaultScopes(KujialeScope.values())))
                .build();
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        String response = doPostAuthorizationCode(callback.getCode());
        return getAuthToken(response);
    }

    private AccToken getAuthToken(String response) {
        Map<String, Object> accessTokenObject = checkResponse(response);
        Map<String, Object> resultObject = (Map<String, Object>) accessTokenObject.get("d");
        if (resultObject == null) {
            throw new AuthorizedException("Missing d in access token response");
        }

        String accessToken = (String) resultObject.get("accessToken");
        if (accessToken == null) {
            throw new AuthorizedException("Missing accessToken in response");
        }
        String refreshToken = (String) resultObject.get("refreshToken");
        Object expiresInObj = resultObject.get("expiresIn");
        int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;

        return AccToken.builder().accessToken(accessToken).refreshToken(refreshToken).expireIn(expiresIn).build();
    }

    private Map<String, Object> checkResponse(String response) {
        try {
            Map<String, Object> accessTokenObject = JsonKit.toPojo(response, Map.class);
            if (accessTokenObject == null) {
                throw new AuthorizedException("Failed to parse response: empty response");
            }
            String code = (String) accessTokenObject.get("c");
            if (!"0".equals(code)) {
                String message = (String) accessTokenObject.get("m");
                throw new AuthorizedException(message != null ? message : "Unknown error");
            }
            return accessTokenObject;
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse response: " + e.getMessage());
        }
    }

    @Override
    public Material getUserInfo(AccToken accToken) {
        String openId = this.getOpenId(accToken);
        String response = Httpx.get(Builder.fromUrl(complex.userInfo())
                .queryParam("access_token", accToken.getAccessToken()).queryParam("open_id", openId).build());
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }
            String code = (String) object.get("c");
            if (!"0".equals(code)) {
                String message = (String) object.get("m");
                throw new AuthorizedException(message != null ? message : "Unknown error");
            }

            Map<String, Object> resultObject = (Map<String, Object>) object.get("d");
            if (resultObject == null) {
                throw new AuthorizedException("Missing d in user info response");
            }

            String userName = (String) resultObject.get("userName");
            if (userName == null) {
                throw new AuthorizedException("Missing userName in user info response");
            }
            String avatar = (String) resultObject.get("avatar");
            String openIdFromResponse = (String) resultObject.get("openId");

            return Material.builder().rawJson(JsonKit.toJsonString(resultObject)).username(userName).nickname(userName)
                    .avatar(avatar).uuid(openIdFromResponse).token(accToken).source(complex.toString()).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    /**
     * 获取酷家乐的openId，此id在当前client范围内可以唯一识别授权用户
     *
     * @param accToken 通过{@link KujialeProvider#getAccessToken(Callback)}获取到的{@code accToken}
     * @return openId
     */
    private String getOpenId(AccToken accToken) {
        String response = Httpx.get(Builder.fromUrl("https://oauth.kujiale.com/oauth2/auth/user")
                .queryParam("access_token", accToken.getAccessToken()).build());
        Map<String, Object> accessTokenObject = checkResponse(response);
        String openId = (String) accessTokenObject.get("d");
        if (openId == null) {
            throw new AuthorizedException("Missing openId in response");
        }
        return openId;
    }

    @Override
    public Message refresh(AccToken accToken) {
        String response = Httpx.post(refreshTokenUrl(accToken.getRefreshToken()));
        return Message.builder().errcode(ErrorCode.SUCCESS.getCode()).data(getAuthToken(response)).build();
    }

}