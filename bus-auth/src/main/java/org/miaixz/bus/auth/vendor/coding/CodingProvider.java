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
package org.miaixz.bus.auth.vendor.coding;

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
 * Third-party authentication client for a concrete Coding.net tenant.
 *
 * <p>
 * Coding endpoint hosts are registration-owned because they contain the tenant prefix. Every remote operation uses the
 * injected Fabric context, authorization state uses the injected atomic store, and client secrets are resolved only for
 * the token exchange.
 * </p>
 *
 * @author Kimi Liu
 */
public class CodingProvider extends AbstractProvider {

    /**
     * Creates a Coding client from the complete immutable dependency aggregate.
     *
     * @param configuration non-null registration, Fabric, clock, state-store, cache, and secret dependencies
     * @throws NullPointerException if the configuration or one of its required dependencies is null
     * @throws AuthorizedException  if required Coding registration data or endpoints are absent
     */
    public CodingProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.CODING);
    }

    /**
     * Converts one Coding-relative profile path to its fixed public host.
     *
     * @param path relative path, or null when Coding omitted the property
     * @return absolute Coding URL, or null
     */
    private static String absolute(final String path) {
        return path == null ? null : "https://coding.net" + path;
    }

    /**
     * Rejects an empty Coding response or a non-zero vendor status code.
     *
     * @param response typed Coding response envelope
     * @throws AuthorizedException if the response is absent or reports an error
     */
    private static void validate(final CodingResponse response) {
        if (response == null) {
            throw new AuthorizedException("Failed to parse Coding response: empty response");
        }
        if (response.code() != 0) {
            throw new AuthorizedException(response.msg() == null ? "Unknown Coding error" : response.msg());
        }
    }

    /**
     * Builds the tenant-specific Coding authorization URL and atomically registers its state.
     *
     * @param context immutable root operation context used for state ownership
     * @param state   optional caller-supplied state; a generated state is used when absent
     * @return successful client message containing the complete authorization URL
     * @throws NullPointerException if {@code context} is null
     * @throws AuthorizedException  if the authorization endpoint is absent or state registration fails
     */
    @Override
    public Message<String> build(final Context context, final String state) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        return Message.success(
                VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.AUTHORIZE)).queryParam("response_type", "code")
                        .queryParam("client_id", registration.clientId())
                        .queryParam("redirect_uri", registration.redirectUri())
                        .queryParam("scope", scopes(Symbol.SPACE, true, getScopes(CodingScope.values())))
                        .queryParam("state", state(current, state)).build());
    }

    /**
     * Exchanges the callback authorization code through the tenant-specific Coding token endpoint.
     *
     * @param context  immutable root operation context used to resolve the client secret
     * @param callback immutable inbound callback containing the standard {@code code} parameter
     * @return successful client message containing access, expiry, and refresh token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if Coding returns an error, an empty document, or no access token
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final TokenResponse response = JsonKit
                .toPojo(doPostToken(current, inbound.value("code").orElse(null)), TokenResponse.class);
        validate(response);
        if (response.access_token() == null) {
            throw new AuthorizedException("Missing access_token in response");
        }
        return Message.success(
                VendorTokenSet.builder().token(response.access_token()).expireIn(response.expires_in())
                        .refresh(response.refresh_token()).build());
    }

    /**
     * Retrieves and maps the Coding profile associated with an access token.
     *
     * @param context immutable root operation context for this profile operation
     * @param token   non-null Coding token set
     * @return successful client message containing the mapped Coding identity
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if Coding returns an error or omits the profile identifier
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final UserResponse response = JsonKit.toPojo(doGetUserInfo(authorization), UserResponse.class);
        validate(response);
        final UserData data = response.data();
        if (data == null || data.id() == null) {
            throw new AuthorizedException("Missing data.id in user info response");
        }
        final String name = data.name();
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(data)).uuid(String.valueOf(data.id()))
                        .username(name).avatar(absolute(data.avatar())).blog(absolute(data.path())).nickname(name)
                        .company(data.company()).location(data.location()).gender(Gender.of(data.sex()))
                        .email(data.email()).remark(data.slogan()).token(authorization).source(descriptor().id())
                        .build());
    }

    /**
     * Common Coding response status fields.
     *
     * @author Kimi Liu
     */
    private interface CodingResponse {

        /**
         * Returns the Coding numeric result code.
         *
         * @return zero for success, or a vendor error code
         */
        int code();

        /**
         * Returns the Coding diagnostic message.
         *
         * @return diagnostic message, or null
         */
        String msg();
    }

    /**
     * Typed Coding token response.
     *
     * @param code          numeric result code
     * @param msg           diagnostic message
     * @param access_token  access token
     * @param expires_in    access-token lifetime in seconds
     * @param refresh_token refresh token
     * @author Kimi Liu
     */
    private record TokenResponse(int code, String msg, String access_token, int expires_in, String refresh_token)
            implements CodingResponse {
    }

    /**
     * Typed Coding user-information envelope.
     *
     * @param code numeric result code
     * @param msg  diagnostic message
     * @param data nested profile data
     * @author Kimi Liu
     */
    private record UserResponse(int code, String msg, UserData data) implements CodingResponse {
    }

    /**
     * Typed Coding profile data.
     *
     * @param id       stable Coding user identifier
     * @param name     display and login name
     * @param avatar   relative avatar path
     * @param path     relative profile path
     * @param company  company name
     * @param location location text
     * @param sex      vendor gender text
     * @param email    email address
     * @param slogan   profile slogan
     * @author Kimi Liu
     */
    private record UserData(Long id, String name, String avatar, String path, String company, String location,
            String sex, String email, String slogan) {
    }

}
