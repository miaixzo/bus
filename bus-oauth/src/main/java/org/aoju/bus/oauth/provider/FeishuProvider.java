/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.oauth.provider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.cache.metric.ExtendCache;
import org.aoju.bus.core.lang.MediaType;
import org.aoju.bus.core.lang.exception.AuthorizedException;
import org.aoju.bus.core.toolkit.UriKit;
import org.aoju.bus.http.Httpx;
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Message;
import org.aoju.bus.oauth.magic.Property;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝登录
 *
 * @author Kimi Liu
 * @version 6.0.0
 * @since JDK 1.8+
 */
public class FeishuProvider extends AbstractProvider {

    public FeishuProvider(Context context) {
        super(context, Registry.FEISHU);
    }

    public FeishuProvider(Context context, ExtendCache extendCache) {
        super(context, Registry.FEISHU, extendCache);
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        JSONObject requestObject = new JSONObject();
        requestObject.put("app_id", context.getAppKey());
        requestObject.put("app_secret", context.getAppSecret());
        requestObject.put("grant_type", "authorization_code");
        requestObject.put("code", callback.getCode());
        String response = Httpx.post(source.accessToken(), requestObject.toJSONString(), MediaType.APPLICATION_JSON);
        JSONObject jsonObject = JSON.parseObject(response);
        this.checkResponse(jsonObject);
        return AccToken.builder()
                .accessToken(jsonObject.getString("access_token"))
                .refreshToken(jsonObject.getString("refresh_token"))
                .expireIn(jsonObject.getIntValue("expires_in"))
                .tokenType(jsonObject.getString("token_type"))
                .openId(jsonObject.getString("open_id"))
                .build();

    }

    @Override
    public Property getUserInfo(AccToken authToken) {
        String accessToken = authToken.getAccessToken();

        Map<String, String> map = new HashMap<>();
        map.put("Content-Type", MediaType.APPLICATION_JSON);
        map.put("Authorization", "Bearer " + accessToken);
        String response = Httpx.get(source.userInfo(), null, map);
        JSONObject object = JSON.parseObject(response);
        return Property.builder()
                .rawJson(object)
                .avatar(object.getString("AvatarUrl"))
                .username(object.getString("Mobile"))
                .email(object.getString("Email"))
                .nickname("Name")
                .build();
    }

    @Override
    public Message refresh(AccToken authToken) {
        JSONObject requestObject = new JSONObject();
        requestObject.put("app_id", context.getAppKey());
        requestObject.put("app_secret", context.getAppSecret());
        requestObject.put("grant_type", "refresh_token");
        requestObject.put("refresh_token", authToken.getRefreshToken());

        String response = Httpx.post(source.refresh(), requestObject.toJSONString(), MediaType.APPLICATION_JSON);
        JSONObject jsonObject = JSON.parseObject(response);
        this.checkResponse(jsonObject);
        return Message.builder()
                .errcode(Builder.ErrorCode.SUCCESS.getCode())
                .data(AccToken.builder()
                        .accessToken(jsonObject.getString("access_token"))
                        .refreshToken(jsonObject.getString("refresh_token"))
                        .expireIn(jsonObject.getIntValue("expires_in"))
                        .tokenType(jsonObject.getString("token_type"))
                        .openId(jsonObject.getString("open_id"))
                        .build())
                .build();

    }

    @Override
    public String authorize(String state) {
        return Builder.fromUrl(source.authorize())
                .queryParam("app_id", context.getAppKey())
                .queryParam("redirect_uri", UriKit.encode(context.getRedirectUri()))
                .queryParam("state", getRealState(state))
                .build();
    }

    /**
     * 校验响应内容是否正确
     *
     * @param jsonObject 响应内容
     */
    private void checkResponse(JSONObject jsonObject) {
        if (jsonObject.getIntValue("code") != 0) {
            throw new AuthorizedException(jsonObject.getString("message"));
        }
    }

}
