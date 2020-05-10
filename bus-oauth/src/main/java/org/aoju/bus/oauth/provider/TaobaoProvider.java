/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.oauth.provider;

import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.cache.metric.ExtendCache;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.exception.AuthorizedException;
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Property;

/**
 * 淘宝登录
 *
 * @author Kimi Liu
 * @version 5.9.0
 * @since JDK 1.8+
 */
public class TaobaoProvider extends DefaultProvider {

    public TaobaoProvider(Context context) {
        super(context, Registry.TAOBAO);
    }

    public TaobaoProvider(Context context, ExtendCache extendCache) {
        super(context, Registry.TAOBAO, extendCache);
    }

    @Override
    protected AccToken getAccessToken(Callback Callback) {
        return AccToken.builder().accessCode(Callback.getCode()).build();
    }

    @Override
    protected Property getUserInfo(AccToken token) {
        String response = doPostAuthorizationCode(token.getAccessCode());
        JSONObject object = JSONObject.parseObject(response);
        if (object.containsKey("error")) {
            throw new AuthorizedException(object.getString("error_description"));
        }
        token.setAccessToken(object.getString("access_token"));
        token.setRefreshToken(object.getString("refresh_token"));
        token.setExpireIn(object.getIntValue("expires_in"));
        token.setUid(object.getString("taobao_user_id"));
        token.setOpenId(object.getString("taobao_open_uid"));

        String nick = urlDecode(object.getString("taobao_user_nick"));
        return Property.builder()
                .uuid(object.getString("taobao_user_id"))
                .username(nick)
                .nickname(nick)
                .gender(Normal.Gender.UNKNOWN)
                .token(token)
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

}
