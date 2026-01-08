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
package org.miaixz.bus.auth.nimble.qq;

import org.miaixz.bus.auth.magic.Authorization;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
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
 * QQ login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class QqProvider extends AbstractProvider {

    /**
     * Constructs a {@code QqProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public QqProvider(Context context) {
        super(context, Registry.QQ);
    }

    /**
     * Constructs a {@code QqProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public QqProvider(Context context, CacheX cache) {
        super(context, Registry.QQ, cache);
    }

    /**
     * Retrieves the access token from QQ's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link Authorization} containing access token details
     */
    @Override
    public Message token(Callback callback) {
        String response = doPostToken(callback.getCode());
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(this.getAuthToken(response)).build();
    }

    /**
     * Refreshes the access token (renews its validity).
     *
     * @param authorization the token information returned after successful login
     * @return a {@link Message} containing the refreshed token information
     */
    @Override
    public Message refresh(Authorization authorization) {
        String response = Httpx.get(refreshUrl(authorization.getRefresh()));
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(getAuthToken(response)).build();
    }

    /**
     * Retrieves user information from QQ's user info endpoint.
     *
     * @param authorization the {@link Authorization} obtained after successful authorization
     * @return {@link Claims} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Message userInfo(Authorization authorization) {
        String openId = this.getOpenId(authorization);
        String response = doGetUserInfo(authorization);
        Map<String, Object> object = JsonKit.toPojo(response, Map.class);
        if (!"0".equals(object.get("ret"))) {
            throw new AuthorizedException((String) object.get("msg"));
        }
        String avatar = (String) object.get("figureurl_qq_2");
        if (StringKit.isEmpty(avatar)) {
            avatar = (String) object.get("figureurl_qq_1");
        }

        String location = String.format("%s-%s", object.get("province"), object.get("city"));

        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(
                        Claims.builder().rawJson(JsonKit.toJsonString(object)).username((String) object.get("nickname"))
                                .nickname((String) object.get("nickname")).avatar(avatar).location(location)
                                .uuid(openId).gender(Gender.of((String) object.get("gender"))).token(authorization)
                                .source(complex.toString()).build())
                .build();
    }

    /**
     * Retrieves the OpenId of the QQ user. Supports custom enabling of the unionid query function. If the unionid query
     * function is enabled, the developer needs to apply for the unionid function via email. Reference link:
     * {@see <a href="http://wiki.connect.qq.com/unionid%E4%BB%8B%E7%BB%8D">UnionID Introduction - QQ Connect Wiki</a>}
     *
     * @param authorization the {@code token} obtained via {@link QqProvider#token(Callback)}
     * @return the user's OpenId or UnionId if available
     * @throws AuthorizedException if parsing the response fails or an error is returned by QQ
     */
    private String getOpenId(Authorization authorization) {
        String response = Httpx.get(
                Builder.fromUrl("https://graph.qq.com/oauth2.0/me").queryParam("access_token", authorization.getToken())
                        .queryParam("unionid", context.isFlag() ? 1 : 0).build());
        String removePrefix = response.replace("callback(", "");
        String removeSuffix = removePrefix.replace(");", "");
        String openId = removeSuffix.trim();
        Map<String, Object> object = JsonKit.toPojo(openId, Map.class);
        if (object.containsKey("error")) {
            throw new AuthorizedException(
                    (String) object.get("error") + Symbol.COLON + (String) object.get("error_description"));
        }
        authorization.setOpenId((String) object.get("openid"));
        if (object.containsKey("unionid")) {
            authorization.setUnionId((String) object.get("unionid"));
        }
        return StringKit.isEmpty(authorization.getUnionId()) ? authorization.getOpenId() : authorization.getUnionId();
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
                .queryParam("oauth_consumer_key", context.getClientId()).queryParam("openid", authorization.getOpenId())
                .build();
    }

    /**
     * Parses the access token response string into an {@link Authorization} object.
     *
     * @param response the response string from the access token endpoint
     * @return the parsed {@link Authorization}
     * @throws AuthorizedException if the response indicates an error or is missing required token information
     */
    private Authorization getAuthToken(String response) {
        Map<String, String> object = Builder.parseStringToMap(response);
        if (!object.containsKey("access_token") || object.containsKey("code")) {
            throw new AuthorizedException(object.get("msg"));
        }
        return Authorization.builder().token(object.get("access_token"))
                .expireIn(Integer.parseInt(object.getOrDefault("expires_in", "0"))).refresh(object.get("refresh_token"))
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
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(
                Builder.fromUrl((String) super.build(state).getData())
                        .queryParam("scope", this.getScopes(Symbol.COMMA, false, this.getScopes(QqScope.values())))
                        .build())
                .build();
    }

}
