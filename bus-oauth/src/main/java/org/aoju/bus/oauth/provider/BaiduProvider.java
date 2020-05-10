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
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.http.Httpx;
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Message;
import org.aoju.bus.oauth.magic.Property;

/**
 * 百度账号登录
 *
 * @author Kimi Liu
 * @version 5.9.0
 * @since JDK 1.8+
 */
public class BaiduProvider extends DefaultProvider {

    public BaiduProvider(Context context) {
        super(context, Registry.BAIDU);
    }

    public BaiduProvider(Context context, ExtendCache extendCache) {
        super(context, Registry.BAIDU, extendCache);
    }

    @Override
    protected AccToken getAccessToken(Callback Callback) {
        String response = doPostAuthorizationCode(Callback.getCode());
        return getAuthToken(response);
    }

    @Override
    protected Property getUserInfo(AccToken token) {
        JSONObject object = JSONObject.parseObject(doGetUserInfo(token));
        this.checkResponse(object);
        return Property.builder()
                .uuid(object.getString("userid"))
                .username(object.getString("username"))
                .nickname(object.getString("username"))
                .avatar(getAvatar(object))
                .remark(object.getString("userdetail"))
                .gender(Normal.Gender.getGender(object.getString("sex")))
                .token(token)
                .source(source.toString())
                .build();
    }

    private String getAvatar(JSONObject object) {
        String protrait = object.getString("portrait");
        return StringUtils.isEmpty(protrait) ? null : String.format("http://himg.bdimg.com/sys/portrait/item/%s.jpg", protrait);
    }

    @Override
    public Message revoke(AccToken token) {
        JSONObject object = JSONObject.parseObject(doGetRevoke(token));
        this.checkResponse(object);
        // 返回1表示取消授权成功,否则失败
        Builder.ErrorCode status = object.getIntValue("result") == 1 ? Builder.ErrorCode.SUCCESS : Builder.ErrorCode.FAILURE;
        return Message.builder().errcode(status.getCode()).errmsg(status.getMsg()).build();
    }

    @Override
    public Message refresh(AccToken token) {
        String refreshUrl = Builder.fromUrl(this.source.refresh())
                .queryParam("grant_type", "refresh_token")
                .queryParam("refresh_token", token.getRefreshToken())
                .queryParam("client_id", this.context.getAppKey())
                .queryParam("client_secret", this.context.getAppSecret())
                .build();
        return Message.builder()
                .errcode(Builder.ErrorCode.SUCCESS.getCode())
                .data(this.getAuthToken(Httpx.get(refreshUrl)))
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
                .queryParam("display", "popup")
                .queryParam("state", getRealState(state))
                .build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(JSONObject object) {
        if (object.containsKey("error") || object.containsKey("error_code")) {
            String msg = object.containsKey("error_description") ? object.getString("error_description") : object.getString("error_msg");
            throw new AuthorizedException(msg);
        }
    }

    private AccToken getAuthToken(String response) {
        JSONObject accessTokenObject = JSONObject.parseObject(response);
        this.checkResponse(accessTokenObject);
        return AccToken.builder()
                .accessToken(accessTokenObject.getString("access_token"))
                .refreshToken(accessTokenObject.getString("refresh_token"))
                .scope(accessTokenObject.getString("scope"))
                .expireIn(accessTokenObject.getIntValue("expires_in"))
                .build();
    }

}
