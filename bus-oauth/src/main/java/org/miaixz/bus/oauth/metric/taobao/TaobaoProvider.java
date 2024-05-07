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
package org.miaixz.bus.oauth.metric.taobao;

import com.alibaba.fastjson.JSONObject;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.exception.AuthorizedException;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.core.toolkit.UriKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.*;
import org.miaixz.bus.oauth.metric.DefaultProvider;

/**
 * 淘宝 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TaobaoProvider extends DefaultProvider {

    public TaobaoProvider(Context context) {
        super(context, Registry.TAOBAO);
    }

    public TaobaoProvider(Context context, ExtendCache authorizeCache) {
        super(context, Registry.TAOBAO, authorizeCache);
    }

    @Override
    protected AccToken getAccessToken(Callback authCallback) {
        return AccToken.builder().accessCode(authCallback.getCode()).build();
    }

    private AccToken getAuthToken(JSONObject object) {
        this.checkResponse(object);

        return AccToken.builder()
                .accessToken(object.getString("access_token"))
                .expireIn(object.getIntValue("expires_in"))
                .tokenType(object.getString("token_type"))
                .idToken(object.getString("id_token"))
                .refreshToken(object.getString("refresh_token"))
                .uid(object.getString("taobao_user_id"))
                .openId(object.getString("taobao_open_uid"))
                .build();
    }

    private void checkResponse(JSONObject object) {
        if (object.containsKey("error")) {
            throw new AuthorizedException(object.getString("error_description"));
        }
    }

    @Override
    protected Property getUserInfo(AccToken accToken) {
        String response = doPostAuthorizationCode(accToken.getAccessCode());
        JSONObject accessTokenObject = JSONObject.parseObject(response);
        if (accessTokenObject.containsKey("error")) {
            throw new AuthorizedException(accessTokenObject.getString("error_description"));
        }
        accToken = this.getAuthToken(accessTokenObject);

        String nick = UriKit.decode(accessTokenObject.getString("taobao_user_nick"));
        return Property.builder()
                .rawJson(accessTokenObject)
                .uuid(StringKit.isEmpty(accToken.getUid()) ? accToken.getOpenId() : accToken.getUid())
                .username(nick)
                .nickname(nick)
                .gender(Gender.UNKNOWN)
                .token(accToken)
                .source(complex.toString())
                .build();
    }

    @Override
    public Message refresh(AccToken oldToken) {
        String tokenUrl = refreshTokenUrl(oldToken.getRefreshToken());
        String response = Httpx.post(tokenUrl);
        JSONObject accessTokenObject = JSONObject.parseObject(response);
        return Message.builder()
                .errcode(ErrorCode.SUCCESS.getCode())
                .data(this.getAuthToken(accessTokenObject))
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
                .queryParam("client_id", context.getAppKey())
                .queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("view", "web")
                .queryParam("state", getRealState(state))
                .build();
    }

}
