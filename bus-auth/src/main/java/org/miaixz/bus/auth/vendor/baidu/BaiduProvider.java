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
package org.miaixz.bus.auth.vendor.baidu;

import java.util.Objects;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party authentication client for Baidu OAuth token, profile, refresh, and revocation operations.
 *
 * <p>
 * Every remote call uses the injected Fabric context. State is registered atomically, client secrets are resolved for
 * token-bearing operations only, and all vendor documents are mapped through typed response records.
 * </p>
 *
 * @author Kimi Liu
 */
public class BaiduProvider extends AbstractProvider {

    /**
     * Baidu portrait URL template used by the fixed profile mapping.
     */
    private static final String AVATAR_TEMPLATE = "http://himg.bdimg.com/sys/portrait/item/%s.jpg";

    /**
     * Creates a Baidu client from the complete immutable dependency aggregate.
     *
     * @param configuration non-null registration, Fabric, clock, state-store, cache, and secret dependencies
     * @throws NullPointerException if the configuration or one of its required dependencies is null
     * @throws AuthorizedException  if the registration is invalid for Baidu
     */
    public BaiduProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.BAIDU);
    }

    /**
     * Parses and validates one Baidu token response.
     *
     * @param json token response document
     * @return mapped token set
     * @throws AuthorizedException if the response is empty, reports an error, or omits the access token
     */
    private static VendorTokenSet readToken(final String json) {
        final TokenResponse response = JsonKit.toPojo(json, TokenResponse.class);
        validate(response);
        if (response.access_token() == null) {
            throw new AuthorizedException("Missing access_token in response");
        }
        return VendorTokenSet.builder().token(response.access_token()).refresh(response.refresh_token())
                .scope(response.scope()).expireIn(response.expires_in()).build();
    }

    /**
     * Rejects an empty response or either standard Baidu error shape.
     *
     * @param response typed Baidu response
     * @throws AuthorizedException if the response is null or reports an error
     */
    private static void validate(final BaiduResponse response) {
        if (response == null) {
            throw new AuthorizedException("Failed to parse Baidu response: empty response");
        }
        if (response.error() != null || response.error_code() != null) {
            final String message = response.error_description() == null ? response.error_msg()
                    : response.error_description();
            throw new AuthorizedException(message == null ? "Unknown error" : message);
        }
    }

    /**
     * Builds the Baidu popup authorization URL and atomically registers state.
     *
     * @param context immutable root operation context used for state ownership
     * @param state   optional caller-supplied state; a generated state is used when absent
     * @return successful client message containing the complete authorization URL
     * @throws NullPointerException if {@code context} is null
     * @throws AuthorizedException  if state registration fails
     */
    @Override
    public Message<String> build(final Context context, final String state) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        return Message.success(
                VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.AUTHORIZE)).queryParam("response_type", "code")
                        .queryParam("client_id", registration.clientId())
                        .queryParam("redirect_uri", registration.redirectUri())
                        .queryParam("state", state(current, state)).queryParam("display", "popup")
                        .queryParam("scope", scopes(Symbol.SPACE, true, getScopes(BaiduScope.values()))).build());
    }

    /**
     * Exchanges the Baidu callback authorization code through the token endpoint.
     *
     * @param context  immutable root operation context used to resolve the client secret
     * @param callback immutable inbound callback containing the standard {@code code} parameter
     * @return successful client message containing the mapped Baidu token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if Baidu returns an invalid token document
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        return Message.success(readToken(doPostToken(current, inbound.value("code").orElse(null))));
    }

    /**
     * Retrieves and maps the Baidu profile associated with an access token.
     *
     * @param context immutable root operation context for this profile operation
     * @param token   non-null Baidu token set
     * @return successful client message containing the mapped Baidu identity
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if Baidu returns an invalid profile document
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final UserResponse response = JsonKit.toPojo(doGetUserInfo(authorization), UserResponse.class);
        validate(response);
        final String userId = response.userid() == null ? response.openid() : response.userid();
        if (userId == null) {
            throw new AuthorizedException("Missing userid or openid in response");
        }
        final String avatar = StringKit.isEmpty(response.portrait()) ? null
                : String.format(AVATAR_TEMPLATE, response.portrait());
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(response)).uuid(userId)
                        .username(response.username()).nickname(response.username()).avatar(avatar)
                        .remark(response.userdetail()).gender(Gender.of(response.sex())).token(authorization)
                        .source(descriptor().id()).build());
    }

    /**
     * Refreshes a Baidu access token through the configured refresh endpoint.
     *
     * @param context immutable root operation context used to resolve the client secret
     * @param token   non-null token set containing the refresh token
     * @return successful client message containing the refreshed token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if Baidu returns an invalid token document
     */
    @Override
    public Message<VendorTokenSet> refresh(final Context context, final VendorTokenSet token) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final String url = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.REFRESH))
                .queryParam("grant_type", "refresh_token").queryParam("refresh_token", authorization.getRefresh())
                .queryParam("client_id", registration.clientId()).queryParam("client_secret", secret(current)).build();
        return Message.success(readToken(get(url)));
    }

    /**
     * Revokes a Baidu access token and maps the numeric result flag into a client Message.
     *
     * @param context immutable root operation context for this revocation
     * @param token   non-null token set containing the access token
     * @return success when Baidu returns result {@code 1}; otherwise the standard failure Message
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if Baidu returns an invalid revocation document
     */
    @Override
    public Message<Void> revoke(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final RevokeResponse response = JsonKit.toPojo(doGetRevoke(authorization), RevokeResponse.class);
        validate(response);
        final Errors status = response.result() == 1 ? ErrorCode._SUCCESS : ErrorCode._FAILURE;
        return Message.<Void>builder().errcode(status.getKey()).errmsg(status.getValue()).build();
    }

    /**
     * Common Baidu error fields.
     *
     * @author Kimi Liu
     */
    private interface BaiduResponse {

        /**
         * Returns the textual OAuth error code.
         *
         * @return textual error code, or null
         */
        String error();

        /**
         * Returns the legacy error code.
         *
         * @return numeric or textual legacy error code, or null
         */
        Object error_code();

        /**
         * Returns the OAuth diagnostic description.
         *
         * @return OAuth error description, or null
         */
        String error_description();

        /**
         * Returns the legacy diagnostic message.
         *
         * @return legacy error message, or null
         */
        String error_msg();
    }

    /**
     * Typed Baidu token response.
     *
     * @param access_token      access token
     * @param refresh_token     refresh token
     * @param scope             granted scope text
     * @param expires_in        access-token lifetime in seconds
     * @param error             OAuth error code
     * @param error_code        legacy error code
     * @param error_description OAuth diagnostic message
     * @param error_msg         legacy diagnostic message
     * @author Kimi Liu
     */
    private record TokenResponse(String access_token, String refresh_token, String scope, int expires_in, String error,
            Object error_code, String error_description, String error_msg) implements BaiduResponse {
    }

    /**
     * Typed Baidu profile response.
     *
     * @param userid            modern user identifier
     * @param openid            legacy user identifier
     * @param username          display name
     * @param userdetail        profile remark
     * @param sex               vendor gender value
     * @param portrait          portrait resource identifier
     * @param error             OAuth error code
     * @param error_code        legacy error code
     * @param error_description OAuth diagnostic message
     * @param error_msg         legacy diagnostic message
     * @author Kimi Liu
     */
    private record UserResponse(String userid, String openid, String username, String userdetail, String sex,
            String portrait, String error, Object error_code, String error_description, String error_msg)
            implements BaiduResponse {
    }

    /**
     * Typed Baidu revocation response.
     *
     * @param result            one for successful revocation
     * @param error             OAuth error code
     * @param error_code        legacy error code
     * @param error_description OAuth diagnostic message
     * @param error_msg         legacy diagnostic message
     * @author Kimi Liu
     */
    private record RevokeResponse(int result, String error, Object error_code, String error_description,
            String error_msg) implements BaiduResponse {
    }

}
