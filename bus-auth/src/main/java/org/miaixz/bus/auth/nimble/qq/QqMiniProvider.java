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
package org.miaixz.bus.auth.nimble.qq;

import lombok.Data;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.AccToken;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.Material;
import org.miaixz.bus.auth.nimble.AbstractProvider;

import java.util.Map;

/**
 * QQ 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class QqMiniProvider extends AbstractProvider {

    public QqMiniProvider(Context context) {
        super(context, Registry.QQ_MINI);
    }

    public QqMiniProvider(Context context, ExtendCache cache) {
        super(context, Registry.QQ_MINI, cache);
    }

    @Override
    public AccToken getAccessToken(Callback authCallback) {
        // 使用 code 获取对应的 openId、unionId 等字段
        String response = Httpx.get(accessTokenUrl(authCallback.getCode()));

        Map<String, Object> accessTokenObject = JsonKit.toPojo(response, Map.class);
        checkResponse(accessTokenObject);

        // 拼装结果
        return AccToken.builder().openId((String) accessTokenObject.get("openid"))
                .unionId((String) accessTokenObject.get("unionid"))
                .accessToken((String) accessTokenObject.get("session_key")).build();
    }

    @Override
    public Material getUserInfo(AccToken authToken) {
        // 如果需要用户信息，需要在小程序调用函数后传给后端
        return Material.builder().rawJson(JsonKit.toJsonString(authToken)).username("").nickname("").avatar("")
                .uuid(authToken.getOpenId()).token(authToken).source(complex.toString()).build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param response 请求响应内容
     */
    private void checkResponse(Map<String, Object> response) {
        if (!Symbol.ZERO.equals(response.get("errcode"))) {
            throw new AuthorizedException((String) response.get("errmsg"));
        }
    }

    @Override
    protected String accessTokenUrl(String code) {
        return Builder.fromUrl(this.complex.getConfig().get(Builder.ACCESSTOKEN))
                .queryParam("appid", context.getAppKey()).queryParam("secret", context.getAppSecret())
                .queryParam("js_code", code).queryParam("grant_type", "authorization_code").build();
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