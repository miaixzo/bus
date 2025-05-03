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
package org.miaixz.bus.oauth.metric.coding;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.Material;
import org.miaixz.bus.oauth.metric.AbstractProvider;

import java.util.Map;

/**
 * Coding 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CodingProvider extends AbstractProvider {

    public CodingProvider(Context context) {
        super(context, Registry.CODING);
    }

    public CodingProvider(Context context, ExtendCache cache) {
        super(context, Registry.CODING, cache);
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        String response = doGetAuthorizationCode(callback.getCode());
        try {
            Map<String, Object> accessTokenObject = JsonKit.toPojo(response, Map.class);
            if (accessTokenObject == null) {
                throw new AuthorizedException("Failed to parse access token response: empty response");
            }
            this.checkResponse(accessTokenObject);

            String accessToken = (String) accessTokenObject.get("access_token");
            if (accessToken == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            Object expiresInObj = accessTokenObject.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;
            String refreshToken = (String) accessTokenObject.get("refresh_token");

            return AccToken.builder().accessToken(accessToken).expireIn(expiresIn).refreshToken(refreshToken).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
        }
    }

    @Override
    public Material getUserInfo(AccToken accToken) {
        String response = doGetUserInfo(accToken);
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }
            this.checkResponse(object);

            Map<String, Object> data = (Map<String, Object>) object.get("data");
            if (data == null) {
                throw new AuthorizedException("Missing data field in user info response");
            }

            String id = String.valueOf(data.get("id"));
            if (id == null) {
                throw new AuthorizedException("Missing id in user info response");
            }
            String name = (String) data.get("name");
            String avatar = (String) data.get("avatar");
            String path = (String) data.get("path");
            String company = (String) data.get("company");
            String location = (String) data.get("location");
            String sex = (String) data.get("sex");
            String email = (String) data.get("email");
            String slogan = (String) data.get("slogan");

            return Material.builder().rawJson(JsonKit.toJsonString(data)).uuid(id).username(name)
                    .avatar(avatar != null ? "https://coding.net" + avatar : null)
                    .blog(path != null ? "https://coding.net" + path : null).nickname(name).company(company)
                    .location(location).gender(Gender.of(sex)).email(email).remark(slogan).token(accToken)
                    .source(complex.toString()).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(Map<String, Object> object) {
        if ((int) object.get("code") != 0) {
            throw new AuthorizedException((String) object.get("msg"));
        }
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     */
    @Override
    public String authorize(String state) {
        return Builder.fromUrl(String.format(complex.authorize(), context.getPrefix()))
                .queryParam("response_type", "code").queryParam("client_id", context.getAppKey())
                .queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("scope", this.getScopes(Symbol.SPACE, true, this.getDefaultScopes(CodingScope.values())))
                .queryParam("state", getRealState(state)).build();
    }

    /**
     * 返回获取accessToken的url
     *
     * @param code 授权码
     * @return 返回获取accessToken的url
     */
    @Override
    public String accessTokenUrl(String code) {
        return Builder.fromUrl(String.format(complex.accessToken(), context.getPrefix())).queryParam("code", code)
                .queryParam("client_id", context.getAppKey()).queryParam("client_secret", context.getAppSecret())
                .queryParam("grant_type", "authorization_code").queryParam("redirect_uri", context.getRedirectUri())
                .build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param accToken token
     * @return 返回获取userInfo的url
     */
    @Override
    public String userInfoUrl(AccToken accToken) {
        return Builder.fromUrl(String.format(complex.userInfo(), context.getPrefix()))
                .queryParam("access_token", accToken.getAccessToken()).build();
    }

}