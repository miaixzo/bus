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
package org.miaixz.bus.auth.vendor.figma;

import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party client for Figma OAuth authorization, token, profile, and refresh operations.
 *
 * <p>
 * Every remote call uses the injected Fabric context. Client secrets are resolved once per token or refresh operation,
 * and caller-owned secret arrays are cleared by the shared provider contract.
 * </p>
 *
 * @author Kimi Liu
 */
public class FigmaProvider extends AbstractProvider {

    /**
     * Creates a Figma OAuth client from explicit runtime dependencies.
     *
     * @param configuration non-null registration, Fabric, clock, state-store, cache, and secret dependencies
     * @throws NullPointerException if the configuration or a required dependency is null
     * @throws AuthorizedException  if the Figma registration is invalid
     */
    public FigmaProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.FIGMA);
    }

    /**
     * Parses and validates a Figma operation response.
     *
     * @param json operation response document
     * @return validated typed response
     * @throws AuthorizedException if the response is empty or reports an error
     */
    private static Response read(final String json) {
        final Response response = JsonKit.toPojo(json, Response.class);
        if (response == null) {
            throw new AuthorizedException("Failed to parse Figma response: empty response");
        }
        if (response.error() != null) {
            throw new AuthorizedException(response.error() + Symbol.COLON
                    + (response.message() == null ? "Unknown message" : response.message()));
        }
        return response;
    }

    /**
     * Maps common Figma token fields with the operation-specific identifier.
     *
     * @param response validated typed response
     * @param initial  whether to map the initial token's user identifier instead of the refresh open identifier
     * @return mapped token set
     * @throws AuthorizedException if the access token is absent
     */
    private static VendorTokenSet token(final Response response, final boolean initial) {
        if (response.access_token() == null) {
            throw new AuthorizedException("Missing access_token in Figma response");
        }
        final VendorTokenSet.VendorTokenSetBuilder<?, ?> builder = VendorTokenSet.builder()
                .token(response.access_token()).refresh(response.refresh_token()).scope(response.scope())
                .expireIn(response.expires_in());
        return (initial ? builder.userId(response.user_id()) : builder.openId(response.open_id())).build();
    }

    /**
     * Builds the Figma authorization URL and atomically registers state.
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
                        .queryParam("scope", scopes(Symbol.COMMA, true, getScopes(FigmaScope.values()))).build());
    }

    /**
     * Exchanges an authorization code through Figma's empty-form token POST.
     *
     * @param context  immutable root operation context used to resolve the client secret
     * @param callback immutable inbound callback containing the standard {@code code} parameter
     * @return successful client message containing mapped Figma token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint, secret, or response is invalid
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final String clientSecret = secret(current);
        final String url = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.TOKEN))
                .queryParam("code", inbound.value("code").orElse(null)).queryParam("client_id", registration.clientId())
                .queryParam("client_secret", clientSecret).queryParam("grant_type", "authorization_code")
                .queryParam("redirect_uri", registration.redirectUri()).build();
        final String credentials = registration.clientId() + Symbol.COLON + clientSecret;
        final Response response = read(
                post(
                        url,
                        null,
                        Map.of(
                                Http.Header.CONTENT_TYPE,
                                MediaType.APPLICATION_FORM_URLENCODED,
                                Http.Header.AUTHORIZATION,
                                "Basic " + Base64.encode(credentials.getBytes(Charset.UTF_8)))));
        return Message.success(token(response, true));
    }

    /**
     * Retrieves and maps the Figma profile associated with an access token.
     *
     * @param context immutable root operation context for this profile request
     * @param token   non-null Figma token set
     * @return successful client message containing the mapped Figma identity
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
                        Map.of(
                                Http.Header.CONTENT_TYPE,
                                MediaType.APPLICATION_FORM_URLENCODED,
                                Http.Header.AUTHORIZATION,
                                Http.Auth.BEARER_PREFIX + authorization.getToken())));
        if (response.id() == null) {
            throw new AuthorizedException("Missing id in Figma profile response");
        }
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(response)).uuid(response.id())
                        .username(response.handle()).avatar(response.img_url()).email(response.email())
                        .token(authorization).source(descriptor().id()).build());
    }

    /**
     * Refreshes a Figma token through the vendor's three-query empty-form POST.
     *
     * @param context immutable root operation context used to resolve the client secret
     * @param token   non-null token set containing the refresh token
     * @return successful client message containing mapped refreshed token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint, secret, or response is invalid
     */
    @Override
    public Message<VendorTokenSet> refresh(final Context context, final VendorTokenSet token) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final String url = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.REFRESH))
                .queryParam("client_id", registration.clientId()).queryParam("client_secret", secret(current))
                .queryParam("refresh_token", authorization.getRefresh()).build();
        final Response response = read(
                post(url, null, Map.of(Http.Header.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)));
        return Message.success(token(response, false));
    }

    /**
     * Typed union of the frozen Figma token, refresh, profile, and error fields.
     *
     * @param error         vendor error code
     * @param message       vendor error description
     * @param access_token  sensitive access token
     * @param refresh_token sensitive refresh token
     * @param scope         granted scope text
     * @param user_id       initial token user identifier
     * @param open_id       refreshed token open identifier
     * @param expires_in    access-token lifetime in seconds
     * @param id            profile identifier
     * @param handle        profile display handle
     * @param img_url       profile image URL
     * @param email         profile email address
     * @author Kimi Liu
     */
    private record Response(String error, String message, String access_token, String refresh_token, String scope,
            String user_id, String open_id, int expires_in, String id, String handle, String img_url, String email) {
    }

}
