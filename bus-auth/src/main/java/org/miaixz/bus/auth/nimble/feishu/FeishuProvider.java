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
package org.miaixz.bus.auth.nimble.feishu;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.AccToken;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Material;
import org.miaixz.bus.auth.nimble.AbstractProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * 飞书 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FeishuProvider extends AbstractProvider {

    public FeishuProvider(Context context) {
        super(context, Registry.FEISHU);
    }

    public FeishuProvider(Context context, ExtendCache cache) {
        super(context, Registry.FEISHU, cache);
    }

    /**
     * 获取 app_access_token（企业自建应用）
     * <p>
     * Token 有效期为 2 小时，在此期间调用该接口 token 不会改变。当 token 有效期小于 30 分的时候，再次请求获取 token 的时候， 会生成一个新的 token，与此同时老的 token 依然有效。
     *
     * @return appAccessToken
     */
    private String getAppAccessToken() {
        String cacheKey = this.complex.getName().concat(":app_access_token:").concat(context.getAppKey());
        String cacheAppAccessToken = String.valueOf(this.cache.get(cacheKey));
        if (StringKit.isNotEmpty(cacheAppAccessToken)) {
            return cacheAppAccessToken;
        }
        String url = "https://open.feishu.cn/open-apis/auth/v3/app_access_token/internal/";
        Map<String, Object> requestObject = new HashMap<>();
        requestObject.put("app_id", context.getAppKey());
        requestObject.put("app_secret", context.getAppSecret());

        Map<String, String> header = new HashMap<>();
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON);

        String response = Httpx.post(url, JsonKit.toJsonString(requestObject), header, MediaType.APPLICATION_JSON);
        Map<String, Object> jsonObject = JsonKit.toPojo(response, Map.class);
        if (jsonObject == null) {
            throw new AuthorizedException("Failed to parse app access token response: empty response");
        }
        this.checkResponse(jsonObject);
        String appAccessToken = (String) jsonObject.get("app_access_token");
        if (appAccessToken == null) {
            throw new AuthorizedException("Missing app_access_token in response");
        }
        Object expireObj = jsonObject.get("expire");
        long expire = expireObj instanceof Number ? ((Number) expireObj).longValue() : 0;
        // 缓存 app access token
        this.cache.cache(cacheKey, appAccessToken, expire * 1000);
        return appAccessToken;
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        Map<String, Object> requestObject = new HashMap<>();
        requestObject.put("app_access_token", this.getAppAccessToken());
        requestObject.put("grant_type", "authorization_code");
        requestObject.put("code", callback.getCode());
        return getToken(requestObject, this.complex.getConfig().get(Builder.ACCESSTOKEN));
    }

    @Override
    public Material getUserInfo(AccToken accToken) {
        String accessToken = accToken.getAccessToken();
        Map<String, String> header = new HashMap<>();
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        header.put("Authorization", "Bearer " + accessToken);
        String response = Httpx.get(this.complex.getConfig().get(Builder.USERINFO), null, header);
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }
            this.checkResponse(object);
            Map<String, Object> data = (Map<String, Object>) object.get("data");
            if (data == null) {
                throw new AuthorizedException("Missing data in user info response");
            }

            String unionId = (String) data.get("union_id");
            String name = (String) data.get("name");
            String avatarUrl = (String) data.get("avatar_url");
            String email = (String) data.get("email");

            return Material.builder().rawJson(JsonKit.toJsonString(object)).uuid(unionId).username(name).nickname(name)
                    .avatar(avatarUrl).email(email).gender(Gender.UNKNOWN).token(accToken).source(complex.toString())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    @Override
    public Message refresh(AccToken accToken) {
        Map<String, Object> requestObject = new HashMap<>();
        requestObject.put("app_access_token", this.getAppAccessToken());
        requestObject.put("grant_type", "refresh_token");
        requestObject.put("refresh_token", accToken.getRefreshToken());
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(getToken(requestObject, this.complex.getConfig().get(Builder.REFRESH))).build();
    }

    private AccToken getToken(Map<String, Object> param, String url) {
        Map<String, String> header = new HashMap<>();
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        String response = Httpx.post(url, JsonKit.toJsonString(param), header, MediaType.APPLICATION_JSON);
        try {
            Map<String, Object> jsonObject = JsonKit.toPojo(response, Map.class);
            if (jsonObject == null) {
                throw new AuthorizedException("Failed to parse token response: empty response");
            }
            this.checkResponse(jsonObject);
            Map<String, Object> data = (Map<String, Object>) jsonObject.get("data");
            if (data == null) {
                throw new AuthorizedException("Missing data in token response");
            }

            String accessToken = (String) data.get("access_token");
            if (accessToken == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            String refreshToken = (String) data.get("refresh_token");
            Object expiresInObj = data.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;
            String tokenType = (String) data.get("token_type");
            String openId = (String) data.get("open_id");

            return AccToken.builder().accessToken(accessToken).refreshToken(refreshToken).expireIn(expiresIn)
                    .tokenType(tokenType).openId(openId).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse token response: " + e.getMessage());
        }
    }

    @Override
    public String authorize(String state) {
        return Builder.fromUrl(complex.getConfig().get(Builder.AUTHORIZE)).queryParam("app_id", context.getAppKey())
                .queryParam("redirect_uri", UrlEncoder.encodeAll(context.getRedirectUri()))
                .queryParam("state", getRealState(state)).build();
    }

    /**
     * 校验响应内容是否正确
     *
     * @param jsonObject 响应内容
     */
    private void checkResponse(Map<String, Object> jsonObject) {
        Object codeObj = jsonObject.get("code");
        int code = codeObj instanceof Number ? ((Number) codeObj).intValue() : -1;
        if (code != 0) {
            String message = (String) jsonObject.get("message");
            throw new AuthorizedException(message != null ? message : "Unknown error");
        }
    }

}