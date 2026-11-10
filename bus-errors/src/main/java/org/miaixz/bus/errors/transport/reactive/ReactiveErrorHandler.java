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

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.ErrorResponse;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
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
import org.miaixz.bus.errors.transport.http.PublicHttpError;
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
     * Checks the policy before a protocol adapter validates facts for a prospective HTTP error.
     *
     * @param exchange active server exchange
     * @param status   status the adapter is able to verify
     * @return whether the current path selects the native contract for this status
     */
    public static boolean usesNativeStatus(ServerWebExchange exchange, HttpStatus status) {
        HttpErrorPolicy policy = exchange.getAttribute(POLICY);
        if (policy == null) {
            return false;
        }
        return policy.isStatusOnly()
                ? policy.matches(exchange.getRequest().getPath().pathWithinApplication(), status.value())
                : usesNativeRule(exchange, ErrorEngine.typeForStatus(status.value()));
    }

    /**
     * Requests visible route method facts when a verified lookup must distinguish 404 from 405.
     *
     * @param exchange active server exchange
     * @return whether the adapter should look for a visible route with another HTTP method
     */
    public static boolean needsRouteMethodFacts(ServerWebExchange exchange) {
        HttpErrorPolicy policy = exchange.getAttribute(POLICY);
        return usesNativeStatus(exchange, HttpStatus.METHOD_NOT_ALLOWED) || (policy != null && policy.isStatusOnly()
                && policy.matches(
                        exchange.getRequest().getPath().pathWithinApplication(),
                        HttpStatus.NOT_FOUND.value()));
    }

    /**
     * Rejects an explicitly incompatible request media type at an endpoint that consumes JSON. Requests without
     * Content-Type retain the endpoint's existing behavior.
     *
     * @param exchange active server exchange
     * @throws RouteFailure if the enabled rule rejects the declared media type
     */
    public static void verifyJsonContentType(ServerWebExchange exchange) {
        if (!usesNativeStatus(exchange, HttpStatus.UNSUPPORTED_MEDIA_TYPE)) {
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
     * Maps an upstream HTTP error to its status and approved protocol hints. WebClient discards the unconsumed body.
     *
     * @param response upstream HTTP error response
     * @return a failure carrying the verified status and allowed hints
     */
    public static Mono<? extends Throwable> upstreamFailure(ClientResponse response) {
        return Mono.just(new UpstreamStatus(response.statusCode(), response.headers().asHttpHeaders()));
    }

    /**
     * Stores only verified status facts from an upstream error.
     */
    private static final class UpstreamStatus extends ResponseStatusException {

        @java.io.Serial
        private static final long serialVersionUID = 1L;
        /** Common media types that can be advertised without reflecting an upstream token. */
        private static final Set<String> PUBLIC_MEDIA = Set.of(
                "application/json",
                "application/xml",
                "text/plain",
                "application/x-www-form-urlencoded",
                "multipart/form-data",
                "text/event-stream",
                "*/*");
        /** Detached protocol hints with catalogued or numeric values. */
        private final transient HttpHeaders headers;

        /**
         * Creates an upstream failure without retaining arbitrary remote fields.
         *
         * @param status verified response status
         * @param remote remote response headers
         */
        private UpstreamStatus(HttpStatusCode status, HttpHeaders remote) {
            super(status);
            var safe = new HttpHeaders();
            if (status.value() == 405) {
                for (String value : remote.getOrEmpty(HttpHeaders.ALLOW)) {
                    Arrays.stream(value.split(",")).map(String::trim).filter(
                            method -> Arrays.stream(HttpMethod.values()).anyMatch(known -> known.matches(method)))
                            .forEach(method -> safe.add(HttpHeaders.ALLOW, method));
                }
            }
            if (status.value() == 415 || status.value() == 406) {
                for (String value : remote.getOrEmpty(HttpHeaders.ACCEPT)) {
                    Arrays.stream(value.split(",")).map(String::trim).filter(PUBLIC_MEDIA::contains)
                            .forEach(media -> safe.add(HttpHeaders.ACCEPT, media));
                }
            }
            String retry = remote.getFirst(HttpHeaders.RETRY_AFTER);
            if ((status.value() == 429 || status.value() == 503) && retry != null && retry.matches("[0-9]{1,10}")) {
                safe.set(HttpHeaders.RETRY_AFTER, retry);
            }
            this.headers = HttpHeaders.readOnlyHttpHeaders(safe);
        }

        /** Returns only the approved upstream protocol hints. */
        @Override
        public HttpHeaders getHeaders() {
            return this.headers == null ? HttpHeaders.EMPTY : this.headers;
        }
    }

    /**
     * Renders an explicit protocol failure with a code-owned public definition.
     *
     * @param status   verified error status
     * @param type     failure classification
     * @param failure  diagnostic cause, or null for a deliberate rejection
     * @param error    registered or code-owned public error
     * @param renderer serializer for the existing protocol contract
     * @return response that claims the shared boundary only when written
     */
    public static Mono<ServerResponse> render(
            HttpStatusCode status,
            ErrorType type,
            Throwable failure,
            PublicHttpError error,
            Function<PublicHttpError, Mono<ServerResponse>> renderer) {
        return Mono.defer(() -> {
            if (failure != null) {
                ThrowableNormalizer.rethrowFatal(failure);
            }
            return renderer.apply(error);
        }).map(response -> new ProtocolResponse(response, status, type, failure));
    }

    /**
     * Renders an in-band error as a separate protocol message. The HTTP exchange can stay open and carry other
     * messages, so this operation neither claims its termination nor changes its committed status.
     *
     * @param exchange active gateway exchange for correlation
     * @param type     shared failure classification
     * @param error    code-owned public error definition
     * @param renderer serializer for one protocol frame
     * @param <T>      encoded frame type
     * @return safe protocol frame
     */
    public static <T> T renderFrame(
            ServerWebExchange exchange,
            ErrorType type,
            PublicHttpError error,
            Function<PublicHttpError, T> renderer) {
        HttpErrorPolicy policy = exchange.getAttribute(POLICY);
        var context = new ErrorContext("vortex", "sse-frame", ErrorContext.Phase.OPEN,
                policy == null ? 0 : policy.version());
        var operation = ErrorBoundary.standard().open();
        var decision = operation
                .decide(context, new FailureFacts(FailureFacts.Source.LOCAL, ErrorEngine.descriptor(type), 0), null);
        return operation.complete(context, decision, null, exchange.getRequest().getId(), () -> renderer.apply(error));
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
     * Reports a reactive response write error separately while preserving the original signal and disposition.
     *
     * @param exchange  active server exchange
     * @param operation operation that owns the response
     * @param context   context of the original failure
     * @param decision  selected original error decision
     * @param failure   original error, or null for a structured rejection
     * @param writer    response preparation and write
     * @return original write completion or failure
     */
    private static Mono<Void> writeResponse(
            ServerWebExchange exchange,
            ErrorBoundary.Operation operation,
            ErrorContext context,
            ErrorDecision decision,
            Throwable failure,
            Supplier<Mono<Void>> writer) {
        String requestId = exchange.getRequest().getId();
        return Mono.defer(() -> {
            var response = exchange.getResponse();
            response.getHeaders().clear();
            response.getCookies().clear();
            ReactiveResponseGuard.approve(response);
            return writer.get();
        }).onErrorResume(secondary -> {
            ErrorBoundary.diagnose(context, "error-response", secondary, requestId);
            return exchange.getResponse().isCommitted() ? Mono.error(secondary)
                    : ReactiveResponseGuard.completeSafely(exchange.getResponse(), HttpStatus.INTERNAL_SERVER_ERROR);
        }).doFinally(signal -> operation.observe(context, decision, true, failure, requestId, signal.name()));
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
            return terminal ? Mono.error(failure) : Mono.empty();
        }
        if (this.policy != null && status != null
                && this.policy.select(
                        exchange.getRequest().getPath().pathWithinApplication(),
                        decision,
                        this.policy.isStatusOnly() ? route != null || error != null
                                : failure instanceof ErrorResponse) == Mode.NATIVE_HTTP) {
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
        var outputStatus = error == null ? selected.status() : error.getStatusCode();
        return writeResponse(exchange, operation, context, decision, failure, () -> {
            response.setStatusCode(outputStatus);
            if (error != null) {
                HttpErrorResponse.empty(outputStatus, error.getHeaders(), "").headers()
                        .forEach((name, values) -> response.getHeaders().put(name, values));
            }
            if (outputStatus.value() == 503 && !response.getHeaders().containsHeader(HttpHeaders.RETRY_AFTER)) {
                response.getHeaders().set(HttpHeaders.RETRY_AFTER, "1");
            }
            if (error != null && !(primary instanceof ResponseStatusException)) {
                var publicError = PublicHttpError.forStatus(outputStatus);
                return this.legacyWriter.write(exchange, publicError.code(), publicError.description());
            }
            return this.legacyWriter.write(exchange, selected.code(), selected.description());
        });
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

    /**
     * Associates a protocol error with the actual write and its terminal observation.
     *
     * @param delegate formatted protocol response
     * @param status   verified status
     * @param type     semantic classification supplied by the protocol
     * @param failure  diagnostic cause
     */
    private record ProtocolResponse(ServerResponse delegate, HttpStatusCode status, ErrorType type, Throwable failure)
            implements ServerResponse {

        /**
         * Returns the formatted response status.
         */
        @Override
        public HttpStatusCode statusCode() {
            return this.delegate.statusCode();
        }

        /**
         * Returns the formatted response headers.
         */
        @Override
        public HttpHeaders headers() {
            return this.delegate.headers();
        }

        /**
         * Returns the formatted response cookies.
         */
        @Override
        public MultiValueMap<String, ResponseCookie> cookies() {
            return this.delegate.cookies();
        }

        /**
         * Writes the response after claiming termination for this exchange.
         *
         * @param exchange      active exchange
         * @param writerContext response codec context
         * @return response completion
         */
        @Override
        public Mono<Void> writeTo(ServerWebExchange exchange, ServerResponse.Context writerContext) {
            var context = ReactiveErrorHandler.context(exchange, exchange.getAttribute(POLICY));
            var operation = ReactiveErrorHandler.operation(exchange, ErrorBoundary.standard());
            var decision = operation.decide(
                    context,
                    new FailureFacts(FailureFacts.Source.LOCAL, ErrorEngine.descriptor(this.type),
                            this.status.isError() ? this.status.value() : 0),
                    this.failure);
            if (decision.action() == ErrorDecision.Action.PROPAGATE && this.failure != null) {
                return Mono.error(this.failure);
            }
            if (decision.action() != ErrorDecision.Action.RENDER || !operation.claimTermination()) {
                return Mono.empty();
            }
            return writeResponse(
                    exchange,
                    operation,
                    context,
                    decision,
                    this.failure,
                    () -> this.delegate.writeTo(exchange, writerContext));
        }
    }

}
