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
package org.miaixz.bus.auth.nimble.baidu;

import org.miaixz.bus.auth.magic.Authorization;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Claims;
import org.miaixz.bus.auth.nimble.AbstractProvider;

import java.util.Map;

/**
 * Baidu login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BaiduProvider extends AbstractProvider {

    /**
     * Constructs a {@code BaiduProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public BaiduProvider(Context context) {
        super(context, Registry.BAIDU);
    }

    /**
     * Constructs a {@code BaiduProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public BaiduProvider(Context context, CacheX cache) {
        super(context, Registry.BAIDU, cache);
    }

    /**
     * Retrieves the access token from Baidu's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link Authorization} containing access token details
     */
    @Override
    public Message token(Callback callback) {
        String response = doPostToken(callback.getCode());
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(getAuthToken(response)).build();
    }

    /**
     * Retrieves user information from Baidu's user info endpoint. Note: User information returned by new and old
     * applications may be inconsistent.
     * <p>
     * Example URLs:
     * <ul>
     * <li>https://openapi.baidu.com/rest/2.0/passport/users/getInfo?access_token=...</li>
     * <li>https://openapi.baidu.com/rest/2.0/passport/users/getInfo?access_token=...</li>
     * </ul>
     *
     * @param authorization the token information
     * @return {@link Claims} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Message userInfo(Authorization authorization) {
        String userInfo = doGetUserInfo(authorization);
        try {
            Map<String, Object> object = JsonKit.toPojo(userInfo, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }
            this.checkResponse(object);

            String userId = object.containsKey("userid") ? (String) object.get("userid")
                    : (String) object.get("openid");
            if (userId == null) {
                throw new AuthorizedException("Missing userid or openid in response");
            }
            String username = (String) object.get("username");
            String userDetail = (String) object.get("userdetail");
            String sex = (String) object.get("sex");

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                    .data(
                            Claims.builder().rawJson(JsonKit.toJsonString(object)).uuid(userId).username(username)
                                    .nickname(username).avatar(getAvatar(object)).remark(userDetail)
                                    .gender(Gender.of(sex)).token(authorization).source(complex.toString()).build())
                    .build();
        } catch (

        Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
        }
    }

    /**
     * Retrieves the user's avatar URL from the response object.
     *
     * @param object the map containing user information
     * @return the avatar URL, or null if not found
     */
    private String getAvatar(Map<String, Object> object) {
        String portrait = (String) object.get("portrait");
        return StringKit.isEmpty(portrait) ? null
                : String.format("http://himg.bdimg.com/sys/portrait/item/%s.jpg", portrait);
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
            this.checkResponse(object);

            // Returns 1 for successful authorization cancellation, otherwise failed
            Object resultObj = object.get("result");
            int result = resultObj instanceof Number ? ((Number) resultObj).intValue() : 0;
            Errors status = result == 1 ? ErrorCode._SUCCESS : ErrorCode._FAILURE;
            return Message.builder().errcode(status.getKey()).errmsg(status.getValue()).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse revoke response: " + e.getMessage());
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
        String refreshUrl = Builder.fromUrl(this.complex.refresh()).queryParam("grant_type", "refresh_token")
                .queryParam("refresh_token", authorization.getRefresh())
                .queryParam("client_id", this.context.getClientId())
                .queryParam("client_secret", this.context.getClientSecret()).build();
        String response = Httpx.get(refreshUrl);
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(this.getAuthToken(response)).build();
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
                Builder.fromUrl((String) super.build(state).getData()).queryParam("display", "popup")
                        .queryParam("scope", this.getScopes(Symbol.SPACE, true, this.getScopes(BaiduScope.values())))
                        .build())
                .build();
    }

    /**
     * Checks the response content for errors.
     *
     * @param object the response map to check
     * @throws AuthorizedException if the response contains an error or message indicating failure
     */
    private void checkResponse(Map<String, Object> object) {
        if (object.containsKey("error") || object.containsKey("error_code")) {
            String msg = object.containsKey("error_description") ? (String) object.get("error_description")
                    : (String) object.get("error_msg");
            throw new AuthorizedException(msg != null ? msg : "Unknown error");
        }
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
            String refresh = (String) object.get("refresh_token");
            String scope = (String) object.get("scope");
            Object expiresInObj = object.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;

            return Authorization.builder().token(token).refresh(refresh).scope(scope).expireIn(expiresIn).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
        }
    }

}
