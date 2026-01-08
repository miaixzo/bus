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

import lombok.Data;
import org.miaixz.bus.auth.magic.Authorization;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.Claims;
import org.miaixz.bus.auth.nimble.AbstractProvider;

import java.util.Map;

/**
 * QQ Mini Program login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class QqMiniProvider extends AbstractProvider {

    /**
     * Constructs a {@code QqMiniProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public QqMiniProvider(Context context) {
        super(context, Registry.QQ_MINI);
    }

    /**
     * Constructs a {@code QqMiniProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public QqMiniProvider(Context context, CacheX cache) {
        super(context, Registry.QQ_MINI, cache);
    }

    /**
     * Retrieves the access token for the QQ Mini Program. This method uses the authorization code to obtain the openId,
     * unionId, and session_key.
     *
     * @param authCallback the callback object containing the authorization code
     * @return the {@link Authorization} containing access token details
     * @throws AuthorizedException if the response indicates an error or is missing required token information
     */
    @Override
    public Message token(Callback authCallback) {
        // Use the code to get the corresponding openId, unionId, etc.
        String response = Httpx.get(tokenUrl(authCallback.getCode()));
        Map<String, Object> object = JsonKit.toPojo(response, Map.class);

        this.checkResponse(object);

        // Assemble the result
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(
                Authorization.builder().openId((String) object.get("openid")).unionId((String) object.get("unionid"))
                        .token((String) object.get("session_key")).build())
                .build();
    }

    /**
     * Retrieves user information for the QQ Mini Program. Note: If user information is required, it needs to be passed
     * to the backend after the Mini Program calls a function.
     *
     * @param authorization the {@link Authorization} obtained after successful authorization
     * @return {@link Claims} containing the user's information
     */
    @Override
    public Message userInfo(Authorization authorization) {
        // If user information is required, it needs to be passed to the backend after the Mini Program calls a function
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(
                Claims.builder().rawJson(JsonKit.toJsonString(authorization)).username("").nickname("").avatar("")
                        .uuid(authorization.getOpenId()).token(authorization).source(complex.toString()).build())
                .build();
    }

    /**
     * Checks if the response content is correct.
     *
     * @param response the response content from the request
     * @throws AuthorizedException if the response indicates an error
     */
    private void checkResponse(Map<String, Object> response) {
        if (!Symbol.ZERO.equals(response.get(Consts.ERRCODE))) {
            throw new AuthorizedException((String) response.get(Consts.ERRMSG));
        }
    }

    /**
     * Constructs the access token URL for the QQ Mini Program.
     *
     * @param code the authorization code
     * @return the access token URL
     */
    @Override
    protected String tokenUrl(String code) {
        return Builder.fromUrl(this.complex.token()).queryParam("appid", context.getClientId())
                .queryParam("secret", context.getClientSecret()).queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code").build();
    }

    /**
     * Data class representing the response from QQ Mini Program's jscode2session API.
     */
    @Data
    private static class JSCode2SessionResponse {

        /**
         * Error code returned by the API.
         */
        private String errcode;
        /**
         * Error message returned by the API.
         */
        private String errmsg;
        /**
         * Session key for the user.
         */
        private String session_key;
        /**
         * User's OpenID.
         */
        private String openid;
        /**
         * User's UnionID.
         */
        private String unionid;

    }

}
