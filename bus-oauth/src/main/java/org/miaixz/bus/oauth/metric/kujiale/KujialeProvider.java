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
package org.miaixz.bus.oauth.metric.kujiale;

import com.alibaba.fastjson.JSONObject;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.exception.AuthorizedException;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.*;
import org.miaixz.bus.oauth.metric.DefaultProvider;

/**
 * 酷家乐 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class KujialeProvider extends DefaultProvider {

    public KujialeProvider(Context context) {
        super(context, Registry.KUJIALE);
    }

    public KujialeProvider(Context context, ExtendCache authorizeCache) {
        super(context, Registry.KUJIALE, authorizeCache);
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     * 默认只向用户请求用户信息授权
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     */
    @Override
    public String authorize(String state) {
        return Builder.fromUrl(super.authorize(state))
                .queryParam("scope", this.getScopes(",", false, this.getDefaultScopes(KujialeScope.values())))
                .build();
    }

    @Override
    public AccToken getAccessToken(Callback authCallback) {
        String response = doPostAuthorizationCode(authCallback.getCode());
        return getAuthToken(response);
    }

    private AccToken getAuthToken(String response) {
        JSONObject accessTokenObject = checkResponse(response);
        JSONObject resultObject = accessTokenObject.getJSONObject("d");
        return AccToken.builder()
                .accessToken(resultObject.getString("accessToken"))
                .refreshToken(resultObject.getString("refreshToken"))
                .expireIn(resultObject.getIntValue("expiresIn"))
                .build();
    }

    private JSONObject checkResponse(String response) {
        JSONObject accessTokenObject = JSONObject.parseObject(response);
        if (!"0".equals(accessTokenObject.getString("c"))) {
            throw new AuthorizedException(accessTokenObject.getString("m"));
        }
        return accessTokenObject;
    }

    @Override
    public Property getUserInfo(AccToken accToken) {
        String openId = this.getOpenId(accToken);
        String response = Httpx.get(Builder.fromUrl(complex.userInfo())
                .queryParam("access_token", accToken.getAccessToken())
                .queryParam("open_id", openId)
                .build());
        JSONObject object = JSONObject.parseObject(response);
        if (!"0".equals(object.getString("c"))) {
            throw new AuthorizedException(object.getString("m"));
        }
        JSONObject resultObject = object.getJSONObject("d");

        return Property.builder()
                .rawJson(resultObject)
                .username(resultObject.getString("userName"))
                .nickname(resultObject.getString("userName"))
                .avatar(resultObject.getString("avatar"))
                .uuid(resultObject.getString("openId"))
                .token(accToken)
                .source(complex.toString())
                .build();
    }

    /**
     * 获取酷家乐的openId，此id在当前client范围内可以唯一识别授权用户
     *
     * @param accToken 通过{@link KujialeProvider#getAccessToken(Callback)}获取到的{@code accToken}
     * @return openId
     */
    private String getOpenId(AccToken accToken) {
        String response = Httpx.get(Builder.fromUrl("https://oauth.kujiale.com/oauth2/auth/user")
                .queryParam("access_token", accToken.getAccessToken())
                .build());
        JSONObject accessTokenObject = checkResponse(response);
        return accessTokenObject.getString("d");
    }

    @Override
    public Message refresh(AccToken accToken) {
        String response = Httpx.post(refreshTokenUrl(accToken.getRefreshToken()));
        return Message.builder().errcode(ErrorCode.SUCCESS.getCode()).data(getAuthToken(response)).build();
    }

}
