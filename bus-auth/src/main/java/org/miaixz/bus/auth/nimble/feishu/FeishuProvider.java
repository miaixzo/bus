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
import org.miaixz.bus.auth.magic.Authorization;
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
    private String getToken() {
        String cacheKey = this.complex.getName().concat(":app_access_token:").concat(context.getClientId());
        String cacheToken = String.valueOf(this.cache.read(cacheKey));
        if (StringKit.isNotEmpty(cacheToken)) {
            return cacheToken;
        }
        String url = "https://open.feishu.cn/open-apis/auth/v3/app_access_token/internal/";
        Map<String, Object> object = new HashMap<>();
        object.put("app_id", context.getClientId());
        object.put("app_secret", context.getClientSecret());

        Map<String, String> header = new HashMap<>();
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON);

        String response = Httpx.post(url, JsonKit.toJsonString(object), header, MediaType.APPLICATION_JSON);
        object = JsonKit.toPojo(response, Map.class);
        if (object == null) {
            throw new AuthorizedException("Failed to parse app access token response: empty response");
        }
        this.checkResponse(object);
        String token = (String) object.get("app_access_token");
        if (token == null) {
            throw new AuthorizedException("Missing app_access_token in response");
        }
        Object expireObj = object.get("expire");
        long expire = expireObj instanceof Number ? ((Number) expireObj).longValue() : 0;
        // Cache app access token
        this.cache.write(cacheKey, token, expire * 1000);
        return token;
    }

    /**
     * Retrieves the access token from Feishu's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link Authorization} containing access token details
     */
    @Override
    public Message token(Callback callback) {
        Map<String, Object> requestObject = new HashMap<>();
        requestObject.put("app_access_token", this.getToken());
        requestObject.put("grant_type", "authorization_code");
        requestObject.put("code", callback.getCode());

        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(this.getToken(requestObject, this.complex.token())).build();
    }

    /**
     * Retrieves user information from Feishu's user info endpoint.
     *
     * @param authorization the {@link Authorization} obtained after successful authorization
     * @return {@link Message} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Message userInfo(Authorization authorization) {
        String token = authorization.getToken();
        Map<String, String> header = new HashMap<>();
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        header.put("Authorization", "Bearer " + token);
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

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                    .data(
                            Material.builder().rawJson(JsonKit.toJsonString(object)).uuid(unionId).username(name)
                                    .nickname(name).avatar(avatarUrl).email(email).gender(Gender.UNKNOWN)
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
        Map<String, Object> requestObject = new HashMap<>();
        requestObject.put("app_access_token", this.getToken());
        requestObject.put("grant_type", "refresh_token");
        requestObject.put("refresh_token", authorization.getRefresh());
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(this.getToken(requestObject, this.complex.refresh())).build();
    }

    /**
     * Retrieves an authentication token from the specified URL with given parameters.
     *
     * @param param a map of parameters for the token request
     * @param url   the URL to request the token from
     * @return the {@link Authorization} containing token details
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    private Authorization getToken(Map<String, Object> param, String url) {
        Map<String, String> header = new HashMap<>();
        header.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        String response = Httpx.post(url, JsonKit.toJsonString(param), header, MediaType.APPLICATION_JSON);
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse token response: empty response");
            }
            this.checkResponse(object);
            object = (Map<String, Object>) object.get(Consts.DATA);
            if (object == null) {
                throw new AuthorizedException("Missing data in token response");
            }

            String token = (String) object.get("access_token");
            if (token == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            String refresh = (String) object.get("refresh_token");
            Object expiresInObj = object.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;
            String tokenType = (String) object.get("token_type");
            String openId = (String) object.get("open_id");

            return Authorization.builder().token(token).refresh(refresh).expireIn(expiresIn).token_type(tokenType)
                    .openId(openId).build();
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
    public Message build(String state) {
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(
                        Builder.fromUrl(complex.authorize()).queryParam("app_id", context.getClientId())
                                .queryParam("redirect_uri", UrlEncoder.encodeAll(context.getRedirectUri()))
                                .queryParam("state", getRealState(state)).build())
                .build();
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
