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
package org.miaixz.bus.auth.nimble.rednote;

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.AuthToken;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Material;
import org.miaixz.bus.auth.nimble.AbstractProvider;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;

/**
 * 小红书商业平台
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RednoteMarketiProvider extends AbstractProvider {

    public RednoteMarketiProvider(Context context) {
        super(context, Registry.REDNOTE_MARKET);
    }

    public RednoteMarketiProvider(Context context, CacheX cache) {
        super(context, Registry.REDNOTE_MARKET, cache);
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     */
    @Override
    public String authorize(String state) {
        return Builder.fromUrl(this.complex.authorize()).queryParam("appId", this.context.getAppKey())
                .queryParam("scope", this.getScopes(" ", true, getDefaultScopes(RednoteMarketiScope.values())))
                .queryParam("redirectUri", this.context.getRedirectUri()).queryParam("state", getRealState(state))
                .build();
    }

    @Override
    public AuthToken getAccessToken(Callback callback) {
        Map<String, String> form = new HashMap<>(7);
        form.put("app_id", this.context.getAppKey());
        form.put("secret", this.context.getAppSecret());
        form.put("code", callback.getCode());
        String response = Httpx.post(this.complex.accessToken(), form);
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse access token response: empty response");
            }
            this.checkResponse(object);
            String accessToken = (String) object.get("access_token");
            if (accessToken == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            return AuthToken.builder().accessToken((String) object.get("access_token"))
                    .expireIn((Integer) object.get("access_token_expires_in"))
                    .refreshToken((String) object.get("refresh_token")).scope((String) object.get("scope")).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
        }
    }

    @Override
    public Material getUserInfo(AuthToken authToken) {
        throw new UnsupportedOperationException("不支持获取用户信息 url");
    }

    @Override
    public Message refresh(AuthToken authToken) {
        Map<String, String> form = new HashMap<>(7);
        form.put("app_id", this.context.getAppKey());
        form.put("secret", this.context.getAppSecret());
        form.put("refresh_token", authToken.getRefreshToken());

        String response = Httpx.post(this.complex.refresh(), form);
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse access token response: empty response");
            }
            this.checkResponse(object);
            String accessToken = (String) object.get("access_token");
            if (accessToken == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(
                    AuthToken.builder().accessToken((String) object.get("access_token"))
                            .refreshToken((String) object.get("refresh_token")).scope((String) object.get("scope"))
                            .expireIn((Integer) object.get("expires_in")).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
        }
    }

    /**
     * 校验响应结果
     *
     * @param object 接口返回的结果
     */
    private void checkResponse(Map<String, Object> object) {
        if ((Integer) object.get("code") != 0) {
            String error = (String) object.get("error");
            throw new AuthorizedException(error != null ? error : "Unknown error");
        }
        if (object.containsKey("error")) {
            String subError = (String) object.get("sub_error");
            String errorDescription = (String) object.get("error_description");
            throw new AuthorizedException((subError != null ? subError : "Unknown sub_error") + ":"
                    + (errorDescription != null ? errorDescription : "Unknown description"));
        }
    }

}
