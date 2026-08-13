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
package org.miaixz.bus.auth.vendor.toutiao;

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
 * Third-party client for Toutiao authorization, token, and profile operations.
 *
 * @author Kimi Liu
 */
public class ToutiaoProvider extends AbstractProvider {

    /**
     * Creates a Toutiao client from explicit runtime dependencies.
     *
     * @param configuration complete non-null vendor dependency aggregate
     */
    public ToutiaoProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.TOUTIAO);
    }

    /**
     * Rejects a Toutiao error code through the package-private exact error table.
     *
     * @param errorCode optional remote error code
     * @throws AuthorizedException when an error code is present
     */
    private static void check(final String errorCode) {
        if (errorCode != null) {
            throw new AuthorizedException(ToutiaoErrors.getErrorCode(errorCode).getValue());
        }
    }

    /**
     * Builds the Toutiao authorization URL and atomically registers its state.
     *
     * @param context root operation context
     * @param state   optional authorization state
     * @return successful message containing the authorization URL
     */
    @Override
    public Message<String> build(final Context context, final String state) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        return Message.success(
                VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.AUTHORIZE)).queryParam("response_type", "code")
                        .queryParam("client_key", registration.clientId())
                        .queryParam("redirect_uri", registration.redirectUri()).queryParam("auth_only", 1)
                        .queryParam("display", 0).queryParam("state", state(current, state)).build());
    }

    /**
     * Exchanges the authorization code through Toutiao's query-bearing empty form POST.
     *
     * @param context  root operation context used for secret resolution
     * @param callback immutable inbound callback
     * @return successful message containing the mapped token set
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final String url = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.TOKEN))
                .queryParam("code", inbound.value("code").orElse(null))
                .queryParam("client_key", registration.clientId()).queryParam("client_secret", secret(current))
                .queryParam("grant_type", "authorization_code").build();
        final TokenResponse response = JsonKit.toPojo(post(url), TokenResponse.class);
        check(response == null ? null : response.error_code());
        if (response == null || response.access_token() == null) {
            throw new AuthorizedException("Toutiao token response is missing access_token");
        }
        return Message.success(
                VendorTokenSet.builder().token(response.access_token()).expireIn(response.expires_in())
                        .openId(response.open_id()).build());
    }

    /**
     * Retrieves the Toutiao profile using the client key and access token query fields.
     *
     * @param context root operation context
     * @param token   non-null token set
     * @return successful message containing the mapped identity
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final String url = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.USERINFO))
                .queryParam("client_key", registration.clientId()).queryParam("access_token", authorization.getToken())
                .build();
        final ProfileResponse response = JsonKit.toPojo(get(url), ProfileResponse.class);
        check(response == null ? null : response.error_code());
        if (response == null || response.data() == null || response.data().uid() == null) {
            throw new AuthorizedException("Toutiao profile response is missing data.uid");
        }
        final User user = response.data();
        final String displayName = "14".equals(user.uid_type()) ? "Anonymous User" : user.screen_name();
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(response)).uuid(user.uid()).username(displayName)
                        .nickname(displayName).avatar(user.avatar_url()).remark(user.description())
                        .gender(Gender.of(user.gender())).token(authorization).source(descriptor().id()).build());
    }

    /**
     * Toutiao token response.
     *
     * @param error_code   optional remote error code
     * @param access_token access token
     * @param expires_in   access-token lifetime in seconds
     * @param open_id      stable token owner identifier
     * @author Kimi Liu
     */
    private record TokenResponse(String error_code, String access_token, int expires_in, String open_id) {
    }

    /**
     * Toutiao profile envelope.
     *
     * @param error_code optional remote error code
     * @param data       profile payload
     * @author Kimi Liu
     */
    private record ProfileResponse(String error_code, User data) {
    }

    /**
     * Toutiao profile payload.
     *
     * @param uid         stable user identifier
     * @param uid_type    user-identifier category
     * @param screen_name display name
     * @param avatar_url  avatar URL
     * @param description profile description
     * @param gender      vendor gender text
     * @author Kimi Liu
     */
    private record User(String uid, String uid_type, String screen_name, String avatar_url, String description,
            String gender) {
    }

}
