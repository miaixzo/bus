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
package org.miaixz.bus.oauth.metric.teambition;

import com.alibaba.fastjson.JSONObject;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.exception.AuthorizedException;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.*;
import org.miaixz.bus.oauth.metric.DefaultProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Teambition 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TeambitionProvider extends DefaultProvider {

    public TeambitionProvider(Context context) {
        super(context, Registry.TEAMBITION);
    }

    public TeambitionProvider(Context context, ExtendCache authorizeCache) {
        super(context, Registry.TEAMBITION, authorizeCache);
    }

    /**
     * @param authCallback 回调返回的参数
     * @return 所有信息
     */
    @Override
    protected AccToken getAccessToken(Callback authCallback) {
        Map<String, Object> form = new HashMap<>(7);
        form.put("client_id", context.getAppKey());
        form.put("client_secret", context.getAppSecret());
        form.put("code", authCallback.getCode());
        form.put("grant_type", "code");

        String response = Httpx.post(complex.accessToken(), form);
        JSONObject accessTokenObject = JSONObject.parseObject(response);

        this.checkResponse(accessTokenObject);

        return AccToken.builder()
                .accessToken(accessTokenObject.getString("access_token"))
                .refreshToken(accessTokenObject.getString("refresh_token"))
                .build();
    }

    @Override
    protected Property getUserInfo(AccToken accToken) {
        String accessToken = accToken.getAccessToken();
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", "OAuth2 " + accessToken);

        String response = Httpx.get(complex.userInfo(), null, header);
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        accToken.setUid(object.getString("_id"));

        return Property.builder()
                .rawJson(object)
                .uuid(object.getString("_id"))
                .username(object.getString("name"))
                .nickname(object.getString("name"))
                .avatar(object.getString("avatarUrl"))
                .blog(object.getString("website"))
                .location(object.getString("location"))
                .email(object.getString("email"))
                .gender(Gender.UNKNOWN)
                .token(accToken)
                .source(complex.toString())
                .build();
    }

    @Override
    public Message refresh(AccToken oldToken) {
        String uid = oldToken.getUid();
        String refreshToken = oldToken.getRefreshToken();

        Map<String, Object> form = new HashMap<>(4);
        form.put("_userId", uid);
        form.put("refresh_token", refreshToken);
        String response = Httpx.post(complex.refresh(), form);
        JSONObject refreshTokenObject = JSONObject.parseObject(response);

        this.checkResponse(refreshTokenObject);

        return Message.builder()
                .errcode(ErrorCode.SUCCESS.getCode())
                .data(AccToken.builder()
                        .accessToken(refreshTokenObject.getString("access_token"))
                        .refreshToken(refreshTokenObject.getString("refresh_token"))
                        .build())
                .build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(JSONObject object) {
        if ((object.containsKey("message") && object.containsKey("name"))) {
            throw new AuthorizedException(object.getString("name") + ", " + object.getString("message"));
        }
    }

}
