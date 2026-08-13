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
package org.miaixz.bus.auth.vendor.pinterest;

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
 * Third-party client for Pinterest authorization, token exchange, and profile retrieval.
 *
 * <p>
 * Remote requests use only registration-owned endpoints and the injected Fabric context. Token exchange resolves the
 * client secret for the active root context and clears the caller-owned character array after use.
 * </p>
 *
 * @author Kimi Liu
 */
public class PinterestProvider extends AbstractProvider {

    /**
     * Pinterest response status that denotes failure.
     */
    private static final String FAILURE = "failure";

    /**
     * Creates a Pinterest client from explicit runtime dependencies.
     *
     * @param configuration non-null registration, Fabric, clock, state-store, cache, and secret dependencies
     */
    public PinterestProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.PINTEREST);
    }

    /**
     * Extracts the 60-by-60-pixel avatar variant.
     *
     * @param images image variants indexed by Pinterest dimension labels, or {@code null}
     * @return avatar URL, or {@code null} when the variant is absent
     */
    private static String avatar(final Map<String, ImageResponse> images) {
        if (images == null) {
            return null;
        }
        final ImageResponse image = images.get("60x60");
        return image == null ? null : image.url();
    }

    /**
     * Parses and validates a typed Pinterest response.
     *
     * @param document  JSON response document
     * @param operation diagnostic operation name that contains no credentials
     * @return parsed response
     * @throws AuthorizedException if the document is empty, malformed, omits status, or reports failure
     */
    private static Response read(final String document, final String operation) {
        try {
            final Response response = JsonKit.toPojo(document, Response.class);
            if (response == null) {
                throw new AuthorizedException("Failed to parse " + operation + " response: empty response");
            }
            if (response.status() == null || FAILURE.equals(response.status())) {
                throw new AuthorizedException(response.message() == null ? "Unknown error" : response.message());
            }
            return response;
        } catch (AuthorizedException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new AuthorizedException("Failed to parse " + operation + " response: " + exception.getMessage());
        }
    }

    /**
     * Builds the Pinterest consent URL with comma-separated default scopes and atomically registered state.
     *
     * @param context immutable root operation context
     * @param state   optional authorization state
     * @return successful message containing the authorization URL
     * @throws NullPointerException if the context is {@code null}
     */
    @Override
    public Message<String> build(final Context context, final String state) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        return Message.success(
                VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.AUTHORIZE)).queryParam("response_type", "code")
                        .queryParam("client_id", registration.clientId())
                        .queryParam("redirect_uri", registration.redirectUri())
                        .queryParam("state", state(current, state))
                        .queryParam("scope", scopes(Symbol.COMMA, false, getScopes(PinterestScope.values()))).build());
    }

    /**
     * Exchanges an authorization code through an empty form POST with query credentials.
     *
     * @param context  root context used to resolve and clear the client secret
     * @param callback inbound callback containing the authorization code
     * @return successful message containing the Pinterest token
     * @throws NullPointerException if the context or callback is {@code null}
     * @throws AuthorizedException  if Pinterest reports failure or omits the access token
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final String url = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.TOKEN))
                .queryParam("code", inbound.value("code").orElse(null)).queryParam("client_id", registration.clientId())
                .queryParam("client_secret", secret(current)).queryParam("grant_type", "authorization_code")
                .queryParam("redirect_uri", registration.redirectUri()).build();
        final Response response = read(post(url), "access token");
        if (response.access_token() == null) {
            throw new AuthorizedException("Missing access_token in response");
        }
        return Message.success(
                VendorTokenSet.builder().token(response.access_token()).tokenType(response.token_type()).build());
    }

    /**
     * Retrieves the Pinterest profile with fixed profile fields and the access token in the query.
     *
     * @param context immutable root operation context
     * @param token   non-null token set containing the Pinterest access token
     * @return successful message containing the mapped identity
     * @throws NullPointerException if the context or token is {@code null}
     * @throws AuthorizedException  if Pinterest reports failure or omits profile data or its identifier
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final Map<String, String> query = new LinkedHashMap<>();
        query.put("access_token", authorization.getToken());
        query.put("fields", "id,username,first_name,last_name,bio,image");
        final Response response = read(get(endpoint(VendorEndpoint.USERINFO), query), "user info");
        final UserResponse user = response.data();
        if (user == null) {
            throw new AuthorizedException("Missing data in user info response");
        }
        if (user.id() == null) {
            throw new AuthorizedException("Missing id in user info response");
        }
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(user)).uuid(user.id())
                        .avatar(avatar(user.image())).username(user.username())
                        .nickname(user.first_name() + Symbol.SPACE + user.last_name()).gender(Gender.UNKNOWN)
                        .remark(user.bio()).token(authorization).source(descriptor().id()).build());
    }

    /**
     * Typed Pinterest image variant.
     *
     * @param url image URL
     * @author Kimi Liu
     */
    private record ImageResponse(String url) {
    }

    /**
     * Typed Pinterest profile data.
     *
     * @param id         profile identifier
     * @param username   account name
     * @param first_name first name
     * @param last_name  last name
     * @param bio        profile biography
     * @param image      immutable image variants parsed from the response
     * @author Kimi Liu
     */
    private record UserResponse(String id, String username, String first_name, String last_name, String bio,
            Map<String, ImageResponse> image) {
    }

    /**
     * Typed union of Pinterest token, profile-envelope, and status fields.
     *
     * @param status       response status
     * @param message      failure diagnostic
     * @param access_token access token
     * @param token_type   token type
     * @param data         profile data
     * @author Kimi Liu
     */
    private record Response(String status, String message, String access_token, String token_type, UserResponse data) {
    }

}
