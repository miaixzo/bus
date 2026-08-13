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
package org.miaixz.bus.auth.vendor.rednote;

import java.util.LinkedHashMap;
import java.util.Map;
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
 * Third-party client for Rednote marketing authorization, token, and refresh operations.
 *
 * @author Kimi Liu
 */
public class RednoteMarketiProvider extends AbstractProvider {

    /**
     * Creates the client.
     *
     * @param configuration non-null runtime dependencies
     */
    public RednoteMarketiProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.REDNOTE_MARKET);
    }

    /**
     * Parses a response.
     *
     * @param document JSON response
     * @return typed response
     * @throws AuthorizedException on failure
     */
    private static Response read(final String document) {
        final Response response = JsonKit.toPojo(document, Response.class);
        if (response == null)
            throw new AuthorizedException("Failed to parse access token response: empty response");
        if (response.code() != 0)
            throw new AuthorizedException(response.error() == null ? "Unknown error" : response.error());
        if (response.error() != null)
            throw new AuthorizedException((response.sub_error() == null ? "Unknown sub_error" : response.sub_error())
                    + Symbol.COLON
                    + (response.error_description() == null ? "Unknown description" : response.error_description()));
        if (response.access_token() == null)
            throw new AuthorizedException("Missing access_token in response");
        return response;
    }

    /**
     * Maps token fields.
     *
     * @param response response
     * @param initial  initial exchange flag
     * @return token set
     */
    private static VendorTokenSet token(final Response response, final boolean initial) {
        return VendorTokenSet.builder().token(response.access_token()).refresh(response.refresh_token())
                .scope(response.scope()).expireIn(initial ? response.access_token_expires_in() : response.expires_in())
                .build();
    }

    /**
     * Builds the marketing consent URL.
     *
     * @param context root context
     * @param state   optional state
     * @return authorization URL
     */
    @Override
    public Message<String> build(final Context context, final String state) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        return Message.success(
                VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.AUTHORIZE))
                        .queryParam("appId", registration.clientId())
                        .queryParam("scope", scopes(Symbol.SPACE, true, getScopes(RednoteMarketiScope.values())))
                        .queryParam("redirectUri", registration.redirectUri())
                        .queryParam("state", state(current, state)).build());
    }

    /**
     * Exchanges a code through a three-field form POST.
     *
     * @param context  root context
     * @param callback callback
     * @return token result
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final Map<String, String> form = new LinkedHashMap<>();
        form.put("app_id", registration.clientId());
        form.put("secret", secret(current));
        form.put("code", inbound.value("code").orElse(null));
        return Message.success(token(read(post(endpoint(VendorEndpoint.TOKEN), form)), true));
    }

    /**
     * Refreshes a token through a three-field form POST.
     *
     * @param context root context
     * @param token   token set
     * @return refreshed token
     */
    @Override
    public Message<VendorTokenSet> refresh(final Context context, final VendorTokenSet token) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final Map<String, String> form = new LinkedHashMap<>();
        form.put("app_id", registration.clientId());
        form.put("secret", secret(current));
        form.put("refresh_token", authorization.getRefresh());
        return Message.success(token(read(post(endpoint(VendorEndpoint.REFRESH), form)), false));
    }

    /**
     * Typed response.
     *
     * @param code                    code
     * @param error                   error
     * @param sub_error               sub-error
     * @param error_description       diagnostic
     * @param access_token            token
     * @param access_token_expires_in initial lifetime
     * @param refresh_token           refresh token
     * @param scope                   scope
     * @param expires_in              refresh lifetime
     * @author Kimi Liu
     */
    private record Response(int code, String error, String sub_error, String error_description, String access_token,
            int access_token_expires_in, String refresh_token, String scope, int expires_in) {
    }

}
