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
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.http.Httpx;
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Message;
import org.aoju.bus.oauth.magic.Property;
import org.aoju.bus.oauth.metric.StateCache;

/**
 * 酷家乐授权登录
 *
 * @author Kimi Liu
 * @version 5.5.5
 * @since JDK 1.8+
 */
public class KujialeProvider extends DefaultProvider {

    public KujialeProvider(Context context) {
        super(context, Registry.KUJIALE);
    }

    public KujialeProvider(Context context, StateCache stateCache) {
        super(context, Registry.KUJIALE, stateCache);
    }

    /**
     * 返回带{@code state}参数的授权url,授权回调时会带上这个{@code state}
     * 默认只向用户请求用户信息授权
     *
     * @param state state 验证授权流程的参数,可以防止csrf
     * @return 返回授权地址
     * @since 2.0.1
     */
    @Override
    public String authorize(String state) {
        return authorize(state, "get_user_info");
    }

    /**
     * 请求授权url
     *
     * @param state    state 验证授权流程的参数,可以防止csrf
     * @param scopeStr 请求用户授权时向用户显示的可进行授权的列表 如果要填写多个接口名称,请用逗号隔开
     *                 参考https://open.kujiale.com/open/apps/2/docs?doc_id=95#Step1%EF%BC%9A%E8%8E%B7%E5%8F%96Authorization%20Code参数表内的scope字段
     * @return authorize url
     */
    public String authorize(String state, String scopeStr) {
        Builder builder = Builder.fromBaseUrl(source.authorize())
                .queryParam("response_type", "code")
                .queryParam("client_id", context.getClientId())
                .queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("state", getRealState(state));
        if (StringUtils.isNotEmpty(scopeStr)) {
            builder.queryParam("scope", scopeStr);
        }
        return builder.build();
    }

    @Override
    public AccToken getAccessToken(Callback Callback) {
        return getAuthToken(doPostAuthorizationCode(Callback.getCode()));
    }

    private AccToken getAuthToken(String response) {
        JSONObject object = checkResponse(response);
        JSONObject resultObject = object.getJSONObject("d");
        return AccToken.builder()
                .accessToken(resultObject.getString("accessToken"))
                .refreshToken(resultObject.getString("refreshToken"))
                .expireIn(resultObject.getIntValue("expiresIn"))
                .build();
    }

    private JSONObject checkResponse(String response) {
        JSONObject object = JSONObject.parseObject(response);
        if (!Symbol.ZERO.equals(object.getString("c"))) {
            throw new InstrumentException(object.getString("m"));
        }
        return object;
    }

    @Override
    public Property getUserInfo(AccToken token) {
        String openId = this.getOpenId(token);
        String response = Httpx.get(Builder.fromBaseUrl(source.userInfo())
                .queryParam("access_token", token.getAccessToken())
                .queryParam("open_id", openId)
                .build());
        JSONObject object = JSONObject.parseObject(response);
        if (!Symbol.ZERO.equals(object.getString("c"))) {
            throw new InstrumentException(object.getString("m"));
        }
        JSONObject resultObject = object.getJSONObject("d");

        return Property.builder()
                .username(resultObject.getString("userName"))
                .nickname(resultObject.getString("userName"))
                .avatar(resultObject.getString("avatar"))
                .uuid(resultObject.getString("openId"))
                .token(token)
                .source(source.toString())
                .build();
    }

    /**
     * 获取酷家乐的openId,此id在当前client范围内可以唯一识别授权用户
     *
     * @param token 通过{@link KujialeProvider#getAccessToken(Callback)}获取到的{@code authToken}
     * @return openId
     */
    private String getOpenId(AccToken token) {
        String response = Httpx.get(Builder.fromBaseUrl("https://oauth.kujiale.com/oauth2/auth/user")
                .queryParam("access_token", token.getAccessToken())
                .build());
        JSONObject accessTokenObject = checkResponse(response);
        return accessTokenObject.getString("d");
    }

    @Override
    public Message refresh(AccToken token) {
        String response = Httpx.post(refreshTokenUrl(token.getRefreshToken()));
        return Message.builder().errcode(Builder.Status.SUCCESS.getCode()).data(getAuthToken(response)).build();
    }

}
