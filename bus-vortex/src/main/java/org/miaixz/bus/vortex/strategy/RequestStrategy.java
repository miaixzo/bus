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
package org.miaixz.bus.vortex.strategy;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import org.miaixz.bus.core.Order;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Holder;
import org.miaixz.bus.vortex.Octets;
import org.miaixz.bus.vortex.magic.ErrorCode;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Basic request strategy for routes without protocol-specific request parsing.
 * <p>
 * This class initializes request metadata and provides protected parsing operations for protocol request strategies.
 * Protocol-specific classes decide whether to parse and cache the body or pass the request through untouched.
 *
 * @author Kimi Liu
 */
@org.springframework.core.annotation.Order(Order.FIRST)
public class RequestStrategy extends AbstractStrategy {

    /**
     * Creates a request strategy.
     */
    public RequestStrategy() {
        // No initialization required.
    }

    /**
     * Initializes common request metadata and continues without parsing the body.
     *
     * @param exchange The current server exchange.
     * @param chain    The next strategy in the chain.
     * @return A {@code Mono<Void>} that signals the completion of this strategy.
     */
    @Override
    public Mono<Void> apply(ServerWebExchange exchange, Chain chain) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);
            return chain.apply(prepare(exchange, context, false));
        });
    }

    /**
     * Initializes context request metadata and optionally supplies a default content type.
     *
     * @param exchange           current exchange
     * @param context            request context
     * @param defaultContentType whether missing content type should be defaulted for body parsing
     * @return prepared exchange
     */
    protected ServerWebExchange prepare(ServerWebExchange exchange, Context context, boolean defaultContentType) {
        context.setX_request_ipv4(this.getClientIp(exchange.getRequest()));
        context.setQuery(exchange.getRequest().getQueryParams().toSingleValueMap());

        ServerWebExchange mutate = defaultContentType ? setContentType(exchange) : exchange;
        ServerHttpRequest request = mutate.getRequest();
        Logger.debug(
                true,
                "Vortex",
                "Request headers captured: strategy=request, clientIp={}, path={}, headerCount={}, contentType={}",
                context.getX_request_ip(),
                request.getURI().getPath(),
                request.getHeaders().size(),
                request.getHeaders().getContentType());
        Logger.debug(
                true,
                "Vortex",
                "Request header snapshot: strategy=request, clientIp={}, path={}",
                context.getX_request_ip(),
                request.getURI().getPath());
        Logger.debug(
                true,
                "Vortex",
                "Request headers: strategy=request, clientIp={}, headers={}",
                context.getX_request_ip(),
                request.getHeaders().toSingleValueMap());
        return mutate;
    }

    /**
     * Parses a REST-like request into {@link Context#getParameters()}.
     *
     * @param exchange current exchange
     * @param chain    remaining chain
     * @param context  request context
     * @return parsing completion signal
     */
    protected Mono<Void> parse(ServerWebExchange exchange, Chain chain, Context context) {
        ServerHttpRequest request = exchange.getRequest();
        if (context.getHttpMethod() == Http.Method.GET) {
            return handleGetRequest(exchange, chain, context);
        }
        MediaType contentType = request.getHeaders().getContentType();
        if (contentType == null) {
            return handleFormRequest(exchange, chain, context);
        } else if (org.miaixz.bus.core.net.MediaType.isJson(contentType.toString())) {
            return handleJsonRequest(exchange, chain, context);
        } else if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(contentType)) {
            long contentLength = request.getHeaders().getContentLength();
            if (contentLength > Holder.getMaxMultipartRequestSize()) {
                throw new ValidateException(ErrorCode._100530);
            }
            return handleMultipartRequest(exchange, chain, context);
        } else if (MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(contentType)) {
            return handleFormRequest(exchange, chain, context);
        }
        return handleFormRequest(exchange, chain, context);
    }

    /**
     * Handles GET requests by extracting parameters directly from the URL query.
     *
     * @param exchange The current server exchange.
     * @param chain    The next strategy in the chain.
     * @param context  The request context to be populated.
     * @return A {@code Mono<Void>} that signals the completion of processing.
     */
    private Mono<Void> handleGetRequest(ServerWebExchange exchange, Chain chain, Context context) {
        return Mono.fromRunnable(() -> {
            context.getParameters().putAll(context.getQuery());
            Logger.debug(
                    true,
                    "Vortex",
                    "GET request parameter snapshot: strategy=request, clientIp={}, path={}",
                    context.getX_request_ip(),
                    exchange.getRequest().getURI().getPath());
            Logger.debug(
                    true,
                    "Vortex",
                    "Request parameters: strategy=request, clientIp={}, parameters={}",
                    context.getX_request_ip(),
                    context.getParameters());
            Logger.info(
                    true,
                    "Vortex",
                    "GET request parameters processed: strategy=request, clientIp={}, path={}, parameterCount={}",
                    context.getX_request_ip(),
                    exchange.getRequest().getURI().getPath(),
                    context.getParameters().size());
        }).then(chain.apply(exchange)).doFinally(
                signalType -> Logger.info(
                        false,
                        "Vortex",
                        "Request processing completed: strategy=request, clientIp={}, path={}, executionTimeMs={}",
                        context.getX_request_ip(),
                        exchange.getRequest().getURI().getPath(),
                        (System.currentTimeMillis() - context.getTimestamp())));
    }

    /**
     * Handles a bounded {@code application/json} request after acquiring its exact logical-byte budget.
     * <p>
     * Buffered request modes require Content-Length. The resulting lease remains active through downstream request
     * replay and the complete response lifecycle.
     *
     * @param exchange The current server exchange.
     * @param chain    The next strategy in the chain.
     * @param context  The request context.
     * @return A {@code Mono<Void>} that signals the completion of processing.
     */
    private Mono<Void> handleJsonRequest(ServerWebExchange exchange, Chain chain, Context context) {
        return Octets
                .readForParsing(
                        exchange.getRequest().getBody(),
                        Math.toIntExact(Holder.get().getMaxBufferedRequestSize()),
                        Holder.requestBufferBudget(),
                        exchange.getRequest().getHeaders().getContentLength())
                .onErrorMap(DataBufferLimitException.class, error -> new ValidateException(ErrorCode._100530))
                .flatMap(body -> processJsonData(exchange, chain, context, body).doFinally(signal -> body.close()));
    }

    /**
     * Performs the actual parsing of a JSON request body.
     * <p>
     * The body has already been copied into one exact array. Object properties are added to the {@link Context}; array
     * roots are validated and marked for original-body forwarding. A request decorator then exposes the same bytes to
     * the downstream client without another aggregation.
     *
     * @param exchange The current server exchange.
     * @param chain    The next strategy in the chain.
     * @param context  The request context to be populated.
     * @param body     bounded request body and its logical-byte lease
     * @return A {@code Mono<Void>} that signals the completion of processing.
     */
    private Mono<Void> processJsonData(
            ServerWebExchange exchange,
            Chain chain,
            Context context,
            Octets.BufferedBody body) {

        return Mono.fromCallable(() -> {
            byte[] bytes = body.bytes();
            String jsonBody = new String(bytes, Charset.UTF_8);
            boolean jsonArray = isJsonArray(jsonBody);
            try {
                if (jsonArray) {
                    JsonKit.toList(jsonBody);
                    context.getParameters().putAll(context.getQuery());
                    exchange.getAttributes().put(Context.JSON_ARRAY_BODY_ATTRIBUTE, Boolean.TRUE);
                } else {
                    Map<String, Object> jsonMap = JsonKit.toMap(jsonBody);
                    context.getParameters().putAll(jsonMap);
                }
            } catch (ValidateException exception) {
                throw exception;
            } catch (RuntimeException exception) {
                throw new ValidateException(ErrorCode._100302.getKey(), ErrorCode._100302.getValue(), exception);
            }
            Logger.debug(
                    true,
                    "Vortex",
                    "JSON request parameter snapshot: strategy=request, clientIp={}, path={}, bytes={}",
                    context.getX_request_ip(),
                    exchange.getRequest().getURI().getPath(),
                    bytes.length);
            Logger.debug(
                    true,
                    "Vortex",
                    "Request parameters: strategy=request, clientIp={}, parameters={}",
                    context.getX_request_ip(),
                    context.getParameters());

            ServerHttpRequest newRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {

                /**
                 * Returns the cached request body as a Flux of DataBuffer.
                 * <p>
                 * The wrapped array is retained by the emitted buffer. This container drops its own byte references
                 * after publication, while the outer request lifecycle retains the logical-byte lease.
                 *
                 * @return A Flux emitting a single DataBuffer containing the cached body
                 */
                @Override
                public Flux<DataBuffer> getBody() {
                    return Flux.defer(() -> Mono.just(exchange.getResponse().bufferFactory().wrap(body.bytes())))
                            .doFinally(signal -> body.discardBytes());
                }
            };

            Logger.info(
                    true,
                    "Vortex",
                    "JSON request processed: strategy=request, clientIp={}, path={}, parameterCount={}",
                    context.getX_request_ip(),
                    exchange.getRequest().getURI().getPath(),
                    context.getParameters().size());

            return exchange.mutate().request(newRequest).build();
        }).flatMap(chain::apply).doFinally(
                signalType -> Logger.info(
                        false,
                        "Vortex",
                        "Request processing completed: strategy=request, clientIp={}, path={}, executionTimeMs={}",
                        context.getX_request_ip(),
                        exchange.getRequest().getURI().getPath(),
                        (System.currentTimeMillis() - context.getTimestamp())))
                .onErrorResume(e -> {
                    Logger.error(
                            false,
                            "Vortex",
                            e,
                            "JSON request processing failed: strategy=request, clientIp={}, exception={}",
                            context.getX_request_ip(),
                            e.getClass().getSimpleName());
                    return Mono.error(e);
                });
    }

    /**
     * Determines whether a JSON request uses an array as its root.
     * <p>
     * UTF-8 byte-order marks and leading whitespace are ignored. REST request bodies must use either an object or an
     * array as the root value.
     *
     * @param json JSON request text
     * @return {@code true} for an array root, or {@code false} for an object root
     * @throws ValidateException when the body is empty or uses an unsupported root value
     */
    private boolean isJsonArray(String json) {
        if (StringKit.isBlank(json)) {
            throw new ValidateException(ErrorCode._100302);
        }
        for (int index = 0; index < json.length(); index++) {
            char value = json.charAt(index);
            if (value == '\uFEFF' || Character.isWhitespace(value)) {
                continue;
            }
            if (value == Symbol.C_BRACKET_LEFT) {
                return true;
            }
            if (value == Symbol.C_BRACE_LEFT) {
                return false;
            }
            throw new ValidateException(ErrorCode._100302);
        }
        throw new ValidateException(ErrorCode._100302);
    }

    /**
     * Handles a bounded {@code application/x-www-form-urlencoded} body under the request-byte budget.
     *
     * @param exchange The current server exchange.
     * @param chain    The next strategy in the chain.
     * @param context  The request context.
     * @return A {@code Mono<Void>} that signals the completion of processing.
     */
    private Mono<Void> handleFormRequest(ServerWebExchange exchange, Chain chain, Context context) {
        return Octets
                .readForParsing(
                        exchange.getRequest().getBody(),
                        Math.toIntExact(Holder.get().getMaxBufferedRequestSize()),
                        Holder.requestBufferBudget(),
                        exchange.getRequest().getHeaders().getContentLength())
                .onErrorMap(DataBufferLimitException.class, error -> new ValidateException(ErrorCode._100530))
                .flatMap(body -> processFormData(exchange, chain, context, body).doFinally(signal -> body.close()));
    }

    /**
     * Performs the actual parsing of a form-data request body.
     * <p>
     * This method decorates the consumed request with its exact buffered bytes, then uses
     * {@link ServerWebExchange#getFormData()} to parse parameters into the {@link Context}. The logical-byte lease is
     * retained through the complete downstream lifecycle.
     *
     * @param exchange The current server exchange.
     * @param chain    The next strategy in the chain.
     * @param context  The request context to be populated.
     * @param body     bounded request body and its logical-byte lease
     * @return A {@code Mono<Void>} that signals the completion of processing.
     */
    private Mono<Void> processFormData(
            ServerWebExchange exchange,
            Chain chain,
            Context context,
            Octets.BufferedBody body) {

        return Mono.fromCallable(() -> {
            /**
             * Decorator that exposes the already buffered request body to the form reader.
             * <p>
             * The form reader consumes one wrapped view of the exact array; no second aggregation is performed.
             */
            ServerHttpRequest newRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {

                /**
                 * Returns the cached request body as a Flux of DataBuffer.
                 * <p>
                 * The emitted buffer retains the array after this container drops its own byte references. The
                 * logical-byte lease remains attached to the complete request lifecycle.
                 *
                 * @return A Flux emitting a single DataBuffer containing the cached body
                 */
                @Override
                public Flux<DataBuffer> getBody() {
                    return Flux.defer(() -> Mono.just(exchange.getResponse().bufferFactory().wrap(body.bytes())))
                            .doFinally(signal -> body.discardBytes());
                }
            };
            return exchange.mutate().request(newRequest).build();
        }).flatMap(newExchange -> newExchange.getFormData().flatMap(params -> {
            context.getParameters().putAll(params.toSingleValueMap());
            Logger.debug(
                    true,
                    "Vortex",
                    "Form request parameter snapshot: strategy=request, clientIp={}, path={}",
                    getClientIp(newExchange.getRequest()),
                    newExchange.getRequest().getURI().getPath());
            Logger.debug(
                    true,
                    "Vortex",
                    "Request parameters: strategy=request, clientIp={}, parameters={}",
                    context.getX_request_ip(),
                    context.getParameters());
            Logger.info(
                    true,
                    "Vortex",
                    "Form request processed: strategy=request, clientIp={}, path={}, parameterCount={}",
                    getClientIp(newExchange.getRequest()),
                    newExchange.getRequest().getURI().getPath(),
                    context.getParameters().size());
            return chain.apply(newExchange);
        })).doFinally(
                signalType -> Logger.info(
                        false,
                        "Vortex",
                        "Request processing completed: strategy=request, clientIp={}, path={}, executionTimeMs={}",
                        context.getX_request_ip(),
                        exchange.getRequest().getURI().getPath(),
                        (System.currentTimeMillis() - context.getTimestamp())))
                .onErrorResume(e -> {
                    Logger.error(
                            false,
                            "Vortex",
                            e,
                            "Form request processing failed: strategy=request, clientIp={}, exception={}",
                            context.getX_request_ip(),
                            e.getClass().getSimpleName());
                    return Mono.error(e);
                });
    }

    /**
     * Handles {@code multipart/form-data} requests, typically used for file uploads.
     * <p>
     * This method delegates directly to {@link #processMultipartData(ServerWebExchange, Chain, Context, MultiValueMap)}
     * after using the built-in {@link ServerWebExchange#getMultipartData()} parser.
     * <p>
     * <b>Performance Optimization:</b> Spring WebFlux's getMultipartData() already uses streaming processing for file
     * uploads. File parts are not loaded entirely into memory but are processed as streams. This prevents OOM errors
     * when handling large file uploads.
     * <p>
     * Note: Timeout and retry are handled at VortexHandler level, not here.
     *
     * @param exchange The current server exchange.
     * @param chain    The next strategy in the chain.
     * @param context  The request context.
     * @return A {@code Mono<Void>} that signals the completion of processing.
     */
    private Mono<Void> handleMultipartRequest(ServerWebExchange exchange, Chain chain, Context context) {
        long contentLength = exchange.getRequest().getHeaders().getContentLength();

        if (contentLength > 0) {
            Logger.info(
                    true,
                    "Vortex",
                    "Multipart request detected: strategy=request, clientIp={}, bytes={}, mode=streaming",
                    context.getX_request_ip(),
                    contentLength);
        }

        return exchange.getMultipartData().flatMap(params -> processMultipartData(exchange, chain, context, params));
    }

    /**
     * Performs the actual processing of multipart form data.
     * <p>
     * This method iterates through the parsed parts, separating them into form fields (which are added to
     * {@link Context#getParameters()}) and file parts (which are added to {@link Context#getFileParts()}).
     *
     * @param exchange The current server exchange.
     * @param chain    The next strategy in the chain.
     * @param context  The request context to be populated.
     * @param params   A {@link MultiValueMap} containing all parsed parts of the multipart request.
     * @return A {@code Mono<Void>} that signals the completion of processing.
     */
    private Mono<Void> processMultipartData(
            ServerWebExchange exchange,
            Chain chain,
            Context context,
            MultiValueMap<String, Part> params) {

        return Mono.fromRunnable(() -> {
            Map<String, String> formMap = new LinkedHashMap<>();
            Map<String, Part> fileMap = new LinkedHashMap<>();

            params.toSingleValueMap().forEach((k, v) -> {
                if (v instanceof FormFieldPart) {
                    formMap.put(k, ((FormFieldPart) v).value());
                } else if (v instanceof FilePart) {
                    fileMap.put(k, v);
                }
            });

            context.getParameters().putAll(formMap);
            context.setFileParts(fileMap);
            Logger.debug(
                    true,
                    "Vortex",
                    "Multipart request parameter snapshot: strategy=request, clientIp={}, path={}",
                    context.getX_request_ip(),
                    exchange.getRequest().getURI().getPath());
            Logger.debug(
                    true,
                    "Vortex",
                    "Request parameters: strategy=request, clientIp={}, parameters={}",
                    context.getX_request_ip(),
                    context.getParameters());
            Logger.debug(
                    true,
                    "Vortex",
                    "Request file fields: strategy=request, clientIp={}, fileFields={}",
                    context.getX_request_ip(),
                    fileMap.keySet());

            Logger.info(
                    true,
                    "Vortex",
                    "Multipart request processed: strategy=request, clientIp={}, path={}, parameterCount={}",
                    context.getX_request_ip(),
                    exchange.getRequest().getURI().getPath(),
                    context.getParameters().size());
        }).then(chain.apply(exchange)).doFinally(
                signalType -> Logger.info(
                        false,
                        "Vortex",
                        "Request processing completed: strategy=request, clientIp={}, path={}, executionTimeMs={}",
                        context.getX_request_ip(),
                        exchange.getRequest().getURI().getPath(),
                        (System.currentTimeMillis() - context.getTimestamp())))
                .onErrorResume(e -> {
                    Logger.error(
                            false,
                            "Vortex",
                            e,
                            "Multipart request processing failed: strategy=request, clientIp={}, exception={}",
                            context.getX_request_ip(),
                            e.getClass().getSimpleName());
                    return Mono.error(e);
                });
    }

}
