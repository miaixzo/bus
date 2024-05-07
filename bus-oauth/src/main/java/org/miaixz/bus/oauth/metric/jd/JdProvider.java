/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org justauth and other contributors.           *
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
package org.miaixz.bus.oauth.metric.jd;

import com.alibaba.fastjson.JSONObject;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.exception.AuthorizedException;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.*;
import org.miaixz.bus.oauth.metric.DefaultProvider;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 京东 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JdProvider extends DefaultProvider {

    public JdProvider(Context context) {
        super(context, Registry.JD);
    }

    public JdProvider(Context context, ExtendCache authorizeCache) {
        super(context, Registry.JD, authorizeCache);
    }

    @Override
    protected AccToken getAccessToken(Callback authCallback) {
        Map<String, Object> form = new HashMap<>(7);
        form.put("app_key", context.getAppKey());
        form.put("app_secret", context.getAppSecret());
        form.put("grant_type", "authorization_code");
        form.put("code", authCallback.getCode());
        String response = Httpx.post(complex.accessToken(), form);
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        return AccToken.builder()
                .accessToken(object.getString("access_token"))
                .expireIn(object.getIntValue("expires_in"))
                .refreshToken(object.getString("refresh_token"))
                .scope(object.getString("scope"))
                .openId(object.getString("open_id"))
                .build();
    }

    @Override
    protected Property getUserInfo(AccToken accToken) {
        Builder urlBuilder = Builder.fromBaseUrl(complex.userInfo())
                .queryParam("access_token", accToken.getAccessToken())
                .queryParam("app_key", context.getAppKey())
                .queryParam("method", "jingdong.user.getUserInfoByOpenId")
                .queryParam("360buy_param_json", "{\"openId\":\"" + accToken.getOpenId() + "\"}")
                .queryParam("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .queryParam("v", "2.0");
        urlBuilder.queryParam("sign", Builder.generateJdSignature(context.getAppSecret(), urlBuilder.getReadOnlyParams()));
        String response = Httpx.post(urlBuilder.build(true));
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        JSONObject data = this.getUserDataJsonObject(object);

        return Property.builder()
                .rawJson(data)
                .uuid(accToken.getOpenId())
                .username(data.getString("nickName"))
                .nickname(data.getString("nickName"))
                .avatar(data.getString("imageUrl"))
                .gender(Gender.of(data.getString("gendar")))
                .token(accToken)
                .source(complex.toString())
                .build();
    }

    /**
     * 个人用户无法申请应用
     * 暂时只能参考官网给出的返回结果解析
     *
     * @param object 请求返回结果
     * @return data JSONObject
     */
    private JSONObject getUserDataJsonObject(JSONObject object) {
        return object.getJSONObject("jingdong_user_getUserInfoByOpenId_response")
                .getJSONObject("getuserinfobyappidandopenid_result")
                .getJSONObject("data");
    }

    @Override
    public Message refresh(AccToken oldToken) {
        Map<String, Object> form = new HashMap<>(7);
        form.put("app_key", context.getAppKey());
        form.put("app_secret", context.getAppSecret());
        form.put("grant_type", "refresh_token");
        form.put("refresh_token", oldToken.getRefreshToken());
        String response = Httpx.post(complex.refresh(), form);
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        return Message.builder()
                .errcode(ErrorCode.SUCCESS.getCode())
                .data(AccToken.builder()
                        .accessToken(object.getString("access_token"))
                        .expireIn(object.getIntValue("expires_in"))
                        .refreshToken(object.getString("refresh_token"))
                        .scope(object.getString("scope"))
                        .openId(object.getString("open_id"))
                        .build())
                .build();
    }

    private void checkResponse(JSONObject object) {
        if (object.containsKey("error_response")) {
            throw new AuthorizedException(object.getJSONObject("error_response").getString("zh_desc"));
        }
    }

    @Override
    public String authorize(String state) {
        return Builder.fromBaseUrl(complex.authorize())
                .queryParam("app_key", context.getAppKey())
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("scope", this.getScopes(" ", true, this.getDefaultScopes(JdScope.values())))
                .queryParam("state", getRealState(state))
                .build();
    }

}
