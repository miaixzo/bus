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
package org.miaixz.bus.auth.protocol.saml.server;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Session;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.cache.ExpiringValue;
import org.miaixz.bus.auth.protocol.saml.*;
import org.miaixz.bus.auth.resolver.ConsumerMetadata;
import org.miaixz.bus.auth.runtime.ExecutionServices;
import org.miaixz.bus.auth.runtime.LoadResult;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.data.id.UUID;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Processes the SAML 2.0 Single Logout profile at an identity provider.
 * <p>
 * The service supports one framework SessionIndex per request because the root framework defines one session model. The
 * registered service provider supplies its standard {@code single_logout_service_url} through externally resolved
 * client metadata; the request itself cannot select an arbitrary response destination.
 * </p>
 *
 * @author Kimi Liu
 */
public final class SingleLogoutService {

    /**
     * Maximum compare-and-replace attempts after concurrent session changes.
     */
    private static final int MAXIMUM_REPLACE_ATTEMPTS = 3;

    /**
     * Registered metadata member containing the service-provider SingleLogoutService response location.
     */
    private static final String SINGLE_LOGOUT_SERVICE_URL = "single_logout_service_url";

    /**
     * SAML entity identifier NameID format.
     */
    private static final String ENTITY_NAME_ID = "urn:oasis:names:tc:SAML:2.0:nameid-format:entity";

    /**
     * Standard second-level status for an unknown session principal.
     */
    private static final String UNKNOWN_PRINCIPAL = "urn:oasis:names:tc:SAML:2.0:status:UnknownPrincipal";

    /**
     * Validated identity-provider options.
     */
    private final SamlServerOptions options;

    /**
     * Compiled server-role Source identifier used to isolate the shared Session cache key.
     */
    private final String providerId;

    /**
     * External loaders and framework-owned parsers used by this service.
     */
    private final ExecutionServices services;

    /**
     * Standard SAML error response mapper.
     */
    private final SamlErrorMapper errorMapper;

    /**
     * Creates a SingleLogoutService with explicit registration and session dependencies.
     *
     * @param providerId  compiled server-role Source identifier
     * @param options     validated SAML Provider options
     * @param services    externally owned execution services
     * @param errorMapper standard SAML error response mapper
     * @throws IllegalArgumentException if a collaborator is {@code null}
     */
    public SingleLogoutService(final String providerId, final SamlServerOptions options,
            final ExecutionServices services, final SamlErrorMapper errorMapper) {
        this.providerId = Assert.notBlank(providerId, "SAML Provider identifier must not be blank");
        this.options = Assert.notNull(options, "SAML Provider options must not be null");
        this.services = Assert.notNull(services, "SAML execution services must not be null");
        this.errorMapper = Assert.notNull(errorMapper, "SAML error mapper must not be null");
    }

    /**
     * Extracts and validates the registered service-provider SingleLogoutService URL.
     *
     * @param client registered service provider
     * @return trusted HTTPS URL or {@code null} when missing or invalid
     */
    private static String logoutDestination(final ConsumerMetadata client) {
        final JsonValue value = client.metadata().values().get(SINGLE_LOGOUT_SERVICE_URL);
        if (!(value instanceof JsonValue.StringValue string)) {
            return null;
        }
        try {
            final URI uri = new URI(string.value());
            return Protocol.HTTPS.name.equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null
                    && uri.getRawFragment() == null ? string.value() : null;
        } catch (URISyntaxException exception) {
            return null;
        }
    }

    /**
     * Creates a non-sensitive framework failure.
     *
     * @param code        shared Bus error code
     * @param description safe diagnostic description
     * @return immutable failure value
     */
    private static Outcome.Failure failure(final Errors code, final String description) {
        return new Outcome.Failure(code, description, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Creates a type-inferred completed stage.
     *
     * @param outcome completed outcome
     * @param <T>     success value type
     * @return completed asynchronous stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Ends the exactly identified framework session and returns a standard LogoutResponse.
     *
     * @param request standard SAML Logout Request validated at the Redirect Binding boundary
     * @param context immutable invocation context
     * @param timeout shared end-to-end time budget
     * @return stage containing a standard LogoutResponse or a closed failure when no trusted route exists
     */
    public CompletionStage<Outcome<LogoutResponse>> singleLogout(
            final LogoutRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(request, "SAML Logout Request must not be null");
        Assert.notNull(context, "SAML Single Logout context must not be null");
        Assert.notNull(timeout, "SAML Single Logout time budget must not be null");
        final Outcome<String> requester = requester(request, timeout);
        return switch (requester) {
            case Outcome.Succeeded<String> success -> LoadResult
                    .parse(
                            services.consumerLoader().load(success.value(), context, timeout),
                            loaded -> services.consumerParser().parse(success.value(), loaded))
                    .thenCompose(resolved -> switch (resolved) {
                        case Outcome.Succeeded<ConsumerMetadata> client -> end(
                                request,
                                client.value(),
                                context,
                                timeout);
                        case Outcome.Rejected<ConsumerMetadata> rejected -> completed(
                                Outcome.rejected(rejected.failure()));
                        case Outcome.Failed<ConsumerMetadata> failed -> completed(Outcome.failed(failed.failure()));
                    });
            case Outcome.Rejected<String> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<String> failed -> completed(Outcome.failed(failed.failure()));
        };
    }

    /**
     * Validates fields needed before trusting a registered response endpoint.
     *
     * @param request standard Logout Request
     * @param timeout operation time budget
     * @return requester entityID or a closed validation failure
     */
    private Outcome<String> requester(final LogoutRequest request, final Timeout.Budget timeout) {
        final var endpoint = options.singleLogoutServiceEndpoint().getOrNull();
        if (endpoint == null) {
            return Outcome.rejected(failure(ErrorCode._404, "SAML Single Logout is not configured"));
        }
        if (timeout.expired()) {
            return Outcome.failed(failure(ErrorCode._408, "SAML Logout Request has no remaining time budget"));
        }
        if (!endpoint.url().toString().equals(request.destination().getOrNull())) {
            return Outcome.rejected(failure(ErrorCode._400, "SAML Logout Request Destination is invalid"));
        }
        final Issuer issuer = request.issuer().getOrNull();
        if (issuer == null || request.sessionIndexes().size() != 1) {
            return Outcome.rejected(
                    failure(ErrorCode._400, "SAML Logout Request requires an Issuer and exactly one SessionIndex"));
        }
        final NameID issuerName = issuer.nameId();
        final String format = issuerName.format().getOrNull();
        if ((format != null && !ENTITY_NAME_ID.equals(format)) || issuerName.nameQualifier().isPresent()
                || issuerName.spNameQualifier().isPresent() || issuerName.spProvidedId().isPresent()) {
            return Outcome.rejected(failure(ErrorCode._400, "SAML Logout Request Issuer is invalid"));
        }
        final Instant now = timeout.clock().now();
        if (request.issueInstant().isBefore(now.minus(options.clockSkew()))
                || request.issueInstant().isAfter(now.plus(options.clockSkew())) || (request.notOnOrAfter().isPresent()
                        && !request.notOnOrAfter().getOrNull().plus(options.clockSkew()).isAfter(now))) {
            return Outcome.rejected(failure(ErrorCode._400, "SAML Logout Request is outside its validity interval"));
        }
        return Outcome.succeeded(issuerName.value());
    }

    /**
     * Resolves the registered response route and ends the requested session.
     *
     * @param request validated Logout Request
     * @param client  registered service provider
     * @param context invocation context
     * @param timeout operation budget
     * @return standard LogoutResponse stage
     */
    private CompletionStage<Outcome<LogoutResponse>> end(
            final LogoutRequest request,
            final ConsumerMetadata client,
            final Context context,
            final Timeout.Budget timeout) {
        final String issuer = request.issuer().getOrNull().nameId().value();
        final String destination = logoutDestination(client);
        if (!issuer.equals(client.id()) || destination == null) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    "SAML requester has no trusted SingleLogoutService response endpoint")));
        }
        return endSession(new Session.Key(request.sessionIndexes().get(0)), timeout, 1)
                .thenApply(outcome -> switch (outcome) {
                    case Outcome.Succeeded<Void> ignored -> Outcome.succeeded(success(request, destination, timeout));
                    case Outcome.Rejected<Void> rejected -> Outcome.succeeded(
                            errorMapper.logoutResponse(
                                    request,
                                    destination,
                                    UNKNOWN_PRINCIPAL,
                                    "Requested SAML session is not active",
                                    timeout.clock().now()));
                    case Outcome.Failed<Void> failed -> Outcome.failed(failed.failure());
                });
    }

    /**
     * Atomically transitions one active framework Session to {@link Session.State#ENDED}.
     *
     * @param sessionKey verified SAML SessionIndex represented by the root Session key
     * @param timeout    shared operation budget
     * @param attempt    one-based compare-and-replace attempt
     * @return stage containing success, unknown-session rejection, or Store failure
     */
    private CompletionStage<Outcome<Void>> endSession(
            final Session.Key sessionKey,
            final Timeout.Budget timeout,
            final int attempt) {
        if (timeout.expired()) {
            return completed(Outcome.failed(failure(ErrorCode._408, "SAML session ending exhausted its time budget")));
        }
        final String key = Builder.sha256Hex(providerId + '\0' + sessionKey.value());
        final CompletionStage<ExpiringValue<Session>> lookup;
        try {
            lookup = services.sessionCache().get(key);
        } catch (RuntimeException exception) {
            return completed(Outcome.failed(failure(ErrorCode._500, "SAML Session cache lookup failed")));
        }
        if (lookup == null) {
            return completed(Outcome.failed(failure(ErrorCode._500, "SAML Session cache returned no lookup stage")));
        }
        return lookup
                .handle(
                        (stored, thrown) -> thrown == null ? Outcome.succeeded(stored)
                                : Outcome.<ExpiringValue<Session>>failed(
                                        failure(ErrorCode._500, "SAML Session cache lookup failed")))
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<ExpiringValue<Session>> success -> replaceSession(
                            key,
                            sessionKey,
                            success.value(),
                            timeout,
                            attempt);
                    case Outcome.Rejected<ExpiringValue<Session>> rejected -> completed(
                            Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<ExpiringValue<Session>> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Validates a loaded Session and performs one atomic state replacement.
     *
     * @param key          isolated Session cache key
     * @param requestedKey verified requested Session key
     * @param stored       currently stored value, or {@code null}
     * @param timeout      shared operation budget
     * @param attempt      one-based replacement attempt
     * @return stage containing transition result
     */
    private CompletionStage<Outcome<Void>> replaceSession(
            final String key,
            final Session.Key requestedKey,
            final ExpiringValue<Session> stored,
            final Timeout.Budget timeout,
            final int attempt) {
        final Instant now = timeout.clock().now();
        if (stored == null || stored.value() == null || !stored.expiresAt().isAfter(now)) {
            return completed(Outcome.rejected(failure(ErrorCode._404, "Requested SAML session is not active")));
        }
        final Session session = stored.value();
        if (!requestedKey.equals(session.key())) {
            return completed(
                    Outcome.rejected(failure(ErrorCode._400, "Stored Session does not match the SAML SessionIndex")));
        }
        if (session.state() != Session.State.ACTIVE) {
            return completed(Outcome.rejected(failure(ErrorCode._404, "Requested SAML session is not active")));
        }
        final Session ended = new Session(session.key(), Session.State.ENDED, session.issuedAt(), session.expiresAt());
        final ExpiringValue<Session> update = new ExpiringValue<>(ended, stored.expiresAt());
        final long ttlMillis = Math.max(1L, Duration.between(now, stored.expiresAt()).toMillis());
        final CompletionStage<Boolean> replacement;
        try {
            replacement = services.sessionCache().replace(key, stored, update, ttlMillis);
        } catch (RuntimeException exception) {
            return completed(Outcome.failed(failure(ErrorCode._500, "SAML Session cache replacement failed")));
        }
        if (replacement == null) {
            return completed(
                    Outcome.failed(failure(ErrorCode._500, "SAML Session cache returned no replacement stage")));
        }
        return replacement
                .handle(
                        (replaced, thrown) -> thrown == null && replaced != null ? Outcome.succeeded(replaced)
                                : Outcome.<Boolean>failed(
                                        failure(ErrorCode._500, "SAML Session cache replacement failed")))
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<Boolean> success -> {
                        if (success.value())
                            yield completed(Outcome.succeeded(null));
                        if (attempt >= MAXIMUM_REPLACE_ATTEMPTS) {
                            yield completed(
                                    Outcome.failed(failure(ErrorCode._500, "SAML session changed concurrently")));
                        }
                        yield endSession(requestedKey, timeout, attempt + 1);
                    }
                    case Outcome.Rejected<Boolean> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<Boolean> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Creates a standard successful LogoutResponse.
     *
     * @param request     correlated Logout Request
     * @param destination registered service-provider endpoint
     * @param timeout     operation time source
     * @return unsigned semantic LogoutResponse for final binding encoding and signing
     */
    private LogoutResponse success(
            final LogoutRequest request,
            final String destination,
            final Timeout.Budget timeout) {
        return new LogoutResponse(Symbol.C_UNDERLINE + UUID.randomUUID().toString(true), Optional.of(request.id()),
                "2.0", timeout.clock().now(), Optional.of(destination), Optional.empty(),
                Optional.of(
                        new Issuer(new NameID(options.entityId(), Optional.empty(), Optional.empty(),
                                Optional.of(ENTITY_NAME_ID), Optional.empty()))),
                Optional.empty(), List.of(),
                new Status(new StatusCode(StatusCode.SUCCESS, Optional.empty()), Optional.empty(), Optional.empty()));
    }

}
