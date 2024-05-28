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
package org.miaixz.bus.oauth.metric.pinterest;

import com.alibaba.fastjson.JSONObject;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.Property;
import org.miaixz.bus.oauth.metric.DefaultProvider;

import java.util.Objects;

/**
 * Pinterest 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PinterestProvider extends DefaultProvider {

    private static final String FAILURE = "failure";

    public PinterestProvider(Context context) {
        super(context, Registry.PINTEREST);
    }

    public PinterestProvider(Context context, ExtendCache authorizeCache) {
        super(context, Registry.PINTEREST, authorizeCache);
    }

    @Override
    protected AccToken getAccessToken(Callback authCallback) {
        String response = doPostAuthorizationCode(authCallback.getCode());
        JSONObject accessTokenObject = JSONObject.parseObject(response);
        this.checkResponse(accessTokenObject);
        return AccToken.builder()
                .accessToken(accessTokenObject.getString("access_token"))
                .tokenType(accessTokenObject.getString("token_type"))
                .build();
    }

    @Override
    protected Property getUserInfo(AccToken accToken) {
        String userinfoUrl = userInfoUrl(accToken);
        // TODO: 是否需要 .setFollowRedirects(true)
        String response = Httpx.get(userinfoUrl);
        JSONObject object = JSONObject.parseObject(response);
        this.checkResponse(object);
        JSONObject userObj = object.getJSONObject("data");
        return Property.builder()
                .rawJson(userObj)
                .uuid(userObj.getString("id"))
                .avatar(getAvatarUrl(userObj))
                .username(userObj.getString("username"))
                .nickname(userObj.getString("first_name") + Symbol.SPACE + userObj.getString("last_name"))
                .gender(Gender.UNKNOWN)
                .remark(userObj.getString("bio"))
                .token(accToken)
                .source(complex.toString())
                .build();
    }

    private String getAvatarUrl(JSONObject userObj) {
        // image is a map data structure
        JSONObject jsonObject = userObj.getJSONObject("image");
        if (Objects.isNull(jsonObject)) {
            return null;
        }
        return jsonObject.getJSONObject("60x60").getString("url");
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     */
    @Override
    public String authorize(String state) {
        return Builder.fromUrl(super.authorize(state))
                .queryParam("scope", this.getScopes(Symbol.COMMA, false, this.getDefaultScopes(PinterestScope.values())))
                .build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param accToken token
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AccToken accToken) {
        return Builder.fromUrl(complex.userInfo())
                .queryParam("access_token", accToken.getAccessToken())
                .queryParam("fields", "id,username,first_name,last_name,bio,image")
                .build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(JSONObject object) {
        if (!object.containsKey("status") && FAILURE.equals(object.getString("status"))) {
            throw new AuthorizedException(object.getString("message"));
        }
    }

}
