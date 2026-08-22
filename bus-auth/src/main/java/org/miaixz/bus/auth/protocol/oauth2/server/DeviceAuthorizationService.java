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
package org.miaixz.bus.auth.protocol.oauth2.server;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.cache.DeviceCodeCache;
import org.miaixz.bus.auth.cache.ExpiringValue;
import org.miaixz.bus.auth.guard.ScopeValidator;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.resolver.ConsumerMetadata;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Issues RFC 8628 device and user codes and creates their initial pending atomic state.
 * <p>
 * This service implements only the standard device authorization operation. The externally hosted verification page
 * owns user interaction and authorization, while device polling remains a device-code grant at the standard token
 * endpoint.
 * </p>
 *
 * @author Kimi Liu
 */
public class DeviceAuthorizationService {

    /**
     * Uppercase alphabet excluding visually ambiguous I, L, O, U, 0, and 1 characters.
     */
    private static final char[] USER_CODE_ALPHABET = "23456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();

    /**
     * Provider identifier used to isolate device-code state.
     */
    private final String providerId;

    /**
     * Frozen Provider options supplying grant, lifetime, interval, and verification URI policy.
     */
    private final OAuth2ServerOptions options;

    /**
     * External client loader and framework atomic device-code cache.
     */
    private final DriverServices services;

    /**
     * Standard scope validator used for client and Provider subset checks.
     */
    private final ScopeValidator scopeValidator;

    /**
     * Number of secure random bytes required by the OAuth security baseline.
     */
    private final int deviceCodeBytes;

    /**
     * Creates a device authorization service for one compiled OAuth Provider.
     *
     * @param providerId     compiled server-role Source identifier
     * @param options        validated Provider options
     * @param services       caller-owned runtime dependencies
     * @param scopeValidator standard scope validator
     * @throws IllegalArgumentException if text is blank or a collaborator is {@code null}
     */
    public DeviceAuthorizationService(final String providerId, final OAuth2ServerOptions options,
            final DriverServices services, final ScopeValidator scopeValidator) {
        this.providerId = Assert.notBlank(providerId, "OAuth 2.x Provider id must not be blank");
        this.options = Assert.notNull(options, "OAuth 2.x Provider options must not be null");
        this.services = Assert.notNull(services, "OAuth 2.x execution services must not be null");
        this.scopeValidator = Assert.notNull(scopeValidator, "OAuth 2.x scope validator must not be null");
        final int entropyBits = Math
                .max(Normal._256, services.securityBaseline().require(Protocol.OAUTH2).minimumEntropyBits());
        this.deviceCodeBytes = (entropyBits + Byte.SIZE - 1) / Byte.SIZE;
    }

    /**
     * Generates a grouped, unambiguous user verification code using the shared secure random source.
     *
     * @return eight-character code formatted as two four-character groups
     */
    private static String userCode() {
        final StringBuilder value = new StringBuilder(Normal._8 + 1);
        final SecureRandom random = RandomKit.getSecureRandom();
        for (int index = 0; index < Normal._8; index++) {
            if (index == Normal._4) {
                value.append(Symbol.C_MINUS);
            }
            value.append(USER_CODE_ALPHABET[random.nextInt(USER_CODE_ALPHABET.length)]);
        }
        return value.toString();
    }

    /**
     * Creates a safe framework failure carrying one registered OAuth error identifier.
     *
     * @param error       existing Bus error definition
     * @param oauthError  registered OAuth error code
     * @param description non-sensitive diagnostic description
     * @return immutable safe failure
     */
    private static Outcome.Failure failure(
            final Errors error,
            final OAuth2ErrorCode oauthError,
            final String description) {
        return new Outcome.Failure(error, description, new JsonValue.ObjectValue(
                Map.of(org.miaixz.bus.auth.Builder.OAUTH_ERROR, new JsonValue.StringValue(oauthError.value()))));
    }

    /**
     * Creates an already-completed device authorization outcome stage.
     *
     * @param outcome device authorization outcome
     * @return completed stage
     */
    private static CompletionStage<Outcome<DeviceAuthorizationResponse>> completed(
            final Outcome<DeviceAuthorizationResponse> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Validates one standard request and issues its device and user verification codes.
     *
     * @param request standard device authorization request
     * @param client  resolved immutable client metadata
     * @param context invocation context carrying an authenticated or identified client
     * @param timeout shared end-to-end operation timeout
     * @return asynchronous standard device authorization response outcome
     */
    public CompletionStage<Outcome<DeviceAuthorizationResponse>> deviceAuthorization(
            final DeviceAuthorizationRequest request,
            final ConsumerMetadata client,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(request, "OAuth 2.x device authorization request must not be null");
        Assert.notNull(client, "OAuth 2.x device authorization client must not be null");
        Assert.notNull(context, "OAuth 2.x device authorization context must not be null");
        Assert.notNull(timeout, "OAuth 2.x device authorization timeout must not be null");
        if (timeout.expired()) {
            return completed(
                    Outcome.failed(
                            failure(
                                    ErrorCode._408,
                                    OAuth2ErrorCode.TEMPORARILY_UNAVAILABLE,
                                    "OAuth 2.x device authorization has no remaining timeout")));
        }
        if (!options.grantTypesSupported().contains(GrantType.DEVICE_CODE)
                || options.deviceAuthorizationEndpoint().isEmpty() || options.deviceVerificationUri().isEmpty()) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    OAuth2ErrorCode.UNSUPPORTED_GRANT_TYPE,
                                    "OAuth 2.x device authorization is disabled by the Provider")));
        }
        final String clientId = client.id();
        if (clientId == null || !clientId.equals(request.clientId())) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._401,
                                    OAuth2ErrorCode.INVALID_CLIENT,
                                    "OAuth 2.x device authorization client binding is invalid")));
        }

        if (!client.grantTypes().contains(GrantType.DEVICE_CODE)) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    OAuth2ErrorCode.UNAUTHORIZED_CLIENT,
                                    "OAuth 2.x client is not registered for the device authorization grant")));
        }
        final List<String> scope = request.scope().isEmpty() ? List.of() : request.scope().getOrNull().values();
        if (!validScope(scope, client)) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    OAuth2ErrorCode.INVALID_SCOPE,
                                    "OAuth 2.x device authorization scope is not allowed")));
        }
        return create(clientId, scope, timeout, 1);
    }

    /**
     * Creates pending device authorization state, retrying only atomic uniqueness collisions.
     *
     * @param clientId verified client identifier
     * @param scope    validated requested scope
     * @param timeout  shared operation timeout
     * @param attempt  one-based create attempt number
     * @return asynchronous device authorization response outcome
     */
    private CompletionStage<Outcome<DeviceAuthorizationResponse>> create(
            final String clientId,
            final List<String> scope,
            final Timeout timeout,
            final int attempt) {
        if (timeout.expired()) {
            return completed(
                    Outcome.failed(
                            failure(
                                    ErrorCode._408,
                                    OAuth2ErrorCode.TEMPORARILY_UNAVAILABLE,
                                    "OAuth 2.x device authorization has no remaining timeout")));
        }
        final String deviceCode = deviceCode();
        final String userCode = userCode();
        final Instant expiresAt = timeout.clock().now().plus(options.deviceCodeLifetime());
        final DeviceCodeCache.Entry entry = new DeviceCodeCache.Entry(providerId, clientId, userCode, scope,
                DeviceCodeCache.Status.PENDING, options.devicePollingInterval(), Optional.empty(), Optional.empty());
        final CompletionStage<Boolean> creation;
        try {
            creation = services.deviceCodeCache().issue(key(deviceCode), new ExpiringValue<>(entry, expiresAt));
        } catch (RuntimeException exception) {
            return completed(
                    Outcome.failed(
                            failure(
                                    ErrorCode._500,
                                    OAuth2ErrorCode.SERVER_ERROR,
                                    "OAuth 2.x device authorization state persistence failed")));
        }
        return creation.handle((created, thrown) -> new CreateResult(Boolean.TRUE.equals(created), thrown))
                .thenCompose(result -> {
                    if (result.failure() != null) {
                        return completed(
                                Outcome.failed(
                                        failure(
                                                ErrorCode._500,
                                                OAuth2ErrorCode.SERVER_ERROR,
                                                "OAuth 2.x device authorization state persistence failed")));
                    }
                    if (!result.created() && attempt < org.miaixz.bus.auth.Builder.MAXIMUM_RETRY_ATTEMPTS) {
                        return create(clientId, scope, timeout, attempt + 1);
                    }
                    if (!result.created()) {
                        return completed(
                                Outcome.failed(
                                        failure(
                                                ErrorCode._500,
                                                OAuth2ErrorCode.SERVER_ERROR,
                                                "OAuth 2.x device authorization code allocation failed")));
                    }
                    final String verificationUri = options.deviceVerificationUri().getOrNull();
                    final String complete = Url.parse(verificationUri).newBuilder()
                            .query(OAuth2.Parameters.USER_CODE, userCode).build().toString();
                    return completed(
                            Outcome.succeeded(
                                    new DeviceAuthorizationResponse(deviceCode, userCode, verificationUri,
                                            Optional.of(complete), options.deviceCodeLifetime().toSeconds(),
                                            Optional.of(options.devicePollingInterval().toSeconds()))));
                });
    }

    /**
     * Validates the requested scope against the client and server-role Source options.
     *
     * @param scope  requested scope-token list
     * @param client resolved client registration
     * @return whether every scope is allowed
     */
    private boolean validScope(final List<String> scope, final ConsumerMetadata client) {
        try {
            scopeValidator.validateRequested(scope, client.scopes());
            scopeValidator.validateRequested(scope, options.scopesSupported());
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    /**
     * Generates an opaque Base64URL device code and clears its temporary random bytes.
     *
     * @return high-entropy device code
     */
    private String deviceCode() {
        final byte[] bytes = RandomKit.randomBytes(deviceCodeBytes, RandomKit.getSecureRandom());
        try {
            return Base64.encodeUrlSafe(bytes);
        } finally {
            Arrays.fill(bytes, (byte) 0);
        }
    }

    /**
     * Produces a Provider-isolated irreversible key for an opaque device code.
     *
     * @param deviceCode generated opaque device code
     * @return hexadecimal SHA-256 cache key
     */
    private String key(final String deviceCode) {
        return Builder.sha256Hex(providerId + Symbol.C_NUL + deviceCode);
    }

    /**
     * Couples an atomic create result with its completion failure.
     *
     * @param created whether pending state was created
     * @param failure completion failure, or {@code null}
     * @author Kimi Liu
     */
    private record CreateResult(boolean created, Throwable failure) {

    }

}
