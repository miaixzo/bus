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
package org.miaixz.bus.auth.nimble.meituan;

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.auth.magic.Authorization;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Claims;
import org.miaixz.bus.auth.nimble.AbstractProvider;

/**
 * Meituan login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MeituanProvider extends AbstractProvider {

    /**
     * Constructs a {@code MeituanProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public MeituanProvider(Context context) {
        super(context, Registry.MEITUAN);
    }

    /**
     * Constructs a {@code MeituanProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public MeituanProvider(Context context, CacheX cache) {
        super(context, Registry.MEITUAN, cache);
    }

    /**
     * Retrieves the access token from Meituan's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link Authorization} containing access token details
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    @Override
    public Message token(Callback callback) {
        Map<String, String> form = new HashMap<>(7);
        form.put("app_id", context.getClientId());
        form.put("secret", context.getClientSecret());
        form.put("code", callback.getCode());
        form.put("grant_type", "authorization_code");

        String response = Httpx.post(this.complex.token(), form);
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
            String refresh = (String) object.get("refresh_token");
            Object expiresInObj = object.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                    .data(Authorization.builder().token(token).refresh(refresh).expireIn(expiresIn).build()).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
        }
    }

    /**
     * Retrieves user information from Meituan's user info endpoint.
     *
     * @param authorization the {@link Authorization} obtained after successful authorization
     * @return {@link Claims} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Message userInfo(Authorization authorization) {
        Map<String, String> form = new HashMap<>(5);
        form.put("app_id", context.getClientId());
        form.put("secret", context.getClientSecret());
        form.put("access_token", authorization.getToken());

        String response = Httpx.post(this.complex.userinfo(), form);
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }

            this.checkResponse(object);

            String openid = (String) object.get("openid");
            if (openid == null) {
                throw new AuthorizedException("Missing openid in user info response");
            }
            String nickname = (String) object.get("nickname");
            String avatar = (String) object.get("avatar");

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                    .data(
                            Claims.builder().rawJson(JsonKit.toJsonString(object)).uuid(openid).username(nickname)
                                    .nickname(nickname).avatar(avatar).gender(Gender.UNKNOWN).token(authorization)
                                    .source(complex.toString()).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    /**
     * Refreshes the access token (renews its validity).
     *
     * @param authorization the token information returned after successful login
     * @return a {@link Message} containing the refreshed token information
     * @throws AuthorizedException if parsing the response fails or an error occurs during token refresh
     */
    @Override
    public Message refresh(Authorization authorization) {
        Map<String, String> form = new HashMap<>(7);
        form.put("app_id", context.getClientId());
        form.put("secret", context.getClientSecret());
        form.put("refresh_token", authorization.getRefresh());
        form.put("grant_type", "refresh_token");

        String response = Httpx.post(this.complex.refresh(), form);
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse refresh token response: empty response");
            }

            this.checkResponse(object);

            String token = (String) object.get("access_token");
            if (token == null) {
                throw new AuthorizedException("Missing access_token in refresh response");
            }
            String refresh = (String) object.get("refresh_token");
            Object expiresInObj = object.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                    .data(Authorization.builder().token(token).refresh(refresh).expireIn(expiresIn).build()).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse refresh token response: " + e.getMessage());
        }
    }

    /**
     * Checks the response content for errors.
     *
     * @param object the response map to check
     * @throws AuthorizedException if the response indicates an error or message indicating failure
     */
    private void checkResponse(Map<String, Object> object) {
        if (object.containsKey("error_code")) {
            String errorMsg = (String) object.get("erroe_msg"); // Note the typo "erroe_msg" in the original code
            throw new AuthorizedException(errorMsg != null ? errorMsg : "Unknown error");
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
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(Builder.fromUrl((String) super.build(state).getData()).queryParam("scope", "").build()).build();
    }

}
