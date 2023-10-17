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
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.core.toolkit.UriKit;
import org.aoju.bus.http.Httpx;
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Message;
import org.aoju.bus.oauth.magic.Property;

/**
 * 淘宝登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TaobaoProvider extends AbstractProvider {

    public TaobaoProvider(Context context) {
        super(context, Registry.TAOBAO);
    }

    public TaobaoProvider(Context context, ExtendCache extendCache) {
        super(context, Registry.TAOBAO, extendCache);
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        return AccToken.builder().accessCode(callback.getCode()).build();
    }

    @Override
    public Property getUserInfo(AccToken accToken) {
        String response = doPostAuthorizationCode(accToken.getAccessCode());
        JSONObject object = JSONObject.parseObject(response);
        if (object.containsKey("error")) {
            throw new AuthorizedException(object.getString("error_description"));
        }
        accToken.setAccessToken(object.getString("access_token"));
        accToken.setRefreshToken(object.getString("refresh_token"));
        accToken.setExpireIn(object.getIntValue("expires_in"));
        accToken.setUid(object.getString("taobao_user_id"));
        accToken.setOpenId(object.getString("taobao_open_uid"));

        accToken = this.getAuthToken(object);

        String nick = UriKit.decode(object.getString("taobao_user_nick"));
        return Property.builder()
                .rawJson(new JSONObject())
                .uuid(StringKit.isEmpty(accToken.getUid()) ? accToken.getOpenId() : accToken.getUid())
                .username(nick)
                .nickname(nick)
                .gender(Normal.Gender.UNKNOWN)
                .token(accToken)
                .source(source.toString())
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
                .queryParam("client_id", context.getAppKey())
                .queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("view", "web")
                .queryParam("state", getRealState(state))
                .build();
    }

    @Override
    public Message refresh(AccToken oldToken) {
        String response = Httpx.post(refreshTokenUrl(oldToken.getRefreshToken()));
        JSONObject accessTokenObject = JSONObject.parseObject(response);
        return Message.builder()
                .errcode(Builder.ErrorCode.SUCCESS.getCode())
                .data(this.getAuthToken(accessTokenObject))
                .build();
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

}
