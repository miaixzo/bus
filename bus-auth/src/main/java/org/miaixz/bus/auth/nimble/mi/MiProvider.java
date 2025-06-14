/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.auth.nimble.mi;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.AccToken;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Material;
import org.miaixz.bus.auth.nimble.AbstractProvider;

import java.text.MessageFormat;
import java.util.Map;

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
        try {
            Map<String, Object> accessTokenObject = JsonKit.toPojo(jsonStr, Map.class);
            if (accessTokenObject == null) {
                throw new AuthorizedException("Failed to parse access token response: empty response");
            }

            if (accessTokenObject.containsKey("error")) {
                String errorDescription = (String) accessTokenObject.get("error_description");
                throw new AuthorizedException(errorDescription != null ? errorDescription : "Unknown error");
            }

            String accessToken = (String) accessTokenObject.get("access_token");
            if (accessToken == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            Object expiresInObj = accessTokenObject.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;
            String scope = (String) accessTokenObject.get("scope");
            String tokenType = (String) accessTokenObject.get("token_type");
            String refreshToken = (String) accessTokenObject.get("refresh_token");
            String openId = (String) accessTokenObject.get("openId");
            String macAlgorithm = (String) accessTokenObject.get("mac_algorithm");
            String macKey = (String) accessTokenObject.get("mac_key");

            return AccToken.builder().accessToken(accessToken).expireIn(expiresIn).scope(scope).tokenType(tokenType)
                    .refreshToken(refreshToken).openId(openId).macAlgorithm(macAlgorithm).macKey(macKey).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
        }
    }

    @Override
    public Material getUserInfo(AccToken accToken) {
        // 获取用户信息
        String userResponse = doGetUserInfo(accToken);
        try {
            Map<String, Object> userProfile = JsonKit.toPojo(userResponse, Map.class);
            if (userProfile == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }

            String result = (String) userProfile.get("result");
            if ("error".equalsIgnoreCase(result)) {
                String description = (String) userProfile.get("description");
                throw new AuthorizedException(description != null ? description : "Unknown error");
            }

            Map<String, Object> object = (Map<String, Object>) userProfile.get("data");
            if (object == null) {
                throw new AuthorizedException("Missing data in user info response");
            }

            String miliaoNick = (String) object.get("miliaoNick");
            if (miliaoNick == null) {
                throw new AuthorizedException("Missing miliaoNick in user info response");
            }
            String miliaoIcon = (String) object.get("miliaoIcon");
            String mail = (String) object.get("mail");

            Material authUser = Material.builder().rawJson(JsonKit.toJsonString(object)).uuid(accToken.getOpenId())
                    .username(miliaoNick).nickname(miliaoNick).avatar(miliaoIcon).email(mail).gender(Gender.UNKNOWN)
                    .token(accToken).source(complex.toString()).build();

            // 获取用户邮箱手机号等信息
            String emailPhoneUrl = MessageFormat.format("{0}?clientId={1}&token={2}",
                    "https://open.account.xiaomi.com/user/phoneAndEmail", context.getAppKey(),
                    accToken.getAccessToken());

            String emailResponse = Httpx.get(emailPhoneUrl);
            try {
                Map<String, Object> userEmailPhone = JsonKit.toPojo(emailResponse, Map.class);
                if (userEmailPhone == null) {
                    Logger.warn("Failed to parse email/phone response: empty response");
                    return authUser;
                }

                String emailResult = (String) userEmailPhone.get("result");
                if (!"error".equalsIgnoreCase(emailResult)) {
                    Map<String, Object> emailPhone = (Map<String, Object>) userEmailPhone.get("data");
                    if (emailPhone != null) {
                        String email = (String) emailPhone.get("email");
                        authUser.setEmail(email);
                    }
                } else {
                    Logger.warn("小米开发平台暂时不对外开放用户手机及邮箱信息的获取");
                }
            } catch (Exception e) {
                Logger.warn("Failed to parse email/phone response: " + e.getMessage());
            }

            return authUser;
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    /**
     * 刷新access token （续期）
     *
     * @param accToken 登录成功后返回的Token信息
     * @return Message
     */
    @Override
    public Message refresh(AccToken accToken) {
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
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
        return Builder.fromUrl(this.complex.getConfig().get(Builder.USERINFO))
                .queryParam("clientId", context.getAppKey()).queryParam("token", accToken.getAccessToken()).build();
    }

}