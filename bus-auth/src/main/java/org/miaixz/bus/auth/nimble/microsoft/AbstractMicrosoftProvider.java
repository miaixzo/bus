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
package org.miaixz.bus.auth.nimble.microsoft;

import org.miaixz.bus.auth.magic.AuthToken;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.url.UrlDecoder;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Complex;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Material;
import org.miaixz.bus.auth.nimble.AbstractProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class for Microsoft login providers. This class handles login methods for both Microsoft International and
 * Microsoft China accounts to third-party websites.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractMicrosoftProvider extends AbstractProvider {

    /**
     * Constructs an {@code AbstractMicrosoftProvider} with the specified context and complex configuration.
     *
     * @param context the authentication context
     * @param complex the complex configuration for Microsoft
     */
    public AbstractMicrosoftProvider(Context context, Complex complex) {
        super(context, complex);
    }

    /**
     * Constructs an {@code AbstractMicrosoftProvider} with the specified context, complex configuration, and cache.
     *
     * @param context the authentication context
     * @param complex the complex configuration for Microsoft
     * @param cache   the cache implementation
     */
    public AbstractMicrosoftProvider(Context context, Complex complex, CacheX cache) {
        super(context, complex, cache);
    }

    /**
     * Retrieves the access token from Microsoft's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link AuthToken} containing access token details
     */
    @Override
    public AuthToken getAccessToken(Callback callback) {
        return getToken(accessTokenUrl(callback.getCode()));
    }

    /**
     * Retrieves the token, applicable for both obtaining access tokens and refreshing tokens.
     *
     * @param accessTokenUrl the actual URL to request the token from
     * @return the {@link AuthToken} object
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    private AuthToken getToken(String accessTokenUrl) {
        Map<String, String> form = new HashMap<>();
        UrlDecoder.decodeMap(accessTokenUrl, Charset.DEFAULT_UTF_8).forEach(form::put);

        String response = Httpx.post(accessTokenUrl, form);
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
            String scope = (String) accessTokenObject.get("scope");
            String tokenType = (String) accessTokenObject.get("token_type");
            String refreshToken = (String) accessTokenObject.get("refresh_token");

            return AuthToken.builder().accessToken(accessToken).expireIn(expiresIn).scope(scope).tokenType(tokenType)
                    .refreshToken(refreshToken).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
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
            String errorDescription = (String) object.get("error_description");
            throw new AuthorizedException(errorDescription != null ? errorDescription : "Unknown error");
        }
    }

    /**
     * Retrieves user information from Microsoft's user info endpoint.
     *
     * @param authToken the {@link AuthToken} obtained after successful authorization
     * @return {@link Material} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Material getUserInfo(AuthToken authToken) {
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", authToken.getTokenType() + Symbol.SPACE + authToken.getAccessToken());

        String userInfo = Httpx.get(userInfoUrl(authToken), null, header);
        try {
            Map<String, Object> object = JsonKit.toPojo(userInfo, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }

            this.checkResponse(object);

            String id = (String) object.get("id");
            if (id == null) {
                throw new AuthorizedException("Missing id in user info response");
            }
            String userPrincipalName = (String) object.get("userPrincipalName");
            String displayName = (String) object.get("displayName");
            String officeLocation = (String) object.get("officeLocation");
            String mail = (String) object.get("mail");

            return Material.builder().rawJson(JsonKit.toJsonString(object)).uuid(id).username(userPrincipalName)
                    .nickname(displayName).location(officeLocation).email(mail).gender(Gender.UNKNOWN).token(authToken)
                    .source(complex.toString()).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    /**
     * Refreshes the access token (renews its validity).
     *
     * @param authToken the token information returned after successful login
     * @return a {@link Message} containing the refreshed token information
     */
    @Override
    public Message refresh(AuthToken authToken) {
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(getToken(refreshTokenUrl(authToken.getRefreshToken()))).build();
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
        // Compatible with Microsoft Entra ID login (formerly Microsoft AAD)
        String tenantId = StringKit.isEmpty(context.getUnionId()) ? "common" : context.getUnionId();
        return Builder.fromUrl(String.format(complex.authorize(), tenantId)).queryParam("response_type", "code")
                .queryParam("client_id", context.getAppKey()).queryParam("redirect_uri", context.getRedirectUri())
                .queryParam("state", getRealState(state)).queryParam("response_mode", "query")
                .queryParam(
                        "scope",
                        this.getScopes(Symbol.SPACE, false, this.getDefaultScopes(MicrosoftScope.values())))
                .build();
    }

    /**
     * Returns the URL to obtain the access token.
     *
     * @param code the authorization code
     * @return the URL to obtain the access token
     */
    @Override
    protected String accessTokenUrl(String code) {
        String tenantId = StringKit.isEmpty(context.getUnionId()) ? "common" : context.getUnionId();
        return Builder.fromUrl(String.format(this.complex.accessToken(), tenantId)).queryParam("code", code)
                .queryParam("client_id", context.getAppKey()).queryParam("client_secret", context.getAppSecret())
                .queryParam("grant_type", "authorization_code")
                .queryParam(
                        "scope",
                        this.getScopes(Symbol.SPACE, false, this.getDefaultScopes(MicrosoftScope.values())))
                .queryParam("redirect_uri", context.getRedirectUri()).build();
    }

    /**
     * Returns the URL to obtain user information.
     *
     * @param authToken the user's authorization token
     * @return the URL to obtain user information
     */
    @Override
    protected String userInfoUrl(AuthToken authToken) {
        return Builder.fromUrl(this.complex.userinfo()).build();
    }

    /**
     * Returns the URL to refresh the access token.
     *
     * @param refreshToken the user's refresh token
     * @return the URL to refresh the access token
     */
    @Override
    protected String refreshTokenUrl(String refreshToken) {
        String tenantId = StringKit.isEmpty(context.getUnionId()) ? "common" : context.getUnionId();
        return Builder.fromUrl(String.format(this.complex.refresh(), tenantId))
                .queryParam("client_id", context.getAppKey()).queryParam("client_secret", context.getAppSecret())
                .queryParam("refresh_token", refreshToken).queryParam("grant_type", "refresh_token")
                .queryParam(
                        "scope",
                        this.getScopes(Symbol.SPACE, false, this.getDefaultScopes(MicrosoftScope.values())))
                .queryParam("redirect_uri", context.getRedirectUri()).build();
    }

}
