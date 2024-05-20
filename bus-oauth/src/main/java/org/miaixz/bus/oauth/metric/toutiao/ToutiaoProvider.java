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
package org.miaixz.bus.oauth.metric.toutiao;

import com.alibaba.fastjson.JSONObject;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.ErrorCode;
import org.miaixz.bus.oauth.magic.Property;
import org.miaixz.bus.oauth.metric.DefaultProvider;

/**
 * 今日头条 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ToutiaoProvider extends DefaultProvider {

    public ToutiaoProvider(Context context) {
        super(context, Registry.TOUTIAO);
    }

    public ToutiaoProvider(Context context, ExtendCache authorizeCache) {
        super(context, Registry.TOUTIAO, authorizeCache);
    }

    @Override
    protected AccToken getAccessToken(Callback authCallback) {
        String response = doGetAuthorizationCode(authCallback.getCode());
        JSONObject accessTokenObject = JSONObject.parseObject(response);

        this.checkResponse(accessTokenObject);

        return AccToken.builder()
                .accessToken(accessTokenObject.getString("access_token"))
                .expireIn(accessTokenObject.getIntValue("expires_in"))
                .openId(accessTokenObject.getString("open_id"))
                .build();
    }

    @Override
    protected Property getUserInfo(AccToken accToken) {
        String userResponse = doGetUserInfo(accToken);

        JSONObject userProfile = JSONObject.parseObject(userResponse);

        this.checkResponse(userProfile);

        JSONObject user = userProfile.getJSONObject("data");

        boolean isAnonymousUser = user.getIntValue("uid_type") == 14;
        String anonymousUserName = "匿名用户";

        return Property.builder()
                .rawJson(user)
                .uuid(user.getString("uid"))
                .username(isAnonymousUser ? anonymousUserName : user.getString("screen_name"))
                .nickname(isAnonymousUser ? anonymousUserName : user.getString("screen_name"))
                .avatar(user.getString("avatar_url"))
                .remark(user.getString("description"))
                .gender(Gender.of(user.getString("gender")))
                .token(accToken)
                .source(complex.toString())
                .build();
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     */
    @Override
    public String authorize(String state) {
        return Builder.fromUrl(complex.authorize())
                .queryParam("response_type", "code")
                .queryParam("client_key", context.getAppKey())
                .queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("auth_only", 1)
                .queryParam("display", 0)
                .queryParam("state", getRealState(state))
                .build();
    }

    /**
     * 返回获取accessToken的url
     *
     * @param code 授权码
     * @return 返回获取accessToken的url
     */
    @Override
    protected String accessTokenUrl(String code) {
        return Builder.fromUrl(complex.accessToken())
                .queryParam("code", code)
                .queryParam("client_key", context.getAppKey())
                .queryParam("client_secret", context.getAppSecret())
                .queryParam("grant_type", "authorization_code")
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
        return Builder.fromUrl(complex.userInfo())
                .queryParam("client_key", context.getAppKey())
                .queryParam("access_token", accToken.getAccessToken())
                .build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(JSONObject object) {
        if (object.containsKey("error_code")) {
            throw new AuthorizedException(ErrorCode.Toutiao.getErrorCode(object.getString("error_code")).getDesc());
        }
    }

}
