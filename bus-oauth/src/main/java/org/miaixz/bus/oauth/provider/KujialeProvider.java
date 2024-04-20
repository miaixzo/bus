/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.oauth.provider;

import com.alibaba.fastjson.JSONObject;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.exception.AuthorizedException;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.Message;
import org.miaixz.bus.oauth.magic.Property;
import org.miaixz.bus.oauth.metric.OauthScope;

/**
 * 酷家乐授权登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class KujialeProvider extends AbstractProvider {

    public KujialeProvider(Context context) {
        super(context, Registry.KUJIALE);
    }

    public KujialeProvider(Context context, ExtendCache extendCache) {
        super(context, Registry.KUJIALE, extendCache);
    }

    /**
     * 返回带{@code state}参数的授权url,授权回调时会带上这个{@code state}
     * 默认只向用户请求用户信息授权
     *
     * @param state state 验证授权流程的参数,可以防止csrf
     * @return 返回授权地址
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
        return Builder.fromUrl(super.authorize(state))
                .queryParam("scope", this.getScopes(Symbol.COMMA, false, getScopes(true, OauthScope.Kujiale.values())))
                .build();
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        return getAuthToken(doPostAuthorizationCode(callback.getCode()));
    }

    @Override
    public Property getUserInfo(AccToken accToken) {
        String openId = this.getOpenId(accToken);
        String response = Httpx.get(Builder.fromUrl(source.userInfo())
                .queryParam("access_token", accToken.getAccessToken())
                .queryParam("open_id", openId)
                .build());
        JSONObject jsonObject = JSONObject.parseObject(response);
        if (!Symbol.ZERO.equals(jsonObject.getString("c"))) {
            throw new AuthorizedException(jsonObject.getString("m"));
        }
        JSONObject object = jsonObject.getJSONObject("d");

        return Property.builder()
                .rawJson(object)
                .username(object.getString("userName"))
                .nickname(object.getString("userName"))
                .avatar(object.getString("avatar"))
                .uuid(object.getString("openId"))
                .token(accToken)
                .source(source.toString())
                .build();
    }

    @Override
    public Message refresh(AccToken accToken) {
        String response = Httpx.post(refreshTokenUrl(accToken.getRefreshToken()));
        return Message.builder().errcode(Builder.ErrorCode.SUCCESS.getCode()).data(getAuthToken(response)).build();
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
            throw new AuthorizedException(object.getString("m"));
        }
        return object;
    }

    /**
     * 获取酷家乐的openId,此id在当前client范围内可以唯一识别授权用户
     *
     * @param accToken 通过{@link KujialeProvider#getAccessToken(Callback)}获取到的{@code authToken}
     * @return openId
     */
    private String getOpenId(AccToken accToken) {
        String response = Httpx.get(Builder.fromUrl("https://oauth.kujiale.com/oauth2/auth/user")
                .queryParam("access_token", accToken.getAccessToken())
                .build());
        JSONObject accessTokenObject = checkResponse(response);
        return accessTokenObject.getString("d");
    }

}
