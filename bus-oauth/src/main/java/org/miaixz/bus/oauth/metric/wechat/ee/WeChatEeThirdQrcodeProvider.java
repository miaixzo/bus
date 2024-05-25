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
package org.miaixz.bus.oauth.metric.wechat.ee;

import com.alibaba.fastjson.JSONObject;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Checker;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.*;

/**
 * 企业微信 第三方二维码登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WeChatEeThirdQrcodeProvider extends AbstractWeChatEeProvider {

    public WeChatEeThirdQrcodeProvider(Context context) {
        super(context, Registry.WECHAT_EE_QRCODE_THIRD);
    }

    public WeChatEeThirdQrcodeProvider(Context context, ExtendCache authorizeCache) {
        super(context, Registry.WECHAT_EE_QRCODE_THIRD, authorizeCache);
    }

    @Override
    public String authorize(String state) {
        return Builder.fromUrl(complex.authorize())
                .queryParam("appid", context.getAppKey())
                .queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("state", getRealState(state))
                .queryParam("usertype", context.getType())
                .build();
    }

    @Override
    public Message login(Callback authCallback) {
        try {
            if (!context.isIgnoreState()) {
                Checker.checkState(authCallback.getState(), complex, authorizeCache);
            }
            AccToken accToken = this.getAccessToken(authCallback);
            Property user = this.getUserInfo(accToken);
            return Message.builder().errcode(ErrorCode.SUCCESS.getCode()).data(user).build();
        } catch (Exception e) {
            Logger.error("Failed to login with oauth authorization.", e);
            return this.responseError(e);
        }
    }

    @Override
    protected AccToken getAccessToken(Callback authCallback) {
        try {
            String response = doGetAuthorizationCode(accessTokenUrl());
            JSONObject object = this.checkResponse(response);
            AccToken accToken = AccToken.builder()
                    .accessToken(object.getString("provider_access_token"))
                    .expireIn(object.getIntValue("expires_in"))
                    .build();
            return accToken;
        } catch (Exception e) {
            throw new AuthorizedException("企业微信获取token失败", e);
        }
    }

    @Override
    protected String doGetAuthorizationCode(String code) {
        JSONObject data = new JSONObject();
        data.put("corpid", context.getAppKey());
        data.put("provider_secret", context.getAppSecret());
        return Httpx.post(accessTokenUrl(code), data.toJSONString(), MediaType.APPLICATION_JSON);
    }

    /**
     * 获取token的URL
     *
     * @return accessTokenUrl
     */
    protected String accessTokenUrl() {
        return Builder.fromUrl(complex.accessToken())
                .build();
    }

    @Override
    protected Property getUserInfo(AccToken accToken) {
        JSONObject response = this.checkResponse(doGetUserInfo(accToken));
        return Property.builder()
                .rawJson(response)
                .build();
    }

    @Override
    protected String doGetUserInfo(AccToken accToken) {
        JSONObject data = new JSONObject();
        data.put("auth_code", accToken.getCode());
        return Httpx.post(userInfoUrl(accToken), data.toJSONString(), MediaType.APPLICATION_JSON);
    }

    @Override
    protected String userInfoUrl(AccToken accToken) {
        return Builder.fromUrl(complex.userInfo())
                .queryParam("access_token", accToken.getAccessToken()).
                build();
    }

    private JSONObject checkResponse(String response) {
        JSONObject object = JSONObject.parseObject(response);
        if (object.containsKey("errcode") && object.getIntValue("errcode") != 0) {
            throw new AuthorizedException(object.getString("errmsg"), complex.getName());
        }
        return object;
    }

}
