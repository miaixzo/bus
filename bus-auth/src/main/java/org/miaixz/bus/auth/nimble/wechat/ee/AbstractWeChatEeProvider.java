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
package org.miaixz.bus.auth.nimble.wechat.ee;

import org.miaixz.bus.auth.magic.Authorization;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Complex;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Claims;
import org.miaixz.bus.auth.nimble.wechat.AbstractWeChatProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class for WeChat Enterprise login providers.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractWeChatEeProvider extends AbstractWeChatProvider {

    /**
     * Constructs an {@code AbstractWeChatEeProvider} with the specified context and complex configuration.
     *
     * @param context the authentication context
     * @param complex the complex configuration for WeChat Enterprise
     */
    public AbstractWeChatEeProvider(Context context, Complex complex) {
        super(context, complex);
    }

    /**
     * Constructs an {@code AbstractWeChatEeProvider} with the specified context, complex configuration, and cache.
     *
     * @param context the authentication context
     * @param complex the complex configuration for WeChat Enterprise
     * @param cache   the cache implementation
     */
    public AbstractWeChatEeProvider(Context context, Complex complex, CacheX cache) {
        super(context, complex, cache);
    }

    /**
     * Retrieves the access token from WeChat Enterprise's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link Authorization} containing access token details
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    @Override
    public Message token(Callback callback) {
        String response = doPostToken(tokenUrl(null));
        Map<String, Object> object = this.checkResponse(response);

        String token = (String) object.get("access_token");
        if (token == null) {
            throw new AuthorizedException("Missing access_token in response");
        }
        Object expiresInObj = object.get("expires_in");
        int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;

        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(Authorization.builder().token(token).expireIn(expiresIn).code(callback.getCode()).build())
                .build();
    }

    /**
     * Retrieves user information from WeChat Enterprise's user info endpoint.
     *
     * @param authorization the {@link Authorization} obtained after successful authorization
     * @return {@link Claims} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Message userInfo(Authorization authorization) {
        String response = doGetUserInfo(authorization);
        Map<String, Object> object = this.checkResponse(response);

        // Returns OpenId or other, both indicate non-current enterprise users, not supported
        if (!object.containsKey("userid")) {
            throw new AuthorizedException(ErrorCode._110004.getKey(), complex);
        }
        String userId = (String) object.get("userid");
        String userTicket = (String) object.get("user_ticket");
        Map<String, Object> data = getUserDetail(authorization.getToken(), userId, userTicket);

        String name = (String) data.get("name");
        String alias = (String) data.get("alias");
        String avatar = (String) data.get("avatar");
        String address = (String) data.get("address");
        String email = (String) data.get("email");
        String gender = (String) data.get("gender");

        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(
                Claims.builder().rawJson(JsonKit.toJsonString(data)).username(name).nickname(alias).avatar(avatar)
                        .location(address).email(email).uuid(userId).gender(getWechatRealGender(gender))
                        .token(authorization).source(complex.toString()).build())
                .build();
    }

    /**
     * Checks the response content for errors.
     *
     * @param response the response string to check
     * @return the parsed response map if the request result is normal
     * @throws AuthorizedException if the response indicates an error or is malformed
     */
    private Map<String, Object> checkResponse(String response) {
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse response: empty response");
            }
            Object errcodeObj = object.get(Consts.ERRCODE);
            if (errcodeObj != null && !errcodeObj.equals(0)) {
                String errmsg = (String) object.get(Consts.ERRMSG);
                throw new AuthorizedException(errmsg != null ? errmsg : "Unknown error", complex.getName());
            }
            return object;
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse response: " + e.getMessage());
        }
    }

    /**
     * Returns the URL to obtain the access token.
     *
     * @param code the authorization code
     * @return the URL to obtain the access token
     */
    @Override
    public String tokenUrl(String code) {
        return Builder.fromUrl(this.complex.token()).queryParam("corpid", context.getClientId())
                .queryParam("corpsecret", context.getClientSecret()).build();
    }

    /**
     * Returns the URL to obtain user information.
     *
     * @param authorization the user's authorization token
     * @return the URL to obtain user information
     */
    @Override
    public String userInfoUrl(Authorization authorization) {
        return Builder.fromUrl(this.complex.userinfo()).queryParam("access_token", authorization.getToken())
                .queryParam("code", authorization.getCode()).build();
    }

    /**
     * Retrieves user details.
     *
     * @param token      the access token
     * @param userId     the user ID within the enterprise
     * @param userTicket the user ticket, used to obtain user information or sensitive information
     * @return a map containing user details
     */
    private Map<String, Object> getUserDetail(String token, String userId, String userTicket) {
        // User basic information
        String userInfoUrl = Builder.fromUrl("https://qyapi.weixin.qq.com/cgi-bin/user/get")
                .queryParam("access_token", token).queryParam("userid", userId).build();
        String response = Httpx.get(userInfoUrl);
        Map<String, Object> object = checkResponse(response);

        // User sensitive information
        if (StringKit.isNotEmpty(userTicket)) {
            String userDetailUrl = Builder.fromUrl("https://qyapi.weixin.qq.com/cgi-bin/auth/getuserdetail")
                    .queryParam("access_token", token).build();
            Map<String, Object> param = new HashMap<>();
            param.put("user_ticket", userTicket);
            String userDetailResponse = Httpx
                    .post(userDetailUrl, JsonKit.toJsonString(param), MediaType.APPLICATION_JSON);
            Map<String, Object> userDetail = checkResponse(userDetailResponse);

            object.putAll(userDetail);
        }
        return object;
    }

}
