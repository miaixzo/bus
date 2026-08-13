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
package org.miaixz.bus.auth.vendor.aliyun;

import java.util.Objects;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party authentication client for Aliyun OpenID Connect token and user-information operations.
 *
 * <p>
 * The inherited authorization URL operation owns state registration. This class sends token and user-information
 * requests only through the injected Fabric context and maps the fixed Aliyun response fields into vendor client
 * results.
 * </p>
 *
 * @author Kimi Liu
 */
public class AliyunProvider extends AbstractProvider {

    /**
     * Creates an Aliyun client from the complete immutable dependency aggregate.
     *
     * @param configuration non-null registration, Fabric, clock, state-store, cache, and secret dependencies
     * @throws NullPointerException if the configuration or one of its required dependencies is null
     * @throws AuthorizedException  if the registration is invalid for Aliyun
     */
    public AliyunProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.ALIYUN);
    }

    /**
     * Exchanges the standard callback authorization code at the Aliyun token endpoint.
     *
     * @param context  immutable root operation context used to resolve the client secret
     * @param callback immutable inbound callback containing the standard {@code code} parameter
     * @return successful client message containing the mapped Aliyun token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if Aliyun returns an empty or malformed token document
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final TokenResponse response = JsonKit
                .toPojo(doPostToken(current, inbound.value("code").orElse(null)), TokenResponse.class);
        if (response == null) {
            throw new AuthorizedException(VendorErrors._110004);
        }
        return Message.success(
                VendorTokenSet.builder().token(response.access_token()).expireIn(response.expires_in())
                        .tokenType(response.token_type()).idToken(response.id_token()).refresh(response.refresh_token())
                        .build());
    }

    /**
     * Retrieves and maps the Aliyun identity associated with an access token.
     *
     * @param context immutable root operation context for this user-information operation
     * @param token   non-null Aliyun token set whose access token is sent to the user-info endpoint
     * @return successful client message containing the mapped Aliyun identity
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if Aliyun returns an empty or malformed user document
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final UserResponse response = JsonKit.toPojo(doGetUserInfo(authorization), UserResponse.class);
        if (response == null) {
            throw new AuthorizedException(VendorErrors._110004);
        }
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(response)).uuid(response.sub())
                        .username(response.login_name()).nickname(response.name()).gender(Gender.UNKNOWN)
                        .token(authorization).source(descriptor().id()).build());
    }

    /**
     * Typed Aliyun token response.
     *
     * @param access_token  access token
     * @param expires_in    access-token lifetime in seconds
     * @param token_type    token scheme label
     * @param id_token      OpenID Connect identity token
     * @param refresh_token refresh token
     * @author Kimi Liu
     */
    private record TokenResponse(String access_token, int expires_in, String token_type, String id_token,
            String refresh_token) {
    }

    /**
     * Typed Aliyun user-information response.
     *
     * @param sub        stable Aliyun subject identifier
     * @param login_name account login name
     * @param name       display name
     * @author Kimi Liu
     */
    private record UserResponse(String sub, String login_name, String name) {
    }

}
