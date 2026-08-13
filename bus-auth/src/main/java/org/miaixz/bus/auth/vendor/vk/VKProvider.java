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
package org.miaixz.bus.auth.vendor.vk;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.codec.state.StateEnvelopeCodec;
import org.miaixz.bus.auth.guard.ReplayKey;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party client for VK OAuth 2.0 authorization, PKCE, token, profile, refresh, and revocation operations.
 *
 * @author Kimi Liu
 */
public class VKProvider extends AbstractProvider {

    /**
     * Form content-type header shared by VK POST operations.
     */
    private static final Map<String, String> FORM_HEADER = Map
            .of(Http.Header.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);

    /**
     * Creates a VK client from explicit runtime dependencies.
     *
     * @param configuration complete non-null vendor dependency aggregate
     */
    public VKProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.VK);
    }

    /**
     * Maps and validates a VK token response.
     *
     * @param document response JSON
     * @param deviceId device identifier retained across refresh
     * @return mapped token set
     */
    private static VendorTokenSet token(final String document, final String deviceId) {
        final TokenResponse response = JsonKit.toPojo(document, TokenResponse.class);
        check(response == null ? null : response.error(), response == null ? null : response.message());
        if (response == null || response.access_token() == null)
            throw new AuthorizedException("VK token response is missing access_token");
        return VendorTokenSet.builder().idToken(response.id_token()).token(response.access_token())
                .refresh(response.refresh_token()).tokenType(response.token_type()).scope(response.scope())
                .deviceId(deviceId).userId(response.user_id()).expireIn(response.expires_in()).build();
    }

    /**
     * Rejects a VK error envelope.
     *
     * @param error   optional error identifier
     * @param message optional error message
     */
    private static void check(final String error, final String message) {
        if (error != null || message != null)
            throw new AuthorizedException(error != null ? error : message);
    }

    /**
     * Derives the tenant-isolated PKCE verifier key.
     *
     * @param context root operation context
     * @param state   authorization state
     * @return derived storage key
     */
    private static String verifierKey(final Context context, final String state) {
        return ReplayKey.derive(context.tenantId(), "oauth2", "vk-pkce", state);
    }

    /**
     * Builds the VK authorization URL and stores an optional PKCE verifier atomically.
     *
     * @param context root operation context
     * @param state   optional authorization state
     * @return successful message containing the authorization URL
     */
    @Override
    public Message<String> build(final Context context, final String state) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final String actualState = state(current, state);
        final VendorRequestBuilder request = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.AUTHORIZE))
                .queryParam("response_type", "code").queryParam("client_id", registration.clientId())
                .queryParam("redirect_uri", registration.redirectUri()).queryParam("state", actualState)
                .queryParam("scope", scopes(Symbol.SPACE, true, getScopes(VKScope.values())));
        if (registration.pkce()) {
            final String verifier = VendorRequestBuilder.codeVerifier();
            storeVerifier(current, actualState, verifier);
            request.queryParam("code_challenge", VendorRequestBuilder.codeChallenge("S256", verifier))
                    .queryParam("code_challenge_method", "S256");
        }
        return Message.success(request.build());
    }

    /**
     * Exchanges a VK authorization code with optional one-time PKCE verification.
     *
     * @param context  root operation context
     * @param callback immutable inbound callback
     * @return successful message containing the mapped token set
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final Map<String, String> form = new LinkedHashMap<>();
        form.put("grant_type", "authorization_code");
        form.put("redirect_uri", registration.redirectUri());
        form.put("client_id", registration.clientId());
        form.put("code", inbound.value("code").orElse(null));
        form.put("state", inbound.value("state").orElse(null));
        form.put("device_id", inbound.value("device_id").orElse(null));
        if (registration.pkce())
            form.put("code_verifier", takeVerifier(current, inbound.value("state").orElse(null)));
        return Message.success(token(post(endpoint(VendorEndpoint.TOKEN), form, FORM_HEADER), form.get("device_id")));
    }

    /**
     * Retrieves and maps a VK profile through a form POST.
     *
     * @param context root operation context
     * @param token   non-null token set
     * @return successful message containing the mapped identity
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final Map<String, String> form = Map
                .of("access_token", authorization.getToken(), "client_id", registration.clientId());
        final ProfileEnvelope response = JsonKit
                .toPojo(post(endpoint(VendorEndpoint.USERINFO), form, FORM_HEADER), ProfileEnvelope.class);
        check(response == null ? null : response.error(), response == null ? null : response.message());
        if (response == null || response.user() == null || response.user().user_id() == null) {
            throw new AuthorizedException("VK profile response is missing user.user_id");
        }
        final User user = response.user();
        return Message.success(
                VendorIdentity.builder().uuid(user.user_id()).username(user.first_name())
                        .nickname(user.first_name() + Symbol.SPACE + user.last_name()).avatar(user.avatar())
                        .email(user.email()).token(authorization).rawJson(JsonKit.toJsonString(user))
                        .source(descriptor().id()).build());
    }

    /**
     * Refreshes a VK access token with the historical device and IP fields.
     *
     * @param context root operation context
     * @param token   non-null token set
     * @return successful message containing the refreshed token set
     */
    @Override
    public Message<VendorTokenSet> refresh(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final Map<String, String> form = new LinkedHashMap<>();
        form.put("grant_type", "refresh_token");
        form.put("refresh_token", authorization.getRefresh());
        form.put("state", ID.objectId());
        form.put("device_id", authorization.getDeviceId());
        form.put("client_id", registration.clientId());
        form.put("ip", "10.10.10.10");
        return Message.success(token(post(endpoint(VendorEndpoint.REFRESH), form, FORM_HEADER), form.get("device_id")));
    }

    /**
     * Revokes a VK access token through a form POST.
     *
     * @param context root operation context
     * @param token   non-null token set
     * @return success when VK returns response value {@code 1}, otherwise a failure message
     */
    @Override
    public Message<Void> revoke(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final RevokeResponse response = JsonKit.toPojo(
                post(
                        endpoint(VendorEndpoint.REVOKE),
                        Map.of("access_token", authorization.getToken(), "client_id", registration.clientId()),
                        FORM_HEADER),
                RevokeResponse.class);
        check(response == null ? null : response.error(), response == null ? null : response.message());
        return response != null && Symbol.ONE.equals(response.response()) ? Message.success(null)
                : Message.failure("110004", "VK revocation failed");
    }

    /**
     * Stores a PKCE verifier until the root context deadline.
     *
     * @param context  root operation context
     * @param state    authorization state
     * @param verifier sensitive verifier
     */
    private void storeVerifier(final Context context, final String state, final String verifier) {
        final Duration ttl = context.remaining(clock);
        final Boolean stored = stateStore.putIfAbsent(
                context,
                verifierKey(context, state),
                StateEnvelopeCodec.INSTANCE.encode(verifier.getBytes(StandardCharsets.UTF_8)),
                ttl).toCompletableFuture().join();
        if (!Boolean.TRUE.equals(stored))
            throw new AuthorizedException("VK PKCE verifier already exists");
    }

    /**
     * Atomically consumes and decodes a PKCE verifier.
     *
     * @param context root operation context
     * @param state   authorization state
     * @return decoded verifier
     */
    private String takeVerifier(final Context context, final String state) {
        final byte[] value = stateStore.take(context, verifierKey(context, state)).toCompletableFuture().join()
                .orElseThrow(() -> new AuthorizedException("VK PKCE verifier is missing"));
        return new String(StateEnvelopeCodec.INSTANCE.decode(value), StandardCharsets.UTF_8);
    }

    /**
     * VK token response.
     *
     * @param error         optional error identifier
     * @param message       optional error message
     * @param id_token      identity token
     * @param access_token  access token
     * @param refresh_token refresh token
     * @param token_type    token type
     * @param scope         granted scope
     * @param user_id       user identifier
     * @param expires_in    lifetime in seconds
     * @author Kimi Liu
     */
    private record TokenResponse(String error, String message, String id_token, String access_token,
            String refresh_token, String token_type, String scope, String user_id, int expires_in) {
    }

    /**
     * VK profile envelope.
     *
     * @param error   optional error identifier
     * @param message optional error message
     * @param user    profile payload
     * @author Kimi Liu
     */
    private record ProfileEnvelope(String error, String message, User user) {
    }

    /**
     * VK profile.
     *
     * @param user_id    stable identifier
     * @param first_name first name
     * @param last_name  last name
     * @param avatar     avatar URL
     * @param email      email address
     * @author Kimi Liu
     */
    private record User(String user_id, String first_name, String last_name, String avatar, String email) {
    }

    /**
     * VK revocation response.
     *
     * @param error    optional error identifier
     * @param message  optional error message
     * @param response success marker
     * @author Kimi Liu
     */
    private record RevokeResponse(String error, String message, String response) {
    }

}
