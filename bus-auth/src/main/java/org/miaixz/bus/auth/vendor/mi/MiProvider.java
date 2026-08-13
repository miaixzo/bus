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
package org.miaixz.bus.auth.vendor.mi;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party client for Xiaomi authorization, token, refresh, profile, and contact operations.
 *
 * <p>
 * Every remote operation uses the injected Fabric context. Authorization state uses the injected atomic store, while
 * token operations resolve the client secret for the current root context and clear its caller-owned character buffer
 * immediately after request construction.
 * </p>
 *
 * @author Kimi Liu
 */
public class MiProvider extends AbstractProvider {

    /**
     * Prefix added to Xiaomi token response documents.
     */
    private static final String RESPONSE_PREFIX = "&&&START&&&";

    /**
     * Creates a Xiaomi client from the complete immutable dependency aggregate.
     *
     * @param configuration non-null registration, Fabric, clock, state-store, cache, and secret dependencies
     * @throws NullPointerException if the configuration or a required dependency is null
     * @throws AuthorizedException  if the Xiaomi registration is invalid
     */
    public MiProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.MI);
    }

    /**
     * Parses one prefixed Xiaomi token response.
     *
     * @param document token response document
     * @return mapped token set
     * @throws AuthorizedException if parsing fails or the response contains no access token
     */
    private static VendorTokenSet readToken(final String document) {
        try {
            final String json = Objects.requireNonNull(document, "Token response must not be null")
                    .replace(RESPONSE_PREFIX, Normal.EMPTY);
            final TokenResponse response = JsonKit.toPojo(json, TokenResponse.class);
            if (response == null) {
                throw new AuthorizedException("Failed to parse access token response: empty response");
            }
            if (response.error() != null) {
                throw new AuthorizedException(
                        response.error_description() == null ? "Unknown error" : response.error_description());
            }
            if (response.access_token() == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            return VendorTokenSet.builder().token(response.access_token()).expireIn(response.expires_in())
                    .scope(response.scope()).tokenType(response.token_type()).refresh(response.refresh_token())
                    .openId(response.openId()).macAlgorithm(response.mac_algorithm()).macKey(response.mac_key())
                    .build();
        } catch (Exception exception) {
            throw new AuthorizedException("Failed to parse access token response: " + exception.getMessage());
        }
    }

    /**
     * Validates required Xiaomi profile fields.
     *
     * @param response typed profile response
     * @throws AuthorizedException if the response is empty, erroneous, or missing profile data
     */
    private static void validateProfile(final ProfileResponse response) {
        if (response == null) {
            throw new AuthorizedException("Failed to parse user info response: empty response");
        }
        if ("error".equalsIgnoreCase(response.result())) {
            throw new AuthorizedException(response.description() == null ? "Unknown error" : response.description());
        }
        if (response.data() == null) {
            throw new AuthorizedException("Missing data in user info response");
        }
        if (response.data().miliaoNick() == null) {
            throw new AuthorizedException("Missing miliaoNick in user info response");
        }
    }

    /**
     * Builds the Xiaomi authorization URL and atomically registers its state.
     *
     * @param context immutable root operation context used for state ownership
     * @param state   optional caller-supplied state
     * @return successful message containing the authorization URL
     * @throws NullPointerException if {@code context} is null
     * @throws AuthorizedException  if state registration fails
     */
    @Override
    public Message<String> build(final Context context, final String state) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        return Message.success(
                VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.AUTHORIZE)).queryParam("response_type", "code")
                        .queryParam("client_id", registration.clientId())
                        .queryParam("redirect_uri", registration.redirectUri())
                        .queryParam("state", state(current, state))
                        .queryParam("scope", scopes(Symbol.SPACE, true, getScopes(MiScope.values())))
                        .queryParam("skip_confirm", "false").build());
    }

    /**
     * Exchanges a Xiaomi authorization code using the historical query-authenticated GET request.
     *
     * @param context  immutable root operation context used to resolve the client secret
     * @param callback immutable inbound callback containing the authorization code
     * @return successful message containing the mapped Xiaomi token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint, secret, or response is invalid
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        return Message.success(readToken(get(tokenUrl(current, inbound.value("code").orElse(null)))));
    }

    /**
     * Retrieves the Xiaomi profile and the optional phone-and-email projection through two GET requests.
     *
     * @param context immutable root operation context
     * @param token   non-null Xiaomi token set
     * @return successful message containing the combined Xiaomi identity
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if a required profile field or response is invalid
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final Map<String, String> query = Map
                .of("clientId", registration.clientId(), "token", authorization.getToken());
        final String profileDocument = get(endpoint(VendorEndpoint.USERINFO), query);
        try {
            final ProfileResponse profile = JsonKit.toPojo(profileDocument, ProfileResponse.class);
            validateProfile(profile);
            String email = profile.data().mail();
            final String contactDocument = get(contactEndpoint(), query);
            try {
                final ContactResponse contact = JsonKit.toPojo(contactDocument, ContactResponse.class);
                if (contact != null && !"error".equalsIgnoreCase(contact.result()) && contact.data() != null
                        && contact.data().email() != null) {
                    email = contact.data().email();
                }
            } catch (RuntimeException ignored) {
                // The historical optional contact projection does not invalidate a successfully mapped profile.
            }
            final ProfileData data = profile.data();
            return Message.success(
                    VendorIdentity.builder().rawJson(JsonKit.toJsonString(data)).uuid(authorization.getOpenId())
                            .username(data.miliaoNick()).nickname(data.miliaoNick()).avatar(data.miliaoIcon())
                            .email(email).gender(Gender.UNKNOWN).token(authorization).source(descriptor().id())
                            .build());
        } catch (Exception exception) {
            throw new AuthorizedException("Failed to parse user info response: " + exception.getMessage());
        }
    }

    /**
     * Refreshes a Xiaomi access token using the historical query-authenticated GET request.
     *
     * @param context immutable root operation context used to resolve the client secret
     * @param token   non-null token set containing a refresh token
     * @return successful message containing the refreshed Xiaomi token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint, secret, or response is invalid
     */
    @Override
    public Message<VendorTokenSet> refresh(final Context context, final VendorTokenSet token) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        return Message.success(readToken(get(refreshUrl(current, authorization.getRefresh()))));
    }

    /**
     * Builds the optional contact endpoint as a sibling of the effective profile endpoint.
     *
     * @return concrete phone-and-email endpoint
     */
    private String contactEndpoint() {
        final URI profile = URI.create(endpoint(VendorEndpoint.USERINFO));
        final String path = profile.getPath();
        final String sibling = path.substring(0, path.lastIndexOf('/') + 1) + "phoneAndEmail";
        return profile.resolve(sibling).toString();
    }

    /**
     * Typed Xiaomi token and error response.
     *
     * @param access_token      access token
     * @param expires_in        access-token lifetime in seconds
     * @param scope             granted scope text
     * @param token_type        token type
     * @param refresh_token     refresh token
     * @param openId            stable Xiaomi user identifier
     * @param mac_algorithm     MAC algorithm
     * @param mac_key           MAC key
     * @param error             vendor error code
     * @param error_description vendor diagnostic text
     * @author Kimi Liu
     */
    private record TokenResponse(String access_token, int expires_in, String scope, String token_type,
            String refresh_token, String openId, String mac_algorithm, String mac_key, String error,
            String error_description) {
    }

    /**
     * Typed Xiaomi profile envelope.
     *
     * @param result      result marker
     * @param description vendor diagnostic text
     * @param data        profile data
     * @author Kimi Liu
     */
    private record ProfileResponse(String result, String description, ProfileData data) {
    }

    /**
     * Typed Xiaomi profile data.
     *
     * @param miliaoNick Xiaomi display name
     * @param miliaoIcon profile image URL
     * @param mail       profile email address
     * @author Kimi Liu
     */
    private record ProfileData(String miliaoNick, String miliaoIcon, String mail) {
    }

    /**
     * Typed Xiaomi contact envelope.
     *
     * @param result      result marker
     * @param description vendor diagnostic text
     * @param data        contact data
     * @author Kimi Liu
     */
    private record ContactResponse(String result, String description, ContactData data) {
    }

    /**
     * Typed Xiaomi contact projection.
     *
     * @param email optional email address
     * @author Kimi Liu
     */
    private record ContactData(String email) {
    }

}
