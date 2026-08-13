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
package org.miaixz.bus.auth.vendor.taobao;

import java.util.Objects;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.url.UrlDecoder;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party client for Taobao authorization, deferred token exchange, profile, and refresh operations.
 *
 * @author Kimi Liu
 */
public class TaobaoProvider extends AbstractProvider {

    /**
     * Creates a Taobao client from explicit runtime dependencies.
     *
     * @param configuration complete non-null vendor dependency aggregate
     */
    public TaobaoProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.TAOBAO);
    }

    /**
     * Parses and validates a Taobao token response.
     *
     * @param document JSON response
     * @return validated response
     */
    private static Response read(final String document) {
        final Response response = JsonKit.toPojo(document, Response.class);
        if (response == null) {
            throw new AuthorizedException("Taobao token response is empty");
        }
        if (response.error() != null) {
            throw new AuthorizedException(response.error_description() == null ? "Taobao token request failed"
                    : response.error_description());
        }
        if (response.access_token() == null) {
            throw new AuthorizedException("Taobao token response is missing access_token");
        }
        return response;
    }

    /**
     * Maps Taobao token fields.
     *
     * @param response validated response
     * @return mapped token set
     */
    private static VendorTokenSet map(final Response response) {
        return VendorTokenSet.builder().token(response.access_token()).expireIn(response.expires_in())
                .tokenType(response.token_type()).idToken(response.id_token()).refresh(response.refresh_token())
                .uid(response.taobao_user_id()).openId(response.taobao_open_uid()).build();
    }

    /**
     * Builds the Taobao web authorization URL.
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
                        .queryParam("client_id", registration.clientId())
                        .queryParam("redirect_uri", registration.redirectUri()).queryParam("view", "web")
                        .queryParam("state", state(current, state)).build());
    }

    /**
     * Preserves the callback code locally as the deferred Taobao token.
     *
     * @param context  root operation context
     * @param callback immutable inbound callback
     * @return successful message containing the callback code
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        return Message.success(VendorTokenSet.builder().token(inbound.value("code").orElse(null)).build());
    }

    /**
     * Exchanges the deferred callback code and maps the returned Taobao identity.
     *
     * @param context root operation context used for secret resolution
     * @param token   deferred token containing the callback code
     * @return successful message containing the mapped identity and real token set
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        Objects.requireNonNull(token, "Token set must not be null");
        final Response response = read(post(tokenUrl(current, token.getToken())));
        final VendorTokenSet authorization = map(response);
        final String nickname = UrlDecoder.decode(response.taobao_user_nick());
        final String identifier = StringKit.isEmpty(authorization.getUid()) ? authorization.getOpenId()
                : authorization.getUid();
        if (identifier == null) {
            throw new AuthorizedException("Taobao token response is missing a user identifier");
        }
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(response)).uuid(identifier).username(nickname)
                        .nickname(nickname).gender(Gender.UNKNOWN).token(authorization).source(descriptor().id())
                        .build());
    }

    /**
     * Refreshes a Taobao token with a query-bearing empty form POST.
     *
     * @param context root operation context used for secret resolution
     * @param token   non-null token set containing a refresh token
     * @return successful message containing the refreshed token set
     */
    @Override
    public Message<VendorTokenSet> refresh(final Context context, final VendorTokenSet token) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        return Message.success(map(read(post(refreshUrl(current, authorization.getRefresh())))));
    }

    /**
     * Taobao token and identity response.
     *
     * @param error             optional error identifier
     * @param error_description optional error description
     * @param access_token      access token
     * @param expires_in        access-token lifetime in seconds
     * @param token_type        token type
     * @param id_token          identity token
     * @param refresh_token     refresh token
     * @param taobao_user_id    Taobao user identifier
     * @param taobao_open_uid   Taobao OpenUID
     * @param taobao_user_nick  URL-encoded nickname
     * @author Kimi Liu
     */
    private record Response(Object error, String error_description, String access_token, int expires_in,
            String token_type, String id_token, String refresh_token, String taobao_user_id, String taobao_open_uid,
            String taobao_user_nick) {
    }

}
