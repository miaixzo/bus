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
import org.aoju.bus.http.Httpx;
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Message;
import org.aoju.bus.oauth.magic.Property;
import org.aoju.bus.oauth.metric.OauthScope;

/**
 * 抖音登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DouyinProvider extends AbstractProvider {

    public DouyinProvider(Context context) {
        super(context, Registry.DOUYIN);
    }

    public DouyinProvider(Context context, ExtendCache extendCache) {
        super(context, Registry.DOUYIN, extendCache);
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        return this.getToken(accessTokenUrl(callback.getCode()));
    }

    @Override
    public Property getUserInfo(AccToken accToken) {
        JSONObject jsonObject = JSONObject.parseObject(doGetUserInfo(accToken));
        this.checkResponse(jsonObject);
        JSONObject object = jsonObject.getJSONObject("data");
        return Property.builder()
                .rawJson(object)
                .uuid(object.getString("union_id"))
                .username(object.getString("nickname"))
                .nickname(object.getString("nickname"))
                .avatar(object.getString("avatar"))
                .remark(object.getString("description"))
                .gender(Normal.Gender.of(object.getString("gender")))
                .location(String.format("%s %s %s", object.getString("country"), object.getString("province"), object.getString("city")))
                .token(accToken)
                .source(source.toString())
                .build();
    }

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
        return Builder.fromUrl(source.authorize())
                .queryParam("response_type", "code")
                .queryParam("client_key", context.getAppKey())
                .queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("scope", this.getScopes(Symbol.COMMA, true, getScopes(true, OauthScope.Douyin.values())))
                .queryParam("state", getRealState(state))
                .build();
    }

    /**
     * 返回获取accessToken的url
     *
     * @param code oauth的授权码
     * @return 返回获取accessToken的url
     */
    @Override
    public String accessTokenUrl(String code) {
        return Builder.fromUrl(source.accessToken())
                .queryParam("code", code)
                .queryParam("client_key", context.getAppKey())
                .queryParam("client_secret", context.getAppSecret())
                .queryParam("grant_type", "authorization_code")
                .build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param accToken oauth返回的token
     * @return 返回获取userInfo的url
     */
    @Override
    public String userInfoUrl(AccToken accToken) {
        return Builder.fromUrl(source.userInfo())
                .queryParam("access_token", accToken.getAccessToken())
                .queryParam("open_id", accToken.getOpenId())
                .build();
    }

    /**
     * 返回获取accessToken的url
     *
     * @param refreshToken oauth返回的refreshtoken
     * @return 返回获取accessToken的url
     */
    @Override
    public String refreshTokenUrl(String refreshToken) {
        return Builder.fromUrl(source.refresh())
                .queryParam("client_key", context.getAppKey())
                .queryParam("refresh_token", refreshToken)
                .queryParam("grant_type", "refresh_token")
                .build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(JSONObject object) {
        String message = object.getString("message");
        JSONObject data = object.getJSONObject("data");
        int errorCode = data.getIntValue("error_code");
        if ("error".equals(message) || errorCode != 0) {
            throw new AuthorizedException(Normal.EMPTY + errorCode, data.getString("description"));
        }
    }

    /**
     * 获取token,适用于获取access_token和刷新token
     *
     * @param accessTokenUrl 实际请求token的地址
     * @return token对象
     */
    private AccToken getToken(String accessTokenUrl) {
        JSONObject object = JSONObject.parseObject(Httpx.post(accessTokenUrl));
        this.checkResponse(object);
        JSONObject dataObj = object.getJSONObject("data");
        return AccToken.builder()
                .accessToken(dataObj.getString("access_token"))
                .openId(dataObj.getString("open_id"))
                .expireIn(dataObj.getIntValue("expires_in"))
                .refreshToken(dataObj.getString("refresh_token"))
                .scope(dataObj.getString("scope"))
                .build();
    }

}
