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
package org.miaixz.bus.auth.metric.oauth2;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.metric.AuthMetric.Client;
import org.miaixz.bus.auth.metric.AuthMetric.Invocation;
import org.miaixz.bus.auth.metric.AuthMetric.Runtime;
import org.miaixz.bus.auth.metric.AuthMetric.Subject;
import org.miaixz.bus.auth.metric.OAuth2.*;
import org.miaixz.bus.auth.metric.shared.json.StrictJsonReader;
import org.miaixz.bus.auth.metric.shared.security.ReplayKey;
import org.miaixz.bus.auth.metric.shared.state.StateEnvelopeCodec;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.xyz.StringKit;

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
    private final Runtime runtime;

    /**
     * Creates one device authorization state machine.
     *
     * @param policy  trusted OAuth policy
     * @param runtime validated authentication runtime
     */
    public DeviceAuthorizationFlow(final Policy policy, final Runtime runtime) {
        this.policy = Assert.notNull(policy, "OAuth policy must be not null!");
        this.runtime = Assert.notNull(runtime, "Authentication runtime must be not null!");
    }

    /**
     * Reads one required string member.
     *
     * @param values decoded object
     * @param name   member name
     * @return exact string value
     */
    private static String text(final Map<?, ?> values, final String name) {
        final Object value = values.get(name);
        if (!(value instanceof String)) {
            throw new ProtocolException(ProtocolError.INVALID_GRANT);
        }
        return (String) value;
    }

    /**
     * Reads one required integral member.
     *
     * @param values decoded object
     * @param name   member name
     * @return integral value
     */
    private static long number(final Map<?, ?> values, final String name) {
        final Object value = values.get(name);
        if (!(value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long)) {
            throw new ProtocolException(ProtocolError.INVALID_GRANT);
        }
        return ((Number) value).longValue();
    }

    /**
     * Derives one tenant-isolated device state key.
     *
     * @param invocation tenant-scoped operation context
     * @param kind       state kind
     * @param value      opaque credential
     * @return protected state key
     */
    private static String key(final Invocation invocation, final String kind, final String value) {
        return ReplayKey.derive(invocation.tenantId(), "oauth2", kind, value);
    }

    /**
     * Adds a trusted duration with stable overflow handling.
     *
     * @param instant  base instant
     * @param duration trusted duration
     * @return resulting instant
     */
    private static Instant add(final Instant instant, final Duration duration) {
        try {
            return instant.plus(duration);
        } catch (final DateTimeException | ArithmeticException failure) {
            throw new ProtocolException(ProtocolError.TEMPORARILY_UNAVAILABLE.getKey(),
                    ProtocolError.TEMPORARILY_UNAVAILABLE.getValue(), failure);
        }
    }

    /**
     * Creates an already failed protocol stage.
     *
     * @param error fixed protocol error
     * @param <T>   result type
     * @return failed stage
     */
    private static <T> CompletionStage<T> failed(final ProtocolError error) {
        return CompletableFuture.failedFuture(new ProtocolException(error));
    }

    /**
     * Creates one pending 600-second device transaction.
     *
     * @param invocation tenant-scoped operation context
     * @param request    device authorization request
     * @return stage containing device verification details
     */
    public CompletionStage<DeviceAuthorizationResponse> authorize(
            final Invocation invocation,
            final DeviceAuthorizationRequest request) {
        final Invocation context = Assert.notNull(invocation, "Invocation must be not null!");
        final DeviceAuthorizationRequest input = Assert
                .notNull(request, "Device authorization request must be not null!");
        OAuth2Validator.grant(GrantType.DEVICE_CODE, policy.grants());
        final CompletionStage<Optional<Client>> resolved = Assert.notNull(
                runtime.clients().resolve(context, input.clientId()),
                "Client resolver stage must be not null!");
        return resolved.thenCompose(optional -> {
            final Client client = OAuth2Validator.client(optional, input.clientId());
            if (!AUTH_NONE.equals(client.attributes().get(TOKEN_ENDPOINT_AUTH_METHOD))) {
                return failed(ProtocolError.UNAUTHORIZED_CLIENT);
            }
            final Set<String> scopes = OAuth2Validator.scopes(input.scopes(), policy.scopes(), runtime.limits());
            final String deviceCode = credential(DEVICE_BYTES);
            final String userCode = userCode();
            final String deviceKey = key(context, "device", deviceCode);
            final String userKey = key(context, "user", userCode);
            final Instant now = clock();
            final DeviceState state = new DeviceState(client.id(), scopes, PENDING, null, add(now, LIFETIME),
                    add(now, Duration.ofSeconds(INITIAL_INTERVAL)), INITIAL_INTERVAL);
            final CompletionStage<Boolean> deviceCreated = Assert.notNull(
                    runtime.states().putIfAbsent(context, deviceKey, encode(state), LIFETIME),
                    "State-store create stage must be not null!");
            return deviceCreated.thenCompose(created -> {
                if (!Boolean.TRUE.equals(created)) {
                    return failed(ProtocolError.TEMPORARILY_UNAVAILABLE);
                }
                final CompletionStage<Boolean> userCreated = Assert.notNull(
                        runtime.states().putIfAbsent(
                                context,
                                userKey,
                                StateEnvelopeCodec.encode(deviceKey.getBytes(StandardCharsets.US_ASCII)),
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
    public CompletionStage<Void> complete(final Invocation invocation, final DeviceVerificationRequest request) {
        final Invocation context = Assert.notNull(invocation, "Invocation must be not null!");
        final DeviceVerificationRequest input = Assert
                .notNull(request, "Device verification request must be not null!");
        if (StringKit.isBlank(input.userCode()) || StringKit.isBlank(input.subjectId())) {
            return failed(ProtocolError.INVALID_REQUEST);
        }
        final String userKey = key(context, "user", input.userCode());
        final CompletionStage<Optional<byte[]>> mapping = Assert
                .notNull(runtime.states().get(context, userKey), "State-store read stage must be not null!");
        return mapping.thenCompose(optional -> {
            if (optional == null || optional.isEmpty()) {
                return failed(ProtocolError.EXPIRED_TOKEN);
            }
            final String deviceKey = new String(StateEnvelopeCodec.decode(optional.get()), StandardCharsets.US_ASCII);
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
    public CompletionStage<TokenResponse> exchange(final Invocation invocation, final TokenRequest request) {
        final Invocation context = Assert.notNull(invocation, "Invocation must be not null!");
        final TokenRequest input = Assert.notNull(request, "Token request must be not null!");
        OAuth2Validator.grant(input.grantType(), policy.grants());
        if (input.grantType() != GrantType.DEVICE_CODE || StringKit.isBlank(input.clientId())
                || StringKit.isBlank(input.deviceCode()) || input.code() != null || input.redirectUri() != null
                || input.codeVerifier() != null || input.refreshToken() != null || !input.scopes().isEmpty()) {
            return failed(ProtocolError.INVALID_REQUEST);
        }
        final String deviceKey = key(context, "device", input.deviceCode());
        final CompletionStage<Optional<Client>> resolved = Assert.notNull(
                runtime.clients().resolve(context, input.clientId()),
                "Client resolver stage must be not null!");
        return resolved.thenCompose(optional -> {
            final Client client = OAuth2Validator.client(optional, input.clientId());
            return OAuth2Validator.authenticate(context, client, input.clientSecret(), runtime);
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
            final Invocation invocation,
            final String deviceKey,
            final DeviceVerificationRequest request) {
        final CompletionStage<Optional<byte[]>> loaded = Assert
                .notNull(runtime.states().get(invocation, deviceKey), "State-store read stage must be not null!");
        return loaded.thenCompose(optional -> {
            if (optional == null || optional.isEmpty()) {
                return failed(ProtocolError.EXPIRED_TOKEN);
            }
            final byte[] current = optional.get();
            final DeviceState state = decode(current);
            final Instant now = clock();
            if (!PENDING.equals(state.status()) || !state.expiresAt().isAfter(now)) {
                return failed(ProtocolError.EXPIRED_TOKEN);
            }
            final Set<String> scopes = OAuth2Validator
                    .scopes(request.approvedScopes(), state.scopes(), runtime.limits());
            final CompletionStage<Optional<Subject>> subject = Assert.notNull(
                    runtime.subjects().resolve(invocation, request.subjectId()),
                    "Subject resolver stage must be not null!");
            return subject.thenCompose(resolved -> {
                if (resolved == null || resolved.isEmpty() || !request.subjectId().equals(resolved.get().id())) {
                    return failed(ProtocolError.ACCESS_DENIED);
                }
                final DeviceState update = new DeviceState(state.clientId(), scopes,
                        request.approved() ? APPROVED : DENIED, request.subjectId(), state.expiresAt(),
                        state.nextPollAt(), state.interval());
                final Duration ttl = Duration.between(now, state.expiresAt());
                final CompletionStage<Boolean> replaced = Assert.notNull(
                        runtime.states().compareAndSet(invocation, deviceKey, current, encode(update), ttl),
                        "State-store replace stage must be not null!");
                return replaced.thenCompose(
                        success -> Boolean.TRUE.equals(success) ? CompletableFuture.completedFuture(null)
                                : failed(ProtocolError.INVALID_REQUEST));
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
            final Invocation invocation,
            final String deviceKey,
            final Client client) {
        final CompletionStage<Optional<byte[]>> loaded = Assert
                .notNull(runtime.states().get(invocation, deviceKey), "State-store read stage must be not null!");
        return loaded.thenCompose(optional -> {
            if (optional == null || optional.isEmpty()) {
                return failed(ProtocolError.EXPIRED_TOKEN);
            }
            final byte[] current = optional.get();
            final DeviceState state = decode(current);
            final Instant now = clock();
            if (!state.clientId().equals(client.id()) || !state.expiresAt().isAfter(now)) {
                return failed(ProtocolError.EXPIRED_TOKEN);
            }
            if (DENIED.equals(state.status())) {
                return consumeFailure(invocation, deviceKey, ProtocolError.ACCESS_DENIED);
            }
            if (APPROVED.equals(state.status())) {
                final CompletionStage<Optional<byte[]>> taken = Assert.notNull(
                        runtime.states().take(invocation, deviceKey),
                        "State-store take stage must be not null!");
                return taken.thenCompose(
                        value -> value == null || value.isEmpty() ? failed(ProtocolError.EXPIRED_TOKEN)
                                : AuthorizationCodeFlow.issueTokens(
                                        invocation,
                                        state.subjectId(),
                                        state.clientId(),
                                        state.scopes(),
                                        true,
                                        null,
                                        policy,
                                        runtime));
            }
            final boolean early = now.isBefore(state.nextPollAt());
            final long interval = early ? Math.addExact(state.interval(), SLOW_DOWN_INCREMENT) : state.interval();
            final DeviceState update = new DeviceState(state.clientId(), state.scopes(), PENDING, null,
                    state.expiresAt(), add(now, Duration.ofSeconds(interval)), interval);
            final CompletionStage<Boolean> replaced = Assert.notNull(
                    runtime.states().compareAndSet(
                            invocation,
                            deviceKey,
                            current,
                            encode(update),
                            Duration.between(now, state.expiresAt())),
                    "State-store replace stage must be not null!");
            return replaced.thenCompose(
                    success -> Boolean.TRUE.equals(success)
                            ? failed(early ? ProtocolError.SLOW_DOWN : ProtocolError.AUTHORIZATION_PENDING)
                            : failed(ProtocolError.TEMPORARILY_UNAVAILABLE));
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
            final Invocation invocation,
            final String key,
            final ProtocolError error) {
        final CompletionStage<Optional<byte[]>> taken = Assert
                .notNull(runtime.states().take(invocation, key), "State-store take stage must be not null!");
        return taken.thenCompose(ignored -> failed(error));
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
    private <T> CompletionStage<T> cleanup(final Invocation invocation, final String key, final ProtocolError error) {
        final CompletionStage<Boolean> removed = Assert
                .notNull(runtime.states().remove(invocation, key), "State-store remove stage must be not null!");
        return removed.thenCompose(ignored -> failed(error));
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
        values.put("scopes", String.join(Symbol.SPACE, state.scopes()));
        values.put("status", state.status());
        values.put("subject", state.subjectId() == null ? "" : state.subjectId());
        values.put("expires_at", state.expiresAt().getEpochSecond());
        values.put("next_poll_at", state.nextPollAt().getEpochSecond());
        values.put("interval", state.interval());
        return StateEnvelopeCodec.encode(runtime.json().write(Map.copyOf(values)));
    }

    /**
     * Decodes one strict device state envelope.
     *
     * @param envelope encoded state
     * @return validated device state
     */
    private DeviceState decode(final byte[] envelope) {
        final Object decoded = new StrictJsonReader(runtime.json(), runtime.limits())
                .read(StateEnvelopeCodec.decode(envelope), Map.class);
        if (!(decoded instanceof Map<?, ?> values)) {
            throw new ProtocolException(ProtocolError.INVALID_GRANT);
        }
        final String scopes = text(values, "scopes");
        final LinkedHashSet<String> scopeSet = scopes.isEmpty() ? new LinkedHashSet<>()
                : new LinkedHashSet<>(StringKit.split(scopes, Symbol.SPACE));
        final String subject = text(values, "subject");
        return new DeviceState(text(values, "client"), Set.copyOf(scopeSet), text(values, "status"),
                subject.isEmpty() ? null : subject, Instant.ofEpochSecond(number(values, "expires_at")),
                Instant.ofEpochSecond(number(values, "next_poll_at")), number(values, "interval"));
    }

    /**
     * Generates one opaque random credential.
     *
     * @param bytes exact random byte count
     * @return unpadded Base64url credential
     */
    private String credential(final int bytes) {
        final byte[] value = runtime.random().nextBytes(bytes);
        if (value == null || value.length != bytes) {
            throw new ProtocolException(ProtocolError.TEMPORARILY_UNAVAILABLE);
        }
        return Base64.encodeUrlSafe(value);
    }

    /**
     * Generates one uppercase user-facing code without ambiguous punctuation.
     *
     * @return formatted user code
     */
    private String userCode() {
        final String value = credential(USER_BYTES).toUpperCase(java.util.Locale.ROOT);
        return value.substring(Normal._0, Normal._4) + Symbol.MINUS + value.substring(Normal._4, Normal._8);
    }

    /**
     * Returns the current security-clock instant.
     *
     * @return non-null instant
     */
    private Instant clock() {
        return Assert.notNull(runtime.clock().now(), "Clock value must be not null!");
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
     */
    private record DeviceState(String clientId, Set<String> scopes, String status, String subjectId, Instant expiresAt,
            Instant nextPollAt, long interval) {
    }

}
