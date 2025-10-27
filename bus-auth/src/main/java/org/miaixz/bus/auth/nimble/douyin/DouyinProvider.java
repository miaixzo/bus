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
package org.miaixz.bus.auth.nimble.douyin;

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
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;

/**
 * Douyin (TikTok) login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DouyinProvider extends AbstractProvider {

    /**
     * Constructs a {@code DouyinProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public DouyinProvider(Context context) {
        super(context, Registry.DOUYIN);
    }

    /**
     * Constructs a {@code DouyinProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public DouyinProvider(Context context, CacheX cache) {
        super(context, Registry.DOUYIN, cache);
    }

    /**
     * Retrieves the access token from Douyin's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link Authorization} containing access token details
     */
    @Override
    public Message token(Callback callback) {
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(this.getToken(tokenUrl(callback.getCode())))
                .build();
    }

    /**
     * Retrieves user information from Douyin's user info endpoint.
     *
     * @param authorization the {@link Authorization} obtained after successful authorization
     * @return {@link Material} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Message userInfo(Authorization authorization) {
        String response = doGetUserInfo(authorization);
        try {
            Map<String, Object> userInfoObject = JsonKit.toPojo(response, Map.class);
            if (userInfoObject == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }
            this.checkResponse(userInfoObject);

            Map<String, Object> data = (Map<String, Object>) userInfoObject.get(Consts.DATA);
            if (data == null) {
                throw new AuthorizedException("Missing data field in user info response");
            }

            String unionId = (String) data.get("union_id");
            if (unionId == null) {
                throw new AuthorizedException("Missing union_id in user info response");
            }
            String nickname = (String) data.get("nickname");
            String avatar = (String) data.get("avatar");
            String description = (String) data.get("description");
            String gender = (String) data.get("gender");
            String country = (String) data.get("country");
            String province = (String) data.get("province");
            String city = (String) data.get("city");

            authorization.setUnionId(unionId);

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                    .data(
                            Material.builder().rawJson(JsonKit.toJsonString(data)).uuid(unionId).username(nickname)
                                    .nickname(nickname).avatar(avatar).remark(description).gender(Gender.of(gender))
                                    .location(String.format("%s %s %s", country, province, city)).token(authorization)
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
     */
    @Override
    public Message refresh(Authorization authorization) {
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(getToken(refreshUrl(authorization.getRefresh()))).build();
    }

    /**
     * Checks the response content for errors.
     *
     * @param object the response map to check
     * @throws AuthorizedException if the response indicates an error or message indicating failure
     */
    private void checkResponse(Map<String, Object> object) {
        String message = (String) object.get("message");
        Map<String, Object> data = (Map<String, Object>) object.get(Consts.DATA);
        if (data == null) {
            throw new AuthorizedException("Missing data field in response");
        }
        Object errorCodeObj = data.get("error_code");
        String errorCode = errorCodeObj != null ? String.valueOf(errorCodeObj) : null;
        if ("error".equals(message) || !"0".equals(errorCode)) {
            String description = (String) data.get("description");
            throw new AuthorizedException(errorCode, description != null ? description : "Unknown error");
        }
    }

    /**
     * Retrieves the token, applicable for both obtaining access tokens and refreshing tokens.
     *
     * @param tokenUrl the actual URL to request the token from
     * @return the {@link Authorization} object
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    private Authorization getToken(String tokenUrl) {
        String response = Httpx.post(tokenUrl);
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse token response: empty response");
            }
            this.checkResponse(object);

            object = (Map<String, Object>) object.get(Consts.DATA);
            if (object == null) {
                throw new AuthorizedException("Missing data field in token response");
            }

            String token = (String) object.get("access_token");
            if (token == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            String openId = (String) object.get("open_id");
            Object expiresInObj = object.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;
            String refresh = (String) object.get("refresh_token");
            Object refreshExpiresInObj = object.get("refresh_expires_in");
            int refreshExpiresIn = refreshExpiresInObj instanceof Number ? ((Number) refreshExpiresInObj).intValue()
                    : 0;
            String scope = (String) object.get("scope");

            return Authorization.builder().token(token).openId(openId).expireIn(expiresIn).refresh(refresh)
                    .refreshExpireIn(refreshExpiresIn).scope(scope).build();
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
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(
                Builder.fromUrl(this.complex.authorize()).queryParam("response_type", "code")
                        .queryParam("client_key", context.getClientId())
                        .queryParam("redirect_uri", context.getRedirectUri())
                        .queryParam("scope", this.getScopes(Symbol.COMMA, true, this.getScopes(DouyinScope.values())))
                        .queryParam("state", getRealState(state)).build())
                .build();
    }

    /**
     * Returns the URL to obtain the access token.
     *
     * @param code the OAuth authorization code
     * @return the URL to obtain the access token
     */
    @Override
    protected String tokenUrl(String code) {
        return Builder.fromUrl(this.complex.token()).queryParam("code", code)
                .queryParam("client_key", context.getClientId()).queryParam("client_secret", context.getClientSecret())
                .queryParam("grant_type", "authorization_code").queryParam("redirect_uri", context.getRedirectUri())
                .build();
    }

    /**
     * Returns the URL to obtain user information.
     *
     * @param authorization the OAuth token returned
     * @return the URL to obtain user information
     */
    @Override
    protected String userInfoUrl(Authorization authorization) {
        return Builder.fromUrl(this.complex.userinfo()).queryParam("access_token", authorization.getToken())
                .queryParam("open_id", authorization.getOpenId()).build();
    }

    /**
     * Returns the URL to refresh the access token.
     *
     * @param refresh the OAuth refresh token returned
     * @return the URL to refresh the access token
     */
    @Override
    protected String refreshUrl(String refresh) {
        return Builder.fromUrl(this.complex.refresh()).queryParam("client_key", context.getClientId())
                .queryParam("refresh_token", refresh).queryParam("grant_type", "refresh_token").build();
    }

}
