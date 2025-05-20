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
package org.miaixz.bus.oauth.metric.figma;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.HTTP;
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

/**
 * Figma 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FigmaProvider extends AbstractProvider {

    public FigmaProvider(Context context) {
        super(context, Registry.FIGMA);
    }

    public FigmaProvider(Context context, ExtendCache cache) {
        super(context, Registry.FIGMA, cache);
    }

    @Override
    public String authorize(String state) {
        return Builder.fromUrl(super.authorize(state))
                .queryParam("scope", this.getScopes(Symbol.COMMA, true, getDefaultScopes(FigmaScope.values()))).build();
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        Map<String, String> headers = new HashMap<>(3);
        headers.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        headers.put("Authorization",
                "Basic ".concat(Base64.encode((this.context.getAppKey().concat(":").concat(this.context.getAppSecret()))
                        .getBytes(StandardCharsets.UTF_8))));
        String response = Httpx.post(super.accessTokenUrl(callback.getCode()), headers);
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
            String refreshToken = (String) accessTokenObject.get("refresh_token");
            String scope = (String) accessTokenObject.get("scope");
            String userId = (String) accessTokenObject.get("user_id");
            Object expiresInObj = accessTokenObject.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;

            return AccToken.builder().accessToken(accessToken).refreshToken(refreshToken).scope(scope).userId(userId)
                    .expireIn(expiresIn).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
        }
    }

    @Override
    public Message refresh(AccToken accToken) {
        Map<String, String> headers = new HashMap<>(3);
        headers.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        String response = Httpx.post(refreshTokenUrl(accToken.getRefreshToken()), headers);
        try {
            Map<String, Object> dataObj = JsonKit.toPojo(response, Map.class);
            if (dataObj == null) {
                throw new AuthorizedException("Failed to parse refresh token response: empty response");
            }
            this.checkResponse(dataObj);

            String accessToken = (String) dataObj.get("access_token");
            if (accessToken == null) {
                throw new AuthorizedException("Missing access_token in refresh response");
            }
            String openId = (String) dataObj.get("open_id");
            Object expiresInObj = dataObj.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;
            String refreshToken = (String) dataObj.get("refresh_token");
            String scope = (String) dataObj.get("scope");

            return Message
                    .builder().errcode(ErrorCode.SUCCESS.getCode()).data(AccToken.builder().accessToken(accessToken)
                            .openId(openId).expireIn(expiresIn).refreshToken(refreshToken).scope(scope).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse refresh token response: " + e.getMessage());
        }
    }

    @Override
    protected String refreshTokenUrl(String refreshToken) {
        return Builder.fromUrl(complex.refresh()).queryParam("client_id", context.getAppKey())
                .queryParam("client_secret", context.getAppSecret()).queryParam("refresh_token", refreshToken).build();
    }

    @Override
    public Material getUserInfo(AccToken accToken) {
        Map<String, String> headers = new HashMap<>(3);
        headers.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        headers.put("Authorization", "Bearer " + accToken.getAccessToken());
        String response = Httpx.get(complex.userInfo(), null, headers);
        try {
            Map<String, Object> data = JsonKit.toPojo(response, Map.class);
            if (data == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }
            this.checkResponse(data);

            String id = (String) data.get("id");
            if (id == null) {
                throw new AuthorizedException("Missing id in user info response");
            }
            String handle = (String) data.get("handle");
            String imgUrl = (String) data.get("img_url");
            String email = (String) data.get("email");

            return Material.builder().rawJson(JsonKit.toJsonString(data)).uuid(id).username(handle).avatar(imgUrl)
                    .email(email).token(accToken).source(complex.toString()).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    /**
     * 校验响应结果
     *
     * @param object 接口返回的结果
     */
    private void checkResponse(Map<String, Object> object) {
        if (object.containsKey("error")) {
            String error = (String) object.get("error");
            String message = (String) object.get("message");
            throw new AuthorizedException(
                    (error != null ? error : "Unknown error") + ":" + (message != null ? message : "Unknown message"));
        }
    }

}