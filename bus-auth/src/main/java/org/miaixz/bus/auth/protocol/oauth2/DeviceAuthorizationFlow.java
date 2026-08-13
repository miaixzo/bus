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
package org.miaixz.bus.auth.protocol.oauth2;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Subject;
import org.miaixz.bus.auth.cache.StateStore;
import org.miaixz.bus.auth.codec.json.JsonValues;
import org.miaixz.bus.auth.codec.state.StateEnvelopeCodec;
import org.miaixz.bus.auth.codec.state.StateJsonCodec;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2.*;
import org.miaixz.bus.auth.resolver.SecretResolver;
import org.miaixz.bus.auth.resolver.SubjectResolver;
import org.miaixz.bus.auth.runtime.Limits;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.fabric.Clock;

/**
 * Implements the OAuth device authorization grant with bounded polling and one-time completion.
 * <p>
 * Device and user codes expire after exactly 600 seconds. Poll state and verification decisions use state-store
 * compare-and-set operations, while successful token exchange atomically takes the approved state. Polling before the
 * current interval returns {@code slow_down} and increases the interval by five seconds.
 * </p>
 *
 * @author Kimi Liu
 */
public final class DeviceAuthorizationFlow {

    /**
     * Exact device transaction lifetime.
     */
    private static final Duration LIFETIME = Duration.ofSeconds(600);

    /**
     * Initial polling interval in seconds.
     */
    private static final long INITIAL_INTERVAL = Normal._5;

    /**
     * Slow-down interval increment in seconds.
     */
    private static final long SLOW_DOWN_INCREMENT = Normal._5;

    /**
     * Random byte count for the secret device code.
     */
    private static final int DEVICE_BYTES = Normal._32;

    /**
     * Random byte count encoded into the user-facing code.
     */
    private static final int USER_BYTES = Normal._6;

    /**
     * Pending transaction status.
     */
    private static final String PENDING = "pending";

    /**
     * Approved transaction status.
     */
    private static final String APPROVED = "approved";

    /**
     * Denied transaction status.
     */
    private static final String DENIED = "denied";

    /**
     * Registered client attribute selecting token-endpoint authentication.
     */
    private static final String TOKEN_ENDPOINT_AUTH_METHOD = "token_endpoint_auth_method";

    /**
     * Public-client authentication method required by the unauthenticated device endpoint.
     */
    private static final String AUTH_NONE = "none";

    /**
     * Trusted OAuth policy.
     */
    private final Policy policy;

    /**
     * Validated authentication runtime.
     */
    private final OAuth2Dependencies dependencies;

    /**
     * Tenant-isolated atomic device transaction store.
     */
    private final StateStore states;

    /**
     * Trusted subject resolver used when a user approves a device.
     */
    private final SubjectResolver subjects;

    /**
     * Product secret resolver used during token polling client authentication.
     */
    private final SecretResolver secrets;

    /**
     * Fabric clock used for transaction expiration and polling intervals.
     */
    private final Clock clock;

    /**
     * Secure random source used for device and user codes.
     */
    private final SecureRandom random;

    /**
     * Explicit JSON provider used for bounded device state.
     */
    private final JsonProvider json;

    /**
     * Closed parser and allocation limits.
     */
    private final Limits limits;

    /**
     * Creates one device authorization state machine.
     *
     * @param policy       trusted OAuth policy
     * @param dependencies registered-client and authorization-grant product ports
     * @param states       tenant-isolated atomic state store
     * @param subjects     trusted subject resolver
     * @param secrets      client secret resolver
     * @param clock        Fabric protocol clock
     * @param random       secure random source for generated codes
     * @param json         explicit JSON provider for bounded state
     * @param limits       closed parser and allocation limits
     */
    public DeviceAuthorizationFlow(final Policy policy, final OAuth2Dependencies dependencies, final StateStore states,
            final SubjectResolver subjects, final SecretResolver secrets, final Clock clock, final SecureRandom random,
            final JsonProvider json, final Limits limits) {
        this.policy = Assert.notNull(policy, "OAuth policy must be not null!");
        this.dependencies = Assert.notNull(dependencies, "OAuth dependencies must be not null!");
        this.states = Assert.notNull(states, "State store must be not null!");
        this.subjects = Assert.notNull(subjects, "Subject resolver must be not null!");
        this.secrets = Assert.notNull(secrets, "Secret resolver must be not null!");
        this.clock = Assert.notNull(clock, "Clock must be not null!");
        this.random = Assert.notNull(random, "Secure random must be not null!");
        this.json = Assert.notNull(json, "JSON provider must be not null!");
        this.limits = Assert.notNull(limits, "Limits must be not null!");
    }

    /**
     * Creates the fixed invalid-grant JSON member failure.
     */
    private static RuntimeException invalidGrant() {
        return new ProtocolException(ProtocolError.INVALID_GRANT);
    }

    /**
     * Creates one pending 600-second device transaction.
     *
     * @param invocation tenant-scoped operation context
     * @param request    device authorization request
     * @return stage containing device verification details
     */
    public CompletionStage<DeviceAuthorizationResponse> authorize(
            final Context invocation,
            final DeviceAuthorizationRequest request) {
        final Context context = Assert.notNull(invocation, "Context must be not null!");
        final DeviceAuthorizationRequest input = Assert
                .notNull(request, "Device authorization request must be not null!");
        OAuth2Validator.grant(GrantType.DEVICE_CODE, policy.grants());
        final CompletionStage<Optional<RegisteredClient>> resolved = Assert.notNull(
                dependencies.clients().resolve(context, input.clientId()),
                "RegisteredClient resolver stage must be not null!");
        return resolved.thenCompose(optional -> {
            final RegisteredClient client = OAuth2Validator.client(optional, input.clientId());
            if (!AUTH_NONE.equals(client.tokenEndpointAuthMethod())) {
                return OAuth2Support.failed(ProtocolError.UNAUTHORIZED_CLIENT);
            }
            final Set<String> scopes = OAuth2Validator.scopes(input.scopes(), policy.scopes(), limits);
            final String deviceCode = OAuth2Support
                    .credential(random, DEVICE_BYTES, ProtocolError.TEMPORARILY_UNAVAILABLE);
            final String userCode = userCode();
            final String deviceKey = OAuth2Support.key(context, "device", deviceCode);
            final String userKey = OAuth2Support.key(context, "user", userCode);
            final Instant now = OAuth2Support.now(clock);
            final DeviceState state = new DeviceState(client.id(), scopes, PENDING, null,
                    OAuth2Support.add(now, LIFETIME, ProtocolError.TEMPORARILY_UNAVAILABLE),
                    OAuth2Support.add(now, Duration.ofSeconds(INITIAL_INTERVAL), ProtocolError.TEMPORARILY_UNAVAILABLE),
                    INITIAL_INTERVAL);
            final CompletionStage<Boolean> deviceCreated = Assert.notNull(
                    states.putIfAbsent(context, deviceKey, encode(state), LIFETIME),
                    "State-store create stage must be not null!");
            return deviceCreated.thenCompose(created -> {
                if (!Boolean.TRUE.equals(created)) {
                    return OAuth2Support.failed(ProtocolError.TEMPORARILY_UNAVAILABLE);
                }
                final CompletionStage<Boolean> userCreated = Assert
                        .notNull(
                                states.putIfAbsent(
                                        context,
                                        userKey,
                                        StateEnvelopeCodec.INSTANCE
                                                .encode(deviceKey.getBytes(StandardCharsets.US_ASCII)),
                                        LIFETIME),
                                "State-store create stage must be not null!");
                return userCreated.thenCompose(mapped -> {
                    if (!Boolean.TRUE.equals(mapped)) {
                        return cleanup(context, deviceKey, ProtocolError.TEMPORARILY_UNAVAILABLE);
                    }
                    final URI complete = URI.create(
                            policy.deviceVerificationUri().toASCIIString()
                                    + (policy.deviceVerificationUri().getRawQuery() == null ? Symbol.QUESTION_MARK
                                            : Symbol.AND)
                                    + "user_code=" + userCode);
                    return CompletableFuture.completedFuture(
                            new DeviceAuthorizationResponse(deviceCode, userCode, policy.deviceVerificationUri(),
                                    complete, LIFETIME.toSeconds(), INITIAL_INTERVAL));
                });
            });
        });
    }

    /**
     * Applies one verification-page approval or denial exactly once.
     *
     * @param invocation tenant-scoped operation context
     * @param request    verification-page decision
     * @return stage completed after the state transition
     */
    public CompletionStage<Void> complete(final Context invocation, final DeviceVerificationRequest request) {
        final Context context = Assert.notNull(invocation, "Context must be not null!");
        final DeviceVerificationRequest input = Assert
                .notNull(request, "Device verification request must be not null!");
        if (StringKit.isBlank(input.userCode()) || StringKit.isBlank(input.subjectId())) {
            return OAuth2Support.failed(ProtocolError.INVALID_REQUEST);
        }
        final String userKey = OAuth2Support.key(context, "user", input.userCode());
        final CompletionStage<Optional<byte[]>> mapping = Assert
                .notNull(states.get(context, userKey), "State-store read stage must be not null!");
        return mapping.thenCompose(optional -> {
            if (optional == null || optional.isEmpty()) {
                return OAuth2Support.failed(ProtocolError.EXPIRED_TOKEN);
            }
            final String deviceKey = new String(StateEnvelopeCodec.INSTANCE.decode(optional.get()),
                    StandardCharsets.US_ASCII);
            return decide(context, deviceKey, input);
        });
    }

    /**
     * Polls one device transaction and consumes an approved code exactly once.
     *
     * @param invocation tenant-scoped operation context
     * @param request    device-code token request
     * @return stage containing issued tokens
     */
    public CompletionStage<TokenResponse> exchange(final Context invocation, final TokenRequest request) {
        final Context context = Assert.notNull(invocation, "Context must be not null!");
        final TokenRequest input = Assert.notNull(request, "Token request must be not null!");
        OAuth2Validator.grant(input.grantType(), policy.grants());
        if (input.grantType() != GrantType.DEVICE_CODE || StringKit.isBlank(input.clientId())
                || StringKit.isBlank(input.deviceCode()) || input.code() != null || input.redirectUri() != null
                || input.codeVerifier() != null || input.refreshToken() != null || !input.scopes().isEmpty()) {
            return OAuth2Support.failed(ProtocolError.INVALID_REQUEST);
        }
        final String deviceKey = OAuth2Support.key(context, "device", input.deviceCode());
        final CompletionStage<Optional<RegisteredClient>> resolved = Assert.notNull(
                dependencies.clients().resolve(context, input.clientId()),
                "RegisteredClient resolver stage must be not null!");
        return resolved.thenCompose(optional -> {
            final RegisteredClient client = OAuth2Validator.client(optional, input.clientId());
            return OAuth2Validator.authenticate(context, client, input.clientSecret(), secrets);
        }).thenCompose(client -> poll(context, deviceKey, client));
    }

    /**
     * Applies the user decision to the authoritative device state.
     *
     * @param invocation tenant-scoped operation context
     * @param deviceKey  protected device state key
     * @param request    verification decision
     * @return completion stage
     */
    private CompletionStage<Void> decide(
            final Context invocation,
            final String deviceKey,
            final DeviceVerificationRequest request) {
        final CompletionStage<Optional<byte[]>> loaded = Assert
                .notNull(states.get(invocation, deviceKey), "State-store read stage must be not null!");
        return loaded.thenCompose(optional -> {
            if (optional == null || optional.isEmpty()) {
                return OAuth2Support.failed(ProtocolError.EXPIRED_TOKEN);
            }
            final byte[] current = optional.get();
            final DeviceState state = decode(current);
            final Instant now = OAuth2Support.now(clock);
            if (!PENDING.equals(state.status()) || !state.expiresAt().isAfter(now)) {
                return OAuth2Support.failed(ProtocolError.EXPIRED_TOKEN);
            }
            final Set<String> scopes = OAuth2Validator.scopes(request.approvedScopes(), state.scopes(), limits);
            final CompletionStage<Optional<Subject>> subject = Assert.notNull(
                    subjects.resolve(invocation, request.subjectId()),
                    "Subject resolver stage must be not null!");
            return subject.thenCompose(resolved -> {
                if (resolved == null || resolved.isEmpty() || !request.subjectId().equals(resolved.get().id())) {
                    return OAuth2Support.failed(ProtocolError.ACCESS_DENIED);
                }
                final DeviceState update = new DeviceState(state.clientId(), scopes,
                        request.approved() ? APPROVED : DENIED, request.subjectId(), state.expiresAt(),
                        state.nextPollAt(), state.interval());
                final Duration ttl = Duration.between(now, state.expiresAt());
                final CompletionStage<Boolean> replaced = Assert.notNull(
                        states.compareAndSet(invocation, deviceKey, current, encode(update), ttl),
                        "State-store replace stage must be not null!");
                return replaced.thenCompose(
                        success -> Boolean.TRUE.equals(success) ? CompletableFuture.completedFuture(null)
                                : OAuth2Support.failed(ProtocolError.INVALID_REQUEST));
            });
        });
    }

    /**
     * Executes one interval-aware poll against the authoritative device state.
     *
     * @param invocation tenant-scoped operation context
     * @param deviceKey  protected device state key
     * @param client     authenticated client
     * @return token or protocol-failure stage
     */
    private CompletionStage<TokenResponse> poll(
            final Context invocation,
            final String deviceKey,
            final RegisteredClient client) {
        final CompletionStage<Optional<byte[]>> loaded = Assert
                .notNull(states.get(invocation, deviceKey), "State-store read stage must be not null!");
        return loaded.thenCompose(optional -> {
            if (optional == null || optional.isEmpty()) {
                return OAuth2Support.failed(ProtocolError.EXPIRED_TOKEN);
            }
            final byte[] current = optional.get();
            final DeviceState state = decode(current);
            final Instant now = OAuth2Support.now(clock);
            if (!state.clientId().equals(client.id()) || !state.expiresAt().isAfter(now)) {
                return OAuth2Support.failed(ProtocolError.EXPIRED_TOKEN);
            }
            if (DENIED.equals(state.status())) {
                return consumeFailure(invocation, deviceKey, ProtocolError.ACCESS_DENIED);
            }
            if (APPROVED.equals(state.status())) {
                final CompletionStage<Optional<byte[]>> taken = Assert
                        .notNull(states.take(invocation, deviceKey), "State-store take stage must be not null!");
                return taken.thenCompose(
                        value -> value == null || value.isEmpty() ? OAuth2Support.failed(ProtocolError.EXPIRED_TOKEN)
                                : AuthorizationCodeFlow.issueTokens(
                                        invocation,
                                        state.subjectId(),
                                        state.clientId(),
                                        state.scopes(),
                                        true,
                                        null,
                                        policy,
                                        dependencies,
                                        states,
                                        clock,
                                        random,
                                        json,
                                        limits));
            }
            final boolean early = now.isBefore(state.nextPollAt());
            final long interval = early ? Math.addExact(state.interval(), SLOW_DOWN_INCREMENT) : state.interval();
            final DeviceState update = new DeviceState(state.clientId(), state.scopes(), PENDING, null,
                    state.expiresAt(),
                    OAuth2Support.add(now, Duration.ofSeconds(interval), ProtocolError.TEMPORARILY_UNAVAILABLE),
                    interval);
            final CompletionStage<Boolean> replaced = Assert.notNull(
                    states.compareAndSet(
                            invocation,
                            deviceKey,
                            current,
                            encode(update),
                            Duration.between(now, state.expiresAt())),
                    "State-store replace stage must be not null!");
            return replaced.thenCompose(
                    success -> Boolean.TRUE.equals(success)
                            ? OAuth2Support
                                    .failed(early ? ProtocolError.SLOW_DOWN : ProtocolError.AUTHORIZATION_PENDING)
                            : OAuth2Support.failed(ProtocolError.TEMPORARILY_UNAVAILABLE));
        });
    }

    /**
     * Removes terminal device state before returning its stable failure.
     *
     * @param invocation tenant-scoped operation context
     * @param key        protected state key
     * @param error      terminal protocol error
     * @param <T>        result type
     * @return failed stage
     */
    private <T> CompletionStage<T> consumeFailure(
            final Context invocation,
            final String key,
            final ProtocolError error) {
        final CompletionStage<Optional<byte[]>> taken = Assert
                .notNull(states.take(invocation, key), "State-store take stage must be not null!");
        return taken.thenCompose(ignored -> OAuth2Support.failed(error));
    }

    /**
     * Removes partially created state before returning a stable failure.
     *
     * @param invocation tenant-scoped operation context
     * @param key        protected state key
     * @param error      protocol error
     * @param <T>        result type
     * @return failed stage
     */
    private <T> CompletionStage<T> cleanup(final Context invocation, final String key, final ProtocolError error) {
        final CompletionStage<Boolean> removed = Assert
                .notNull(states.remove(invocation, key), "State-store remove stage must be not null!");
        return removed.thenCompose(ignored -> OAuth2Support.failed(error));
    }

    /**
     * Encodes one device state into the common authenticated envelope.
     *
     * @param state immutable device state
     * @return encoded state
     */
    private byte[] encode(final DeviceState state) {
        final Map<String, Object> values = new LinkedHashMap<>();
        values.put("client", state.clientId());
        values.put("scopes", OAuth2Support.encodeScopes(state.scopes()));
        values.put("status", state.status());
        values.put("subject", state.subjectId() == null ? "" : state.subjectId());
        values.put("expires_at", state.expiresAt().getEpochSecond());
        values.put("next_poll_at", state.nextPollAt().getEpochSecond());
        values.put("interval", state.interval());
        return new StateJsonCodec(json, limits.maxJsonBytes(), limits.maxJsonDepth()).encode(values);
    }

    /**
     * Decodes one strict device state envelope.
     *
     * @param envelope encoded state
     * @return validated device state
     */
    private DeviceState decode(final byte[] envelope) {
        final Map<String, Object> values = new StateJsonCodec(json, limits.maxJsonBytes(), limits.maxJsonDepth())
                .decode(envelope);
        final int maximum = limits.maxJsonBytes();
        final String subject = JsonValues.text(values, "subject", maximum, DeviceAuthorizationFlow::invalidGrant);
        return new DeviceState(
                JsonValues.requiredText(values, "client", maximum, DeviceAuthorizationFlow::invalidGrant),
                OAuth2Support.decodeScopes(
                        JsonValues.text(values, "scopes", maximum, DeviceAuthorizationFlow::invalidGrant),
                        ProtocolError.INVALID_GRANT),
                JsonValues.requiredText(values, "status", maximum, DeviceAuthorizationFlow::invalidGrant),
                subject.isEmpty() ? null : subject,
                Instant.ofEpochSecond(JsonValues.integer(values, "expires_at", DeviceAuthorizationFlow::invalidGrant)),
                Instant.ofEpochSecond(
                        JsonValues.integer(values, "next_poll_at", DeviceAuthorizationFlow::invalidGrant)),
                JsonValues.integer(values, "interval", DeviceAuthorizationFlow::invalidGrant));
    }

    /**
     * Generates one uppercase user-facing code without ambiguous punctuation.
     *
     * @return formatted user code
     */
    private String userCode() {
        final String value = OAuth2Support.credential(random, USER_BYTES, ProtocolError.TEMPORARILY_UNAVAILABLE)
                .toUpperCase(java.util.Locale.ROOT);
        return value.substring(Normal._0, Normal._4) + Symbol.MINUS + value.substring(Normal._4, Normal._8);
    }

    /**
     * Immutable device transaction state.
     *
     * @param clientId   registered client identifier
     * @param scopes     authorized scopes
     * @param status     pending, approved, or denied status
     * @param subjectId  optional approved subject
     * @param expiresAt  fixed transaction expiration
     * @param nextPollAt earliest next poll
     * @param interval   current polling interval in seconds
     * @author Kimi Liu
     */
    private record DeviceState(String clientId, Set<String> scopes, String status, String subjectId, Instant expiresAt,
            Instant nextPollAt, long interval) {
    }

}
