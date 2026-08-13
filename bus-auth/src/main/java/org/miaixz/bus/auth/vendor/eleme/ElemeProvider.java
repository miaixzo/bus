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
package org.miaixz.bus.auth.vendor.eleme;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party client for Ele.me OAuth and signed merchant-profile operations.
 *
 * <p>
 * All HTTP calls use the injected Fabric context. Request timestamps use the injected Fabric clock, and Basic or
 * RPC-signature client secrets are resolved per root operation without being retained.
 * </p>
 *
 * @author Kimi Liu
 */
public class ElemeProvider extends AbstractProvider {

    /**
     * Ele.me merchant-profile RPC action.
     */
    private static final String USER_ACTION = "eleme.user.getUser";

    /**
     * Creates an Ele.me client from explicit runtime dependencies.
     *
     * @param configuration non-null registration, Fabric, clock, state-store, cache, and secret dependencies
     * @throws NullPointerException if the configuration or a required dependency is null
     * @throws AuthorizedException  if the Ele.me registration is invalid
     */
    public ElemeProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.ELEME);
    }

    /**
     * Produces the uppercase Ele.me MD5 RPC signature.
     *
     * @param appKey     public application key
     * @param secret     transient application secret
     * @param timestamp  request timestamp in epoch milliseconds
     * @param action     RPC action
     * @param token      access token
     * @param parameters immutable RPC parameters
     * @return uppercase hexadecimal MD5 signature
     */
    private static String sign(
            final String appKey,
            final String secret,
            final long timestamp,
            final String action,
            final String token,
            final Map<String, Object> parameters) {
        final Map<String, Object> sorted = new TreeMap<>(parameters);
        sorted.put("app_key", appKey);
        sorted.put("timestamp", timestamp);
        final StringBuilder values = new StringBuilder();
        sorted.forEach((name, value) -> values.append(name).append(Symbol.EQUAL).append(JsonKit.toJsonString(value)));
        return org.miaixz.bus.crypto.Builder.md5Hex(String.format("%s%s%s%s", action, token, values, secret))
                .toUpperCase(java.util.Locale.ROOT);
    }

    /**
     * Parses and validates an Ele.me token response.
     *
     * @param json token response document
     * @return mapped token set
     * @throws AuthorizedException if the response reports an error or omits its access token
     */
    private static VendorTokenSet readToken(final String json) {
        final TokenResponse response = JsonKit.toPojo(json, TokenResponse.class);
        if (response == null) {
            throw new AuthorizedException("Failed to parse Ele.me token response: empty response");
        }
        if (response.error() != null) {
            throw new AuthorizedException(
                    response.error_description() == null ? "Unknown error" : response.error_description());
        }
        if (response.access_token() == null) {
            throw new AuthorizedException("Missing access_token in Ele.me token response");
        }
        return VendorTokenSet.builder().token(response.access_token()).refresh(response.refresh_token())
                .tokenType(response.token_type()).expireIn(response.expires_in()).build();
    }

    /**
     * Builds the Ele.me authorization URL with its fixed {@code all} scope and atomically registers state.
     *
     * @param context immutable root operation context used for state ownership
     * @param state   optional caller-supplied state; a generated state is used when absent
     * @return successful client message containing the authorization URL
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
                        .queryParam("state", state(current, state)).queryParam("scope", "all").build());
    }

    /**
     * Exchanges an authorization code using an authenticated form POST.
     *
     * @param context  immutable root operation context used for Basic authentication
     * @param callback immutable inbound callback containing the standard {@code code} parameter
     * @return successful client message containing mapped Ele.me token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint, secret, or response is invalid
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final Map<String, String> form = new LinkedHashMap<>();
        form.put("client_id", registration.clientId());
        form.put("redirect_uri", registration.redirectUri());
        form.put("code", inbound.value("code").orElse(null));
        form.put("grant_type", "authorization_code");
        return Message.success(
                readToken(
                        post(
                                endpoint(VendorEndpoint.TOKEN),
                                form,
                                headers(current, MediaType.APPLICATION_FORM_URLENCODED, requestId(), true))));
    }

    /**
     * Retrieves the signed Ele.me merchant profile through its JSON RPC endpoint.
     *
     * @param context immutable root operation context used for signing
     * @param token   non-null Ele.me token set
     * @return successful client message containing the mapped merchant identity
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint, secret, or response is invalid
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final long timestamp = clock.now().toEpochMilli();
        final Map<String, Object> parameters = Map.of();
        final Map<String, Object> metas = new LinkedHashMap<>();
        metas.put("app_key", registration.clientId());
        metas.put("timestamp", timestamp);
        final String signature = sign(
                registration.clientId(),
                secret(current),
                timestamp,
                USER_ACTION,
                authorization.getToken(),
                parameters);
        final String requestId = requestId();
        final Map<String, Object> body = new LinkedHashMap<>();
        body.put("nop", "1.0.0");
        body.put("id", requestId);
        body.put("action", USER_ACTION);
        body.put("token", authorization.getToken());
        body.put("metas", metas);
        body.put("params", parameters);
        body.put("signature", signature);
        final ProfileResponse response = JsonKit.toPojo(
                post(
                        endpoint(VendorEndpoint.USERINFO),
                        JsonKit.toJsonString(body),
                        headers(current, MediaType.APPLICATION_JSON, requestId, false),
                        MediaType.APPLICATION_JSON),
                ProfileResponse.class);
        if (response == null) {
            throw new AuthorizedException("Failed to parse Ele.me profile response: empty response");
        }
        if (response.name() != null) {
            throw new AuthorizedException(response.message() == null ? "Unknown error" : response.message());
        }
        if (response.error() != null) {
            throw new AuthorizedException(
                    response.error().message() == null ? "Unknown error" : response.error().message());
        }
        if (response.result() == null || response.result().userId() == null) {
            throw new AuthorizedException("Missing result.userId in Ele.me profile response");
        }
        final ProfileResult result = response.result();
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(result)).uuid(result.userId())
                        .username(result.userName()).nickname(result.userName()).gender(Gender.UNKNOWN)
                        .token(authorization).source(descriptor().id()).build());
    }

    /**
     * Refreshes an Ele.me access token using an authenticated form POST.
     *
     * @param context immutable root operation context used for Basic authentication
     * @param token   non-null token set containing the refresh token
     * @return successful client message containing mapped refreshed token fields
     * @throws NullPointerException if an argument is null
     * @throws AuthorizedException  if the endpoint, secret, or response is invalid
     */
    @Override
    public Message<VendorTokenSet> refresh(final Context context, final VendorTokenSet token) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final Map<String, String> form = new LinkedHashMap<>();
        form.put("refresh_token", authorization.getRefresh());
        form.put("grant_type", "refresh_token");
        return Message.success(
                readToken(
                        post(
                                endpoint(VendorEndpoint.REFRESH),
                                form,
                                headers(current, MediaType.APPLICATION_FORM_URLENCODED, requestId(), true))));
    }

    /**
     * Builds the fixed Ele.me HTTP header set.
     *
     * @param context       root operation context used only when Basic authentication is requested
     * @param contentType   request content type
     * @param requestId     uppercase request identifier
     * @param authenticated whether to include the Basic authorization header
     * @return immutable header map
     */
    private Map<String, String> headers(
            final Context context,
            final String contentType,
            final String requestId,
            final boolean authenticated) {
        final Map<String, String> values = new LinkedHashMap<>();
        values.put(Http.Header.ACCEPT, "text/xml,text/javascript,text/html");
        values.put(Http.Header.CONTENT_TYPE, contentType);
        values.put(Http.Header.ACCEPT_ENCODING, "gzip");
        values.put(Http.Header.USER_AGENT, "eleme-openapi-java-sdk");
        values.put("x-eleme-requestid", requestId);
        if (authenticated) {
            final String credentials = registration.clientId() + Symbol.COLON + secret(context);
            values.put(
                    Http.Header.AUTHORIZATION,
                    "Basic " + Base64.encode(credentials.getBytes(StandardCharsets.UTF_8)));
        }
        return Map.copyOf(values);
    }

    /**
     * Generates an uppercase request identifier using Bus ID generation and the injected clock.
     *
     * @return non-empty uppercase request identifier
     */
    private String requestId() {
        return (ID.objectId() + Symbol.OR + clock.now().toEpochMilli()).toUpperCase(java.util.Locale.ROOT);
    }

    /**
     * Typed Ele.me token response.
     *
     * @param error             vendor error code
     * @param error_description vendor error description
     * @param access_token      access token
     * @param refresh_token     refresh token
     * @param token_type        token scheme
     * @param expires_in        access-token lifetime in seconds
     * @author Kimi Liu
     */
    private record TokenResponse(String error, String error_description, String access_token, String refresh_token,
            String token_type, int expires_in) {
    }

    /**
     * Typed Ele.me profile response.
     *
     * @param name    top-level error name
     * @param message top-level error message
     * @param error   nested error
     * @param result  merchant profile result
     * @author Kimi Liu
     */
    private record ProfileResponse(String name, String message, ProfileError error, ProfileResult result) {
    }

    /**
     * Typed nested Ele.me profile error.
     *
     * @param message error message
     * @author Kimi Liu
     */
    private record ProfileError(String message) {
    }

    /**
     * Typed Ele.me merchant profile result.
     *
     * @param userId   stable merchant user identifier
     * @param userName merchant display name
     * @author Kimi Liu
     */
    private record ProfileResult(String userId, String userName) {
    }

}
