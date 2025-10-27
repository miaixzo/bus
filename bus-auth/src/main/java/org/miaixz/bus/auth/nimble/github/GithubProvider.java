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

import org.miaixz.bus.auth.magic.Authorization;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.Material;
import org.miaixz.bus.auth.nimble.AbstractProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Github login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GithubProvider extends AbstractProvider {

    /**
     * Constructs a {@code GithubProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public GithubProvider(Context context) {
        super(context, Registry.GITHUB);
    }

    /**
     * Constructs a {@code GithubProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public GithubProvider(Context context, CacheX cache) {
        super(context, Registry.GITHUB, cache);
    }

    /**
     * Retrieves the access token from GitHub's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link Authorization} containing access token details
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    @Override
    public Message token(Callback callback) {
        String response = doGetToken(callback.getCode());
        Map<String, String> res = Builder.parseStringToMap(response);

        this.checkResponse(res.containsKey("error"), res.get("error_description"));

        String token = res.get("access_token");
        if (token == null) {
            throw new AuthorizedException("Missing access_token in response");
        }

        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(
                Authorization.builder().token(token).scope(res.get("scope")).token_type(res.get("token_type")).build())
                .build();
    }

    /**
     * Retrieves user information from GitHub's user info endpoint.
     *
     * @param authorization the {@link Authorization} obtained after successful authorization
     * @return {@link Material} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Message userInfo(Authorization authorization) {
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", "token " + authorization.getToken());
        String response = Httpx.get(Builder.fromUrl(this.complex.userinfo()).build(), null, header);
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

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(
                    Material.builder().rawJson(JsonKit.toJsonString(object)).uuid(id).username(login).avatar(avatarUrl)
                            .blog(blog).nickname(name).company(company).location(location).email(email).remark(bio)
                            .gender(Gender.UNKNOWN).token(authorization).source(complex.toString()).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    /**
     * Checks the response for errors and throws an {@link AuthorizedException} if an error is present.
     *
     * @param error            a boolean indicating if an error occurred
     * @param errorDescription the description of the error, if any
     * @throws AuthorizedException if an error is present
     */
    private void checkResponse(boolean error, String errorDescription) {
        if (error) {
            throw new AuthorizedException(errorDescription != null ? errorDescription : "Unknown error");
        }
    }

    /**
     * Returns the authorization URL with a {@code state} parameter. The {@code state} will be included in the
     * authorization callback.
     *
     * @param state the parameter to verify the authorization process, which can prevent CSRF attacks
     * @return the authorization URL
     */
    @Override
    public Message build(String state) {
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(
                Builder.fromUrl((String) super.build(state).getData())
                        .queryParam("scope", this.getScopes(Symbol.SPACE, true, this.getScopes(GithubScope.values())))
                        .build())
                .build();
    }

}
