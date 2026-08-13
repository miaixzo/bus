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
package org.miaixz.bus.auth.vendor.google;

import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party client for Google OpenID Connect authorization, token, and profile operations.
 *
 * <p>
 * Every remote call uses the injected Fabric context. Authorization state uses the injected atomic store, and the
 * client secret is resolved only for the authorization-code exchange.
 * </p>
 *
 * @author Kimi Liu
 */
public class GoogleProvider extends AbstractProvider {

    /**
     * Creates a Google OpenID Connect client from explicit runtime dependencies.
     *
     * @param configuration non-null registration, Fabric, clock, state-store, cache, and secret dependencies
     * @throws NullPointerException if the configuration or a required dependency is null
     * @throws AuthorizedException  if the Google registration is invalid
     */
    public GoogleProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.GOOGLE);
    }

    /**
     * Parses and validates a Google operation response.
     *
     * @param json operation response document
     * @return validated typed response
     * @throws AuthorizedException if the response is empty or reports an error
     */
    private static Response read(final String json) {
        final Response response = JsonKit.toPojo(json, Response.class);
        if (response == null) {
            throw new AuthorizedException("Failed to parse Google response: empty response");
        }
        if (response.error() != null || response.error_description() != null) {
            throw new AuthorizedException((response.error() == null ? "Unknown error" : response.error()) + Symbol.COLON
                    + (response.error_description() == null ? "Unknown description" : response.error_description()));
        }
        return response;
    }

    /**
     * Builds the offline Google authorization URL and atomically registers state.
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
                        .queryParam("client_id", registration.clientId())
                        .queryParam("redirect_uri", registration.redirectUri())
                        .queryParam("state", state(current, state)).queryParam("access_type", "offline")
                        .queryParam("scope", scopes(Symbol.SPACE, false, getScopes(GoogleScope.values())))
                        .queryParam("prompt", "select_account").build());
    }

    /**
     * Exchanges an authorization code through Google's token POST endpoint.
     *
     * @param context  immutable root operation context used to resolve the client secret
     * @param callback immutable inbound callback containing the standard {@code code} parameter
     * @return successful client message containing mapped Google token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint, secret, or response is invalid
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final Response response = read(doPostToken(current, inbound.value("code").orElse(null)));
        if (response.access_token() == null) {
            throw new AuthorizedException("Missing access_token in Google token response");
        }
        return Message.success(
                VendorTokenSet.builder().token(response.access_token()).expireIn(response.expires_in())
                        .scope(response.scope()).tokenType(response.token_type()).refresh(response.refresh_token())
                        .refreshExpireIn(response.refresh_token_expires_in()).idToken(response.id_token()).build());
    }

    /**
     * Retrieves and maps the Google profile using the vendor's empty-form POST.
     *
     * @param context immutable root operation context for this profile request
     * @param token   non-null Google token set
     * @return successful client message containing the mapped Google identity
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint or response is invalid
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final String url = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.USERINFO))
                .queryParam("access_token", authorization.getToken()).build();
        final Response response = read(
                post(url, null, Map.of(Http.Header.AUTHORIZATION, Http.Auth.BEARER_PREFIX + authorization.getToken())));
        if (response.sub() == null) {
            throw new AuthorizedException("Missing sub in Google profile response");
        }
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(response)).uuid(response.sub())
                        .username(response.email()).avatar(response.picture()).nickname(response.name())
                        .location(response.locale()).email(response.email()).gender(Gender.UNKNOWN).token(authorization)
                        .source(descriptor().id()).build());
    }

    /**
     * Typed union of the frozen Google token, profile, and error fields.
     *
     * @param error                    vendor error code
     * @param error_description        vendor error description
     * @param access_token             sensitive access token
     * @param expires_in               access-token lifetime in seconds
     * @param scope                    granted scope text
     * @param token_type               access-token scheme
     * @param id_token                 signed OpenID Connect token
     * @param refresh_token            sensitive refresh token
     * @param refresh_token_expires_in refresh-token lifetime in seconds
     * @param sub                      stable subject identifier
     * @param email                    email address
     * @param picture                  profile image URL
     * @param name                     display name
     * @param locale                   locale identifier
     * @author Kimi Liu
     */
    private record Response(String error, String error_description, String access_token, int expires_in, String scope,
            String token_type, String id_token, String refresh_token, int refresh_token_expires_in, String sub,
            String email, String picture, String name, String locale) {
    }

}
