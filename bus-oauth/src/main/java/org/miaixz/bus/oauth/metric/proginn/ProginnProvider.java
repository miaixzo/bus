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
package org.miaixz.bus.oauth.metric.proginn;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.Gender;
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

import java.util.HashMap;
import java.util.Map;

/**
 * 程序员客栈 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ProginnProvider extends AbstractProvider {

    public ProginnProvider(Context context) {
        super(context, Registry.PROGINN);
    }

    public ProginnProvider(Context context, ExtendCache cache) {
        super(context, Registry.PROGINN, cache);
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        Map<String, String> params = new HashMap<>();
        params.put("code", callback.getCode());
        params.put("client_id", context.getAppKey());
        params.put("client_secret", context.getAppSecret());
        params.put("grant_type", "authorization_code");
        params.put("redirect_uri", context.getRedirectUri());
        String response = Httpx.post(Registry.PROGINN.accessToken(), params);
        Map<String, Object> accessTokenObject = JsonKit.toPojo(response, Map.class);
        this.checkResponse(accessTokenObject);
        return AccToken.builder().accessToken((String) accessTokenObject.get("access_token"))
                .refreshToken((String) accessTokenObject.get("refresh_token"))
                .uid((String) accessTokenObject.get("uid")).tokenType((String) accessTokenObject.get("token_type"))
                .expireIn(((Number) accessTokenObject.get("expires_in")).intValue()).build();
    }

    @Override
    public Material getUserInfo(AccToken accToken) {
        String userInfo = doGetUserInfo(accToken);
        Map<String, Object> object = JsonKit.toPojo(userInfo, Map.class);
        this.checkResponse(object);
        return Material.builder().rawJson(JsonKit.toJsonString(object)).uuid((String) object.get("uid"))
                .username((String) object.get("nickname")).nickname((String) object.get("nickname"))
                .avatar((String) object.get("avatar")).email((String) object.get("email")).gender(Gender.UNKNOWN)
                .token(accToken).source(complex.toString()).build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(Map<String, Object> object) {
        if (object.containsKey("error")) {
            throw new AuthorizedException((String) object.get("error_description"));
        }
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
                .queryParam("scope", this.getScopes(Symbol.SPACE, true, this.getDefaultScopes(ProginnScope.values())))
                .build();
    }
}