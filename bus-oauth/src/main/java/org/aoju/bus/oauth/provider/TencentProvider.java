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
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Property;

/**
 * 腾讯云登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TencentProvider extends AbstractProvider {

    public TencentProvider(Context context) {
        super(context, Registry.TENCENT);
    }

    public TencentProvider(Context context, ExtendCache extendCache) {
        super(context, Registry.TENCENT, extendCache);
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        JSONObject object = JSONObject.parseObject(doGetAuthorizationCode(callback.getCode()));
        this.checkResponse(object);
        return AccToken.builder()
                .accessToken(object.getString("access_token"))
                .expireIn(object.getIntValue("expires_in"))
                .refreshToken(object.getString("refresh_token"))
                .build();
    }

    @Override
    public Property getUserInfo(AccToken accToken) {
        JSONObject object = JSONObject.parseObject(doGetUserInfo(accToken));
        this.checkResponse(object);

        object = object.getJSONObject("data");
        return Property.builder()
                .rawJson(object)
                .uuid(object.getString("id"))
                .username(object.getString("name"))
                .avatar("https://dev.tencent.com/" + object.getString("avatar"))
                .blog("https://dev.tencent.com/" + object.getString("path"))
                .nickname(object.getString("name"))
                .company(object.getString("company"))
                .location(object.getString("location"))
                .gender(Normal.Gender.of(object.getString("sex")))
                .email(object.getString("email"))
                .remark(object.getString("slogan"))
                .token(accToken)
                .source(source.toString())
                .build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(JSONObject object) {
        if (object.getIntValue("code") != 0) {
            throw new AuthorizedException(object.getString("msg"));
        }
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
                .queryParam("client_id", context.getAppKey())
                .queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("scope", "user")
                .queryParam("state", getRealState(state))
                .build();
    }

}
