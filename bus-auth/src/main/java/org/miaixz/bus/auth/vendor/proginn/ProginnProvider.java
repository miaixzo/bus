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
package org.miaixz.bus.auth.vendor.proginn;

import java.util.LinkedHashMap;
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
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party client for Proginn authorization, token exchange, and profile retrieval.
 *
 * @author Kimi Liu
 */
public class ProginnProvider extends AbstractProvider {

    /**
     * Creates a Proginn client from explicit runtime dependencies.
     *
     * @param configuration non-null registration, Fabric, clock, state-store, cache, and secret dependencies
     */
    public ProginnProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.PROGINN);
    }

    /**
     * Parses and validates a Proginn response.
     *
     * @param document JSON response document
     * @return parsed response
     * @throws AuthorizedException if parsing fails or the response reports an error
     */
    private static Response read(final String document) {
        final Response response = JsonKit.toPojo(document, Response.class);
        if (response == null)
            throw new AuthorizedException("Failed to parse Proginn response: empty response");
        if (response.error() != null)
            throw new AuthorizedException(response.error_description());
        return response;
    }

    /**
     * Builds the standard consent URL with Proginn default scopes and atomically registered state.
     *
     * @param context immutable root operation context
     * @param state   optional authorization state
     * @return successful message containing the authorization URL
     */
    @Override
    public Message<String> build(final Context context, final String state) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        return Message.success(
                VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.AUTHORIZE)).queryParam("response_type", "code")
                        .queryParam("client_id", registration.clientId())
                        .queryParam("redirect_uri", registration.redirectUri())
                        .queryParam("state", state(current, state))
                        .queryParam("scope", scopes(Symbol.SPACE, true, getScopes(ProginnScope.values()))).build());
    }

    /**
     * Exchanges an authorization code through a form POST.
     *
     * @param context  root context used to resolve and clear the client secret
     * @param callback inbound callback containing the authorization code
     * @return successful message containing token fields
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final Map<String, String> form = new LinkedHashMap<>();
        form.put("code", inbound.value("code").orElse(null));
        form.put("client_id", registration.clientId());
        form.put("client_secret", secret(current));
        form.put("grant_type", "authorization_code");
        form.put("redirect_uri", registration.redirectUri());
        final Response response = read(post(endpoint(VendorEndpoint.TOKEN), form));
        return Message.success(
                VendorTokenSet.builder().token(response.access_token()).refresh(response.refresh_token())
                        .uid(response.uid()).tokenType(response.token_type()).expireIn(response.expires_in()).build());
    }

    /**
     * Retrieves the Proginn profile with the access token in the query.
     *
     * @param context immutable root operation context
     * @param token   non-null token set
     * @return successful message containing the mapped identity
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final Response response = read(
                get(endpoint(VendorEndpoint.USERINFO), Map.of("access_token", authorization.getToken())));
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(response)).uuid(response.uid())
                        .username(response.nickname()).nickname(response.nickname()).avatar(response.avatar())
                        .email(response.email()).gender(Gender.UNKNOWN).token(authorization).source(descriptor().id())
                        .build());
    }

    /**
     * Typed union of Proginn token, profile, and error fields.
     *
     * @param access_token      access token
     * @param refresh_token     refresh token
     * @param uid               user identifier
     * @param token_type        token type
     * @param expires_in        token lifetime in seconds
     * @param nickname          display name
     * @param avatar            avatar URL
     * @param email             email address
     * @param error             error code
     * @param error_description error diagnostic
     * @author Kimi Liu
     */
    private record Response(String access_token, String refresh_token, String uid, String token_type, int expires_in,
            String nickname, String avatar, String email, String error, String error_description) {
    }

}
