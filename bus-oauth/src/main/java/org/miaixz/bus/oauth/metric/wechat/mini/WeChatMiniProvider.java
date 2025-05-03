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
package org.miaixz.bus.oauth.metric.wechat.mini;

import lombok.Data;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.Material;
import org.miaixz.bus.oauth.metric.AbstractProvider;

/**
 * 微信小程序授权登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WeChatMiniProvider extends AbstractProvider {

    public WeChatMiniProvider(Context context) {
        super(context, Registry.WECHAT_MINI);
    }

    public WeChatMiniProvider(Context context, ExtendCache cache) {
        super(context, Registry.WECHAT_MINI, cache);
    }

    @Override
    public AccToken getAccessToken(Callback authCallback) {
        // 参见 https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/login/auth.code2Session.html 文档
        // 使用 code 获取对应的 openId、unionId 等字段
        String response = Httpx.get(accessTokenUrl(authCallback.getCode()));
        JSCode2SessionResponse accessTokenObject = JsonKit.toPojo(response, JSCode2SessionResponse.class);
        assert accessTokenObject != null;
        checkResponse(accessTokenObject);
        // 拼装结果
        return AccToken.builder().openId(accessTokenObject.getOpenid()).unionId(accessTokenObject.getUnionid())
                .accessToken(accessTokenObject.getSession_key()).build();
    }

    @Override
    public Material getUserInfo(AccToken AccToken) {
        // 参见 https://developers.weixin.qq.com/miniprogram/dev/api/open-api/user-info/wx.getUserProfile.html 文档
        // 如果需要用户信息，需要在小程序调用函数后传给后端
        return Material.builder().username("").nickname("").avatar("").uuid(AccToken.getOpenId()).token(AccToken)
                .source(complex.toString()).build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param response 请求响应内容
     */
    private void checkResponse(JSCode2SessionResponse response) {
        if (!Symbol.ZERO.equals(response.getErrcode())) {
            throw new AuthorizedException(response.getErrcode(), response.getErrmsg());
        }
    }

    @Override
    protected String accessTokenUrl(String code) {
        return Builder.fromUrl(complex.accessToken()).queryParam("appid", context.getAppKey())
                .queryParam("secret", context.getAppSecret()).queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code").build();
    }

    @Data
    private static class JSCode2SessionResponse {

        private String errcode;
        private String errmsg;
        private String session_key;
        private String openid;
        private String unionid;

    }

}
