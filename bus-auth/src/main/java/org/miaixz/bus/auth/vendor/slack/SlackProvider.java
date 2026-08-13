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
package org.miaixz.bus.auth.vendor.slack;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party client for Slack authorization, token, profile, and revocation operations.
 *
 * @author Kimi Liu
 */
public class SlackProvider extends AbstractProvider {

    /**
     * Creates a Slack client from explicit runtime dependencies.
     *
     * @param configuration complete non-null vendor dependency aggregate
     */
    public SlackProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.SLACK);
    }

    /**
     * Returns the fixed Slack form content-type header.
     *
     * @return immutable header map
     */
    private static Map<String, String> formHeaders() {
        return Map.of(Http.Header.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
    }

    /**
     * Validates Slack's common response status without exposing credentials.
     *
     * @param ok       success flag
     * @param error    optional error name
     * @param metadata optional diagnostic metadata
     * @throws AuthorizedException when Slack does not report success
     */
    private static void validate(final Boolean ok, final String error, final Metadata metadata) {
        if (!Boolean.TRUE.equals(ok)) {
            final String message = error == null ? "Slack request failed" : error;
            final List<String> details = metadata == null ? null : metadata.messages();
            throw new AuthorizedException(details == null || details.isEmpty() ? message
                    : message + Symbol.SEMICOLON + String.join(Symbol.COMMA, details));
        }
    }

    /**
     * Builds the Slack authorization URL with comma-delimited encoded scopes.
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
                        .queryParam("client_id", registration.clientId()).queryParam("state", state(current, state))
                        .queryParam("redirect_uri", registration.redirectUri())
                        .queryParam("scope", scopes(Symbol.COMMA, true, getScopes(SlackScope.values()))).build());
    }

    /**
     * Exchanges a Slack callback with a credential-bearing GET request.
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
                .queryParam("code", inbound.value("code").orElse(null)).queryParam("client_id", registration.clientId())
                .queryParam("client_secret", secret(current)).queryParam("redirect_uri", registration.redirectUri())
                .build();
        final TokenResponse response = JsonKit.toPojo(get(url, null, formHeaders()), TokenResponse.class);
        validate(
                response == null ? null : response.ok(),
                response == null ? null : response.error(),
                response == null ? null : response.response_metadata());
        if (response.access_token() == null || response.authed_user() == null || response.authed_user().id() == null) {
            throw new AuthorizedException("Slack token response is missing required fields");
        }
        return Message.success(
                VendorTokenSet.builder().token(response.access_token()).scope(response.scope())
                        .tokenType(response.token_type()).uid(response.authed_user().id()).build());
    }

    /**
     * Retrieves a Slack user and profile with a Bearer GET request.
     *
     * @param context root operation context
     * @param token   non-null token set containing a Slack user identifier
     * @return successful message containing the mapped identity
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final Map<String, String> headers = new java.util.LinkedHashMap<>(formHeaders());
        headers.put(Http.Header.AUTHORIZATION, Http.Auth.BEARER_PREFIX + authorization.getToken());
        final String url = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.USERINFO))
                .queryParam("user", authorization.getUid()).build();
        final UserResponse response = JsonKit.toPojo(get(url, null, headers), UserResponse.class);
        validate(
                response == null ? null : response.ok(),
                response == null ? null : response.error(),
                response == null ? null : response.response_metadata());
        if (response.user() == null || response.user().id() == null) {
            throw new AuthorizedException("Slack user response is missing required fields");
        }
        final User user = response.user();
        final Profile profile = user.profile();
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(user)).uuid(user.id()).username(user.name())
                        .nickname(user.real_name()).avatar(profile == null ? null : profile.image_original())
                        .email(profile == null ? null : profile.email()).gender(Gender.UNKNOWN).token(authorization)
                        .source(descriptor().id()).build());
    }

    /**
     * Revokes a Slack token with a Bearer GET request.
     *
     * @param context root operation context
     * @param token   non-null token set
     * @return success when Slack reports {@code revoked=true}; otherwise the standard failure
     */
    @Override
    public Message<Void> revoke(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final Map<String, String> headers = new java.util.LinkedHashMap<>(formHeaders());
        headers.put(Http.Header.AUTHORIZATION, Http.Auth.BEARER_PREFIX + authorization.getToken());
        final RevokeResponse response = JsonKit
                .toPojo(get(endpoint(VendorEndpoint.REVOKE), null, headers), RevokeResponse.class);
        validate(
                response == null ? null : response.ok(),
                response == null ? null : response.error(),
                response == null ? null : response.response_metadata());
        return Boolean.TRUE.equals(response.revoked()) ? Message.success(null) : Message.failure(ErrorCode._FAILURE);
    }

    /**
     * Slack token response.
     *
     * @param ok                success flag
     * @param error             optional error name
     * @param response_metadata optional diagnostic metadata
     * @param access_token      access token
     * @param scope             granted scopes
     * @param token_type        token type
     * @param authed_user       authorized user
     * @author Kimi Liu
     */
    private record TokenResponse(Boolean ok, String error, Metadata response_metadata, String access_token,
            String scope, String token_type, AuthedUser authed_user) {
    }

    /**
     * Authorized Slack user.
     *
     * @param id stable user identifier
     * @author Kimi Liu
     */
    private record AuthedUser(String id) {
    }

    /**
     * Slack user response.
     *
     * @param ok                success flag
     * @param error             optional error name
     * @param response_metadata optional diagnostic metadata
     * @param user              user payload
     * @author Kimi Liu
     */
    private record UserResponse(Boolean ok, String error, Metadata response_metadata, User user) {
    }

    /**
     * Slack user payload.
     *
     * @param id        stable identifier
     * @param name      account name
     * @param real_name display name
     * @param profile   profile payload
     * @author Kimi Liu
     */
    private record User(String id, String name, String real_name, Profile profile) {
    }

    /**
     * Slack profile payload.
     *
     * @param image_original original avatar URL
     * @param email          email address
     * @author Kimi Liu
     */
    private record Profile(String image_original, String email) {
    }

    /**
     * Slack revocation response.
     *
     * @param ok                success flag
     * @param error             optional error name
     * @param response_metadata optional diagnostic metadata
     * @param revoked           revocation result
     * @author Kimi Liu
     */
    private record RevokeResponse(Boolean ok, String error, Metadata response_metadata, Boolean revoked) {
    }

    /**
     * Slack response diagnostics.
     *
     * @param messages diagnostic messages
     * @author Kimi Liu
     */
    private record Metadata(List<String> messages) {
    }

}
