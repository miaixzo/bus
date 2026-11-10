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

import java.time.Duration;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;

import org.miaixz.bus.errors.ErrorBoundary;
import org.miaixz.bus.errors.ErrorContext;
import org.miaixz.bus.errors.FailureFacts;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Prevents an unapproved gateway HTTP error from writing its original body or headers.
 *
 * @author Kimi Liu
 */
public final class ReactiveResponseGuard extends ServerHttpResponseDecorator {

    /**
     * Correlation ID assigned by the reactive server.
     */
    private final String requestId;
    /**
     * Error policy used for a write that bypassed the normal handlers.
     */
    private final ErrorBoundary boundary;
    /**
     * Whether the common boundary owns the current error response.
     */
    private boolean approved;

    /**
     * Wraps one server response for the lifetime of its HTTP exchange.
     *
     * @param response  server-owned response
     * @param requestId server-generated request ID
     * @param boundary  shared error boundary
     */
    private ReactiveResponseGuard(ServerHttpResponse response, String requestId, ErrorBoundary boundary) {
        super(response);
        this.requestId = requestId;
        this.boundary = boundary;
    }

    /**
     * Installs the guard outside routing, filters and exception handling.
     *
     * @param handler  gateway HTTP handler
     * @param boundary shared error boundary
     * @return guarded HTTP handler
     */
    public static HttpHandler guard(HttpHandler handler, ErrorBoundary boundary) {
        return (request, response) -> handler
                .handle(request, new ReactiveResponseGuard(response, request.getId(), boundary));
    }

    /**
     * Permits the selected public error response to use its protocol writer.
     *
     * @param response exchange response, potentially wrapped by another filter
     */
    static void approve(ServerHttpResponse response) {
        while (response instanceof ServerHttpResponseDecorator decorator) {
            if (decorator instanceof ReactiveResponseGuard guard) {
                guard.approved = true;
                return;
            }
            response = decorator.getDelegate();
        }
    }

    /**
     * Completes an empty error without carrying diagnostic response headers or serializer state.
     *
     * @param response uncommitted server response
     * @param status   public HTTP status to preserve
     * @return completion signal
     */
    static Mono<Void> completeSafely(ServerHttpResponse response, HttpStatusCode status) {
        response.getHeaders().clear();
        response.getCookies().clear();
        response.setStatusCode(status);
        response.getHeaders().setCacheControl("no-store");
        return response.setComplete();
    }

    /**
     * Releases an already-produced buffer and cancels the rejected body without draining an arbitrary stream.
     *
     * @param body rejected body publisher
     * @return completion after release and cancellation
     */
    private static Mono<Void> discard(Flux<? extends DataBuffer> body) {
        return body.take(1).doOnNext(DataBufferUtils::release).doOnDiscard(DataBuffer.class, DataBufferUtils::release)
                .then().timeout(Duration.ofSeconds(1), Mono.empty()).onErrorResume(failure -> Mono.empty());
    }

    /**
     * Allows approved error output and successful bodies through without buffering.
     *
     * @param body body publisher from the response writer
     * @return response completion
     */
    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        return Mono.defer(
                () -> blocked() ? discard(Flux.from(body)).then(Mono.defer(this::reject)) : super.writeWith(body));
    }

    /**
     * Allows approved streams and prevents a flush from committing an unapproved HTTP error.
     *
     * @param body publishers grouped by flush boundary
     * @return response completion
     */
    @Override
    public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
        return Mono.defer(
                () -> blocked() ? discard(Flux.from(body).concatMap(Flux::from, 1)).then(Mono.defer(this::reject))
                        : super.writeAndFlushWith(body));
    }

    /**
     * Completes an empty response after validating ownership of its status.
     *
     * @return response completion
     */
    @Override
    public Mono<Void> setComplete() {
        return Mono.defer(() -> blocked() ? reject() : super.setComplete());
    }

    /**
     * Determines whether the first public write would bypass the error boundary.
     *
     * @return whether a fallback is required
     */
    private boolean blocked() {
        var status = getStatusCode();
        return !this.approved && !isCommitted() && status != null && status.isError();
    }

    /**
     * Reports the unauthorized write and publishes only its verified status.
     *
     * @return completion of the empty safe response
     */
    private Mono<Void> reject() {
        var status = getStatusCode();
        var context = new ErrorContext("vortex", "unapproved-error-response", ErrorContext.Phase.OPEN);
        var operation = this.boundary.open();
        var decision = operation
                .decide(context, new FailureFacts(FailureFacts.Source.LOCAL, null, status.value()), null);
        return completeSafely(getDelegate(), status)
                .doFinally(signal -> operation.observe(context, decision, true, null, this.requestId, signal.name()));
    }

}
