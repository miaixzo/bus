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
package org.miaixz.bus.auth.nimble.amazon;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;

/**
 * Amazon login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AmazonProvider extends AbstractProvider {

    /**
     * Constructs an {@code AmazonProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public AmazonProvider(Context context) {
        super(context, Registry.AMAZON);
    }

    /**
     * Constructs an {@code AmazonProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public AmazonProvider(Context context, CacheX cache) {
        super(context, Registry.AMAZON, cache);
    }

    /**
     * Returns the authorization URL with a {@code state} parameter. The {@code state} will be included in the
     * authorization callback. Reference:
     * https://developer.amazon.com/zh/docs/login-with-amazon/authorization-code-grant.html#authorization-request
     *
     * @param state the parameter to verify the authorization process, which can prevent CSRF attacks
     * @return the authorization URL
     */
    @Override
    public Message build(String state) {
        String realState = getRealState(state);
        Builder builder = Builder.fromUrl(this.complex.authorize()).queryParam("client_id", context.getClientId())
                .queryParam("scope", this.getScopes(Symbol.SPACE, true, this.getScopes(AmazonScope.values())))
                .queryParam("redirect_uri", context.getRedirectUri()).queryParam("response_type", "code")
                .queryParam("state", realState);

        if (context.isPkce()) {
            String cacheKey = this.complex.getName().concat(":code_verifier:").concat(realState);
            String codeVerifier = Builder.codeVerifier();
            String codeChallengeMethod = "S256";
            String codeChallenge = Builder.codeChallenge(codeChallengeMethod, codeVerifier);
            builder.queryParam("code_challenge", codeChallenge)
                    .queryParam("code_challenge_method", codeChallengeMethod);
            // Cache codeVerifier for ten minutes
            this.cache.write(cacheKey, codeVerifier, TimeUnit.MINUTES.toMillis(10));
        }

        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(builder.build()).build();
    }

    /**
     * Retrieves the access token from Amazon's authorization server. Reference:
     * https://developer.amazon.com/zh/docs/login-with-amazon/authorization-code-grant.html#access-token-request
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link Authorization} containing access token details
     */
    @Override
    public Message token(Callback callback) {
        Map<String, String> form = new HashMap<>(9);
        form.put("grant_type", "authorization_code");
        form.put("code", callback.getCode());
        form.put("redirect_uri", context.getRedirectUri());
        form.put("client_id", context.getClientId());
        form.put("client_secret", context.getClientSecret());

        if (context.isPkce()) {
            String cacheKey = this.complex.getName().concat(":code_verifier:").concat(callback.getState());
            String codeVerifier = String.valueOf(this.cache.read(cacheKey));
            form.put("code_verifier", codeVerifier);
        }
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(this.getToken(form, this.complex.token()))
                .build();
    }

    /**
     * Refreshes the access token (renews its validity).
     *
     * @param authorization the token information returned after successful login
     * @return a {@link Message} containing the refreshed token information
     */
    @Override
    public Message refresh(Authorization authorization) {
        Map<String, String> form = new HashMap<>(7);
        form.put("grant_type", "refresh_token");
        form.put("refresh_token", authorization.getRefresh());
        form.put("client_id", context.getClientId());
        form.put("client_secret", context.getClientSecret());
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(getToken(form, this.complex.refresh()))
                .build();
    }

    /**
     * Retrieves an authentication token from the specified URL with given parameters.
     *
     * @param param a map of parameters for the token request
     * @param url   the URL to request the token from
     * @return the {@link Authorization} containing token details
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    private Authorization getToken(Map<String, String> param, String url) {
        Map<String, String> header = new HashMap<>();
        header.put(HTTP.HOST, "api.amazon.com");
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED + ";charset=UTF-8");

        String response = Httpx.post(url, param, header);
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse JSON response: empty response");
            }
            this.checkResponse(object);

            String token = (String) object.get("access_token");
            if (token == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            String tokenType = (String) object.get("token_type");
            Object expiresInObj = object.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;
            String refresh = (String) object.get("refresh_token");

            return Authorization.builder().token(token).token_type(tokenType).expireIn(expiresIn).refresh(refresh)
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse token response: " + e.getMessage());
        }
    }

    /**
     * Checks the response content for errors.
     *
     * @param jsonObject the response map to check
     * @throws AuthorizedException if the response indicates an error or message indicating failure
     */
    private void checkResponse(Map<String, Object> jsonObject) {
        if (jsonObject.containsKey("error")) {
            String errorDescription = (String) jsonObject.get("error_description");
            throw new AuthorizedException(errorDescription != null ? errorDescription : "Unknown error");
        }
    }

    /**
     * Retrieves user information from Amazon's user info endpoint. Reference:
     * https://developer.amazon.com/zh/docs/login-with-amazon/obtain-customer-profile.html#call-profile-endpoint
     *
     * @param authorization the token information
     * @return {@link Claims} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Message userInfo(Authorization authorization) {
        String token = authorization.getToken();
        this.checkToken(token);
        Map<String, String> header = new HashMap<>();
        header.put(HTTP.HOST, "api.amazon.com");
        header.put(HTTP.AUTHORIZATION, "bearer " + token);

        String userInfo = Httpx.get(this.complex.userinfo(), new HashMap<>(0), header);
        try {
            Map<String, Object> object = JsonKit.toPojo(userInfo, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }
            this.checkResponse(object);

            String userId = (String) object.get("user_id");
            if (userId == null) {
                throw new AuthorizedException("Missing user_id in response");
            }
            String name = (String) object.get("name");
            String email = (String) object.get("email");

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                    .data(
                            Claims.builder().rawJson(JsonKit.toJsonString(object)).uuid(userId).username(name)
                                    .nickname(name).email(email).gender(Gender.UNKNOWN).source(complex.toString())
                                    .token(authorization).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    /**
     * Checks the validity of the access token by calling Amazon's token info endpoint.
     *
     * @param token the access token to check
     * @throws AuthorizedException if the token is invalid or an error occurs during validation
     */
    private void checkToken(String token) {
        String tokenInfo = Httpx
                .get("https://api.amazon.com/auth/o2/tokeninfo?access_token=" + UrlEncoder.encodeAll(token));
        try {
            Map<String, Object> jsonObject = JsonKit.toPojo(tokenInfo, Map.class);
            if (jsonObject == null) {
                throw new AuthorizedException("Failed to parse token info response: empty response");
            }
            String aud = (String) jsonObject.get("aud");
            if (!context.getClientId().equals(aud)) {
                throw new AuthorizedException(ErrorCode._100113.getKey());
            }
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse token info response: " + e.getMessage());
        }
    }

    /**
     * Constructs the user information URL.
     *
     * @param authorization the user's authorization token
     * @return the user information URL
     */
    @Override
    protected String userInfoUrl(Authorization authorization) {
        return Builder.fromUrl(this.complex.userinfo()).queryParam("user_id", authorization.getUserId())
                .queryParam("screen_name", authorization.getScreenName()).queryParam("include_entities", true).build();
    }

}
