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
package org.miaixz.bus.auth.nimble.feishu;

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.AuthToken;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Material;
import org.miaixz.bus.auth.nimble.AbstractProvider;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;

/**
 * Feishu (Lark) login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FeishuProvider extends AbstractProvider {

    /**
     * Constructs a {@code FeishuProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public FeishuProvider(Context context) {
        super(context, Registry.FEISHU);
    }

    /**
     * Constructs a {@code FeishuProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public FeishuProvider(Context context, CacheX cache) {
        super(context, Registry.FEISHU, cache);
    }

    /**
     * Retrieves the application access token (for enterprise self-built applications).
     * <p>
     * The token is valid for 2 hours. During this period, calling this interface will not change the token. When the
     * token's validity period is less than 30 minutes, requesting a token again will generate a new token, while the
     * old token remains valid.
     *
     * @return the application access token
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    private String getAppAccessToken() {
        String cacheKey = this.complex.getName().concat(":app_access_token:").concat(context.getAppKey());
        String cacheAppAccessToken = String.valueOf(this.cache.read(cacheKey));
        if (StringKit.isNotEmpty(cacheAppAccessToken)) {
            return cacheAppAccessToken;
        }
        String url = "https://open.feishu.cn/open-apis/auth/v3/app_access_token/internal/";
        Map<String, Object> requestObject = new HashMap<>();
        requestObject.put("app_id", context.getAppKey());
        requestObject.put("app_secret", context.getAppSecret());

        Map<String, String> header = new HashMap<>();
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON);

        String response = Httpx.post(url, JsonKit.toJsonString(requestObject), header, MediaType.APPLICATION_JSON);
        Map<String, Object> jsonObject = JsonKit.toPojo(response, Map.class);
        if (jsonObject == null) {
            throw new AuthorizedException("Failed to parse app access token response: empty response");
        }
        this.checkResponse(jsonObject);
        String appAccessToken = (String) jsonObject.get("app_access_token");
        if (appAccessToken == null) {
            throw new AuthorizedException("Missing app_access_token in response");
        }
        Object expireObj = jsonObject.get("expire");
        long expire = expireObj instanceof Number ? ((Number) expireObj).longValue() : 0;
        // Cache app access token
        this.cache.write(cacheKey, appAccessToken, expire * 1000);
        return appAccessToken;
    }

    /**
     * Retrieves the access token from Feishu's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link AuthToken} containing access token details
     */
    @Override
    public AuthToken getAccessToken(Callback callback) {
        Map<String, Object> requestObject = new HashMap<>();
        requestObject.put("app_access_token", this.getAppAccessToken());
        requestObject.put("grant_type", "authorization_code");
        requestObject.put("code", callback.getCode());
        return getToken(requestObject, this.complex.accessToken());
    }

    /**
     * Retrieves user information from Feishu's user info endpoint.
     *
     * @param authToken the {@link AuthToken} obtained after successful authorization
     * @return {@link Material} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Material getUserInfo(AuthToken authToken) {
        String accessToken = authToken.getAccessToken();
        Map<String, String> header = new HashMap<>();
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        header.put("Authorization", "Bearer " + accessToken);
        String response = Httpx.get(this.complex.userinfo(), null, header);
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }
            this.checkResponse(object);
            Map<String, Object> data = (Map<String, Object>) object.get(Consts.DATA);
            if (data == null) {
                throw new AuthorizedException("Missing data in user info response");
            }

            String unionId = (String) data.get("union_id");
            String name = (String) data.get("name");
            String avatarUrl = (String) data.get("avatar_url");
            String email = (String) data.get("email");

            return Material.builder().rawJson(JsonKit.toJsonString(object)).uuid(unionId).username(name).nickname(name)
                    .avatar(avatarUrl).email(email).gender(Gender.UNKNOWN).token(authToken).source(complex.toString())
                    .build();
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
        Map<String, Object> requestObject = new HashMap<>();
        requestObject.put("app_access_token", this.getAppAccessToken());
        requestObject.put("grant_type", "refresh_token");
        requestObject.put("refresh_token", authToken.getRefreshToken());
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(getToken(requestObject, this.complex.refresh())).build();
    }

    /**
     * Retrieves an authentication token from the specified URL with given parameters.
     *
     * @param param a map of parameters for the token request
     * @param url   the URL to request the token from
     * @return the {@link AuthToken} containing token details
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    private AuthToken getToken(Map<String, Object> param, String url) {
        Map<String, String> header = new HashMap<>();
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        String response = Httpx.post(url, JsonKit.toJsonString(param), header, MediaType.APPLICATION_JSON);
        try {
            Map<String, Object> jsonObject = JsonKit.toPojo(response, Map.class);
            if (jsonObject == null) {
                throw new AuthorizedException("Failed to parse token response: empty response");
            }
            this.checkResponse(jsonObject);
            Map<String, Object> data = (Map<String, Object>) jsonObject.get(Consts.DATA);
            if (data == null) {
                throw new AuthorizedException("Missing data in token response");
            }

            String accessToken = (String) data.get("access_token");
            if (accessToken == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            String refreshToken = (String) data.get("refresh_token");
            Object expiresInObj = data.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;
            String tokenType = (String) data.get("token_type");
            String openId = (String) data.get("open_id");

            return AuthToken.builder().accessToken(accessToken).refreshToken(refreshToken).expireIn(expiresIn)
                    .tokenType(tokenType).openId(openId).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse token response: " + e.getMessage());
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
        return Builder.fromUrl(complex.authorize()).queryParam("app_id", context.getAppKey())
                .queryParam("redirect_uri", UrlEncoder.encodeAll(context.getRedirectUri()))
                .queryParam("state", getRealState(state)).build();
    }

    /**
     * Checks the response content for errors.
     *
     * @param jsonObject the response content to check
     * @throws AuthorizedException if the response indicates an error
     */
    private void checkResponse(Map<String, Object> jsonObject) {
        Object codeObj = jsonObject.get("code");
        int code = codeObj instanceof Number ? ((Number) codeObj).intValue() : -1;
        if (code != 0) {
            String message = (String) jsonObject.get("message");
            throw new AuthorizedException(message != null ? message : "Unknown error");
        }
    }

}
