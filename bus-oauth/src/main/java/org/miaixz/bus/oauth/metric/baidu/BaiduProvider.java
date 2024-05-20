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
package org.miaixz.bus.oauth.metric.baidu;

import com.alibaba.fastjson.JSONObject;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.*;
import org.miaixz.bus.oauth.metric.DefaultProvider;

/**
 * 百度 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BaiduProvider extends DefaultProvider {

    public BaiduProvider(Context context) {
        super(context, Registry.BAIDU);
    }

    public BaiduProvider(Context context, ExtendCache authorizeCache) {
        super(context, Registry.BAIDU, authorizeCache);
    }

    @Override
    protected AccToken getAccessToken(Callback authCallback) {
        String response = doPostAuthorizationCode(authCallback.getCode());
        return getAuthToken(response);
    }

    /**
     * https://openapi.baidu.com/rest/2.0/passport/users/getInfo?access_token=121.c86e87cc0828cc1dabb8faee540531d4.YsUIAWvYbgqVni1VhkgKgyLh8nEyELbDOEZs_OA.OgDgmA
     * https://openapi.baidu.com/rest/2.0/passport/users/getInfo?access_token=121.2907d9facf9fb97adf7287fa75496eda.Y3NSjR3-3HKt1RgT0HEl7GgxRXT5gOOVdngXezY.OcC_7g
     * 新旧应用返回的用户信息不一致
     *
     * @param accToken token信息
     * @return Property
     */
    @Override
    protected Property getUserInfo(AccToken accToken) {
        String userInfo = doGetUserInfo(accToken);
        JSONObject object = JSONObject.parseObject(userInfo);
        this.checkResponse(object);
        return Property.builder()
                .rawJson(object)
                .uuid(object.containsKey("userid") ? object.getString("userid") : object.getString("openid"))
                .username(object.getString("username"))
                .nickname(object.getString("username"))
                .avatar(getAvatar(object))
                .remark(object.getString("userdetail"))
                .gender(Gender.of(object.getString("sex")))
                .token(accToken)
                .source(complex.toString())
                .build();
    }

    private String getAvatar(JSONObject object) {
        String protrait = object.getString("portrait");
        return StringKit.isEmpty(protrait) ? null : String.format("http://himg.bdimg.com/sys/portrait/item/%s.jpg", protrait);
    }

    @Override
    public Message revoke(AccToken accToken) {
        String response = doGetRevoke(accToken);
        JSONObject object = JSONObject.parseObject(response);
        this.checkResponse(object);
        // 返回1表示取消授权成功，否则失败
        ErrorCode status = object.getIntValue("result") == 1 ? ErrorCode.SUCCESS : ErrorCode.FAILURE;
        return Message.builder().errcode(status.getCode()).errmsg(status.getDesc()).build();
    }

    @Override
    public Message refresh(AccToken accToken) {
        String refreshUrl = Builder.fromUrl(this.complex.refresh())
                .queryParam("grant_type", "refresh_token")
                .queryParam("refresh_token", accToken.getRefreshToken())
                .queryParam("client_id", this.context.getAppKey())
                .queryParam("client_secret", this.context.getAppSecret())
                .build();
        String response = Httpx.get(refreshUrl);
        return Message.builder()
                .errcode(ErrorCode.SUCCESS.getCode())
                .data(this.getAuthToken(response))
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
        return Builder.fromUrl(super.authorize(state))
                .queryParam("display", "popup")
                .queryParam("scope", this.getScopes(" ", true, this.getDefaultScopes(BaiduScope.values())))
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
