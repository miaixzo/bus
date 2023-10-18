/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.oauth.provider;

import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.cache.metric.ExtendCache;
import org.aoju.bus.core.exception.AuthorizedException;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.http.Httpx;
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Message;
import org.aoju.bus.oauth.magic.Property;
import org.aoju.bus.oauth.metric.OauthScope;

import java.text.MessageFormat;

/**
 * 小米登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class XiaomiProvider extends AbstractProvider {

    public XiaomiProvider(Context context) {
        super(context, Registry.XIAOMI);
    }

    public XiaomiProvider(Context context, ExtendCache extendCache) {
        super(context, Registry.XIAOMI, extendCache);
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        return getToken(accessTokenUrl(callback.getCode()));
    }

    private AccToken getToken(String accessTokenUrl) {
        String response = Httpx.get(accessTokenUrl);
        JSONObject object = JSONObject.parseObject(StringKit.replace(response, "&&&START&&&", Normal.EMPTY));

        if (object.containsKey("error")) {
            throw new AuthorizedException(object.getString("error_description"));
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
    public Property getUserInfo(AccToken accToken) {
        // 获取用户信息
        JSONObject jsonObject = JSONObject.parseObject(doGetUserInfo(accToken));
        if ("error".equalsIgnoreCase(jsonObject.getString("result"))) {
            throw new AuthorizedException(jsonObject.getString("description"));
        }

        JSONObject object = jsonObject.getJSONObject("data");

        Property property = Property.builder()
                .rawJson(object)
                .uuid(accToken.getOpenId())
                .username(object.getString("miliaoNick"))
                .nickname(object.getString("miliaoNick"))
                .avatar(object.getString("miliaoIcon"))
                .email(object.getString("mail"))
                .gender(Normal.Gender.UNKNOWN)
                .token(accToken)
                .source(source.toString())
                .build();

        // 获取用户邮箱手机号等信息
        String emailPhoneUrl = MessageFormat.format("{0}?clientId={1}&token={2}", "https://open.account.xiaomi.com/user/phoneAndEmail", context
                .getAppKey(), accToken.getAccessToken());

        JSONObject userEmailPhone = JSONObject.parseObject(Httpx.get(emailPhoneUrl));
        if (!"error".equalsIgnoreCase(userEmailPhone.getString("result"))) {
            JSONObject emailPhone = userEmailPhone.getJSONObject("data");
            property.setEmail(emailPhone.getString("email"));
        }

        return property;
    }

    /**
     * 刷新access token (续期)
     *
     * @param accToken 登录成功后返回的Token信息
     * @return AuthResponse
     */
    @Override
    public Message refresh(AccToken accToken) {
        return Message.builder()
                .errcode(Builder.ErrorCode.SUCCESS.getCode())
                .data(getToken(refreshTokenUrl(accToken.getRefreshToken())))
                .build();
    }

    /**
     * 返回带{@code state}参数的授权url,授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数,可以防止csrf
     * @return 返回授权地址
     */
    @Override
    public String authorize(String state) {
        return Builder.fromUrl(super.authorize(state))
                .queryParam("skip_confirm", "false")
                .queryParam("scope", this.getScopes(Symbol.SPACE, true, getScopes(true, OauthScope.Mi.values())))
                .build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param accToken 用户授权后的token
     * @return 返回获取userInfo的url
     */
    @Override
    public String userInfoUrl(AccToken accToken) {
        return Builder.fromUrl(source.userInfo())
                .queryParam("clientId", context.getAppKey())
                .queryParam("token", accToken.getAccessToken())
                .build();
    }

}
