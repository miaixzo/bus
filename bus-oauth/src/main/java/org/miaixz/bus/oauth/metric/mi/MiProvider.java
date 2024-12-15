/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org justauth.cn and other contributors.        ~
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
package org.miaixz.bus.oauth.metric.mi;

import java.text.MessageFormat;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.ErrorCode;
import org.miaixz.bus.oauth.magic.Material;
import org.miaixz.bus.oauth.metric.AbstractProvider;

import com.alibaba.fastjson.JSONObject;

/**
 * 小米 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MiProvider extends AbstractProvider {
    private static final String PREFIX = "&&&START&&&";

    public MiProvider(Context context) {
        super(context, Registry.MI);
    }

    public MiProvider(Context context, ExtendCache cache) {
        super(context, Registry.MI, cache);
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        return getToken(accessTokenUrl(callback.getCode()));
    }

    private AccToken getToken(String accessTokenUrl) {
        String response = Httpx.get(accessTokenUrl);
        String jsonStr = response.replace(PREFIX, Normal.EMPTY);
        JSONObject accessTokenObject = JSONObject.parseObject(jsonStr);

        if (accessTokenObject.containsKey("error")) {
            throw new AuthorizedException(accessTokenObject.getString("error_description"));
        }

        return AccToken.builder().accessToken(accessTokenObject.getString("access_token"))
                .expireIn(accessTokenObject.getIntValue("expires_in")).scope(accessTokenObject.getString("scope"))
                .tokenType(accessTokenObject.getString("token_type"))
                .refreshToken(accessTokenObject.getString("refresh_token"))
                .openId(accessTokenObject.getString("openId"))
                .macAlgorithm(accessTokenObject.getString("mac_algorithm"))
                .macKey(accessTokenObject.getString("mac_key")).build();
    }

    @Override
    public Material getUserInfo(AccToken accToken) {
        // 获取用户信息
        String userResponse = doGetUserInfo(accToken);

        JSONObject userProfile = JSONObject.parseObject(userResponse);
        if ("error".equalsIgnoreCase(userProfile.getString("result"))) {
            throw new AuthorizedException(userProfile.getString("description"));
        }

        JSONObject object = userProfile.getJSONObject("data");

        Material authUser = Material.builder().rawJson(object).uuid(accToken.getOpenId())
                .username(object.getString("miliaoNick")).nickname(object.getString("miliaoNick"))
                .avatar(object.getString("miliaoIcon")).email(object.getString("mail")).gender(Gender.UNKNOWN)
                .token(accToken).source(complex.toString()).build();

        // 获取用户邮箱手机号等信息
        String emailPhoneUrl = MessageFormat.format("{0}?clientId={1}&token={2}",
                "https://open.account.xiaomi.com/user/phoneAndEmail", context.getAppKey(), accToken.getAccessToken());

        String emailResponse = Httpx.get(emailPhoneUrl);
        JSONObject userEmailPhone = JSONObject.parseObject(emailResponse);
        if (!"error".equalsIgnoreCase(userEmailPhone.getString("result"))) {
            JSONObject emailPhone = userEmailPhone.getJSONObject("data");
            authUser.setEmail(emailPhone.getString("email"));
        } else {
            Logger.warn("小米开发平台暂时不对外开放用户手机及邮箱信息的获取");
        }

        return authUser;
    }

    /**
     * 刷新access token （续期）
     *
     * @param accToken 登录成功后返回的Token信息
     * @return Message
     */
    @Override
    public Message refresh(AccToken accToken) {
        return Message.builder().errcode(ErrorCode.SUCCESS.getCode())
                .data(getToken(refreshTokenUrl(accToken.getRefreshToken()))).build();
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     */
    @Override
    public String authorize(String state) {
        return Builder.fromUrl(super.authorize(state)).queryParam("skip_confirm", "false")
                .queryParam("scope", this.getScopes(Symbol.SPACE, true, this.getDefaultScopes(MiScope.values())))
                .build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param accToken 用户授权后的token
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AccToken accToken) {
        return Builder.fromUrl(complex.userInfo()).queryParam("clientId", context.getAppKey())
                .queryParam("token", accToken.getAccessToken()).build();
    }

}
