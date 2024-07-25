/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org justauth and other contributors.           ~
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
package org.miaixz.bus.oauth.metric.eleme;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.ErrorCode;
import org.miaixz.bus.oauth.magic.Material;
import org.miaixz.bus.oauth.metric.AbstractProvider;

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
    public static String sign(String appKey, String secret, long timestamp, String action, String token, Map<String, Object> parameters) {
        final Map<String, Object> sorted = new TreeMap<>(parameters);
        sorted.put("app_key", appKey);
        sorted.put("timestamp", timestamp);
        StringBuffer string = new StringBuffer();
        for (Map.Entry<String, Object> entry : sorted.entrySet()) {
            string.append(entry.getKey()).append(Symbol.EQUAL).append(JSON.toJSONString(entry.getValue()));
        }
        String splice = String.format("%s%s%s%s", action, token, string, secret);
        String calculatedSignature = org.miaixz.bus.crypto.Builder.md5Hex(splice);
        return calculatedSignature.toUpperCase();
    }

    @Override
    protected AccToken getAccessToken(Callback callback) {
        Map<String, String> form = new HashMap<>(7);
        form.put("client_id", context.getAppKey());
        form.put("redirect_uri", context.getRedirectUri());
        form.put("code", callback.getCode());
        form.put("grant_type", "authorization_code");

        Map<String, String> header = this.buildHeader(MediaType.APPLICATION_FORM_URLENCODED, this.getRequestId(), true);

        String response = Httpx.post(complex.accessToken(), form, header);
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
    public Message refresh(AccToken oldToken) {
        Map<String, String> form = new HashMap<>(4);
        form.put("refresh_token", oldToken.getRefreshToken());
        form.put("grant_type", "refresh_token");

        Map<String, String> header = this.buildHeader(MediaType.APPLICATION_FORM_URLENCODED, this.getRequestId(), true);
        String response = Httpx.post(complex.refresh(), form, header);

        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        return Message.builder()
                .errcode(ErrorCode.SUCCESS.getCode())
                .data(AccToken.builder()
                        .accessToken(object.getString("access_token"))
                        .refreshToken(object.getString("refresh_token"))
                        .tokenType(object.getString("token_type"))
                        .expireIn(object.getIntValue("expires_in"))
                        .build())
                .build();
    }

    @Override
    protected Material getUserInfo(AccToken accToken) {
        Map<String, Object> parameters = new HashMap<>(4);
        // 获取商户账号信息的API接口名称
        String action = "eleme.user.getUser";
        // 时间戳，单位秒。API服务端允许客户端请求最大时间误差为正负5分钟。
        final long timestamp = System.currentTimeMillis();
        // 公共参数
        Map<String, Object> metasHashMap = new HashMap<>(4);
        metasHashMap.put("app_key", context.getAppKey());
        metasHashMap.put("timestamp", timestamp);
        String signature = sign(context.getAppKey(), context.getAppSecret(), timestamp, action, accToken
                .getAccessToken(), parameters);

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
        String response = Httpx.post(complex.userInfo(), JSONObject.toJSONString(paramsMap), header, MediaType.APPLICATION_JSON);

        JSONObject object = JSONObject.parseObject(response);

        // 校验请求
        if (object.containsKey("name")) {
            throw new AuthorizedException(object.getString("message"));
        }
        if (object.containsKey("error") && null != object.get("error")) {
            throw new AuthorizedException(object.getJSONObject("error").getString("message"));
        }

        JSONObject result = object.getJSONObject("result");

        return Material.builder()
                .rawJson(result)
                .uuid(result.getString("userId"))
                .username(result.getString("userName"))
                .nickname(result.getString("userName"))
                .gender(Gender.UNKNOWN)
                .token(accToken)
                .source(complex.toString())
                .build();
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

    private void checkResponse(JSONObject object) {
        if (object.containsKey("error")) {
            throw new AuthorizedException(object.getString("error_description"));
        }
    }

    @Override
    public String authorize(String state) {
        return Builder.fromUrl(super.authorize(state)).queryParam("scope", "all").build();
    }

}
