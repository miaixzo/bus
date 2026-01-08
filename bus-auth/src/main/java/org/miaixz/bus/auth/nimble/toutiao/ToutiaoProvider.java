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
package org.miaixz.bus.auth.nimble.toutiao;

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
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Toutiao (ByteDance) login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ToutiaoProvider extends AbstractProvider {

    /**
     * Constructs a {@code ToutiaoProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public ToutiaoProvider(Context context) {
        super(context, Registry.TOUTIAO);
    }

    /**
     * Constructs a {@code ToutiaoProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public ToutiaoProvider(Context context, CacheX cache) {
        super(context, Registry.TOUTIAO, cache);
    }

    /**
     * Retrieves the access token from Toutiao's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link Authorization} containing access token details
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    @Override
    public Message token(Callback callback) {
        String response = doPostToken(callback.getCode());
        Map<String, Object> object = JsonKit.toPojo(response, Map.class);

        this.checkResponse(object);

        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(
                        Authorization.builder().token((String) object.get("access_token"))
                                .expireIn(((Number) object.get("expires_in")).intValue())
                                .openId((String) object.get("open_id")).build())
                .build();
    }

    /**
     * Retrieves user information from Toutiao's user info endpoint.
     *
     * @param authorization the {@link Authorization} obtained after successful authorization
     * @return {@link Claims} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Message userInfo(Authorization authorization) {
        String userResponse = doGetUserInfo(authorization);
        Map<String, Object> userProfile = JsonKit.toPojo(userResponse, Map.class);

        this.checkResponse(userProfile);

        Map<String, Object> user = (Map<String, Object>) userProfile.get(Consts.DATA);

        boolean isAnonymousUser = "14".equals(user.get("uid_type"));
        String anonymousUserName = "Anonymous User";

        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(
                        Claims.builder().rawJson(JsonKit.toJsonString(userProfile)).uuid((String) user.get("uid"))
                                .username(isAnonymousUser ? anonymousUserName : (String) user.get("screen_name"))
                                .nickname(isAnonymousUser ? anonymousUserName : (String) user.get("screen_name"))
                                .avatar((String) user.get("avatar_url")).remark((String) user.get("description"))
                                .gender(Gender.of((String) user.get("gender"))).token(authorization)
                                .source(complex.toString()).build())
                .build();
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
                        Builder.fromUrl(complex.authorize()).queryParam("response_type", "code")
                                .queryParam("client_key", context.getClientId())
                                .queryParam("redirect_uri", context.getRedirectUri()).queryParam("auth_only", 1)
                                .queryParam("display", 0).queryParam("state", getRealState(state)).build())
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
        return Builder.fromUrl(this.complex.token()).queryParam("code", code)
                .queryParam("client_key", context.getClientId()).queryParam("client_secret", context.getClientSecret())
                .queryParam("grant_type", "authorization_code").build();
    }

    /**
     * Returns the URL to obtain user information.
     *
     * @param authorization the user's authorization token
     * @return the URL to obtain user information
     */
    @Override
    protected String userInfoUrl(Authorization authorization) {
        return Builder.fromUrl(this.complex.userinfo()).queryParam("client_key", context.getClientId())
                .queryParam("access_token", authorization.getToken()).build();
    }

    /**
     * Checks the response content for errors.
     *
     * @param object the response map to check
     * @throws AuthorizedException if the response indicates an error or message indicating failure
     */
    private void checkResponse(Map<String, Object> object) {
        if (object.containsKey("error_code")) {
            throw new AuthorizedException(ErrorCode.Toutiao.getErrorCode((String) object.get("error_code")).getValue());
        }
    }

}
