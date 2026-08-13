/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.auth.vendor.wechat.ee;

import java.util.Objects;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * WeChat Enterprise third-party QR-code client.
 *
 * @author Kimi Liu
 */
public class WeChatEeThirdQrcodeProvider extends AbstractWeChatEeProvider {

    /**
     * Creates a third-party QR-code client from explicit dependencies.
     *
     * @param configuration complete non-null vendor dependencies
     */
    public WeChatEeThirdQrcodeProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.WECHAT_EE_QRCODE);
    }

    /**
     * Parses and validates a typed enterprise response.
     *
     * @param <T>      response type
     * @param document response JSON
     * @param type     response class
     * @return validated response
     */
    private static <T extends Response> T read(final String document, final Class<T> type) {
        final T response = JsonKit.toPojo(document, type);
        if (response == null)
            throw new AuthorizedException("WeChat Enterprise response is empty");
        if (response.errcode() != 0)
            throw new AuthorizedException(
                    response.errmsg() == null ? "WeChat Enterprise request failed" : response.errmsg());
        return response;
    }

    /**
     * Builds the third-party QR-code authorization URL and atomically registers state.
     *
     * @param context root operation context
     * @param state   optional authorization state
     * @return successful message containing the authorization URL
     */
    @Override
    public Message<String> build(final Context context, final String state) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        return Message.success(
                VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.AUTHORIZE))
                        .queryParam("appid", registration.clientId())
                        .queryParam("redirect_uri", registration.redirectUri())
                        .queryParam("state", state(current, state)).queryParam("usertype", registration.type())
                        .build());
    }

    /**
     * Executes the shared validated state, token, and user-info sequence without token serialization.
     *
     * @param context  root operation context
     * @param callback immutable inbound callback
     * @return vendor identity result
     */
    @Override
    public Message<VendorIdentity> authorize(final Context context, final Callback.Inbound callback) {
        return super.authorize(context, callback);
    }

    /**
     * Retrieves the provider access token using a JSON request and retains the callback code.
     *
     * @param context  root operation context used for secret resolution
     * @param callback immutable inbound callback
     * @return successful message containing the provider token
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final TokenResponse response = read(
                post(
                        endpoint(VendorEndpoint.TOKEN),
                        JsonKit.toJsonString(new TokenRequest(registration.clientId(), secret(current))),
                        MediaType.APPLICATION_JSON),
                TokenResponse.class);
        if (response.provider_access_token() == null)
            throw new AuthorizedException("WeChat Enterprise provider token is missing");
        return Message.success(
                VendorTokenSet.builder().token(response.provider_access_token()).expireIn(response.expires_in())
                        .code(inbound.value("code").orElse(null)).build());
    }

    /**
     * Retrieves third-party login information with the retained callback authorization code.
     *
     * @param context root operation context
     * @param token   non-null provider token retaining the callback code
     * @return successful message containing the raw login-information identity
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final String url = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.USERINFO))
                .queryParam("access_token", authorization.getToken()).build();
        final LoginResponse response = read(
                post(url, JsonKit.toJsonString(new LoginRequest(authorization.getCode())), MediaType.APPLICATION_JSON),
                LoginResponse.class);
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(response)).token(authorization)
                        .source(descriptor().id()).build());
    }

    /**
     * Common response contract. @author Kimi Liu
     */
    private interface Response {

        /**
         * Returns status.
         *
         * @return numeric status
         */
        int errcode();

        /**
         * Returns diagnostic.
         *
         * @return optional diagnostic
         */
        String errmsg();
    }

    /**
     * Token request.
     *
     * @param corpid          corporation identifier
     * @param provider_secret provider secret
     * @author Kimi Liu
     */
    private record TokenRequest(String corpid, String provider_secret) {
    }

    /**
     * Token response.
     *
     * @param errcode               status
     * @param errmsg                message
     * @param provider_access_token token
     * @param expires_in            lifetime in seconds
     * @author Kimi Liu
     */
    private record TokenResponse(int errcode, String errmsg, String provider_access_token, int expires_in)
            implements Response {
    }

    /**
     * Login request.
     *
     * @param auth_code callback code
     * @author Kimi Liu
     */
    private record LoginRequest(String auth_code) {
    }

    /**
     * Login response.
     *
     * @param errcode             status
     * @param errmsg              message
     * @param corp_info           corporation data
     * @param user_info           user data
     * @param redirect_login_info redirect data
     * @author Kimi Liu
     */
    private record LoginResponse(int errcode, String errmsg, Object corp_info, Object user_info,
            Object redirect_login_info) implements Response {
    }

}
