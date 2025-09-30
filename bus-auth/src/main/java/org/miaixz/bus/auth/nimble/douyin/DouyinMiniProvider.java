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
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;

/**
 * 抖音 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DouyinMiniProvider extends AbstractProvider {

    public DouyinMiniProvider(Context context) {
        super(context, Registry.DOUYIN_MINI);
    }

    public DouyinMiniProvider(Context context, CacheX cache) {
        super(context, Registry.DOUYIN_MINI, cache);
    }

    @Override
    public AuthToken getAccessToken(Callback callback) {
        return this.getToken(accessTokenUrl(callback.getCode(), callback.getAnonymous_code()));
    }

    @Override
    public Material getUserInfo(AuthToken authToken) {
        // 参见
        // https://developer.open-douyin.com/docs/resource/zh-CN/mini-game/develop/guide/open-api/info/tt-get-user-info
        // 如果需要用户信息，需要在小程序调用函数后传给后端
        return Material.builder().username(Normal.EMPTY).nickname(Normal.EMPTY).avatar(Normal.EMPTY)
                .uuid(authToken.getOpenId()).token(authToken).source(authToken.toString()).build();
    }

    private String accessTokenUrl(String code, String anonymousCode) {
        return Builder.fromUrl(this.complex.accessToken()).queryParam("appid", this.context.getAppKey())
                .queryParam("secret", this.context.getAppSecret()).queryParam("code", code)
                .queryParam("anonymous_code", anonymousCode).build();
    }

    /**
     * 获取token，适用于获取access_token和刷新token
     *
     * @param accessTokenUrl 实际请求token的地址
     * @return token对象
     */
    private AuthToken getToken(String accessTokenUrl) {
        String response = Httpx.post(accessTokenUrl);
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse token response: empty response");
            }
            this.checkResponse(object);

            Map<String, Object> dataObj = (Map<String, Object>) object.get("data");
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
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(Map<String, Object> object) {
        String message = (String) object.get("message");
        Map<String, Object> data = (Map<String, Object>) object.get("data");
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
