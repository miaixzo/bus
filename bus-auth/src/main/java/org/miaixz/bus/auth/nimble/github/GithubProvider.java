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
package org.miaixz.bus.auth.nimble.github;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.Gender;
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

import java.util.HashMap;
import java.util.Map;

/**
 * Github 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GithubProvider extends AbstractProvider {

    public GithubProvider(Context context) {
        super(context, Registry.GITHUB);
    }

    public GithubProvider(Context context, ExtendCache cache) {
        super(context, Registry.GITHUB, cache);
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        String response = doPostAuthorizationCode(callback.getCode());
        Map<String, String> res = Builder.parseStringToMap(response);

        this.checkResponse(res.containsKey("error"), res.get("error_description"));

        String accessToken = res.get("access_token");
        if (accessToken == null) {
            throw new AuthorizedException("Missing access_token in response");
        }

        return AccToken.builder().accessToken(accessToken).scope(res.get("scope")).tokenType(res.get("token_type"))
                .build();
    }

    @Override
    public Material getUserInfo(AccToken accToken) {
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", "token " + accToken.getAccessToken());
        String response = Httpx.get(Builder.fromUrl(this.complex.getConfig().get(Builder.USERINFO)).build(), null,
                header);
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }

            this.checkResponse(object.containsKey("error"), (String) object.get("error_description"));

            String id = (String) object.get("id");
            if (id == null) {
                throw new AuthorizedException("Missing id in user info response");
            }
            String login = (String) object.get("login");
            String avatarUrl = (String) object.get("avatar_url");
            String blog = (String) object.get("blog");
            String name = (String) object.get("name");
            String company = (String) object.get("company");
            String location = (String) object.get("location");
            String email = (String) object.get("email");
            String bio = (String) object.get("bio");

            return Material.builder().rawJson(JsonKit.toJsonString(object)).uuid(id).username(login).avatar(avatarUrl)
                    .blog(blog).nickname(name).company(company).location(location).email(email).remark(bio)
                    .gender(Gender.UNKNOWN).token(accToken).source(complex.toString()).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    private void checkResponse(boolean error, String errorDescription) {
        if (error) {
            throw new AuthorizedException(errorDescription != null ? errorDescription : "Unknown error");
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
                .queryParam("scope", this.getScopes(Symbol.SPACE, true, this.getDefaultScopes(GithubScope.values())))
                .build();
    }

}