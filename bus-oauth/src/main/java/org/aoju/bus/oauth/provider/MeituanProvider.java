/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
import org.aoju.bus.core.consts.Normal;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.http.HttpClient;
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
 * 美团登录
 *
 * @author Kimi Liu
 * @version 5.1.0
 * @since JDK 1.8+
 */
public class MeituanProvider extends DefaultProvider {

    public MeituanProvider(Context context) {
        super(context, Registry.MEITUAN);
    }

    public MeituanProvider(Context context, StateCache stateCache) {
        super(context, Registry.MEITUAN, stateCache);
    }

    @Override
    protected AccToken getAccessToken(Callback Callback) {
        Map<String, Object> params = new HashMap<>();
        params.put("app_id", context.getClientId());
        params.put("secret", context.getClientSecret());
        params.put("code", Callback.getCode());
        params.put("grant_type", "authorization_code");

        String response = HttpClient.post(source.accessToken(), params);
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        return AccToken.builder()
                .accessToken(object.getString("access_token"))
                .refreshToken(object.getString("refresh_token"))
                .expireIn(object.getIntValue("expires_in"))
                .build();
    }

    @Override
    protected Property getUserInfo(AccToken token) {
        Map<String, Object> params = new HashMap<>();
        params.put("app_id", context.getClientId());
        params.put("secret", context.getClientSecret());
        params.put("access_token", token.getAccessToken());

        String response = HttpClient.post(source.refresh(), params);
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        return Property.builder()
                .uuid(object.getString("openid"))
                .username(object.getString("nickname"))
                .nickname(object.getString("nickname"))
                .avatar(object.getString("avatar"))
                .gender(Normal.Gender.UNKNOWN)
                .token(token)
                .source(source.toString())
                .build();
    }

    @Override
    public Message refresh(AccToken oldToken) {
        Map<String, Object> params = new HashMap<>();
        params.put("app_id", context.getClientId());
        params.put("secret", context.getClientSecret());
        params.put("refresh_token", oldToken.getRefreshToken());
        params.put("grant_type", "refresh_token");

        String response = HttpClient.post(source.refresh(), params);
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        return Message.builder()
                .errcode(Builder.Status.SUCCESS.getCode())
                .data(AccToken.builder()
                        .accessToken(object.getString("access_token"))
                        .refreshToken(object.getString("refresh_token"))
                        .expireIn(object.getIntValue("expires_in"))
                        .build())
                .build();
    }

    private void checkResponse(JSONObject object) {
        if (object.containsKey("error_code")) {
            throw new InstrumentException(object.getString("erroe_msg"));
        }
    }

    @Override
    public String authorize(String state) {
        return Builder.fromBaseUrl(source.authorize())
                .queryParam("response_type", "code")
                .queryParam("app_id", context.getClientId())
                .queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("state", getRealState(state))
                .queryParam("scope", "")
                .build();
    }

}
