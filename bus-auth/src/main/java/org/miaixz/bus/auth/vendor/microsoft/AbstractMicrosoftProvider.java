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
package org.miaixz.bus.auth.vendor.microsoft;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Shared third-party client for Microsoft global and China authorization services.
 *
 * <p>
 * The concrete family member supplies its immutable vendor definition. All endpoint addresses are resolved through
 * registration-over-definition precedence, remote operations use the injected Fabric context, state uses the injected
 * atomic store, and token requests resolve and clear their client secret for the current root context.
 * </p>
 *
 * @author Kimi Liu
 */
public abstract class AbstractMicrosoftProvider extends AbstractProvider {

    /**
     * Creates a Microsoft family client without binding a concrete catalog entry.
     *
     * @param configuration non-null registration, Fabric, clock, state-store, cache, and secret dependencies
     * @param definition    non-null concrete Microsoft family definition
     * @throws NullPointerException if an argument or required dependency is null
     * @throws AuthorizedException  if the registration is invalid for the supplied definition
     */
    protected AbstractMicrosoftProvider(final VendorConfiguration configuration, final VendorDefinition definition) {
        super(configuration, definition);
    }

    /**
     * Parses one Microsoft token response.
     *
     * @param document token response document
     * @return mapped token set
     * @throws AuthorizedException if parsing fails or no access token is present
     */
    private static VendorTokenSet readToken(final String document) {
        try {
            final TokenResponse response = JsonKit.toPojo(document, TokenResponse.class);
            validate(
                    response == null ? null : response.error(),
                    response == null ? null : response.error_description(),
                    "access token",
                    response);
            if (response.access_token() == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            return VendorTokenSet.builder().token(response.access_token()).expireIn(response.expires_in())
                    .scope(response.scope()).tokenType(response.token_type()).refresh(response.refresh_token()).build();
        } catch (Exception exception) {
            throw new AuthorizedException("Failed to parse access token response: " + exception.getMessage());
        }
    }

    /**
     * Validates the common Microsoft error fields and non-null response.
     *
     * @param error       vendor error code
     * @param description vendor diagnostic text
     * @param operation   operation name used for an empty-response diagnostic
     * @param response    typed response instance
     * @throws AuthorizedException if the response is null or contains an error
     */
    private static void validate(
            final String error,
            final String description,
            final String operation,
            final Object response) {
        if (response == null) {
            throw new AuthorizedException("Failed to parse " + operation + " response: empty response");
        }
        if (error != null) {
            throw new AuthorizedException(description == null ? "Unknown error" : description);
        }
    }

    /**
     * Builds the Microsoft authorization URL and atomically registers state.
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
                        .queryParam("state", state(current, state)).queryParam("response_mode", "query")
                        .queryParam("scope", scopes(Symbol.SPACE, false, getScopes(MicrosoftScope.values())))
                        .build(true));
    }

    /**
     * Exchanges a Microsoft authorization code with duplicated query and form parameters.
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
        final TokenRequest request = request(
                current,
                VendorEndpoint.TOKEN,
                "authorization_code",
                "code",
                inbound.value("code").orElse(null));
        return Message.success(readToken(post(request.url(), request.form())));
    }

    /**
     * Retrieves a Microsoft Graph user profile with the token type and access token in the authorization header.
     *
     * @param context immutable root operation context
     * @param token   non-null Microsoft token set
     * @return successful message containing the mapped Microsoft identity
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint or response is invalid
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final String document = get(
                endpoint(VendorEndpoint.USERINFO),
                null,
                Map.of(
                        Http.Header.AUTHORIZATION,
                        authorization.getTokenType() + Symbol.SPACE + authorization.getToken()));
        try {
            final ProfileResponse response = JsonKit.toPojo(document, ProfileResponse.class);
            validate(
                    response == null ? null : response.error(),
                    response == null ? null : response.error_description(),
                    "user info",
                    response);
            if (response.id() == null) {
                throw new AuthorizedException("Missing id in user info response");
            }
            return Message.success(
                    VendorIdentity.builder().rawJson(JsonKit.toJsonString(response)).uuid(response.id())
                            .username(response.userPrincipalName()).nickname(response.displayName())
                            .location(response.officeLocation()).email(response.mail()).gender(Gender.UNKNOWN)
                            .token(authorization).source(descriptor().id()).build());
        } catch (Exception exception) {
            throw new AuthorizedException("Failed to parse user info response: " + exception.getMessage());
        }
    }

    /**
     * Refreshes a Microsoft access token with duplicated query and form parameters.
     *
     * @param context immutable root operation context used to resolve the client secret
     * @param token   non-null token set containing a refresh token
     * @return successful message containing refreshed token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint, secret, or response is invalid
     */
    @Override
    public Message<VendorTokenSet> refresh(final Context context, final VendorTokenSet token) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final TokenRequest request = request(
                current,
                VendorEndpoint.REFRESH,
                "refresh_token",
                "refresh_token",
                authorization.getRefresh());
        return Message.success(readToken(post(request.url(), request.form())));
    }

    /**
     * Builds one Microsoft token request whose query and form contain the same fields.
     *
     * @param context         root operation context used for secret resolution
     * @param role            token endpoint role
     * @param grant           OAuth grant type
     * @param credentialName  grant credential field name
     * @param credentialValue sensitive grant credential
     * @return immutable request URL and form
     */
    private TokenRequest request(
            final Context context,
            final VendorEndpoint role,
            final String grant,
            final String credentialName,
            final String credentialValue) {
        final Map<String, String> form = new LinkedHashMap<>();
        form.put(credentialName, credentialValue);
        form.put("client_id", registration.clientId());
        form.put("client_secret", secret(context));
        form.put("grant_type", grant);
        form.put("scope", scopes(Symbol.SPACE, false, getScopes(MicrosoftScope.values())));
        form.put("redirect_uri", registration.redirectUri());
        final VendorRequestBuilder request = VendorRequestBuilder.fromUrl(endpoint(role));
        form.forEach(request::queryParam);
        return new TokenRequest(request.build(true), Map.copyOf(form));
    }

    /**
     * Immutable query-and-form token request.
     *
     * @param url  token endpoint URL including query parameters
     * @param form immutable form fields duplicated from the query
     * @author Kimi Liu
     */
    private record TokenRequest(String url, Map<String, String> form) {
    }

    /**
     * Typed Microsoft token response.
     *
     * @param access_token      access token
     * @param expires_in        access-token lifetime in seconds
     * @param scope             granted scope text
     * @param token_type        token type
     * @param refresh_token     refresh token
     * @param error             vendor error code
     * @param error_description vendor diagnostic text
     * @author Kimi Liu
     */
    private record TokenResponse(String access_token, int expires_in, String scope, String token_type,
            String refresh_token, String error, String error_description) {
    }

    /**
     * Typed Microsoft Graph profile and error response.
     *
     * @param id                stable Microsoft user identifier
     * @param userPrincipalName account principal name
     * @param displayName       display name
     * @param officeLocation    office location
     * @param mail              email address
     * @param error             vendor error code
     * @param error_description vendor diagnostic text
     * @author Kimi Liu
     */
    private record ProfileResponse(String id, String userPrincipalName, String displayName, String officeLocation,
            String mail, String error, String error_description) {
    }

}
