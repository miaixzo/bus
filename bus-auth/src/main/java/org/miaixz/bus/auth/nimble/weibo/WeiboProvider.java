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
package org.miaixz.bus.auth.nimble.weibo;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.xyz.NetKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.Authorization;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Claims;
import org.miaixz.bus.auth.nimble.AbstractProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Weibo login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WeiboProvider extends AbstractProvider {

    /**
     * Constructs a {@code WeiboProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public WeiboProvider(Context context) {
        super(context, Registry.WEIBO);
    }

    /**
     * Constructs a {@code WeiboProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public WeiboProvider(Context context, CacheX cache) {
        super(context, Registry.WEIBO, cache);
    }

    /**
     * Retrieves the access token from Weibo's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link Authorization} containing access token details
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    @Override
    public Message token(Callback callback) {
        String response = doPostToken(callback.getCode());
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse access token response: empty response");
            }
            if (object.containsKey("error")) {
                String errorDescription = (String) object.get("error_description");
                throw new AuthorizedException(errorDescription != null ? errorDescription : "Unknown error");
            }

            String token = (String) object.get("access_token");
            if (token == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            String uid = (String) object.get("uid");
            Object expiresInObj = object.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                    .data(Authorization.builder().token(token).uid(uid).openId(uid).expireIn(expiresIn).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
        }
    }

    /**
     * Retrieves user information from Weibo's user info endpoint.
     *
     * @param authorization the {@link Authorization} obtained after successful authorization
     * @return {@link Claims} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Message userInfo(Authorization authorization) {
        String token = authorization.getToken();
        String uid = authorization.getUid();
        String oauthParam = String.format("uid=%s&access_token=%s", uid, token);

        Map<String, String> header = new HashMap<>();
        header.put("Authorization", "OAuth2 " + oauthParam);
        header.put("API-RemoteIP", NetKit.getLocalhostStringV4());
        String userInfo = Httpx.get(userInfoUrl(authorization), null, header);
        try {
            Map<String, Object> object = JsonKit.toPojo(userInfo, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }
            if (object.containsKey("error")) {
                String error = (String) object.get("error");
                throw new AuthorizedException(error != null ? error : "Unknown error");
            }

            String id = String.valueOf(object.get("id"));
            if (id == null) {
                throw new AuthorizedException("Missing id in user info response");
            }
            String name = (String) object.get("name");
            String profileImageUrl = (String) object.get("profile_image_url");
            String url = (String) object.get("url");
            String profileUrl = (String) object.get("profile_url");
            String screenName = (String) object.get("screen_name");
            String location = (String) object.get("location");
            String description = (String) object.get("description");
            String gender = (String) object.get("gender");

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                    .data(
                            Claims.builder().rawJson(JsonKit.toJsonString(object)).uuid(id).username(name)
                                    .avatar(profileImageUrl)
                                    .blog(StringKit.isEmpty(url) ? "https://weibo.com/" + profileUrl : url)
                                    .nickname(screenName).location(location).remark(description)
                                    .gender(Gender.of(gender)).token(authorization).source(complex.toString()).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    /**
     * Returns the URL to obtain user information.
     *
     * @param authorization the user's authorization token
     * @return the URL to obtain user information
     */
    @Override
    protected String userInfoUrl(Authorization authorization) {
        return Builder.fromUrl(this.complex.userinfo()).queryParam("access_token", authorization.getToken())
                .queryParam("uid", authorization.getUid()).build();
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
                Builder.fromUrl((String) super.build(state).getData())
                        .queryParam("scope", this.getScopes(Symbol.COMMA, false, this.getScopes(WeiboScope.values())))
                        .build())
                .build();
    }

    /**
     * Revokes the authorization for the given access token.
     *
     * @param authorization the token information to revoke
     * @return a {@link Message} indicating the result of the revocation
     * @throws AuthorizedException if parsing the response fails or an error occurs during revocation
     */
    @Override
    public Message revoke(Authorization authorization) {
        String response = doGetRevoke(authorization);
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse revoke response: empty response");
            }
            if (object.containsKey("error")) {
                String error = (String) object.get("error");
                return Message.builder().errcode(ErrorCode._FAILURE.getKey())
                        .errmsg(error != null ? error : "Unknown error").build();
            }

            Object resultObj = object.get("result");
            boolean result = resultObj instanceof Boolean ? (Boolean) resultObj : false;
            Errors status = result ? ErrorCode._SUCCESS : ErrorCode._FAILURE;
            return Message.builder().errcode(status.getKey()).errmsg(status.getValue()).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse revoke response: " + e.getMessage());
        }
    }

}
