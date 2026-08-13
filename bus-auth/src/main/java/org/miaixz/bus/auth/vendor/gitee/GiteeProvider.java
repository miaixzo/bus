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
package org.miaixz.bus.auth.vendor.gitee;

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
 * Third-party client for Gitee OAuth authorization, token, and profile operations.
 *
 * <p>
 * Every remote call uses the injected Fabric context. Authorization state uses the injected atomic store, and the
 * client secret is resolved only for the authorization-code exchange.
 * </p>
 *
 * @author Kimi Liu
 */
public class GiteeProvider extends AbstractProvider {

    /**
     * Creates a Gitee OAuth client from explicit runtime dependencies.
     *
     * @param configuration non-null registration, Fabric, clock, state-store, cache, and secret dependencies
     * @throws NullPointerException if the configuration or a required dependency is null
     * @throws AuthorizedException  if the Gitee registration is invalid
     */
    public GiteeProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.GITEE);
    }

    /**
     * Parses and validates a Gitee operation response.
     *
     * @param json operation response document
     * @return validated typed response
     * @throws AuthorizedException if the response is empty or reports an error
     */
    private static Response read(final String json) {
        final Response response = JsonKit.toPojo(json, Response.class);
        if (response == null) {
            throw new AuthorizedException("Failed to parse Gitee response: empty response");
        }
        if (response.error() != null) {
            throw new AuthorizedException(
                    response.error_description() == null ? "Unknown Gitee error" : response.error_description());
        }
        return response;
    }

    /**
     * Builds the Gitee authorization URL and atomically registers state.
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
                        .queryParam("state", state(current, state))
                        .queryParam("scope", scopes(Symbol.SPACE, true, getScopes(GiteeScope.values()))).build());
    }

    /**
     * Exchanges an authorization code through Gitee's token GET endpoint.
     *
     * @param context  immutable root operation context used to resolve the client secret
     * @param callback immutable inbound callback containing the standard {@code code} parameter
     * @return successful client message containing mapped Gitee token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint, secret, or response is invalid
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final Response response = read(doGetToken(current, inbound.value("code").orElse(null)));
        if (response.access_token() == null) {
            throw new AuthorizedException("Missing access_token in Gitee token response");
        }
        return Message.success(
                VendorTokenSet.builder().token(response.access_token()).refresh(response.refresh_token())
                        .scope(response.scope()).tokenType(response.token_type()).expireIn(response.expires_in())
                        .build());
    }

    /**
     * Retrieves and maps the Gitee profile associated with an access token.
     *
     * @param context immutable root operation context for this profile request
     * @param token   non-null Gitee token set
     * @return successful client message containing the mapped Gitee identity
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint or response is invalid
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final Response response = read(doGetUserInfo(authorization));
        if (response.id() == null) {
            throw new AuthorizedException("Missing id in Gitee profile response");
        }
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(response)).uuid(response.id())
                        .username(response.login()).avatar(response.avatar_url()).blog(response.blog())
                        .nickname(response.name()).company(response.company()).location(response.address())
                        .email(response.email()).remark(response.bio()).gender(Gender.UNKNOWN).token(authorization)
                        .source(descriptor().id()).build());
    }

    /**
     * Typed union of the frozen Gitee token, profile, and error fields.
     *
     * @param error             vendor error code
     * @param error_description vendor error description
     * @param access_token      sensitive access token
     * @param refresh_token     sensitive refresh token
     * @param scope             granted scope text
     * @param token_type        access-token scheme
     * @param expires_in        access-token lifetime in seconds
     * @param id                profile identifier
     * @param login             account login name
     * @param avatar_url        avatar URL
     * @param blog              blog URL
     * @param name              display name
     * @param company           company text
     * @param address           location text
     * @param email             email address
     * @param bio               profile biography
     * @author Kimi Liu
     */
    private record Response(String error, String error_description, String access_token, String refresh_token,
            String scope, String token_type, int expires_in, String id, String login, String avatar_url, String blog,
            String name, String company, String address, String email, String bio) {
    }

}
