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
package org.miaixz.bus.auth.nimble.coding;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.AuthToken;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.Material;
import org.miaixz.bus.auth.nimble.AbstractProvider;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;

import java.util.Map;

/**
 * Coding login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CodingProvider extends AbstractProvider {

    /**
     * Constructs a {@code CodingProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public CodingProvider(Context context) {
        super(context, Registry.CODING);
    }

    /**
     * Constructs a {@code CodingProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public CodingProvider(Context context, CacheX cache) {
        super(context, Registry.CODING, cache);
    }

    /**
     * Retrieves the access token from Coding's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link AuthToken} containing access token details
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    @Override
    public AuthToken getAccessToken(Callback callback) {
        String response = doPostAuthorizationCode(callback.getCode());
        try {
            Map<String, Object> accessTokenObject = JsonKit.toPojo(response, Map.class);
            if (accessTokenObject == null) {
                throw new AuthorizedException("Failed to parse access token response: empty response");
            }
            this.checkResponse(accessTokenObject);

            String accessToken = (String) accessTokenObject.get("access_token");
            if (accessToken == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            Object expiresInObj = accessTokenObject.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;
            String refreshToken = (String) accessTokenObject.get("refresh_token");

            return AuthToken.builder().accessToken(accessToken).expireIn(expiresIn).refreshToken(refreshToken).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
        }
    }

    /**
     * Retrieves user information from Coding's user info endpoint.
     *
     * @param authToken the {@link AuthToken} obtained after successful authorization
     * @return {@link Material} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Material getUserInfo(AuthToken authToken) {
        String response = doGetUserInfo(authToken);
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }
            this.checkResponse(object);

            Map<String, Object> data = (Map<String, Object>) object.get(Consts.DATA);
            if (data == null) {
                throw new AuthorizedException("Missing data field in user info response");
            }

            String id = String.valueOf(data.get("id"));
            if (id == null) {
                throw new AuthorizedException("Missing id in user info response");
            }
            String name = (String) data.get("name");
            String avatar = (String) data.get("avatar");
            String path = (String) data.get("path");
            String company = (String) data.get("company");
            String location = (String) data.get("location");
            String sex = (String) data.get("sex");
            String email = (String) data.get("email");
            String slogan = (String) data.get("slogan");

            return Material.builder().rawJson(JsonKit.toJsonString(data)).uuid(id).username(name)
                    .avatar(avatar != null ? "https://coding.net" + avatar : null)
                    .blog(path != null ? "https://coding.net" + path : null).nickname(name).company(company)
                    .location(location).gender(Gender.of(sex)).email(email).remark(slogan).token(authToken)
                    .source(complex.toString()).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    /**
     * Checks the response content for errors.
     *
     * @param object the response map to check
     * @throws AuthorizedException if the response indicates an error or message indicating failure
     */
    private void checkResponse(Map<String, Object> object) {
        if ((int) object.get("code") != 0) {
            throw new AuthorizedException((String) object.get("msg"));
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
    public String authorize(String state) {
        return Builder.fromUrl(String.format(this.complex.authorize(), this.context.getPrefix()))
                .queryParam("response_type", "code").queryParam("client_id", this.context.getAppKey())
                .queryParam("redirect_uri", this.context.getRedirectUri())
                .queryParam("scope", this.getScopes(Symbol.SPACE, true, this.getDefaultScopes(CodingScope.values())))
                .queryParam("state", getRealState(state)).build();
    }

    /**
     * Returns the URL to obtain the access token.
     *
     * @param code the authorization code
     * @return the URL to obtain the access token
     */
    @Override
    public String accessTokenUrl(String code) {
        return Builder.fromUrl(String.format(this.complex.accessToken(), this.context.getPrefix()))
                .queryParam("code", code).queryParam("client_id", this.context.getAppKey())
                .queryParam("client_secret", this.context.getAppSecret()).queryParam("grant_type", "authorization_code")
                .queryParam("redirect_uri", this.context.getRedirectUri()).build();
    }

    /**
     * Returns the URL to obtain user information.
     *
     * @param authToken the user's authorization token
     * @return the URL to obtain user information
     */
    @Override
    public String userInfoUrl(AuthToken authToken) {
        return Builder.fromUrl(String.format(this.complex.userinfo(), this.context.getPrefix()))
                .queryParam("access_token", authToken.getAccessToken()).build();
    }

}
