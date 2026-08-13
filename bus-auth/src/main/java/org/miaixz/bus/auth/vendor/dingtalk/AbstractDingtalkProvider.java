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
package org.miaixz.bus.auth.vendor.dingtalk;

import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Shared client implementation for DingTalk account authorization flows.
 *
 * <p>
 * The family base is definition-neutral: concrete subclasses supply their own immutable vendor definition. State, time,
 * HTTP transport, and client-secret resolution are taken exclusively from the injected configuration.
 * </p>
 *
 * @author Kimi Liu
 */
public abstract class AbstractDingtalkProvider extends AbstractProvider {

    /**
     * Creates a DingTalk family client without binding a concrete catalog entry.
     *
     * @param configuration non-null registration and explicit runtime dependencies
     * @param definition    non-null concrete DingTalk vendor definition
     * @throws NullPointerException if an argument or required dependency is null
     * @throws AuthorizedException  if the registration is invalid for the supplied definition
     */
    protected AbstractDingtalkProvider(final VendorConfiguration configuration, final VendorDefinition definition) {
        super(configuration, definition);
    }

    /**
     * Produces the URL-encoded DingTalk HMAC-SHA256 signature.
     *
     * @param secretKey transient application secret text
     * @param timestamp decimal epoch timestamp in milliseconds
     * @return URL-encoded Base64 signature
     */
    private static String sign(final String secretKey, final String timestamp) {
        final byte[] signData = VendorRequestBuilder
                .sign(secretKey.getBytes(Charset.UTF_8), timestamp.getBytes(Charset.UTF_8), Algorithm.HMACSHA256);
        return UrlEncoder.encodeAll(new String(Base64.encode(signData, false), Charset.UTF_8));
    }

    /**
     * Builds the DingTalk account authorization URL and atomically registers state.
     *
     * @param context immutable root operation context used for state ownership
     * @param state   optional caller-supplied state; a generated state is used when absent
     * @return successful client message containing the complete authorization URL
     * @throws NullPointerException if {@code context} is null
     * @throws AuthorizedException  if the endpoint is absent or state registration fails
     */
    @Override
    public Message<String> build(final Context context, final String state) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        return Message.success(
                VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.AUTHORIZE)).queryParam("response_type", "code")
                        .queryParam("appid", registration.clientId()).queryParam("scope", "snsapi_login")
                        .queryParam("redirect_uri", registration.redirectUri())
                        .queryParam("state", state(current, state)).build());
    }

    /**
     * Maps the temporary DingTalk authorization code to the family token carrier without a network call.
     *
     * @param context  immutable root operation context for this token conversion
     * @param callback immutable inbound callback containing the standard {@code code} parameter
     * @return successful client message whose access-token field contains the temporary authorization code
     * @throws NullPointerException if an argument is null
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        return Message.success(VendorTokenSet.builder().token(inbound.value("code").orElse(null)).build());
    }

    /**
     * Signs and sends a DingTalk temporary-code profile request through the injected Fabric context.
     *
     * @param context immutable root operation context used to resolve the DingTalk client secret
     * @param token   token set containing the temporary authorization code
     * @return successful client message containing the mapped DingTalk identity
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint, secret, or DingTalk response is invalid
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final String timestamp = Long.toString(clock.now().toEpochMilli());
        final String signature = sign(secret(current), timestamp);
        final String url = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.USERINFO))
                .queryParam("signature", signature).queryParam("timestamp", timestamp)
                .queryParam("accessKey", registration.clientId()).build();
        final UserResponse response = JsonKit.toPojo(
                post(
                        url,
                        JsonKit.toJsonString(Map.of("tmp_auth_code", authorization.getToken())),
                        MediaType.APPLICATION_JSON),
                UserResponse.class);
        if (response == null) {
            throw new AuthorizedException("Failed to parse DingTalk response: empty response");
        }
        if (response.errcode() != 0) {
            throw new AuthorizedException(response.errmsg() == null ? "Unknown DingTalk error" : response.errmsg());
        }
        final UserData user = response.user_info();
        if (user == null || user.unionid() == null) {
            throw new AuthorizedException("Missing user_info.unionid in DingTalk response");
        }
        final VendorTokenSet identityToken = VendorTokenSet.builder().openId(user.openid()).unionId(user.unionid())
                .build();
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(user)).uuid(user.unionid()).nickname(user.nick())
                        .username(user.nick()).gender(Gender.UNKNOWN).source(descriptor().id()).token(identityToken)
                        .build());
    }

    /**
     * Typed DingTalk account profile response.
     *
     * @param errcode   zero for success, otherwise the DingTalk error code
     * @param errmsg    vendor diagnostic text
     * @param user_info nested user profile
     * @author Kimi Liu
     */
    private record UserResponse(int errcode, String errmsg, UserData user_info) {
    }

    /**
     * Typed DingTalk account profile data.
     *
     * @param openid  application-scoped user identifier
     * @param unionid cross-application user identifier
     * @param nick    display name
     * @author Kimi Liu
     */
    private record UserData(String openid, String unionid, String nick) {
    }

}
