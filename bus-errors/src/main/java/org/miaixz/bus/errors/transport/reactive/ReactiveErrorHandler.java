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
package org.miaixz.bus.errors.transport.reactive;

import java.util.Objects;
import java.util.function.Supplier;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.web.ErrorResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;

import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.errors.*;
import org.miaixz.bus.errors.ErrorDecision.Basis;
import org.miaixz.bus.errors.internal.LegacyErrorSupport;
import org.miaixz.bus.errors.internal.ThrowableNormalizer;
import org.miaixz.bus.errors.policy.PolicySnapshot.Mode;
import org.miaixz.bus.errors.transport.ProtocolErrorBoundary;
import org.miaixz.bus.errors.transport.http.HttpErrorPolicy;
import org.miaixz.bus.errors.transport.http.HttpErrorResponse;
import org.miaixz.bus.errors.transport.http.RouteFailure;

import reactor.core.publisher.Mono;

/**
 * Handles verified route failures and the gateway's legacy HTTP response contract through one boundary.
 *
 * @author Kimi Liu
 */
public final class ReactiveErrorHandler implements WebExceptionHandler {

    /**
     * Exchange attribute carrying the gateway’s compiled route error policy.
     */
    private static final String POLICY = ReactiveErrorHandler.class.getName() + ".policy";

    /**
     * Exchange attribute storing terminal action and observation claims.
     */
    private static final String OPERATION = ReactiveErrorHandler.class.getName() + ".operation";

    /**
     * Compiled route policy for this listener, or null to preserve legacy handling.
     */
    private final HttpErrorPolicy policy;
    /**
     * Optional writer for the gateway’s existing response representation.
     */
    private final LegacyWriter legacyWriter;
    /**
     * Classifier and observer shared by this reactive listener.
     */
    private final ErrorBoundary boundary;

    /**
     * Creates a reactive listener adapter.
     *
     * @param policy       listener route policy, or {@code null} for legacy behavior
     * @param legacyWriter existing response format adapter, or {@code null} for route-only delegation
     */
    public ReactiveErrorHandler(HttpErrorPolicy policy, LegacyWriter legacyWriter) {
        this(policy, legacyWriter, ErrorBoundary.standard());
    }

    /**
     * Creates a reactive adapter with an application error boundary.
     *
     * @param policy       listener route policy, or {@code null} for legacy behavior
     * @param legacyWriter existing format adapter, or {@code null} for route-only delegation
     * @param boundary     shared classifier and observer
     */
    public ReactiveErrorHandler(HttpErrorPolicy policy, LegacyWriter legacyWriter, ErrorBoundary boundary) {
        this.policy = policy;
        this.legacyWriter = legacyWriter;
        this.boundary = Objects.requireNonNull(boundary, "boundary");
    }

    /**
     * Associates the listener's compiled policy with the active exchange before routing.
     *
     * @param exchange active server exchange
     * @param policy   selected HTTP policy
     */
    public static void bindPolicy(ServerWebExchange exchange, HttpErrorPolicy policy) {
        exchange.getAttributes().put(POLICY, policy);
    }

    /**
     * Checks whether a route owner should collect additional facts for an explicitly selected type.
     *
     * @param exchange active server exchange
     * @param type     semantic failure being considered
     * @return whether the gateway selected the native HTTP contract
     */
    public static boolean usesNativeRule(ServerWebExchange exchange, ErrorType type) {
        HttpErrorPolicy policy = exchange.getAttribute(POLICY);
        return policy != null && policy
                .semanticMode(exchange.getRequest().getPath().pathWithinApplication(), type) == Mode.NATIVE_HTTP;
    }

    /**
     * Rejects an explicitly incompatible request media type at an endpoint that consumes JSON. Requests without
     * Content-Type retain the endpoint's existing behavior.
     *
     * @param exchange active server exchange
     * @throws RouteFailure if the enabled rule rejects the declared media type
     */
    public static void verifyJsonContentType(ServerWebExchange exchange) {
        if (!usesNativeRule(exchange, ErrorType.UNSUPPORTED_MEDIA_TYPE)) {
            return;
        }
        try {
            var contentType = exchange.getRequest().getHeaders().getContentType();
            if (contentType == null || org.miaixz.bus.core.net.MediaType.isJson(contentType.toString())) {
                return;
            }
        } catch (InvalidMediaTypeException ignored) {
            // A malformed Content-Type is also incompatible with this JSON consumer.
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        throw new RouteFailure(ErrorCode._100803, HttpStatus.UNSUPPORTED_MEDIA_TYPE, headers);
    }

    /**
     * Completes a gateway admission rejection with the existing empty 503 and retry hint.
     *
     * @param exchange active server exchange
     * @param boundary shared classifier and observer
     * @return response-write completion
     */
    public static Mono<Void> rejectOverload(ServerWebExchange exchange, ErrorBoundary boundary) {
        return Mono.defer(() -> {
            var context = context(exchange, exchange.getAttribute(POLICY));
            var operation = operation(exchange, boundary);
            var decision = operation.decide(
                    context,
                    new FailureFacts(FailureFacts.Source.LOCAL, ErrorEngine.descriptor(ErrorType.GATEWAY_OVERLOADED),
                            HttpStatus.SERVICE_UNAVAILABLE.value()),
                    null);
            if (decision.action() != ErrorDecision.Action.RENDER || !operation.claimTermination()) {
                return Mono.empty();
            }
            return writeResponse(exchange, operation, context, decision, null, () -> {
                var response = exchange.getResponse();
                response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                response.getHeaders().set(HttpHeaders.RETRY_AFTER, "1");
                return response.setComplete();
            });
        });
    }

    /**
     * Captures the gateway transmission phase and selected policy version.
     *
     * @param exchange active exchange
     * @param policy   compiled listener policy, or null
     * @return phase-aware error context
     */
    private static ErrorContext context(ServerWebExchange exchange, HttpErrorPolicy policy) {
        return new ErrorContext("vortex", "http",
                exchange.getResponse().isCommitted() ? ErrorContext.Phase.COMMITTED : ErrorContext.Phase.OPEN,
                policy == null ? 0 : policy.version());
    }

    /**
     * Finds or creates the error operation shared by all handlers of this exchange.
     *
     * @param exchange active exchange
     * @param boundary classifier and observer used for a new operation
     * @return operation-local failure state
     */
    private static ErrorBoundary.Operation operation(ServerWebExchange exchange, ErrorBoundary boundary) {
        return (ErrorBoundary.Operation) exchange.getAttributes().computeIfAbsent(OPERATION, key -> boundary.open());
    }

    /**
     * Handles one gateway failure while retaining legacy negotiation and wire types.
     *
     * @param exchange active server exchange
     * @param failure  original failure
     * @return completion of the selected response or propagation of the failure
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable failure) {
        return Mono.defer(() -> handleFailure(exchange, failure));
    }

    /**
     * Selects a terminal signal, native HTTP response or legacy response for one gateway failure.
     *
     * @param exchange active gateway exchange
     * @param failure  original failure to process
     * @return response completion or the original error signal
     */
    private Mono<Void> handleFailure(ServerWebExchange exchange, Throwable failure) {
        var response = exchange.getResponse();
        if (response.isCommitted() && this.legacyWriter == null) {
            return Mono.error(failure);
        }
        var context = context(exchange, this.policy);
        var operation = operation(exchange, this.boundary);
        Throwable primary = ThrowableNormalizer.primary(failure);
        RouteFailure route = primary instanceof RouteFailure verified ? verified : null;
        ErrorResponse error = primary instanceof ErrorResponse candidate && candidate.getStatusCode().isError()
                ? candidate
                : null;
        LegacyErrorSupport legacy = this.legacyWriter == null || route != null || error != null ? null
                : LegacyErrorSupport.from(failure);
        HttpStatusCode status = route != null ? route.status()
                : error != null ? error.getStatusCode() : legacy == null ? null : legacy.status();
        var facts = new FailureFacts(FailureFacts.Source.LOCAL, null, status == null ? 0 : status.value());
        var decision = operation.decide(context, facts, failure);
        if (decision.action() == ErrorDecision.Action.PROPAGATE) {
            if (decision.basis() != Basis.FATAL_SIGNAL) {
                operation.observe(context, decision, true, failure, exchange.getRequest().getId());
            }
            return Mono.error(failure);
        }
        if (decision.action() == ErrorDecision.Action.TERMINATE) {
            boolean terminal = ProtocolErrorBoundary
                    .claim(operation, context, decision, failure, exchange.getRequest().getId());
            // When enabled, hand a committed stream failure to the server so it can terminate the connection.
            return terminal && this.policy != null && this.policy.hasSemanticRules() ? Mono.error(failure)
                    : Mono.empty();
        }
        if (this.policy != null && status != null
                && this.policy.select(
                        exchange.getRequest().getPath().pathWithinApplication(),
                        decision,
                        failure instanceof ErrorResponse) == Mode.NATIVE_HTTP) {
            if (!operation.claimTermination()) {
                return Mono.empty();
            }
            return writeResponse(exchange, operation, context, decision, failure, () -> {
                HttpHeaders headers = new HttpHeaders();
                HttpHeaders verifiedHeaders = route != null ? route.headers()
                        : error != null ? error.getHeaders() : HttpHeaders.EMPTY;
                verifiedHeaders.forEach(headers::addAll);
                if (status.value() == 503 && !headers.containsHeader(HttpHeaders.RETRY_AFTER)) {
                    headers.set(HttpHeaders.RETRY_AFTER, "1");
                }
                var selected = HttpErrorResponse.empty(status, headers, this.policy.cacheControl());
                selected.headers().forEach((name, values) -> {
                    if (HttpHeaders.CACHE_CONTROL.equalsIgnoreCase(name) && !this.policy.cacheControl().isBlank()) {
                        response.getHeaders().put(name, values);
                    } else {
                        response.getHeaders().addAll(name, values);
                    }
                });
                response.setStatusCode(selected.status());
                return response.setComplete();
            });
        }
        if (this.legacyWriter == null) {
            return Mono.error(failure);
        }
        if (!operation.claimTermination()) {
            return Mono.empty();
        }
        var selected = legacy == null ? LegacyErrorSupport.from(failure) : legacy;
        return writeResponse(exchange, operation, context, decision, failure, () -> {
            response.setStatusCode(selected.status());
            if (selected.status() == HttpStatus.SERVICE_UNAVAILABLE) {
                response.getHeaders().set(HttpHeaders.RETRY_AFTER, "1");
            }
            return this.legacyWriter.write(exchange, selected.code(), selected.description());
        });
    }

    /**
     * Reports a reactive response write error separately while preserving the original signal and disposition.
     *
     * @param exchange active server exchange
     * @param operation operation that owns the response
     * @param context context of the original failure
     * @param decision selected original error decision
     * @param failure original error, or null for a structured rejection
     * @param writer response preparation and write
     * @return original write completion or failure
     */
    private static Mono<Void> writeResponse(ServerWebExchange exchange, ErrorBoundary.Operation operation,
            ErrorContext context, ErrorDecision decision, Throwable failure, Supplier<Mono<Void>> writer) {
        String requestId = exchange.getRequest().getId();
        return Mono.defer(writer)
                .doOnError(secondary -> ErrorBoundary.diagnose(context, "error-response", secondary, requestId))
                .doFinally(signal -> operation.observe(context, decision, true, failure, requestId, signal.name()));
    }

    /**
     * Writes the existing gateway DTO in the negotiated format.
     *
     * @author Kimi Liu
     */
    @FunctionalInterface
    public interface LegacyWriter {

        /**
         * Writes the previously selected legacy application code and description.
         *
         * @param exchange    active server exchange
         * @param code        legacy application code
         * @param description legacy description, possibly {@code null}
         * @return response-write completion
         */
        Mono<Void> write(ServerWebExchange exchange, String code, String description);

    }

}
