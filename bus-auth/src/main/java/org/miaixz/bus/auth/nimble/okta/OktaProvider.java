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
package org.miaixz.bus.auth.nimble.okta;

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.Authorization;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Claims;
import org.miaixz.bus.auth.nimble.AbstractProvider;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;

/**
 * Okta login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class OktaProvider extends AbstractProvider {

    /**
     * Constructs an {@code OktaProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public OktaProvider(Context context) {
        super(context, Registry.OKTA);
    }

    /**
     * Constructs an {@code OktaProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public OktaProvider(Context context, CacheX cache) {
        super(context, Registry.OKTA, cache);
    }

    /**
     * Retrieves the access token from Okta's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link Authorization} containing access token details
     */
    @Override
    public Message token(Callback callback) {
        String tokenUrl = tokenUrl(callback.getCode());

        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(getAuthToken(tokenUrl)).build();
    }

    /**
     * Retrieves the authentication token from the specified URL.
     *
     * @param tokenUrl the URL to fetch the access token from
     * @return the {@link Authorization} containing token details
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    private Authorization getAuthToken(String tokenUrl) {
        Map<String, String> header = new HashMap<>();
        header.put("accept", MediaType.APPLICATION_JSON);
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        header.put(
                HTTP.AUTHORIZATION,
                "Basic " + Base64.encode(context.getClientId().concat(Symbol.COLON).concat(context.getClientSecret())));

        String response = Httpx.post(tokenUrl, null, header);
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
            String tokenType = (String) object.get("token_type");
            Object expiresInObj = object.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;
            String scope = (String) object.get("scope");
            String refresh = (String) object.get("refresh_token");
            String idToken = (String) object.get("id_token");

            return Authorization.builder().token(token).token_type(tokenType).expireIn(expiresIn).scope(scope)
                    .refresh(refresh).idToken(idToken).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
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
        if (null == authorization.getRefresh()) {
            return Message.builder().errcode(ErrorCode._100113.getKey()).errmsg(ErrorCode._100113.getValue()).build();
        }
        String refreshUrl = refreshUrl(authorization.getRefresh());
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(this.getAuthToken(refreshUrl)).build();
    }

    /**
     * Retrieves user information from Okta's user info endpoint.
     *
     * @param authorization the {@link Authorization} obtained after successful authorization
     * @return {@link Claims} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Message userInfo(Authorization authorization) {
        Map<String, String> header = new HashMap<>();
        header.put(HTTP.AUTHORIZATION, HTTP.BEARER + authorization.getToken());

        String response = Httpx.post(userInfoUrl(authorization), null, header);
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }

            this.checkResponse(object);

            String sub = (String) object.get("sub");
            if (sub == null) {
                throw new AuthorizedException("Missing sub in user info response");
            }
            String name = (String) object.get("name");
            String nickname = (String) object.get("nickname");
            String email = (String) object.get("email");
            String sex = (String) object.get("sex");
            Map<String, Object> address = (Map<String, Object>) object.get("address");
            String streetAddress = address != null ? (String) address.get("street_address") : null;

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                    .data(
                            Claims.builder().rawJson(JsonKit.toJsonString(object)).uuid(sub).username(name)
                                    .nickname(nickname).email(email).location(streetAddress).gender(Gender.of(sex))
                                    .token(authorization).source(complex.toString()).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    /**
     * Revokes the authorization for the given access token.
     *
     * @param authorization the token information to revoke
     * @return a {@link Message} indicating the result of the revocation
     */
    @Override
    public Message revoke(Authorization authorization) {
        Map<String, String> params = new HashMap<>(4);
        params.put("token", authorization.getToken());
        params.put("token_type_hint", "access_token");

        Map<String, String> header = new HashMap<>();
        header.put(
                HTTP.AUTHORIZATION,
                "Basic " + Base64.encode(context.getClientId().concat(Symbol.COLON).concat(context.getClientSecret())));

        Httpx.post(revokeUrl(authorization), params, header);
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue()).build();
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
     * Returns the authorization URL with a {@code state} parameter. The {@code state} will be included in the
     * authorization callback.
     *
     * @param state the parameter to verify the authorization process, which can prevent CSRF attacks
     * @return the authorization URL
     */
    @Override
    public Message build(String state) {
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(
                Builder.fromUrl(
                        String.format(
                                complex.authorize(),
                                context.getPrefix(),
                                ObjectKit.defaultIfNull(context.getUnionId(), "default")))
                        .queryParam("response_type", "code").queryParam("prompt", "consent")
                        .queryParam("client_id", context.getClientId())
                        .queryParam("redirect_uri", context.getRedirectUri())
                        .queryParam("scope", this.getScopes(Symbol.SPACE, true, this.getScopes(OktaScope.values())))
                        .queryParam("state", getRealState(state)).build())
                .build();
    }

    /**
     * Constructs the access token URL for Okta.
     *
     * @param code the authorization code
     * @return the access token URL
     */
    @Override
    public String tokenUrl(String code) {
        return Builder
                .fromUrl(
                        String.format(
                                this.complex.token(),
                                context.getPrefix(),
                                ObjectKit.defaultIfNull(context.getUnionId(), "default")))
                .queryParam("code", code).queryParam("grant_type", "authorization_code")
                .queryParam("redirect_uri", context.getRedirectUri()).build();
    }

    /**
     * Constructs the refresh token URL for Okta.
     *
     * @param refresh the refresh token
     * @return the refresh token URL
     */
    @Override
    protected String refreshUrl(String refresh) {
        return Builder
                .fromUrl(
                        String.format(
                                this.complex.refresh(),
                                context.getPrefix(),
                                ObjectKit.defaultIfNull(context.getUnionId(), "default")))
                .queryParam("refresh_token", refresh).queryParam("grant_type", "refresh_token").build();
    }

    /**
     * Constructs the revoke authorization URL for Okta.
     *
     * @param authorization the access token
     * @return the revoke authorization URL
     */
    @Override
    protected String revokeUrl(Authorization authorization) {
        return String.format(
                this.complex.revoke(),
                context.getPrefix(),
                ObjectKit.defaultIfNull(context.getUnionId(), "default"));
    }

    /**
     * Constructs the user information URL for Okta.
     *
     * @param authorization the access token
     * @return the user information URL
     */
    @Override
    public String userInfoUrl(Authorization authorization) {
        return String.format(
                this.complex.userinfo(),
                context.getPrefix(),
                ObjectKit.defaultIfNull(context.getUnionId(), "default"));
    }

}
