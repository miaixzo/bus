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
package org.miaixz.bus.auth.nimble.alipay;

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Checker;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.Authorization;
import org.miaixz.bus.auth.magic.Callback;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.magic.Claims;
import org.miaixz.bus.auth.nimble.AbstractProvider;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;

/**
 * Alipay login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AlipayProvider extends AbstractProvider {

    private static final String GATEWAY = "https://openapi.alipay.com/gateway.do";

    /**
     * Constructs an {@code AlipayProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public AlipayProvider(Context context) {
        super(context, Registry.ALIPAY);
        check(context);
    }

    /**
     * Constructs an {@code AlipayProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public AlipayProvider(Context context, CacheX cache) {
        super(context, Registry.ALIPAY, cache);
        check(context);
    }

    /**
     * Checks the completeness and validity of the context configuration for Alipay authentication. Ensures that the
     * unionId is not empty and the redirect URI is not a localhost address.
     *
     * @param context the authentication context
     * @throws AuthorizedException if the unionId is empty or the redirect URI is invalid
     */
    protected void check(Context context) {
        Checker.check(context, Registry.ALIPAY);

        if (!StringKit.isNotEmpty(context.getUnionId())) {
            throw new AuthorizedException(ErrorCode._110002.getKey(), Registry.ALIPAY);
        }

        if (Protocol.isLocalHost(context.getRedirectUri())) {
            throw new AuthorizedException(ErrorCode._110005.getKey(), Registry.ALIPAY);
        }
    }

    /**
     * Checks the callback data for Alipay authentication. Ensures that the authorization code (auth_code) is not empty.
     *
     * @param callback the callback object containing authorization data
     * @throws AuthorizedException if the authorization code is empty
     */
    @Override
    protected void validate(Callback callback) {
        if (StringKit.isEmpty(callback.getAuth_code())) {
            throw new AuthorizedException(ErrorCode._110007.getKey(), complex);
        }
    }

    /**
     * Retrieves the access token from Alipay's authorization server.
     *
     * @param callback the callback object containing the authorization code
     * @return the {@link Authorization} containing access token details
     * @throws AuthorizedException if parsing the response fails or required token information is missing
     */
    @Override
    public Message token(Callback callback) {
        Map<String, String> params = new HashMap<>();
        params.put("app_id", context.getClientId());
        params.put("method", "alipay.system.auth.token");
        params.put("charset", Charset.DEFAULT_UTF_8);
        params.put("sign_type", "RSA2");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("version", "1.0");
        params.put("grant_type", "authorization_code");
        params.put("code", callback.getAuth_code());

        String response = Httpx.post(GATEWAY, params);
        Map<String, Object> json = JsonKit.toMap(response);
        Map<String, Object> tokenResponse = (Map<String, Object>) json.get("alipay_system_oauth_token_response");

        if (tokenResponse.containsKey("error_response")) {
            throw new AuthorizedException(
                    (String) ((Map<String, Object>) tokenResponse.get("error_response")).get("sub_msg"));
        }

        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(
                        Authorization.builder().token((String) tokenResponse.get("access_token"))
                                .uid((String) tokenResponse.get("user_id"))
                                .expireIn(Integer.parseInt((String) tokenResponse.get("expires_in")))
                                .refresh((String) tokenResponse.get("refresh_token")).build())
                .build();
    }

    /**
     * Refreshes the access token (renews its validity).
     *
     * @param authorization the token information returned after successful login
     * @return a {@link Message} containing the refreshed token information
     * @throws AuthorizedException if parsing the response fails or an error occurs during token refresh
     */
    @Override
    public Message refresh(Authorization authorization) {
        Map<String, String> params = new HashMap<>();
        params.put("app_id", context.getClientId());
        params.put("method", "alipay.system.auth.token");
        params.put("charset", Charset.DEFAULT_UTF_8);
        params.put("sign_type", "RSA2");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("version", "1.0");
        params.put("grant_type", "refresh_token");
        params.put("refresh_token", authorization.getRefresh());

        String response = Httpx.post(GATEWAY, params);
        Map<String, Object> json = JsonKit.toMap(response);
        Map<String, Object> tokenResponse = (Map<String, Object>) json.get("alipay_system_oauth_token_response");

        if (tokenResponse.containsKey("error_response")) {
            throw new AuthorizedException(
                    (String) ((Map<String, Object>) tokenResponse.get("error_response")).get("sub_msg"));
        }

        return Message.builder().errcode(ErrorCode._SUCCESS.getKey())
                .data(
                        Authorization.builder().token((String) tokenResponse.get("access_token"))
                                .uid((String) tokenResponse.get("user_id"))
                                .expireIn(Integer.parseInt((String) tokenResponse.get("expires_in")))
                                .refresh((String) tokenResponse.get("refresh_token")).build())
                .build();
    }

    /**
     * Retrieves user information from Alipay's user info endpoint.
     *
     * @param authorization the {@link Authorization} obtained after successful authorization
     * @return {@link Claims} containing the user's information
     * @throws AuthorizedException if parsing the response fails or required user information is missing
     */
    @Override
    public Message userInfo(Authorization authorization) {
        Map<String, String> params = new HashMap<>();
        params.put("app_id", context.getClientId());
        params.put("method", "alipay.user.info.share");
        params.put("charset", Charset.DEFAULT_UTF_8);
        params.put("sign_type", "RSA2");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("version", "1.0");
        params.put("auth_token", authorization.getToken());

        String response = Httpx.post(GATEWAY, params);
        Map<String, Object> json = JsonKit.toMap(response);
        Map<String, Object> userResponse = (Map<String, Object>) json.get("alipay_user_info_share_response");

        if (userResponse.containsKey("error_response")) {
            throw new AuthorizedException(
                    (String) ((Map<String, Object>) userResponse.get("error_response")).get("sub_msg"));
        }

        String province = (String) userResponse.get("province");
        String city = (String) userResponse.get("city");
        String location = String
                .format("%s %s", StringKit.isEmpty(province) ? "" : province, StringKit.isEmpty(city) ? "" : city);

        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).data(
                Claims.builder().rawJson(JsonKit.toJsonString(userResponse)).uuid((String) userResponse.get("user_id"))
                        .username(
                                StringKit.isEmpty((String) userResponse.get("user_name"))
                                        ? (String) userResponse.get("nick_name")
                                        : (String) userResponse.get("user_name"))
                        .nickname((String) userResponse.get("nick_name")).avatar((String) userResponse.get("avatar"))
                        .location(location).gender(Gender.of((String) userResponse.get("gender"))).token(authorization)
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
                        Builder.fromUrl(this.complex.authorize()).queryParam("app_id", context.getClientId())
                                .queryParam("scope", "auth_user").queryParam("redirect_uri", context.getRedirectUri())
                                .queryParam("state", getRealState(state)).build())
                .build();
    }

}
