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
package org.miaixz.bus.auth.vendor.okta;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party client for Okta authorization, token, profile, refresh, and revocation operations.
 *
 * <p>
 * Concrete endpoints come from registration, all HTTP uses the injected Fabric context, and each Basic-authenticated
 * operation resolves and clears its client secret for the current root context.
 * </p>
 *
 * @author Kimi Liu
 */
public class OktaProvider extends AbstractProvider {

    /**
     * Creates an Okta client from explicit runtime dependencies.
     *
     * @param configuration non-null registration, Fabric, clock, state-store, cache, and secret dependencies
     */
    public OktaProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.OKTA);
    }

    /**
     * Parses and validates a common Okta response.
     *
     * @param document response document
     * @return typed response
     */
    private static Response read(final String document) {
        final Response response = JsonKit.toPojo(document, Response.class);
        if (response == null)
            throw new AuthorizedException("Failed to parse Okta response: empty response");
        if (response.error() != null)
            throw new AuthorizedException(
                    response.error_description() == null ? "Unknown error" : response.error_description());
        return response;
    }

    /**
     * Maps a token-shaped Okta document after validating its error and access-token fields.
     *
     * @param document response document returned by the token or refresh endpoint
     * @return immutable token set containing the fields supplied by Okta
     * @throws AuthorizedException when the document is invalid, reports an error, or omits the access token
     */
    private static VendorTokenSet readToken(final String document) {
        final Response response = read(document);
        if (response.access_token() == null)
            throw new AuthorizedException("Missing access_token in Okta response");
        return VendorTokenSet.builder().token(response.access_token()).tokenType(response.token_type())
                .expireIn(response.expires_in()).scope(response.scope()).refresh(response.refresh_token())
                .idToken(response.id_token()).build();
    }

    /**
     * Builds the Okta consent URL and atomically registers state.
     *
     * @param context immutable root operation context
     * @param state   optional authorization state
     * @return successful message containing the authorization URL
     */
    @Override
    public Message<String> build(final Context context, final String state) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        return Message.success(
                VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.AUTHORIZE)).queryParam("response_type", "code")
                        .queryParam("prompt", "consent").queryParam("client_id", registration.clientId())
                        .queryParam("redirect_uri", registration.redirectUri())
                        .queryParam("scope", scopes(Symbol.SPACE, true, getScopes(OktaScope.values())))
                        .queryParam("state", state(current, state)).build());
    }

    /**
     * Exchanges an authorization code through an empty form POST with Basic authentication.
     *
     * @param context  root context used for secret resolution
     * @param callback inbound callback containing the code
     * @return successful message containing token fields
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final String url = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.TOKEN))
                .queryParam("code", inbound.value("code").orElse(null)).queryParam("grant_type", "authorization_code")
                .queryParam("redirect_uri", registration.redirectUri()).build();
        return Message.success(readToken(post(url, null, headers(current))));
    }

    /**
     * Retrieves the Okta profile through an empty form POST with Bearer authentication.
     *
     * @param context immutable root operation context
     * @param token   non-null token set
     * @return successful message containing the mapped identity
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final Response response = read(
                post(
                        endpoint(VendorEndpoint.USERINFO),
                        null,
                        Map.of(Http.Header.AUTHORIZATION, Http.Auth.BEARER_PREFIX + authorization.getToken())));
        if (response.sub() == null)
            throw new AuthorizedException("Missing sub in Okta response");
        final String location = response.address() == null ? null : response.address().street_address();
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(response)).uuid(response.sub())
                        .username(response.name()).nickname(response.nickname()).email(response.email())
                        .location(location).gender(Gender.of(response.sex())).token(authorization)
                        .source(descriptor().id()).build());
    }

    /**
     * Refreshes an Okta token through an empty form POST with Basic authentication.
     *
     * @param context root context used for secret resolution
     * @param token   non-null token set
     * @return refreshed token result or the standard missing-refresh failure
     */
    @Override
    public Message<VendorTokenSet> refresh(final Context context, final VendorTokenSet token) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        if (authorization.getRefresh() == null)
            return Message.failure(VendorErrors._110007);
        final String url = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.REFRESH))
                .queryParam("refresh_token", authorization.getRefresh()).queryParam("grant_type", "refresh_token")
                .build();
        return Message.success(readToken(post(url, null, headers(current))));
    }

    /**
     * Revokes an Okta access token with Basic authentication.
     *
     * @param context root context used for secret resolution
     * @param token   non-null token set
     * @return successful empty result
     */
    @Override
    public Message<Void> revoke(final Context context, final VendorTokenSet token) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final Map<String, String> form = new LinkedHashMap<>();
        form.put("token", authorization.getToken());
        form.put("token_type_hint", "access_token");
        post(endpoint(VendorEndpoint.REVOKE), form, Map.of(Http.Header.AUTHORIZATION, basic(current)));
        return Message.success(null);
    }

    /**
     * Builds headers for an Okta token operation.
     *
     * @param context root context used for secret resolution
     * @return immutable request headers
     */
    private Map<String, String> headers(final Context context) {
        return Map.of(
                "accept",
                MediaType.APPLICATION_JSON,
                Http.Header.CONTENT_TYPE,
                MediaType.APPLICATION_FORM_URLENCODED,
                Http.Header.AUTHORIZATION,
                basic(context));
    }

    /**
     * Builds a transient Basic authorization value.
     *
     * @param context root context used for secret resolution
     * @return Basic authorization value
     */
    private String basic(final Context context) {
        return "Basic " + Base64.encode(registration.clientId() + Symbol.COLON + secret(context));
    }

    /**
     * Typed postal-address fragment returned by the Okta user-info endpoint.
     *
     * @param street_address street address supplied by the identity tenant, or {@code null} when absent
     * @author Kimi Liu
     */
    private record AddressResponse(String street_address) {
    }

    /**
     * Typed union of Okta token, profile, and error fields.
     *
     * @param access_token      access token
     * @param token_type        token type
     * @param expires_in        token lifetime in seconds
     * @param scope             granted scope text
     * @param refresh_token     refresh token
     * @param id_token          ID token
     * @param sub               subject identifier
     * @param name              account name
     * @param nickname          nickname
     * @param email             email address
     * @param sex               gender text
     * @param address           postal address
     * @param error             error code
     * @param error_description error diagnostic
     * @author Kimi Liu
     */
    private record Response(String access_token, String token_type, int expires_in, String scope, String refresh_token,
            String id_token, String sub, String name, String nickname, String email, String sex,
            AddressResponse address, String error, String error_description) {
    }

}
