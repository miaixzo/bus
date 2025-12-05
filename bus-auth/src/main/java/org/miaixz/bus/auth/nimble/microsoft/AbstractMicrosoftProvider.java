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

import org.miaixz.bus.auth.magic.Authorization;
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
import org.miaixz.bus.auth.magic.Claims;
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
     * @return the {@link Authorization} containing access token details
     */
    @Override
    public Message token(Callback callback) {
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(getToken(tokenUrl(callback.getCode())))
                .build();
    }

    /**
     * Retrieves the token, applicable for both obtaining access tokens and refreshing tokens.
     *
     * @param tokenUrl the actual URL to request the token from
     * @return the {@link Authorization} object
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    private Authorization getToken(String tokenUrl) {
        Map<String, String> form = new HashMap<>();
        UrlDecoder.decodeMap(tokenUrl, Charset.DEFAULT_UTF_8).forEach(form::put);

        String response = Httpx.post(tokenUrl, form);
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
            String refresh = (String) object.get("refresh_token");

            return Authorization.builder().token(token).expireIn(expiresIn).scope(scope).token_type(tokenType)
                    .refresh(refresh).build();
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
     * @param authorization the {@link Authorization} obtained after successful authorization
     * @return {@link Claims} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Message userInfo(Authorization authorization) {
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", authorization.getToken_type() + Symbol.SPACE + authorization.getToken());

        String userInfo = Httpx.get(userInfoUrl(authorization), null, header);
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

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                    .data(
                            Claims.builder().rawJson(JsonKit.toJsonString(object)).uuid(id).username(userPrincipalName)
                                    .nickname(displayName).location(officeLocation).email(mail).gender(Gender.UNKNOWN)
                                    .token(authorization).source(complex.toString()).build())
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
     */
    @Override
    public Message refresh(Authorization authorization) {
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(getToken(refreshUrl(authorization.getRefresh()))).build();
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
        // Compatible with Microsoft Entra ID login (formerly Microsoft AAD)
        String tenantId = StringKit.isEmpty(context.getUnionId()) ? "common" : context.getUnionId();
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(
                Builder.fromUrl(String.format(complex.authorize(), tenantId)).queryParam("response_type", "code")
                        .queryParam("client_id", context.getClientId())
                        .queryParam("redirect_uri", context.getRedirectUri()).queryParam("state", getRealState(state))
                        .queryParam("response_mode", "query")
                        .queryParam(
                                "scope",
                                this.getScopes(Symbol.SPACE, false, this.getScopes(MicrosoftScope.values())))
                        .build())
                .build();
    }

    /**
     * Returns the URL to obtain the access token.
     *
     * @param code the authorization code
     * @return the URL to obtain the access token
     */
    @Override
    protected String tokenUrl(String code) {
        String tenantId = StringKit.isEmpty(context.getUnionId()) ? "common" : context.getUnionId();
        return Builder.fromUrl(String.format(this.complex.token(), tenantId)).queryParam("code", code)
                .queryParam("client_id", context.getClientId()).queryParam("client_secret", context.getClientSecret())
                .queryParam("grant_type", "authorization_code")
                .queryParam("scope", this.getScopes(Symbol.SPACE, false, this.getScopes(MicrosoftScope.values())))
                .queryParam("redirect_uri", context.getRedirectUri()).build();
    }

    /**
     * Returns the URL to obtain user information.
     *
     * @param authorization the user's authorization token
     * @return the URL to obtain user information
     */
    @Override
    protected String userInfoUrl(Authorization authorization) {
        return Builder.fromUrl(this.complex.userinfo()).build();
    }

    /**
     * Returns the URL to refresh the access token.
     *
     * @param refresh the user's refresh token
     * @return the URL to refresh the access token
     */
    @Override
    protected String refreshUrl(String refresh) {
        String tenantId = StringKit.isEmpty(context.getUnionId()) ? "common" : context.getUnionId();
        return Builder.fromUrl(String.format(this.complex.refresh(), tenantId))
                .queryParam("client_id", context.getClientId()).queryParam("client_secret", context.getClientSecret())
                .queryParam("refresh_token", refresh).queryParam("grant_type", "refresh_token")
                .queryParam("scope", this.getScopes(Symbol.SPACE, false, this.getScopes(MicrosoftScope.values())))
                .queryParam("redirect_uri", context.getRedirectUri()).build();
    }

}
