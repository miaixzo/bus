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
package org.miaixz.bus.auth.vendor.douyin;

import java.util.Objects;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party client for Douyin OAuth authorization, token, profile, and refresh operations.
 *
 * <p>
 * Every remote call uses the injected Fabric context. Authorization state uses the injected atomic store, and the
 * client secret is resolved only for the authorization-code exchange.
 * </p>
 *
 * @author Kimi Liu
 */
public class DouyinProvider extends AbstractProvider {

    /**
     * Creates a Douyin OAuth client from explicit runtime dependencies.
     *
     * @param configuration non-null registration, Fabric, clock, state-store, cache, and secret dependencies
     * @throws NullPointerException if the configuration or a required dependency is null
     * @throws AuthorizedException  if the Douyin registration is invalid
     */
    public DouyinProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.DOUYIN);
    }

    /**
     * Parses and validates one Douyin token response.
     *
     * @param json token response document
     * @return mapped token set
     * @throws AuthorizedException if the response is invalid or omits its access token
     */
    private static VendorTokenSet readToken(final String json) {
        final Data data = read(json).data();
        if (data.access_token() == null) {
            throw new AuthorizedException("Missing access_token in Douyin token response");
        }
        return VendorTokenSet.builder().token(data.access_token()).openId(data.open_id()).expireIn(data.expires_in())
                .refresh(data.refresh_token()).refreshExpireIn(data.refresh_expires_in()).scope(data.scope()).build();
    }

    /**
     * Parses and validates the common Douyin response envelope.
     *
     * @param json response document
     * @return validated typed response
     * @throws AuthorizedException if the response or data is absent or reports an error
     */
    private static Response read(final String json) {
        final Response response = JsonKit.toPojo(json, Response.class);
        if (response == null) {
            throw new AuthorizedException("Failed to parse Douyin response: empty response");
        }
        if (response.data() == null) {
            throw new AuthorizedException("Missing data field in Douyin response");
        }
        final String errorCode = response.data().error_code();
        if ("error".equals(response.message()) || !"0".equals(errorCode)) {
            final String description = response.data().description();
            throw new AuthorizedException(errorCode, description == null ? "Unknown error" : description);
        }
        return response;
    }

    /**
     * Builds the Douyin authorization URL and atomically registers state.
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
                        .queryParam("client_key", registration.clientId())
                        .queryParam("redirect_uri", registration.redirectUri())
                        .queryParam("scope", scopes(Symbol.COMMA, true, getScopes(DouyinScope.values())))
                        .queryParam("state", state(current, state)).build());
    }

    /**
     * Exchanges an authorization code through the Douyin token endpoint.
     *
     * @param context  immutable root operation context used to resolve the client secret
     * @param callback immutable inbound callback containing the standard {@code code} parameter
     * @return successful client message containing mapped Douyin token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint, secret, or response is invalid
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final String url = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.TOKEN))
                .queryParam("code", inbound.value("code").orElse(null))
                .queryParam("client_key", registration.clientId()).queryParam("client_secret", secret(current))
                .queryParam("grant_type", "authorization_code").queryParam("redirect_uri", registration.redirectUri())
                .build();
        return Message.success(readToken(post(url)));
    }

    /**
     * Retrieves and maps the Douyin profile associated with a token and open identifier.
     *
     * @param context immutable root operation context for this profile request
     * @param token   non-null Douyin token set
     * @return successful client message containing the mapped Douyin identity
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint or response is invalid
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final String url = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.USERINFO))
                .queryParam("access_token", authorization.getToken()).queryParam("open_id", authorization.getOpenId())
                .build();
        final Response response = read(get(url));
        final Data data = response.data();
        if (data.union_id() == null) {
            throw new AuthorizedException("Missing union_id in Douyin user response");
        }
        authorization.setUnionId(data.union_id());
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(data)).uuid(data.union_id())
                        .username(data.nickname()).nickname(data.nickname()).avatar(data.avatar())
                        .remark(data.description()).gender(Gender.of(data.gender()))
                        .location(String.format("%s %s %s", data.country(), data.province(), data.city()))
                        .token(authorization).source(descriptor().id()).build());
    }

    /**
     * Refreshes a Douyin access token using the existing refresh token.
     *
     * @param context immutable root operation context for this refresh request
     * @param token   non-null token set containing the refresh token
     * @return successful client message containing mapped refreshed token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint or response is invalid
     */
    @Override
    public Message<VendorTokenSet> refresh(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final String url = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.REFRESH))
                .queryParam("client_key", registration.clientId())
                .queryParam("refresh_token", authorization.getRefresh()).queryParam("grant_type", "refresh_token")
                .build();
        return Message.success(readToken(post(url)));
    }

    /**
     * Typed common Douyin response envelope.
     *
     * @param message vendor status text
     * @param data    token, profile, and error fields
     * @author Kimi Liu
     */
    private record Response(String message, Data data) {
    }

    /**
     * Typed union of the frozen Douyin token, profile, and error fields.
     *
     * @param error_code         vendor status code normalized as text
     * @param description        error description or profile description
     * @param access_token       access token
     * @param open_id            application-scoped user identifier
     * @param expires_in         access-token lifetime in seconds
     * @param refresh_token      refresh token
     * @param refresh_expires_in refresh-token lifetime in seconds
     * @param scope              granted scope text
     * @param union_id           cross-application user identifier
     * @param nickname           display name
     * @param avatar             avatar URL
     * @param gender             vendor gender text
     * @param country            country text
     * @param province           province text
     * @param city               city text
     * @author Kimi Liu
     */
    private record Data(String error_code, String description, String access_token, String open_id, int expires_in,
            String refresh_token, int refresh_expires_in, String scope, String union_id, String nickname, String avatar,
            String gender, String country, String province, String city) {
    }

}
