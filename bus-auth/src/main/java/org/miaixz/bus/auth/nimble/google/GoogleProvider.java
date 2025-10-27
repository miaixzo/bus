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
package org.miaixz.bus.auth.nimble.google;

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.Authorization;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Material;
import org.miaixz.bus.auth.nimble.AbstractProvider;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;

/**
 * Google login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GoogleProvider extends AbstractProvider {

    /**
     * Constructs a {@code GoogleProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public GoogleProvider(Context context) {
        super(context, Registry.GOOGLE);
    }

    /**
     * Constructs a {@code GoogleProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public GoogleProvider(Context context, CacheX cache) {
        super(context, Registry.GOOGLE, cache);
    }

    /**
     * Retrieves the access token from Google's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link Authorization} containing access token details
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    @Override
    public Message token(Callback callback) {
        String response = doPostToken(callback.getCode());
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse access token response: empty response");
            }
            this.checkResponse(object);

            String token = (String) object.get("access_token");
            if (token == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            Object expiresInObj = object.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;
            String scope = (String) object.get("scope");
            String tokenType = (String) object.get("token_type");
            String idToken = (String) object.get("id_token");
            String refresh_token = (String) object.get("refresh_token");
            int refresh_token_expires_in = (int) object.get("refresh_token_expires_in");

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(
                    Authorization.builder().token(token).expireIn(expiresIn).scope(scope).token_type(tokenType)
                            .refresh(refresh_token).refreshExpireIn(refresh_token_expires_in).idToken(idToken).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
        }
    }

    /**
     * Retrieves user information from Google's user info endpoint.
     *
     * @param authorization the {@link Authorization} obtained after successful authorization
     * @return {@link Material} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Message userInfo(Authorization authorization) {
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", "Bearer " + authorization.getToken());
        String userInfo = Httpx.post(userInfoUrl(authorization), null, header);
        try {
            Map<String, Object> object = JsonKit.toPojo(userInfo, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }
            this.checkResponse(object);

            String sub = (String) object.get("sub");
            if (sub == null) {
                throw new AuthorizedException("Missing sub in user info response");
            }
            String email = (String) object.get("email");
            String picture = (String) object.get("picture");
            String name = (String) object.get("name");
            String locale = (String) object.get("locale");

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                    .data(
                            Material.builder().rawJson(JsonKit.toJsonString(object)).uuid(sub).username(email)
                                    .avatar(picture).nickname(name).location(locale).email(email).gender(Gender.UNKNOWN)
                                    .token(authorization).source(complex.toString()).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
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
                Builder.fromUrl((String) super.build(state).getData()).queryParam("access_type", "offline")
                        .queryParam("scope", this.getScopes(Symbol.SPACE, false, this.getScopes(GoogleScope.values())))
                        .queryParam("prompt", "select_account").build())
                .build();
    }

    /**
     * Returns the URL to obtain user information.
     *
     * @param authorization the user's authorization token
     * @return the URL to obtain user information
     */
    @Override
    protected String userInfoUrl(Authorization authorization) {
        return Builder.fromUrl(this.complex.userinfo()).queryParam("access_token", authorization.getToken()).build();
    }

    /**
     * Checks the response content for errors.
     *
     * @param object the response map to check
     * @throws AuthorizedException if the response contains an error or message indicating failure
     */
    private void checkResponse(Map<String, Object> object) {
        if (object.containsKey("error") || object.containsKey("error_description")) {
            String error = (String) object.get("error");
            String errorDescription = (String) object.get("error_description");
            throw new AuthorizedException((error != null ? error : "Unknown error") + Symbol.COLON
                    + (errorDescription != null ? errorDescription : "Unknown description"));
        }
    }

}
