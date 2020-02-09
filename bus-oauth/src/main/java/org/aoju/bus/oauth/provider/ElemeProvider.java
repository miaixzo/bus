/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.oauth.provider;

import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.core.codec.Base64;
import org.aoju.bus.core.key.ObjectID;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.http.Httpx;
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Message;
import org.aoju.bus.oauth.magic.Property;
import org.aoju.bus.oauth.metric.StateCache;

import java.util.HashMap;
import java.util.Map;

/**
 * 饿了么
 * <p>
 * 注：集成的是正式环境,非沙箱环境
 *
 * @author Kimi Liu
 * @version 5.5.6
 * @since JDK 1.8+
 */
public class ElemeProvider extends DefaultProvider {

    public ElemeProvider(Context context) {
        super(context, Registry.ELEME);
    }

    public ElemeProvider(Context context, StateCache stateCache) {
        super(context, Registry.ELEME, stateCache);
    }

    @Override
    protected AccToken getAccessToken(Callback Callback) {
        Map<String, String> header = new HashMap<>();
        header.put("client_id", context.getClientId());
        header.put("redirect_uri", context.getRedirectUri());
        header.put("code", Callback.getCode());
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
    protected Property getUserInfo(AccToken token) {
        Map<String, Object> parameters = new HashMap<>();
        // 获取商户账号信息的API接口名称
        String action = "eleme.user.getUser";
        // 时间戳,单位秒 API服务端允许客户端请求最大时间误差为正负5分钟
        final long timestamp = System.currentTimeMillis();
        // 公共参数
        Map<String, String> metasHashMap = new HashMap<>();
        metasHashMap.put("app_key", context.getClientId());
        metasHashMap.put("timestamp", Normal.EMPTY + timestamp);
        String signature = generateElemeSignature(context.getClientId(), context.getClientSecret(), timestamp, action, token.getAccessToken(), parameters);

        String requestId = this.getRequestId();

        Map<String, String> header = new HashMap<>();
        header.put("nop", "1.0.0");
        header.put("id", requestId);
        header.put("action", action);
        header.put("token", token.getAccessToken());
        header.put("metas", metasHashMap.toString());
        header.put("params", parameters.toString());
        header.put("signature", signature);

        // 设置header
        this.setHeader(header, "application/json; charset=utf-8", requestId);

        String response = Httpx.post(source.userInfo(), null, header);
        JSONObject object = JSONObject.parseObject(response);

        // 校验请求
        if (object.containsKey("name")) {
            throw new InstrumentException(object.getString("message"));
        }
        if (object.containsKey("error") && null != object.get("error")) {
            throw new InstrumentException(object.getJSONObject("error").getString("message"));
        }

        JSONObject result = object.getJSONObject("result");

        return Property.builder()
                .uuid(result.getString("userId"))
                .username(result.getString("userName"))
                .nickname(result.getString("userName"))
                .gender(Normal.Gender.UNKNOWN)
                .token(token)
                .source(source.toString())
                .build();
    }

    @Override
    public Message refresh(AccToken oldToken) {
        Map<String, String> header = new HashMap<>();
        header.put("refresh_token", oldToken.getRefreshToken());
        header.put("grant_type", "refresh_token");
        this.setHeader(header);
        String response = Httpx.post(source.refresh(), null, header);
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        return Message.builder()
                .errcode(Builder.Status.SUCCESS.getCode())
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
        return Builder.fromBaseUrl(super.authorize(state))
                .queryParam("scope", "all")
                .build();
    }

    private String getBasic(String appKey, String appSecret) {
        StringBuilder sb = new StringBuilder();
        String encodeToString = Base64.encode((appKey + Symbol.COLON + appSecret).getBytes());
        sb.append("Basic").append(Symbol.SPACE).append(encodeToString);
        return sb.toString();
    }

    private void setHeader(Map<String, String> header) {
        setHeader(header, "application/x-www-form-urlencoded;charset=UTF-8", getRequestId());
        header.put("Authorization", this.getBasic(context.getClientId(), context.getClientSecret()));
    }

    private void setHeader(Map<String, String> header, String contentType, String requestId) {
        header.put("Accept", "text/xml,text/javascript,text/html");
        header.put("Content-Type", contentType);
        header.put("Accept-Encoding", "gzip");
        header.put("User-Agent", "eleme-openapi-java-sdk");
        header.put("x-eleme-requestid", requestId);
    }

    private String getRequestId() {
        return (ObjectID.id() + Symbol.OR + System.currentTimeMillis()).toUpperCase();
    }

    private void checkResponse(JSONObject object) {
        if (object.containsKey("error")) {
            throw new InstrumentException(object.getString("error_description"));
        }
    }

}
