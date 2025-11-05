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
package org.miaixz.bus.auth.nimble.dingtalk;

import org.miaixz.bus.auth.magic.Authorization;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Complex;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.Claims;
import org.miaixz.bus.auth.nimble.AbstractProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class for DingTalk login providers. This class handles both DingTalk account login for third-party websites
 * and QR code login for third-party websites.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractDingtalkProvider extends AbstractProvider {

    /**
     * Constructs an {@code AbstractDingtalkProvider} with the specified context and complex configuration.
     *
     * @param context the authentication context
     * @param complex the complex configuration for DingTalk
     */
    public AbstractDingtalkProvider(Context context, Complex complex) {
        super(context, complex);
    }

    /**
     * Constructs an {@code AbstractDingtalkProvider} with the specified context, complex configuration, and cache.
     *
     * @param context the authentication context
     * @param complex the complex configuration for DingTalk
     * @param cache   the cache implementation
     */
    public AbstractDingtalkProvider(Context context, Complex complex, CacheX cache) {
        super(context, complex, cache);
    }

    /**
     * Generates the signature for a DingTalk request.
     *
     * @param secretKey the application secret key of the platform
     * @param timestamp the timestamp
     * @return the generated signature
     */
    public static String sign(String secretKey, String timestamp) {
        byte[] signData = Builder.sign(
                secretKey.getBytes(Charset.UTF_8),
                timestamp.getBytes(Charset.UTF_8),
                Algorithm.HMACSHA256.getValue());
        return UrlEncoder.encodeAll(new String(Base64.encode(signData, false)));
    }

    /**
     * Retrieves the access token from DingTalk's authorization server. For DingTalk, the access token is typically
     * derived from the authorization code.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link Authorization} containing access token details
     */
    @Override
    public Message token(Callback callback) {
        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(Authorization.builder().token(callback.getCode()).build()).build();
    }

    /**
     * Retrieves user information from DingTalk's user info endpoint.
     *
     * @param authorization the {@link Authorization} obtained after successful authorization
     * @return {@link Claims} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Message userInfo(Authorization authorization) {
        String code = authorization.getToken();
        Map<String, Object> param = new HashMap<>();
        param.put("tmp_auth_code", code);
        String response = Httpx
                .post(userInfoUrl(authorization), JsonKit.toJsonString(param), MediaType.APPLICATION_JSON);
        try {
            Map<String, Object> object = JsonKit.toPojo(response, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse user info response: empty response");
            }

            Object errCodeObj = object.get(Consts.ERRCODE);
            int errCode = errCodeObj instanceof Number ? ((Number) errCodeObj).intValue() : -1;
            if (errCode != 0) {
                String errMsg = (String) object.get(Consts.ERRMSG);
                throw new AuthorizedException(errMsg != null ? errMsg : "Unknown error");
            }

            Map<String, Object> userInfo = (Map<String, Object>) object.get("user_info");
            if (userInfo == null) {
                throw new AuthorizedException("Missing user_info in response");
            }

            String openId = (String) userInfo.get("openid");
            String unionId = (String) userInfo.get("unionid");
            if (unionId == null) {
                throw new AuthorizedException("Missing unionid in user_info");
            }
            String nick = (String) userInfo.get("nick");

            Authorization token = Authorization.builder().openId(openId).unionId(unionId).build();

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(
                    Claims.builder().rawJson(JsonKit.toJsonString(userInfo)).uuid(unionId).nickname(nick)
                            .username(nick).gender(Gender.UNKNOWN).source(complex.toString()).token(token).build())
                    .build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse user info response: " + e.getMessage());
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
                        Builder.fromUrl(this.complex.authorize()).queryParam("response_type", "code")
                                .queryParam("appid", context.getClientId()).queryParam("scope", "snsapi_login")
                                .queryParam("redirect_uri", context.getRedirectUri())
                                .queryParam("state", getRealState(state)).build())
                .build();
    }

    /**
     * Returns the URL to obtain user information.
     *
     * @param authorization the user's authorization token
     * @return the URL to obtain user information
     */
    @Override
    protected String userInfoUrl(Authorization authorization) {
        // Calculate signature value based on timestamp and appSecret
        String timestamp = System.currentTimeMillis() + "";
        String urlEncodeSignature = sign(context.getClientSecret(), timestamp);

        return Builder.fromUrl(this.complex.userinfo()).queryParam("signature", urlEncodeSignature)
                .queryParam("timestamp", timestamp).queryParam("accessKey", context.getClientId()).build();
    }

}
