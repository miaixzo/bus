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
package org.miaixz.bus.vortex.handler;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.errors.ErrorBoundary;
import org.miaixz.bus.errors.transport.http.HttpErrorPolicy;
import org.miaixz.bus.errors.transport.reactive.ReactiveErrorHandler;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Formats;

import reactor.core.publisher.Mono;

/**
 * Vortex compatibility facade retaining the negotiated legacy response DTO and its serializer.
 *
 * @author Kimi Liu
 */
public class ErrorsHandler implements WebExceptionHandler {

    /**
     * Unified reactive handler using the gateway’s legacy formatter when required.
     */
    private final ReactiveErrorHandler delegate;

    /**
     * Creates the original gateway handler without a listener route policy.
     */
    public ErrorsHandler() {
        this(null);
    }

    /**
     * Creates a unified handler for the Vortex listener.
     *
     * @param policy selected route policy, or {@code null} for the original gateway behavior
     */
    public ErrorsHandler(HttpErrorPolicy policy) {
        this(policy, ErrorBoundary.standard());
    }

    /**
     * Creates a gateway handler with the application's shared classifier and observer.
     *
     * @param policy   optional gateway route policy
     * @param boundary shared error boundary
     */
    public ErrorsHandler(HttpErrorPolicy policy, ErrorBoundary boundary) {
        this.delegate = new ReactiveErrorHandler(policy, ErrorsHandler::writeLegacy, boundary);
    }

    /**
     * Handles the failure through the shared reactive boundary.
     *
     * @param exchange current exchange
     * @param failure  original failure
     * @return response-write completion
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable failure) {
        return this.delegate.handle(exchange, failure);
    }

    /**
     * Serializes the original gateway DTO using the negotiated media type and byte length.
     *
     * @param exchange exchange whose response is still writable
     * @param code legacy application error code
     * @param description legacy description, possibly null
     * @return completion of the legacy response write
     */
    private static Mono<Void> writeLegacy(ServerWebExchange exchange, String code, String description) {
        var response = exchange.getResponse();
        Context context = exchange.getAttribute(Context.$);
        Formats format = context == null ? Formats.JSON : context.getFormat();
        var body = format.getProvider().serialize(Message.builder().code(code).desc(description).build()).map(value -> {
            byte[] bytes = value instanceof byte[] binary ? binary : String.valueOf(value).getBytes(Charset.UTF_8);
            response.getHeaders().setContentType(format.getMediaType());
            response.getHeaders().setContentLength(bytes.length);
            return response.bufferFactory().wrap(bytes);
        });
        return response.writeWith(body);
    }

    /**
     * Represents a standardized message structure for API responses, typically used for error messages.
     *
     * @author Kimi Liu
     */
    @Getter
    @Setter
    @SuperBuilder
    public static class Message {

        /**
         * The response code, indicating the specific error or status.
         */
        private String code;

        /**
         * The descriptive error message or status message.
         */
        private String desc;

        /**
         * Creates an empty response message.
         */
        public Message() {
            // No initialization required.
        }

    }

}
