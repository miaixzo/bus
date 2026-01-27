/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.auth.nimble.figma;

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.auth.magic.Authorization;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.HTTP;
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
 * Figma login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FigmaProvider extends AbstractProvider {

    /**
     * Constructs a {@code FigmaProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public FigmaProvider(Context context) {
        super(context, Registry.FIGMA);
    }

    /**
     * Constructs a {@code FigmaProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public FigmaProvider(Context context, CacheX cache) {
        super(context, Registry.FIGMA, cache);
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
                .data(
                        Builder.fromUrl((String) super.build(state).getData())
                                .queryParam("scope", this.getScopes(Symbol.COMMA, true, getScopes(FigmaScope.values())))
                                .build())
                .build();
    }

    /**
     * Retrieves the access token from Figma's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link Authorization} containing access token details
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    @Override
    public Message token(Callback callback) {
        Map<String, String> headers = new HashMap<>(3);
        headers.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        headers.put(
                HTTP.AUTHORIZATION,
                "Basic ".concat(
                        Base64.encode(
                                (this.context.getClientId().concat(":").concat(this.context.getClientSecret()))
                                        .getBytes(Charset.UTF_8))));
        String response = Httpx.post(super.tokenUrl(callback.getCode()), headers);
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
            String scope = (String) object.get("scope");
            String userId = (String) object.get("user_id");
            Object expiresInObj = object.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                    .data(
                            Authorization.builder().token(token).refresh(refresh).scope(scope).userId(userId)
                                    .expireIn(expiresIn).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
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
        Map<String, String> headers = new HashMap<>(3);
        headers.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        String response = Httpx.post(refreshUrl(authorization.getRefresh()), headers);
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
            String openId = (String) object.get("open_id");
            Object expiresInObj = object.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;
            String refresh = (String) object.get("refresh_token");
            String scope = (String) object.get("scope");

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                    .data(
                            Authorization.builder().token(token).openId(openId).expireIn(expiresIn).refresh(refresh)
                                    .scope(scope).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse refresh token response: " + e.getMessage());
        }
    }

    /**
     * Constructs the refresh token URL for Figma.
     *
     * @param refresh the refresh token
     * @return the refresh token URL
     */
    @Override
    protected String refreshUrl(String refresh) {
        return Builder.fromUrl(this.complex.refresh()).queryParam("client_id", context.getClientId())
                .queryParam("client_secret", context.getClientSecret()).queryParam("refresh_token", refresh).build();
    }

    /**
     * Retrieves user information from Figma's user info endpoint.
     *
     * @param authorization the {@link Authorization} obtained after successful authorization
     * @return {@link Claims} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Message userInfo(Authorization authorization) {
        Map<String, String> headers = new HashMap<>(3);
        headers.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        headers.put(HTTP.AUTHORIZATION, HTTP.BEARER + authorization.getToken());
        String response = Httpx.get(this.complex.userinfo(), null, headers);
        try {
            Map<String, Object> data = JsonKit.toPojo(response, Map.class);
            if (data == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }
            this.checkResponse(data);

            String id = (String) data.get("id");
            if (id == null) {
                throw new AuthorizedException("Missing id in user info response");
            }
            String handle = (String) data.get("handle");
            String imgUrl = (String) data.get("img_url");
            String email = (String) data.get("email");

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(
                    Claims.builder().rawJson(JsonKit.toJsonString(data)).uuid(id).username(handle).avatar(imgUrl)
                            .email(email).token(authorization).source(complex.toString()).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    /**
     * Checks the response content for errors.
     *
     * @param object the response map to check
     * @throws AuthorizedException if the response contains an error or message indicating failure
     */
    private void checkResponse(Map<String, Object> object) {
        if (object.containsKey("error")) {
            String error = (String) object.get("error");
            String message = (String) object.get("message");
            throw new AuthorizedException(
                    (error != null ? error : "Unknown error") + ":" + (message != null ? message : "Unknown message"));
        }
    }

}
