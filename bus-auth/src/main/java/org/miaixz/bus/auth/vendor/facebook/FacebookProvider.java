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
package org.miaixz.bus.auth.vendor.facebook;

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
 * Third-party client for Facebook OAuth authorization, token, and profile operations.
 *
 * <p>
 * Every remote call uses the injected Fabric context. Authorization state uses the injected atomic store, and the
 * client secret is resolved only for the authorization-code exchange.
 * </p>
 *
 * @author Kimi Liu
 */
public class FacebookProvider extends AbstractProvider {

    /**
     * Facebook profile fields frozen by the historical provider contract.
     */
    private static final String PROFILE_FIELDS = "id,name,birthday,gender,hometown,email,devices,picture.width(400),link";

    /**
     * Creates a Facebook OAuth client from explicit runtime dependencies.
     *
     * @param configuration non-null registration, Fabric, clock, state-store, cache, and secret dependencies
     * @throws NullPointerException if the configuration or a required dependency is null
     * @throws AuthorizedException  if the Facebook registration is invalid
     */
    public FacebookProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.FACEBOOK);
    }

    /**
     * Parses and validates a Facebook token response.
     *
     * @param json token response document
     * @return validated typed token response
     * @throws AuthorizedException if the response reports an error or omits its access token
     */
    private static TokenResponse readToken(final String json) {
        final TokenResponse response = JsonKit.toPojo(json, TokenResponse.class);
        check(response == null ? null : response.error(), "token");
        if (response == null || response.access_token() == null) {
            throw new AuthorizedException("Missing access_token in Facebook token response");
        }
        return response;
    }

    /**
     * Parses and validates a Facebook profile response.
     *
     * @param json profile response document
     * @return validated typed profile response
     * @throws AuthorizedException if the response reports an error or omits its identifier
     */
    private static ProfileResponse readProfile(final String json) {
        final ProfileResponse response = JsonKit.toPojo(json, ProfileResponse.class);
        check(response == null ? null : response.error(), "profile");
        if (response == null || response.id() == null) {
            throw new AuthorizedException("Missing id in Facebook profile response");
        }
        return response;
    }

    /**
     * Converts a Facebook error envelope into the root authorization exception.
     *
     * @param error     optional typed error payload
     * @param operation response operation name used when the response is empty
     * @throws AuthorizedException if the error payload is present
     */
    private static void check(final ErrorResponse error, final String operation) {
        if (error != null) {
            throw new AuthorizedException(error.message() == null ? "Unknown Facebook error" : error.message());
        }
        if (operation == null) {
            throw new AuthorizedException("Missing Facebook operation name");
        }
    }

    /**
     * Builds the Facebook authorization URL and atomically registers state.
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
                        .queryParam("scope", scopes(Symbol.COMMA, false, getScopes(FacebookScope.values()))).build());
    }

    /**
     * Exchanges an authorization code through Facebook's token endpoint using HTTP GET.
     *
     * @param context  immutable root operation context used to resolve the client secret
     * @param callback immutable inbound callback containing the standard {@code code} parameter
     * @return successful client message containing mapped Facebook token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint, secret, or response is invalid
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final TokenResponse response = readToken(doGetToken(current, inbound.value("code").orElse(null)));
        return Message.success(
                VendorTokenSet.builder().token(response.access_token()).expireIn(response.expires_in())
                        .tokenType(response.token_type()).build());
    }

    /**
     * Retrieves and maps the Facebook profile associated with an access token.
     *
     * @param context immutable root operation context for this profile request
     * @param token   non-null Facebook token set
     * @return successful client message containing the mapped Facebook identity
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint or response is invalid
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final String url = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.USERINFO))
                .queryParam("access_token", authorization.getToken()).queryParam("fields", PROFILE_FIELDS).build();
        final ProfileResponse response = readProfile(get(url));
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(response)).uuid(response.id())
                        .username(response.name()).nickname(response.name()).blog(response.link())
                        .avatar(
                                response.picture() == null || response.picture().data() == null ? null
                                        : response.picture().data().url())
                        .location(response.locale()).email(response.email()).gender(Gender.of(response.gender()))
                        .token(authorization).source(descriptor().id()).build());
    }

    /**
     * Typed Facebook token response.
     *
     * @param access_token sensitive bearer access token
     * @param expires_in   access-token lifetime in seconds
     * @param token_type   access-token scheme
     * @param error        optional Facebook error payload
     * @author Kimi Liu
     */
    private record TokenResponse(String access_token, int expires_in, String token_type, ErrorResponse error) {
    }

    /**
     * Typed Facebook profile response.
     *
     * @param id      application-scoped user identifier
     * @param name    display name
     * @param link    public profile URL
     * @param locale  locale identifier
     * @param email   email address granted by scope
     * @param gender  vendor gender text
     * @param picture nested profile picture payload
     * @param error   optional Facebook error payload
     * @author Kimi Liu
     */
    private record ProfileResponse(String id, String name, String link, String locale, String email, String gender,
            PictureResponse picture, ErrorResponse error) {
    }

    /**
     * Typed Facebook picture envelope.
     *
     * @param data profile picture data
     * @author Kimi Liu
     */
    private record PictureResponse(PictureData data) {
    }

    /**
     * Typed Facebook picture data.
     *
     * @param url profile picture URL
     * @author Kimi Liu
     */
    private record PictureData(String url) {
    }

    /**
     * Typed Facebook error payload.
     *
     * @param message human-readable vendor error message
     * @author Kimi Liu
     */
    private record ErrorResponse(String message) {
    }

}
