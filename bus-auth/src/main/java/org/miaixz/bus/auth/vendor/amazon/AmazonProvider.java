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
package org.miaixz.bus.auth.vendor.amazon;

import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party authentication client for Login with Amazon authorization, token, and profile operations.
 *
 * <p>
 * All remote calls use the injected Fabric context. Optional PKCE verifiers use the explicitly injected bus-cache
 * instance for the fixed ten-minute compatibility lifetime, and client secrets are resolved only for token calls.
 * </p>
 *
 * @author Kimi Liu
 */
public class AmazonProvider extends AbstractProvider {

    /**
     * PKCE verifier lifetime in milliseconds.
     */
    private static final long PKCE_TTL_MILLIS = Duration.ofMinutes(10).toMillis();

    /**
     * PKCE method supported by Login with Amazon.
     */
    private static final String PKCE_METHOD = "S256";

    /**
     * Explicit caller-owned compatibility cache used only for PKCE verifiers.
     */
    private final CacheX<String, String> cache;

    /**
     * Creates an Amazon client from the complete immutable dependency aggregate.
     *
     * @param configuration non-null registration, Fabric, clock, state-store, cache, and secret dependencies
     * @throws NullPointerException if the configuration or one of its required dependencies is null
     * @throws AuthorizedException  if the registration is invalid for Amazon
     */
    public AmazonProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.AMAZON);
        this.cache = stringCache(configuration.cache());
    }

    /**
     * Rejects an empty response or a standard Amazon error response.
     *
     * @param response typed response envelope
     * @throws AuthorizedException if the response is null or reports an error
     */
    private static void validate(final AmazonResponse response) {
        if (response == null) {
            throw new AuthorizedException("Failed to parse Amazon response: empty response");
        }
        if (response.error() != null) {
            throw new AuthorizedException(
                    response.error_description() == null ? "Unknown error" : response.error_description());
        }
    }

    /**
     * Replaces the final path segment of an effective endpoint URI.
     *
     * @param endpoint effective endpoint URI text
     * @param sibling  replacement sibling name
     * @return sibling endpoint URI text
     */
    private static String sibling(final String endpoint, final String sibling) {
        final URI source = URI.create(endpoint);
        final String path = source.getPath();
        final int separator = path.lastIndexOf('/');
        return source.resolve(path.substring(0, separator + 1) + sibling).toString();
    }

    /**
     * Narrows the explicitly injected compatibility cache to Amazon's string PKCE entries.
     *
     * @param cache caller-owned cache instance
     * @return the same cache viewed through its Amazon-owned key and value types
     */
    @SuppressWarnings("unchecked")
    private static CacheX<String, String> stringCache(final CacheX<?, ?> cache) {
        return (CacheX<String, String>) Objects.requireNonNull(cache, "Cache must not be null");
    }

    /**
     * Builds the Amazon authorization URL, atomically registers state, and stores a PKCE verifier when enabled.
     *
     * @param context immutable root operation context used for state ownership
     * @param state   optional caller-supplied state; a generated state is used when absent
     * @return successful client message containing the complete authorization URL
     * @throws NullPointerException if {@code context} is null
     * @throws AuthorizedException  if state registration or PKCE generation fails
     */
    @Override
    public Message<String> build(final Context context, final String state) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final String actualState = state(current, state);
        final VendorRequestBuilder request = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.AUTHORIZE))
                .queryParam("client_id", registration.clientId())
                .queryParam("scope", scopes(Symbol.SPACE, true, getScopes(AmazonScope.values())))
                .queryParam("redirect_uri", registration.redirectUri()).queryParam("response_type", "code")
                .queryParam("state", actualState);
        if (registration.pkce()) {
            final String verifier = VendorRequestBuilder.codeVerifier();
            request.queryParam("code_challenge", VendorRequestBuilder.codeChallenge(PKCE_METHOD, verifier))
                    .queryParam("code_challenge_method", PKCE_METHOD);
            cache.write(pkceKey(actualState), verifier, PKCE_TTL_MILLIS);
        }
        return Message.success(request.build());
    }

    /**
     * Exchanges an Amazon authorization code, including the cached PKCE verifier when enabled.
     *
     * @param context  immutable root operation context used to resolve the client secret
     * @param callback immutable inbound callback containing standard code and state parameters
     * @return successful client message containing the mapped Amazon token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the token response is empty, malformed, or reports an error
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final Map<String, String> form = new LinkedHashMap<>();
        form.put("grant_type", "authorization_code");
        form.put("code", inbound.value("code").orElse(null));
        form.put("redirect_uri", registration.redirectUri());
        form.put("client_id", registration.clientId());
        form.put("client_secret", secret(current));
        if (registration.pkce()) {
            form.put("code_verifier", cache.read(pkceKey(inbound.value("state").orElse(null))));
        }
        return Message.success(readToken(form, endpoint(VendorEndpoint.TOKEN)));
    }

    /**
     * Refreshes an Amazon access token through the configured refresh endpoint.
     *
     * @param context immutable root operation context used to resolve the client secret
     * @param token   non-null token set containing the refresh token
     * @return successful client message containing the refreshed token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the token response is empty, malformed, or reports an error
     */
    @Override
    public Message<VendorTokenSet> refresh(final Context context, final VendorTokenSet token) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final Map<String, String> form = new LinkedHashMap<>();
        form.put("grant_type", "refresh_token");
        form.put("refresh_token", authorization.getRefresh());
        form.put("client_id", registration.clientId());
        form.put("client_secret", secret(current));
        return Message.success(readToken(form, endpoint(VendorEndpoint.REFRESH)));
    }

    /**
     * Validates an Amazon access token and retrieves its customer profile.
     *
     * @param context immutable root operation context for this profile operation
     * @param token   non-null Amazon token set
     * @return successful client message containing the mapped Amazon identity
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if token validation or profile parsing fails
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        validateToken(authorization.getToken());
        final Map<String, String> headers = Map.of(
                Http.Header.HOST,
                "api.amazon.com",
                Http.Header.AUTHORIZATION,
                Http.Auth.BEARER_PREFIX + authorization.getToken());
        final UserResponse response = JsonKit
                .toPojo(get(endpoint(VendorEndpoint.USERINFO), Map.of(), headers), UserResponse.class);
        validate(response);
        if (response.user_id() == null) {
            throw new AuthorizedException("Missing user_id in response");
        }
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(response)).uuid(response.user_id())
                        .username(response.name()).nickname(response.name()).email(response.email())
                        .gender(Gender.UNKNOWN).source(descriptor().id()).token(authorization).build());
    }

    /**
     * Sends one Amazon token form and validates the typed response.
     *
     * @param form ordered token or refresh fields
     * @param url  effective token endpoint
     * @return mapped token set
     * @throws AuthorizedException if parsing fails, an error is reported, or the access token is absent
     */
    private VendorTokenSet readToken(final Map<String, String> form, final String url) {
        final Map<String, String> headers = Map.of(Http.Header.HOST, "api.amazon.com");
        final TokenResponse response = JsonKit.toPojo(post(url, form, headers), TokenResponse.class);
        validate(response);
        if (response.access_token() == null) {
            throw new AuthorizedException("Missing access_token in response");
        }
        return VendorTokenSet.builder().token(response.access_token()).tokenType(response.token_type())
                .expireIn(response.expires_in()).refresh(response.refresh_token()).build();
    }

    /**
     * Validates the token audience through Amazon's token-info sibling endpoint.
     *
     * @param token sensitive Amazon access token
     * @throws AuthorizedException if the token-info document is invalid or targets another client
     */
    private void validateToken(final String token) {
        final String tokenInfo = sibling(endpoint(VendorEndpoint.TOKEN), "tokeninfo");
        final String url = VendorRequestBuilder.fromUrl(tokenInfo).queryParam("access_token", token).build();
        final TokenInfoResponse response = JsonKit.toPojo(get(url), TokenInfoResponse.class);
        if (response == null || !registration.clientId().equals(response.aud())) {
            throw new AuthorizedException(ErrorCode._100113);
        }
    }

    /**
     * Builds the deterministic cache key for one Amazon PKCE verifier.
     *
     * @param state opaque authorization state
     * @return vendor-prefixed cache key
     */
    private String pkceKey(final String state) {
        return descriptor().id() + ":code_verifier:" + state;
    }

    /**
     * Common fields shared by typed Amazon operation responses.
     *
     * @author Kimi Liu
     */
    private interface AmazonResponse {

        /**
         * Returns the vendor error code.
         *
         * @return error code, or null for success
         */
        String error();

        /**
         * Returns the vendor diagnostic message.
         *
         * @return error description, or null when absent
         */
        String error_description();
    }

    /**
     * Typed Amazon token response.
     *
     * @param access_token      access token
     * @param token_type        token scheme label
     * @param expires_in        access-token lifetime in seconds
     * @param refresh_token     refresh token
     * @param error             vendor error code
     * @param error_description vendor diagnostic message
     * @author Kimi Liu
     */
    private record TokenResponse(String access_token, String token_type, int expires_in, String refresh_token,
            String error, String error_description) implements AmazonResponse {
    }

    /**
     * Typed Amazon customer profile response.
     *
     * @param user_id           Amazon customer identifier
     * @param name              customer display name
     * @param email             customer email address
     * @param error             vendor error code
     * @param error_description vendor diagnostic message
     * @author Kimi Liu
     */
    private record UserResponse(String user_id, String name, String email, String error, String error_description)
            implements AmazonResponse {
    }

    /**
     * Typed Amazon token-info response.
     *
     * @param aud client identifier for which the token was issued
     * @author Kimi Liu
     */
    private record TokenInfoResponse(String aud) {
    }

}
