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
package org.miaixz.bus.auth.nimble.renren;

import org.miaixz.bus.auth.magic.Authorization;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Claims;
import org.miaixz.bus.auth.nimble.AbstractProvider;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Renren login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RenrenProvider extends AbstractProvider {

    /**
     * Constructs a {@code RenrenProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public RenrenProvider(Context context) {
        super(context, Registry.RENREN);
    }

    /**
     * Constructs a {@code RenrenProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public RenrenProvider(Context context, CacheX cache) {
        super(context, Registry.RENREN, cache);
    }

    /**
     * Retrieves the access token from Renren's authorization server.
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
     * Retrieves user information from Renren's user info endpoint.
     *
     * @param authorization the {@link Authorization} obtained after successful authorization
     * @return {@link Claims} containing the user's information
     */
    @Override
    public Message userInfo(Authorization authorization) {
        String response = doGetUserInfo(authorization);
        Map<String, Object> userObj = (Map<String, Object>) JsonKit.toPojo(response, Map.class).get("response");

        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(
                        Claims.builder().rawJson(JsonKit.toJsonString(userObj)).uuid((String) userObj.get("id"))
                                .avatar(getAvatarUrl(userObj)).nickname((String) userObj.get("name"))
                                .company(getCompany(userObj)).gender(getGender(userObj)).token(authorization)
                                .source(complex.toString()).build())
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
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(getToken(this.refreshUrl(authorization.getRefresh()))).build();
    }

    /**
     * Retrieves an authentication token from the specified URL.
     *
     * @param url the URL to request the token from
     * @return the {@link Authorization} containing token details
     * @throws AuthorizedException if the response indicates an error or is missing required token information
     */
    private Authorization getToken(String url) {
        String response = Httpx.post(url);
        Map<String, Object> jsonObject = JsonKit.toPojo(response, Map.class);
        if (jsonObject.containsKey("error")) {
            throw new AuthorizedException("Failed to get token from Renren: " + jsonObject);
        }

        return Authorization.builder().token_type((String) jsonObject.get("token_type"))
                .expireIn(((Number) jsonObject.get("expires_in")).intValue())
                .token(UrlEncoder.encodeAll((String) jsonObject.get("access_token")))
                .refresh(UrlEncoder.encodeAll((String) jsonObject.get("refresh_token")))
                .openId(((Map<String, Object>) jsonObject.get("user")).get("id").toString()).build();
    }

    /**
     * Retrieves the avatar URL from the user object.
     *
     * @param userObj the map containing user information
     * @return the avatar URL, or null if not found
     */
    private String getAvatarUrl(Map<String, Object> userObj) {
        List<Map<String, Object>> jsonArray = (List<Map<String, Object>>) userObj.get("avatar");
        if (Objects.isNull(jsonArray) || jsonArray.isEmpty()) {
            return null;
        }
        return jsonArray.get(0).get("url").toString();
    }

    /**
     * Retrieves the gender from the user object.
     *
     * @param userObj the map containing user information
     * @return the {@link Gender} of the user, or {@link Gender#UNKNOWN} if not found
     */
    private Gender getGender(Map<String, Object> userObj) {
        Map<String, Object> basicInformation = (Map<String, Object>) userObj.get("basicInformation");
        if (Objects.isNull(basicInformation)) {
            return Gender.UNKNOWN;
        }
        return Gender.of((String) basicInformation.get("sex"));
    }

    /**
     * Retrieves the company name from the user object.
     *
     * @param userObj the map containing user information
     * @return the company name, or null if not found
     */
    private String getCompany(Map<String, Object> userObj) {
        List<Map<String, Object>> jsonArray = (List<Map<String, Object>>) userObj.get("work");
        if (Objects.isNull(jsonArray) || jsonArray.isEmpty()) {
            return null;
        }
        return jsonArray.get(0).get("name").toString();
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
                .queryParam("userId", authorization.getOpenId()).build();
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
                        .queryParam("scope", this.getScopes(Symbol.COMMA, false, this.getScopes(RenrenScope.values())))
                        .build())
                .build();
    }

}
