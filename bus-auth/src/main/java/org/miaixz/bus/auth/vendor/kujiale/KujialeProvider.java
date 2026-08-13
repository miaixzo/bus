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
package org.miaixz.bus.auth.vendor.kujiale;

import java.net.URI;
import java.util.Objects;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party client for Kujiale authorization, token, refresh, and profile operations.
 *
 * <p>
 * All remote calls use the injected Fabric context. Authorization state uses the injected atomic store, and client
 * secrets are resolved per token operation and cleared without being retained by this client.
 * </p>
 *
 * @author Kimi Liu
 */
public class KujialeProvider extends AbstractProvider {

    /**
     * Creates a Kujiale client from the complete immutable dependency aggregate.
     *
     * @param configuration non-null registration, Fabric, clock, state-store, cache, and secret dependencies
     * @throws NullPointerException if the configuration or a required dependency is null
     * @throws AuthorizedException  if the Kujiale registration is invalid
     */
    public KujialeProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.KUJIALE);
    }

    /**
     * Parses and validates a Kujiale token envelope.
     *
     * @param json token response document
     * @return mapped token set
     * @throws AuthorizedException if the envelope reports failure or omits token data
     */
    private static VendorTokenSet readToken(final String json) {
        final TokenResponse response = JsonKit.toPojo(json, TokenResponse.class);
        validate(response);
        if (response.d() == null || response.d().accessToken() == null) {
            throw new AuthorizedException("Missing accessToken in Kujiale response");
        }
        return VendorTokenSet.builder().token(response.d().accessToken()).refresh(response.d().refreshToken())
                .expireIn(response.d().expiresIn()).build();
    }

    /**
     * Rejects an empty Kujiale envelope or any nonzero vendor result code.
     *
     * @param response typed response envelope
     * @throws AuthorizedException if the response is empty or reports failure
     */
    private static void validate(final KujialeResponse response) {
        if (response == null) {
            throw new AuthorizedException("Failed to parse Kujiale response: empty response");
        }
        if (!Symbol.ZERO.equals(response.c())) {
            throw new AuthorizedException(response.m() == null ? "Unknown error" : response.m());
        }
    }

    /**
     * Builds the Kujiale authorization URL with comma-delimited scopes and atomically registers state.
     *
     * @param context immutable root operation context used for state ownership
     * @param state   optional caller-supplied state; a generated state is used when absent
     * @return successful client message containing the complete authorization URL
     * @throws NullPointerException if {@code context} is null
     * @throws AuthorizedException  if the endpoint or state registration is invalid
     */
    @Override
    public Message<String> build(final Context context, final String state) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        return Message.success(
                VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.AUTHORIZE)).queryParam("response_type", "code")
                        .queryParam("client_id", registration.clientId())
                        .queryParam("redirect_uri", registration.redirectUri())
                        .queryParam("state", state(current, state))
                        .queryParam("scope", scopes(Symbol.COMMA, false, getScopes(KujialeScope.values()))).build());
    }

    /**
     * Exchanges a Kujiale authorization code through the standard query-based empty-form POST.
     *
     * @param context  immutable root operation context used to resolve the client secret
     * @param callback immutable inbound callback containing the standard {@code code} parameter
     * @return successful client message containing mapped Kujiale token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint, secret, or response is invalid
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        return Message.success(readToken(post(tokenUrl(current, inbound.value("code").orElse(null)))));
    }

    /**
     * Resolves the token's OpenID and retrieves the corresponding Kujiale profile.
     *
     * @param context immutable root operation context for this profile operation
     * @param token   non-null Kujiale token set
     * @return successful client message containing the mapped Kujiale identity
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if either endpoint or response is invalid
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final OpenIdResponse openId = JsonKit.toPojo(
                get(
                        VendorRequestBuilder.fromUrl(openIdEndpoint())
                                .queryParam("access_token", authorization.getToken()).build()),
                OpenIdResponse.class);
        validate(openId);
        if (openId.d() == null) {
            throw new AuthorizedException("Missing OpenID in Kujiale response");
        }
        final String url = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.USERINFO))
                .queryParam("access_token", authorization.getToken()).queryParam("open_id", openId.d()).build();
        final ProfileResponse response = JsonKit.toPojo(get(url), ProfileResponse.class);
        validate(response);
        if (response.d() == null || response.d().userName() == null) {
            throw new AuthorizedException("Missing userName in Kujiale profile response");
        }
        final ProfileData data = response.d();
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(data)).username(data.userName())
                        .nickname(data.userName()).avatar(data.avatar()).uuid(data.openId()).token(authorization)
                        .source(descriptor().id()).build());
    }

    /**
     * Refreshes a Kujiale token through the standard query-based empty-form POST.
     *
     * @param context immutable root operation context used to resolve the client secret
     * @param token   non-null token set containing the refresh token
     * @return successful client message containing refreshed Kujiale token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint, secret, or response is invalid
     */
    @Override
    public Message<VendorTokenSet> refresh(final Context context, final VendorTokenSet token) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        return Message.success(readToken(post(refreshUrl(current, authorization.getRefresh()))));
    }

    /**
     * Resolves Kujiale's fixed {@code user} sibling from the effective token endpoint.
     *
     * @return effective OpenID endpoint preserving registration endpoint overrides
     */
    private String openIdEndpoint() {
        final URI token = URI.create(endpoint(VendorEndpoint.TOKEN));
        final String path = token.getPath();
        return token.resolve(path.substring(0, path.lastIndexOf('/') + 1) + "user").toString();
    }

    /**
     * Common Kujiale response status fields.
     *
     * @author Kimi Liu
     */
    private interface KujialeResponse {

        /**
         * Returns the vendor result code.
         *
         * @return result code
         */
        String c();

        /**
         * Returns the vendor diagnostic message.
         *
         * @return diagnostic message, or null when absent
         */
        String m();
    }

    /**
     * Kujiale token data.
     *
     * @param accessToken  sensitive access token
     * @param refreshToken sensitive refresh token
     * @param expiresIn    access-token lifetime in seconds
     * @author Kimi Liu
     */
    private record TokenData(String accessToken, String refreshToken, int expiresIn) {
    }

    /**
     * Kujiale token envelope.
     *
     * @param c vendor result code
     * @param m vendor diagnostic message
     * @param d token data
     * @author Kimi Liu
     */
    private record TokenResponse(String c, String m, TokenData d) implements KujialeResponse {
    }

    /**
     * Kujiale OpenID envelope.
     *
     * @param c vendor result code
     * @param m vendor diagnostic message
     * @param d client-scoped OpenID
     * @author Kimi Liu
     */
    private record OpenIdResponse(String c, String m, String d) implements KujialeResponse {
    }

    /**
     * Kujiale profile fields.
     *
     * @param userName account display name
     * @param avatar   profile image URL
     * @param openId   client-scoped OpenID
     * @author Kimi Liu
     */
    private record ProfileData(String userName, String avatar, String openId) {
    }

    /**
     * Kujiale profile envelope.
     *
     * @param c vendor result code
     * @param m vendor diagnostic message
     * @param d profile data
     * @author Kimi Liu
     */
    private record ProfileResponse(String c, String m, ProfileData d) implements KujialeResponse {
    }

}
