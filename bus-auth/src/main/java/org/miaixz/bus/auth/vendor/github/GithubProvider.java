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
package org.miaixz.bus.auth.vendor.github;

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
 * Third-party client for GitHub OAuth authorization, form-token, and profile operations.
 *
 * <p>
 * Every remote call uses the injected Fabric context. Authorization state uses the injected atomic store, and the
 * client secret is resolved only for the authorization-code exchange.
 * </p>
 *
 * @author Kimi Liu
 */
public class GithubProvider extends AbstractProvider {

    /**
     * Creates a GitHub OAuth client from explicit runtime dependencies.
     *
     * @param configuration non-null registration, Fabric, clock, state-store, cache, and secret dependencies
     * @throws NullPointerException if the configuration or a required dependency is null
     * @throws AuthorizedException  if the GitHub registration is invalid
     */
    public GithubProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.GITHUB);
    }

    /**
     * Builds the GitHub authorization URL and atomically registers state.
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
                        .queryParam("scope", scopes(Symbol.SPACE, true, getScopes(GithubScope.values()))).build());
    }

    /**
     * Exchanges an authorization code and parses GitHub's form-encoded token response.
     *
     * @param context  immutable root operation context used to resolve the client secret
     * @param callback immutable inbound callback containing the standard {@code code} parameter
     * @return successful client message containing mapped GitHub token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint, secret, or response is invalid
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final Map<String, String> response = VendorRequestBuilder
                .parseStringToMap(doGetToken(current, inbound.value("code").orElse(null)));
        if (response.containsKey("error")) {
            throw new AuthorizedException(response.get("error_description") == null ? "Unknown GitHub error"
                    : response.get("error_description"));
        }
        final String accessToken = response.get("access_token");
        if (accessToken == null) {
            throw new AuthorizedException("Missing access_token in GitHub token response");
        }
        return Message.success(
                VendorTokenSet.builder().token(accessToken).scope(response.get("scope"))
                        .tokenType(response.get("token_type")).build());
    }

    /**
     * Retrieves and maps the GitHub profile associated with an access token.
     *
     * @param context immutable root operation context for this profile request
     * @param token   non-null GitHub token set
     * @return successful client message containing the mapped GitHub identity
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint or response is invalid
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final ProfileResponse response = JsonKit.toPojo(
                get(
                        endpoint(VendorEndpoint.USERINFO),
                        null,
                        Map.of(Http.Header.AUTHORIZATION, "token " + authorization.getToken())),
                ProfileResponse.class);
        if (response == null) {
            throw new AuthorizedException("Failed to parse GitHub profile response: empty response");
        }
        if (response.error() != null) {
            throw new AuthorizedException(
                    response.error_description() == null ? "Unknown GitHub error" : response.error_description());
        }
        if (response.id() == null) {
            throw new AuthorizedException("Missing id in GitHub profile response");
        }
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(response)).uuid(response.id())
                        .username(response.login()).avatar(response.avatar_url()).blog(response.blog())
                        .nickname(response.name()).company(response.company()).location(response.location())
                        .email(response.email()).remark(response.bio()).gender(Gender.UNKNOWN).token(authorization)
                        .source(descriptor().id()).build());
    }

    /**
     * Typed frozen GitHub profile and error fields.
     *
     * @param error             vendor error code
     * @param error_description vendor error description
     * @param id                profile identifier
     * @param login             account login name
     * @param avatar_url        avatar URL
     * @param blog              blog URL
     * @param name              display name
     * @param company           company text
     * @param location          location text
     * @param email             email address
     * @param bio               profile biography
     * @author Kimi Liu
     */
    private record ProfileResponse(String error, String error_description, String id, String login, String avatar_url,
            String blog, String name, String company, String location, String email, String bio) {
    }

}
