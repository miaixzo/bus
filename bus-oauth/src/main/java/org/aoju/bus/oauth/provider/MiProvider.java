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
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.http.HttpClient;
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Message;
import org.aoju.bus.oauth.magic.Property;
import org.aoju.bus.oauth.metric.StateCache;

import java.text.MessageFormat;

/**
 * 小米登录
 *
 * @author Kimi Liu
 * @version 5.0.1
 * @since JDK 1.8+
 */
public class MiProvider extends DefaultProvider {

    public MiProvider(Context config) {
        super(config, Registry.MI);
    }

    public MiProvider(Context config, StateCache stateCache) {
        super(config, Registry.MI, stateCache);
    }

    @Override
    protected AccToken getAccessToken(Callback Callback) {
        return getToken(accessTokenUrl(Callback.getCode()));
    }

    private AccToken getToken(String accessTokenUrl) {
        String response = HttpClient.get(accessTokenUrl);
        JSONObject object = JSONObject.parseObject(StringUtils.replace(response, "&&&START&&&", Normal.EMPTY));

        if (object.containsKey("error")) {
            throw new InstrumentException(object.getString("error_description"));
        }

        return AccToken.builder()
                .accessToken(object.getString("access_token"))
                .expireIn(object.getIntValue("expires_in"))
                .scope(object.getString("scope"))
                .tokenType(object.getString("token_type"))
                .refreshToken(object.getString("refresh_token"))
                .openId(object.getString("openId"))
                .macAlgorithm(object.getString("mac_algorithm"))
                .macKey(object.getString("mac_key"))
                .build();
    }

    @Override
    protected Property getUserInfo(AccToken token) {
        // 获取用户信息
        JSONObject object = JSONObject.parseObject(doGetUserInfo(token));
        if ("error".equalsIgnoreCase(object.getString("result"))) {
            throw new InstrumentException(object.getString("description"));
        }

        JSONObject user = object.getJSONObject("data");

        Property property = Property.builder()
                .uuid(token.getOpenId())
                .username(user.getString("miliaoNick"))
                .nickname(user.getString("miliaoNick"))
                .avatar(user.getString("miliaoIcon"))
                .email(user.getString("mail"))
                .gender(Normal.Gender.UNKNOWN)
                .token(token)
                .source(source.toString())
                .build();

        // 获取用户邮箱手机号等信息
        String emailPhoneUrl = MessageFormat.format("{0}?clientId={1}&token={2}", "https://open.account.xiaomi.com/user/phoneAndEmail", config
                .getClientId(), token.getAccessToken());

        JSONObject userEmailPhone = JSONObject.parseObject(HttpClient.get(emailPhoneUrl));
        if (!"error".equalsIgnoreCase(userEmailPhone.getString("result"))) {
            JSONObject emailPhone = userEmailPhone.getJSONObject("data");
            property.setEmail(emailPhone.getString("email"));
        }

        return property;
    }

    /**
     * 刷新access token （续期）
     *
     * @param token 登录成功后返回的Token信息
     * @return AuthResponse
     */
    @Override
    public Message refresh(AccToken token) {
        return Message.builder()
                .errcode(Builder.Status.SUCCESS.getCode())
                .data(getToken(refreshTokenUrl(token.getRefreshToken())))
                .build();
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     * @since 1.9.3
     */
    @Override
    public String authorize(String state) {
        return Builder.fromBaseUrl(source.authorize())
                .queryParam("response_type", "code")
                .queryParam("client_id", config.getClientId())
                .queryParam("redirect_uri", config.getRedirectUri())
                .queryParam("scope", "user/profile%20user/openIdV2%20user/phoneAndEmail")
                .queryParam("skip_confirm", "false")
                .queryParam("state", getRealState(state))
                .build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param token 用户授权后的token
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AccToken token) {
        return Builder.fromBaseUrl(source.userInfo())
                .queryParam("clientId", config.getClientId())
                .queryParam("token", token.getAccessToken())
                .build();
    }
}
