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
package org.miaixz.bus.auth.nimble.huawei;

import org.miaixz.bus.auth.magic.Authorization;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Material;
import org.miaixz.bus.auth.nimble.AbstractProvider;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Huawei login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HuaweiProvider extends AbstractProvider {

    /**
     * Constructs a {@code HuaweiProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public HuaweiProvider(Context context) {
        super(context, Registry.HUAWEI);
    }

    /**
     * Constructs a {@code HuaweiProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public HuaweiProvider(Context context, CacheX cache) {
        super(context, Registry.HUAWEI, cache);
    }

    /**
     * Retrieves the access token from Huawei's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link Authorization} containing access token details
     */
    @Override
    public Message token(Callback callback) {
        Map<String, String> form = new HashMap<>(8);
        form.put("grant_type", "authorization_code");
        form.put("code", callback.getCode());
        form.put("client_id", context.getClientId());
        form.put("client_secret", context.getClientSecret());
        form.put("redirect_uri", context.getRedirectUri());

        if (context.isPkce()) {
            String cacheKey = this.complex.getName().concat(":code_verifier:").concat(callback.getState());
            String codeVerifier = (String) this.cache.read(cacheKey);
            form.put("code_verifier", codeVerifier);
        }

        Map<String, String> header = new HashMap<>(8);
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);

        String response = Httpx.post(this.complex.token(), header, form);

        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(getAuthToken(response)).build();
    }

    /**
     * Retrieves user information from Huawei's user info endpoint.
     *
     * @param authorization the token information
     * @return {@link Material} containing the user's information
     * @see AbstractProvider#token(Callback)
     */
    @Override
    public Message userInfo(Authorization authorization) {
        String idToken = authorization.getIdToken();
        if (StringKit.isEmpty(idToken)) {
            Map<String, String> form = new HashMap<>(7);
            form.put("access_token", authorization.getToken());
            form.put("getNickName", "1");
            form.put("nsp_svc", "GOpen.User.getInfo");

            Map<String, String> header = new HashMap<>(7);
            header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
            String response = Httpx.post(this.complex.userinfo(), header, form);
            try {
                Map<String, Object> object = JsonKit.toPojo(response, Map.class);
                if (object == null) {
                    throw new AuthorizedException("Failed to parse user info response: empty response");
                }
                this.checkResponse(object);

                String unionID = (String) object.get("unionID");
                if (unionID == null) {
                    throw new AuthorizedException("Missing unionID in user info response");
                }
                String displayName = (String) object.get("displayName");
                String headPictureURL = (String) object.get("headPictureURL");

                return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                        .data(
                                Material.builder().rawJson(JsonKit.toJsonString(object)).uuid(unionID)
                                        .username(displayName).nickname(displayName).gender(Gender.UNKNOWN)
                                        .avatar(headPictureURL).token(authorization).source(context.toString()).build())
                        .build();
            } catch (Exception e) {
                throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
            }
        }
        String payload = new String(Base64.getUrlDecoder().decode(idToken.split("\\.")[1]), Charset.UTF_8);
        try {
            Map<String, Object> object = JsonKit.toPojo(payload, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse id_token payload: empty response");
            }

            String sub = (String) object.get("sub");
            if (sub == null) {
                throw new AuthorizedException("Missing sub in id_token payload");
            }
            String name = (String) object.get("name");
            String nickname = (String) object.get("nickname");
            String picture = (String) object.get("picture");

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                    .data(
                            Material.builder().rawJson(JsonKit.toJsonString(object)).uuid(sub).username(name)
                                    .nickname(nickname).gender(Gender.UNKNOWN).avatar(picture).token(authorization)
                                    .source(complex.toString()).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse id_token payload: " + e.getMessage());
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
        Map<String, String> form = new HashMap<>(7);
        form.put("client_id", context.getClientId());
        form.put("client_secret", context.getClientSecret());
        form.put("refresh_token", authorization.getRefresh());
        form.put("grant_type", "refresh_token");

        Map<String, String> header = new HashMap<>(7);
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        String response = Httpx.post(this.complex.refresh(), header, form);

        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(getAuthToken(response)).build();
    }

    /**
     * Parses the access token response string into an {@link Authorization} object.
     *
     * @param response the response string from the access token endpoint
     * @return the parsed {@link Authorization}
     * @throws AuthorizedException if the response indicates an error or is missing required token information
     */
    private Authorization getAuthToken(String response) {
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
            String refresh = (String) object.get("refresh_token");
            String idToken = (String) object.get("id_token");

            return Authorization.builder().token(token).expireIn(expiresIn).refresh(refresh).idToken(idToken).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
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
        String realState = getRealState(state);

        Builder builder = Builder.fromUrl((String) super.build(state).getData()).queryParam("access_type", "offline")
                .queryParam("scope", this.getScopes(Symbol.SPACE, true, this.getScopes(HuaweiScope.values())));

        if (context.isPkce()) {
            String cacheKey = this.complex.getName().concat(":code_verifier:").concat(realState);
            String codeVerifier = Builder.codeVerifier();
            String codeChallengeMethod = "S256";
            String codeChallenge = Builder.codeChallenge(codeChallengeMethod, codeVerifier);
            builder.queryParam("code_challenge", codeChallenge)
                    .queryParam("code_challenge_method", codeChallengeMethod);
            // 缓存 codeVerifier 十分钟
            this.cache.write(cacheKey, codeVerifier, TimeUnit.MINUTES.toMillis(10));
        }
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(builder.build()).build();
    }

    /**
     * Checks the response content for errors.
     *
     * @param object the response map to check
     * @throws AuthorizedException if the response contains an error or message indicating failure
     */
    private void checkResponse(Map<String, Object> object) {
        if (object.containsKey("NSP_STATUS")) {
            String error = (String) object.get("error");
            throw new AuthorizedException(error != null ? error : "Unknown error");
        }
        if (object.containsKey("error")) {
            String subError = (String) object.get("sub_error");
            String errorDescription = (String) object.get("error_description");
            throw new AuthorizedException((subError != null ? subError : "Unknown sub_error") + Symbol.COLON
                    + (errorDescription != null ? errorDescription : "Unknown description"));
        }
    }

}
