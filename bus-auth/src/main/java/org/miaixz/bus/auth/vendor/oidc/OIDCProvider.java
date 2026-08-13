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
package org.miaixz.bus.auth.vendor.oidc;

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
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Generic third-party OpenID Connect client backed entirely by registration-owned endpoints.
 *
 * <p>
 * Remote calls use the injected Fabric context, state uses the injected atomic store, and the token operation resolves
 * and clears the client secret for the current root operation context.
 * </p>
 *
 * @author Kimi Liu
 */
public class OIDCProvider extends AbstractProvider {

    /**
     * Creates a generic OpenID Connect client from explicit dependencies.
     *
     * @param configuration non-null registration, Fabric, clock, state-store, cache, and secret dependencies
     */
    public OIDCProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.OIDC);
    }

    /**
     * Parses and validates the common token, profile, and error document.
     *
     * @param document response document
     * @return non-null typed response
     * @throws AuthorizedException if parsing fails or the response contains an error
     */
    private static Response read(final String document) {
        final Response response = JsonKit.toPojo(document, Response.class);
        if (response == null) {
            throw new AuthorizedException("Failed to parse OIDC response: empty response");
        }
        if (response.error() != null) {
            throw new AuthorizedException(
                    response.error_description() == null ? "Unknown error" : response.error_description());
        }
        return response;
    }

    /**
     * Builds the configured authorization URL and atomically registers state.
     *
     * @param context immutable root operation context used for state ownership
     * @param state   optional caller-supplied state
     * @return successful message containing the authorization URL
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
                        .queryParam("scope", scopes(Symbol.SPACE, true, registration.scopes())).build());
    }

    /**
     * Exchanges an authorization code through a form POST.
     *
     * @param context  immutable root operation context used to resolve the client secret
     * @param callback immutable inbound callback containing the authorization code
     * @return successful message containing mapped token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint, secret, or response is invalid
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
        if (response.access_token() == null) {
            throw new AuthorizedException("Missing access_token in OIDC response");
        }
        return Message.success(
                VendorTokenSet.builder().token(response.access_token()).tokenType(response.token_type())
                        .idToken(response.id_token()).scope(response.scope()).build());
    }

    /**
     * Retrieves standard OpenID Connect user claims through a Bearer-authenticated GET request.
     *
     * @param context immutable root operation context
     * @param token   non-null OpenID Connect token set
     * @return successful message containing the mapped identity
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint or response is invalid
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final Response response = read(
                get(
                        endpoint(VendorEndpoint.USERINFO),
                        null,
                        Map.of(Http.Header.AUTHORIZATION, Http.Auth.BEARER_PREFIX + authorization.getToken())));
        if (response.sub() == null) {
            throw new AuthorizedException("Missing sub in OIDC response");
        }
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(response)).uuid(response.sub())
                        .username(response.preferred_username()).nickname(response.nickname())
                        .avatar(response.picture()).blog(response.website()).email(response.email())
                        .gender(Gender.of(response.gender())).token(authorization).source(descriptor().id()).build());
    }

    /**
     * Typed union of standard OpenID Connect token, profile, and error fields.
     *
     * @param access_token       access token
     * @param token_type         token type
     * @param id_token           ID token
     * @param scope              granted scope text
     * @param sub                subject identifier
     * @param preferred_username preferred username
     * @param nickname           display nickname
     * @param picture            profile image URL
     * @param website            website URL
     * @param email              email address
     * @param gender             gender text
     * @param error              vendor error code
     * @param error_description  vendor diagnostic text
     * @author Kimi Liu
     */
    private record Response(String access_token, String token_type, String id_token, String scope, String sub,
            String preferred_username, String nickname, String picture, String website, String email, String gender,
            String error, String error_description) {
    }

}
