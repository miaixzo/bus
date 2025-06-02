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
package org.miaixz.bus.auth.nimble.eleme;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.HTTP;
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
import java.util.TreeMap;

/**
 * 饿了么 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ElemeProvider extends AbstractProvider {

    public ElemeProvider(Context context) {
        super(context, Registry.ELEME);
    }

    public ElemeProvider(Context context, ExtendCache cache) {
        super(context, Registry.ELEME, cache);
    }

    /**
     * 生成饿了么请求的签名
     *
     * @param appKey     平台应用的授权key
     * @param secret     平台应用的授权密钥
     * @param timestamp  时间戳，单位秒。API服务端允许客户端请求最大时间误差为正负5分钟。
     * @param action     饿了么请求的api方法
     * @param token      用户授权的token
     * @param parameters 加密参数
     * @return Signature
     */
    public static String sign(String appKey, String secret, long timestamp, String action, String token,
            Map<String, Object> parameters) {
        final Map<String, Object> sorted = new TreeMap<>(parameters);
        sorted.put("app_key", appKey);
        sorted.put("timestamp", timestamp);
        StringBuffer string = new StringBuffer();
        for (Map.Entry<String, Object> entry : sorted.entrySet()) {
            string.append(entry.getKey()).append(Symbol.EQUAL).append(JsonKit.toJsonString(entry.getValue()));
        }
        String splice = String.format("%s%s%s%s", action, token, string, secret);
        String calculatedSignature = org.miaixz.bus.crypto.Builder.md5Hex(splice);
        return calculatedSignature.toUpperCase();
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        Map<String, String> form = new HashMap<>(7);
        form.put("client_id", context.getAppKey());
        form.put("redirect_uri", context.getRedirectUri());
        form.put("code", callback.getCode());
        form.put("grant_type", "authorization_code");

        Map<String, String> header = this.buildHeader(MediaType.APPLICATION_FORM_URLENCODED, this.getRequestId(), true);

        String response = Httpx.post(this.complex.getConfig().get(Builder.ACCESSTOKEN), form, header);
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse access token response: empty response");
            }

            this.checkResponse(object);

            String accessToken = (String) object.get("access_token");
            if (accessToken == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            String refreshToken = (String) object.get("refresh_token");
            String tokenType = (String) object.get("token_type");
            Object expiresInObj = object.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;

            return AccToken.builder().accessToken(accessToken).refreshToken(refreshToken).tokenType(tokenType)
                    .expireIn(expiresIn).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
        }
    }

    @Override
    public Message refresh(AccToken oldToken) {
        Map<String, String> form = new HashMap<>(4);
        form.put("refresh_token", oldToken.getRefreshToken());
        form.put("grant_type", "refresh_token");

        Map<String, String> header = this.buildHeader(MediaType.APPLICATION_FORM_URLENCODED, this.getRequestId(), true);
        String response = Httpx.post(this.complex.getConfig().get(Builder.REFRESH), form, header);

        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse refresh token response: empty response");
            }

            this.checkResponse(object);

            String accessToken = (String) object.get("access_token");
            if (accessToken == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            String refreshToken = (String) object.get("refresh_token");
            String tokenType = (String) object.get("token_type");
            Object expiresInObj = object.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;

            return Message.builder().errcode(ErrorCode.SUCCESS.getCode())
                    .data(AccToken.builder().accessToken(accessToken).refreshToken(refreshToken).tokenType(tokenType)
                            .expireIn(expiresIn).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse refresh token response: " + e.getMessage());
        }
    }

    @Override
    public Material getUserInfo(AccToken accToken) {
        Map<String, Object> parameters = new HashMap<>(4);
        // 获取商户账号信息的API接口名称
        String action = "eleme.user.getUser";
        // 时间戳，单位秒。API服务端允许客户端请求最大时间误差为正负5分钟。
        final long timestamp = System.currentTimeMillis();
        // 公共参数
        Map<String, Object> metasHashMap = new HashMap<>(4);
        metasHashMap.put("app_key", context.getAppKey());
        metasHashMap.put("timestamp", timestamp);
        String signature = sign(context.getAppKey(), context.getAppSecret(), timestamp, action,
                accToken.getAccessToken(), parameters);

        String requestId = this.getRequestId();

        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("nop", "1.0.0");
        paramsMap.put("id", requestId);
        paramsMap.put("action", action);
        paramsMap.put("token", accToken.getAccessToken());
        paramsMap.put("metas", metasHashMap);
        paramsMap.put("params", parameters);
        paramsMap.put("signature", signature);

        Map<String, String> header = this.buildHeader(MediaType.APPLICATION_JSON, requestId, false);
        String response = Httpx.post(this.complex.getConfig().get(Builder.USERINFO), JsonKit.toJsonString(paramsMap),
                header, MediaType.APPLICATION_JSON);

        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }

            // 校验请求
            if (object.containsKey("name")) {
                String message = (String) object.get("message");
                throw new AuthorizedException(message != null ? message : "Unknown error");
            }
            if (object.containsKey("error") && object.get("error") != null) {
                Map<String, Object> error = (Map<String, Object>) object.get("error");
                String errorMessage = (String) error.get("message");
                throw new AuthorizedException(errorMessage != null ? errorMessage : "Unknown error");
            }

            Map<String, Object> result = (Map<String, Object>) object.get("result");
            if (result == null) {
                throw new AuthorizedException("Missing result field in user info response");
            }

            String userId = (String) result.get("userId");
            if (userId == null) {
                throw new AuthorizedException("Missing userId in user info response");
            }
            String userName = (String) result.get("userName");

            return Material.builder().rawJson(JsonKit.toJsonString(result)).uuid(userId).username(userName)
                    .nickname(userName).gender(Gender.UNKNOWN).token(accToken).source(complex.toString()).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    private String getBasic(String appKey, String appSecret) {
        StringBuilder sb = new StringBuilder();
        String encodeToString = Base64.encode((appKey + Symbol.COLON + appSecret).getBytes());
        sb.append("Basic").append(Symbol.SPACE).append(encodeToString);
        return sb.toString();
    }

    private Map<String, String> buildHeader(String contentType, String requestId, boolean auth) {
        Map<String, String> header = new HashMap<>();
        header.put(HTTP.ACCEPT, "text/xml,text/javascript,text/html");
        header.put(HTTP.CONTENT_TYPE, contentType);
        header.put(HTTP.ACCEPT_ENCODING, "gzip");
        header.put(HTTP.USER_AGENT, "eleme-openapi-java-sdk");
        header.put("x-eleme-requestid", requestId);

        if (auth) {
            header.put("Authorization", this.getBasic(context.getAppKey(), context.getAppSecret()));
        }
        return header;
    }

    private String getRequestId() {
        return (ID.objectId() + "|" + System.currentTimeMillis()).toUpperCase();
    }

    private void checkResponse(Map<String, Object> object) {
        if (object.containsKey("error")) {
            String errorDescription = (String) object.get("error_description");
            throw new AuthorizedException(errorDescription != null ? errorDescription : "Unknown error");
        }
    }

    @Override
    public String authorize(String state) {
        return Builder.fromUrl(super.authorize(state)).queryParam("scope", "all").build();
    }

}