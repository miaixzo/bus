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
package org.miaixz.bus.auth.nimble.kujiale;

import org.miaixz.bus.auth.magic.AuthToken;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Material;
import org.miaixz.bus.auth.nimble.AbstractProvider;

import java.util.Map;

/**
 * Kujiale login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class KujialeProvider extends AbstractProvider {

    /**
     * Constructs a {@code KujialeProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public KujialeProvider(Context context) {
        super(context, Registry.KUJIALE);
    }

    /**
     * Constructs a {@code KujialeProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public KujialeProvider(Context context, CacheX cache) {
        super(context, Registry.KUJIALE, cache);
    }

    /**
     * Returns the authorization URL with a {@code state} parameter. The {@code state} will be included in the
     * authorization callback. By default, only user information authorization is requested.
     *
     * @param state the parameter to verify the authorization process, which can prevent CSRF attacks
     * @return the authorization URL
     */
    @Override
    public String authorize(String state) {
        return Builder.fromUrl(super.authorize(state))
                .queryParam("scope", this.getScopes(Symbol.COMMA, false, this.getDefaultScopes(KujialeScope.values())))
                .build();
    }

    /**
     * Retrieves the access token from Kujiale's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link AuthToken} containing access token details
     */
    @Override
    public AuthToken getAccessToken(Callback callback) {
        String response = doPostAuthorizationCode(callback.getCode());
        return getAuthToken(response);
    }

    /**
     * Parses the access token response string into an {@link AuthToken} object.
     *
     * @param response the response string from the access token endpoint
     * @return the parsed {@link AuthToken}
     * @throws AuthorizedException if the response indicates an error or is missing required token information
     */
    private AuthToken getAuthToken(String response) {
        Map<String, Object> accessTokenObject = checkResponse(response);
        Map<String, Object> resultObject = (Map<String, Object>) accessTokenObject.get("d");
        if (resultObject == null) {
            throw new AuthorizedException("Missing d in access token response");
        }

        String accessToken = (String) resultObject.get("accessToken");
        if (accessToken == null) {
            throw new AuthorizedException("Missing accessToken in response");
        }
        String refreshToken = (String) resultObject.get("refreshToken");
        Object expiresInObj = resultObject.get("expiresIn");
        int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;

        return AuthToken.builder().accessToken(accessToken).refreshToken(refreshToken).expireIn(expiresIn).build();
    }

    /**
     * Checks the response content for errors.
     *
     * @param response the response string to check
     * @return the parsed response map if successful
     * @throws AuthorizedException if the response indicates an error or is malformed
     */
    private Map<String, Object> checkResponse(String response) {
        try {
            Map<String, Object> accessTokenObject = JsonKit.toPojo(response, Map.class);
            if (accessTokenObject == null) {
                throw new AuthorizedException("Failed to parse response: empty response");
            }
            String code = (String) accessTokenObject.get("c");
            if (!"0".equals(code)) {
                String message = (String) accessTokenObject.get("m");
                throw new AuthorizedException(message != null ? message : "Unknown error");
            }
            return accessTokenObject;
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse response: " + e.getMessage());
        }
    }

    /**
     * Retrieves user information from Kujiale's user info endpoint.
     *
     * @param authToken the {@link AuthToken} obtained after successful authorization
     * @return {@link Material} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Material getUserInfo(AuthToken authToken) {
        String openId = this.getOpenId(authToken);
        String response = Httpx.get(
                Builder.fromUrl(this.complex.userinfo()).queryParam("access_token", authToken.getAccessToken())
                        .queryParam("open_id", openId).build());
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }
            String code = (String) object.get("c");
            if (!"0".equals(code)) {
                String message = (String) object.get("m");
                throw new AuthorizedException(message != null ? message : "Unknown error");
            }

            Map<String, Object> resultObject = (Map<String, Object>) object.get("d");
            if (resultObject == null) {
                throw new AuthorizedException("Missing d in user info response");
            }

            String userName = (String) resultObject.get("userName");
            if (userName == null) {
                throw new AuthorizedException("Missing userName in user info response");
            }
            String avatar = (String) resultObject.get("avatar");
            String openIdFromResponse = (String) resultObject.get("openId");

            return Material.builder().rawJson(JsonKit.toJsonString(resultObject)).username(userName).nickname(userName)
                    .avatar(avatar).uuid(openIdFromResponse).token(authToken).source(complex.toString()).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    /**
     * Retrieves the Kujiale OpenId. This ID can uniquely identify the authorized user within the current client scope.
     *
     * @param authToken the {@code accessToken} obtained via {@link KujialeProvider#getAccessToken(Callback)}
     * @return the OpenId
     * @throws AuthorizedException if the response indicates an error or is missing the OpenId
     */
    private String getOpenId(AuthToken authToken) {
        String response = Httpx.get(
                Builder.fromUrl("https://oauth.kujiale.com/oauth2/auth/user")
                        .queryParam("access_token", authToken.getAccessToken()).build());
        Map<String, Object> accessTokenObject = checkResponse(response);
        String openId = (String) accessTokenObject.get("d");
        if (openId == null) {
            throw new AuthorizedException("Missing openId in response");
        }
        return openId;
    }

    /**
     * Refreshes the access token (renews its validity).
     *
     * @param authToken the token information returned after successful login
     * @return a {@link Message} containing the refreshed token information
     */
    @Override
    public Message refresh(AuthToken authToken) {
        String response = Httpx.post(refreshTokenUrl(authToken.getRefreshToken()));
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(getAuthToken(response)).build();
    }

}
