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
package org.miaixz.bus.auth.vendor.alipay;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party authentication client for Alipay OAuth 2.0 and its signed gateway operations.
 *
 * <p>
 * All gateway requests use the injected Fabric context. Security time is read from the injected Fabric clock, and
 * authorization state is registered through the injected atomic store. The registration must include the Alipay union
 * identifier and a non-local redirect URI.
 * </p>
 *
 * @author Kimi Liu
 */
public class AlipayProvider extends AbstractProvider {

    /**
     * Alipay gateway method used for token exchange and refresh.
     */
    private static final String TOKEN_METHOD = "alipay.system.auth.token";

    /**
     * Alipay gateway method used for identity retrieval.
     */
    private static final String USER_METHOD = "alipay.user.info.share";

    /**
     * Alipay RSA signature algorithm label carried by every gateway request.
     */
    private static final String SIGN_TYPE = "RSA2";

    /**
     * Alipay gateway protocol version carried by every request.
     */
    private static final String VERSION = "1.0";

    /**
     * Creates an Alipay client from the complete immutable dependency aggregate.
     *
     * @param configuration non-null registration, Fabric, clock, state-store, cache, and secret dependencies
     * @throws NullPointerException if the configuration or one of its required dependencies is null
     * @throws AuthorizedException  if required registration data is absent or the redirect URI targets localhost
     */
    public AlipayProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.ALIPAY);
        validateAlipayRegistration(configuration.registration());
    }

    /**
     * Parses an optional decimal lifetime into seconds.
     *
     * @param value decimal lifetime text
     * @return parsed seconds, or zero when the field is absent
     */
    private static int integer(final String value) {
        return value == null ? 0 : Integer.parseInt(value);
    }

    /**
     * Validates Alipay-only redirect restrictions after common registration validation.
     *
     * @param registration immutable Alipay client registration
     * @throws AuthorizedException if the registered redirect URI targets localhost
     */
    private static void validateAlipayRegistration(final VendorRegistration registration) {
        if (Protocol.isLocalHost(registration.redirectUri())) {
            throw new AuthorizedException(VendorErrors._110003);
        }
    }

    /**
     * Builds the Alipay authorization URL locally and atomically registers its state.
     *
     * @param context immutable root operation context used to register state
     * @param state   optional caller-supplied state; a generated state is used when absent
     * @return successful client message containing the complete authorization URL
     * @throws NullPointerException if {@code context} is null
     * @throws AuthorizedException  if the authorize endpoint is absent or state registration fails
     */
    @Override
    public Message<String> build(final Context context, final String state) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        return Message.success(
                VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.AUTHORIZE))
                        .queryParam("app_id", registration.clientId()).queryParam("scope", "auth_user")
                        .queryParam("redirect_uri", registration.redirectUri())
                        .queryParam("state", state(current, state)).build());
    }

    /**
     * Exchanges the Alipay {@code auth_code} callback value through the token gateway method.
     *
     * @param context  immutable root operation context supplying deterministic security time
     * @param callback immutable inbound callback containing {@code auth_code}
     * @return successful client message containing access and refresh token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if Alipay returns an error response or omits its token response object
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final Map<String, String> params = common(current, TOKEN_METHOD);
        params.put("grant_type", "authorization_code");
        params.put("code", inbound.value("auth_code").orElse(null));
        return Message.success(token(post(endpoint(VendorEndpoint.TOKEN), params)));
    }

    /**
     * Retrieves the Alipay identity associated with an access token through the user-info gateway method.
     *
     * @param context immutable root operation context supplying deterministic security time
     * @param token   non-null Alipay token set
     * @return successful client message containing the mapped Alipay identity
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if Alipay returns an error response or omits its user response object
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final Map<String, String> params = common(current, USER_METHOD);
        params.put("auth_token", authorization.getToken());
        final GatewayResponse response = gateway(post(endpoint(VendorEndpoint.USERINFO), params));
        final UserResponse user = response.alipay_user_info_share_response();
        if (user == null) {
            throw new AuthorizedException(VendorErrors._110004);
        }
        final String location = String.format(
                "%s %s",
                StringKit.isEmpty(user.province()) ? Normal.EMPTY : user.province(),
                StringKit.isEmpty(user.city()) ? Normal.EMPTY : user.city());
        final String username = StringKit.isEmpty(user.user_name()) ? user.nick_name() : user.user_name();
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(user)).uuid(user.user_id()).username(username)
                        .nickname(user.nick_name()).avatar(user.avatar()).location(location)
                        .gender(Gender.of(user.gender())).token(authorization).source(descriptor().id()).build());
    }

    /**
     * Refreshes an Alipay token through the token gateway method.
     *
     * @param context immutable root operation context supplying deterministic security time
     * @param token   non-null token set containing the refresh token
     * @return successful client message containing the refreshed token set
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if Alipay returns an error response or omits its token response object
     */
    @Override
    public Message<VendorTokenSet> refresh(final Context context, final VendorTokenSet token) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final Map<String, String> params = common(current, TOKEN_METHOD);
        params.put("grant_type", "refresh_token");
        params.put("refresh_token", authorization.getRefresh());
        return Message.success(token(post(endpoint(VendorEndpoint.TOKEN), params)));
    }

    /**
     * Creates the common ordered Alipay gateway form fields.
     *
     * @param context root operation context supplying the injected clock
     * @param method  exact Alipay gateway method name
     * @return mutable insertion-ordered form owned by the caller
     */
    private Map<String, String> common(final Context context, final String method) {
        final Map<String, String> params = new LinkedHashMap<>();
        params.put("app_id", registration.clientId());
        params.put("method", method);
        params.put("charset", Charset.DEFAULT_UTF_8);
        params.put("sign_type", SIGN_TYPE);
        params.put("timestamp", String.valueOf(clock.now().toEpochMilli()));
        params.put("version", VERSION);
        return params;
    }

    /**
     * Parses and validates one token gateway response.
     *
     * @param json Alipay gateway JSON document
     * @return mapped token set
     * @throws AuthorizedException if the response contains an error or no token payload
     */
    private VendorTokenSet token(final String json) {
        final GatewayResponse response = gateway(json);
        final TokenResponse token = response.alipay_system_oauth_token_response();
        if (token == null) {
            throw new AuthorizedException(VendorErrors._110004);
        }
        return VendorTokenSet.builder().token(token.access_token()).uid(token.user_id())
                .expireIn(integer(token.expires_in())).refresh(token.refresh_token()).build();
    }

    /**
     * Parses a gateway document and converts its standard error payload into an authorization exception.
     *
     * @param json Alipay gateway JSON document
     * @return non-null typed gateway envelope
     * @throws AuthorizedException if parsing yields no envelope or an error response is present
     */
    private GatewayResponse gateway(final String json) {
        final GatewayResponse response = JsonKit.toPojo(json, GatewayResponse.class);
        if (response == null) {
            throw new AuthorizedException(VendorErrors._110004);
        }
        ErrorResponse error = response.error_response();
        if (error == null && response.alipay_system_oauth_token_response() != null) {
            error = response.alipay_system_oauth_token_response().error_response();
        }
        if (error == null && response.alipay_user_info_share_response() != null) {
            error = response.alipay_user_info_share_response().error_response();
        }
        if (error != null) {
            throw new AuthorizedException(error.sub_msg());
        }
        return response;
    }

    /**
     * Typed Alipay gateway response envelope.
     *
     * @param alipay_system_oauth_token_response token operation response
     * @param alipay_user_info_share_response    user-information operation response
     * @param error_response                     standard root error response
     * @author Kimi Liu
     */
    private record GatewayResponse(TokenResponse alipay_system_oauth_token_response,
            UserResponse alipay_user_info_share_response, ErrorResponse error_response) {
    }

    /**
     * Typed Alipay token response.
     *
     * @param access_token   access token
     * @param user_id        Alipay user identifier
     * @param expires_in     token lifetime in seconds encoded as decimal text
     * @param refresh_token  refresh token
     * @param error_response nested compatibility error response
     * @author Kimi Liu
     */
    private record TokenResponse(String access_token, String user_id, String expires_in, String refresh_token,
            ErrorResponse error_response) {
    }

    /**
     * Typed Alipay user-information response.
     *
     * @param user_id        Alipay user identifier
     * @param user_name      user name
     * @param nick_name      nickname
     * @param avatar         avatar URL
     * @param province       province name
     * @param city           city name
     * @param gender         gender code
     * @param error_response nested compatibility error response
     * @author Kimi Liu
     */
    private record UserResponse(String user_id, String user_name, String nick_name, String avatar, String province,
            String city, String gender, ErrorResponse error_response) {
    }

    /**
     * Typed Alipay gateway error response.
     *
     * @param sub_msg vendor diagnostic message
     * @author Kimi Liu
     */
    private record ErrorResponse(String sub_msg) {
    }

}
