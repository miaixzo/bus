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
package org.miaixz.bus.auth.nimble.gitlab;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.AccToken;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.Material;
import org.miaixz.bus.auth.nimble.AbstractProvider;

import java.util.Map;

/**
 * Gitlab 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GitlabProvider extends AbstractProvider {

    public GitlabProvider(Context context) {
        super(context, Registry.GITLAB);
    }

    public GitlabProvider(Context context, ExtendCache cache) {
        super(context, Registry.GITLAB, cache);
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        String response = doPostAuthorizationCode(callback.getCode());
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
            String refreshToken = (String) object.get("refresh_token");
            String idToken = (String) object.get("id_token");
            String tokenType = (String) object.get("token_type");
            String scope = (String) object.get("scope");

            return AccToken.builder().accessToken(accessToken).refreshToken(refreshToken).idToken(idToken)
                    .tokenType(tokenType).scope(scope).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
        }
    }

    @Override
    public Material getUserInfo(AccToken accToken) {
        String response = doGetUserInfo(accToken);
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }
            this.checkResponse(object);

            String id = (String) object.get("id");
            if (id == null) {
                throw new AuthorizedException("Missing id in user info response");
            }
            String username = (String) object.get("username");
            String name = (String) object.get("name");
            String avatarUrl = (String) object.get("avatar_url");
            String webUrl = (String) object.get("web_url");
            String organization = (String) object.get("organization");
            String location = (String) object.get("location");
            String email = (String) object.get("email");
            String bio = (String) object.get("bio");

            return Material.builder().rawJson(JsonKit.toJsonString(object)).uuid(id).username(username).nickname(name)
                    .avatar(avatarUrl).blog(webUrl).company(organization).location(location).email(email).remark(bio)
                    .gender(Gender.UNKNOWN).token(accToken).source(complex.toString()).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    private void checkResponse(Map<String, Object> object) {
        // auth/token 验证异常
        if (object.containsKey("error")) {
            String errorDescription = (String) object.get("error_description");
            throw new AuthorizedException(errorDescription != null ? errorDescription : "Unknown error");
        }
        // user 验证异常
        if (object.containsKey("message")) {
            String message = (String) object.get("message");
            throw new AuthorizedException(message != null ? message : "Unknown error");
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
                .queryParam("scope", this.getScopes(Symbol.PLUS, false, this.getDefaultScopes(GitlabScope.values())))
                .build();
    }

}