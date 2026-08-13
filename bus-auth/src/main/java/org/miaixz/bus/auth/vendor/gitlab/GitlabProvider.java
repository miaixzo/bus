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
package org.miaixz.bus.auth.vendor.gitlab;

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
 * Third-party client for GitLab OAuth authorization, token, and profile operations.
 *
 * <p>
 * Every remote call uses the injected Fabric context. Authorization state uses the injected atomic store, and the
 * client secret is resolved only for the authorization-code exchange.
 * </p>
 *
 * @author Kimi Liu
 */
public class GitlabProvider extends AbstractProvider {

    /**
     * Creates a GitLab OAuth client from explicit runtime dependencies.
     *
     * @param configuration non-null registration, Fabric, clock, state-store, cache, and secret dependencies
     * @throws NullPointerException if the configuration or a required dependency is null
     * @throws AuthorizedException  if the GitLab registration is invalid
     */
    public GitlabProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.GITLAB);
    }

    /**
     * Parses and validates a GitLab operation response.
     *
     * @param json operation response document
     * @return validated typed response
     * @throws AuthorizedException if the response is empty or reports an error
     */
    private static Response read(final String json) {
        final Response response = JsonKit.toPojo(json, Response.class);
        if (response == null) {
            throw new AuthorizedException("Failed to parse GitLab response: empty response");
        }
        if (response.error() != null) {
            throw new AuthorizedException(
                    response.error_description() == null ? "Unknown GitLab error" : response.error_description());
        }
        if (response.message() != null) {
            throw new AuthorizedException(response.message());
        }
        return response;
    }

    /**
     * Builds the GitLab authorization URL and atomically registers state.
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
                        .queryParam("scope", scopes(Symbol.PLUS, false, getScopes(GitlabScope.values()))).build());
    }

    /**
     * Exchanges an authorization code through GitLab's token POST endpoint.
     *
     * @param context  immutable root operation context used to resolve the client secret
     * @param callback immutable inbound callback containing the standard {@code code} parameter
     * @return successful client message containing mapped GitLab token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint, secret, or response is invalid
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final Response response = read(doPostToken(current, inbound.value("code").orElse(null)));
        if (response.access_token() == null) {
            throw new AuthorizedException("Missing access_token in GitLab token response");
        }
        return Message.success(
                VendorTokenSet.builder().token(response.access_token()).refresh(response.refresh_token())
                        .idToken(response.id_token()).tokenType(response.token_type()).scope(response.scope()).build());
    }

    /**
     * Retrieves and maps the GitLab profile associated with an access token.
     *
     * @param context immutable root operation context for this profile request
     * @param token   non-null GitLab token set
     * @return successful client message containing the mapped GitLab identity
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint or response is invalid
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final Response response = read(doGetUserInfo(authorization));
        if (response.id() == null) {
            throw new AuthorizedException("Missing id in GitLab profile response");
        }
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(response)).uuid(response.id())
                        .username(response.username()).nickname(response.name()).avatar(response.avatar_url())
                        .blog(response.web_url()).company(response.organization()).location(response.location())
                        .email(response.email()).remark(response.bio()).gender(Gender.UNKNOWN).token(authorization)
                        .source(descriptor().id()).build());
    }

    /**
     * Typed union of the frozen GitLab token, profile, and error fields.
     *
     * @param error             vendor token error code
     * @param error_description vendor token error description
     * @param message           vendor profile error description
     * @param access_token      sensitive access token
     * @param refresh_token     sensitive refresh token
     * @param id_token          signed OpenID Connect token
     * @param token_type        access-token scheme
     * @param scope             granted scope text
     * @param id                profile identifier
     * @param username          account username
     * @param name              display name
     * @param avatar_url        avatar URL
     * @param web_url           profile URL
     * @param organization      organization text
     * @param location          location text
     * @param email             email address
     * @param bio               profile biography
     * @author Kimi Liu
     */
    private record Response(String error, String error_description, String message, String access_token,
            String refresh_token, String id_token, String token_type, String scope, String id, String username,
            String name, String avatar_url, String web_url, String organization, String location, String email,
            String bio) {
    }

}
