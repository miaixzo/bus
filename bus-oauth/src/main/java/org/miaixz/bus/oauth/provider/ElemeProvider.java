/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.oauth.provider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.exception.AuthorizedException;
import org.miaixz.bus.core.key.ObjectID;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.Message;
import org.miaixz.bus.oauth.magic.Property;

import java.lang.System;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 饿了么
 * <p>
 * 注：集成的是正式环境,非沙箱环境
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ElemeProvider extends AbstractProvider {

    public ElemeProvider(Context context) {
        super(context, Registry.ELEME);
    }

    public ElemeProvider(Context context, ExtendCache extendCache) {
        super(context, Registry.ELEME, extendCache);
    }

    /**
     * 生成饿了么请求的Signature
     * <p>
     * 代码copy并修改自：https://coding.net/u/napos_openapi/p/eleme-openapi-java-sdk/git/blob/master/src/main/java/eleme/openapi/sdk/utils/SignatureUtil.java
     *
     * @param appKey     平台应用的授权key
     * @param secret     平台应用的授权密钥
     * @param timestamp  时间戳,单位秒 API服务端允许客户端请求最大时间误差为正负5分钟
     * @param action     饿了么请求的api方法
     * @param token      用户授权的token
     * @param parameters 加密参数
     * @return Signature
     */
    private static String signature(String appKey, String secret, long timestamp, String action, String token, Map<String, Object> parameters) {
        final Map<String, Object> sorted = new TreeMap<>();
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            sorted.put(entry.getKey(), entry.getValue());
        }
        sorted.put("app_key", appKey);
        sorted.put("timestamp", timestamp);
        StringBuffer string = new StringBuffer();
        for (Map.Entry<String, Object> entry : sorted.entrySet()) {
            string.append(entry.getKey()).append(Symbol.EQUAL).append(JSON.toJSONString(entry.getValue()));
        }
        String splice = String.format("%s%s%s%s", action, token, string, secret);
        String calculatedSignature = md5(splice);
        return calculatedSignature.toUpperCase();
    }

    /**
     * MD5加密饿了么请求的Signature
     * <p>
     * 代码copy并修改自：https://coding.net/u/napos_openapi/p/eleme-openapi-java-sdk/git/blob/master/src/main/java/eleme/openapi/sdk/utils/SignatureUtil.java
     *
     * @param text 饿了么请求的Signature
     * @return the string
     */
    private static String md5(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance(Algorithm.MD5.getValue());
            md.update(text.getBytes(Charset.UTF_8));
            byte[] byteData = md.digest();
            StringBuilder buffer = new StringBuilder();
            for (byte byteDatum : byteData) {
                buffer.append(Integer.toString((byteDatum & 0xff) + 0x100, Normal._16).substring(1));
            }
            return null == buffer ? Normal.EMPTY : buffer.toString();
        } catch (Exception ignored) {
            throw new AuthorizedException(ignored.getMessage());
        }
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        Map<String, String> header = new HashMap<>();
        header.put("client_id", context.getAppKey());
        header.put("redirect_uri", context.getRedirectUri());
        header.put("code", callback.getCode());
        header.put("grant_type", "authorization_code");

        // 设置header
        this.setHeader(header);

        String response = Httpx.post(source.accessToken(), null, header);
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        return AccToken.builder()
                .accessToken(object.getString("access_token"))
                .refreshToken(object.getString("refresh_token"))
                .tokenType(object.getString("token_type"))
                .expireIn(object.getIntValue("expires_in"))
                .build();
    }

    @Override
    public Message refresh(AccToken accToken) {
        Map<String, String> header = new HashMap<>();
        header.put("refresh_token", accToken.getRefreshToken());
        header.put("grant_type", "refresh_token");
        this.setHeader(header);
        String response = Httpx.post(source.refresh(), null, header);
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        return Message.builder()
                .errcode(Builder.ErrorCode.SUCCESS.getCode())
                .data(AccToken.builder()
                        .accessToken(object.getString("access_token"))
                        .refreshToken(object.getString("refresh_token"))
                        .tokenType(object.getString("token_type"))
                        .expireIn(object.getIntValue("expires_in"))
                        .build())
                .build();
    }

    @Override
    public String authorize(String state) {
        return Builder.fromUrl(super.authorize(state))
                .queryParam("scope", "all")
                .build();
    }

    @Override
    public Property getUserInfo(AccToken accToken) {
        Map<String, Object> parameters = new HashMap<>();
        // 获取商户账号信息的API接口名称
        String action = "eleme.user.getUser";
        // 时间戳,单位秒 API服务端允许客户端请求最大时间误差为正负5分钟
        final long timestamp = System.currentTimeMillis();
        // 公共参数
        Map<String, String> metasHashMap = new HashMap<>();
        metasHashMap.put("app_key", context.getAppKey());
        metasHashMap.put("timestamp", Normal.EMPTY + timestamp);
        String signature = signature(context.getAppKey(), context.getAppSecret(), timestamp, action, accToken.getAccessToken(), parameters);

        String requestId = this.getRequestId();

        Map<String, String> header = new HashMap<>();
        header.put("nop", "1.0.0");
        header.put("id", requestId);
        header.put("action", action);
        header.put("token", accToken.getAccessToken());
        header.put("metas", metasHashMap.toString());
        header.put("params", parameters.toString());
        header.put("signature", signature);

        // 设置header
        this.setHeader(header, "application/json; charset=utf-8", requestId);

        String response = Httpx.post(source.userInfo(), null, header);
        JSONObject jsonObject = JSONObject.parseObject(response);

        // 校验请求
        if (jsonObject.containsKey("name")) {
            throw new AuthorizedException(jsonObject.getString("message"));
        }
        if (jsonObject.containsKey("error") && null != jsonObject.get("error")) {
            throw new AuthorizedException(jsonObject.getJSONObject("error").getString("message"));
        }

        JSONObject object = jsonObject.getJSONObject("result");

        return Property.builder()
                .rawJson(object)
                .uuid(object.getString("userId"))
                .username(object.getString("userName"))
                .nickname(object.getString("userName"))
                .gender(Normal.Gender.UNKNOWN)
                .token(accToken)
                .source(source.toString())
                .build();
    }

    private String getBasic(String appKey, String appSecret) {
        StringBuilder sb = new StringBuilder();
        String encodeToString = Base64.encode((appKey + Symbol.COLON + appSecret).getBytes());
        sb.append("Basic").append(Symbol.SPACE).append(encodeToString);
        return sb.toString();
    }

    private void setHeader(Map<String, String> header) {
        setHeader(header, MediaType.APPLICATION_FORM_URLENCODED + ";charset=UTF-8", getRequestId());
        header.put(Header.AUTHORIZATION, this.getBasic(context.getAppKey(), context.getAppSecret()));
    }

    private void setHeader(Map<String, String> header, String contentType, String requestId) {
        header.put(Header.ACCEPT, "text/xml,text/javascript,text/html");
        header.put(Header.CONTENT_TYPE, contentType);
        header.put(Header.ACCEPT_ENCODING, "gzip");
        header.put(Header.USER_AGENT, "eleme-openapi-java-sdk");
        header.put("x-eleme-requestid", requestId);
    }

    private String getRequestId() {
        return (ObjectID.id() + Symbol.OR + System.currentTimeMillis()).toUpperCase();
    }

    private void checkResponse(JSONObject object) {
        if (object.containsKey("error")) {
            throw new AuthorizedException(object.getString("error_description"));
        }
    }

}
