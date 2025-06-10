/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.auth.nimble.wechat.ee;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Checker;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.AccToken;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Material;

import java.util.HashMap;
import java.util.Map;

/**
 * 企业微信 第三方二维码登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WeChatEeThirdQrcodeProvider extends AbstractWeChatEeProvider {

    public WeChatEeThirdQrcodeProvider(Context context) {
        super(context, Registry.WECHAT_EE_QRCODE);
    }

    public WeChatEeThirdQrcodeProvider(Context context, ExtendCache cache) {
        super(context, Registry.WECHAT_EE_QRCODE, cache);
    }

    @Override
    public String authorize(String state) {
        return Builder.fromUrl(complex.getConfig().get(Builder.AUTHORIZE)).queryParam("appid", context.getAppKey())
                .queryParam("redirect_uri", context.getRedirectUri()).queryParam("state", getRealState(state))
                .queryParam("usertype", context.getType()).build();
    }

    @Override
    public Message login(Callback callback) {
        try {
            if (!context.isIgnoreState()) {
                Checker.check(callback.getState(), complex, cache);
            }
            AccToken accToken = this.getAccessToken(callback);
            Material user = this.getUserInfo(accToken);
            return Message.builder().errcode(ErrorCode.SUCCESS.getCode()).data(user).build();
        } catch (Exception e) {
            Logger.error("Failed to login with auth authorization.", e);
            return this.responseError(e);
        }
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        try {
            String response = doGetAuthorizationCode(accessTokenUrl());
            Map<String, Object> object = this.checkResponse(response);
            String accessToken = (String) object.get("provider_access_token");
            if (accessToken == null) {
                throw new AuthorizedException("Missing provider_access_token in response");
            }
            Object expiresInObj = object.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;

            return AccToken.builder().accessToken(accessToken).expireIn(expiresIn).build();
        } catch (Exception e) {
            throw new AuthorizedException("企业微信获取token失败", e);
        }
    }

    @Override
    public String doGetAuthorizationCode(String code) {
        Map<String, Object> data = new HashMap<>();
        data.put("corpid", context.getAppKey());
        data.put("provider_secret", context.getAppSecret());
        return Httpx.post(accessTokenUrl(code), JsonKit.toJsonString(data), MediaType.APPLICATION_JSON);
    }

    /**
     * 获取token的URL
     *
     * @return accessTokenUrl
     */
    public String accessTokenUrl() {
        return Builder.fromUrl(this.complex.getConfig().get(Builder.ACCESSTOKEN)).build();
    }

    @Override
    public Material getUserInfo(AccToken accToken) {
        Map<String, Object> response = this.checkResponse(doGetUserInfo(accToken));
        return Material.builder().rawJson(JsonKit.toJsonString(response)).build();
    }

    @Override
    public String doGetUserInfo(AccToken accToken) {
        Map<String, Object> data = new HashMap<>();
        data.put("auth_code", accToken.getCode());
        return Httpx.post(userInfoUrl(accToken), JsonKit.toJsonString(data), MediaType.APPLICATION_JSON);
    }

    @Override
    public String userInfoUrl(AccToken accToken) {
        return Builder.fromUrl(this.complex.getConfig().get(Builder.USERINFO))
                .queryParam("access_token", accToken.getAccessToken()).build();
    }

    private Map<String, Object> checkResponse(String response) {
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse response: empty response");
            }
            Object errcodeObj = object.get("errcode");
            if (errcodeObj != null && !errcodeObj.equals(0)) {
                String errmsg = (String) object.get("errmsg");
                throw new AuthorizedException(errmsg != null ? errmsg : "Unknown error", complex.getName());
            }
            return object;
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse response: " + e.getMessage());
        }
    }

}