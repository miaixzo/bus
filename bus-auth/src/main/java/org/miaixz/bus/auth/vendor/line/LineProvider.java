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
package org.miaixz.bus.auth.vendor.line;

import java.util.LinkedHashMap;
import java.util.Map;
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
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party client for LINE authorization, token, refresh, profile, and revocation operations.
 *
 * <p>
 * All remote calls use the injected Fabric context. Authorization state uses the injected atomic store, and client
 * secrets are resolved per credential operation and cleared without being retained.
 * </p>
 *
 * @author Kimi Liu
 */
public class LineProvider extends AbstractProvider {

    /**
     * Creates a LINE client from the complete immutable dependency aggregate.
     *
     * @param configuration non-null registration, Fabric, clock, state-store, cache, and secret dependencies
     */
    public LineProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.LINE);
    }

    /**
     * Parses a LINE token response into the shared token model.
     *
     * @param json token response document
     * @return mapped token set
     * @throws AuthorizedException if the response is empty or omits the access token
     */
    private static VendorTokenSet readToken(final String json) {
        final TokenResponse response = JsonKit.toPojo(json, TokenResponse.class);
        if (response == null || response.access_token() == null) {
            throw new AuthorizedException("Missing access_token in LINE response");
        }
        return VendorTokenSet.builder().token(response.access_token()).refresh(response.refresh_token())
                .expireIn(response.expires_in()).idToken(response.id_token()).scope(response.scope())
                .tokenType(response.token_type()).build();
    }

    /**
     * Builds the LINE authorization URL with matching state and nonce values.
     *
     * @param context immutable root operation context used for state ownership
     * @param state   optional caller-supplied state
     * @return successful message containing the authorization URL
     */
    @Override
    public Message<String> build(final Context context, final String state) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final String actualState = state(current, state);
        return Message.success(
                VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.AUTHORIZE)).queryParam("response_type", "code")
                        .queryParam("client_id", registration.clientId())
                        .queryParam("redirect_uri", registration.redirectUri()).queryParam("state", actualState)
                        .queryParam("nonce", actualState)
                        .queryParam("scope", scopes(Symbol.SPACE, true, getScopes(LineScope.values()))).build());
    }

    /**
     * Exchanges an authorization code through LINE's token form endpoint.
     *
     * @param context  immutable root operation context used to resolve the client secret
     * @param callback immutable inbound callback containing the authorization code
     * @return successful message containing mapped token fields
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final Map<String, String> form = new LinkedHashMap<>();
        form.put("grant_type", "authorization_code");
        form.put("code", inbound.value("code").orElse(null));
        form.put("redirect_uri", registration.redirectUri());
        form.put("client_id", registration.clientId());
        form.put("client_secret", secret(current));
        return Message.success(readToken(post(endpoint(VendorEndpoint.TOKEN), form)));
    }

    /**
     * Retrieves the LINE profile with a Bearer authorization header.
     *
     * @param context immutable root operation context
     * @param token   non-null LINE token set
     * @return successful message containing the mapped identity
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final ProfileResponse response = JsonKit.toPojo(
                get(
                        endpoint(VendorEndpoint.USERINFO),
                        null,
                        Map.of(
                                Http.Header.CONTENT_TYPE,
                                "application/x-www-form-urlencoded",
                                Http.Header.AUTHORIZATION,
                                Http.Auth.BEARER_PREFIX + authorization.getToken())),
                ProfileResponse.class);
        if (response == null || response.userId() == null) {
            throw new AuthorizedException("Missing userId in LINE profile response");
        }
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(response)).uuid(response.userId())
                        .username(response.displayName()).nickname(response.displayName()).avatar(response.pictureUrl())
                        .remark(response.statusMessage()).gender(Gender.UNKNOWN).token(authorization)
                        .source(descriptor().id()).build());
    }

    /**
     * Refreshes a LINE access token through the token endpoint.
     *
     * @param context immutable root operation context used to resolve the client secret
     * @param token   non-null token set containing the refresh token
     * @return successful message containing refreshed token fields
     */
    @Override
    public Message<VendorTokenSet> refresh(final Context context, final VendorTokenSet token) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final Map<String, String> form = new LinkedHashMap<>();
        form.put("grant_type", "refresh_token");
        form.put("refresh_token", authorization.getRefresh());
        form.put("client_id", registration.clientId());
        form.put("client_secret", secret(current));
        return Message.success(readToken(post(endpoint(VendorEndpoint.TOKEN), form)));
    }

    /**
     * Revokes LINE authorization with the access token and resolved client credentials.
     *
     * @param context immutable root operation context used to resolve the client secret
     * @param token   non-null token set containing the access token
     * @return success or failure message matching LINE's revoked flag
     */
    @Override
    public Message<Void> revoke(final Context context, final VendorTokenSet token) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final Map<String, String> form = new LinkedHashMap<>();
        form.put("access_token", authorization.getToken());
        form.put("client_id", registration.clientId());
        form.put("client_secret", secret(current));
        final RevokeResponse response = JsonKit
                .toPojo(post(endpoint(VendorEndpoint.REVOKE), form), RevokeResponse.class);
        if (response == null) {
            throw new AuthorizedException("Failed to parse LINE revoke response: empty response");
        }
        final Errors status = response.revoked() ? ErrorCode._SUCCESS : ErrorCode._FAILURE;
        return Message.<Void>builder().errcode(status.getKey()).errmsg(status.getValue()).build();
    }

    /**
     * Typed LINE token response.
     *
     * @param access_token  sensitive access token
     * @param refresh_token sensitive refresh token
     * @param expires_in    access-token lifetime in seconds
     * @param id_token      signed OpenID Connect token
     * @param scope         granted scope text
     * @param token_type    token scheme label
     * @author Kimi Liu
     */
    private record TokenResponse(String access_token, String refresh_token, int expires_in, String id_token,
            String scope, String token_type) {
    }

    /**
     * Typed LINE profile response.
     *
     * @param userId        stable LINE user identifier
     * @param displayName   account display name
     * @param pictureUrl    profile image URL
     * @param statusMessage account status text
     * @author Kimi Liu
     */
    private record ProfileResponse(String userId, String displayName, String pictureUrl, String statusMessage) {
    }

    /**
     * Typed LINE revocation response.
     *
     * @param revoked whether authorization was revoked
     * @author Kimi Liu
     */
    private record RevokeResponse(boolean revoked) {
    }

}
