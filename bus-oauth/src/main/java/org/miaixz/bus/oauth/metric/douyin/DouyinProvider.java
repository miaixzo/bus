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
package org.miaixz.bus.oauth.metric.douyin;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
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
 * 抖音 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DouyinProvider extends AbstractProvider {

    public DouyinProvider(Context context) {
        super(context, Registry.DOUYIN);
    }

    public DouyinProvider(Context context, ExtendCache cache) {
        super(context, Registry.DOUYIN, cache);
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        return this.getToken(accessTokenUrl(callback.getCode()));
    }

    @Override
    public Material getUserInfo(AccToken accToken) {
        String response = doGetUserInfo(accToken);
        try {
            Map<String, Object> userInfoObject = JsonKit.toPojo(response, Map.class);
            if (userInfoObject == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }
            this.checkResponse(userInfoObject);

            Map<String, Object> data = (Map<String, Object>) userInfoObject.get("data");
            if (data == null) {
                throw new AuthorizedException("Missing data field in user info response");
            }

            String unionId = (String) data.get("union_id");
            if (unionId == null) {
                throw new AuthorizedException("Missing union_id in user info response");
            }
            String nickname = (String) data.get("nickname");
            String avatar = (String) data.get("avatar");
            String description = (String) data.get("description");
            String gender = (String) data.get("gender");
            String country = (String) data.get("country");
            String province = (String) data.get("province");
            String city = (String) data.get("city");

            accToken.setUnionId(unionId);

            return Material.builder().rawJson(JsonKit.toJsonString(data)).uuid(unionId).username(nickname)
                    .nickname(nickname).avatar(avatar).remark(description).gender(Gender.of(gender))
                    .location(String.format("%s %s %s", country, province, city)).token(accToken)
                    .source(complex.toString()).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    @Override
    public Message refresh(AccToken oldToken) {
        return Message.builder().errcode(ErrorCode.SUCCESS.getCode())
                .data(getToken(refreshTokenUrl(oldToken.getRefreshToken()))).build();
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

    /**
     * 获取token，适用于获取access_token和刷新token
     *
     * @param accessTokenUrl 实际请求token的地址
     * @return token对象
     */
    private AccToken getToken(String accessTokenUrl) {
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
            String openId = (String) dataObj.get("open_id");
            Object expiresInObj = dataObj.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;
            String refreshToken = (String) dataObj.get("refresh_token");
            Object refreshExpiresInObj = dataObj.get("refresh_expires_in");
            int refreshExpiresIn = refreshExpiresInObj instanceof Number ? ((Number) refreshExpiresInObj).intValue()
                    : 0;
            String scope = (String) dataObj.get("scope");

            return AccToken.builder().accessToken(accessToken).openId(openId).expireIn(expiresIn)
                    .refreshToken(refreshToken).refreshTokenExpireIn(refreshExpiresIn).scope(scope).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse token response: " + e.getMessage());
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
        return Builder.fromUrl(complex.authorize()).queryParam("response_type", "code")
                .queryParam("client_key", context.getAppKey()).queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("scope", this.getScopes(Symbol.COMMA, true, this.getDefaultScopes(DouyinScope.values())))
                .queryParam("state", getRealState(state)).build();
    }

    /**
     * 返回获取accessToken的url
     *
     * @param code oauth的授权码
     * @return 返回获取accessToken的url
     */
    @Override
    protected String accessTokenUrl(String code) {
        return Builder.fromUrl(complex.accessToken()).queryParam("code", code)
                .queryParam("client_key", context.getAppKey()).queryParam("client_secret", context.getAppSecret())
                .queryParam("grant_type", "authorization_code").build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param accToken oauth返回的token
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AccToken accToken) {
        return Builder.fromUrl(complex.userInfo()).queryParam("access_token", accToken.getAccessToken())
                .queryParam("open_id", accToken.getOpenId()).build();
    }

    /**
     * 返回获取accessToken的url
     *
     * @param refreshToken oauth返回的refreshtoken
     * @return 返回获取accessToken的url
     */
    @Override
    protected String refreshTokenUrl(String refreshToken) {
        return Builder.fromUrl(complex.refresh()).queryParam("client_key", context.getAppKey())
                .queryParam("refresh_token", refreshToken).queryParam("grant_type", "refresh_token").build();
    }

}